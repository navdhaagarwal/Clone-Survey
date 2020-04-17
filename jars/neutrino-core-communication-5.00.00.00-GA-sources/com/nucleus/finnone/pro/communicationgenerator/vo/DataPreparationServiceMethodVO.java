package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.io.Serializable;

public class DataPreparationServiceMethodVO  implements Serializable {
	
    private static final long serialVersionUID = 74267278132163712L;
    
	private String serviceInterfaceName;
	private String targetServiceName;
	private String targetMethodName;
	
	public String getServiceInterfaceName() {
		return serviceInterfaceName;
	}
	public void setServiceInterfaceName(String serviceInterfaceName) {
		this.serviceInterfaceName = serviceInterfaceName;
	}
	public String getTargetServiceName() {
		return targetServiceName;
	}
	public void setTargetServiceName(String targetServiceName) {
		this.targetServiceName = targetServiceName;
	}
	public String getTargetMethodName() {
		return targetMethodName;
	}
	public void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}
	
	@Override
	public int hashCode() {
		return new StringBuilder(getStringValue(serviceInterfaceName)).append(getStringValue(targetServiceName))
				.append(getStringValue(targetMethodName)).toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataPreparationServiceMethodVO other = (DataPreparationServiceMethodVO) obj;
		if (serviceInterfaceName == null) {
			if (other.serviceInterfaceName != null)
				return false;
		} else if (!serviceInterfaceName.equals(other.serviceInterfaceName))
			return false;
		if (targetMethodName == null) {
			if (other.targetMethodName != null)
				return false;
		} else if (!targetMethodName.equals(other.targetMethodName))
			return false;
		if (targetServiceName == null) {
			if (other.targetServiceName != null)
				return false;
		} else if (!targetServiceName.equals(other.targetServiceName))
			return false;
		return true;
	}

	private String getStringValue(String str) {
		return str == null ? "" : str;
	}
}
