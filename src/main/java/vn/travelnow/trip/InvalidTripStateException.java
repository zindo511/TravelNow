package vn.travelnow.trip;

public class InvalidTripStateException extends RuntimeException {

    public InvalidTripStateException(String message) {
        super(message);
    }
}
