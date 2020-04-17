package com.nucleus.web.communicationscheduler.controller;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

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
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestSchedulerMapping;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationSchedulerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationSchedulerHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationEventRequestSchedulerVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;


@Transactional
@Controller
@RequestMapping(value = "/CommunicationEventRequestScheduler")
public class CommunicationEventRequestSchedulerController extends BaseController{

	
	
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
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
	
	
	
	private static final String EVENT_CODE_LIST="eventCodeList";
	private static final String COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO="communicationEventRequestSchedulerVO";
	private static final String COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER="communicationEventRequestSchedulerMaster";
	private static final String EVENT_CODE_SELECTED_LIST="eventCodeSelectedList";
	private static final String COMMUNICATION_EVENT_REQUEST_SCHEDULER="CommunicationEventRequestScheduler";
	private static final String MASTER_ID="masterID";
	private static final String SCHEDULER_NAME="schedulerName";
	private static final String SAVING_COMMN_EVENT_REQ_SCHEDULER="Saving CommunicationEventRequestSchedulerVO Details-->";
	private static final String REDIRECT_GRID="redirect:/app/grid/CommunicationEventRequestScheduler/CommunicationEventRequestScheduler/loadColumnConfig";
	private static final String COMMUNICATION_EVENT_CODE="eventCodeListFragment";
	private static final String CRON_MAP="cronMap";
	private static final String END_DATE="endDate";
	
 
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/create")
	public String createCommunicationEventRequestScheduler(ModelMap map) {
		updateModelMapForCreateOrUpdate(map);
		return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveCommunicationEventRequestScheduler(
			CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		BaseLoggers.flowLogger.debug(SAVING_COMMN_EVENT_REQ_SCHEDULER
				+ communicationEventRequestSchedulerVO);
		Map<String, Object> validateMap = new HashMap<String, Object>();
		CommunicationEventRequestScheduler communicationEventRequestScheduler = prepareDataForSaveOrSaveAndSendForApproval(communicationEventRequestSchedulerVO);
		validateMap.put(SCHEDULER_NAME,
				communicationEventRequestScheduler.getSchedulerName());
	
		List<String> colNameList = baseMasterService.hasEntity(
				CommunicationEventRequestScheduler.class, validateMap);
		int count = fetchNumberOfDuplicateSchedulers(communicationEventRequestScheduler);

		if (result.hasErrors() || (count > 0)) {			
			updateMapForDuplicateSchedulerName(communicationEventRequestSchedulerVO,map);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.masterEntityChangedByUser(
				communicationEventRequestScheduler, user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
		}
		return REDIRECT_GRID;
	}
	
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('VIEW_COMMUNICATIONEVENTREQUESTSCHEDULER') or hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER') or hasAuthority('CHECKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewCommunicationEventRequestScheduler(@PathVariable("id") Long id, ModelMap map) 
	{
		UserInfo currentUser = getUserDetails();
		CommunicationEventRequestScheduler communicationEventRequestScheduler = baseMasterService.getMasterEntityWithActionsById(CommunicationEventRequestScheduler.class, id, currentUser.getUserEntityId().getUri());
		updateMapForViewOrEdit(communicationEventRequestScheduler,map);
		map.put("viewable", true);

		return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
	}


	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/edit/{id}")
	public String editCommunicationEventRequestScheduler(@PathVariable("id") Long id, ModelMap map) {
		CommunicationEventRequestScheduler communicationEventRequestScheduler = baseMasterService.getMasterEntityById(CommunicationEventRequestScheduler.class, id);		
		boolean recordPreviouslyApproved=checkIfRecordPreviouslyApproved(communicationEventRequestScheduler);
		updateMapForViewOrEdit(communicationEventRequestScheduler, map);
		map.put("editLink", recordPreviouslyApproved);
		map.put("edit", true);
		map.put("approvedEdit", recordPreviouslyApproved);
		/*try {
			map.put(CRON_MAP, prepareJSONStringFromObject(getDecodedCronExpression(communicationEventRequestScheduler.getCronExpression())));
		} catch (Exception ex) {
		    BaseLoggers.exceptionLogger.error(ex.getMessage(),ex);
			map.put(CRON_MAP, null);
		}*/
		return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApprovalCommunicationEventRequestScheduler(
			@Validated CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		 Map<String, Object> validateMap = new HashMap<String, Object>();
		BaseLoggers.flowLogger
				.debug(SAVING_COMMN_EVENT_REQ_SCHEDULER
						+ communicationEventRequestSchedulerVO);
		CommunicationEventRequestScheduler communicationEventRequestScheduler = prepareDataForSaveOrSaveAndSendForApproval(communicationEventRequestSchedulerVO);
			validateMap.put(SCHEDULER_NAME,
					communicationEventRequestScheduler.getSchedulerName());
			List<String> colNameList = baseMasterService.hasEntity(
					CommunicationEventRequestScheduler.class, validateMap);
			int count = fetchNumberOfDuplicateSchedulers(communicationEventRequestScheduler);
			if (result.hasErrors()
					|| (count > 0)) {
			updateMapForDuplicateSchedulerName(communicationEventRequestSchedulerVO,map);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.saveAndSendForApproval(
				communicationEventRequestScheduler, user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return  COMMUNICATION_EVENT_REQUEST_SCHEDULER_MASTER;
		}
		return REDIRECT_GRID;
	}
	
	protected List<EventCode> getUnMappedEventCodesBasedOnModule(SourceProduct sourceProduct) {
	return communicationSchedulerService.getUnMappedEventCodesBasedOnModule(sourceProduct);	 
	}
	
	protected List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds) {
		return  communicationSchedulerService.getEventCodeListFromIds(eventCodeIds);
		}
	
	private Map<String, Object> convertClassToVO(CommunicationEventRequestScheduler communicationEventRequestScheduler) {
		List <EventCode> eventCodeSelectedList = new ArrayList<EventCode>();
		Map<String,Object> map=new HashMap<String, Object>();
		CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO = new CommunicationEventRequestSchedulerVO();
		communicationEventRequestSchedulerVO.setId(communicationEventRequestScheduler.getId());
		communicationEventRequestSchedulerVO.setActiveFlag(communicationEventRequestScheduler.isActiveFlag());
		communicationEventRequestSchedulerVO.setCronBuilderSelector(communicationEventRequestScheduler.getCronBuilderSelector());
		communicationEventRequestSchedulerVO.setCronExpression(communicationEventRequestScheduler.getCronExpression());
		communicationEventRequestSchedulerVO.setMaintainExecutionLog(communicationEventRequestScheduler.getMaintainExecutionLog());
		communicationEventRequestSchedulerVO.setRunOnHoliday(communicationEventRequestScheduler.getRunOnHoliday());
		communicationEventRequestSchedulerVO.setSchedulerName(communicationEventRequestScheduler.getSchedulerName());
		communicationEventRequestSchedulerVO.setSourceProduct(communicationEventRequestScheduler.getSourceProduct().getCode());
		communicationEventRequestSchedulerVO.setEndDate(communicationEventRequestScheduler.getEndDate());
		for (CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping:communicationEventRequestScheduler.getCommunicationEventRequestSchedulerMappings()) {
			eventCodeSelectedList.add(communicationEventRequestSchedulerMapping.getEventCode());
		}
		map.put(COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO,communicationEventRequestSchedulerVO);
		map.put(EVENT_CODE_SELECTED_LIST,eventCodeSelectedList);
		return map;
	}
	
	private boolean checkIfRecordPreviouslyApproved(CommunicationEventRequestScheduler communicationEventRequestScheduler){
		boolean recordPreviouslyApproved=false;
		CommunicationEventRequestScheduler lastApprovedRecord = (CommunicationEventRequestScheduler) baseMasterService
				.getLastApprovedEntityByUnapprovedEntityId(communicationEventRequestScheduler
						.getEntityId());
		if (lastApprovedRecord!=null ||communicationEventRequestScheduler.getApprovalStatus()==0) {
			recordPreviouslyApproved = true;
		}
		return recordPreviouslyApproved;
	}
		
	private CommunicationEventRequestScheduler prepareDataForSaveOrSaveAndSendForApproval(
			CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO) {
		CommunicationEventRequestScheduler communicationEventRequestScheduler = null;
		List<CommunicationEventRequestSchedulerMapping> communicationEventRequestSchedulerMappings = new ArrayList<CommunicationEventRequestSchedulerMapping>();
		boolean recordPreviouslyApproved = false;
		if (communicationEventRequestSchedulerVO.getId() != null) {
			CommunicationEventRequestScheduler persistedCommunicationEventRequestScheduler = baseMasterService
					.getMasterEntityById(
							CommunicationEventRequestScheduler.class,
							communicationEventRequestSchedulerVO.getId());
				communicationEventRequestScheduler = (CommunicationEventRequestScheduler) persistedCommunicationEventRequestScheduler
						.cloneYourself(CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
				communicationEventRequestScheduler.setId(persistedCommunicationEventRequestScheduler.getId());
				recordPreviouslyApproved=checkIfRecordPreviouslyApproved(persistedCommunicationEventRequestScheduler);
		} else {
			communicationEventRequestScheduler = new CommunicationEventRequestScheduler();
		}

		if (recordPreviouslyApproved) {
			communicationEventRequestScheduler
					.setActiveFlag(communicationEventRequestSchedulerVO
							.isActiveFlag());
		} else {
			communicationEventRequestScheduler
					.setSchedulerName(communicationEventRequestSchedulerVO
							.getSchedulerName());
			communicationEventRequestScheduler
					.setCronExpression(communicationEventRequestSchedulerVO
							.getCronExpression());
			communicationEventRequestScheduler
					.setMaintainExecutionLog(communicationEventRequestSchedulerVO
							.getMaintainExecutionLog());
			/*communicationEventRequestScheduler
					.setSourceProduct(genericParameterService.findByCode(ProductInformationLoader.getProductName(), SourceProduct.class));*/
			
			communicationEventRequestScheduler
			.setSourceProduct(genericParameterService.findByCode(communicationEventRequestSchedulerVO.getSourceProduct(), SourceProduct.class));
			
			
			
			
			communicationEventRequestScheduler
					.setRunOnHoliday(communicationEventRequestSchedulerVO
							.getRunOnHoliday());
			communicationEventRequestScheduler
					.setActiveFlag(communicationEventRequestSchedulerVO
							.isActiveFlag());
			communicationEventRequestScheduler
					.setCronBuilderSelector(communicationEventRequestSchedulerVO
							.getCronBuilderSelector());			
			communicationEventRequestScheduler.setEndDate(communicationEventRequestSchedulerVO.getEndDate());
			List<EventCode> eventCodes = getEventCodeListFromIds(communicationEventRequestSchedulerVO
					.getEventCodeIds());
			if (hasElements(eventCodes)) {
				for (EventCode eventCode : eventCodes) {
					CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping = new CommunicationEventRequestSchedulerMapping();
					communicationEventRequestSchedulerMapping
							.setEventCode(eventCode);
					communicationEventRequestSchedulerMapping
							.setSourceProduct(communicationEventRequestScheduler
									.getSourceProduct());
					communicationEventRequestSchedulerMappings
							.add(communicationEventRequestSchedulerMapping);
				}
			}
			communicationEventRequestScheduler
					.setCommunicationEventRequestSchedulerMappings(communicationEventRequestSchedulerMappings);
		}
		return communicationEventRequestScheduler;
	}
	
	private void updateModelMapForCreateOrUpdate(ModelMap map){
		List<EventCode> eventCodeList=new ArrayList<EventCode>();
		communicationSchedulerHelper.getBasicInitParameters(map);	   	
	   	CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO=new CommunicationEventRequestSchedulerVO();
	   	if(notNull(ProductInformationLoader.getProductName()))
        {
	   	communicationEventRequestSchedulerVO.setSourceProduct(ProductInformationLoader.getProductName());
        }
	   	try {
	   	map.put(EVENT_CODE_LIST, getUnMappedEventCodesBasedOnModule(genericParameterService.findByCode(communicationEventRequestSchedulerVO.getSourceProduct(), SourceProduct.class)));
	   	} catch (Exception ex) {
	   		BaseLoggers.flowLogger
			.error("Source Product is not Defined" +ex);
	   	}	   	
        map.put(COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO, communicationEventRequestSchedulerVO);
        map.put(MASTER_ID, COMMUNICATION_EVENT_REQUEST_SCHEDULER);        
	}	
	
	private void updateMapForViewOrEdit(CommunicationEventRequestScheduler communicationEventRequestScheduler,ModelMap map) {
		communicationSchedulerHelper.getBasicInitParameters(map);
		List<String> actions = (List<String>) communicationEventRequestScheduler.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
		Map<String,Object> convertMap=convertClassToVO(communicationEventRequestScheduler);
		List<EventCode> eventCodeList=getUnMappedEventCodesBasedOnModule(communicationEventRequestScheduler.getSourceProduct());
		map.put(COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO, convertMap.get(COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO));
		map.put(MASTER_ID, COMMUNICATION_EVENT_REQUEST_SCHEDULER);
		List<EventCode> eventCodeSelectedList=(List<EventCode>) convertMap.get(EVENT_CODE_SELECTED_LIST);
		for(EventCode eventCode:eventCodeSelectedList) {
			eventCodeList.add(eventCode);
		}
		map.put(EVENT_CODE_LIST, eventCodeList);
		map.put(EVENT_CODE_SELECTED_LIST, eventCodeSelectedList);
		map.put(END_DATE, communicationEventRequestScheduler.getEndDate());
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONEVENTREQUESTSCHEDULER')")
	@RequestMapping(value = "/getUnMappedEventCodesBasedOnModule", method = RequestMethod.GET)
	public String getUnMappedEventCodesBasedOnModule(@RequestParam(value = "sourceProduct", required = false) String sourceProductCode, ModelMap map) throws IOException {
		SourceProduct sourceProduct=genericParameterService.findByCode(sourceProductCode, SourceProduct.class);
	    List<EventCode> eventCodeList=getUnMappedEventCodesBasedOnModule(sourceProduct);
		if (hasNoElements(eventCodeList)) {
			eventCodeList= new ArrayList<EventCode>();
		}
		map.put(EVENT_CODE_LIST, eventCodeList);
		map.put(MASTER_ID, COMMUNICATION_EVENT_REQUEST_SCHEDULER);
		return COMMUNICATION_EVENT_CODE;		
	}	
	
	protected int fetchNumberOfDuplicateSchedulers(
			CommunicationEventRequestScheduler communicationEventRequestScheduler) {
		String uuid = null;
		int count = 0;
		if (notNull(communicationEventRequestScheduler)) {
			uuid = communicationEventRequestScheduler.getEntityLifeCycleData().getUuid();
		}
		String countScheduler= communicationSchedulerService.fetchNumberOfDuplicateSchedulers(
				communicationEventRequestScheduler.getSchedulerName(),
				true,
				communicationEventRequestScheduler.getSourceProduct(),
				communicationEventRequestScheduler.getId(), uuid);
		if (notNull(countScheduler)) {
			count=Integer.parseInt(countScheduler);
		}
				
		return count;
	}
	
	protected void updateMapForDuplicateSchedulerName(CommunicationEventRequestSchedulerVO communicationEventRequestSchedulerVO,ModelMap map) {
		communicationEventRequestSchedulerVO.setCronExpression(null);
		/*communicationEventRequestSchedulerVO
				.setSourceProduct(ProductInformationLoader.getProductName());*/
		
		communicationEventRequestSchedulerVO
		.setSourceProduct(communicationEventRequestSchedulerVO.getSourceProduct());
		map.put(EVENT_CODE_LIST,
				getUnMappedEventCodesBasedOnModule(genericParameterService
						.findByCode(communicationEventRequestSchedulerVO
								.getSourceProduct(), SourceProduct.class)));
		map.put(EVENT_CODE_SELECTED_LIST,
				getEventCodeListFromIds(communicationEventRequestSchedulerVO
						.getEventCodeIds()));
		map.put(COMMUNICATION_EVENT_REQUEST_SCHEDULER_VO,
				communicationEventRequestSchedulerVO);
		map.put(MASTER_ID, COMMUNICATION_EVENT_REQUEST_SCHEDULER);
	}
	
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

}
