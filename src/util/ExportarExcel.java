package util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportarExcel {

    /**
     * Exporta el reporte de Productos Más Vendidos generado por ReporteDAO.
     */
    public static boolean exportarProductosMasVendidos(String rutaArchivo, List<Object[]> datos, String periodo) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos Más Vendidos");

            // Estilos del encabezado
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle bodyStyle = crearEstiloCuerpo(workbook);

            // Fila de título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE PRODUCTOS MÁS VENDIDOS - PERIODO: " + periodo);

            // Encabezados de tabla
            String[] columnas = {"Producto", "Categoría", "Cantidad Vendida", "Total Recaudado (S/)"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenado de filas
            int rowNum = 3;
            for (Object[] fila : datos) {
                Row row = sheet.createRow(rowNum++);

                Cell c1 = row.createCell(0);
                c1.setCellValue(fila[0].toString()); // Producto
                c1.setCellStyle(bodyStyle);

                Cell c2 = row.createCell(1);
                c2.setCellValue(fila[1].toString()); // Categoría
                c2.setCellStyle(bodyStyle);

                Cell c3 = row.createCell(2);
                c3.setCellValue((Integer) fila[2]);  // Cantidad
                c3.setCellStyle(bodyStyle);

                Cell c4 = row.createCell(3);
                c4.setCellValue((Double) fila[3]);   // Recaudado
                c4.setCellStyle(currencyStyle);
            }

            // Autoajuste de columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fileOut = new FileOutputStream(rutaArchivo)) {
                workbook.write(fileOut);
            }
            return true;

        } catch (IOException e) {
            System.err.println("Error al exportar productos más vendidos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exporta el reporte de Métodos de Pago generado por ReporteDAO.
     */
    public static boolean exportarMetodosPago(String rutaArchivo, List<Object[]> datos, String periodo) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Métodos de Pago");

            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle bodyStyle = crearEstiloCuerpo(workbook);

            // Fila de título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE VENTAS POR MÉTODO DE PAGO - PERIODO: " + periodo);

            // Encabezados de tabla
            String[] columnas = {"Método de Pago", "Cantidad de Transacciones", "Total Recaudado (S/)"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenado de filas
            int rowNum = 3;
            for (Object[] fila : datos) {
                Row row = sheet.createRow(rowNum++);

                Cell c1 = row.createCell(0);
                c1.setCellValue(fila[0].toString()); // Método
                c1.setCellStyle(bodyStyle);

                Cell c2 = row.createCell(1);
                c2.setCellValue((Integer) fila[1]);  // Cantidad
                c2.setCellStyle(bodyStyle);

                Cell c3 = row.createCell(2);
                c3.setCellValue((Double) fila[2]);   // Total
                c3.setCellStyle(currencyStyle);
            }

            // Autoajuste de columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(rutaArchivo)) {
                workbook.write(fileOut);
            }
            return true;

        } catch (IOException e) {
            System.err.println("Error al exportar métodos de pago: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // ESTILOS PRIVADOS REUTILIZABLES
    // ============================================================

    private static CellStyle crearEstiloEncabezado(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    private static CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("S/ #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        aplicarBordes(style);
        return style;
    }

    private static CellStyle crearEstiloCuerpo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        aplicarBordes(style);
        return style;
    }

    private static void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}