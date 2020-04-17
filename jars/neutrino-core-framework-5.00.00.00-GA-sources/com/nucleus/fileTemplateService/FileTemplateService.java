/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.fileTemplateService;

import java.io.InputStream;
import java.util.Map;

import com.nucleus.service.BaseService;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

// TODO: Auto-generated Javadoc
/**
 * The Interface FileTemplateService.
 * @author Nucleus Software India Pvt Ltd
 */
public interface FileTemplateService extends BaseService {

    /**
     * Generate document.
     *
     * @param inputStream the input stream
     * @param key the key
     * @param context the context
     * @param metadata the metadata
     * @return the file
     */
    public Object generateDocument(InputStream inputStream, String key, Map<Object, Object> context, FieldsMetadata metadata);

    /**
     * Generate document from key.
     *
     * @param key the key
     * @param context the context
     * @param metadata the metadata
     * @return the object
     */
    public Object generateDocumentFromKey(String key, Map<Object, Object> context, FieldsMetadata metadata);
}
