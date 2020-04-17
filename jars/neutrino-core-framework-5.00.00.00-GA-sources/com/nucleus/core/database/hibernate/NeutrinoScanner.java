package com.nucleus.core.database.hibernate;

import org.hibernate.boot.archive.scan.spi.AbstractScannerImpl;

/**
 * Integration class required to enable recognition of JPA entities in JBoss due to its
 * special VFS protocol handling in JBoss
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoScanner extends AbstractScannerImpl {

    public NeutrinoScanner() {
        super(NeutrinoArchiveDescriptorFactory.INSTANCE);
    }

}
