/*
 * Copyright 2008-2010 Xebia and the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleus.core.util.security.cert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.joda.time.DateTime;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.util.security.cert.KeyAndCertificateHolder.Builder;

public class NeutrinoX509CertificateGenerator {

    private static final String STORE_TYPE_JKS    = "JKS";
    private static final String STORE_TYPE_PKCS12 = "PKCS12";
    private static final String CERT_SIGN_ALGO    = "SHA1WithRSAEncryption";

    static {
        // adds the Bouncy castle provider to java security
        Security.addProvider(new BouncyCastleProvider());
    }

    @SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
    public static KeyAndCertificateHolder generateIssuerSignedX509Certificate(IssuerInfo issuerInfo,
            SubjectInfo subjectInfo, StoreType storeType) {

        try {
            DateTime validityBeginDate = subjectInfo.getCertificateIssueDate();
            DateTime validityEndDate = subjectInfo.getCertificateExpirationDate();

            // GENERATE THE PUBLIC/PRIVATE RSA KEY PAIR
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048, new SecureRandom());

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // GENERATE THE X509 CERTIFICATE
            X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
            //
            // subject name table.
            //
            Hashtable attrs = new Hashtable();
            Vector order = new Vector();

            if (StringUtils.isNoneBlank(subjectInfo.getUid())) {
                attrs.put(X509Principal.UID, subjectInfo.getUid());
                order.addElement(X509Principal.UID);
            }
            // orgnization
            if (StringUtils.isNoneBlank(subjectInfo.getOrganizationName())) {
                attrs.put(X509Principal.O, subjectInfo.getOrganizationName());
                order.addElement(X509Principal.O);
            }
            if (StringUtils.isNoneBlank(subjectInfo.getOrganizationalUnitName())) {
                attrs.put(X509Principal.OU, subjectInfo.getOrganizationalUnitName());
                order.addElement(X509Principal.OU);
            }
            // address
            if (StringUtils.isNoneBlank(subjectInfo.getLocalityName())) {
                attrs.put(X509Principal.L, subjectInfo.getLocalityName());
                order.addElement(X509Principal.L);
            }
            if (StringUtils.isNoneBlank(subjectInfo.getStateOrProvinceName())) {
                attrs.put(X509Principal.ST, subjectInfo.getStateOrProvinceName());
                order.addElement(X509Principal.ST);
            }
            if (StringUtils.isNoneBlank(subjectInfo.getStreetAddress())) {
                attrs.put(X509Principal.STREET, subjectInfo.getStreetAddress());
                order.addElement(X509Principal.STREET);
            }
            if (StringUtils.isNoneBlank(subjectInfo.getCountryCode())) {
                attrs.put(X509Principal.C, subjectInfo.getCountryCode());
                order.addElement(X509Principal.C);
            }
            if (StringUtils.isNoneBlank(subjectInfo.getEmailAddress())) {
                attrs.put(X509Principal.EmailAddress, subjectInfo.getEmailAddress());
                order.addElement(X509Principal.EmailAddress);
            }

            //
            // create the certificate - version 3
            //
            v3CertGen.reset();
            BigInteger certSerialNumber = getCertSerialNumber();
            v3CertGen.setSerialNumber(certSerialNumber);
            v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(issuerInfo.getCertificate()));
            v3CertGen.setNotBefore(validityBeginDate.toDate());
            v3CertGen.setNotAfter(validityEndDate.toDate());
            v3CertGen.setSubjectDN(new X509Principal(order, attrs));
            v3CertGen.setPublicKey(keyPair.getPublic());
            v3CertGen.setSignatureAlgorithm(CERT_SIGN_ALGO);

            //
            // add the extensions
            //
            v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
                    new SubjectKeyIdentifierStructure(keyPair.getPublic()));

            v3CertGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(
                    issuerInfo.getPublicKey()));

            X509Certificate cert = v3CertGen.generate(issuerInfo.getPrivateKey(), "BC");

            // certificate verification-->OPTIONAL
            cert.checkValidity(new Date());
            cert.verify(issuerInfo.getPublicKey());

            // packing of key and certificate(full chain) to export
            KeyStore store = KeyStore.getInstance(storeType.name());
            store.load(null, null);

            // random generation
            char[] storePassword = getStorePassword();
            String keyAlias = getKeyAlias();
            char[] keyPassword = getKeyPassword();

            // private key is given an alias and a password
            store.setKeyEntry(keyAlias, keyPair.getPrivate(), keyPassword,
                    new Certificate[] { cert, issuerInfo.getCertificate() });
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // storage file also given a password
            store.store(outputStream, storePassword);

            KeyAndCertificateHolder certificateHolder = new KeyAndCertificateHolder.Builder().keyAlias(keyAlias)
                    .keyPassword(keyPassword).storePassword(storePassword)
                    .storeFileName(getStoreFileName(storeType.getFileExtension())).storeType(storeType)
                    .keyCertificateStore(outputStream.toByteArray()).certSerialNumber(certSerialNumber).build();
            if (cert.getSubjectDN() != null) {
                certificateHolder.setSubjectDN(cert.getSubjectDN().toString());
            }
            return certificateHolder;
        } catch (Exception e) {
            throw new SystemException("Error in generating certificate.", e);
        }

    }

    // private helpers
    private static BigInteger getCertSerialNumber() {
        return new BigInteger(RandomStringUtils.randomNumeric(20));
    }

    private static String getStoreFileName(String extension) {
        return "Keycert_".concat(RandomStringUtils.randomAlphabetic(15)).concat(".").concat(extension);
    }

    private static String getKeyAlias() {
        return RandomStringUtils.randomAlphabetic(20);
    }

    private static char[] getKeyPassword() {
        return RandomStringUtils.randomAlphanumeric(30).toCharArray();
    }

    private static char[] getStorePassword() {
        return RandomStringUtils.randomAlphanumeric(30).toCharArray();
    }

}
