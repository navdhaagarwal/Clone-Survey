package com.nucleus.web.genericparameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.autocomplete.AutocompleteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.genericparameter.entity.DynamicGenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameterMetaData;
import com.nucleus.core.genericparameter.entity.OfflineColumnType;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.binder.MasterMapDataBinder;
import com.nucleus.web.binder.WebDataBinderRegistry;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.technical.RegistrarConstants;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import com.nucleus.html.util.HtmlUtils;

@Transactional
@Controller
public class SeedDataController extends BaseController {

    private static final String                  newRowId = "0";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    private static final String MASTER_ID    =   "GenericParameter";
    private static final String DynamicDtypeClass = "com.nucleus.core.genericparameter.entity.DynamicGenericParameter"; 
    
    @Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;
	
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

   
    private static WebDataBinderRegistry registry;
    
    /*@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new GenericParameterValidator());
    }*/

    public WebDataBinderRegistry getRegistry() {
		return registry;
	}


    @Autowired(required=false)
	public void setRegistry(WebDataBinderRegistry registry) {
		this.registry = registry;
	}



    @RequestMapping(value = "/GenericParameter/create")
    public String createGenericParameter(ModelMap map) {

        List genericParameterList;
        genericParameterList = genericParameterService.findAllGenericParameterTypesFromDB();


        Collections.sort(genericParameterList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {

                String s1Array[] = StringUtils.delimitedListToStringArray(s1, ".");
                String s2Array[] = StringUtils.delimitedListToStringArray(s2, ".");
                String stringA = s1Array.length > 0 ? s1Array[s1Array.length - 1] : "";
                String stringB = s2Array.length > 0 ? s2Array[s2Array.length - 1] : "";

                return stringA.compareTo(stringB);
            }
        });




        map.put("dTypeList", genericParameterList);
        map.put("genericParameter", new GenericParameterForm());
        map.put("codeInvalid", false);
        map.put("masterID", MASTER_ID);
        return "genericParameter";
    }



    @RequestMapping(value = "/GenericParameter/populateDtypes")
    @ResponseBody
    public AutocompleteVO populateStageName(ModelMap map, @RequestParam String value,
                                            @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                            @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                            @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                            @RequestParam(required = false) Boolean strictSearchOnitemsList) {

        AutocompleteVO autocompleteVO = new AutocompleteVO();
        String[] searchColumnList = searchCol.split(" ");
        String[] sclHeading=new String[searchColumnList.length];
        for(int i=0;i<searchColumnList.length;i++)
        {
            searchColumnList[i]=searchColumnList[i].replace(".", "");
            sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null, Locale.getDefault());
        }
        if (strictSearchOnitemsList == null) {
            strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
            loadApprovedEntityFlag = false;
        }
        List<Map<String, ?>> list = genericParameterService.searchDtypes(className, itemVal, searchColumnList, value, loadApprovedEntityFlag, itemsList, strictSearchOnitemsList, page);
        if(list.size() > 0) {
            Map listMap = (Map)list.get(list.size() - 1);
            int sizeList1 = ((Integer)listMap.get("size")).intValue();
            list.remove(list.size() - 1);
           // map.put("size", Integer.valueOf(sizeList1));
            //map.put("page", Integer.valueOf(page));
        autocompleteVO.setS(Integer.valueOf(sizeList1));
        autocompleteVO.setP(page);
        }
        if(i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        //map.put("data", list);

        autocompleteVO.setD(list);
        if(idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        //map.put("idCurr", idCurr);
        //map.put("i_label", i_label);
        //map.put("content_id", content_id);
        autocompleteVO.setIc(idCurr);
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);

        return autocompleteVO;
        //return "autocomplete";
    }







    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(value = "/loadParentCode")
    public String loadParentCode(ModelMap map,@RequestParam(value = "dType_class", required = false) String dType_class) {
        Class<GenericParameter> suppliedGenericParameterClass = GenericParameter.class;
        try {
            suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(dType_class);
        } catch (ClassNotFoundException e) {
				try{
					suppliedGenericParameterClass =genericParameterService.findGenericParameterTypes(dType_class);
					
                	if(suppliedGenericParameterClass != null && !suppliedGenericParameterClass.isAssignableFrom(DynamicGenericParameter.class)) {
                		//do nothing
                	}else if(suppliedGenericParameterClass != null && suppliedGenericParameterClass.isAssignableFrom(DynamicGenericParameter.class)){
						return "genericParentCode";
					}else{
                		suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(DynamicDtypeClass);
                	}
					List<String> parentCodeList=genericParameterService.findParentsForGenericParameter(suppliedGenericParameterClass.getSimpleName());
					List<String> newparentCodeList = new ArrayList<>();
			        map.put("parentCodeList",parentCodeList);
			        GenericParameter genericParameter= genericParameterService.getDefaultValueForDynamicDtype(suppliedGenericParameterClass,dType_class);
			        if(genericParameter!=null){
			            map.put("defaultValue",genericParameter.getCode());
			        }
					return "genericParentCode";
				}catch (Exception ex) {
					BaseLoggers.exceptionLogger.error("Error in loading master data:" + dType_class, ex.getMessage());
					return null;
				}
        }


        List<String> parentCodeList=genericParameterService.findParentsForGenericParameter(suppliedGenericParameterClass.getSimpleName());
        map.put("parentCodeList",parentCodeList);

        GenericParameterMetaData genericParameterMetaData=genericParameterService.getDTypeMetaData(dType_class);
        map.put("genericParameterMetaData",genericParameterMetaData);

        GenericParameter genericParameter1= genericParameterService.getDefaultValue(suppliedGenericParameterClass);
        if(genericParameter1!=null){
            map.put("defaultValue",genericParameter1.getCode());
        }


        return "genericParentCode";
    }



    @RequestMapping(value = "/GenericParameter/save", method = RequestMethod.POST)
    public <T extends GenericParameter>String saveGenericParameter(@Validated GenericParameterForm genericParameterForm, BindingResult result, ModelMap map,
                                @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug(genericParameterForm.toString());
        Boolean isNew=false;
        Class<T> genericParameterEntityClass=null;
        GenericParameter genericParameter = null;
        if (!(result.hasErrors())) {
            if ((validateGenericData(genericParameterForm, map)) && (!(genericParameterForm.getNotModifiable()))) {

                try {
                    genericParameterEntityClass = (Class<T>) Class.forName(genericParameterForm.getdTYpe());
                    genericParameter = genericParameterEntityClass.newInstance();
                    convertVOtoEntity(genericParameterForm,genericParameter);
                } catch (Exception e) {
                    try{
                    	genericParameterEntityClass =genericParameterService.findGenericParameterTypes(genericParameterForm.getdTYpe());
                    	if(genericParameterEntityClass != null) {
                    		genericParameter = genericParameterEntityClass.newInstance();
                            convertVOtoEntity(genericParameterForm,genericParameter);
                    	}else{
                    		isNew=true;
	                    	Class.forName(genericParameterForm.getdTYpe(), false, this.getClass().getClassLoader());
	                    	genericParameterEntityClass= (Class<T>) Class.forName(DynamicDtypeClass);
	                    	genericParameter = genericParameterEntityClass.newInstance();
	                    	convertVOtoEntity(genericParameterForm,genericParameter);
	                    	genericParameter.setDynamicParameterName(genericParameterForm.getdTYpe());
                    	}
                    }catch(Exception ex){
                    	BaseLoggers.exceptionLogger.error("Error in loading master data:" + genericParameterForm.getdTYpe(), e.getMessage());
                    }
                }

                }

           }

        if (result.hasErrors() || !((validateGenericData(genericParameterForm, map))) ) {

            map.put("genericParameter", genericParameterForm);
            map.put("viewable", false);
            result.rejectValue("code", "label.generic.code.validation.exists");
            map.put("masterID", MASTER_ID);
            return "genericParameter";
        }

        User user = getUserDetails().getUserReference();
        boolean eventResult = executeMasterEvent(genericParameter,"contextObjectGenericParameter",map);
        if(!eventResult){
            map.put("genericParameter", genericParameterForm);
            map.put("viewable", false);
            map.put("masterID", MASTER_ID);
            return "genericParameter";
        }

        if (user != null) {
        	if(null != genericParameter.getId()){
        		GenericParameter genericParameter1 = baseMasterService.getMasterEntityWithActionsById(GenericParameter.class, genericParameter.getId(), user.getUri());
        		if(null == genericParameter.getCode() || "".equalsIgnoreCase(genericParameter.getCode())){
        			genericParameter.setCode(genericParameter1.getCode());
        		}
	        	if(genericParameter1.getClass().isAssignableFrom(DynamicGenericParameter.class)){
	        		genericParameter.setDynamicParameterName(genericParameter1.getDynamicParameterName());
	        	}
        	}
            makerCheckerService.masterEntityChangedByUser(genericParameter, user);
        }
        if(isNew){
        	Map<String,Object> map1 =  new HashMap<>();
			map1.put(RegistrarConstants.DYNAMIC_PARAMETER_FIELD, genericParameterForm.getdTYpe());
			registry.registerBinder(genericParameterForm.getdTYpe(),new MasterMapDataBinder(genericParameterEntityClass,map1,
					 RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));
        }
        if (createAnotherMaster) {

            map.put("genericParameter", new GenericParameterForm());
            map.put("masterID", MASTER_ID);
            map.put("viewable", false);
            return "genericParameter";

        }
        map.put("masterID", MASTER_ID);
        return "redirect:/app/grid/GenericParameter/GenericParameter/loadColumnConfig";
    }




    @RequestMapping(value = "/GenericParameter/saveAndSendForApproval", method = RequestMethod.POST)
    public <T extends GenericParameter>String saveAndSendForApproval(@Validated GenericParameterForm genericParameterForm, BindingResult result, ModelMap map,
                                         @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug(genericParameterForm.toString());
        Boolean isNew=false;
        Class<T> genericParameterEntityClass=null;
        GenericParameter genericParameter = null;
        if (!(result.hasErrors())) {
            if ((validateGenericData(genericParameterForm, map)) && (!(genericParameterForm.getNotModifiable()))) {

                try {
                    genericParameterEntityClass = (Class<T>) Class.forName(genericParameterForm.getdTYpe());
                    genericParameter = genericParameterEntityClass.newInstance();
                    convertVOtoEntity(genericParameterForm,genericParameter);
                } catch (Exception e) {
                	try{
                		genericParameterEntityClass =genericParameterService.findGenericParameterTypes(genericParameterForm.getdTYpe());
                    	if(genericParameterEntityClass != null) {
                    		genericParameter = genericParameterEntityClass.newInstance();
                            convertVOtoEntity(genericParameterForm,genericParameter);
                    	}else{
	                    	genericParameterEntityClass= (Class<T>) Class.forName(DynamicDtypeClass);
	                    	genericParameter = genericParameterEntityClass.newInstance();
	                    	convertVOtoEntity(genericParameterForm,genericParameter);
	                    	genericParameter.setDynamicParameterName(genericParameterForm.getdTYpe());
	                    	isNew=true;
                    	}
                    }catch(Exception ex){
                    	BaseLoggers.exceptionLogger.error("Error in loading master data:" + genericParameterForm.getdTYpe(), ex.getMessage());
                    }
                }

            }

        }
        if (result.hasErrors() || !((validateGenericData(genericParameterForm, map))) ) {

            map.put("genericParameter", genericParameterForm);
            map.put("viewable", false);
            map.put("masterID", MASTER_ID);
            result.rejectValue("code", "label.generic.code.validation.exists");
            return "genericParameter";
        }


        User user = getUserDetails().getUserReference();
        boolean eventResult = executeMasterEvent(genericParameter,"contextObjectGenericParameter",map);
        if(!eventResult){
            map.put("genericParameter", genericParameterForm);
            map.put("viewable", false);
            map.put("masterID", MASTER_ID);
            return "genericParameter";
        }

        if (user != null) {
        	if(null != genericParameter.getId()){
        		GenericParameter genericParameter1 = baseMasterService.getMasterEntityWithActionsById(GenericParameter.class, genericParameter.getId(), user.getUri());
        		if(null == genericParameter.getCode() || "".equalsIgnoreCase(genericParameter.getCode())){
        			genericParameter.setCode(genericParameter1.getCode());
        		}
	        	if(genericParameter1.getClass().isAssignableFrom(DynamicGenericParameter.class)){
	        		genericParameter.setDynamicParameterName(genericParameter1.getDynamicParameterName());
	        	}
        	}
            makerCheckerService.saveAndSendForApproval(genericParameter, user);
        }
        if(isNew){
        	Map<String,Object> map1 =  new HashMap<>();
			map1.put(RegistrarConstants.DYNAMIC_PARAMETER_FIELD, genericParameterForm.getdTYpe());
			registry.registerBinder(genericParameterForm.getdTYpe(),new MasterMapDataBinder(genericParameterEntityClass, 
					 map1,
					 RegistrarConstants.AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION));
        }
        if (createAnotherMaster) {

            map.put("genericParameter", new GenericParameterForm());
            map.put("masterID", MASTER_ID);
            map.put("viewable", false);
            return "genericParameter";

        }
        map.put("masterID", MASTER_ID);
        return "redirect:/app/grid/GenericParameter/GenericParameter/loadColumnConfig";
    }



    @RequestMapping(value = "/GenericParameter/edit/{id}")
    public String editGenericParameter(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        GenericParameter genericParameter = baseMasterService.getMasterEntityWithActionsById(GenericParameter.class, id, currentUser.getUserEntityId()
                .getUri());
        if (genericParameter.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            GenericParameter prevGenericParameter= (GenericParameter) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(genericParameter.getEntityId());
            GenericParameterForm prevGenericParameterForm=new GenericParameterForm();
            if(prevGenericParameter !=null){
                convertEntitytoVO(prevGenericParameter,prevGenericParameterForm);
            }

            map.put("prevGenericParameter", prevGenericParameterForm);
            map.put("editLink", true);
        }
        if (genericParameter.getApprovalStatus() == ApprovalStatus.APPROVED
                || genericParameter.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || genericParameter.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            map.put("disableIsoCode", true);
        }



        GenericParameterForm genericParameterForm=new GenericParameterForm();

        convertEntitytoVO(genericParameter,genericParameterForm);

        Class<GenericParameter> genericParameterClass=convertGenericToClass(genericParameterForm.getdTYpe());
        GenericParameter genericParameter1;
        if(genericParameter.getClass().isAssignableFrom(DynamicGenericParameter.class)){
        	genericParameter1= genericParameterService.getDefaultValueForDynamicDtype(genericParameterClass,genericParameter.getDynamicParameterName());
        }else{
        	genericParameter1= genericParameterService.getDefaultValue(genericParameterClass);
        }
        if(genericParameter1!=null){
            map.put("defaultValue",genericParameter1.getCode());
        }
        List<String> parentCodeList=genericParameterService.findParentsForGenericParameter(genericParameterClass.getSimpleName());
        map.put("parentCodeList",parentCodeList);

        GenericParameterMetaData genericParameterMetaData=genericParameterService.getDTypeMetaData(genericParameterForm.getdTYpe());
        map.put("genericParameterMetaData",genericParameterMetaData);

        map.put("genericParameter", genericParameterForm);
        map.put("viewable", false);
        map.put("codeInvalid", false);
        map.put("masterID", MASTER_ID);
        map.put("edit", true);

        return "genericParameter";
    }



    @RequestMapping(value = "/GenericParameter/view/{id}", method = RequestMethod.GET)
    public String viewGenericParameter(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        GenericParameter genericParameter = baseMasterService.getMasterEntityWithActionsById(GenericParameter.class, id, currentUser.getUserEntityId()
                .getUri());
        if (genericParameter.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || genericParameter.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            GenericParameter prevGenericParameter = (GenericParameter) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(genericParameter
                    .getEntityId());
            GenericParameterForm prevGenericParameterForm=new GenericParameterForm();
            if(prevGenericParameter!=null){
                convertEntitytoVO(prevGenericParameter,prevGenericParameterForm);
            }
            map.put("prevGenericParameter", prevGenericParameterForm);
            map.put("editLink", true);
        } else if (genericParameter.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            GenericParameter prevGenericParameter = (GenericParameter) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(genericParameter
                    .getEntityId());
            GenericParameterForm prevGenericParameterForm=new GenericParameterForm();

            convertEntitytoVO(prevGenericParameter,prevGenericParameterForm);

            map.put("prevGenericParameter", prevGenericParameterForm);
            map.put("viewLink", true);
        }

        GenericParameterForm genericParameterForm=new GenericParameterForm();

        convertEntitytoVO(genericParameter,genericParameterForm);

        Class<GenericParameter> genericParameterClass=convertGenericToClass(genericParameterForm.getdTYpe());
        GenericParameter genericParameter1;
        if("DynamicGenericParameter".equalsIgnoreCase(genericParameter.getClass().getSimpleName())){
        	genericParameter1= genericParameterService.getDefaultValueForDynamicDtype(genericParameterClass,genericParameter.getDynamicParameterName());
        }else{
        	genericParameter1= genericParameterService.getDefaultValue(genericParameterClass);
        }
        if(genericParameter1!=null){
            map.put("defaultValue",genericParameter1.getCode());
        }
        List<String> parentCodeList=genericParameterService.findParentsForGenericParameter(genericParameterClass.getSimpleName());
        map.put("parentCodeList",parentCodeList);

        GenericParameterMetaData genericParameterMetaData=genericParameterService.getDTypeMetaData(genericParameterForm.getdTYpe());
        map.put("genericParameterMetaData",genericParameterMetaData);

        map.put("genericParameter", genericParameterForm);
        map.put("masterID", MASTER_ID);
        map.put("codeInvalid", false);
        map.put("viewable", true);
        if (genericParameter.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> actions = (ArrayList<String>) genericParameter.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
        return "genericParameter";
    }



    private void convertVOtoEntity(GenericParameterForm genericParameterForm,GenericParameter genericParameter){
        if(!StringUtils.isEmpty(genericParameterForm.getId())){
            genericParameter.setId(Long.parseLong(genericParameterForm.getId()));
        }
        genericParameter.setName(genericParameterForm.getName());
        genericParameter.setCode(genericParameterForm.getCode());
        genericParameter.setDescription(genericParameterForm.getDescription());
        genericParameter.setOfflineFlag(genericParameterForm.getOfflineFlag());
        genericParameter.setParentCode(genericParameterForm.getParentCode());
        genericParameter.setActiveFlag(genericParameterForm.isActiveFlag());
       /* Class<GenericParameter> genericParameterClass=convertGenericToClass(genericParameterForm.getdTYpe());
        GenericParameter genericParameter1=genericParameterService.getDefaultValue(genericParameterClass);

        if(genericParameter1!=null && genericParameterForm.getDefaultFlag()){
            genericParameter1.setDefaultFlag(false);
            genericParameterService.updateGenericParameter(genericParameter1);
            entityDao.flush();
        }*/
        genericParameter.setDefaultFlag(genericParameterForm.getDefaultFlag());


    }

    private void convertEntitytoVO(GenericParameter genericParameter,GenericParameterForm genericParameterForm){

        genericParameterForm.setId(String.valueOf(genericParameter.getId()));
        genericParameterForm.setCode(genericParameter.getCode());
        genericParameterForm.setName(genericParameter.getName());
        genericParameterForm.setDescription(genericParameter.getDescription());
        genericParameterForm.setParentCode(genericParameter.getParentCode());
        genericParameterForm.setActiveFlag(genericParameter.isActiveFlag());
        genericParameterForm.setDefaultFlag(genericParameter.getDefaultFlag());
        genericParameterForm.setdTYpe(genericParameter.getClass().getName());
        genericParameterForm.setDtypeSimpleName(genericParameter.getClass().getSimpleName());
        if(genericParameter.getClass().isAssignableFrom(DynamicGenericParameter.class)){
        	genericParameterForm.setdTYpe(genericParameter.getDynamicParameterName());
        	genericParameterForm.setDtypeSimpleName(genericParameter.getDynamicParameterName());
        }
    }

    private Class<GenericParameter> convertGenericToClass(String dType_class){

        Class<GenericParameter> suppliedGenericParameterClass = GenericParameter.class;
        try {
            suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(dType_class);
        } catch (ClassNotFoundException e) {
        	try {
				suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(DynamicDtypeClass);
			} catch (ClassNotFoundException e1) {
				 BaseLoggers.exceptionLogger.error("Error in loading master data:" + dType_class, e.getMessage());
		            return null;
			}
           
        }

        return suppliedGenericParameterClass;


    }

    @SuppressWarnings("unchecked")
    //@PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(value = "/addgenericparameters", method = RequestMethod.GET)
    public String loadGenericData(ModelMap map) {
        @SuppressWarnings("rawtypes")
        List genericParameterList;
        genericParameterList = genericParameterService.findAllGenericParameterTypesFromDB();


        Collections.sort(genericParameterList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {

                String s1Array[] = StringUtils.delimitedListToStringArray(s1, ".");
                String s2Array[] = StringUtils.delimitedListToStringArray(s2, ".");
                String stringA = s1Array.length > 0 ? s1Array[s1Array.length - 1] : "";
                String stringB = s2Array.length > 0 ? s2Array[s2Array.length - 1] : "";

                return stringA.compareTo(stringB);
            }
        });

        map.put("dTypeList", genericParameterList);
        return "seedData";
    }









    @SuppressWarnings("unchecked")
    public <T extends GenericParameter> String updateGenericData(GenericParameterForm genericParameterform,
            String dType_class, BindingResult result) {
        String message;       
        Class<T> genericParameterEntityClass = null;
        GenericParameter genericParameter = null;
        try {
            genericParameterEntityClass = (Class<T>) Class.forName(dType_class);
//            genericParameter = genericParameterEntityClass.newInstance();
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in Updating GenericData:" + dType_class, e.getMessage());
        }
        
      
        genericParameter = genericParameterService.findById(new Long(genericParameterform.getId()), genericParameterEntityClass);
        genericParameter.initializeAuthorities();
        fwCacheHelper.detachEntity(genericParameter);
        // Hibernate.initialize(genericParameter);
        genericParameter.setName(genericParameterform.getName());
        genericParameter.setDescription(genericParameterform.getDescription());
        genericParameter.setOfflineFlag(genericParameterform.getOfflineFlag());
/*if(genericParameterform.getActiveFlag()!=null) {
    genericParameter.setPersistenceStatus(genericParameterform.getActiveFlag());
}*/
        genericParameter.setParentCode(genericParameterform.getParentCode());
//        genericParameter.setId(new Long(genericParameterform.getId()));
        genericParameterService.updateGenericParameter(genericParameter);

        message = "true";
        return message;
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(value = "/{dType_class}/saveGenericData", method = RequestMethod.POST)
    public @ResponseBody
    <T extends GenericParameter> String addGenericData(GenericParameterForm genericParameterform, ModelMap map,
            BindingResult result, @PathVariable("dType_class") String dType_class) {
        BaseLoggers.flowLogger.debug("Saving Seed Generic Data Details-->"+"\nClass Type: "+dType_class+""+genericParameterform.getLogInfo());
        String message = null;
        if (!(result.hasErrors())) {
            if ((validateGenericData(genericParameterform,  map)) && (!(genericParameterform.getNotModifiable()))) {
                if (!(genericParameterform.getId().equals(newRowId))) {
                    updateGenericData(genericParameterform, dType_class, result);
                    message = "true";
                    return message;
                }
                Class<T> genericParameterEntityClass;
                GenericParameter genericParameter = null;
                try {
                    genericParameterEntityClass = (Class<T>) Class.forName(dType_class);
                    genericParameter = genericParameterEntityClass.newInstance();
                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error("Error in Adding GenericData:" + dType_class, e.getMessage());
                }
                genericParameter.setName(genericParameterform.getName());
                genericParameter.setCode(genericParameterform.getCode());
                genericParameter.setDescription(genericParameterform.getDescription());
                genericParameter.setOfflineFlag(genericParameterform.getOfflineFlag());
                genericParameter.setParentCode(genericParameterform.getParentCode());
                /*if(genericParameterform.getActiveFlag()!=null){
                    genericParameter.setPersistenceStatus(genericParameterform.getActiveFlag());
                }*/

                genericParameterService.createGenericParameter(genericParameter);
                message = "true";
                return message;
            } else {
                message = "false";
                if(map.get(VALIDATION_ERROR)!= null){
                    message = message+','+map.get(VALIDATION_ERROR);
                }
                
                return message;
            }
        } else {
            message = "false";
        }
        return message;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(value = "/{dType_class}/loadDType")
    public @ResponseBody
    Map loadMasterData(@PathVariable("dType_class") String dType_class) {
        Class<GenericParameter> suppliedGenericParameterClass = GenericParameter.class;
        try {
            suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(dType_class);
        } catch (ClassNotFoundException e) {
        	try {
				suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(DynamicDtypeClass);
			} catch (ClassNotFoundException e1) {
				 BaseLoggers.exceptionLogger.error("Error in loading master data:" + dType_class, e.getMessage());
		            return null;
			}
           
        }


        List<GenericParameter> genericParameters = genericParameterService.retrieveTypes(suppliedGenericParameterClass,false);
        List<GenericParameterForm> genericParameterFormsList = new ArrayList<>();
        List<OfflineColumnType> offlineTypes = genericParameterService.retrieveTypes(OfflineColumnType.class);
        GenericParameter offlineTypeObject = genericParameterService.findByCode(suppliedGenericParameterClass.getSimpleName(), OfflineColumnType.class);
        for (GenericParameter genericParameter : genericParameters) {
            GenericParameterForm genericParameterForm = new GenericParameterForm();
            genericParameterForm.setCode(genericParameter.getCode());
            genericParameterForm.setDescription(genericParameter.getDescription());
            if (genericParameter.getId() != null) {
                genericParameterForm.setId(genericParameter.getId().toString());
            }
            genericParameterForm.setName(genericParameter.getName());
            genericParameterForm.setdTYpe(genericParameter.getClass().getName());
            genericParameterForm.setdTYpe(dType_class);
            genericParameterForm.setNotModifiable(genericParameter.getNotModifiable());
            genericParameterForm.setAvailableOffline(offlineTypes.contains(offlineTypeObject)?Boolean.TRUE:Boolean.FALSE);
            genericParameterForm.setOfflineFlag(genericParameter.getOfflineFlag());
            genericParameterForm.setParentCode(genericParameter.getParentCode());
            //genericParameterForm.setActiveFlag(genericParameter.getPersistenceStatus());
            ArrayList<String> actions = new ArrayList<>();
            actions.add("Edit");
            actions.add("Delete");
            actions.add(String.valueOf(genericParameter.getNotModifiable()));
            genericParameterForm.addProperty("actions",actions);
            genericParameterFormsList.add(genericParameterForm);
        }
        

        GenericParameterMetaData genericParameterMetaData=genericParameterService.getDTypeMetaData(dType_class);
        List<String> parentCodeList=genericParameterService.findParentsForGenericParameter(suppliedGenericParameterClass.getSimpleName());
        Map jsonData = new HashMap();
        JSONSerializer iSerializer = new JSONSerializer();
        jsonData.put("genericParameterMetaData",genericParameterMetaData);
        jsonData.put("parentCodeList",parentCodeList);
        jsonData.put("dTypeCanBeOffline", offlineTypeObject==null?Boolean.FALSE:Boolean.TRUE);
        jsonData.put("aaData", genericParameterFormsList);
        String jsonString = iSerializer.exclude("*.class")
                .include("aaData.id", "aaData.code", "aaData.name", "aaData.description","aaData.parentCode","aaData.activeFlag","aaData.availableOffline","aaData.offlineFlag" ,"aaData.viewProperties.actions","aaData.notModifiable" , "genericParameterMetaData.purpose","genericParameterMetaData.dTypeActionFlag","dTypeCanBeOffline","parentCodeList")
                .exclude("*").deepSerialize(jsonData);
        jsonData = (HashMap<String, Object>) new JSONDeserializer().deserialize(jsonString);
        
        return jsonData;
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(value = "delete/{recordId}")
    public @ResponseBody
    <T> String deleteMasterEntity(@PathVariable("recordId") Long[] id, GenericParameterForm genericParameterform) {
        Class<T> genericParameterEntityClass;
        GenericParameter genericParameter = null;
        try {
            genericParameterEntityClass = (Class<T>) Class.forName(genericParameterform.getdTYpe());
            genericParameter = (GenericParameter) genericParameterEntityClass.newInstance();
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in Deleting Record", e.getMessage());
        }

        /*=======CALL SERVICE TO DELETE From DB==============*/
        return "seedData";
    }

    @SuppressWarnings("unchecked")
    public boolean validateGenericData(GenericParameterForm genericParameterform, ModelMap map) {

        Class<GenericParameter> c = GenericParameter.class;
        try {
            c = (Class<GenericParameter>) Class.forName(genericParameterform.getdTYpe());
        } catch (ClassNotFoundException e) {
       		try {
       			c =genericParameterService.findGenericParameterTypes(genericParameterform.getdTYpe());
       			if(c != null && !c.isAssignableFrom(DynamicGenericParameter.class)) {
       				// do nothing
            	}else if (c != null && c.isAssignableFrom(DynamicGenericParameter.class)){
            		return false;
            	}else{
       				c= (Class<GenericParameter>) Class.forName(DynamicDtypeClass);
            	}
				} catch (ClassNotFoundException e1) {
					BaseLoggers.exceptionLogger.error("Error in validating generic data:" + genericParameterform.getdTYpe(), e1.getMessage());
		            return false;
				}
       			catch (Exception e1) {
				BaseLoggers.exceptionLogger.error("Error in validating generic data:" + genericParameterform.getdTYpe(), e1.getMessage());
	            return false;
			}
        }
        List<GenericParameter> genericParameters = genericParameterService.retrieveTypesForDuplication(c);
        if(c.isAssignableFrom(DynamicGenericParameter.class)){
        	for (GenericParameter gparam : genericParameters) {
	            if ((null == gparam.getDynamicParameterName() || genericParameterform.getdTYpe().equals(gparam.getDynamicParameterName())) && 
	            		(null != gparam.getCode() && gparam.getCode().trim().equals(genericParameterform.getCode()) && !genericParameterform.getId().equals(gparam.getId().toString()))) {
                    map.put("codeInvalid", true);
	                return false;
	            }
	        }
        }else{
	        for (GenericParameter gparam : genericParameters) {
	            if (null != gparam.getCode() && gparam.getCode().trim().equals(genericParameterform.getCode())
	                    && !genericParameterform.getId().equals(gparam.getId().toString())) {
                    map.put("codeInvalid", true);
	                return false;
	            }else{
                    map.put("codeInvalid", false);
                }
	        }
        }
        return true;
    }


}
