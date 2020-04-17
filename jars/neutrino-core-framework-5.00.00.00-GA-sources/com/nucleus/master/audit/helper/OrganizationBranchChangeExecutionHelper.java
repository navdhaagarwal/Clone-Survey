package com.nucleus.master.audit.helper;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.metadata.BiDiTreeNodePointerByField;
import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;
import com.nucleus.master.audit.service.util.MasterChangeFieldFormatter;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.service.BaseServiceImpl;

@Component("organizationbranchChangeExecutionHelper")
public class OrganizationBranchChangeExecutionHelper extends MasterChangeExecutionHelper {


	@Inject
	@Named("baseMasterDao")
	private BaseMasterDao baseDao;
	
	@PostConstruct
	public void init(){
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(DailySchedule.class, "openingTime"), new DailyScheduleDateTimeFormatter( DEFAULT_TIME_FORMAT));
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(DailySchedule.class, "closingTime"), new DailyScheduleDateTimeFormatter( DEFAULT_TIME_FORMAT));
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(DailySchedule.class, "lunchFrom"), new DailyScheduleDateTimeFormatter( DEFAULT_TIME_FORMAT));
		withFieldFormatterhandler(new BiDiTreeNodePointerByField(DailySchedule.class, "lunchTo"), new DailyScheduleDateTimeFormatter( DEFAULT_TIME_FORMAT));
	}
	
	@Override
	public void postProcess(MasterChangeDiffHolder diffHolder, MasterChangeEntityHolder oldEntity,
			MasterChangeEntityHolder newEntity) {
		if(oldEntity.getRootEntity() == null || newEntity.getRootEntity() == null){
			return;
		}
		// checking for parent branch mapping -> both sould present as this is mandatory
		if(CollectionUtils.isNotEmpty(((OrganizationBranch)oldEntity.getRootEntity()).getParentBranchMapping())
				&& CollectionUtils.isNotEmpty(((OrganizationBranch)newEntity.getRootEntity()).getParentBranchMapping())){
			List<ParentBranchMapping> oldParentBranchMapping = ((OrganizationBranch)oldEntity.getRootEntity()).getParentBranchMapping();
			List<ParentBranchMapping> newParentBranchMapping = ((OrganizationBranch)newEntity.getRootEntity()).getParentBranchMapping();
			for (int i = 0; i < oldParentBranchMapping.size(); i++) {
				ParentBranchMapping oldCurrent = oldParentBranchMapping.get(i);
				ParentBranchMapping newCurrent = newParentBranchMapping.get(i);
				if(oldCurrent.getModuleName().getId() != newCurrent.getModuleName().getId()){
					Object oldModuleName = baseDao.getColumnValueFromEntity(SystemName.class, oldCurrent.getModuleName().getId(), "code");
					Object newModuleName = baseDao.getColumnValueFromEntity(SystemName.class, newCurrent.getModuleName().getId(), "code");
					diffHolder.addDeltaInString(MasterChangeMessageGenerationUtility.CHANGED+
							"Parent Branch Mapping[ Index :"+i+"] -> Module Name"+MasterChangeMessageGenerationUtility.FROM+oldModuleName+
								MasterChangeMessageGenerationUtility.TO+newModuleName);
				}
				if(oldCurrent.getParentBranch().getId() != newCurrent.getParentBranch().getId()){
					Object oldBranchName = baseDao.getColumnValueFromEntity(OrganizationBranch.class, oldCurrent.getParentBranch().getId(), "branchCode");
					Object newBranchName = baseDao.getColumnValueFromEntity(OrganizationBranch.class, newCurrent.getParentBranch().getId(), "branchCode");
					diffHolder.addDeltaInString(MasterChangeMessageGenerationUtility.CHANGED+
							"Parent Branch Mapping[ Index :"+i+"] -> Parent Branch"+MasterChangeMessageGenerationUtility.FROM+oldBranchName+
								MasterChangeMessageGenerationUtility.TO+newBranchName);
				
				}
			}
		}
	}
	
}


class DailyScheduleDateTimeFormatter extends BaseServiceImpl implements MasterChangeFieldFormatter {

	public DailyScheduleDateTimeFormatter(String userPreferenceTimeZone) {
		super();
		this.userPreferenceTimeZone = userPreferenceTimeZone;
	}

	String userPreferenceTimeZone;
	
	@Override
	public String format(Object value) {
		if(value == null || StringUtils.isEmpty(value.toString())){
			return null;
		}
		DateTime val = (DateTime)value;
		return org.apache.commons.lang.time.DateFormatUtils.format(val.getMillis(),userPreferenceTimeZone);
	}
	
}
