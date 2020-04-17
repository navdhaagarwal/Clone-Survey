package com.nucleus.core.datastore.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;

import com.ibm.icu.util.Calendar;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDaoImpl;

@Named("documentDataStoreDao")
public class DocumentDataStoreDaoImpl extends EntityDaoImpl implements DocumentDataStoreDao {

	@Override
    public String saveDocument(InputStream inputStream, String attachmentName, String contentType) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Excption occured while saving document in database:" + e.getMessage());
            throw new SystemException("Excption occured while saving document in database:", e);
        }

        return saveDocument(outputStream, attachmentName, contentType);
    }

    @Override
    public String saveDocument(ByteArrayOutputStream outputStream, String attachmentName, String contentType) {
        Session session = (Session) getEntityManager().getDelegate();

        Blob newContent = session.getLobHelper().createBlob(outputStream.toByteArray());
        OracleDocumentEntity documentEntity = new OracleDocumentEntity(attachmentName, attachmentName, newContent,
                contentType, Calendar.getInstance().getTime());
        persist(documentEntity);
        return documentEntity.getId().toString();
    }

    @Override
    public <T> T findObjectId(String id, Class<T> entityClass) {
        return getEntityManager().find(entityClass, new Long(id));
    }

    @Override
    public void removeDocument(String id) {
        delete(find(OracleDocumentEntity.class,Long.valueOf(id)));
    }

}
