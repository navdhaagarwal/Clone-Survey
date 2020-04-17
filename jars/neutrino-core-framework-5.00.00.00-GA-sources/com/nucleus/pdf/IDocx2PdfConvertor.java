package com.nucleus.pdf;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by gajendra.jatav on 5/27/2019.
 */
public interface IDocx2PdfConvertor {

    public byte[] convert(byte[] docxData) throws IOException;

    public byte[] convert(InputStream docxDataStream) throws IOException;

}
