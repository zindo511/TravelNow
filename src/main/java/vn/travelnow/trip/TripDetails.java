package vn.travelnow.trip;

import vn.travelnow.car.CarBooking;
import vn.travelnow.flight.FlightBooking;
import vn.travelnow.hotel.HotelBooking;
import vn.travelnow.payment.Payment;

import java.time.Instant;

/** Ảnh chụp một chuyến đi cùng các thành phần của nó. {@code car} là null nếu không thuê xe. */
public record TripDetails(
        String tripId,
        String userId,
        TripStatus status,
        Instant createdAt,
        HotelBooking hotel,
        FlightBooking flight,
        CarBooking car,
        Payment payment
) {

    static TripDetails of(Trip trip, HotelBooking hotel, FlightBooking flight, CarBooking car, Payment payment) {
        return new TripDetails(trip.getTripId(), trip.getUserId(), trip.getStatus(), trip.getCreatedAt(),
                hotel, flight, car, payment);
    }
}
