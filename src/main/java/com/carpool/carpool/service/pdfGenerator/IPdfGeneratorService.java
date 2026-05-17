package com.carpool.carpool.service.pdfGenerator;

import com.carpool.carpool.model.reservation.Reservation;

public interface IPdfGeneratorService {
        byte[] generatePaymentReceiptPdf(Reservation reservation);
    }
