package vn.travelnow.trip;

public class TripNotFoundException extends RuntimeException {

    public TripNotFoundException(String tripId) {
        super("Không tìm thấy chuyến đi " + tripId);
    }
}
