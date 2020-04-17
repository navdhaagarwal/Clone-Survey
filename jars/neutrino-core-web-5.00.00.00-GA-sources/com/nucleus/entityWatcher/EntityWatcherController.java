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

package com.nucleus.entityWatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.itemwatch.service.ItemWatcherService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Controller
@Transactional
@RequestMapping(value = "/entityWatcher")
public class EntityWatcherController extends BaseController {

    @Inject
    @Named("userService")
    private UserService        userService;

    /*@Inject
    @Named("leadService")
    private LeadService leadService;*/

    @Inject
    @Named("messageSource")
    protected MessageSource    messageSource;

    @Inject
    @Named("itemWatcherService")
    private ItemWatcherService itemWatcherService;

    private String getMessageFromPropertyFile(HttpServletRequest request, String key) throws IOException {
        Locale locale = RequestContextUtils.getLocale(request);
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * This method decides initial action for Watcher. It may Start or Stop
     * Watching. It returns array of String that includes initial action and
     * number of watchers for given Entity
     * 
     * @param request
     * @param entityId
     * @param currentWatchingEntityClassName
     * @return
     * @throws IOException
     */
    @RequestMapping("/watcherActions/startStopWatching")
    @ResponseBody
    public String[] getInitialActionWithViewCounts(HttpServletRequest request, @RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName) throws IOException {

        String[] actionPlusCountViews = new String[2];

        if (currentWatchingEntityClassName != null && entityId != null) {
            String watcherUri = getUserDetails().getUserEntityId().getUri();

            String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();

            if (watcherUri != null) {

                boolean checkWatcher = itemWatcherService.isUserAlreadyWatcherForEntity(watchedEntityUri, watcherUri);

                if (checkWatcher) {
                    actionPlusCountViews[0] = getMessageFromPropertyFile(request, "label.watcher.stop.watching");
                } else {
                    actionPlusCountViews[0] = getMessageFromPropertyFile(request, "label.watcher.follow.me");
                }

                Long countViews = itemWatcherService.findNumberOfWatchersForEntity(watchedEntityUri);
                actionPlusCountViews[1] = countViews.toString();
            }
        }
        return actionPlusCountViews;
    }

    /**
     * This method handles Start/Stop action of Watcher. It returns array of
     * String that includes number of watchers, next action after start/stop
     * watching and success message
     * 
     * @param request
     * @param entityId
     * @param currentWatchingEntityClassName
     * @return
     * @throws IOException
     */
    @RequestMapping("/watcherActions/startStop")
    @ResponseBody
    public String[] watcherStartStopActions(HttpServletRequest request, @RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName) throws IOException {

        String[] actionMessagePlusViewCountOnAction = new String[3];

        if (currentWatchingEntityClassName != null && entityId != null) {
            String watcherUri = getUserDetails().getUserEntityId().getUri();
            String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();

            if (watcherUri != null) {

                Set<String> allWatchersUri = itemWatcherService.findAllWatchersFor(watchedEntityUri);

                if (!(allWatchersUri.contains(watcherUri))) {
                    itemWatcherService.addUserToWatchedItem(watchedEntityUri, watcherUri);
                    actionMessagePlusViewCountOnAction[0] = getMessageFromPropertyFile(request,
                            "label.watcher.started.successfully");
                    actionMessagePlusViewCountOnAction[1] = getMessageFromPropertyFile(request,
                            "label.watcher.stop.watching");
                } else {
                    itemWatcherService.deleteUserFromWatchedItem(watchedEntityUri, watcherUri);
                    actionMessagePlusViewCountOnAction[0] = getMessageFromPropertyFile(request,
                            "label.watcher.stopped.successfully");
                    actionMessagePlusViewCountOnAction[1] = getMessageFromPropertyFile(request,
                            "label.watcher.follow.me");
                }

                Long countViews = itemWatcherService.findNumberOfWatchersForEntity(watchedEntityUri);
                actionMessagePlusViewCountOnAction[2] = countViews.toString();
            }
        }
        return actionMessagePlusViewCountOnAction;
    }

    /**
     * This method handles view action of watcher.
     * 
     * @param entityId
     * @param currentWatchingEntityClassName
     * @param map
     * @return
     */
    @RequestMapping("/watcherActions/view")
    public String watcherViewAction(@RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName, ModelMap map) {

        if (currentWatchingEntityClassName != null && entityId != null) {

            String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();
            String watcherUri = getUserDetails().getUserEntityId().getUri();

            Set<String> allWatchersUri;

            if (watcherUri != null) {

                allWatchersUri = itemWatcherService.findAllWatchersFor(watchedEntityUri);

                List<UserInfo> allWatchersInfo = new ArrayList<UserInfo>();
                for (String watcherUriLocal : allWatchersUri) {
                    Long getUserId = Long.valueOf(watcherUriLocal.split(":")[1]);
                    allWatchersInfo.add(userService.getUserById(getUserId));
                }

                UserInfo currentUserInfo;

                for (UserInfo watcherInfo : allWatchersInfo)
                    if (watcherInfo.getUserEntityId().getUri().equals(watcherUri)) {
                        currentUserInfo = watcherInfo;
                        allWatchersInfo.remove(watcherInfo);
                        allWatchersInfo.add(0, currentUserInfo);
                        break;
                    }

                // map.put("allWatchersUri", allWatchersUri);
                map.put("allWatchersInfo", allWatchersInfo); // Put all
                                                             // Watchers(UserInfo)
            }
        }
        return "viewWatchers";
    }

    /**
     * This method handles manage action of watcher.
     * 
     * @param entityId
     * @param currentWatchingEntityClassName
     * @param map
     * @return
     */
    @RequestMapping("/watcherActions/manage")
    public String watcherManageAction(@RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName, ModelMap map) {

        if (currentWatchingEntityClassName != null && entityId != null) {

            String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();
            Set<String> allWatchersUri = itemWatcherService.findAllWatchersFor(watchedEntityUri);

            List<UserInfo> allWatchersInfo = new ArrayList<UserInfo>();
            List<User> allUsers = userService.getAllUser();

            if (!allWatchersUri.isEmpty() && !allUsers.isEmpty()) {
                for (String watcherUri : allWatchersUri) {
                    Long getUserId = Long.valueOf(watcherUri.split(":")[1]);
                    allWatchersInfo.add(userService.getUserById(getUserId));

                    if (!allUsers.isEmpty()) {
                        for (User user : allUsers) {
                            if (user.getUri().equals(watcherUri)) {
                                allUsers.remove(user);
                                break;
                            }
                        }
                    }
                }
            }

            /* Put All Watchers of given Entity */
            map.put("allWatchersInfo", allWatchersInfo);

            /* Put All Users excluding watchers(if any) */
            map.put("allUsers", allUsers);
        }
        return "manageWatchers";
    }

    /**
     * This method handles adding or removing multiple watchers from list.
     * 
     * @param request
     * @param action
     * @param entityId
     * @param currentWatchingEntityClassName
     * @param listOfCheckedWatchers
     * @return
     * @throws IOException
     */
    @RequestMapping("/watcherActions/manage/addOrRemoveListOfWatchers/{action}")
    @ResponseBody
    public String watcherManageAddOrRemoveAction(HttpServletRequest request, @PathVariable String action,
            @RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName,
            @RequestParam("listOfCheckedWatchers") String[] listOfCheckedWatchers) throws IOException {

        String successMessage = "";

        if (currentWatchingEntityClassName != null && entityId != null) {

            if (listOfCheckedWatchers.length != 0) {
                String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();

                if ("removeList".equalsIgnoreCase(action)) {
                    for (String checkedWatcher : listOfCheckedWatchers) {
                        itemWatcherService.deleteUserFromWatchedItem(watchedEntityUri, checkedWatcher);
                    }
                    successMessage = getMessageFromPropertyFile(request, "label.watcher.removed.successfully");
                } else {

                    for (String checkedWatcher : listOfCheckedWatchers) {
                        itemWatcherService.addUserToWatchedItem(watchedEntityUri, checkedWatcher);
                    }
                    successMessage = getMessageFromPropertyFile(request, "label.watcher.added.successfully");
                }
            } else
                successMessage = getMessageFromPropertyFile(request, "label.select.watcher.to.addOrremove");
        }
        return successMessage;
    }

    /**
     * This method handles adding or removing selected watcher
     * 
     * @param request
     * @param action
     * @param entityId
     * @param currentWatchingEntityClassName
     * @param currentWatcherUri
     * @return
     * @throws IOException
     */
    @RequestMapping("/watcherActions/manage/addOrRemoveThisWatcher/{action}")
    @ResponseBody
    public String watcherManageAddOrRemoveAction(HttpServletRequest request, @PathVariable String action,
            @RequestParam("entityId") Long entityId,
            @RequestParam("currentWatchingEntityClassName") String currentWatchingEntityClassName,
            @RequestParam("currentWatcherUri") String currentWatcherUri) throws IOException {

        String successMessage = "";

        if (currentWatchingEntityClassName != null && entityId != null) {

            String watchedEntityUri = currentWatchingEntityClassName + ":" + entityId.toString();

            if (currentWatcherUri != null) {
                if ("removeThis".equalsIgnoreCase(action)) {
                    itemWatcherService.deleteUserFromWatchedItem(watchedEntityUri, currentWatcherUri);
                    successMessage = getMessageFromPropertyFile(request, "label.watcher.removed.successfully");
                } else {
                    itemWatcherService.addUserToWatchedItem(watchedEntityUri, currentWatcherUri);
                    successMessage = getMessageFromPropertyFile(request, "label.watcher.added.successfully");
                }
            } else
                successMessage = getMessageFromPropertyFile(request, "label.watcher.no.addremove");
        }
        return successMessage;
    }

}
