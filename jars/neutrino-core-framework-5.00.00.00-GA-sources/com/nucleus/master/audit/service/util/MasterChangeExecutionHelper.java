package com.nucleus.master.audit.service.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.metamodel.clazz.ValueObjectDefinition;
import org.javers.core.metamodel.clazz.ValueObjectDefinitionBuilder;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeDisJointChildEntityJaversHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.metadata.AuditableClassMetadata;
import com.nucleus.master.audit.metadata.BiDiTreeNodePointerByField;
import com.nucleus.persistence.BaseDao;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserProfile;

@Component("masterChangeExecutionHelper")
public class MasterChangeExecutionHelper extends BaseServiceImpl {

	// to be use for calculating diff in master
	private Javers rootEntityJavrseInstance;

	private Map<String, MasterChangeDisJointChildEntityJaversHolder> disJointHolders;

	private Map<BiDiTreeNodePointerByField,MasterChangeFieldDiffCalculator> customeFieldsDiffCalculator;
	
	private Map<BiDiTreeNodePointerByField,MasterChangeFieldFormatter> fieldFormatter;

	public Javers getRootEntityJavrseInstance() {
		return rootEntityJavrseInstance;
	}

	public Map<String, MasterChangeDisJointChildEntityJaversHolder> getDisJointHolders() {
		return disJointHolders;
	}


	public MasterChangeJaversHolder getJaversInstance(AuditableClassMetadata classMeta,MasterChangeExecutionHelper helper) throws Exception{
		
		try {
			JaversBuilder builderRef = classMeta.build();
			MasterChangeJaversHolder holder = new MasterChangeJaversHolder(builderRef);
			if(MapUtils.isNotEmpty(classMeta.getDisJointChildMetadata())){
				classMeta.getDisJointChildMetadata().forEach((k,v)->{
					try {
						holder.withDisJointHolders(k, new MasterChangeDisJointChildEntityJaversHolder(v.build()));
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.error("Error in Building Javers Instance",e);
					}
				});
			}
			helper.postProcessJaversBuilderInstance(holder);
			return holder;
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in Building Javers Instance",e);
			throw e;
		}
	}

	public void preProcess(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity) {

	}

	public void postProcess(MasterChangeDiffHolder diffHolder, MasterChangeEntityHolder oldEntity,
			MasterChangeEntityHolder newEntity) {

	}

	
	public void postProcessJaversBuilderInstance(MasterChangeJaversHolder holder){
		
	}
	
	public MasterChangeExecutionHelper withCustomFieldDiffhandler(BiDiTreeNodePointerByField fieldPointer,
			MasterChangeFieldDiffCalculator fieldDiffCalculator){
		if(fieldPointer ==null || fieldDiffCalculator == null){
			return this;
		}
		if(this.customeFieldsDiffCalculator == null){
			this.customeFieldsDiffCalculator = new HashMap<>();
		}
		this.customeFieldsDiffCalculator.put(fieldPointer, fieldDiffCalculator);
		return this;
	}
	
	public MasterChangeFieldDiffCalculator getfieldDiffCalculator(BiDiTreeNodePointerByField fieldPointer){
		if(customeFieldsDiffCalculator != null){
			return customeFieldsDiffCalculator.get(fieldPointer);
		}
		return null;
	}
	
	public MasterChangeExecutionHelper withFieldFormatterhandler(BiDiTreeNodePointerByField fieldPointer,
			MasterChangeFieldFormatter fieldFormatter){
		if(fieldPointer ==null || fieldFormatter == null){
			return this;
		}
		if(this.fieldFormatter == null){
			this.fieldFormatter = new HashMap<>();
		}
		this.fieldFormatter.put(fieldPointer, fieldFormatter);
		return this;
	}
	
	public MasterChangeFieldFormatter getFieldFormatter(BiDiTreeNodePointerByField fieldPointer){
		if(fieldFormatter != null){
			return fieldFormatter.get(fieldPointer);
		}
		return null;
	}
	
	public MasterChangeFieldFormatter getDateFormatterInstance(){
		return new DateFormatter();
	}
	
	
	public MasterChangeFieldFormatter getDateTimeToDateFormatterInstance(){
		return new DateTimeToDateFormatter();
	}

	public MasterChangeFieldFormatter getDateTimeToTimeFormatterInstance(){
		return new DateTimeToTimeFormatter();
	}
	
	public MasterChangeFieldFormatter getIdToValueResolver(Class cls,String disPlayColumn,BaseMasterDao masterDao){
		return new IdToValueResolver(cls,disPlayColumn,masterDao);
	}
	
}

class DateFormatter implements MasterChangeFieldFormatter{

	@Override
	public String format(Object value) {
		if(value == null){
			return StringUtils.EMPTY;
		}
		Date d = (Date)value;
		return new SimpleDateFormat("dd/MM/yyyy").format(d);
	}
	
}


class DateTimeToDateFormatter implements MasterChangeFieldFormatter{

	@Override
	public String format(Object value) {
		if(value == null){
			return StringUtils.EMPTY;
		}
		DateTime d = (DateTime)value;
		return new SimpleDateFormat("dd/MM/yyyy").format(d.toDate());
	}
	
}

class DateTimeToTimeFormatter implements MasterChangeFieldFormatter{

	@Override
	public String format(Object value) {
		if(value == null){
			return StringUtils.EMPTY;
		}
		DateTime d = (DateTime)value;
		return new SimpleDateFormat("hh:mm a").format(d.toDate());
	}

}

class IdToValueResolver implements MasterChangeFieldFormatter{

	private Class clsInstance;
	private String clsFieldName;
	private BaseMasterDao dao;
	
	public IdToValueResolver(Class clsInstance, String clsFieldName, BaseMasterDao dao) {
		super();
		this.clsInstance = clsInstance;
		this.clsFieldName = clsFieldName;
		this.dao = dao;
	}

	@Override
	public String format(Object value) {
		if(value == null){
			return StringUtils.EMPTY;
		}
		Long valInLong = (Long)value;
		Object result = dao.getColumnValueFromEntity(clsInstance, valInLong, clsFieldName);
		if(result !=null){
			return result.toString();
		}
		return StringUtils.EMPTY;
	}
	
}
