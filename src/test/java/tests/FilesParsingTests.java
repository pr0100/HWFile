package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FilesParsingTests {

  private ClassLoader cl = FilesParsingTests.class.getClassLoader();

  public File extractFilesWithExtension(String zipFilePath, String outputDir, String fileExtension) throws Exception {
    File dir = new File(outputDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    try (ZipInputStream zis = new ZipInputStream(
        cl.getResourceAsStream(zipFilePath)
    )) {
      ZipEntry entry;

      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();

        if (entryName.endsWith(fileExtension)) {
          File newFile = new File(dir, entryName);
          new File(newFile.getParent()).mkdirs();

          try (FileOutputStream fos = new FileOutputStream(newFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
          return newFile;
        }
        zis.closeEntry();
      }
    }
    return null;
  }


  @Test
  @DisplayName("Проверка автора PDF файла")
  void pdfFileParsingTest() throws Exception {
    File file = extractFilesWithExtension("input/folder1.zip", "output", ".pdf");
    PDF pdf = new PDF(file);
    assertEquals("Stanislav Strelkov", pdf.author);
  }

  @Test
  @DisplayName("Проверка содержимого файла формата xlsx")
  void xlxsFileParsingTest() throws Exception {
    File file = extractFilesWithExtension("input/folder2.zip", "output", ".xlsx");
    XLS xls = new XLS(file);
    String valueAtCell = xls.excel.getSheetAt(1).getRow(2).getCell(4).getStringCellValue();
    int numberOfSheets = xls.excel.getNumberOfSheets();
    assertTrue(valueAtCell.contains("API"));
    assertEquals(2, numberOfSheets);
  }

  @Test
  @DisplayName("Проверка наличия PDF файла внутри архива")
  void zipFilePDFExistenceTest() throws Exception {
    String expectedExtension = ".pdf";
    boolean pdfFound = false;
    try (ZipInputStream zis = new ZipInputStream(
        cl.getResourceAsStream("input/folder1.zip")
    )) {
      ZipEntry entry;

      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();

        if (entryName.endsWith(expectedExtension)) {
          pdfFound = true;
          break;
        }
      }
    }
    assertTrue(pdfFound);
  }


}
