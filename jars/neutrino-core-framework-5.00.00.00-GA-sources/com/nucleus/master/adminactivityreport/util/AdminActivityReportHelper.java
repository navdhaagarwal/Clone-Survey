package com.nucleus.master.adminactivityreport.util;

import com.nucleus.entity.BaseEntity;
import com.nucleus.service.BaseServiceImpl;
import org.springframework.stereotype.Component;

@Component("adminActivityReportHelper")
public interface AdminActivityReportHelper  {


	public void processReport(BaseEntity oldEntity, BaseEntity newEntity);

	
}
