package com.nucleus.web.state.master;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nucleus.address.District;
import com.nucleus.core.role.entity.Role;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserServiceImpl;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/testBM")
public class TestController extends BaseController{


	@Inject
	@Named("baseMasterService")
	private BaseMasterService   baseMasterService;

	@Inject
	@Named("userService")
	protected UserServiceImpl         userService;

	@RequestMapping("/show")
	public String show(ModelMap map){
		System.out.println("********************** IN Controller");
		List<District> districts = baseMasterService.getLastApprovedEntities(District.class);
		System.out.println("********************** IN Controller got list :"+districts.size());

		List<Role> roleList = baseMasterService.getAllApprovedAndActiveEntities(Role.class);
		List<Role> userRoles = userService.getRolesFromUserId(9011l);
		
		System.out.println("********************** IN Controller got role list :"+roleList.size()+" user role list :"+userRoles.size());

		
		map.put("districtName1", districts.get(1).getDistrictName());
		map.put("districtName2", districts.get(2).getDistrictName());
		map.put("districtList1", districts);
		map.put("districtList", districts);
		map.put("dist", districts.get(21));
		map.put("dist1", districts.get(22));

		map.put("districtIds", districts.get(22).getId()+","+districts.get(16).getId()+","+districts.get(45).getId());
		map.put("districtIds1", districts.get(21).getId()+","+districts.get(15).getId()+","+districts.get(65).getId());
		
		map.put("userRoles", userRoles);
        map.put("roleList", roleList);
		
		TestPojo tp = new TestPojo();
		tp.setDistrictName1(districts.get(1).getDistrictName());
		tp.setDistrictName2(districts.get(2).getDistrictName());
		tp.setDistrictList(districts);
		tp.setDistrictList1(districts);
		tp.setDist(districts.get(21));
		tp.setDist1(districts.get(22));

		tp.setDistrictIds(districts.get(22).getId()+","+districts.get(16).getId()+","+districts.get(45).getId());
		tp.setDistrictIds1(districts.get(21).getId()+","+districts.get(15).getId()+","+districts.get(65).getId());
		map.put("testBM", tp);
		return "testBM";
	}

	@RequestMapping(value="/submit", method = RequestMethod.POST)
	public String submit(){
		System.out.println("********************** IN Controller SUBMIT");
		return "display";
	}
}
