/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import static com.nucleus.event.EventTypes.MAKER_CHECKER_CREATE_EVENT;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_UPDATE_EVENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.locale.LanguageInfoReader;
import com.nucleus.core.locale.LanguageInfoVO;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.CriteriaQueryExecutor;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.event.MakerCheckerHelper;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named("messageResourceService")
public class MessageResourceServiceImpl extends BaseServiceImpl
		implements MessageResourceService, ApplicationContextAware {

	ApplicationContext applicationContext;

	DatabaseDrivenMessageSource databaseDrivenMessageSource;

	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao baseMasterDao;

	@Inject
	@Named(value = "makerCheckerHelper")
	protected MakerCheckerHelper makerCheckerHelper;

	@Inject
	@Named("languageInfoReader")
	private LanguageInfoReader languageInfoReader;

	@Override
	public List<MessageResource> loadAllMessages() {
		return entityDao.findAll(MessageResource.class);
	}

	@Override
	public MessageResource getMessageResourceById(Long id) {
		NamedQueryExecutor<MessageResource> query = new NamedQueryExecutor<MessageResource>(
				"messageResource.getmessageResourceById").addParameter("id", id);
		MessageResource messageResource = entityDao.executeQueryForSingleValue(query);
		MessageResource messageResourceForView =new MessageResource();
		messageResourceForView.copyFrom(messageResource,CloneOptionConstants.COPY_WITH_ID_AND_UUID);
		List<MessageResourceValue> messageResourceValuesList = new ArrayList<>();
		Map<String, LanguageInfoVO> localeLanguageInfoMap = languageInfoReader.getAvailableLocaleLanguageInfoMap();
		List<String> localeKeyList=new ArrayList<>();
		localeKeyList.add("default_message");
		if(!localeLanguageInfoMap.isEmpty()){
			localeKeyList.addAll(localeLanguageInfoMap.keySet());
		}
		Map<String,MessageResourceValue>  localeKeyAndMsgResourceValueMap=new HashMap<>();
		
		for(MessageResourceValue messageResourceValue : messageResource.getMessageResourceValues()){
			localeKeyAndMsgResourceValueMap.put(messageResourceValue.getLocaleKey(), messageResourceValue);
		}
		
		for(String locale_Key:localeKeyList){
			if(localeKeyAndMsgResourceValueMap.containsKey(locale_Key)){
				messageResourceValuesList.add(localeKeyAndMsgResourceValueMap.get(locale_Key));
			}else{
				MessageResourceValue messageResourceValue = new MessageResourceValue();
				messageResourceValue.setLocaleKey(locale_Key);
				messageResourceValuesList.add(messageResourceValue);
			}
		}
		messageResourceForView.setMessageResourceValues(messageResourceValuesList);
		return messageResourceForView;
}

	@Override
	public void saveMessageResource(MessageResource messageResource, User user) {
		NeutrinoValidator.notNull(messageResource, "Message Resource can not be null");
		List<MessageResourceValue> messageResourceValueList = new ArrayList<MessageResourceValue>();
		if (messageResource.getId() != null) {
		updateMessageResourceValueList(messageResource);
		}
		messageResource.getEntityLifeCycleData().setCreatedByUri(user.getUri());
		messageResource.getEntityLifeCycleData().setCreationTimeStamp(DateUtils.getCurrentUTCTime());
		if (messageResource.getMessageResourceValues() != null) {
			messageResourceValueList = messageResource.getMessageResourceValues();
			Iterator<MessageResourceValue> messageResourceValueItr = messageResourceValueList.iterator();
			while (messageResourceValueItr.hasNext()) {
				MessageResourceValue messageResourceValue = messageResourceValueItr.next();
				if (!ValidatorUtils.isNull(messageResourceValue.getLocaleValue()) && messageResourceValue.getLocaleValue().isEmpty()) {
					messageResourceValue.setLocaleValue(null);
				}

			}
		}
		messageResource.setMessageResourceValues(messageResourceValueList);
		if (messageResource.getId() == null) {
			entityDao.persist(messageResource);
			MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_CREATE_EVENT, true, user.getEntityId(),
					messageResource, makerCheckerHelper.getEntityDescription(messageResource.getEntityDisplayName()));
			event.addNonWatcherToNotify(user.getEntityId().getUri());
			eventBus.fireEvent(event);
		} else {
			entityDao.update(messageResource);
			MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATE_EVENT, true, user.getEntityId(),
					messageResource, makerCheckerHelper.getEntityDescription(messageResource.getEntityDisplayName()));
			event.addNonWatcherToNotify(user.getEntityId().getUri());
			eventBus.fireEvent(event);
		}

	}

	private MessageResource updateMessageResourceValueList(MessageResource messageResource) {
		NamedQueryExecutor<MessageResource> query = new NamedQueryExecutor<MessageResource>(
				"messageResource.getmessageResourceById").addParameter("id", messageResource.getId());
		MessageResource messageResourceDB = entityDao.executeQueryForSingleValue(query);
		Map<String,MessageResourceValue>  localeKeyAndMsgResourceValueMap=new HashMap<>();
		List<MessageResourceValue> messageResourceValues=messageResource.getMessageResourceValues();
		if(!messageResourceValues.isEmpty()){
		for(MessageResourceValue messageResourceValue : messageResourceValues){
			localeKeyAndMsgResourceValueMap.put(messageResourceValue.getLocaleKey(), messageResourceValue);
		}
		}
		for(MessageResourceValue messageResourceValueDB:messageResourceDB.getMessageResourceValues()){
			if(null!=messageResourceValueDB && !localeKeyAndMsgResourceValueMap.containsKey(messageResourceValueDB.getLocaleKey())){
				messageResourceValues.add(messageResourceValueDB);
			}
		}
		return  messageResource;
	}

	@Override
	public void updateMessageResourceIntoCache(MessageResource messageResource) {
		databaseDrivenMessageSource.updateMap(messageResource);
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		this.applicationContext = arg0;
		databaseDrivenMessageSource = (DatabaseDrivenMessageSource) applicationContext.getBean("messageSource");
	}

	@Override
	public Long getCountOfAllMessage() {
		NamedQueryExecutor<Long> countQuery = new NamedQueryExecutor<Long>("messageResource.getcountofmessages");
		return entityDao.executeQueryForSingleValue(countQuery);
	}

	@Override
	public List<MessageResource> findEntity(Map<String, Object> queryMap, Integer iDisplayStart,
			Integer iDisplayLength) {
		if (queryMap == null) {
			return null;
		}
		CriteriaQueryExecutor<MessageResource> criteriaQueryExecutor = new CriteriaQueryExecutor<MessageResource>(
				MessageResource.class);
		for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
			criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR, entry.getValue());
		}
		return baseMasterDao.executeQuery(criteriaQueryExecutor, iDisplayStart,iDisplayLength);
	}

	@Override
	public List<Map<String, String>> loadAllMessageResourceByLocale(String localeKey) {
		NeutrinoValidator.notNull(localeKey, "Locale is null");
		String query = "Select new Map(mr.messageKey as MESSAGEKEY , mrv.localeValue as MESSAGEVALUE) FROM "
				+ "MessageResource mr  INNER JOIN mr.messageResourceValues mrv WHERE mrv.localeKey = :localeKey";
		return entityDao.executeQuery((new JPAQueryExecutor<Map<String, String>>(query).addParameter("localeKey", localeKey)));
		
	}

	@Override
	public Long getCountOfMessageResourceByKey(String messageKey, String uuid) {
		NamedQueryExecutor<Long> count = new NamedQueryExecutor<Long>(
				"messageResource.getCountOfMessageResourceByKey").addParameter("messageKey", messageKey)
						.addParameter("uuid", uuid);
		return entityDao.executeQueryForSingleValue(count);	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW,readOnly = true)
	public MessageResource getMessageResourceByCode(String code) {
		NamedQueryExecutor<MessageResource> query = new NamedQueryExecutor<MessageResource>(
				"messageResource.getMessageResourceByCode").addParameter("code", code);
		List<MessageResource> messageResourceList = entityDao.executeQuery(query);
		if(messageResourceList.size() > 0)
			return messageResourceList.get(0);
		else
			return null;
	}
	
}
