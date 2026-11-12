package vn.travelnow.shared;

/**
 * Trạng thái của một lần đặt chỗ (khách sạn, chuyến bay, xe).
 * Quy ước giả lập: bản ghi BOOKED nghĩa là "đối tác đã xác nhận giữ chỗ",
 * CANCELLED nghĩa là "đã hủy giữ chỗ với đối tác".
 */
public enum BookingStatus {
    BOOKED,
    CANCELLED
}
