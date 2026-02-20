// Report service
package com.procurement.procurement.service.report;

import com.procurement.procurement.dto.report.ReportRequestDTO;
import com.procurement.procurement.entity.vendor.Vendor;
import com.procurement.procurement.entity.vendor.VendorRating;
import com.procurement.procurement.repository.vendor.VendorRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.pdf.JRPdfExporter;

@Service
public class ReportService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private VendorRepository vendorRepository;

    // ===================== Common Jasper Generator =====================
    public JasperPrint generateReport(String reportPath, List<?> data, Map<String, Object> parameters)
            throws JRException {

        System.err.println(">>> [ReportService] ENTRY: generateReport for: " + reportPath);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        if (parameters == null) {
            parameters = new HashMap<>();
        }

        try {
            System.err.println(">>> [ReportService] STEP 1: Attempting to find resource: classpath:" + reportPath);
            Resource resource = resourceLoader.getResource("classpath:" + reportPath);

            if (!resource.exists()) {
                System.err.println(">>> [ReportService] ERROR: Resource DOES NOT EXIST: classpath:" + reportPath);
                throw new RuntimeException("Report template NOT FOUND at: classpath:" + reportPath);
            }

            System.err.println(">>> [ReportService] STEP 2: Resource found. URI: " + resource.getURI());

            try (InputStream reportStream = resource.getInputStream()) {
                if (reportStream == null) {
                    System.err.println(">>> [ReportService] ERROR: InputStream is null for: " + reportPath);
                    throw new RuntimeException("Resource input stream is NULL for: " + reportPath);
                }

                byte[] bytes = reportStream.readAllBytes();
                System.err.println(">>> [ReportService] STEP 2.1: Read bytes: " + bytes.length);

                if (bytes.length > 0) {
                    String snippet = new String(bytes, 0, Math.min(bytes.length, 100), StandardCharsets.UTF_8);
                    System.err.println(">>> [ReportService] STEP 2.2: Content snippet: "
                            + snippet.replace("\n", " ").replace("\r", " "));
                } else {
                    System.err.println(">>> [ReportService] ERROR: Byte array is EMPTY!");
                }

                System.err.println(">>> [ReportService] STEP 3: Compiling report now from ByteArrayInputStream...");
                try (InputStream bais = new ByteArrayInputStream(bytes)) {
                    JasperReport jasperReport = null;
                    try {
                        jasperReport = JasperCompileManager.compileReport(bais);
                    } catch (Throwable t) {
                        System.err.println(">>> [ReportService] COMPILATION THROWABLE: " + t.getClass().getName()
                                + " - " + t.getMessage());
                        printCauses(t);
                        throw new RuntimeException("Critical failure during Jasper compilation: " + t.getMessage(), t);
                    }

                    System.err.println(">>> [ReportService] STEP 4: Filling report now...");
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

                    System.err.println(">>> [ReportService] SUCCESS: Report filled successfully.");
                    return jasperPrint;
                }
            }
        } catch (IOException e) {
            String stackTrace = getStackTrace(e);
            System.err.println(">>> [ReportService] ERROR: IOException during resource loading: " + stackTrace);
            printCauses(e);
            throw new RuntimeException("Failed to load report template: " + e.getMessage() + "\nStack: " + stackTrace,
                    e);
        } catch (JRException e) {
            String stackTrace = getStackTrace(e);
            System.err
                    .println(">>> [ReportService] ERROR: JRException during report compilation/filling: " + stackTrace);
            printCauses(e);
            throw e;
        } catch (Throwable t) {
            String stackTrace = getStackTrace(t);
            System.err.println(">>> [ReportService] CRITICAL ERROR: Unexpected error type: " + t.getClass().getName()
                    + "\nStack: " + stackTrace);
            printCauses(t);
            throw new RuntimeException(
                    "Unexpected failure in generateReport: " + t.getMessage() + "\nStack: " + stackTrace, t);
        }
    }

    private String getStackTrace(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    private void printCauses(Throwable t) {
        if (t == null)
            return;
        Throwable cause = t.getCause();
        int depth = 1;
        while (cause != null && depth < 10) {
            System.err.println(">>> [ReportService] CAUSE " + depth + ": " + cause.getClass().getName() + " - "
                    + cause.getMessage());
            cause = cause.getCause();
            depth++;
        }
    }

    // ===================== Export PDF =====================
    public byte[] exportReportToPdf(JasperPrint jasperPrint) throws JRException {
        System.err.println(">>> [ReportService] Exporting to PDF using explicit JRPdfExporter...");
        JRPdfExporter exporter = new JRPdfExporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    // ===================== Export Excel =====================
    public byte[] exportReportToExcel(JasperPrint jasperPrint) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    // ===================== Vendor Report Generator =====================
    public byte[] generateVendorReport(ReportRequestDTO request, String format) {
        System.err.println(">>> [ReportService] ENTRY: generateVendorReport with format: " + format);

        try {
            List<Vendor> vendors;
            if (request != null && request.getVendorId() != null) {
                System.err.println(">>> [ReportService] Fetching specific vendor: " + request.getVendorId());
                vendors = vendorRepository.findById(request.getVendorId())
                        .map(List::of)
                        .orElse(List.of());
            } else {
                System.err.println(">>> [ReportService] Fetching all vendors.");
                vendors = vendorRepository.findAll();
            }

            // Transform entities to Map format expected by JRXML
            List<Map<String, Object>> data = vendors.stream().map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vendorName", v.getName());
                map.put("status", v.getStatus());

                // Calculate average rating
                double avgRating = 0.0;
                if (v.getRatings() != null && !v.getRatings().isEmpty()) {
                    avgRating = v.getRatings().stream()
                            .mapToInt(VendorRating::getRating)
                            .average()
                            .orElse(0.0);
                }
                map.put("rating", avgRating);
                return map;
            }).collect(java.util.stream.Collectors.toList());

            System.err.println(">>> [ReportService] Found " + data.size() + " vendors for report.");

            JasperPrint jasperPrint = generateReport(
                    "jasper/vendor_report.jrxml",
                    data,
                    new HashMap<>(Map.of("title", "Vendor Performance Report")));

            if ("excel".equalsIgnoreCase(format)) {
                return exportReportToExcel(jasperPrint);
            }
            return exportReportToPdf(jasperPrint);

        } catch (Throwable t) {
            System.err.println(">>> [ReportService] TOP-LEVEL CATCH: " + t.getMessage());
            printCauses(t);
            throw new RuntimeException("Report generation failed: " + t.getMessage(), t);
        }
    }
}
