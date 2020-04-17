package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationNameDAO")
public class CommunicationNameDAO extends EntityDaoImpl implements ICommunicationNameDAO{

	
	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao     baseMasterDao;
	
	@Inject
    @Named("entityDao")
    private EntityDao entityDao;

	public static final String LTAPPROVAL_STATUS="approvalStatus";
	@Override
	public List<CommunicationTemplate> getCommunicationTemplatesAssociatedWithComunicationName(Long id) {
		NeutrinoValidator.notNull(id);
		List<Integer> approvalList = new ArrayList<Integer>();
        approvalList.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        approvalList.add(ApprovalStatus.UNAPPROVED_HISTORY);
        NamedQueryExecutor<CommunicationTemplate> templatesListQuery = new NamedQueryExecutor<CommunicationTemplate>("CommunicationTemplate.getCommunicationTemplatesUsingId").addParameter("id", id).addParameter(LTAPPROVAL_STATUS, approvalList);
        List<CommunicationTemplate> templates = entityDao.executeQuery(templatesListQuery);
        return baseMasterDao.getVersionedList(CommunicationTemplate.class, templates);
	}
	
	@Override
	public List<CommunicationName> getApprovedCommunicationNames(){
		
		List<Integer> approvalList = new ArrayList<Integer>();
		approvalList.add(ApprovalStatus.APPROVED);
		approvalList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		approvalList.add(ApprovalStatus.APPROVED_DELETED);
		NamedQueryExecutor<CommunicationName> approvedNamesListQuery = new NamedQueryExecutor<CommunicationName>("CommunicationName.getCommunicationNames").addParameter(LTAPPROVAL_STATUS, approvalList);

		return entityDao.executeQuery(approvedNamesListQuery);
		
	}

    @Override
    public List<CommunicationParameter> getCommunicationParametersForAdHocBasedOnModule(
            SourceProduct sourceProduct) {
        List<Integer> approvalStatusList=getApprovalStatusList();
        NamedQueryExecutor<CommunicationParameter> executor = new NamedQueryExecutor<CommunicationParameter>(
                "CommunicationParameter.getCommunicationParametersForAdHocBasedOnModule")
                .addParameter("sourceProduct", sourceProduct)
                .addParameter("approvalStatusList", approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
       
    }
	
    protected List<Integer> getApprovalStatusList() {
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.UNAPPROVED);
        approvalStatusList.add(ApprovalStatus.UNAPPROVED_HISTORY);
        approvalStatusList.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        return approvalStatusList;
   }
    

	@Override
	public List<CommunicationName> getCommunicationNamesBasedOnCommunicationType(
			CommunicationType communicationType,SourceProduct sourceProduct) {
		List<Integer> approvalList = new ArrayList<Integer>();
		approvalList.add(ApprovalStatus.APPROVED);
		approvalList.add(ApprovalStatus.APPROVED_MODIFIED);
		NamedQueryExecutor<CommunicationName> approvedNamesListQuery = new NamedQueryExecutor<CommunicationName>(
				"CommunicationName.getCommunicationNamesBasedOnCommunicationType")
				.addParameter(LTAPPROVAL_STATUS, approvalList)
				.addParameter("communicationTypeCode", communicationType.getCode())
				.addParameter("sourceProduct", sourceProduct);
		return entityDao.executeQuery(approvedNamesListQuery);
		
	}

	@Override
	public List<CommunicationName> getApprovedInitializedCommunication() {
		
		List<Integer> approvalList = new ArrayList<Integer>();
		approvalList.add(ApprovalStatus.APPROVED);
		approvalList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		approvalList.add(ApprovalStatus.APPROVED_DELETED);
		NamedQueryExecutor<CommunicationName> approvedNamesListQuery = new NamedQueryExecutor<CommunicationName>(
				"CommunicationName.getInitializedCommunication").addParameter(LTAPPROVAL_STATUS, approvalList);

		return entityDao.executeQuery(approvedNamesListQuery);
		
	}

	@Override
	public List<CommunicationTemplate> getAllInitializedCommunicationTemplates() {
		
		NamedQueryExecutor<CommunicationTemplate> approvedNamesListQuery = new NamedQueryExecutor<CommunicationTemplate>(
				"CommunicationName.getAllInitializedCommunicationTemplates");

		return entityDao.executeQuery(approvedNamesListQuery);
	}
}
