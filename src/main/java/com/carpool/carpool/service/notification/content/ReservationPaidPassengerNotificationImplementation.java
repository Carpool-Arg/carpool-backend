package com.carpool.carpool.service.notification.content;

import com.carpool.carpool.dto.notificationPayload.NotificationPayloadDTO;
import com.carpool.carpool.enums.dispatchPolicy.DispatchPolicyEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.service.pdfGenerator.IPdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReservationPaidPassengerNotificationImplementation implements INotificationContentService<Reservation>{

    private final IPdfGeneratorService pdfGeneratorService;

    @Override
    public NotificationEventEnum getEvent() {
        return NotificationEventEnum.RESERVATION_PAID_PASSENGER;
    }

    @Override
    public DispatchPolicyEnum getPolicy() {
        return DispatchPolicyEnum.EMAIL_ONLY;
    }

    @Override
    public NotificationPayloadDTO build(Reservation reservation) {
        byte[] pdfBytes = pdfGeneratorService.generatePaymentReceiptPdf(reservation);
        String firstName = reservation.getUser().getName();
        String lastName = reservation.getUser().getLastname();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));

        String filename = String.format("comprobante_%s-%s_%s.pdf", firstName, lastName, timestamp);

        return NotificationPayloadDTO.builder()
                .emailSubject("Comprobante de pago - Carpool")
                .emailTitle("¡Tu pago fue procesado exitosamente!")
                .emailMessage("Hola " + reservation.getUser().getName()
                        + ", adjuntamos tu comprobante de pago.")
                .emailMessageFooter("Gracias por usar Carpool.")
                .emailAttachmentBytes(pdfBytes)
                .emailAttachmentFilename(filename)
                .build();
    }
}
