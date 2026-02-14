package com.insurecloud.document;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class DocumentGenerator {

    /**
     * Generates a PDF document for an insurance policy using the provided event details.
     *
     * @param event The policy issued event containing the data for the document.
     * @return An InputStream containing the generated PDF content.
     */
    public InputStream generatePolicyPdf(PolicyIssuedEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();
        document.add(new Paragraph("INSURE CLOUD - POLICY DOCUMENT"));
        document.add(new Paragraph("-------------------------------"));
        document.add(new Paragraph("Policy ID: " + event.policyId()));
        document.add(new Paragraph("Policy Number: " + event.policyNumber()));
        document.add(new Paragraph("Customer ID: " + event.customerId()));
        document.add(new Paragraph("Premium Amount: $" + event.premiumAmount()));
        document.add(new Paragraph("Status: ACTIVE"));
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
