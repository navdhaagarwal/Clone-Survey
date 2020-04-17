package com.nucleus.core.cache.handler.master;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.service.BroadcastMessageService;
import com.nucleus.broadcast.service.BroadcastMessageServiceImpl;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.service.ConfigurationServiceImpl;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.currency.Currency;
import com.nucleus.currency.CurrencyCacheService;
import com.nucleus.currency.CurrencyCacheServiceImpl;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.event.ConfigurationUpdatedEvent;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.ObjectGraphParameter;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.ScriptParameter;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.rules.service.ParameterService;
import com.nucleus.rules.service.ParameterServiceImpl;
import com.nucleus.rules.service.RuleCacheService;
import com.nucleus.rules.service.RuleCacheServiceImpl;
import com.nucleus.user.User;


/**
 * 
 * Maker Checker Event Listener for the Parameter Master
 * 
 * 
 * @author prateek.chachra
 * @since GA 1.4
 * @see {@link CommonMasterMakerCheckerEventListener}
 */

@Named
@Transactional
public class CacheMasterMakerCheckerEventHandler extends GenericEventListener implements TransactionPostCommitWork {

	@Inject
	@Named("broadcastMessageService")
	private BroadcastMessageService broadcastMessageService;
	
	@Inject
	@Named("parameterService")
	private ParameterService 		parameterService;
	
	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService currencyCacheService;
	
	@Inject
	@Named("ruleCacheService")
	private RuleCacheService ruleCacheService;
	
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
    @Inject
    @Named("fwCacheHelper")
    private FWCacheHelper fwCacheHelper;
    
    public final String ARGUMENT = "ARGUMENT";

	/**
	 * 
	 * 
	 * This method overrides the method in the {@link GenericEventListener}
	 * 
	 * The method is loaded when the Event is launched. It returns true
	 * for the kind of Events(and Classes) this EventListener is defined for.
	 * 
	 * @param event MakerCheckerEvent when the Parameter is edited.
	 */
	@Override
	public boolean canHandleEvent(Event event) {
		if (event instanceof MakerCheckerEvent || event instanceof ConfigurationUpdatedEvent) {
			Class<? extends Entity> entityClazz = event.getOwnerEntityId()
					.getEntityClass();
			if (entityClazz != null
					&& (Parameter.class.isAssignableFrom(entityClazz)
							|| Currency.class.equals(entityClazz) || Rule.class
								.isAssignableFrom(entityClazz) || User.class.equals(entityClazz) || ConfigurationGroup.class.equals(entityClazz) ||BroadcastMessage.class.equals(entityClazz))) {
				return true;

			}
		}

		return false;
	}
	/**
	 * 
	 * This method overrides the method in the {@link GenericEventListener}
	 * 
	 * 
	 * The method is loaded if <tt>canHandleEvent(event)</tt> returns <tt>true</tt> for the
	 * specified Event.
	 * 
	 * The method runs the private method <tt>executeParameterCacheRefresh</tt> and refreshes 
	 * the cache on the launch of the Event.
	 * 
	 */
	@Override
	public void handleEvent(Event event) {
		EntityId roleEntityId = event.getOwnerEntityId();
		if (roleEntityId != null) {
			if ((Parameter.class).isAssignableFrom(roleEntityId
					.getEntityClass())) {
				executeParameterCacheRefresh(roleEntityId.getLocalId(),
						event.getEventType());
			} else if (Currency.class.equals(roleEntityId.getEntityClass())) {
				executeCurrencyCacheRefresh(roleEntityId.getLocalId(),
						event.getEventType());
			} else if ((Rule.class).isAssignableFrom(roleEntityId
					.getEntityClass())) {
				executeRuleCacheRefresh(roleEntityId.getLocalId(),
						event.getEventType());
			} else if (User.class.equals(roleEntityId.getEntityClass()) || ConfigurationGroup.class.equals(roleEntityId.getEntityClass())) {
				executeUserConfigurationCacheRefresh(roleEntityId.getLocalId(),
						event.getEventType());
			}
			
			else if ((BroadcastMessage.class).isAssignableFrom(roleEntityId
					.getEntityClass())) {
				executeBroadcastCacheRefresh(roleEntityId.getLocalId(),
						event.getEventType());
			}
		}

	}

	
	
	
	
	
	private void executeBroadcastCacheRefresh(Long broadcastMessageId, int eventType) {
		Map<String,Object> dataMap = new HashMap<>();
		if (matchTransactionEvent(eventType, EventTypes.MAKER_CHECKER_APPROVED,
				EventTypes.MAKER_CHECKER_UPDATED_APPROVED,
				EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
			
			BroadcastMessage message = new BroadcastMessage();
			message.setId(broadcastMessageId);
			
			dataMap.put(ARGUMENT, message);
			dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP,
			fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.BROADCAST_MESSAGE_CACHE
					));
			
			
				TransactionPostCommitWorker.handlePostCommit(this, dataMap, false);
				
		}
	
	}
	/**
	 * A private method that launches a thread to rebuild the Parameter Cache.
	 * 
	 * 
	 * 
	 * @param parameterId The target ID of the Parameter for which the 
	 * Event is being launched.
	 * @param eventType The type of the Event launched (An Integer)
	 */
	private void executeParameterCacheRefresh(
			final Long parameterId, final int eventType) {
			Map<String,Object> dataMap = new HashMap<>();
		if (matchTransactionEvent(eventType, EventTypes.MAKER_CHECKER_APPROVED,
				EventTypes.MAKER_CHECKER_UPDATED_APPROVED,
				EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
			Parameter parameter = new Parameter();
			parameter.setId(parameterId);
			dataMap.put(ARGUMENT, parameter);
			
			Map<String,ImpactedCache> impactedCacheMap = fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.PARAMETER_CACHE_ID,
					FWCacheConstants.PARAMETER_BY_TYPE_AND_NAME);
			
			if (parameter instanceof ObjectGraphParameter) {
				impactedCacheMap
						.putAll(fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.OG_PARAMETER_BY_OG));
			} else if (parameter instanceof ScriptParameter) {
				impactedCacheMap.putAll(
						fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.DECRYPTED_PARAM_SCRIPT_ID,
								FWCacheConstants.SCRIPTPARAMETER_EVALUATOR_BY_PARAM_ID));
			}
			dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP,impactedCacheMap);
			TransactionPostCommitWorker.handlePostCommit(this, dataMap, true);
		}
	}
	
	 	/**
	    * A private method that launches a thread to rebuild the Currency Cache.
	    * 
	    * @param currencyId The target ID of the Currency for which the 
	    * Event is being launched.
	    * @param eventType The type of the Event launched (An Integer)
	    */
		private void executeCurrencyCacheRefresh(final Long currencyId,
				final int eventType) {
			Map<String,Object> dataMap = new HashMap<>();
			if (matchTransactionEvent(eventType, EventTypes.MAKER_CHECKER_APPROVED,
					EventTypes.MAKER_CHECKER_UPDATED_APPROVED,
					EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
					Currency currency = new Currency();
					currency.setId(currencyId);
					dataMap.put(ARGUMENT, currency);
					dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP,
					fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.CURRENCY_CACHE_BY_ID,
							FWCacheConstants.CURRENCY_COMMON_PROPS, FWCacheConstants.CURRENCY_CONVERSION_RATE,
							FWCacheConstants.CURRENCY_CACHE_APPROVED_ACTIVE));
					TransactionPostCommitWorker.handlePostCommit(this, dataMap, true);
					
			}
		}
		
		/**
		 * A private method that launches a thread to rebuild the RuleCache Cache.
		 * 
		 * @param scriptRuleId
		 * @param eventType
		 */
		private void executeRuleCacheRefresh(final Long ruleId,
					final int eventType) {
				Map<String,Object> dataMap = new HashMap<>();
				if (matchTransactionEvent(eventType, EventTypes.MAKER_CHECKER_APPROVED,
						EventTypes.MAKER_CHECKER_UPDATED_APPROVED,
						EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
						Rule rule = new Rule();
						rule.setId(ruleId);
						dataMap.put(ARGUMENT, rule);
						dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.SCRIPTRULE_EVALUATOR_BY_SCRIPTRULE_ID));
						TransactionPostCommitWorker.handlePostCommit(this, dataMap, true);
						
				}
			}
		
		private void executeUserConfigurationCacheRefresh(final Long userId,
				final int eventType) {
			Map<String,Object> dataMap = new HashMap<>();
			if (matchTransactionEvent(eventType, EventTypes.MAKER_CHECKER_APPROVED,
					EventTypes.MAKER_CHECKER_UPDATED_APPROVED,
					EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
					User user = new User(userId);
					dataMap.put(ARGUMENT, user);
					impactedCacheMapBuilderForConfigurationCacheGroup(dataMap);
					TransactionPostCommitWorker.handlePostCommit(this, dataMap, true);
			}
			else if(matchTransactionEvent(eventType, EventTypes.CONFIGURATION_UPDATED_EVENT)) {
				ConfigurationGroup configgroup = new ConfigurationGroup();
				configgroup.setId(userId);
				dataMap.put(ARGUMENT, configgroup);
				impactedCacheMapBuilderForConfigurationCacheGroup(dataMap);
				TransactionPostCommitWorker.handlePostCommit(this, dataMap, true);
			}
		}
		
		private void impactedCacheMapBuilderForConfigurationCacheGroup(Map<String,Object> dataMap)
		{
			dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(
					FWCacheConstants.CONFIGURATION_DISTINCT_MODIFIABLE_PROPERTYKEY,
					FWCacheConstants.CONFIGURATION_DISTINCT_PROPERTKEY, FWCacheConstants.CONFIGURATION_GROUP_ID,
					FWCacheConstants.CONFIGURATION_GROUP_CACHE_ASSOCIATED_ENTITY,
					FWCacheConstants.ENTITYURI_PROPKEY_CONFIG_MAP, FWCacheConstants.ENTITYURI_PROPKEY_CONFIGVO_MAP));
		}

/**
 * 
 * 
 * Private method to check if the event is one of the specified
 * transaction events. The events are denoted as Constant Integers here.
 * 
 * 
 * @param value The event to be checked
 * @param transactionEvents The specified events.
 * @return true if the event matches the specified Transaction Events
 */

	public Boolean matchTransactionEvent(Integer value,
			Integer... transactionEvents) {
		Boolean transactionEventExists = false;
		if (transactionEvents != null) {
			for (Integer matchingTransactionType : transactionEvents) {
				if (matchingTransactionType.equals(value)) {
					transactionEventExists = true;
					break;
				}

			}
		}
		return transactionEventExists;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void work(Object argumentMap) {
		Map<String,Object> dataMap = (Map<String, Object>) argumentMap;
		Object argument = dataMap.get(ARGUMENT);
		if (argument instanceof Parameter) {
			Parameter parameter = (Parameter) argument;
			parameter = entityDao.find(Parameter.class, parameter.getId());
			dataMap.put(ParameterServiceImpl.PARAMETER_OBJECT, parameter);
			dataMap.put(ParameterServiceImpl.PARAMETER_OBJECT_ID,  parameter.getId());
			parameterService.updateParameterCache(dataMap);
		} else if (argument instanceof Currency) {
			Currency currency = (Currency) argument;
			currency = currencyCacheService.getCurrencyById(currency.getId());
			dataMap.put(CurrencyCacheServiceImpl.CURRENCY_OBJECT, currency);
			dataMap.put(CurrencyCacheServiceImpl.CURRENCY_OBJECT_ID, currency.getId());
			if (currency != null) {
				currencyCacheService.updateCurrencyCache(dataMap);
			}				
		} else if (argument instanceof Rule) {
			Rule rule = (Rule) argument;
			rule = entityDao.find(Rule.class, rule.getId());
			dataMap.put(RuleCacheServiceImpl.SCRIPT_RULE_OBJECT, rule);
			if (rule instanceof ScriptRule) {
				BaseLoggers.flowLogger
						.debug(" Start updating ScriptRule cache :: ");
				ruleCacheService
						.updateScriptRuleCache(dataMap);
				BaseLoggers.flowLogger
						.debug(" End updating ScriptRule cache :: ");
			}
		} else if (argument instanceof User) {
			User user = (User) argument;
			user = entityDao.find(User.class, user.getId());
			EntityId userEntityId = user.getEntityId();
			ConfigurationGroup configurationGroup = configurationService
					.getConfigurationGroupFor(userEntityId, false);
			dataMap.put(ConfigurationServiceImpl.CONFIGURATION_GROUP_OBJECT, configurationGroup);
			configurationService.updateConfigurationCache(dataMap);

		}
		else if(argument instanceof ConfigurationGroup) {
			ConfigurationGroup cg = (ConfigurationGroup) argument;
			cg = configurationService.getConfigurationGroupFromId(cg.getId());
			dataMap.put(ConfigurationServiceImpl.CONFIGURATION_GROUP_OBJECT, cg);
			configurationService.updateConfigurationCache(dataMap);
		}
		
		else if(argument instanceof BroadcastMessage) {
			BroadcastMessage bm = (BroadcastMessage) argument;
			bm = entityDao.find(BroadcastMessage.class, bm.getId());
			dataMap.put(BroadcastMessageServiceImpl.BROADCAST_MESSAGE_OBJECT, bm);
			broadcastMessageService.updateBroadcastMessageCache(dataMap);
			
			
		}
		

	}

}
