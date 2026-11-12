package vn.travelnow.trip;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.travelnow.shared.BookingFailedException;

/** Ánh xạ lỗi nghiệp vụ sang HTTP status, trả về dạng RFC 9457 problem details. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TripNotFoundException.class)
    ProblemDetail notFound(TripNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicateTripException.class)
    ProblemDetail duplicate(DuplicateTripException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidTripStateException.class)
    ProblemDetail invalidState(InvalidTripStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concurrentUpdate(ObjectOptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Chuyến đi vừa bị thay đổi bởi một yêu cầu khác; hãy tra cứu lại trạng thái");
    }

    /** Đã biết thất bại: transaction đã rollback, không có gì được lưu lại. */
    @ExceptionHandler(BookingFailedException.class)
    ProblemDetail bookingFailed(BookingFailedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problem.setTitle("Đặt chuyến đi thất bại");
        problem.setProperty("failedStep", e.getStep());
        return problem;
    }
}
