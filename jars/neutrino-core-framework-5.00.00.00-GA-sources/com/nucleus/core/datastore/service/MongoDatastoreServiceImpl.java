/*package com.nucleus.core.datastore.service;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.Box;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Point;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.nucleus.address.Address;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.service.BaseServiceImpl;

@Named("mongoDatastoreService")
public class MongoDatastoreServiceImpl extends BaseServiceImpl implements DatastoreService {

    @Inject
    @Named("mongoTemplate")
    private MongoTemplate             mongoTemplate;

    @Inject
    @Named("clamAVService")
    private ClamAVService             clamAVUtil;

    *//**
       * The Attribute that is used for the search of the geo spatial index
       *//*
    public static final String        GEOSPATIALDATA = "location";

    private final Map<String, GridFS> gridFSCache    = new LinkedHashMap<String, GridFS>();

    @Override
    public <T> List<T> getObjects(Class<T> entityClass) {
        NeutrinoValidator.notNull(entityClass, "While retrieving object list entity class cannot be null.");
        try {
            return mongoTemplate.findAll(entityClass);
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving object list", e);
        }
    }

    @Override
    public void saveObject(Object objectToSave) {
        NeutrinoValidator.notNull(objectToSave, "Object cannot be null while saving.");
        try {
            mongoTemplate.save(objectToSave);
        } catch (Exception e) {
            throw new SystemException("Exception while saving object: " + objectToSave, e);
        }
    }

    @Override
    public void removeObject(Object objectToRemove) {
        NeutrinoValidator.notNull(objectToRemove, "Object cannot be null while removing.");
        try {
            mongoTemplate.remove(objectToRemove);
        } catch (Exception e) {
            throw new SystemException("Exception while removing object: " + objectToRemove, e);
        }
    }

    @Override
    public <T> T findObjectId(String id, Class<T> entityClass) {
        NeutrinoValidator.notNull(id, "Id cannot be null while retrieving object.");
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null while retrieving object.");
        try {
            return mongoTemplate.findById(id, entityClass);
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving object", e);
        }

    }

    @Override
    public String saveDocument(InputStream stream, String bucket) {
        NeutrinoValidator.notNull(stream, "Stream cannot be null while saving document.");

        try {
            String str = IOUtils.toString(stream);
            int res = clamAVUtil.fileScanner(IOUtils.toInputStream(str));

            if (res == ClamAVUtilConstants.FILE_CLEAN || res == ClamAVUtilConstants.PROBLEM_OCCURED
                    || res == ClamAVUtilConstants.SCANNING_DISABLED) {
                GridFS fileNameSpace = getGridFS(bucket);
                GridFSInputFile datastoreFile = fileNameSpace.createFile(IOUtils.toInputStream(str));
                datastoreFile.save();
                return datastoreFile.getId().toString();

            } else {
                throw new Exception("File may contain a virus!!");
            }

        } catch (Exception e) {
            throw new SystemException("Unable to save File.", e);
        }
    }

    @Override
    public File retriveDocument(String documentId, String bucket) {
        NeutrinoValidator.notNull(documentId, "Document id cannot be null while retrieving document.");
        NeutrinoValidator.notNull(bucket, "Namespace(bucket) cannot be null while retrieving document.");
        try {
            GridFS fileNameSpace = getGridFS(bucket);
            GridFSDBFile fileForOutput = fileNameSpace.findOne(new ObjectId(documentId));
            File file = File.createTempFile("nutrino-ds", "tmp");
            fileForOutput.writeTo(file);
            return file;
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving the file", e);
        }
    }

    @Override
    public <T> void remove(String id, Class<T> entityClass) {
        NeutrinoValidator.notNull(id, "Id cannot be null while removing collection.");
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null while removing collection.");
        try {
            Query query = new Query(Criteria.where("id").is(id));
            mongoTemplate.remove(query, entityClass);
        } catch (Exception e) {
            throw new SystemException(String.format("Exception while removing the object with id: %s and class: %s", id,
                    entityClass.getSimpleName()), e);
        }
    }

    @Override
    public <T> T findObjectBySingleCriteria(String key, String value, Class<T> entityClass) {
        return mongoTemplate.findOne(createQuery(key, value), entityClass);
    }

    @Override
    public <T> void updateDataDocument(String keyToData, String newRefId, Class<T> entityClass) {
        updateDataDocument(keyToData, newRefId, mongoTemplate.getCollectionName(entityClass));
    }

    @Override
    public void updateDataDocument(String keyToData, String newRefId, String collectionName) {
        mongoTemplate.upsert(createQuery("uuid", keyToData), Update.update("refId", newRefId), collectionName);
    }

    @Override
    public <T> void updateAddressLocation(String keyToData, double[] updatedLocation, Class<T> entityClass) {
        updateAddressLocation(keyToData, updatedLocation, mongoTemplate.getCollectionName(entityClass));
    }

    @Override
    public void updateAddressLocation(String keyToData, double[] updatedLocation, String collectionName) {
        mongoTemplate.upsert(createQuery("id", keyToData), Update.update("location", updatedLocation), collectionName);
    }

    @Override
    public <T> List<T> findNearByArea(Point point, Distance distance, Class<T> entityClass) {
        NeutrinoValidator.notNull(point, "Point to be searched cannot be null.");
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null while searching the point.");
        mongoTemplate.indexOps(entityClass).ensureIndex(new GeospatialIndex("location"));
        Criteria criteria = new Criteria(GEOSPATIALDATA).near(point).maxDistance(distance.getValue());
        List<T> tracks = mongoTemplate.find(new Query(criteria), entityClass);
        return tracks;
    }

    @Override
    public <T> List<T> findByPositionWithinCircle(Circle circle, Class<T> entityClass) {
        NeutrinoValidator.notNull(circle, "Circle cannot be null.");
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null while searching within circle.");
        mongoTemplate.indexOps(entityClass).ensureIndex(new GeospatialIndex("location"));
        Criteria criteria = new Criteria(GEOSPATIALDATA).within(circle);
        List<T> tracks = mongoTemplate.find(new Query(criteria), entityClass);
        return tracks;
    }

    @Override
    public <T> List<T> findByPositionWithinBox(Box box, Class<T> entityClass) {
        NeutrinoValidator.notNull(box, "Box cannot be null.");
        NeutrinoValidator.notNull(entityClass, "Entity class cannot be null while searching within box.");
        mongoTemplate.indexOps(entityClass).ensureIndex(new GeospatialIndex("location"));
        Criteria criteria = new Criteria(GEOSPATIALDATA).within(box);
        List<T> tracks = mongoTemplate.find(new Query(criteria), entityClass);
        return tracks;
    }

    @Override
    public void createAddress(Address address) {
        if (address == null) {
            throw new InvalidDataException("Address cannot be null");
        }
        entityDao.persist(address);
    }

    private Query createQuery(String key, String value) {
        Criteria criteria = Criteria.where(key).in(value);
        return new Query(criteria);
    }

    private GridFS getGridFS(String bucketName) {
        bucketName = StringUtils.isBlank(bucketName) ? GridFS.DEFAULT_BUCKET : bucketName;
        if (!gridFSCache.containsKey(bucketName)) {
            gridFSCache.put(bucketName, new GridFS(mongoTemplate.getDb(), bucketName));
        }
        return gridFSCache.get(bucketName);
    }

    @Override
    public String saveDocumentWithDescription(InputStream stream, String contentType, String fileName, String bucket) {
        NeutrinoValidator.notNull(stream, "Stream cannot be null while saving document.");
        try {
            String str = IOUtils.toString(stream);

            int res = clamAVUtil.fileScanner(IOUtils.toInputStream(str));

            if (res == ClamAVUtilConstants.FILE_CLEAN || res == ClamAVUtilConstants.PROBLEM_OCCURED
                    || res == ClamAVUtilConstants.SCANNING_DISABLED) {
                GridFS fileNameSpace = getGridFS(bucket);
                GridFSInputFile datastoreFile = fileNameSpace.createFile(IOUtils.toInputStream(str));
                datastoreFile.setContentType(contentType);
                datastoreFile.setFilename(fileName);
                datastoreFile.save();
                return datastoreFile.getId().toString();

            } else {
                throw new Exception("File may contain a virus!!");
            }

        } catch (Exception e) {
            throw new SystemException("Unable to save File.", e);
        }
    }

}*/