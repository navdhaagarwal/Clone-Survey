package com.nucleus.xml.parser;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.xml.document.PoolableDocumentBuilderFactoryImpl;
import com.nucleus.xml.pooling.ObjectPool;
import com.nucleus.xml.pooling.ObjectPoolingClaimBasedImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;

/**
 * Created by gajendra.jatav on 5/7/2019.
 */

@Component
public class XmlParserConfigurer {

    private Integer queueSize;

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    private static boolean poolingSupported = false;

    @Value(value = "#{'${xml.parser.pool.size}'}")
    public void setParamEncryptionEnabled(String xmlParserPoolSize) {
        if (StringUtils.isEmpty(xmlParserPoolSize)
                || "${xml.parser.pool.size}".equalsIgnoreCase(xmlParserPoolSize)) {
            this.queueSize = 20000;
            return;
        }
        this.queueSize = Integer.valueOf(xmlParserPoolSize);
    }


    @PostConstruct
    public void init() {

        try {

            Class aClass = Class.forName("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            if (aClass != null) {
                ObjectPool<DocumentBuilder, PoolableDocumentBuilderFactoryImpl> documentBuilderFactory =
                        new ObjectPoolingClaimBasedImpl<>(queueSize, 20);
                PoolableDocumentBuilderFactoryImpl.setObjectPool(documentBuilderFactory);
                ObjectPool<SAXParser, PoolableSAXParserFactoryImpl> xmlPareserPool =
                        new ObjectPoolingClaimBasedImpl<>(queueSize, 20);
                PoolableSAXParserFactoryImpl.setObjectPool(xmlPareserPool);
                poolingSupported = true;
            }
        } catch (ClassNotFoundException e) {
            //Not Open/ORACLE jdk
        }

    }

    public static void enablePooling() {

        if (poolingSupported) {
            try {
                PoolableDocumentBuilderFactoryImpl.enablePooling();
                PoolableSAXParserFactoryImpl.enablePooling();
            } catch (Exception e) {
                BaseLoggers.flowLogger.debug("Error while loading PoolableDocumentBuilderFactoryImpl",e);
            }

        }
    }

    public static void releaseObjects() {
        if (poolingSupported) {
            try {
                PoolableDocumentBuilderFactoryImpl.releaseObjects();
                PoolableSAXParserFactoryImpl.releaseParser();
            } catch (Exception e) {
                BaseLoggers.flowLogger.debug("Error while loading PoolableDocumentBuilderFactoryImpl",e);
            }
        }
    }


}
