package net.wowdev.ai.quiz.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExcelImportServiceTest {

  @Mock private FlashCardRepository repository;

  private ExcelImportService service;

  @BeforeEach
  void setUp() {
    service = new ExcelImportService(repository);
  }

  @Test
  void skipsImportWhenDatabaseAlreadyContainsAFlashcard() {
    when(repository.count()).thenReturn(1L);

    service.importExcelData();

    verify(repository, times(1)).count();
    verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void importsNonEmptyRowsAndSkipsInvalidRows() throws Exception {
    ReflectionTestUtils.setField(service, "excelFileResource", resourceWithRows());
    when(repository.count()).thenReturn(0L);

    service.importExcelData();

    var cards = org.mockito.ArgumentCaptor.forClass(FlashCardEntity.class);
    verify(repository, org.mockito.Mockito.times(1)).save(cards.capture());
    assertThat(cards.getValue().getQuestion()).isEqualTo("Question");
    assertThat(cards.getValue().getCorrectAnswer()).isEqualTo("Answer");
  }

  @Test
  void handlesResourceErrorsWithoutThrowing() {
    ReflectionTestUtils.setField(
        service,
        "excelFileResource",
        new ByteArrayResource(new byte[0]) {
          @Override
          public java.io.InputStream getInputStream() throws IOException {
            throw new IOException("broken workbook");
          }
        });
    when(repository.count()).thenReturn(0L);

    service.importExcelData();

    verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void treatsNullRowAsEmpty() throws Exception {
    java.lang.reflect.Method method =
        ExcelImportService.class.getDeclaredMethod(
            "isRowEmpty", org.apache.poi.ss.usermodel.Row.class);
    method.setAccessible(true);

    assertThat(method.invoke(null, (Object) null)).isEqualTo(true);
  }

  private ByteArrayResource resourceWithRows() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet();
      var header = sheet.createRow(0);
      header.createCell(1).setCellValue("Question");
      header.createCell(2).setCellValue("Answer");
      var valid = sheet.createRow(1);
      valid.createCell(1).setCellValue("Question");
      valid.createCell(2).setCellValue("Answer");
      var blank = sheet.createRow(2);
      blank.createCell(1).setCellValue("   ");
      var missingAnswer = sheet.createRow(3);
      missingAnswer.createCell(1).setCellValue("Ignored");
      var nonString = sheet.createRow(4);
      nonString.createCell(1).setCellValue(10);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      workbook.write(output);
      return new ByteArrayResource(output.toByteArray());
    }
  }
}
