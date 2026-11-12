package vn.travelnow.trip;

import java.time.Instant;

/** Một dòng trong danh sách chuyến đi. */
public record TripSummary(String tripId, String userId, TripStatus status, Instant createdAt) {

    static TripSummary of(Trip trip) {
        return new TripSummary(trip.getTripId(), trip.getUserId(), trip.getStatus(), trip.getCreatedAt());
    }
}
