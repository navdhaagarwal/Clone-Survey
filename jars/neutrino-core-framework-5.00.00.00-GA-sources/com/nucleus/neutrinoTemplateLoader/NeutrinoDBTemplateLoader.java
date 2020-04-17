/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.neutrinoTemplateLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.document.core.entity.Document;
import com.nucleus.document.service.DocumentService;
import com.nucleus.logging.BaseLoggers;

import freemarker.cache.TemplateLoader;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> kamal1 Add documentation to class
 */
@Named("neutrinoTemplateLoader")
public class NeutrinoDBTemplateLoader implements TemplateLoader {

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService couchDatastoreService;

    @Inject
    @Named("documentService")
    DocumentService               documentService;

    /**
     * Returns templateSource which is source of template for given name 
     * @param documentStoreId: documentStoreId is the Couch DB id if the Template to be found
     * @return Document Entity
     */
    @Override
    public Object findTemplateSource(String documentStoreId) throws IOException {
        Locale locale = Locale.getDefault();
        String localeName = "_" + locale.toString();
        documentStoreId = documentStoreId.endsWith(localeName) ? documentStoreId.substring(0,
                documentStoreId.lastIndexOf(localeName)) : documentStoreId;

        Document document = documentService.getDocumentByDocumentStoreId(documentStoreId);
        return document;
    }

    /* (non-Javadoc) @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object) */
    @Override
    public long getLastModified(final Object templateSource) {
        long lastModifiedValue = 0L;
        if (templateSource != null) {
            Document doc = (Document) templateSource;
            if (doc.getEntityLifeCycleData() != null) {
                if (doc.getEntityLifeCycleData().getLastUpdatedTimeStamp() != null) {
                    lastModifiedValue = doc.getEntityLifeCycleData().getLastUpdatedTimeStamp().getMillis();
                } else if (doc.getEntityLifeCycleData().getCreationTimeStamp() != null) {
                    lastModifiedValue = doc.getEntityLifeCycleData().getCreationTimeStamp().getMillis();
                }
            }
        }
        return lastModifiedValue;
    }

    /* (non-Javadoc) @see freemarker.cache.TemplateLoader#getReader(java.lang.Object, java.lang.String) */
    @Override
    public Reader getReader(final Object templateSource, final String encoding) throws IOException {
        String documentId = null;
        if (templateSource != null && templateSource instanceof Document) {
            Document document = (Document) templateSource;
            if (document.getDocumentStoreId() != null) {
                documentId = document.getDocumentStoreId();
            }
        }
        Reader reader = null;
        File document = couchDatastoreService.retriveDocument(documentId);
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(document), encoding))) {
            String documentLine = null;
			StringBuilder builder = new StringBuilder();
			
			//Store the contents of the file to the StringBuilder.
			while((documentLine = bufferedReader.readLine()) != null)
				builder.append(documentLine);
			
			//Create a new tokenizer based on the StringReader class instance.
			reader = new StringReader(builder.toString());
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(
                    "Exception occured while creating Reader Object for Template reading in NeutrinoDBTemplateLoader: ",e);
        }
        return reader;
    }

    /* (non-Javadoc) @see freemarker.cache.TemplateLoader#closeTemplateSource(java.lang.Object) */
    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }

}
