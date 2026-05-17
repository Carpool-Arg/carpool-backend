package com.carpool.carpool.service.pdfGenerator;

import com.carpool.carpool.model.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorImplementation implements IPdfGeneratorService{
    private final SpringTemplateEngine templateEngine;

    @Override
    public byte[] generatePaymentReceiptPdf(Reservation reservation) {
        try {
            Context context = new Context();
            String fullName = reservation.getUser().getName() + " " + reservation.getUser().getLastname();
            context.setVariable("passengerName", fullName);
            context.setVariable("total", reservation.getTotal());
            context.setVariable("origin", reservation.getStartCity().getCity().getName());
            context.setVariable("destination", reservation.getDestinationCity().getCity().getName());
            DateTimeFormatter tripDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            context.setVariable("tripDate", reservation.getTrip().getStartTripDateTime().format(tripDateFormatter));
            context.setVariable("transactionId", "TXN-" + System.currentTimeMillis());
            context.setVariable("paymentDate",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            // Renderiza el template a String XHTML
            String html = templateEngine.process("payment-receipt-pdf", context);

            // Convierte XHTML a PDF con Flying Saucer
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF de comprobante: {}", e.getMessage());
            throw new RuntimeException("No se pudo generar el comprobante PDF", e);
        }
    }
}
