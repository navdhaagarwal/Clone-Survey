package com.nucleus.core.datastore.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
/*import org.springframework.data.mongodb.core.geo.Box;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Point;*/
import com.nucleus.address.Address;

public interface DatastoreService {
    
    public <T> List<T> getObjects(Class<T> entityClass);
    
    public void saveObject(Object objectToSave);
    
    public void removeObject(Object objectToRemove);
    
    public <T> void remove(String id, Class<T> entityClass);
    
    public <T> T findObjectId(String id, Class<T> entityClass);
    
    public String saveDocument(InputStream stream,String bucket);
    
    public File retriveDocument(String documentId, String bucket);
    
    public <T> T findObjectBySingleCriteria(String key, String value, Class<T> entityClass);
    
    public <T> void updateDataDocument(String keyToData, String newData, Class<T> entityClass);
    
    public void updateDataDocument(String keyToData, String newData, String collectionName );
    
   // public <T> List<T> findNearByArea(Point p, Distance d, Class<T> entityClass);

   // public <T> List<T> findByPositionWithinCircle(Circle c, Class<T> entityClass);
    
   // public <T> List<T> findByPositionWithinBox(Box b, Class<T> entityClass);
    
    public <T> void updateAddressLocation(String keyToData, double[] updatedLocation, Class<T> entityClass);
    
    public void updateAddressLocation(String keyToData, double[] updatedLocation, String collectionName);
    
    public void createAddress(Address address);
    
    public String saveDocumentWithDescription(InputStream stream, String contentType,
			String fileName, String bucket);
  
    
}

