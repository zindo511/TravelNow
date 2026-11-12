package vn.travelnow.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import vn.travelnow.car.CarRequest;
import vn.travelnow.flight.FlightRequest;
import vn.travelnow.hotel.HotelRequest;
import vn.travelnow.shared.BookingStep;

/**
 * @param tripId            mã do client sinh; cũng là tên file PDF nên chỉ cho phép ký tự an toàn
 * @param car               tùy chọn — null thì bỏ qua bước thuê xe
 * @param simulateFailureAt tùy chọn — ép nhà cung cấp giả lập từ chối ở bước này, để quan sát rollback
 */
public record BookTripRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,64}") String tripId,
        @NotBlank String userId,
        @NotNull @Valid HotelRequest hotel,
        @NotNull @Valid FlightRequest flight,
        @Valid CarRequest car,
        BookingStep simulateFailureAt
) {
}
