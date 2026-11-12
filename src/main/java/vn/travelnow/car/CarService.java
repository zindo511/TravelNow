package vn.travelnow.car;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.travelnow.shared.BookingFailedException;
import vn.travelnow.shared.BookingStep;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Nhà cung cấp xe thuê giả lập. Dịch vụ tùy chọn: chuyến đi có thể không có xe. */
@Service
@RequiredArgsConstructor
public class CarService {

    private static final BigDecimal PRICE_PER_DAY = new BigDecimal("900000");

    private final CarBookingRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public CarBooking book(String tripId, CarRequest request) {
        long days = ChronoUnit.DAYS.between(request.pickupDate(), request.returnDate());
        if (days < 0) {
            throw new BookingFailedException(BookingStep.CAR, "Ngày trả xe không được trước ngày nhận xe");
        }
        // Thuê và trả trong cùng ngày vẫn tính một ngày.
        BigDecimal price = PRICE_PER_DAY.multiply(BigDecimal.valueOf(Math.max(1, days)));
        return repository.save(new CarBooking(
                tripId, request.carType(), request.pickupDate(), request.returnDate(), price));
    }

    /** Hủy xe thuê nếu có; chuyến đi không thuê xe thì không làm gì. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelIfPresent(String tripId) {
        repository.findByTripId(tripId).ifPresent(CarBooking::cancel);
    }

    @Transactional(readOnly = true)
    public Optional<CarBooking> findByTripId(String tripId) {
        return repository.findByTripId(tripId);
    }
}
