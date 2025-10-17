package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Export Service
 * Handles PDF and Excel export functionality
 */
public class ExportService {

    /**
     * Export product comparison to PDF
     */
    public boolean exportToPDF(List<ProductDetail> productDetails, String productName, File outputFile) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Product Comparison Report");
                contentStream.endText();

                // Product name
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Product: " + productName);
                contentStream.endText();

                // Draw line
                contentStream.moveTo(50, 710);
                contentStream.lineTo(550, 710);
                contentStream.stroke();

                // Product details
                float yPosition = 680;
                for (ProductDetail detail : productDetails) {
                    if (yPosition < 100) {
                        // Add new page if needed
                        contentStream.close();
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        PDPageContentStream newStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                    }

                    // Platform header
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Platform: " + detail.getPlatform());
                    contentStream.endText();
                    yPosition -= 20;

                    // Details
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

                    String[] lines = {
                            "Price: Rs." + String.format("%.2f", detail.getPrice()),
                            "Rating: " + String.format("%.1f", detail.getRating()) + "/5",
                            "Seller: " + detail.getSeller(),
                            "Delivery: " + detail.getDeliveryTime(),
                            "Return Policy: " + detail.getReturnPolicy(),
                            "Warranty: " + detail.getWarranty(),
                            "Offers: " + detail.getOffers()
                    };

                    for (String line : lines) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(70, yPosition);
                        contentStream.showText(line);
                        contentStream.endText();
                        yPosition -= 15;
                    }

                    yPosition -= 10; // Extra space between platforms
                }
            }

            document.save(outputFile);
            System.out.println("PDF exported successfully: " + outputFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            System.err.println("PDF export failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Export product comparison to Excel
     */
    public boolean exportToExcel(List<ProductDetail> productDetails, String productName, File outputFile) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Comparison");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Product Comparison: " + productName);
            titleCell.setCellStyle(headerStyle);

            // Header row
            Row headerRow = sheet.createRow(2);
            String[] headers = {"Platform", "Price (₹)", "Rating", "Seller", "Delivery",
                    "Return Policy", "Warranty", "Offers", "Link"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 3;
            for (ProductDetail detail : productDetails) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(detail.getPlatform());
                row.createCell(1).setCellValue(detail.getPrice());
                row.createCell(2).setCellValue(detail.getRating());
                row.createCell(3).setCellValue(detail.getSeller());
                row.createCell(4).setCellValue(detail.getDeliveryTime());
                row.createCell(5).setCellValue(detail.getReturnPolicy());
                row.createCell(6).setCellValue(detail.getWarranty());
                row.createCell(7).setCellValue(detail.getOffers());
                row.createCell(8).setCellValue(detail.getProductLink());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel exported successfully: " + outputFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            System.err.println("Excel export failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get default export directory
     */
    public File getDefaultExportDirectory() {
        String userHome = System.getProperty("user.home");
        File exportDir = new File(userHome, "EcommerceAnalyzer_Exports");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        return exportDir;
    }

    /**
     * Generate filename with timestamp
     */
    public String generateFileName(String productName, String extension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedName = productName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitizedName + "_" + timestamp + "." + extension;
    }
}