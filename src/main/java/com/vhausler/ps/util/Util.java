package com.vhausler.ps.util;

import com.google.gson.Gson;
import com.vhausler.ps.model.Property;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Kitchen sink class for any utility methods.
 */
public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {
        throw new IllegalStateException("Class doesn't support instantiation.");
    }

    /**
     * Creates a {@link Cookie} from a string representation of a json.
     *
     * @param json being parsed as a {@link Cookie}
     * @return {@link Cookie} from a string representation of a json
     */
    public static Cookie getCookie(String json) {
        return new Gson().fromJson(json, Cookie.class);
    }

    /**
     * Creates and saves the Excel file containing all the property values. Excel headers are created from the object field names
     * and property values are taken from the field values themselves.
     * <p>
     * This can be made more abstract in the future. No need to do so, for now.
     *
     * @param properties as data being written into the Excel file
     * @param fileName   of the output file
     * @return Excel file containing all the property values
     */
    @SuppressWarnings("UnusedReturnValue") // future plans
    public static File exportResults(List<Property> properties, String fileName) {
        String[] split = fileName.split("/");
        fileName = split[split.length - 1] + ".xlsx";
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sreality.cz properties");

            // create the header
            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            for (Field declaredField : Property.class.getDeclaredFields()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(declaredField.getName());
            }

            // write the rest of the values
            for (Property property : properties) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                for (Field declaredField : Property.class.getDeclaredFields()) {
                    declaredField.setAccessible(true); // NOSONAR
                    Cell cell = row.createCell(cellIndex++);
                    if (declaredField.getType().isAssignableFrom(Integer.class)) {
                        cell.setCellValue((int) declaredField.get(property));
                    } else {
                        cell.setCellValue(declaredField.get(property).toString());
                    }
                }
            }

            FileOutputStream fos;
            fos = new FileOutputStream(fileName);
            wb.write(fos);
            fos.close();
            return new File(fileName);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return null;
    }
}
