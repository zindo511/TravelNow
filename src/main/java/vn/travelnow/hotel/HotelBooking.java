package vn.travelnow.hotel;

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
@Table(name = "hotel_bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotelBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tham chiếu sang Trip bằng id, không dùng quan hệ JPA: ranh giới module lộ rõ.
    @Column(nullable = false, unique = true, length = 64)
    private String tripId;

    @Column(nullable = false)
    private String hotelName;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status;

    HotelBooking(String tripId, String hotelName, LocalDate checkIn, LocalDate checkOut, BigDecimal price) {
        this.tripId = tripId;
        this.hotelName = hotelName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.price = price;
        this.status = BookingStatus.BOOKED;
    }

    void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
