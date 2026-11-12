package vn.travelnow.trip;

public enum TripStatus {
    /** Trạng thái trung gian bên trong transaction đặt chuyến; không bao giờ được commit. */
    PENDING,
    /** Mọi bước, kể cả thanh toán, đã thành công. */
    BOOKED,
    /** Người dùng chủ động hủy một chuyến đã BOOKED. */
    CANCELLED
}
