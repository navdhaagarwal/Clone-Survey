package com.nucleus.web.state.master;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.address.District;

public class TestPojo {

	private String districtName2;
	
	private String districtName1;

	private List<District> districtList;
	private District dist;
	
	private List<District> districtList1;
	private District dist1;
	
	private String districtIds;
	private String districtIds1;
	
	private String roleIds;
	
	private ArrayList<String> selectedValueList;
	
	public List<District> getDistrictList1() {
		return districtList1;
	}

	public void setDistrictList1(List<District> districtList1) {
		this.districtList1 = districtList1;
	}

	public District getDist1() {
		return dist1;
	}

	public void setDist1(District dist1) {
		this.dist1 = dist1;
	}

	public District getDist() {
		return dist;
	}

	public void setDist(District dist) {
		this.dist = dist;
	}

	public List<District> getDistrictList() {
		return districtList;
	}

	public void setDistrictList(List<District> districtList) {
		this.districtList = districtList;
	}

	public String getDistrictName2() {
		return districtName2;
	}

	public void setDistrictName2(String districtName2) {
		this.districtName2 = districtName2;
	}

	public String getDistrictName1() {
		return districtName1;
	}

	public void setDistrictName1(String districtName1) {
		this.districtName1 = districtName1;
	}

	public String getDistrictIds() {
		return districtIds;
	}

	public void setDistrictIds(String districtIds) {
		this.districtIds = districtIds;
	}

	public String getDistrictIds1() {
		return districtIds1;
	}

	public void setDistrictIds1(String districtIds1) {
		this.districtIds1 = districtIds1;
	}

	public String getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(String roleIds) {
		this.roleIds = roleIds;
	}

	public ArrayList<String> getSelectedValueList() {
		return selectedValueList;
	}

	public void setSelectedValueList(ArrayList<String> selectedValueList) {
		this.selectedValueList = selectedValueList;
	}
}
