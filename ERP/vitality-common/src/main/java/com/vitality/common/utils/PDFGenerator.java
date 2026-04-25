package com.vitality.common.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vitality.common.dtos.OrderInvoice;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Component
public class PDFGenerator {
    private final TemplateEngine templateEngine;

    public PDFGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateInvoicePdf(OrderInvoice orderInvoice) {
        try {
            Context context = new Context();
            context.setVariable("order", orderInvoice);

            String html = templateEngine.process("orderInvoiceTemplate", context);

            return htmlToPdf(html);

        } catch (Exception e) {
            throw new RuntimeException("Invoice PDF generation failed", e);
        }
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("HTML to PDF conversion failed", e);
        }
    }
}
