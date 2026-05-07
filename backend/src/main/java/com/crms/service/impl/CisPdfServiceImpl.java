package com.crms.service.impl;

import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.entity.CISReturnLine;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.CisPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CisPdfServiceImpl implements CisPdfService {

    private static final Color HMRC_GREEN = new Color(0, 112, 60);
    private static final Color HEADER_BG   = new Color(240, 247, 243);
    private static final Color ROW_ALT_BG  = new Color(249, 249, 249);
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CISReturnRepository cisReturnRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePaymentDeductionStatement(Long returnId) {
        CISReturn cisReturn = cisReturnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("CISReturn", returnId));

        if (cisReturn.getCisReturnLines() == null || cisReturn.getCisReturnLines().isEmpty()) {
            throw new IllegalStateException("No return lines found for CIS return " + returnId);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageFooter());
            doc.open();

            addHeader(doc, cisReturn);
            addSummaryTable(doc, cisReturn);
            doc.add(Chunk.NEWLINE);
            addLinesTable(doc, cisReturn);
            doc.add(Chunk.NEWLINE);
            addDeclaration(doc, cisReturn);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CIS300 PDF for return " + returnId, e);
        }
    }

    // ── Header block ──────────────────────────────────────────────────────────

    private void addHeader(Document doc, CISReturn r) throws DocumentException {
        Font titleFont  = new Font(Font.HELVETICA, 16, Font.BOLD, HMRC_GREEN);
        Font subFont    = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{60, 40});

        // Left: title
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("CIS300 — Payment & Deduction Statement", titleFont));
        left.addElement(new Paragraph("Construction Industry Scheme", subFont));
        left.addElement(new Paragraph("Tax Month: " + formatTaxMonth(r.getTaxMonth()), normalFont));
        header.addCell(left);

        // Right: meta
        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font metaFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        right.addElement(rightPara("Return ID: " + r.getId(), metaFont));
        right.addElement(rightPara("Status: " + r.getStatus().name(), metaFont));
        if (r.getSubmittedAt() != null) {
            right.addElement(rightPara("Submitted: " + r.getSubmittedAt().format(DISPLAY_DATE), metaFont));
        }
        if (r.getHmrcReceiptRef() != null) {
            right.addElement(rightPara("HMRC Ref: " + r.getHmrcReceiptRef(), metaFont));
        }
        right.addElement(rightPara("Generated: " + LocalDateTime.now().format(DISPLAY_DATE), metaFont));
        header.addCell(right);

        doc.add(header);

        // Divider line
        LineSeparator line = new LineSeparator(1f, 100f, HMRC_GREEN, Element.ALIGN_CENTER, -5);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    // ── Summary totals table ───────────────────────────────────────────────────

    private void addSummaryTable(Document doc, CISReturn r) throws DocumentException {
        BigDecimal totalGross = r.getCisReturnLines().stream()
                .map(CISReturnLine::getGrossPaid).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeduction = r.getCisReturnLines().stream()
                .map(CISReturnLine::getDeduction).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = totalGross.subtract(totalDeduction);

        Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
        Font valueFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);

        PdfPTable t = new PdfPTable(6);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{20, 13, 13, 13, 14, 14});

        addSummaryHeader(t, "Subcontractors");
        addSummaryHeader(t, "Total Gross (£)");
        addSummaryHeader(t, "Total Deduction (£)");
        addSummaryHeader(t, "Total Net (£)");
        addSummaryHeader(t, "CIS Rate");
        addSummaryHeader(t, "Return Status");

        addSummaryCell(t, String.valueOf(r.getCisReturnLines().size()), valueFont, Element.ALIGN_CENTER);
        addSummaryCell(t, formatAmount(totalGross), valueFont, Element.ALIGN_RIGHT);
        addSummaryCell(t, formatAmount(totalDeduction), valueFont, Element.ALIGN_RIGHT);
        addSummaryCell(t, formatAmount(totalNet), valueFont, Element.ALIGN_RIGHT);
        addSummaryCell(t, "Various", valueFont, Element.ALIGN_CENTER);
        addSummaryCell(t, r.getStatus().name(), valueFont, Element.ALIGN_CENTER);

        doc.add(t);
    }

    // ── Per-subcontractor detail table ────────────────────────────────────────

    private void addLinesTable(Document doc, CISReturn r) throws DocumentException {
        Font headFont  = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
        Font bodyFont  = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        Font totalFont = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);

        Paragraph title = new Paragraph("Subcontractor Deduction Schedule", new Font(Font.HELVETICA, 11, Font.BOLD, HMRC_GREEN));
        title.setSpacingBefore(4);
        title.setSpacingAfter(6);
        doc.add(title);

        PdfPTable t = new PdfPTable(6);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{28, 16, 14, 14, 14, 14});

        String[] headers = {"Subcontractor Name", "UTR", "Gross Paid (£)", "CIS Rate (%)", "Deduction (£)", "Net Paid (£)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(HMRC_GREEN);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(Color.WHITE);
            t.addCell(cell);
        }

        BigDecimal sumGross = BigDecimal.ZERO;
        BigDecimal sumDed   = BigDecimal.ZERO;
        BigDecimal sumNet   = BigDecimal.ZERO;

        boolean alt = false;
        for (CISReturnLine line : r.getCisReturnLines()) {
            Color rowBg = alt ? ROW_ALT_BG : Color.WHITE;
            alt = !alt;

            BigDecimal gross = nvl(line.getGrossPaid());
            BigDecimal ded   = nvl(line.getDeduction());
            BigDecimal net   = nvl(line.getNetPaid());
            sumGross = sumGross.add(gross);
            sumDed   = sumDed.add(ded);
            sumNet   = sumNet.add(net);

            String name = line.getSubcontractor() != null ? line.getSubcontractor().getName() : "—";
            String utr  = (line.getSubcontractor() != null && line.getSubcontractor().getUtr() != null)
                    ? line.getSubcontractor().getUtr() : "N/A";
            String rate = line.getCisRate() != null ? line.getCisRate().stripTrailingZeros().toPlainString() + "%" : "20%";

            addLineCell(t, name, bodyFont, Element.ALIGN_LEFT, rowBg);
            addLineCell(t, utr, bodyFont, Element.ALIGN_CENTER, rowBg);
            addLineCell(t, formatAmount(gross), bodyFont, Element.ALIGN_RIGHT, rowBg);
            addLineCell(t, rate, bodyFont, Element.ALIGN_CENTER, rowBg);
            addLineCell(t, formatAmount(ded), bodyFont, Element.ALIGN_RIGHT, rowBg);
            addLineCell(t, formatAmount(net), bodyFont, Element.ALIGN_RIGHT, rowBg);
        }

        // Totals row
        Color totalBg = new Color(230, 243, 236);
        addLineCell(t, "TOTALS", totalFont, Element.ALIGN_LEFT, totalBg);
        addLineCell(t, "", totalFont, Element.ALIGN_CENTER, totalBg);
        addLineCell(t, formatAmount(sumGross), totalFont, Element.ALIGN_RIGHT, totalBg);
        addLineCell(t, "", totalFont, Element.ALIGN_CENTER, totalBg);
        addLineCell(t, formatAmount(sumDed), totalFont, Element.ALIGN_RIGHT, totalBg);
        addLineCell(t, formatAmount(sumNet), totalFont, Element.ALIGN_RIGHT, totalBg);

        doc.add(t);
    }

    // ── Declaration / footer text ─────────────────────────────────────────────

    private void addDeclaration(Document doc, CISReturn r) throws DocumentException {
        Font noteFont = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);
        Paragraph p = new Paragraph(
            "DECLARATION: This return has been completed to the best of the contractor's knowledge and belief " +
            "and is correct and complete. This document is produced by CRMS CI G UK and represents the " +
            "CIS300 monthly return as required under the Construction Industry Scheme (SI 2005/2045). " +
            "Retain this record for a minimum of 3 years from the end of the tax year to which it relates.",
            noteFont);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingBefore(8);
        doc.add(p);

        if (r.getSubmittedAt() != null && r.getHmrcReceiptRef() != null) {
            Paragraph submitted = new Paragraph(
                "Electronically submitted to HMRC on " + r.getSubmittedAt().format(DISPLAY_DATE) +
                " — Receipt reference: " + r.getHmrcReceiptRef(),
                new Font(Font.HELVETICA, 7, Font.BOLD, HMRC_GREEN));
            submitted.setSpacingBefore(4);
            doc.add(submitted);
        }
    }

    // ── Helper utilities ──────────────────────────────────────────────────────

    private String formatTaxMonth(String taxMonth) {
        if (taxMonth == null) return "—";
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(taxMonth);
            return ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.UK)
                    + " " + ym.getYear();
        } catch (Exception e) {
            return taxMonth;
        }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private Paragraph rightPara(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private void addSummaryHeader(PdfPTable t, String text) {
        Font f = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(cell);
    }

    private void addSummaryCell(PdfPTable t, String text, Font f, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        t.addCell(cell);
    }

    private void addLineCell(PdfPTable t, String text, Font f, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(220, 220, 220));
        t.addCell(cell);
    }

    // ── Page footer event handler ─────────────────────────────────────────────

    private static class PageFooter extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(
                "CRMS CI G UK — CIS300 Payment & Deduction Statement — Page " + writer.getPageNumber() +
                " — CONFIDENTIAL",
                footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                (doc.right() - doc.left()) / 2 + doc.leftMargin(),
                doc.bottom() - 10, 0);
        }
    }
}
