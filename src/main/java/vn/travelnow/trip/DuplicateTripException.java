package vn.travelnow.trip;

public class DuplicateTripException extends RuntimeException {

    public DuplicateTripException(String tripId) {
        super("tripId " + tripId + " đã tồn tại");
    }
}
