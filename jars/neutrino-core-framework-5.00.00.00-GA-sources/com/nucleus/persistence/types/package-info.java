@org.hibernate.annotations.TypeDefs({ @TypeDef(name = "encryptedString", typeClass = NeutrinoEncryptedStringType.class, parameters = { @Parameter(name = "encryptorRegisteredName", value = "frameworkHibernateStringEncryptor") }) })
package com.nucleus.persistence.types;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;

import com.nucleus.jasypt.hibernate5.type.NeutrinoEncryptedStringType;

