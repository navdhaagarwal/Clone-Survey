package com.nucleus.core.misc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.nucleus.logging.BaseLoggers;

public class PropFileSyncUtil {

    public static void syncPropFiles(String baseFilePath, String fileToBeSynchedPath, String newFileCreatedPath) {
        Properties properties1 = new Properties();
        Properties properties2 = new Properties();
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        FileInputStream fileInputStreamToBeSynched=null;
        FileOutputStream fileOutputStream=null;
        FileInputStream fileInputStream =null;
        BufferedReader reader1 = null;
        BufferedWriter writer= null;
        try {
            File baseFile = new File(baseFilePath);
            File fileToBeSynched = new File(fileToBeSynchedPath);
            File newFile = new File(newFileCreatedPath);
            properties1.load(new FileInputStream(baseFile));
            properties2.load(new FileInputStream(fileToBeSynched));
            fileInputStream =new FileInputStream(baseFile);
            fileOutputStream = new FileOutputStream(newFile);
            reader1 = new BufferedReader(new InputStreamReader(new DataInputStream(fileInputStream), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            String indString = "";
            String propertyNameToBeSearched = "";
            while ((indString = reader1.readLine()) != null) {
                try {
                	String[] split1 = indString.split("=");
                    propertyNameToBeSearched = split1[0].trim();
                    fileInputStreamToBeSynched= new FileInputStream(fileToBeSynched);
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(new DataInputStream(fileInputStreamToBeSynched), "UTF-8"));
                    String searchLine = findKey(propertyNameToBeSearched, reader2);
                    reader2.close();
                    if (searchLine == null) {
                        writer.append("******" + propertyNameToBeSearched);
                    } else {
                        writer.append(searchLine);
                    }
                    writer.append(System.getProperty("line.separator"));
                } finally {
                	IOUtils.closeQuietly(fileInputStreamToBeSynched);
                }
            }
            bf.close();
            writer.flush();
            BaseLoggers.eventLogger.info("File:" + "\t" + fileToBeSynched.getName() + "\t" + "is synched with" + "\t"
                    + baseFile.getName() + "\t" + "and new file is created at:" + newFile.getAbsolutePath());
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(fileInputStreamToBeSynched);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(reader1);
            IOUtils.closeQuietly(writer);
        }

    }

    private static String findKey(String propertyNameToBeSearched, BufferedReader reader2) throws
            IOException {
        String splittedLine = "";
        String searchLine = null;
        while ((splittedLine = reader2.readLine()) != null) {

            searchLine = splittedLine;
            String[] split3 = splittedLine.split("=");
            splittedLine = split3[0].trim();
            if ((splittedLine.equals(propertyNameToBeSearched.trim()))) {
                break;
            } else {
                searchLine = null;
                continue;
            }
        }
        return searchLine;
    }

}
