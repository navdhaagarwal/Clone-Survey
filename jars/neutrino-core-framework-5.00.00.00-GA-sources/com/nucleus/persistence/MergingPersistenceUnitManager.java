package com.nucleus.persistence;

import java.net.URL;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Extends {@link DefaultPersistenceUnitManager} to merge configurations of one persistence unit residing in multiple
 * {@code persistence.xml} files into one. This is necessary to allow the declaration of entities in seperate modules.
 * 
 * Refer this blog entry for more details: http://labs.bsb.com/2010/11/configuring-modular-jpa-applications-with-spring/<br/>
 * The solution presented in this blog is different, more complex and little different than what we require. Therefore it has been customized
 * to suit our needs. 
 * 
 * @author praveen.jain
 */
public class MergingPersistenceUnitManager extends DefaultPersistenceUnitManager {

    private final Logger logger = LoggerFactory.getLogger(MergingPersistenceUnitManager.class);

    private String       basePersistenceUnitName;

    @PostConstruct
    public void validateConfiguration() {
        if (StringUtils.isBlank(basePersistenceUnitName)) {
            throw new IllegalStateException(
                    "A base persistence unit name should be defined for PU merging to successfully happen");
        }
    }

    protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        super.postProcessPersistenceUnitInfo(pui);

        // If this is our base persistence unit that we are using to merge all others.. we will skip its processing
        if (isBasePersistenceUnit(pui)) {
            return;
        }

        final MutablePersistenceUnitInfo mainPui = getMainPersistenceUnitInfo(pui);

        logger.info("Merging information from Persistence Unit [" + pui.getPersistenceUnitName() + "] "
                + "to Persistence Unit [" + mainPui.getPersistenceUnitName() + "]");
        mergePersistenceUnit(pui, mainPui);
    }

    /**
     * Sets the name of the persistence unit that should be used for merging. If no
     * such persistence unit exists, an exception will be thrown, preventing
     * the factory to be created.
     * @param persistenceUnitName the name of the persistence unit to be used for merging
     */
    public void setBasePersistenceUnitName(String basePersistenceUnitName) {
        this.basePersistenceUnitName = basePersistenceUnitName;
    }

    /**
     * Merges a persistence unit to another one. Takes care of handling both
     * managed classes and urls. If the persistence unit has managed classes,
     * only merge these and prevents scanning. If no managed classes are
     * available, add the url of the module for entity scanning.
     *
     * @param from the persistence unit to handle
     * @param to the target (merged) persistence unit
     */
    protected void mergePersistenceUnit(MutablePersistenceUnitInfo from, MutablePersistenceUnitInfo to) {
        String fromName = from.getPersistenceUnitName();
        String toName = to.getPersistenceUnitName();
        if (from.getManagedClassNames().size() != 0) {  // Either take into account the listed entities
            for (String s : from.getManagedClassNames()) {
                to.addManagedClassName(s);
            }
            logger.debug("Added [" + from.getManagedClassNames().size() + "] managed classes from PU [" + fromName + "] to "
                    + "PU [" + toName + "]");
        } else {    // Or work with classpath scan
            /*
             * The base PU's classpath will be by default scanned for entities. Therefore if 'from' PU's root URL is different from base PU's
             * root URL, only then add the 'from' PU's URL into base PU's URL list  
             */
        	String toURLString = to.getPersistenceUnitRootUrl().toString();
        	String fromURLString = from.getPersistenceUnitRootUrl().toString();
            if (!toURLString.equals(fromURLString)) {
                to.addJarFileUrl(from.getPersistenceUnitRootUrl());
            }
        }
        for (URL url : from.getJarFileUrls()) {
            to.addJarFileUrl(url);
            logger.debug("Added Jar File URL: [" + url + "] from PU [" + fromName + "] to " + "PU [" + toName + "]");
        }
        for (String s : from.getMappingFileNames()) {
            to.addMappingFileName(s);
            logger.debug("Added Mapping File: [" + s + "] from PU [" + fromName + "] to " + "PU [" + toName + "]");
        }

    }

    /**
     * Specifies whether the specified persistence unit is the template one we
     * use to merge.
     *
     * @param pui the persistence unit to test
     * @return <tt>true</tt> if the persistence unit is the target, template persistence unit
     */
    private boolean isBasePersistenceUnit(MutablePersistenceUnitInfo pui) {
        return (basePersistenceUnitName.equals(pui.getPersistenceUnitName()));
    }

    /**
     * Returns the base  {@link MutablePersistenceUnitInfo} to use for merging, based on the
     * given {@link MutablePersistenceUnitInfo} and the settings of the manager.
     * @return the persistence unit info to be used for merging
     */
    private MutablePersistenceUnitInfo getMainPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        // We have a match, retrieve our persistence unit name then
        final MutablePersistenceUnitInfo result = getPersistenceUnitInfo(basePersistenceUnitName);
        // Sanity check
        if (result == null) {
            throw new IllegalStateException(
                    "No persistence unit found with name ["
                            + basePersistenceUnitName
                            + "] "
                            + "and therefore no merging is possible. It usually means that the bootstrap-persistence.xml has not been "
                            + "included in the list of persistence.xml location(s). Check your configuration as it "
                            + "should be the first in the list!");
        }
        return result;
    }
}
