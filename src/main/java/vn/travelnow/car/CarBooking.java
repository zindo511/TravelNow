package vn.travelnow.car;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.travelnow.shared.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "car_bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tripId;

    @Column(nullable = false)
    private String carType;

    @Column(nullable = false)
    private LocalDate pickupDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status;

    CarBooking(String tripId, String carType, LocalDate pickupDate, LocalDate returnDate, BigDecimal price) {
        this.tripId = tripId;
        this.carType = carType;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.price = price;
        this.status = BookingStatus.BOOKED;
    }

    void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
