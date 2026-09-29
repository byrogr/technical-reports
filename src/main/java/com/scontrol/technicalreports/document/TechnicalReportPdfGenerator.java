package com.scontrol.technicalreports.document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.scontrol.technicalreports.dto.CatalogResponse;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;

/**
 * Genera el PDF de un informe técnico con el mismo layout que la plantilla Excel original:
 * panel azul con etiquetas en negrita y los datos en cajas blancas.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Component
public class TechnicalReportPdfGenerator {

    private static final String LOGO_PATH = "document/scontrol-logo.png";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Colores de la plantilla: acento azul del tema de Excel aclarado un 40 %
    private static final Color PANEL = new Color(0x8F, 0xAA, 0xDC);
    private static final Color BOX = Color.WHITE;
    private static final Color MUTED = new Color(0x59, 0x59, 0x59);

    private static final Font TITLE = new Font(Font.HELVETICA, 20, Font.BOLD);
    private static final Font SECTION = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font LABEL = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font VALUE = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font DETAILS = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font FOOTER = new Font(Font.HELVETICA, 7, Font.NORMAL, MUTED);

    // Ancho relativo de las columnas de margen lateral dentro del panel
    private static final float MARGIN = 0.2f;

    private final byte[] logo;

    public TechnicalReportPdfGenerator() {
        try (InputStream in = new ClassPathResource(LOGO_PATH).getInputStream()) {
            this.logo = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar el logo " + LOGO_PATH, e);
        }
    }

    public byte[] generate(TechnicalReportResponse report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.addTitle("Informe Técnico " + report.reportNumber());
        document.addCreator("Scontrol Ingeniería S.A.C.");
        document.open();

        document.add(header(report));
        document.add(equipmentSection(report));
        document.add(workSection(report));
        document.add(signatures(report));

        Paragraph generatedAt = new Paragraph("Generado el " + LocalDateTime.now().format(DATE_TIME), FOOTER);
        generatedAt.setAlignment(Element.ALIGN_RIGHT);
        generatedAt.setSpacingBefore(4);
        document.add(generatedAt);

        document.close();
        return out.toByteArray();
    }

    // Logo | INFORME TÉCNICO | N° 008-0001
    private PdfPTable header(TechnicalReportResponse report) {
        PdfPTable table = table(new float[] {3, 6, 3});

        Image image = loadLogo();
        image.scaleToFit(95, 57);
        PdfPCell logoCell = panelCell();
        logoCell.addElement(image);
        logoCell.setPadding(8);
        table.addCell(logoCell);

        PdfPCell title = panelCell(new Phrase("INFORME TÉCNICO", TITLE), Element.ALIGN_CENTER);
        title.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(title);

        PdfPTable number = table(new float[] {1, 3});
        PdfPCell numberLabel = panelCell(new Phrase("N°", LABEL), Element.ALIGN_RIGHT);
        numberLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        number.addCell(numberLabel);
        number.addCell(boxCell(report.reportNumber(), Element.ALIGN_CENTER));
        PdfPCell numberCell = nestedCell(number);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setPaddingRight(10);
        table.addCell(numberCell);
        return table;
    }

    // Cliente, equipo y N/S a la izquierda; fechas y estado del equipo a la derecha
    private PdfPTable equipmentSection(TechnicalReportResponse report) {
        PdfPTable left = table(new float[] {1.1f, 4});
        addLabelRow(left, "Cliente:", report.clientName());
        addLabelRow(left, "Equipo:", report.equipmentModel());
        addLabelRow(left, "N/S:", report.equipmentSerialNumber());

        PdfPTable right = table(new float[] {1, 1});
        addLabelRow(right, "Fecha y hora inicio:", format(report.startDatetime()));
        addLabelRow(right, "Fecha y hora término:", format(report.endDatetime()));
        right.addCell(spacer(2, 4));
        PdfPCell statusTitle = panelCell(new Phrase("ESTADO DEL EQUIPO", LABEL), Element.ALIGN_CENTER);
        statusTitle.setColspan(2);
        right.addCell(statusTitle);
        right.addCell(panelCell(new Phrase("INICIO", LABEL), Element.ALIGN_CENTER));
        right.addCell(panelCell(new Phrase("TÉRMINO", LABEL), Element.ALIGN_CENTER));
        right.addCell(boxCell(value(report.initialStatus()), Element.ALIGN_CENTER));
        right.addCell(boxCell(value(report.finalStatus()), Element.ALIGN_CENTER));

        PdfPTable table = table(new float[] {MARGIN, 5.4f, 0.3f, 4.3f, MARGIN});
        table.addCell(spacer(1, 0));
        PdfPCell title = panelCell(new Phrase("DETALLE DE EQUIPO", SECTION), Element.ALIGN_LEFT);
        title.setPaddingLeft(0);
        title.setColspan(3);
        title.setPaddingTop(10);
        title.setPaddingBottom(6);
        table.addCell(title);
        table.addCell(spacer(1, 0));
        table.addCell(spacer(1, 0));
        table.addCell(nestedCell(left));
        table.addCell(spacer(1, 0));
        table.addCell(nestedCell(right));
        table.addCell(spacer(1, 0));
        return table;
    }

    // Evento/falla, acción realizada y detalle del trabajo
    private PdfPTable workSection(TechnicalReportResponse report) {
        PdfPTable table = table(new float[] {MARGIN, 10, MARGIN});
        table.setSplitLate(false);
        addRow(table, sectionTitle("EVENTO / FALLA POR REPARACIÓN"));
        addRow(table, boxCell(value(report.eventFailure()), Element.ALIGN_LEFT));
        addRow(table, spacer(1, 3));
        addRow(table, boxCell(report.affectedComponent(), Element.ALIGN_LEFT));
        addRow(table, sectionTitle("ACCIÓN U OPERACIÓN REALIZADA"));
        addRow(table, boxCell(value(report.action()), Element.ALIGN_LEFT));
        addRow(table, sectionTitle("DETALLE:"));

        PdfPCell details = box(new Phrase(report.details(), DETAILS), Element.ALIGN_LEFT);
        details.setVerticalAlignment(Element.ALIGN_TOP);
        details.setMinimumHeight(300);
        details.setPadding(6);
        details.setLeading(0, 1.3f);
        addRow(table, details);
        return table;
    }

    // Firmas: solo el nombre del técnico y del responsable, como en el Excel
    private PdfPTable signatures(TechnicalReportResponse report) {
        PdfPTable table = table(new float[] {0.5f, 4, 1, 4, 0.5f});
        table.setKeepTogether(true);
        table.addCell(spacer(5, 24));
        table.addCell(spacer(1, 0));
        table.addCell(panelCell(new Phrase("Técnico", LABEL), Element.ALIGN_LEFT));
        table.addCell(spacer(1, 0));
        table.addCell(panelCell(new Phrase("Responsable", LABEL), Element.ALIGN_LEFT));
        table.addCell(spacer(1, 0));
        table.addCell(spacer(1, 0));
        table.addCell(boxCell(value(report.personnel()), Element.ALIGN_CENTER));
        table.addCell(spacer(1, 0));
        table.addCell(boxCell(value(report.supervisor()), Element.ALIGN_CENTER));
        table.addCell(spacer(1, 0));
        table.addCell(spacer(5, 12));
        return table;
    }

    // Fila de ancho completo entre las columnas de margen
    private static void addRow(PdfPTable table, PdfPCell cell) {
        table.addCell(spacer(1, 0));
        table.addCell(cell);
        table.addCell(spacer(1, 0));
    }

    private void addLabelRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = panelCell(new Phrase(label, LABEL), Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(labelCell);
        table.addCell(boxCell(value, Element.ALIGN_LEFT));
        table.addCell(spacer(2, 4));
    }

    private static PdfPTable table(float[] widths) {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        return table;
    }

    private static PdfPCell sectionTitle(String text) {
        PdfPCell cell = panelCell(new Phrase(text, LABEL), Element.ALIGN_LEFT);
        cell.setPaddingLeft(0);
        cell.setPaddingTop(10);
        cell.setPaddingBottom(4);
        return cell;
    }

    private static PdfPCell panelCell() {
        PdfPCell cell = new PdfPCell();
        paint(cell);
        return cell;
    }

    private static PdfPCell panelCell(Phrase phrase, int alignment) {
        PdfPCell cell = new PdfPCell(phrase);
        paint(cell);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingLeft(0);
        cell.setPaddingRight(4);
        return cell;
    }

    private static PdfPCell nestedCell(PdfPTable nested) {
        PdfPCell cell = new PdfPCell(nested);
        paint(cell);
        cell.setPadding(0);
        return cell;
    }

    // El borde del mismo color que el panel tapa las líneas claras que dejan los visores de PDF
    // entre fondos de celdas contiguas
    private static void paint(PdfPCell cell) {
        cell.setBackgroundColor(PANEL);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(PANEL);
        cell.setBorderWidth(0.6f);
    }

    private static PdfPCell spacer(int colspan, float height) {
        PdfPCell cell = panelCell();
        cell.setColspan(colspan);
        cell.setFixedHeight(height);
        return cell;
    }

    private static PdfPCell boxCell(String text, int alignment) {
        return box(new Phrase(text == null ? "" : text, VALUE), alignment);
    }

    // Caja blanca de la plantilla: borde fino y borde inferior más grueso
    private static PdfPCell box(Phrase phrase, int alignment) {
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBackgroundColor(BOX);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderWidth(0.5f);
        cell.setBorderWidthBottom(1.2f);
        cell.setMinimumHeight(16);
        cell.setPadding(4);
        return cell;
    }

    private Image loadLogo() {
        try {
            return Image.getInstance(logo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String value(CatalogResponse option) {
        return option == null ? null : option.value();
    }

    private static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME);
    }
}
