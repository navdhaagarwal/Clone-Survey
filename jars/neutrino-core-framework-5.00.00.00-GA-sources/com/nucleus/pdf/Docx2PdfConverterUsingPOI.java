package com.nucleus.pdf;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Named;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;


import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Created by gajendra.jatav on 5/27/2019.
 */
@Named("docx2PdfConvertor")
public class Docx2PdfConverterUsingPOI implements IDocx2PdfConvertor{

    @Override
    public byte[] convert(byte[] docxData) throws IOException {
        return convert(new ByteArrayInputStream(docxData));
    }

    @Override
    public byte[] convert(InputStream docxInputStream) throws IOException {
        XWPFDocument document = new XWPFDocument(docxInputStream);
        PdfOptions options = PdfOptions.create();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfConverter.getInstance().convert(document, out, options);
        return out.toByteArray();
    }

}
