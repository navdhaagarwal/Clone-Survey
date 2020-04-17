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

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationSchedulerMapping;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationSchedulerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationSchedulerHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationSchedulerVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;


@Transactional
@Controller
@RequestMapping(value = "/CommunicationGenerationScheduler")
public class CommunicationGenerationSchedulerController extends BaseController{

	
	
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
	
	private static final String COMMUNICATION_LIST="communicationList";
	private static final String COMMUNICATION_GENERATION_SCHEDULER_VO="communicationGenerationSchedulerVO";
	private static final String COMMUNICATION_GENERATION_SCHEDULER_MASTER="communicationGenerationSchedulerMaster";
	private static final String COMMUNICATION_SELECTED_LIST="communicationSelectedList";
	private static final String COMMUNICATION_GENERATION_SCHEDULER="CommunicationGenerationScheduler";
	private static final String MASTER_ID="masterID";
	private static final String SCHEDULER_NAME="schedulerName";
	private static final String SAVING_COMMN_GEN_SCHEDULER="Saving CommunicationGenerationSchedulerVO Details-->";
	private static final String REDIRECT_GRID="redirect:/app/grid/CommunicationGenerationScheduler/CommunicationGenerationScheduler/loadColumnConfig";
	private static final String COMMUNICATION_FRAGMENT="communicationListFragment";
	private static final String END_DATE="endDate";
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/create")
	public String createCommunicationGenerationScheduler(ModelMap map) {
		 updateModelMapForCreateOrUpdate(map);
		return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveCommunicationGenerationScheduler(
			CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		BaseLoggers.flowLogger.debug(SAVING_COMMN_GEN_SCHEDULER
				+ communicationGenerationSchedulerVO);
		Map<String, Object> validateMap = new HashMap<String, Object>();
		CommunicationGenerationScheduler communicationGenerationScheduler = prepareDataForSaveOrSaveAndSendForApproval(communicationGenerationSchedulerVO);
		validateMap.put(SCHEDULER_NAME,
				communicationGenerationScheduler.getSchedulerName());
		List<String> colNameList = baseMasterService.hasEntity(
				CommunicationGenerationScheduler.class, validateMap);
		int count = fetchNumberOfDuplicateSchedulers(communicationGenerationScheduler);

		if (result.hasErrors() || (count > 0)) {
			updateMapForDuplicateSchedulerName(communicationGenerationSchedulerVO,map);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.masterEntityChangedByUser(
				communicationGenerationScheduler, user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
		}
		return REDIRECT_GRID;
	}	
	
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('VIEW_COMMUNICATIONGENERATIONSCHEDULER') or hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER') or hasAuthority('CHECKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewCommunicationGenerationScheduler(@PathVariable("id") Long id, ModelMap map) 
	{
		UserInfo currentUser = getUserDetails();
		CommunicationGenerationScheduler communicationGenerationScheduler = baseMasterService.getMasterEntityWithActionsById(CommunicationGenerationScheduler.class, id, currentUser.getUserEntityId().getUri());
		updateMapForViewOrEdit(communicationGenerationScheduler, map);
		map.put("viewable", true);		
		return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
	}


	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/edit/{id}")
	public String editCommunicationGenerationScheduler(@PathVariable("id") Long id, ModelMap map) {
		CommunicationGenerationScheduler communicationGenerationScheduler = baseMasterService.getMasterEntityById(CommunicationGenerationScheduler.class, id);		
		boolean recordPreviouslyApproved=checkIfRecordPreviouslyApproved(communicationGenerationScheduler);
		updateMapForViewOrEdit(communicationGenerationScheduler, map);
		map.put("editLink", recordPreviouslyApproved);
		map.put("edit", true);
		map.put("approvedEdit", recordPreviouslyApproved);
		return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApprovalCommunicationGenerationScheduler(
			@Validated CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO,
			BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		BaseLoggers.flowLogger.debug(SAVING_COMMN_GEN_SCHEDULER
				+ communicationGenerationSchedulerVO);
		CommunicationGenerationScheduler communicationGenerationScheduler = prepareDataForSaveOrSaveAndSendForApproval(communicationGenerationSchedulerVO);
		validateMap.put(SCHEDULER_NAME,
				communicationGenerationScheduler.getSchedulerName());
		List<String> colNameList = baseMasterService.hasEntity(
				CommunicationGenerationScheduler.class, validateMap);
		int count = fetchNumberOfDuplicateSchedulers(communicationGenerationScheduler);

		if (result.hasErrors() || (count > 0)) {
			updateMapForDuplicateSchedulerName(communicationGenerationSchedulerVO,map);
			result.rejectValue(colNameList.get(0),
					"label." + colNameList.get(0) + ".validation.exists");
			return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
		}
		User user = getUserDetails().getUserReference();
		makerCheckerService.saveAndSendForApproval(
				communicationGenerationScheduler, user);
		if (createAnotherMaster) {
			updateModelMapForCreateOrUpdate(map);
			return COMMUNICATION_GENERATION_SCHEDULER_MASTER;
		}
		return REDIRECT_GRID;
	}
	
		private List<CommunicationName> getUnMappedCommunicationsBasedOnModule(SourceProduct sourceProduct) {
		return communicationSchedulerService.getUnMappedCommunicationsBasedOnModule(sourceProduct);
		}
		
		private List<CommunicationName> getCommunicationListFromIds(Long[] communicationIds) {
			return communicationSchedulerService.getCommunicationListFromIds(communicationIds);	 
			}
	
	private Map<String, Object> convertClassToVO(CommunicationGenerationScheduler communicationGenerationScheduler) {
		List <CommunicationName> communicationSelectedList = new ArrayList<CommunicationName>();
		Map<String,Object> map=new HashMap<String, Object>();
		CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO = new CommunicationGenerationSchedulerVO();
		communicationGenerationSchedulerVO.setId(communicationGenerationScheduler.getId());
		communicationGenerationSchedulerVO.setActiveFlag(communicationGenerationScheduler.isActiveFlag());
		communicationGenerationSchedulerVO.setCronBuilderSelector(communicationGenerationScheduler.getCronBuilderSelector());
		communicationGenerationSchedulerVO.setCronExpression(communicationGenerationScheduler.getCronExpression());
		communicationGenerationSchedulerVO.setMaintainExecutionLog(communicationGenerationScheduler.getMaintainExecutionLog());
		communicationGenerationSchedulerVO.setRunOnHoliday(communicationGenerationScheduler.getRunOnHoliday());
		communicationGenerationSchedulerVO.setSchedulerName(communicationGenerationScheduler.getSchedulerName());
		communicationGenerationSchedulerVO.setSourceProduct(communicationGenerationScheduler.getSourceProduct().getCode());
		communicationGenerationSchedulerVO.setEndDate(communicationGenerationScheduler.getEndDate());
		for (CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping:communicationGenerationScheduler.getCommunicationGenerationSchedulerMappings()) {
			communicationSelectedList.add(communicationGenerationSchedulerMapping.getCommunication());
		}
		map.put(COMMUNICATION_GENERATION_SCHEDULER_VO,communicationGenerationSchedulerVO);
		map.put(COMMUNICATION_SELECTED_LIST,communicationSelectedList);
		return map;
	}
	
	private boolean checkIfRecordPreviouslyApproved(CommunicationGenerationScheduler communicationGenerationScheduler){
		boolean recordPreviouslyApproved=false;
		CommunicationGenerationScheduler lastApprovedRecord = (CommunicationGenerationScheduler) baseMasterService
				.getLastApprovedEntityByUnapprovedEntityId(communicationGenerationScheduler
						.getEntityId());
		if (lastApprovedRecord!=null ||communicationGenerationScheduler.getApprovalStatus()==0) {
			recordPreviouslyApproved = true;
		}
		return recordPreviouslyApproved;
	}
		
	private CommunicationGenerationScheduler prepareDataForSaveOrSaveAndSendForApproval(
			CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO) {
		CommunicationGenerationScheduler communicationGenerationScheduler = null;
		List<CommunicationGenerationSchedulerMapping> communicationGenerationSchedulerMappings = new ArrayList<CommunicationGenerationSchedulerMapping>();
		boolean recordPreviouslyApproved = false;
		if (communicationGenerationSchedulerVO.getId() != null) {
			CommunicationGenerationScheduler persistedCommunicationGenerationScheduler = baseMasterService
					.getMasterEntityById(
							CommunicationGenerationScheduler.class,
							communicationGenerationSchedulerVO.getId());
				communicationGenerationScheduler = (CommunicationGenerationScheduler) persistedCommunicationGenerationScheduler
						.cloneYourself(CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
				communicationGenerationScheduler.setId(persistedCommunicationGenerationScheduler.getId());
				recordPreviouslyApproved=checkIfRecordPreviouslyApproved(persistedCommunicationGenerationScheduler);
		} else {
			communicationGenerationScheduler = new CommunicationGenerationScheduler();
		}

		if (recordPreviouslyApproved) {
			communicationGenerationScheduler
					.setActiveFlag(communicationGenerationSchedulerVO
							.isActiveFlag());
		} else {
			communicationGenerationScheduler
					.setSchedulerName(communicationGenerationSchedulerVO
							.getSchedulerName());
			communicationGenerationScheduler
					.setCronExpression(communicationGenerationSchedulerVO
							.getCronExpression());
			communicationGenerationScheduler
					.setMaintainExecutionLog(communicationGenerationSchedulerVO
							.getMaintainExecutionLog());
			/*communicationGenerationScheduler
					.setSourceProduct(genericParameterService.findByCode(ProductInformationLoader.getProductName(), SourceProduct.class));
			*/
			communicationGenerationScheduler
			.setSourceProduct(genericParameterService.findByCode(communicationGenerationSchedulerVO.getSourceProduct(), SourceProduct.class));
			communicationGenerationScheduler
					.setRunOnHoliday(communicationGenerationSchedulerVO
							.getRunOnHoliday());
			communicationGenerationScheduler
					.setActiveFlag(communicationGenerationSchedulerVO
							.isActiveFlag());
			communicationGenerationScheduler
					.setCronBuilderSelector(communicationGenerationSchedulerVO
							.getCronBuilderSelector());
			communicationGenerationScheduler.setEndDate(communicationGenerationSchedulerVO.getEndDate());
			List<CommunicationName> communications = getCommunicationListFromIds(communicationGenerationSchedulerVO
					.getCommunicationIds());

			if (hasElements(communications)) {
				for (CommunicationName communication : communications) {
					CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping = new CommunicationGenerationSchedulerMapping();
					communicationGenerationSchedulerMapping
							.setCommunication(communication);
					communicationGenerationSchedulerMapping
							.setSourceProduct(communicationGenerationScheduler
									.getSourceProduct());
					communicationGenerationSchedulerMappings
							.add(communicationGenerationSchedulerMapping);
				}
			}
			communicationGenerationScheduler
					.setCommunicationGenerationSchedulerMappings(communicationGenerationSchedulerMappings);
		}
		return communicationGenerationScheduler;
	}
	
	private void updateModelMapForCreateOrUpdate(ModelMap map){
	 	List<CommunicationName> communicationList=new ArrayList<CommunicationName>();
	 	communicationSchedulerHelper.getBasicInitParameters(map);
	   	CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO=new CommunicationGenerationSchedulerVO();
	   	if(notNull(ProductInformationLoader.getProductName()))
        {
	   		communicationGenerationSchedulerVO.setSourceProduct(ProductInformationLoader.getProductName());
        }
	   	try {
	   	map.put(COMMUNICATION_LIST, getUnMappedCommunicationsBasedOnModule(genericParameterService.findByCode(communicationGenerationSchedulerVO.getSourceProduct(), SourceProduct.class)));
	   	} catch (Exception ex) {
	   		BaseLoggers.flowLogger
			.error("Source Product is not Defined" +ex);
	   	}	
        map.put(COMMUNICATION_GENERATION_SCHEDULER_VO, communicationGenerationSchedulerVO);
        map.put(MASTER_ID, COMMUNICATION_GENERATION_SCHEDULER);        
	}
	
	private void updateMapForViewOrEdit(CommunicationGenerationScheduler communicationGenerationScheduler,ModelMap map) {
		communicationSchedulerHelper.getBasicInitParameters(map);
		List<String> actions = (List<String>) communicationGenerationScheduler.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
		Map<String,Object> convertMap=convertClassToVO(communicationGenerationScheduler);
		List<CommunicationName> communicationList=getUnMappedCommunicationsBasedOnModule(communicationGenerationScheduler.getSourceProduct());
		map.put(COMMUNICATION_GENERATION_SCHEDULER_VO, convertMap.get(COMMUNICATION_GENERATION_SCHEDULER_VO));
		map.put(MASTER_ID, COMMUNICATION_GENERATION_SCHEDULER);
		List<CommunicationName> communicationSelectedList=(List<CommunicationName>) convertMap.get(COMMUNICATION_SELECTED_LIST);
		for(CommunicationName communication:communicationSelectedList) {
			communicationList.add(communication);
		}
		map.put(COMMUNICATION_LIST, communicationList);
		map.put(COMMUNICATION_SELECTED_LIST, communicationSelectedList);
		map.put(END_DATE, communicationGenerationScheduler.getEndDate());
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONGENERATIONSCHEDULER')")
	@RequestMapping(value = "/getUnMappedCommunicationsBasedOnModule", method = RequestMethod.GET)
	public String getUnMappedCommunicationsBasedOnModule(@RequestParam(value = "sourceProduct", required = false) String sourceProductCode, ModelMap map) throws IOException {
	    SourceProduct sourceProduct=genericParameterService.findByCode(sourceProductCode, SourceProduct.class);
	    List<CommunicationName> communicationList=getUnMappedCommunicationsBasedOnModule(sourceProduct);
		if (hasNoElements(communicationList)) {
			communicationList= new ArrayList<CommunicationName>();
		}
		map.put(COMMUNICATION_LIST, communicationList);
		map.put(MASTER_ID, COMMUNICATION_GENERATION_SCHEDULER);
		return COMMUNICATION_FRAGMENT;		
	}	
	
	protected int fetchNumberOfDuplicateSchedulers(
			CommunicationGenerationScheduler communicationGenerationScheduler) {
		String uuid = null;
		int count=0;
		if (notNull(communicationGenerationScheduler)) {
			uuid = communicationGenerationScheduler.getEntityLifeCycleData().getUuid();
		}
		String countScheduler= communicationSchedulerService.fetchNumberOfDuplicateSchedulers(
				communicationGenerationScheduler.getSchedulerName(),
				false,
				communicationGenerationScheduler.getSourceProduct(),
				communicationGenerationScheduler.getId(), uuid);
		if (notNull(countScheduler)) {
			count=Integer.parseInt(countScheduler);
		}
		return count;
	}
	
	protected void updateMapForDuplicateSchedulerName(
			CommunicationGenerationSchedulerVO communicationGenerationSchedulerVO,
			ModelMap map) {
		communicationGenerationSchedulerVO.setCronExpression(null);
		/*
		communicationGenerationSchedulerVO
				.setSourceProduct(ProductInformationLoader.getProductName());
		*/
		communicationGenerationSchedulerVO
		.setSourceProduct(communicationGenerationSchedulerVO.getSourceProduct());
		/*map.put(COMMUNICATION_LIST,
				getUnMappedCommunicationsBasedOnModule(genericParameterService
						.findByCode(ProductInformationLoader.getProductName()
							 , SourceProduct.class)))*/;
		
		map.put(COMMUNICATION_LIST,
				getUnMappedCommunicationsBasedOnModule(genericParameterService
						.findByCode(communicationGenerationSchedulerVO.getSourceProduct()
							 , SourceProduct.class)));
		
		map.put(COMMUNICATION_SELECTED_LIST,
				getCommunicationListFromIds(communicationGenerationSchedulerVO
						.getCommunicationIds()));
		map.put(COMMUNICATION_GENERATION_SCHEDULER_VO,
				communicationGenerationSchedulerVO);
		map.put(MASTER_ID, COMMUNICATION_GENERATION_SCHEDULER);
	}
}
