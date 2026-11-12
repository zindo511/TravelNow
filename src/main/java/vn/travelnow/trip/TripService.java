package vn.travelnow.trip;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.travelnow.car.CarBooking;
import vn.travelnow.car.CarService;
import vn.travelnow.flight.FlightBooking;
import vn.travelnow.flight.FlightService;
import vn.travelnow.hotel.HotelBooking;
import vn.travelnow.hotel.HotelService;
import vn.travelnow.payment.Payment;
import vn.travelnow.payment.PaymentService;
import vn.travelnow.shared.BookingFailedException;
import vn.travelnow.shared.BookingStep;

import java.math.BigDecimal;

/**
 * Điều phối luồng đặt và hủy chuyến đi. Coupling giữa hotel, flight, car, payment
 * nằm lộ ra ở đây: mỗi bước là một lời gọi hàm trong cùng process, và tính nhất quán
 * được bảo đảm bằng một database transaction duy nhất.
 */
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final HotelService hotelService;
    private final FlightService flightService;
    private final CarService carService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher events;

    /**
     * Năm bước nghiệp vụ trong một transaction. Bất kỳ bước nào ném exception thì
     * không thay đổi nào được commit — kể cả bản ghi Trip ở trạng thái PENDING.
     */
    @Transactional
    public TripDetails bookTrip(BookTripRequest request) {
        String tripId = request.tripId();

        // Bước 1 — Tạo chuyến đi (PENDING)
        if (tripRepository.existsById(tripId)) {
            throw new DuplicateTripException(tripId);
        }
        Trip trip;
        try {
            trip = tripRepository.saveAndFlush(Trip.pending(tripId, request.userId()));
        } catch (DataIntegrityViolationException e) {
            // Hai yêu cầu cùng tripId chạy đồng thời: khóa chính của database là chốt chặn cuối.
            throw new DuplicateTripException(tripId);
        }

        // Bước 2 — Đặt khách sạn
        failIfSimulated(request, BookingStep.HOTEL);
        HotelBooking hotel = hotelService.book(tripId, request.hotel());

        // Bước 3 — Đặt chuyến bay
        failIfSimulated(request, BookingStep.FLIGHT);
        FlightBooking flight = flightService.book(tripId, request.flight());

        // Bước 4 — Thuê xe (tùy chọn)
        CarBooking car = null;
        if (request.car() != null) {
            failIfSimulated(request, BookingStep.CAR);
            car = carService.book(tripId, request.car());
        }

        // Bước 5 — Thanh toán cho toàn bộ chuyến đi
        BigDecimal total = hotel.getPrice().add(flight.getPrice());
        if (car != null) {
            total = total.add(car.getPrice());
        }
        failIfSimulated(request, BookingStep.PAYMENT);
        Payment payment = paymentService.charge(tripId, total);

        trip.markBooked();

        TripDetails details = TripDetails.of(trip, hotel, flight, car, payment);
        events.publishEvent(new TripBookedEvent(details));
        return details;
    }

    /**
     * Hủy một chuyến đã BOOKED, theo thứ tự: hoàn tiền → xe → khách sạn → vé máy bay.
     * Các thay đổi giả lập này cũng nằm trong một transaction.
     */
    @Transactional
    public TripDetails cancelTrip(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new TripNotFoundException(tripId));
        if (trip.getStatus() != TripStatus.BOOKED) {
            throw new InvalidTripStateException(
                    "Chỉ hủy được chuyến đi đã BOOKED; chuyến đi %s đang ở trạng thái %s"
                            .formatted(tripId, trip.getStatus()));
        }

        paymentService.refund(tripId);
        carService.cancelIfPresent(tripId);
        hotelService.cancel(tripId);
        flightService.cancel(tripId);

        trip.markCancelled();
        return assemble(trip);
    }

    @Transactional(readOnly = true)
    public TripDetails getTrip(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new TripNotFoundException(tripId));
        return assemble(trip);
    }

    private TripDetails assemble(Trip trip) {
        String tripId = trip.getTripId();
        return TripDetails.of(trip,
                hotelService.findByTripId(tripId).orElse(null),
                flightService.findByTripId(tripId).orElse(null),
                carService.findByTripId(tripId).orElse(null),
                paymentService.findByTripId(tripId).orElse(null));
    }

    private static void failIfSimulated(BookTripRequest request, BookingStep step) {
        if (request.simulateFailureAt() == step) {
            throw new BookingFailedException(step, "Giả lập: nhà cung cấp từ chối ở bước " + step);
        }
    }
}
