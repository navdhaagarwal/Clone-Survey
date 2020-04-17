package com.nucleus.pdfmerger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PdfMergerUtility {

    public void merge(List<InputStream> sources,FileOutputStream outputStream,String mergedFileTitle, String creator, String subject) throws IOException;
    
}
