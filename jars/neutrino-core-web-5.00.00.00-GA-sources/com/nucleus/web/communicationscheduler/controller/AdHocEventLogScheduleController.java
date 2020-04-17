package com.nucleus.web.communicationscheduler.controller;




import static com.nucleus.core.event.EventCodeType.ADHOC_EVENTCODE;
import static com.nucleus.core.event.EventCodeType.TRANSACTION_BASED_EVENTCODE;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AdHocEventLogSchedule;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AdHocEventLogScheduleMapping;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.IAdHocEventLogCriteriaService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationSchedulerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationSchedulerHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.AdHocEventLogScheduleVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

@Transactional
@Controller
@RequestMapping(value = "/AdHocEventLogSchedule")
public class AdHocEventLogScheduleController extends BaseController {

	
	
	@Inject
	@Named("communicationSchedulerHelper")
	private CommunicationSchedulerHelper communicationSchedulerHelper;
	
	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;
	

	@Inject
    @Named("communicationSchedulerService")
    private ICommunicationSchedulerService communicationSchedulerService;
		
	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;
	
    @Inject
    @Named(value = "entityDao")
    private EntityDao                     entityDao;
    
    @Inject
    @Named(value = "adHocEventLogCriteriaService")
    private IAdHocEventLogCriteriaService            adHocEventLogCriteriaService;
    
    @Inject
    @Named(value = "genericParameterService")    
    private GenericParameterService genericParameterService;
    
  
	
	private static final String ADHOC_EVENT_LOG_SCHEDULE_VO="adHocEventLogScheduleVO";
	private static final String ADHOC_EVENT_LOG_SCHEDULE_MASTER="adHocEventLogSchedule";
	private static final String ADHOC_EVENT_LOG_SCHEDULE="AdHocEventLogSchedule";
	private static final String MASTER_ID="masterID";
	private static final String SCHEDULER_NAME="schedulerName";
	private static final String SAVING_ADHOC_EVENT_LOG_SCHEDULE="Saving adHocEventLogScheduleVO Details-->";
	private static final String REDIRECT_GRID="redirect:/app/grid/AdHocEventLogSchedule/AdHocEventLogSchedule/loadColumnConfig";
	private static final String COMMUNICATION_EVENT_CODE="eventCodeListFragment";
	private static final String EVENT_CODE_SELECTED_LIST="eventCodeSelectedList";
	private static final String EVENT_CODE_LIST="eventCodeList";
	private static final String END_DATE="endDate";
 
	@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/create")
	public String createadHocEventLogSchedule(ModelMap map) {
		updateModelMapForCreateOrUpdate(map);
		return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
	}

	@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveAdHocEventLogSchedule(
			@Validated AdHocEventLogScheduleVO adHocEventLogScheduleVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		BaseLoggers.flowLogger.debug(SAVING_ADHOC_EVENT_LOG_SCHEDULE
				+ adHocEventLogScheduleVO);
		AdHocEventLogSchedule adHocEventLogSchedule = prepareDataForSaveOrSaveAndSendForApproval(adHocEventLogScheduleVO);
		validateMap.put(SCHEDULER_NAME,
				adHocEventLogSchedule.getSchedulerName());
		List<String> colNameList = baseMasterService.hasEntity(
				AdHocEventLogSchedule.class, validateMap);
		int count = fetchNumberOfDuplicateSchedulers(adHocEventLogSchedule);
		if (result.hasErrors() || (count > 0)) {
			adHocEventLogScheduleVO.setCronExpression(null);
			adHocEventLogScheduleVO.setSourceProduct(null);
			map.put(ADHOC_EVENT_LOG_SCHEDULE_VO, adHocEventLogScheduleVO);
			map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.masterEntityChangedByUser(adHocEventLogSchedule,
				user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
		}
		return REDIRECT_GRID;
	}
	
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('VIEW_ADHOCEVENTLOGSCHEDULE') or hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE') or hasAuthority('CHECKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewAdHocEventLogSchedule(@PathVariable("id") Long id, ModelMap map) 
	{
		UserInfo currentUser = getUserDetails();
		AdHocEventLogSchedule adHocEventLogSchedule = baseMasterService.getMasterEntityWithActionsById(AdHocEventLogSchedule.class, id, currentUser.getUserEntityId().getUri());
		updateMapForViewOrEdit(adHocEventLogSchedule,map);
		map.put("viewable", true);
		return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
	}


	@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/edit/{id}")
	public String editAdHocEventLogSchedule(@PathVariable("id") Long id, ModelMap map) {
		AdHocEventLogSchedule adHocEventLogSchedule = baseMasterService.getMasterEntityById(AdHocEventLogSchedule.class, id);		
		boolean recordPreviouslyApproved=checkIfRecordPreviouslyApproved(adHocEventLogSchedule);
		updateMapForViewOrEdit(adHocEventLogSchedule, map);
		map.put("editLink", recordPreviouslyApproved);
		map.put("edit", true);
		map.put("approvedEdit", recordPreviouslyApproved);
		return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
	}
	
	@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApprovalAdHocEventLogSchedule( 
			AdHocEventLogScheduleVO adHocEventLogScheduleVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		BaseLoggers.flowLogger.debug(SAVING_ADHOC_EVENT_LOG_SCHEDULE
				+ adHocEventLogScheduleVO);
		AdHocEventLogSchedule adHocEventLogSchedule = prepareDataForSaveOrSaveAndSendForApproval(adHocEventLogScheduleVO);
		validateMap.put(SCHEDULER_NAME,
				adHocEventLogSchedule.getSchedulerName());
		List<String> colNameList = baseMasterService.hasEntity(
				AdHocEventLogSchedule.class, validateMap);
		int count = fetchNumberOfDuplicateSchedulers(adHocEventLogSchedule);
		if (result.hasErrors() || (count > 0)) {
			adHocEventLogScheduleVO.setCronExpression(null);
			adHocEventLogScheduleVO.setSourceProduct(null);
			map.put(ADHOC_EVENT_LOG_SCHEDULE_VO, adHocEventLogScheduleVO);
			map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.saveAndSendForApproval(adHocEventLogSchedule, user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return ADHOC_EVENT_LOG_SCHEDULE_MASTER;
		}
		return REDIRECT_GRID;
	}
	
	
	private Map<String, Object> convertClassToVO(AdHocEventLogSchedule adHocEventLogSchedule) {
	    List <EventCode> eventCodeSelectedList = new ArrayList<EventCode>();
	    Map<String,Object> map=new HashMap<String, Object>();
		AdHocEventLogScheduleVO adHocEventLogScheduleVO = new AdHocEventLogScheduleVO();
		adHocEventLogScheduleVO.setId(adHocEventLogSchedule.getId());
		adHocEventLogScheduleVO.setActiveFlag(adHocEventLogSchedule.isActiveFlag());
		adHocEventLogScheduleVO.setCronBuilderSelector(adHocEventLogSchedule.getCronBuilderSelector());
		adHocEventLogScheduleVO.setCronExpression(adHocEventLogSchedule.getCronExpression());
		adHocEventLogScheduleVO.setMaintainExecutionLog(adHocEventLogSchedule.getMaintainExecutionLog());
		adHocEventLogScheduleVO.setRunOnHoliday(adHocEventLogSchedule.getRunOnHoliday());
		adHocEventLogScheduleVO.setSchedulerName(adHocEventLogSchedule.getSchedulerName());
		adHocEventLogScheduleVO.setSourceProduct(adHocEventLogSchedule.getSourceProduct().getCode());
		adHocEventLogScheduleVO.setEndDate(adHocEventLogSchedule.getEndDate());
		adHocEventLogScheduleVO.setGenerateMergedFile(adHocEventLogSchedule.getGenerateMergedFile());
		if (adHocEventLogSchedule.getEventCodeType().getCode().equals(ADHOC_EVENTCODE)){
			adHocEventLogScheduleVO.setAdHocFlag(true);
		} else {
			adHocEventLogScheduleVO.setAdHocFlag(false);
		}
		for (AdHocEventLogScheduleMapping adHocEventLogScheduleMapping:adHocEventLogSchedule.getAdHocEventLogScheduleMappings()) {
            eventCodeSelectedList.add(adHocEventLogScheduleMapping.getEventCode());
        }
        map.put(EVENT_CODE_SELECTED_LIST,eventCodeSelectedList);
		map.put(ADHOC_EVENT_LOG_SCHEDULE_VO,adHocEventLogScheduleVO);
		return map;
	}
	
	private boolean checkIfRecordPreviouslyApproved(AdHocEventLogSchedule adHocEventLogSchedule){
		boolean recordPreviouslyApproved=false;
		AdHocEventLogSchedule lastApprovedRecord = (AdHocEventLogSchedule) baseMasterService
				.getLastApprovedEntityByUnapprovedEntityId(adHocEventLogSchedule
						.getEntityId());
		if (lastApprovedRecord!=null ||adHocEventLogSchedule.getApprovalStatus()==0) {
			recordPreviouslyApproved = true;
		}
		return recordPreviouslyApproved;
	}
		
	private AdHocEventLogSchedule prepareDataForSaveOrSaveAndSendForApproval(
			AdHocEventLogScheduleVO adHocEventLogScheduleVO) {
		AdHocEventLogSchedule adHocEventLogSchedule = null;
		List<AdHocEventLogScheduleMapping> adHocEventLogScheduleMappings = new ArrayList<AdHocEventLogScheduleMapping>();
		boolean recordPreviouslyApproved = false;
		if (adHocEventLogScheduleVO.getId() != null) {
			AdHocEventLogSchedule persistedadHocEventLogSchedule = baseMasterService
					.getMasterEntityById(
							AdHocEventLogSchedule.class,
							adHocEventLogScheduleVO.getId());
			adHocEventLogSchedule = (AdHocEventLogSchedule) persistedadHocEventLogSchedule
						.cloneYourself(CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
			adHocEventLogSchedule.setId(persistedadHocEventLogSchedule.getId());
				recordPreviouslyApproved=checkIfRecordPreviouslyApproved(persistedadHocEventLogSchedule);
		} else {
			adHocEventLogSchedule = new AdHocEventLogSchedule();
		}

		if (recordPreviouslyApproved) {
			adHocEventLogSchedule
					.setActiveFlag(adHocEventLogScheduleVO
							.isActiveFlag());
		} else {
			adHocEventLogSchedule
					.setSchedulerName(adHocEventLogScheduleVO
							.getSchedulerName());
			adHocEventLogSchedule
					.setCronExpression(adHocEventLogScheduleVO
							.getCronExpression());
			adHocEventLogSchedule
					.setMaintainExecutionLog(adHocEventLogScheduleVO
							.getMaintainExecutionLog());
			adHocEventLogSchedule
					.setSourceProduct(genericParameterService.findByCode(adHocEventLogScheduleVO.getSourceProduct(), SourceProduct.class));
			adHocEventLogSchedule
					.setRunOnHoliday(adHocEventLogScheduleVO
							.getRunOnHoliday());
			adHocEventLogSchedule
					.setActiveFlag(adHocEventLogScheduleVO
							.isActiveFlag());
			adHocEventLogSchedule
					.setCronBuilderSelector(adHocEventLogScheduleVO
							.getCronBuilderSelector());
			adHocEventLogSchedule.setEndDate(adHocEventLogScheduleVO.getEndDate());
			adHocEventLogSchedule.setGenerateMergedFile(adHocEventLogScheduleVO.getGenerateMergedFile());
			if (adHocEventLogScheduleVO.isAdHocFlag()) {
				adHocEventLogSchedule.setEventCodeType(genericParameterService.findByCode(ADHOC_EVENTCODE, EventCodeType.class));				
			} else {
				adHocEventLogSchedule.setEventCodeType(genericParameterService.findByCode(TRANSACTION_BASED_EVENTCODE, EventCodeType.class));
			}
			List<EventCode> eventCodes = getEventCodeListFromIds(adHocEventLogScheduleVO
                    .getEventCodeIds());
            if (hasElements(eventCodes)) {
                for (EventCode eventCode : eventCodes) {
                    AdHocEventLogScheduleMapping adHocEventLogScheduleMapping = new AdHocEventLogScheduleMapping();
                    adHocEventLogScheduleMapping
                            .setEventCode(eventCode);
                    adHocEventLogScheduleMapping
                            .setSourceProduct(adHocEventLogSchedule
                                    .getSourceProduct());
                    adHocEventLogScheduleMappings
                            .add(adHocEventLogScheduleMapping);
                }
            }
            adHocEventLogSchedule
                    .setAdHocEventLogScheduleMappings(adHocEventLogScheduleMappings);			
		}
		return adHocEventLogSchedule;
	}
	
	private void updateModelMapForCreateOrUpdate(ModelMap map){
	    List<EventCode> eventCodeList=new ArrayList<EventCode>();
		communicationSchedulerHelper.getBasicInitParameters(map);
		map.put(EVENT_CODE_LIST, eventCodeList);
        map.put(ADHOC_EVENT_LOG_SCHEDULE_VO, new AdHocEventLogScheduleVO());
        map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);        
	}	
	
	private void updateMapForViewOrEdit(AdHocEventLogSchedule adHocEventLogSchedule,ModelMap map) {
		communicationSchedulerHelper.getBasicInitParameters(map);
		List<String> actions = (List<String>) adHocEventLogSchedule.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
        List<EventCode> eventCodeList=adHocEventLogCriteriaService.getEventCodesBasedOnModuleAndEventCodeType(adHocEventLogSchedule.getSourceProduct(), adHocEventLogSchedule.getEventCodeType());
		Map<String,Object> convertMap=convertClassToVO(adHocEventLogSchedule);
		List<EventCode> eventCodeSelectedList=(List<EventCode>) convertMap.get(EVENT_CODE_SELECTED_LIST);
        for(EventCode eventCode:eventCodeSelectedList) {
            eventCodeList.add(eventCode);
        }
        map.put(EVENT_CODE_LIST, eventCodeList);
        map.put(EVENT_CODE_SELECTED_LIST, eventCodeSelectedList);
        map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
		map.put(ADHOC_EVENT_LOG_SCHEDULE_VO, convertMap.get(ADHOC_EVENT_LOG_SCHEDULE_VO));
		map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
		map.put(END_DATE, adHocEventLogSchedule.getEndDate());
	}
	
	
	
/*	
	@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	   @RequestMapping(value = "/getEventCodeListBasedOnModule/{sourceProduct}", method = RequestMethod.GET)
	    public @ResponseBody
	    String getEventCodeListBasedOnModule(@PathVariable("sourceProduct") String sourceProduct,ModelMap map) {
	    	 JSONSerializer iSerializer = new JSONSerializer();
	    	 List<Map<String, ?>> par = new ArrayList<Map<String, ?>>();
	    	 List<EventCode> eventCodeList = adHocEventLogCriteriaService.getEventCodesBasedOnModule(sourceProduct);
	    	 for (EventCode eventCode:eventCodeList) {
	    		 Map<String, String> valueMap = new HashMap<String, String>();
	             valueMap.put("id", String.valueOf(eventCode.getId()));
	             valueMap.put("name", eventCode.getName());
	             par.add(valueMap);
	    	 }
	        return iSerializer.serialize(ComboBoxAdapterUtil.listOfMapsToSingleMap(par, "id", "name"));        
	    }*/
	
	protected EventCode getEventCodeFromId(Long eventCodeId) {
		return genericParameterService.findById(eventCodeId, EventCode.class);		
	}
	
	   @PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	    @RequestMapping(value = "/getUnMappedEventCodesBasedOnModule", method = RequestMethod.GET)
	    public String getUnMappedEventCodesBasedOnModule(@RequestParam(value = "sourceProduct", required = false) String sourceProductCode, @RequestParam(value = "adHocFlag", required = false) boolean adHocFlag,ModelMap map) throws IOException {
		  EventCodeType eventCodeType=null;
		   if (adHocFlag) {
	    	   eventCodeType=genericParameterService.findByCode(ADHOC_EVENTCODE, EventCodeType.class);
	       } else {
	    	   eventCodeType=genericParameterService.findByCode(TRANSACTION_BASED_EVENTCODE, EventCodeType.class);
	       }
		   SourceProduct sourceProduct=genericParameterService.findByCode(sourceProductCode, SourceProduct.class);
	        List<EventCode> eventCodeList=adHocEventLogCriteriaService.getEventCodesBasedOnModuleAndEventCodeType(sourceProduct, eventCodeType);
	        if (hasNoElements(eventCodeList)) {
	            eventCodeList= new ArrayList<EventCode>();
	        }
	        map.put(EVENT_CODE_LIST, eventCodeList);
	        map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
	        return COMMUNICATION_EVENT_CODE;        
	    }   
	   
	   protected List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds) {
	        return  communicationSchedulerService.getEventCodeListFromIds(eventCodeIds);
	        }
	
	protected int fetchNumberOfDuplicateSchedulers(
			AdHocEventLogSchedule adHocEventLogSchedule) {
		String uuid = null;
		int count = 0;
		if (notNull(adHocEventLogSchedule)) {
			uuid = adHocEventLogSchedule.getEntityLifeCycleData().getUuid();
		}
		String countScheduler =  adHocEventLogCriteriaService
				.fetchNumberOfDuplicateSchedulersOfAdhocCommunication(
						adHocEventLogSchedule.getSchedulerName(),
						adHocEventLogSchedule.getSourceProduct(),
						adHocEventLogSchedule.getId(), uuid);
		if (notNull(countScheduler)) {
			count = Integer.parseInt(countScheduler);
		}
		
		return count;
	}
	  
	/*@PreAuthorize("hasAuthority('MAKER_ADHOCEVENTLOGSCHEDULE')")
	@RequestMapping(value = "/getUnMappedEventCodesBasedOnModule", method = RequestMethod.GET)
	public String getUnMappedEventCodesBasedOnModule(@RequestParam(value = "sourceProduct", required = false) String sourceProduct, ModelMap map) throws IOException {
		List<EventCode> eventCodeList=getUnMappedEventCodesBasedOnModule(sourceProduct);
		if (hasNoElements(eventCodeList)) {
			eventCodeList= new ArrayList<EventCode>();
		}
		map.put(EVENT_CODE_LIST, eventCodeList);
		map.put(MASTER_ID, COMMUNICATION_EVENT_REQUEST_SCHEDULER);
		return COMMUNICATION_EVENT_CODE;		
	}	*/
	
	/*protected static Map<String, String> getDecodedCronExpression(String cronString) throws ParseException {
		Map<String, String> cronMap = new HashMap<String, String>();
		CronExpression cronExpression = new CronExpression(cronString);
		String[] cronUnits = cronExpression.getExpressionSummary().split("\\n");
		for(int i=0;i<cronUnits.length;i++){
			String[] keyValuePair = cronUnits[i].split(":");
			cronMap.put(keyValuePair[0].trim(), keyValuePair[1].trim());
		}
		return cronMap;
	}
	
	private String prepareJSONStringFromObject(Object object) {
		String jsOnString = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsOnString = mapper.writeValueAsString(object);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Exception: " + e.getMessage(), e);
		}
		return jsOnString;
	}*/
	
/*	private DynamicQueryCriteriaPojo updateFieldsInDynamicQueryCriteriaPojo(adHocEventLogScheduleVO adHocEventLogScheduleVO){
		DynamicQueryCriteriaPojo dynamicQueryCriteriaPojo = new DynamicQueryCriteriaPojo();
		dynamicQueryCriteriaPojo.setQueryContextId(adHocEventLogScheduleVO.getQueryContextId());
		dynamicQueryCriteriaPojo.setWhereClause(adHocEventLogScheduleVO.getWhereClause());
		return dynamicQueryCriteriaPojo;		
	}
	*/
	/*@RequestMapping(value = "/generateReport")
    public @ResponseBody
    HttpEntity<byte[]> generateDynamicReport(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws IOException,
            DRException {

        removeNullFromArray(queryCriteriaPojo);
        // first of all create HQL from dynamic neutrino query
        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);

        BaseLoggers.flowLogger.info("Processed dynamic report query and generated hql --> [{}]",
                queryWrapper.getHqlQueryString());

        DynamicReportConfig dynamicReportConfig = null;
        if (queryWrapper != null) {
            List<Map<String, Object>> list = entityDao.executeQuery(queryWrapper.getMapQueryExecuterWithAllParameterAdded());
            if (list != null) {
                BaseLoggers.flowLogger.info("Found {} results for dynamic report query.Now generating report {} file.",
                        list.size(), queryCriteriaPojo.getReportTitle());
                // create DynamicReportConfig
                dynamicReportConfig = new DynamicReportConfig(list, queryWrapper.getSelectedTokens());
                populateCommonFieldsInDynamicReportConfig(queryCriteriaPojo, dynamicReportConfig);

            }
        }

        DynamicReportPojo dynamicReportPojo = dynamicReportBuilder.generateReport(dynamicReportConfig);

        MediaType mediaType = MediaType.parseMediaType(dynamicReportPojo.getMediaType());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);
        responseHeaders.setContentDispositionFormData("attachment", dynamicReportPojo.getFileName());
        HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(dynamicReportPojo.getReportData(), responseHeaders);
        return fileEntity;
    }*/
    
	
	
	
	
 /*   private void removeNullFromArray(DynamicQueryCriteriaPojo queryCriteriaPojo) {
        queryCriteriaPojo.setSelectItemIds(ArrayUtils.removeElement(queryCriteriaPojo.getSelectItemIds(), null));
        queryCriteriaPojo.setSumForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getSumForTokenIds(), null));
        queryCriteriaPojo.setAvgForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getAvgForTokenIds(), null));
        queryCriteriaPojo.setPercentageForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getPercentageForTokenIds(),
                null));
        queryCriteriaPojo.setCountForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getCountForTokenIds(), null));
    }*/

/*    private DynamicQueryWrapper processQuery(DynamicQueryCriteriaPojo queryCriteriaPojo) {

        if (StringUtils.isNoneBlank(queryCriteriaPojo.getWhereClause())) {
            QueryContext queryContext = entityDao.find(QueryContext.class, queryCriteriaPojo.getQueryContextId());

            Set<Long> consolidatedIds = new HashSet<Long>();
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getSelectItemIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getSumForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getAvgForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getPercentageForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getCountForTokenIds());
            // if grouping is not required
            if (queryCriteriaPojo.getGroupByTokenId() != null) {
                Collections.addAll(consolidatedIds, (queryCriteriaPojo.getGroupByTokenId()));
            }

            // for chart config
             if (queryCriteriaPojo.getKeyTokenId() != null) {
                 Collections.addAll(consolidatedIds, (queryCriteriaPojo.getKeyTokenId()));
             }
             Collections.addAll(consolidatedIds, (queryCriteriaPojo.getSeriesTokenIds()));

            return queryTranslatorService.processQuery(queryCriteriaPojo.getWhereClause(), queryContext,
                    new ArrayList<Long>(consolidatedIds), false);
        }
        return null;

    }*/
    
/*    private void initializeQueryContexts(ModelMap map) {
        List<QueryContext> contexts = entityDao.findAll(QueryContext.class);
        Map<Long, String> queryTokens = queryMetadataService.getAllTokensIdNameMapWithContextIdAndType(20000L,
                Arrays.asList(QueryToken.SELECT_TYPE));
        map.put(SELECTED_ITEM_LIST, queryTokens);
        map.put(QUERY_CONTEXT_LIST, contexts);
    }
    
    private void updateDynamicQueryWrapper(adHocEventLogSchedule adHocEventLogSchedule) {
    	 DynamicQueryCriteriaPojo queryCriteriaPojo=new DynamicQueryCriteriaPojo();
    	 queryCriteriaPojo.setQueryContextId(adHocEventLogSchedule.getDynamicQueryAdHocSchedulerMapping().getQueryContextId());
    	 queryCriteriaPojo.setWhereClause(adHocEventLogSchedule.getDynamicQueryAdHocSchedulerMapping().getWhereClause());
    	 removeNullFromArray(queryCriteriaPojo);
         // first of all create HQL from dynamic neutrino query
         DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);
    }*/
    
	
    
/*	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/getUnMappedEventCodesBasedOnModule", method = RequestMethod.GET)
	public String getUnMappedEventCodesBasedOnModule(@RequestParam(value = "sourceProduct", required = false) String sourceProduct, ModelMap map) throws IOException {
		List<EventCode> eventCodeList=getUnMappedEventCodesBasedOnModule(sourceProduct);
		if (hasNoElements(eventCodeList)) {
			eventCodeList= new ArrayList<EventCode>();
		}
		map.put(EVENT_CODE_LIST, eventCodeList);
		map.put(MASTER_ID, ADHOC_EVENT_LOG_SCHEDULE);
		return COMMUNICATION_EVENT_CODE;		
	}*/	
}
