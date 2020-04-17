package com.nucleus.core.datastore.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface DocumentDataStoreDao {

    public String saveDocument(InputStream stream, String attachmentName, String contentType);

    public <T> T findObjectId(String id, Class<T> entityClass);

    void removeDocument(String id);    

    String saveDocument(ByteArrayOutputStream outputStream, String attachmentName, String contentType);

}
