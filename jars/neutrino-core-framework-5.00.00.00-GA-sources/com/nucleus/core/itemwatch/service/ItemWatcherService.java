/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.itemwatch.service;

import java.util.Set;

/**
 * The Interface ItemWatcherService.
 */
public interface ItemWatcherService {

    /**
     * Adds the user to watched item.
     *
     * @param watchedEntityUri the watched entity uri
     * @param watcherUri the watcher uri
     */
    void addUserToWatchedItem(String watchedEntityUri, String watcherUri);

    /**
     * Adds the users to watched item.
     *
     * @param watchedEntityUri the watched entity uri
     * @param watcherUris the watcher uris
     */
    void addUsersToWatchedItem(String watchedEntityUri, Set<String> watcherUris);

    /**
     * Delete user from watched item.
     *
     * @param watchedEntityUri the watched entity uri
     * @param watcherUri the watcher uri
     */
    void deleteUserFromWatchedItem(String watchedEntityUri, String watcherUri);

    /**
     * Find all watched entity uris for.
     *
     * @param watcherUri the watcher uri
     * @return the sets the
     */
    Set<String> findAllWatchedEntityUrisFor(String watcherUri);

    /**
     * Find all watchers for.
     *
     * @param watchedEntityUri the watched entity uri
     * @return the sets the
     */
    Set<String> findAllWatchersFor(String watchedEntityUri);

    /**
     * Checks if is user already watcher for entity.
     *
     * @param watchedEntityUri the watched entity uri
     * @param watcherUri the watcher uri
     * @return true, if is user already watcher for entity
     */
    boolean isUserAlreadyWatcherForEntity(String watchedEntityUri, String watcherUri);

    /**
     * Find number of watchers for entity.
     *
     * @param watchedEntityUri the watched entity uri
     * @return the long
     */
    long findNumberOfWatchersForEntity(String watchedEntityUri);

    /**
     * 
     * Copies existing users to changed WatchedItem
     * @param fromWatchedEntityUri
     * @param toWatchedEntityUri
     */
    public void copyExistingUsersToChangedWatchedItem(String fromWatchedEntityUri, String toWatchedEntityUri);

}
