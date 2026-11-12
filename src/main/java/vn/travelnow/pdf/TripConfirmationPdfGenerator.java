package vn.travelnow.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;
import vn.travelnow.trip.TripDetails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Dựng PDF xác nhận từ dữ liệu chuyến đi đã lưu. */
@Component
public class TripConfirmationPdfGenerator {

    public byte[] generate(TripDetails trip) {
        List<String> lines = new ArrayList<>();
        lines.add("Trip ID: " + trip.tripId());
        lines.add("User: " + trip.userId());
        lines.add("Status: " + trip.status());
        lines.add("Created at: " + trip.createdAt());
        lines.add("");
        lines.add("Hotel: %s, %s -> %s, %s".formatted(trip.hotel().getHotelName(),
                trip.hotel().getCheckIn(), trip.hotel().getCheckOut(), money(trip.hotel().getPrice())));
        lines.add("Flight: %s -> %s, %s -> %s, %s".formatted(trip.flight().getOrigin(),
                trip.flight().getDestination(), trip.flight().getDepartureDate(),
                trip.flight().getArrivalDate(), money(trip.flight().getPrice())));
        if (trip.car() != null) {
            lines.add("Car: %s, %s -> %s, %s".formatted(trip.car().getCarType(),
                    trip.car().getPickupDate(), trip.car().getReturnDate(), money(trip.car().getPrice())));
        }
        lines.add("");
        lines.add("Total paid: " + money(trip.payment().getAmount()));
        lines.add("Payment confirmation: " + trip.payment().getConfirmationCode());

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setLeading(20f);
                content.newLineAtOffset(50, 780);
                content.setFont(bold, 18);
                content.showText("TravelNow - Trip Confirmation");
                content.newLine();
                content.newLine();
                content.setFont(regular, 12);
                for (String line : lines) {
                    content.showText(toAscii(line));
                    content.newLine();
                }
                content.endText();
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Không tạo được PDF cho chuyến đi " + trip.tripId(), e);
        }
    }

    private static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,d VND", amount.longValue());
    }

    /**
     * Font chuẩn của PDF (Helvetica) không có glyph tiếng Việt, nên bỏ dấu trước khi ghi.
     * Muốn giữ dấu thì cần nhúng một font Unicode đi kèm ứng dụng.
     */
    private static String toAscii(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .replaceAll("[^\\x20-\\x7E]", "?");
    }
}
