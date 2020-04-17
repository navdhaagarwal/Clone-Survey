/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.datastore.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ektorp.Attachment;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.springframework.data.mongodb.core.geo.Box;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Point;

import com.nucleus.address.Address;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@MonitoredWithSpring(name = "couchDataStoreService_IMPL_")
public class CouchDatastoreServiceImpl extends BaseServiceImpl implements DatastorageService {

    /*@Inject
    @Named("couchDbConnector")*/
    private CouchDbConnector db;

    public CouchDatastoreServiceImpl(CouchDbConnector db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getObjectIds(Class<T> entityClass) {
        NeutrinoValidator.notNull(entityClass, "entityClass cannot be null while getiingObjects.");
        return (List<T>) db.getAllDocIds();
    }

    @Override
    public void saveObject(Object objectToSave) {
        NeutrinoValidator.notNull(objectToSave, "Objects to save cannot be null while Saving Objects.");
        db.create(objectToSave);
    }

    @Override
    public void removeObject(Object objectToRemove) {
        NeutrinoValidator.notNull(objectToRemove, "objectToRemove cannot be null while Removing Objects.");
        db.delete(objectToRemove);
    }

    @Override
    public <T> T findObjectId(String id, Class<T> entityClass) {
        NeutrinoValidator.notNull(id, "id cannot be null while finding Objects.");
        NeutrinoValidator.notNull(entityClass, "entityClass cannot be null while finding Objects.");

        return db.get(entityClass, id);
    }

    @Override
    public <T> List<T> findObjectsByMultipleIds(Class<T> entityClass, List<String> documentIdList) {
        NeutrinoValidator.notNull(documentIdList, "document Id List cannot be null while finding Objects.");
        NeutrinoValidator.notNull(entityClass, "entityClass cannot be null while finding Objects.");
        ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(documentIdList);
        List<T> entityList = db.queryView(q, entityClass);
        return entityList;

    }
    
    @Override
    public String saveDocument(InputStream stream, String attachmentName, String contentType, Map<String, String> metadata) {
    	//metadata will be ignored in couch implementation of datastore
    	return this.saveDocument(stream, attachmentName, contentType);
    }
    
    @Override
	public String saveDocument(DataStoreDocument document) {
		return saveDocument(new ByteArrayInputStream(document.getContent()), document.getFileName(), document.getContentType());
	}
    
    @Override
	public DataStoreDocument retrieveDocument(String documentId) {
    	byte [] content = retriveDocumentAsByteArray(documentId);
    	return new DataStoreDocument(content, documentId, "txt");
    }
    
    @Override
    public String saveDocument(InputStream stream, String attachmentName, String contentType) {
        CouchDataEntity couchDataEntity = new CouchDataEntity();
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        NeutrinoValidator.notNull(stream, "InputStream cannot be null while Saving document.");
        NeutrinoValidator.notNull(attachmentName, "AttachmentName cannot be null while Saving document.");
        NeutrinoValidator.notNull(contentType, "Content-Type of the attachment cannot be null while Saving Attachment.");
        String contentInBase64Encoding = null;
        try {
            contentInBase64Encoding = new String(Base64.encodeBase64(IOUtils.toByteArray(stream)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new SystemException("Unable to convert to base64.", e);
        }
        Attachment a = new Attachment(attachmentName, contentInBase64Encoding, contentType);

        couchDataEntity.addInlineAttachment(a);
        couchDbRepository.add(couchDataEntity);
        return couchDataEntity.getId();
    }
    
    @Override
	public String saveDocumentBase64(String contentInBase64Encoding, String attachmentName, String contentType, Map<String, String> metadata){
		//metadata will be ignored in couch implementation of datastore
		return saveDocumentBase64(contentInBase64Encoding, attachmentName, contentType);
	}

    @Override
    public String saveDocumentBase64(String contentInBase64Encoding, String attachmentName, String contentType) {
        CouchDataEntity couchDataEntity = new CouchDataEntity();
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        NeutrinoValidator.notNull(contentInBase64Encoding, "InputString cannot be null while Saving document.");
        boolean isBase64 = Base64.isBase64(contentInBase64Encoding);
        NeutrinoValidator.isTrue(isBase64, "Input String Should Be in Base64 Encoded Format");
        NeutrinoValidator.notNull(attachmentName, "AttachmentName cannot be null while Saving document.");
        NeutrinoValidator.notNull(contentType, "Content-Type of the attachment cannot be null while Saving Attachment.");
        Attachment a = new Attachment(attachmentName, contentInBase64Encoding, contentType);
        couchDataEntity.addInlineAttachment(a);
        couchDbRepository.add(couchDataEntity);
        return couchDataEntity.getId();
    }

    @Override
    public File retriveDocument(String documentId) {
        NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        File file = null;
        if (couchDbRepository.contains(documentId)) {
            CouchDataEntity buk = couchDbRepository.get(documentId);
            Map<String, Attachment> attachmentsMap = buk.getAttachments();
            NeutrinoValidator.isTrue(!attachmentsMap.isEmpty(), "There are no attachments saved");
            Set<String> keyset = attachmentsMap.keySet();

            String attachmentid = null;
            for (String id : keyset) {
                if (id != null) {
                    attachmentid = id;
                    break;
                }
            }
            AttachmentInputStream data = db.getAttachment(documentId, attachmentid);
            try {
                String PREFIX = data.getId();
                String SUFFIX = "." + data.getContentType();
                PREFIX = PREFIX.concat("NTR");
                file = File.createTempFile(PREFIX, SUFFIX);

                // file = new File(data.getId());
                FileOutputStream out = new FileOutputStream(file);
                IOUtils.copy(data, out);
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new SystemException("Exception while retrieving the file", e);
            } finally {
                if (data != null) {
                    try {
                        data.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        throw new SystemException("Exception while retrieving the file", e);
                    }
                }
            }
        }

        return file;
    }
    
    @Override
    public void upDateDocument(String documentId, InputStream stream, String AttachmentName, String contentType) {
        NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
        NeutrinoValidator.notNull(stream, "InputStream cannot be null while Saving document.");
        NeutrinoValidator.notNull(AttachmentName, "AttachmentName cannot be null while Saving document.");
        NeutrinoValidator.notNull(contentType, "Content-Type of the attachment cannot be null while Saving Attachment.");
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        /*Start of Code to Remove Old Attachments*/
        CouchDataEntity buk = null;
        if (couchDbRepository.contains(documentId)) {
            buk = couchDbRepository.get(documentId);
        }
        Map<String, Attachment> attachmentsMap = buk.getAttachments();
        NeutrinoValidator.isTrue(!attachmentsMap.isEmpty(), "There are no attachments saved");
        Collection<Attachment> keyset = attachmentsMap.values();
        Attachment oldSttachment = null;
        for (Attachment attachment : keyset) {
            oldSttachment = attachment;
            break;
        }
        buk.removeAttachment(oldSttachment.getId());

        /*End of Code to Remove Old Attachments*/

        String base64 = null;
        try {
            base64 = new String(Base64.encodeBase64(IOUtils.toByteArray(stream)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new SystemException("Unable to convert to base64.", e);
        }
        Attachment newAttachment = new Attachment(AttachmentName, base64, contentType);
        buk.addInlineAttachment(newAttachment);
        couchDbRepository.update(buk);
    }

    @Override
    public void removeDocument(String documentId) {
        NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        /*Start of Code to Remove Old Attachments*/
        CouchDataEntity buk = null;
        if (couchDbRepository.contains(documentId)) {
            buk = couchDbRepository.get(documentId);
        }
        couchDbRepository.remove(buk);

    }

    @Override
    public byte[] retriveDocumentAsByteArray(String documentId) {
        NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
        CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
        File file = null;
        if (couchDbRepository.contains(documentId)) {
            CouchDataEntity buk = couchDbRepository.get(documentId);
            Map<String, Attachment> attachmentsMap = buk.getAttachments();
            NeutrinoValidator.isTrue(!attachmentsMap.isEmpty(), "There are no attachments saved");
            Set<String> keyset = attachmentsMap.keySet();

            String attachmentid = null;
            for (String id : keyset) {
                if (id != null) {
                    attachmentid = id;
                    break;
                }
            }
            AttachmentInputStream data = db.getAttachment(documentId, attachmentid);

            try {
                return IOUtils.toByteArray(data);
            } catch (IOException e) {
                throw new SystemException(e);
            }

        }
        return null;

    }

    @Override
    public <T> T findObjectBySingleCriteria(String key, String value, Class<T> entityClass) {
        return null;
    }

    @Override
    public <T> void updateDataDocument(String keyToData, String newData, Class<T> entityClass) {
    }

    @Override
    public void updateDataDocument(String keyToData, String newData, String collectionName) {
    }

    @Override
    public <T> List<T> findNearByArea(Point p, Distance d, Class<T> entityClass) {
        return null;
    }

    @Override
    public <T> List<T> findByPositionWithinCircle(Circle c, Class<T> entityClass) {
        return null;
    }

    @Override
    public <T> List<T> findByPositionWithinBox(Box b, Class<T> entityClass) {
        return null;
    }

    @Override
    public <T> void updateAddressLocation(String keyToData, double[] updatedLocation, Class<T> entityClass) {
    }

    @Override
    public void updateAddressLocation(String keyToData, double[] updatedLocation, String collectionName) {
    }

    @Override
    public void createAddress(Address address) {
    }

    @Override
    public <T> void remove(String id, Class<T> entityClass) {
    }

    @Override
    public String saveDocumentWithDescription(InputStream stream, String contentType, String fileName, String bucket) {
        return null;
    }
    
    @Override
    public DocumentMetaData retrieveDocumentWithMetaData(String documentId){
	

    NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
    CouchDbRepository couchDbRepository = new CouchDbRepository(CouchDataEntity.class, db);
    File file = null;
    if (couchDbRepository.contains(documentId)) {
        CouchDataEntity buk = couchDbRepository.get(documentId);
        Map<String, Attachment> attachmentsMap = buk.getAttachments();
        NeutrinoValidator.isTrue(!attachmentsMap.isEmpty(), "There are no attachments saved");
        Set<String> keyset = attachmentsMap.keySet();

        String attachmentid = null;
        for (String id : keyset) {
            if (id != null) {
                attachmentid = id;
                break;
            }
        }
        AttachmentInputStream data = db.getAttachment(documentId, attachmentid);
        attachmentsMap.get(attachmentid).getContentType();
        
        try {
			return new DocumentMetaData(IOUtils.toByteArray(data),attachmentsMap.get(attachmentid).getContentType());
		} catch (IOException e) {
			 throw new SystemException(e);
		}

    }
	return null;
    


}

}
