/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.document.service;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.document.core.entity.*;
import org.springframework.transaction.annotation.Transactional;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named("documentService")
@MonitoredWithSpring(name = "Document_Service_IMPL_")
public class DocumentServiceImpl extends BaseServiceImpl implements DocumentService {

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    @Inject
    @Named("entityDao")
    private EntityDao           entityDao;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    private static final String QUERY_FOR_FETCHING_DOCUMENT_DEFINATION        = "DocumentService.getDocumentDefinitionByClassificationType";
    private static final String QUERY_FOR_FETCHING_ACTIVE_DOCUMENT_DEFINATION = "DocumentService.getActiveDocumentDefinitionByClassificationType";

    @Override
    public List<DocumentDefinition> getApprovedgroupDocuments() {
        NamedQueryExecutor<DocumentDefinition> parameterCriteria = new NamedQueryExecutor<DocumentDefinition>(
                "Document.ApprovedGroupDocuments");
        parameterCriteria.addParameter("approvalStatus", 0)
        				 .addParameter("code", "DocumentTypeIndividualDocument");
        List<DocumentDefinition> documentDefinitionList = entityDao.executeQuery(parameterCriteria);
        if (documentDefinitionList.size() > 0)
            return documentDefinitionList;
        else
            return null;
    }

    @Override
    public List<Map<String, ?>> getApprovedgroupDocumentsForBinder() {
        NamedQueryExecutor<Map<String, ?>> parameterCriteria = new NamedQueryExecutor<Map<String, ?>>(
                "Document.ApprovedGroupDocumentsForBinder");
        parameterCriteria.addParameter("approvalStatus", 0)
		 				 .addParameter("code", "DocumentTypeIndividualDocument");
        List<Map<String, ?>> documentDefinitionList = entityDao.executeQuery(parameterCriteria);
        if (documentDefinitionList.size() > 0)
            return documentDefinitionList;
        else
            return null;
    }

    @Override
    public List<DocumentDefinition> getDocumentDefinitionByClassificationType(Long docClassificationID) {

        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<DocumentDefinition> executor = new NamedQueryExecutor<DocumentDefinition>(
                QUERY_FOR_FETCHING_DOCUMENT_DEFINATION).addParameter("classificationType", docClassificationID)
                .addParameter("statusList", statusList);
        List<DocumentDefinition> documentDefinitionList = new ArrayList<DocumentDefinition>();
        documentDefinitionList = entityDao.executeQuery(executor);
        return documentDefinitionList;

    }

    @Override
    public List<Map<String, Object>> getActiveDocumentDefinitionByClassificationType(Long docClassificationID) {

        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Map<String, Object>> queryExecutor = new NamedQueryExecutor<Map<String, Object>>(
                QUERY_FOR_FETCHING_ACTIVE_DOCUMENT_DEFINATION).addParameter("classificationType", docClassificationID)
                .addParameter("statusList", statusList);
        return entityDao.executeQuery(queryExecutor);

    }

    @Override
    public List<DocumentChecklist> loadDocumentCheckLists() {

        List<DocumentChecklist> documentCheckList = entityDao.findAll(DocumentChecklist.class);

        return documentCheckList;

    }

    @Override
    public DocumentChecklistDefinition getDocumentChecklistDefinitionById(Long id) {
        return entityDao.find(DocumentChecklistDefinition.class, id);
    }

    @Override
    @Transactional(noRollbackFor=BusinessException.class)
    public Long updateDocumentChecklistDefinitionToDocumentChecklist(
            DocumentChecklistDefinition documentChecklistDefinition, Long parentId, User user) {
        NeutrinoValidator.notNull(documentChecklistDefinition, "Document Checklist Definition cannot be updated to null");
        NeutrinoValidator.notNull(user, "User cannot be null");

        if (parentId == null) {
            throw new SystemException("No DocumentChecklist associated with the current object.");
        }
        DocumentChecklist documentChecklist = entityDao.find(DocumentChecklist.class, parentId);
        if (documentChecklist == null) {
            documentChecklist = new DocumentChecklist();
            documentChecklist.markEmptyParent();
            documentChecklist = createDocumentChecklist(documentChecklist, user);
        }
        List<DocumentChecklistDefinition> documentChecklistDefinitions = null;
        if (documentChecklist.getDocuments() == null) {
            documentChecklistDefinitions = new ArrayList<DocumentChecklistDefinition>();
        } else {
            documentChecklistDefinitions = documentChecklist.getDocuments();
        }
        
        checkAndThrowExceptionIfDuplicate(documentChecklistDefinition,documentChecklistDefinitions);
        
        if (documentChecklistDefinition.getId() == null) {
            documentChecklistDefinitions.add(documentChecklistDefinition);
        } else {
            int modIndex = documentChecklistDefinitions.indexOf(documentChecklistDefinition);
            DocumentChecklistDefinition persistedDocumentChecklistDefinition = entityDao.find(
                    DocumentChecklistDefinition.class, documentChecklistDefinition.getId());
            persistedDocumentChecklistDefinition.setClassificationType(documentChecklistDefinition.getClassificationType());
            persistedDocumentChecklistDefinition.setDocument(documentChecklistDefinition.getDocument());
            persistedDocumentChecklistDefinition.setVerificationRequired(documentChecklistDefinition
                    .getVerificationRequired());
            persistedDocumentChecklistDefinition.setMandatory(documentChecklistDefinition.getMandatory());
            persistedDocumentChecklistDefinition.setSource(documentChecklistDefinition.getSource());
            persistedDocumentChecklistDefinition.setOrder(documentChecklistDefinition.getOrder());
            persistedDocumentChecklistDefinition.setOriginalRequired(documentChecklistDefinition.getOriginalRequired());
            documentChecklistDefinitions.set(modIndex, persistedDocumentChecklistDefinition);
        }
        documentChecklist.setDocuments(documentChecklistDefinitions);
        documentChecklist = updateDocumentChecklist(documentChecklist, user);

        return documentChecklist.getId();
    }

    @Override
    public DocumentChecklist createDocumentChecklist(DocumentChecklist changedEntity, User user) {
        NeutrinoValidator.notNull(user, "User cannot be null");
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "DocumentChecklist Entity Cannot be saved null");
        if (changedEntity.getPersistenceStatus() == null) {
            changedEntity.markEmptyParent();
        }
        return (DocumentChecklist) makerCheckerService.masterEntityChangedByUser(changedEntity, user);
    }

    @Override
    public DocumentChecklist updateDocumentChecklist(DocumentChecklist changedEntity, User user) {
        NeutrinoValidator.notNull(user, "User cannot be null");
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "DocumentChecklist Entity Cannot be updated to null");
        DocumentChecklist orgEntity = baseMasterService.getMasterEntityById(DocumentChecklist.class, changedEntity.getId());
        if (orgEntity != null) {
            changedEntity.setDocuments(orgEntity.getDocuments());
        }
        if (changedEntity.getPersistenceStatus() == null) {
            changedEntity.markEmptyParent();
        }
        DocumentChecklist updatedEntity = (DocumentChecklist) makerCheckerService.masterEntityChangedByUser(changedEntity,
                user);
        return updatedEntity;
    }

    @Override
    public DocumentChecklistDefinition findDocumentChecklistDefinitionById(Long id) {
        return entityDao.find(DocumentChecklistDefinition.class, id);
    }

    @Override
    public void removeDocumentChecklistDefinition(DocumentChecklistDefinition documentChecklistDefinition) {
        entityDao.delete(documentChecklistDefinition);
    }

    @Override
    public void updateDocumentChecklist(DocumentChecklist documentChecklist) {
        entityDao.update(documentChecklist);
    }

    @Override
    public List<DocumentMappingCode> getAllApprovedMappingCodes() {
        return genericParameterService.retrieveTypes(DocumentMappingCode.class);
    }

    @Override
    public Document getDocumentByDocumentStoreId(String documentStoreId) {
        NeutrinoValidator.notNull(documentStoreId, "Document Store Id cannot be null");
        Document document = null;
        NamedQueryExecutor<Document> executor = new NamedQueryExecutor<Document>(
                "DocumentService.getDocumentByDocumentStoreId");
        executor.addParameter("documentStoreId", documentStoreId);
        List<Document> documentDefinitionList = entityDao.executeQuery(executor);
        if (documentDefinitionList != null && documentDefinitionList.size() > 0) {
            document = documentDefinitionList.get(0);
        }
        return document;

    }

    @Override
    public void sortDocumentChecklistDefinitionByPriority(List<DocumentChecklistDefinition> documentChecklistDefinitions) {
        Collections.sort(documentChecklistDefinitions, new Comparator<DocumentChecklistDefinition>() {
            @Override
            public int compare(DocumentChecklistDefinition o1, DocumentChecklistDefinition o2) {

                if (o1.getOrder() == null && o2.getOrder() == null) {
                    return 0;
                }

                else if (o1.getOrder() == null) {
                    return 1;
                }

                else if (o2.getOrder() == null) {
                    return -1;
                }

                else if (o1.getOrder() == o2.getOrder()) {
                    return 0;
                }

                return o1.getOrder() < o2.getOrder() ? -1 : 1;
            }
        });
    }

    @Override
    public List<DocumentDefinition> getDocumentUsages(Long id) {
        NamedQueryExecutor<DocumentDefinition> documentPolicyExecutor = new NamedQueryExecutor<DocumentDefinition>(
                "DocumentUsage.getGroupDocument");
        documentPolicyExecutor.addParameter("id", id);
        List<DocumentDefinition> documentDefinitionList = entityDao.executeQuery(documentPolicyExecutor);
        if (documentDefinitionList != null && documentDefinitionList.size() > 0) {
            return documentDefinitionList;
        }
        return null;
    }

    @Override
    public List<DocumentChecklist> getDocumentUsageChecklist(Long id) {
        NamedQueryExecutor<DocumentChecklist> documentPolicyExecutor = new NamedQueryExecutor<DocumentChecklist>(
                "DocumentUsage.getChecklist");
        documentPolicyExecutor.addParameter("id", id);
        List<DocumentChecklist> documentChecklistList = entityDao.executeQuery(documentPolicyExecutor);
        if (documentChecklistList != null && documentChecklistList.size() > 0) {
            return documentChecklistList;
        }
        return null;
    }

    @Override
    public DocumentChecklist getDocumentChecklistFromDocCheckDefId(Long id) {
        NamedQueryExecutor<DocumentChecklist> checkListExecutor = new NamedQueryExecutor<DocumentChecklist>(
                "DocumentService.getDocumentChecklistFromDocumentChecklistDefinitionId");
        checkListExecutor.addParameter("docCheckDefId", id);
        DocumentChecklist documentChecklistTemp = entityDao.executeQueryForSingleValue(checkListExecutor);
       
        return documentChecklistTemp;
    }

    private void checkAndThrowExceptionIfDuplicate(DocumentChecklistDefinition documentChecklistDefinition, List<DocumentChecklistDefinition> documentChecklistDefinitions){
		Long documentId = null;
		if (documentChecklistDefinition.getDocument() != null){
			documentId = documentChecklistDefinition.getDocument().getId();
		}
		
		for (DocumentChecklistDefinition checklistDefinition : documentChecklistDefinitions) {			
			if (checklistDefinition.getDocument() != null && checklistDefinition.getDocument().getId() != null && checklistDefinition.getDocument().getId().equals(documentId)) {
				if (documentChecklistDefinition.getId() == null || !documentChecklistDefinition.getId().equals(checklistDefinition.getId())) {
					DocumentDefinition documentDefinition = entityDao.find(DocumentDefinition.class, documentId);	
					throw ExceptionBuilder
							.getInstance(
									BusinessException.class,"duplicate.document.definition.in.checklist",
									"Document ("+documentDefinition.getName()+ ") is already added into document checklist.")
							.setMessage(
									CoreUtility.prepareMessage(
													"duplicate.document.definition.in.checklist",
													String.valueOf(documentDefinition.getName())))
							.setSeverity(
									ExceptionSeverityEnum.LOW.getEnumValue())
							.build();
				}
			}
		}
	}
}
