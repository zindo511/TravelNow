package vn.travelnow.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {

    Optional<HotelBooking> findByTripId(String tripId);
}
