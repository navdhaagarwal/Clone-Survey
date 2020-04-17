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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.nucleus.core.itemwatch.WatchedItem;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;


/**
 * The Class ItemWatcherServiceImpl.
 */
@Named("itemWatcherService")
public class ItemWatcherServiceImpl extends BaseServiceImpl implements ItemWatcherService {

    @Override
    public void addUserToWatchedItem(String watchedEntityUri, String watcherUri) {
        WatchedItem watchedItem = findWatchedItemByEntityUri(watchedEntityUri);
        watchedItem.setEntityUri(watchedEntityUri);
        watchedItem.addUserUri(watcherUri);
        entityDao.saveOrUpdate(watchedItem);
    }

    @Override
    public void addUsersToWatchedItem(String watchedEntityUri, Set<String> watcherUris) {
        WatchedItem watchedItem = findWatchedItemByEntityUri(watchedEntityUri);
        watchedItem.setEntityUri(watchedEntityUri);
        for (String watcherUri : watcherUris) {
            watchedItem.addUserUri(watcherUri);
        }
        entityDao.saveOrUpdate(watchedItem);
    }

    @Override
    public void deleteUserFromWatchedItem(String watchedEntityUri, String watcherUri) {
        WatchedItem watchedItem = findWatchedItemByEntityUri(watchedEntityUri);
        NeutrinoValidator.notNull(watchedItem.getId(), "Watchers can be deleted only from a persisted watched item");
        watchedItem.deleteUserUri(watcherUri);
        entityDao.update(watchedItem);
    }

    @Override
    public Set<String> findAllWatchedEntityUrisFor(String watcherUri) {
        NamedQueryExecutor<WatchedItem> namedQueryExecutor = new NamedQueryExecutor<WatchedItem>(
                "WatchedItem.findItemsByUserUri").addParameter("userUri", watcherUri);
        namedQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<WatchedItem> watchedItems = entityDao.executeQuery(namedQueryExecutor);
        Set<String> watchedEntityUris = new HashSet<String>();

        for (WatchedItem watchedItem : watchedItems) {
            watchedEntityUris.add(watchedItem.getEntityUri());
        }
        return watchedEntityUris;
    }

    @Override
	public Set<String> findAllWatchersFor(String watchedEntityUri) {
        WatchedItem watchedItem = findWatchedItemByEntityUri(watchedEntityUri);
        Set<String> watchers = watchedItem.getUserUris();
        if (watchers == null) {
            watchers = Collections.emptySet();
        }
        return watchers;
    }

    @Override
    public boolean isUserAlreadyWatcherForEntity(String watchedEntityUri, String watcherUri) {
        boolean isUserAlreadyWatcherForEntity = false;
        NamedQueryExecutor<Long> namedQueryExecutor = new NamedQueryExecutor<Long>("WatchedItem.findItemCountByUserUri")
                .addParameter("entityUri", watchedEntityUri).addParameter("userUri", watcherUri);
        namedQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        Long itemCount = entityDao.executeQueryForSingleValue(namedQueryExecutor);

        if (itemCount > 0) {
            isUserAlreadyWatcherForEntity = true;
        }

        return isUserAlreadyWatcherForEntity;
    }

    @Override
    public long findNumberOfWatchersForEntity(String watchedEntityUri) {
        NamedQueryExecutor<Long> namedQueryExecutor = new NamedQueryExecutor<Long>(
                "WatchedItem.findNumberOfWatchersByEntityUri").addParameter("entityUri", watchedEntityUri);
        namedQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        Long itemCount = entityDao.executeQueryForSingleValue(namedQueryExecutor);

        return itemCount;
    }

    @Override
    public void copyExistingUsersToChangedWatchedItem(String fromWatchedEntityUri, String toWatchedEntityUri) {

        WatchedItem watchedItem = findWatchedItemByEntityUri(fromWatchedEntityUri);
        if (watchedItem != null && watchedItem.getUserUris() != null && !watchedItem.getUserUris().isEmpty()) {
            addUsersToWatchedItem(toWatchedEntityUri, watchedItem.getUserUris());
        }

    }

    /**
     * Find watched item by entity uri.
     *
     * @param watchedEntityUri the watched entity uri
     * @return the watched item
     */
    protected WatchedItem findWatchedItemByEntityUri(String watchedEntityUri) {
        NamedQueryExecutor<WatchedItem> namedQueryExecutor = new NamedQueryExecutor<WatchedItem>(
                "WatchedItem.findByEntityUri").addParameter("entityUri", watchedEntityUri);
        WatchedItem watchedItem = entityDao.executeQueryForSingleValue(namedQueryExecutor);
        if (watchedItem == null) {
            watchedItem = new WatchedItem();
        }
        return watchedItem;
    }

}
