package com.nucleus.core.datastore.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.logging.BaseLoggers;
import org.springframework.beans.factory.annotation.Value;

@Named("xlsxSanitizer")
public class XLSXSanitizer implements FileSanitizer{
	private static final Map<String, String> sanitizableMimeTypes=Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		
	{
		put("application/x-tika-msoffice","XLSX");
		put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","XLSX");
		put("application/x-tika-ooxml","XLSX");
		put("application/zip","XLSX");
		
	}});

	@Value(value = "${block.useruploaded.maliciouscontent}")
	private boolean  blockUserUploadedMaliciouscontent;

	@Override
	public boolean canSanitize(String mimeType,String extensionType) {
		if (!blockUserUploadedMaliciouscontent){
			return false;
		}

		String value=sanitizableMimeTypes.get(mimeType);
		if(StringUtils.isNotEmpty(value) && value.equals(extensionType))
		{
			return true;
		}
		return false;
	}

	@Override
	public void checkSanity(InputStream stream) {
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(stream);
		} catch (IOException e) {
			closeWorkbookQuietly(workbook);
			throw new ServiceInputException("Workbook could not be read");
		}
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			Sheet sheet = workbook.getSheetAt(i);

			Iterator<Row> iterator = sheet.iterator();

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
						closeWorkbookQuietly(workbook);
						throw new ServiceInputException(
								"File contains formulaType");
					}
				}
			}
		}

	}

	private void closeWorkbookQuietly(Workbook workbook) {
		try {
			if (workbook != null) {
				workbook.close();
			}

		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error("Workbook could not be closed",
					e.fillInStackTrace());
		}
	}
}
