package vn.travelnow.flight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {

    Optional<FlightBooking> findByTripId(String tripId);
}
