package com.srikanta.mypg.helpers.revenue;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.srikanta.mypg.R;
import com.srikanta.mypg.models.TenantRevenueModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RevenuePdfHelper {

    public static void generateAndSavePdf(
            Context context,
            String hostelName,
            String hostelAddress,
            String monthText,
            String monthKey,
            String expected,
            String collected,
            String pending,
            String paidTenants,
            List<TenantRevenueModel> tenants
    ) {

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                    "Revenue_" + monthKey + ".pdf");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/MyPG");

            Uri pdfUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pdfUri = context.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            }

            if (pdfUri == null) {
                throw new Exception("Failed to create PDF file");
            }

            OutputStream outputStream =
                    context.getContentResolver().openOutputStream(pdfUri);

            Document document = new Document(PageSize.A4, 36, 36, 60, 80);


            PdfWriter writer =
                    PdfWriter.getInstance(document, outputStream);

            writer.setPageEvent(new FooterEvent());

            document.open();

            // ================= HEADER =================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{75, 25});

            Font hostelFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font addressFont = new Font(Font.FontFamily.HELVETICA, 10);

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.addElement(new Paragraph(safe(hostelName), hostelFont));
            left.addElement(new Paragraph(safe(hostelAddress), addressFont));

            Bitmap bmp = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.logo
            );
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(90, 90);

            PdfPCell right = new PdfPCell(logo);
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);

            header.addCell(left);
            header.addCell(right);

            document.add(header);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // ================= TITLE =================
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title =
                    new Paragraph("Monthly Revenue Report – " + monthText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // ================= TABLE =================
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{28, 10, 12, 10, 10, 10, 10});

            Font head = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font cell = new Font(Font.FontFamily.HELVETICA, 10);

            addHeader(table, "Tenant", head);
            addHeader(table, "Room", head);
            addHeader(table, "Type", head);
            addHeader(table, "Rent", head);
            addHeader(table, "Due", head);
            addHeader(table, "Deposit", head);
            addHeader(table, "Status", head);

            int newTenants = 0;
            int regularTenants = 0;
            int totalDeposit = 0;

            for (TenantRevenueModel t : tenants) {

                boolean isNew = "NEW".equalsIgnoreCase(t.getTenantType());

                addCell(table, safe(t.getName()), cell);
                addCell(table, String.valueOf(t.getRoomNo()), cell);
                addCell(table, safe(t.getTenantType()), cell);
                addCell(table, "₹" + t.getRentAmount(), cell);
                int due = Math.max(
                        t.getRentAmount() - t.getRentPaidAmount(),
                        0
                );

                addCell(table, "₹" + due, cell);

                addCell(table, "₹" + t.getDepositAmount(), cell);

                PdfPCell statusCell = new PdfPCell(
                        new Phrase(safe(t.getStatus()), cell)
                );
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                statusCell.setPadding(6);
                statusCell.setBackgroundColor(
                        "PAID".equalsIgnoreCase(t.getStatus())
                                ? new BaseColor(200, 230, 201)
                                : new BaseColor(255, 205, 210)
                );
                table.addCell(statusCell);

                if (isNew) {
                    newTenants++;
                    totalDeposit += t.getDepositAmount();
                } else {
                    regularTenants++;
                }
            }

            document.add(table);

            // ================= SUMMARY =================
            PdfPTable summary = new PdfPTable(4);
            summary.setTotalWidth(document.right() - document.left());
            summary.setLockedWidth(true);
            summary.setWidths(new float[]{25, 15, 30, 15});

            Font sh = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font sv = new Font(Font.FontFamily.HELVETICA, 11);

            addSummaryCell(summary, "Tenants Paid", sh);
            addSummaryCell(summary, paidTenants, sv);

            addSummaryCell(summary, "Expected Rent", sh);
            addSummaryCell(summary, expected, sv);

            addSummaryCell(summary, "New Tenants", sh);
            addSummaryCell(summary, String.valueOf(newTenants), sv);

            addSummaryCell(summary, "Collected (Rent)", sh);
            addSummaryCell(summary, collected, sv);

            addSummaryCell(summary, "Regular Tenants", sh);
            addSummaryCell(summary, String.valueOf(regularTenants), sv);

            addSummaryCell(summary, "Total Pending", sh);
            addSummaryCell(summary, pending, sv);

            addSummaryCell(summary, "Total Deposit", sh);
            addSummaryCell(summary, "₹" + totalDeposit, sv);

            summary.writeSelectedRows(
                    0, -1,
                    document.left(),
                    document.bottom() + 150,
                    writer.getDirectContent()
            );

            document.close();

            Toast.makeText(context,
                    "PDF saved in Downloads/MyPG",
                    Toast.LENGTH_LONG).show();

            openPdf(context, pdfUri);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    context,
                    "PDF Error: " + e.getClass().getSimpleName(),
                    Toast.LENGTH_LONG
            ).show();

        }
    }

    // ================= FOOTER =================
    static class FooterEvent extends PdfPageEventHelper {

        Font footerFont =
                new Font(Font.FontFamily.HELVETICA, 9,
                        Font.NORMAL, BaseColor.GRAY);

        String generatedOn =
                new SimpleDateFormat("dd-MM-yyyy HH:mm",
                        Locale.getDefault()).format(new Date());

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            PdfContentByte cb = writer.getDirectContent();

            cb.setColorStroke(BaseColor.LIGHT_GRAY);
            cb.moveTo(document.left(), document.bottom() + 20);
            cb.lineTo(document.right(), document.bottom() + 20);
            cb.stroke();

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_LEFT,
                    new Phrase("Powered by MyPG – Smart Hostel Management",
                            footerFont),
                    document.left(),
                    document.bottom() + 12,
                    0
            );

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Generated on " + generatedOn +
                                    " | Page " + writer.getPageNumber(),
                            footerFont),
                    document.right(),
                    document.bottom() + 12,
                    0
            );
        }
    }

    // ================= HELPERS =================
    private static void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static void addSummaryCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }

    private static String safe(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s;
    }

    private static void openPdf(Context context, Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "No PDF viewer found",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
