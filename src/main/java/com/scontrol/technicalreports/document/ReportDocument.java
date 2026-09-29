package com.scontrol.technicalreports.document;

/**
 * Documento generado listo para descargar: nombre de archivo y contenido binario.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record ReportDocument(String fileName, byte[] content) {
}
