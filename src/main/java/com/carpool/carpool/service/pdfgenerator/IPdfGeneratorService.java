package com.carpool.carpool.service.pdfgenerator;

import com.carpool.carpool.model.reservation.Reservation;

public interface IPdfGeneratorService {
        byte[] generatePaymentReceiptPdf(Reservation reservation);
    }
