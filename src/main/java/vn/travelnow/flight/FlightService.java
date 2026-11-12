package vn.travelnow.flight;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.travelnow.shared.BookingFailedException;
import vn.travelnow.shared.BookingStep;

import java.math.BigDecimal;
import java.util.Optional;

/** Hãng bay giả lập: "đặt vé" chỉ là một thao tác ghi database. */
@Service
@RequiredArgsConstructor
public class FlightService {

    private static final BigDecimal TICKET_PRICE = new BigDecimal("1800000");

    private final FlightBookingRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public FlightBooking book(String tripId, FlightRequest request) {
        if (request.arrivalDate().isBefore(request.departureDate())) {
            throw new BookingFailedException(BookingStep.FLIGHT, "Ngày đến không được trước ngày đi");
        }
        return repository.save(new FlightBooking(
                tripId, request.origin(), request.destination(),
                request.departureDate(), request.arrivalDate(), TICKET_PRICE));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancel(String tripId) {
        repository.findByTripId(tripId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vé máy bay của chuyến đi " + tripId))
                .cancel();
    }

    @Transactional(readOnly = true)
    public Optional<FlightBooking> findByTripId(String tripId) {
        return repository.findByTripId(tripId);
    }
}
