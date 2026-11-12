package vn.travelnow.car;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarBookingRepository extends JpaRepository<CarBooking, Long> {

    Optional<CarBooking> findByTripId(String tripId);
}
