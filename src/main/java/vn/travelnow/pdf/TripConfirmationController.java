package vn.travelnow.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TripConfirmationController {

    private final TripConfirmationService confirmationService;

    @GetMapping("/api/trips/{tripId}/confirmation")
    public ResponseEntity<byte[]> download(@PathVariable String tripId) {
        byte[] pdf = confirmationService.getConfirmation(tripId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(tripId + ".pdf").build().toString())
                .body(pdf);
    }
}
