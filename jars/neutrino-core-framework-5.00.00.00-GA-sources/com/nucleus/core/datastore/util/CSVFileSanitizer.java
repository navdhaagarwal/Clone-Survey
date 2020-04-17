package com.nucleus.core.datastore.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Pattern;

import javax.inject.Named;

import au.com.bytecode.opencsv.CSVReader;

import com.nucleus.core.security.BlackListPatternHolder;
import org.springframework.beans.factory.annotation.Value;

@Named("csvSanitizer")
public class CSVFileSanitizer implements FileSanitizer{

	@Value(value = "${block.useruploaded.maliciouscontent}")
	private boolean  blockUserUploadedMaliciouscontent;

	@Override
		public boolean canSanitize(String mimeType,String extensionType) {
			if (!blockUserUploadedMaliciouscontent){
				return false;
			}
			if (extensionType.equals("CSV")) {
				return true;
			}
			return false;
		}

	@Override
	public void checkSanity(InputStream inputStream) {
		String[] values=null;
		try {
			CSVReader csvReader = new CSVReader(new StringReader(
					parseTextFile(inputStream)),',','\"');
			values = csvReader.readNext();
			for (int i = 0; i < values.length; i++) {
				if (values[i].length() > 1) {
					checkSanity(values[i]);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String parseTextFile(InputStream inputStream) {
		StringBuilder stringBuilder = new StringBuilder();
		try {

			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() == 0) {
					continue;
				}
				stringBuilder.append(strLine).append(System.getProperty("line.separator"));

			}
			in.close();
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return stringBuilder.toString();
	}

	private void checkSanity(String value){
		
		for(Pattern pattern :BlackListPatternHolder.getCsvBlackListPattern().values()){
			if(pattern.matcher(value).lookingAt()){
				throw new RuntimeException("CSV content contains formula "+value);
			}
		}
		
	}
}