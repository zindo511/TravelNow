package vn.travelnow.trip;

/** Phát ra bên trong transaction đặt chuyến; listener AFTER_COMMIT chỉ nhận được khi đã commit. */
public record TripBookedEvent(TripDetails trip) {
}
