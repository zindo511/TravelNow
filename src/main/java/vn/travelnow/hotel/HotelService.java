package vn.travelnow.hotel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.travelnow.shared.BookingFailedException;
import vn.travelnow.shared.BookingStep;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Nhà cung cấp khách sạn giả lập: "giữ phòng" chỉ là một thao tác ghi database.
 * MANDATORY: các thao tác ghi bắt buộc chạy trong transaction của luồng đặt/hủy chuyến đi.
 */
@Service
@RequiredArgsConstructor
public class HotelService {

    private static final BigDecimal PRICE_PER_NIGHT = new BigDecimal("1200000");

    private final HotelBookingRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public HotelBooking book(String tripId, HotelRequest request) {
        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        if (nights < 1) {
            throw new BookingFailedException(BookingStep.HOTEL, "Ngày trả phòng phải sau ngày nhận phòng");
        }
        BigDecimal price = PRICE_PER_NIGHT.multiply(BigDecimal.valueOf(nights));
        return repository.save(new HotelBooking(
                tripId, request.hotelName(), request.checkIn(), request.checkOut(), price));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancel(String tripId) {
        repository.findByTripId(tripId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt phòng của chuyến đi " + tripId))
                .cancel();
    }

    @Transactional(readOnly = true)
    public Optional<HotelBooking> findByTripId(String tripId) {
        return repository.findByTripId(tripId);
    }
}
