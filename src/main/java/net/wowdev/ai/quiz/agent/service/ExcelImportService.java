package net.wowdev.ai.quiz.agent.service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ExcelImportService {

  private final FlashCardRepository repository;

  @Value("classpath:questions/questions-answers.xlsx")
  private Resource excelFileResouce;

  public ExcelImportService(FlashCardRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  public void importExcelData() {
    try {
      FlashCardEntity flashCard = repository.findRandomFlashcard();
      if (flashCard != null) return; // data has already been imported
      InputStream is = excelFileResouce.getInputStream();
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);

      for (Row row : sheet) {
        if (row.getRowNum() == 0) continue; // Skip header row

        if (isRowEmpty(row)) continue; // discard empty rows

        Cell qCell = row.getCell(1);
        Cell aCell = row.getCell(2);

        if (qCell != null && aCell != null) {
          FlashCardEntity card = new FlashCardEntity();
          card.setQuestion(qCell.getStringCellValue());
          card.setCorrectAnswer(aCell.getStringCellValue());
          repository.save(card);
        }
      }
      System.out.println("Excel Data successfully imported into H2 Database!");
    } catch (Exception e) {
      System.out.println("Error importing Excel Data into H2 Database!");
      e.printStackTrace();
    }
  }

  private static boolean isRowEmpty(Row row) {
    if (row == null) {
      return true;
    }

    Cell cell = row.getCell(1);
    if (cell != null && cell.getCellType() != CellType.BLANK) {
      // If it's a string, make sure it's not just spaces
      return cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty();
    }
    return true;
  }
}
