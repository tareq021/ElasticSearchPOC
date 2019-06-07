package com.esp.elasticsearchpoc;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utilities {

    public List<Document> readExcel() throws IOException {
        String excelFilePath = "books.xlsx";
        FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = firstSheet.iterator();

        List<Document> documents = new ArrayList<>();
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            if (nextRow.getRowNum() == 0) {
                continue;
            }
            Document document = new Document();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                switch (cell.getColumnIndex()) {
                    case 0:
                        document.setId("");
                        break;
                    case 1:
                        document.setCount(determineValueType(cell));
                        break;
                    case 2:
                        document.setIsbn(determineValueType(cell));
                        break;
                    case 3:
                        document.setAuthors(determineValueType(cell));
                        break;
                    case 4:
                        document.setPublishingYear(determineValueType(cell));
                        break;
                    case 5:
                        document.setOriginalTitle(determineValueType(cell));
                        break;
                    case 6:
                        document.setTitle(determineValueType(cell));
                        break;
                    case 7:
                        document.setLanguage(determineValueType(cell));
                        break;
                    case 8:
                        document.setAverageRating(determineValueType(cell));
                        break;
                }
            }
            documents.add(document);
        }

        workbook.close();
        inputStream.close();
        return documents;
    }

    private String determineValueType(Cell cell) {

        String value = "";

        switch (cell.getCellType()) {
            case BLANK:
                value = "";
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case NUMERIC:
                value = String.valueOf(cell.getNumericCellValue());
                break;
        }
        return value;
    }
}
