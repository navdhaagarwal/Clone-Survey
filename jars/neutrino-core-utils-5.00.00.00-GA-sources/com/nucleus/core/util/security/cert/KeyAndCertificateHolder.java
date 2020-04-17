/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.util.security.cert;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Nucleus Software Exports Limited
 */
public class KeyAndCertificateHolder implements Serializable {

    private static final long serialVersionUID = -9054874693318381980L;

    private byte[]            keyCertificateStore;

    private BigInteger        certSerialNumber;

    private StoreType         storeType;

    private String            storeFileName;

    private char[]            storePassword;
    private String            keyAlias;
    private char[]            keyPassword;

    private String            subjectDN;

    // getters
    public byte[] getKeyCertificateStore() {
        return keyCertificateStore;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public String getStoreFileName() {
        return storeFileName;
    }

    public char[] getStorePassword() {
        return storePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public char[] getKeyPassword() {
        return keyPassword;
    }

    public BigInteger getCertSerialNumber() {
        return certSerialNumber;
    }

    // builder
    public static class Builder {
        private byte[]     keyCertificateStore;
        private BigInteger certSerialNumber;
        private StoreType  storeType;
        private String     storeFileName;
        private char[]     storePassword;
        private String     keyAlias;
        private char[]     keyPassword;

        public Builder keyCertificateStore(byte[] keyCertificateStore) {
            this.keyCertificateStore = keyCertificateStore;
            return this;
        }

        public Builder certSerialNumber(BigInteger certSerialNumber) {
            this.certSerialNumber = certSerialNumber;
            return this;
        }

        public Builder storeType(StoreType storeType) {
            this.storeType = storeType;
            return this;
        }

        public Builder storeFileName(String storeFileName) {
            this.storeFileName = storeFileName;
            return this;
        }

        public Builder storePassword(char[] storePassword) {
            this.storePassword = storePassword;
            return this;
        }

        public Builder keyAlias(String keyAlias) {
            this.keyAlias = keyAlias;
            return this;
        }

        public Builder keyPassword(char[] keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public KeyAndCertificateHolder build() {
            return new KeyAndCertificateHolder(this);
        }
    }

    private KeyAndCertificateHolder(Builder builder) {
        this.keyCertificateStore = builder.keyCertificateStore;
        this.certSerialNumber = builder.certSerialNumber;
        this.storeType = builder.storeType;
        this.storeFileName = builder.storeFileName;
        this.storePassword = builder.storePassword;
        this.keyAlias = builder.keyAlias;
        this.keyPassword = builder.keyPassword;
    }

    public String getSubjectDN() {
        return subjectDN;
    }

    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

}
