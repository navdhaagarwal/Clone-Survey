/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.comment.service.CommentService;
import com.nucleus.core.itemwatch.service.ItemWatcherService;
import com.nucleus.event.EventService;

/**
 * @author Nucleus Software Exports Limited Service to perform event related
 *         operations.
 */
@Named("entityUpdateService")
public class EntityUpdateServiceImpl extends BaseServiceImpl implements
		EntityUpdateService {

	@Inject
	@Named(value = "eventService")
	private EventService eventService;

	@Inject
	@Named(value = "itemWatcherService")
	private ItemWatcherService itemWatcherService;

	@Inject
	@Named(value = "commentService")
	private CommentService commentService;


	@Override
	public void copyAssociatedEntitiesFromSourceToTarget(
			String fromOwnerEntityUri, String toOwnerEntityUri) {

		eventService.copyExistingEventsToChangedEntity(fromOwnerEntityUri,
				toOwnerEntityUri);
		itemWatcherService.copyExistingUsersToChangedWatchedItem(
				fromOwnerEntityUri, toOwnerEntityUri);
		commentService.copyExistingCommentsToChangedEntity(fromOwnerEntityUri,
				toOwnerEntityUri);

	}
}
