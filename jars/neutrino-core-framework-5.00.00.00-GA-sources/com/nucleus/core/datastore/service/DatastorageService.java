/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.datastore.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.geo.Box;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Point;

import com.nucleus.address.Address;

/**
 * @author Nucleus Software India Pvt Ltd 
 * The Interface DatastorageService.
 */
public interface DatastorageService {

    /**
     * Save object.
     *To Save any POJO which Extends to CouchDbDocument of Ektrop in CouchDB
     * @param objectToSave the object to save
     */
    public void saveObject(Object objectToSave);

    /**
     * Removes the object.
     *To Remove any POJO which Extends to CouchDbDocument of Ektrop,which is already saved in CouchDB
     * @param objectToRemove the object to remove
     */
    public void removeObject(Object objectToRemove);

    /**
     * Find object id.
     *
     * @param <T> the generic type
     * @param id the id of the object which need to be retrieved
     * @param entityClass the entity class
     * @return the t
     */
    public <T> T findObjectId(String id, Class<T> entityClass);

    /**
     * Find objects by multiple ids.
     *
     * @param entityClass the entity class
     * @param documentIdList the document id list 
     * @return the list
     */
    public <T> List<T> findObjectsByMultipleIds(Class<T> entityClass, List<String> documentIdList);

    /**
     * Save document.
     *This is used for saving Attachments like PDF,excel etc 
     * @param stream the Inputstream of the attachment to be attached
     * @param AttachmentName the attachment name
     * @param contentType the content type of the Attachmnet
     * @return the string(Id of the Document,which can be used to retrive it delete it )
     * 
     */
    public String saveDocument(InputStream stream, String attachmentName, String contentType);

    /**
     * Save document.
     *This is used for saving Attachments like PDF,excel etc 
     * @param contentInBase64Encoding the String in Base64 encoded format  of the attachment to be attached
     * @param AttachmentName the attachment name
     * @param contentType the content type of the Attachmnet
     * @return the string(Id of the Document,which can be used to retrive it delete it )
     * 
     */
    public String saveDocumentBase64(String contentInBase64Encoding, String attachmentName, String contentType);
    
    /**
     * Overloaded method for save document which also takes the metadata of the document.
     *This is used for saving Attachments like PDF,excel etc 
     * @param stream the Inputstream of the attachment to be attached
     * @param AttachmentName the attachment name
     * @param contentType the content type of the Attachmnet
     * @return the string(Id of the Document,which can be used to retrive it delete it )
     * 
     */
    public String saveDocument(InputStream stream, String attachmentName, String contentType, Map<String, String> metadata);

    /**
     * Overloaded method for save document which also takes the metadata of the document.
     *This is used for saving Attachments like PDF,excel etc 
     * @param contentInBase64Encoding the String in Base64 encoded format  of the attachment to be attached
     * @param AttachmentName the attachment name
     * @param contentType the content type of the Attachmnet
     * @return the string(Id of the Document,which can be used to retrive it delete it )
     * 
     */
    public String saveDocumentBase64(String contentInBase64Encoding, String attachmentName, String contentType, Map<String, String> metadata);


    // change in signature
    /**
     * Gets All the object ids stored in CouchDb database.
     *
     * @param <T> the generic type
     * @param entityClass the entity class
     * @return the object ids
     */
    public <T> List<T> getObjectIds(Class<T> entityClass);

    /**
     * Retrive document if the ID of document is provided,This is the ID which returend when the document is  saved.
     *
     * @param documentId the document id
     * @return the file
     */
    public File retriveDocument(String documentId);

    /*New start*/
    /**
     * Up date document if the ID of document is provided,This is the ID which returend when the document is  saved..
     *
     * @param documentId the document id
     * @param stream the stream
     * @param FileName the file name
     * @param contentType the content type
     */
    public void upDateDocument(String documentId, InputStream stream, String FileName, String contentType);

    /**
     * Removes the document if the ID of document is provided,This is the ID which returend when the document is  saved..
     *
     * @param documentId the document id
     */
    public void removeDocument(String documentId);
    
    /**
     * Pojo based more-efficient implementation of retrieve document. This method doesn't create a temp file and is faster than other implementations
     */
    public DataStoreDocument retrieveDocument(String documentId);
    
    /**
     * Pojo based implementation. Pojo has certain checks which gets trigged at the time of population.. enhancing possibilities of early error detection
     */
    public String saveDocument(DataStoreDocument document);

    /*New End*/

    /*Start Unimplemented methods*/

    /**
     * Removes the Object.if the ID of Object is provided,This is the ID which returend when the Object is  saved.
     *
     * @param <T> the generic type
     * @param id the id
     * @param entityClass the entity class
     */
    public <T> void remove(String id, Class<T> entityClass);

    /**
     * Find object by single criteria.
     *
     * @param <T> the generic type
     * @param key the key
     * @param value the value
     * @param entityClass the entity class
     * @return the t
     */
    public <T> T findObjectBySingleCriteria(String key, String value, Class<T> entityClass);

    /**
     * Update data document.
     *
     * @param <T> the generic type
     * @param keyToData the key to data
     * @param newData the new data
     * @param entityClass the entity class
     */
    public <T> void updateDataDocument(String keyToData, String newData, Class<T> entityClass);

    /**
     * Update data document.
     *
     * @param keyToData the key to data
     * @param newData the new data
     * @param collectionName the collection name
     */
    public void updateDataDocument(String keyToData, String newData, String collectionName);

    /**
     * Find near by area.
     *
     * @param <T> the generic type
     * @param p the p
     * @param d the d
     * @param entityClass the entity class
     * @return the list
     */
    public <T> List<T> findNearByArea(Point p, Distance d, Class<T> entityClass);

    /**
     * Find by position within circle.
     *
     * @param <T> the generic type
     * @param c the c
     * @param entityClass the entity class
     * @return the list
     */
    public <T> List<T> findByPositionWithinCircle(Circle c, Class<T> entityClass);

    /**
     * Find by position within box.
     *
     * @param <T> the generic type
     * @param b the b
     * @param entityClass the entity class
     * @return the list
     */
    public <T> List<T> findByPositionWithinBox(Box b, Class<T> entityClass);

    /**
     * Update address location.
     *
     * @param <T> the generic type
     * @param keyToData the key to data
     * @param updatedLocation the updated location
     * @param entityClass the entity class
     */
    public <T> void updateAddressLocation(String keyToData, double[] updatedLocation, Class<T> entityClass);

    /**
     * Update address location.
     *
     * @param keyToData the key to data
     * @param updatedLocation the updated location
     * @param collectionName the collection name
     */
    public void updateAddressLocation(String keyToData, double[] updatedLocation, String collectionName);

    /**
     * Creates the address.
     *
     * @param address the address
     */
    public void createAddress(Address address);

    // Not Required
    /**
     * Save document with description.
     *
     * @param stream the stream
     * @param contentType the content type
     * @param fileName the file name
     * @param bucket the bucket
     * @return the string
     */
    public String saveDocumentWithDescription(InputStream stream, String contentType, String fileName, String bucket);
    /*End Unimplemented methods*/
    
    byte[] retriveDocumentAsByteArray(String documentId);
    
    DocumentMetaData retrieveDocumentWithMetaData(String documentId);

}
