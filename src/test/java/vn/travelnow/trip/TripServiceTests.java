package vn.travelnow.trip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.travelnow.car.CarBookingRepository;
import vn.travelnow.car.CarRequest;
import vn.travelnow.flight.FlightBookingRepository;
import vn.travelnow.flight.FlightRequest;
import vn.travelnow.hotel.HotelBookingRepository;
import vn.travelnow.hotel.HotelRequest;
import vn.travelnow.payment.PaymentRepository;
import vn.travelnow.payment.PaymentStatus;
import vn.travelnow.pdf.PdfStorageService;
import vn.travelnow.shared.BookingFailedException;
import vn.travelnow.shared.BookingStatus;
import vn.travelnow.shared.BookingStep;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Cố ý không @Transactional: các test cần commit/rollback thật để quan sát kết quả.
@SpringBootTest(properties = "travelnow.pdf.dir=build/test-pdfs")
class TripServiceTests {

    private static final LocalDate DEPART = LocalDate.of(2026, 12, 1);
    private static final LocalDate RETURN = LocalDate.of(2026, 12, 4);

    @Autowired TripService tripService;
    @Autowired TripRepository tripRepository;
    @Autowired HotelBookingRepository hotelRepository;
    @Autowired FlightBookingRepository flightRepository;
    @Autowired CarBookingRepository carRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PdfStorageService pdfStorage;

    @Test
    void bookingWithCarCommitsEverythingAndWritesPdf() {
        String tripId = newTripId();

        TripDetails trip = tripService.bookTrip(request(tripId, true, null));

        assertThat(trip.status()).isEqualTo(TripStatus.BOOKED);
        // 3 đêm khách sạn + vé máy bay + 3 ngày thuê xe
        assertThat(trip.payment().getAmount()).isEqualByComparingTo("8100000");

        TripDetails saved = tripService.getTrip(tripId);
        assertThat(saved.status()).isEqualTo(TripStatus.BOOKED);
        assertThat(saved.hotel().getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(saved.flight().getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(saved.car().getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(saved.payment().getStatus()).isEqualTo(PaymentStatus.PAID);

        assertThat(pdfStorage.load(tripId)).hasValueSatisfying(pdf ->
                assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-"));
    }

    @Test
    void carIsOptional() {
        String tripId = newTripId();

        TripDetails trip = tripService.bookTrip(request(tripId, false, null));

        assertThat(trip.status()).isEqualTo(TripStatus.BOOKED);
        assertThat(trip.car()).isNull();
        assertThat(trip.payment().getAmount()).isEqualByComparingTo("5400000");
        assertThat(carRepository.findByTripId(tripId)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(BookingStep.class)
    void failureAtAnyStepRollsBackEverything(BookingStep step) {
        String tripId = newTripId();

        assertThatThrownBy(() -> tripService.bookTrip(request(tripId, true, step)))
                .isInstanceOf(BookingFailedException.class)
                .extracting("step").isEqualTo(step);

        // Không còn gì sót lại — kể cả bản ghi Trip PENDING.
        assertThat(tripRepository.existsById(tripId)).isFalse();
        assertThat(hotelRepository.findByTripId(tripId)).isEmpty();
        assertThat(flightRepository.findByTripId(tripId)).isEmpty();
        assertThat(carRepository.findByTripId(tripId)).isEmpty();
        assertThat(paymentRepository.findByTripId(tripId)).isEmpty();
        assertThat(pdfStorage.load(tripId)).isEmpty();
    }

    @Test
    void invalidDatesFromProviderRollBack() {
        String tripId = newTripId();
        BookTripRequest request = new BookTripRequest(tripId, "user-1",
                new HotelRequest("Da Nang Riverside", RETURN, DEPART),
                new FlightRequest("HAN", "DAD", DEPART, DEPART), null, null);

        assertThatThrownBy(() -> tripService.bookTrip(request))
                .isInstanceOf(BookingFailedException.class);
        assertThat(tripRepository.existsById(tripId)).isFalse();
    }

    @Test
    void duplicateTripIdIsRejectedAndLeavesTheOriginalUntouched() {
        String tripId = newTripId();
        tripService.bookTrip(request(tripId, false, null));

        assertThatThrownBy(() -> tripService.bookTrip(request(tripId, true, null)))
                .isInstanceOf(DuplicateTripException.class);

        assertThat(tripService.getTrip(tripId).car()).isNull();
        assertThat(paymentRepository.findByTripId(tripId).orElseThrow().getAmount())
                .isEqualByComparingTo("5400000");
    }

    @Test
    void cancelRefundsAndCancelsEveryBooking() {
        String tripId = newTripId();
        tripService.bookTrip(request(tripId, true, null));

        TripDetails cancelled = tripService.cancelTrip(tripId);

        assertThat(cancelled.status()).isEqualTo(TripStatus.CANCELLED);
        TripDetails saved = tripService.getTrip(tripId);
        assertThat(saved.status()).isEqualTo(TripStatus.CANCELLED);
        assertThat(saved.payment().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(saved.car().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(saved.hotel().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(saved.flight().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelWorksWithoutCar() {
        String tripId = newTripId();
        tripService.bookTrip(request(tripId, false, null));

        assertThat(tripService.cancelTrip(tripId).status()).isEqualTo(TripStatus.CANCELLED);
    }

    @Test
    void cancellingTwiceIsRejected() {
        String tripId = newTripId();
        tripService.bookTrip(request(tripId, false, null));
        tripService.cancelTrip(tripId);

        assertThatThrownBy(() -> tripService.cancelTrip(tripId))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void unknownTripIsNotFound() {
        assertThatThrownBy(() -> tripService.getTrip("khong-ton-tai"))
                .isInstanceOf(TripNotFoundException.class);
        assertThatThrownBy(() -> tripService.cancelTrip("khong-ton-tai"))
                .isInstanceOf(TripNotFoundException.class);
    }

    private static String newTripId() {
        return "trip-" + UUID.randomUUID();
    }

    private static BookTripRequest request(String tripId, boolean withCar, BookingStep failAt) {
        return new BookTripRequest(tripId, "user-1",
                new HotelRequest("Đà Nẵng Riverside", DEPART, RETURN),
                new FlightRequest("HAN", "DAD", DEPART, DEPART),
                withCar ? new CarRequest("SUV", DEPART, RETURN) : null,
                failAt);
    }
}
