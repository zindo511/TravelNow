package vn.travelnow.flight;

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
@Table(name = "flight_bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tripId;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private LocalDate arrivalDate;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status;

    FlightBooking(String tripId, String origin, String destination,
                  LocalDate departureDate, LocalDate arrivalDate, BigDecimal price) {
        this.tripId = tripId;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.price = price;
        this.status = BookingStatus.BOOKED;
    }

    void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
}
