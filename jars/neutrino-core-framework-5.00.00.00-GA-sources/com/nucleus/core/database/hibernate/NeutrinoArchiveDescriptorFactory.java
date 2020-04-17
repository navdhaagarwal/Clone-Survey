package com.nucleus.core.database.hibernate;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.hibernate.boot.archive.internal.ExplodedArchiveDescriptor;
import org.hibernate.boot.archive.internal.JarFileBasedArchiveDescriptor;
import org.hibernate.boot.archive.internal.JarInputStreamBasedArchiveDescriptor;
import org.hibernate.boot.archive.internal.JarProtocolArchiveDescriptor;
import org.hibernate.boot.archive.internal.StandardArchiveDescriptorFactory;
import org.hibernate.boot.archive.spi.ArchiveDescriptor;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.internal.util.StringHelper;
import org.springframework.util.ResourceUtils;

public class NeutrinoArchiveDescriptorFactory extends StandardArchiveDescriptorFactory {

	public static final ArchiveDescriptorFactory INSTANCE = new NeutrinoArchiveDescriptorFactory();

	@Override
	public ArchiveDescriptor buildArchiveDescriptor(URL url, String entry) {
		final String protocol = url.getProtocol();
		if (ResourceUtils.URL_PROTOCOL_JAR.equals(protocol)) {
			return new JarProtocolArchiveDescriptor(this, url, entry);
		} else if (ResourceUtils.URL_PROTOCOL_VFS.equals(protocol) || StringHelper.isEmpty(protocol)
				|| ResourceUtils.URL_PROTOCOL_FILE.equals(protocol)) {
			// return
			// VirtualFileSystemArchiveDescriptorFactory.INSTANCE.buildArchiveDescriptor(url,
			// entry);
			return handleVfsAndFileTypes(url, entry);

		}
		/*
		 * else if (StringHelper.isEmpty(protocol) ||
		 * ResourceUtils.URL_PROTOCOL_FILE.equals(protocol)) {
		 * 
		 * return handleVfsAndFileTypes(url, entry);
		 * 
		 * }
		 */

		else {
			// let's assume the url can return the jar as a zip stream
			return new JarInputStreamBasedArchiveDescriptor(this, url, entry);
		}
	}

	private ArchiveDescriptor handleVfsAndFileTypes(URL url, String entry) {

		final File file;
		try {
			final String filePart = url.getFile();
			if (filePart != null && filePart.indexOf(' ') != -1) {
				// unescaped (from the container), keep as is
				file = new File(url.getFile());
			} else {
				file = new File(url.toURI().getSchemeSpecificPart());
			}

			if (!file.exists()) {
				throw new IllegalArgumentException(String.format(
						"File [%s] referenced by given URL [%s] does not exist", filePart, url.toExternalForm()));
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Unable to visit JAR " + url + ". Cause: " + e.getMessage(), e);
		}

		if (file.isDirectory()) {
			return new ExplodedArchiveDescriptor(this, url, entry);
		} else {
			return new JarFileBasedArchiveDescriptor(this, url, entry);
		}
	}

}
