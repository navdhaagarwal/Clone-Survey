package com.nucleus.shortcut;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Hibernate;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseServiceImpl;

@Named("hotkeyService")
public class HotkeyServiceImpl extends BaseServiceImpl implements HotkeyService {

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Override
	public List<Hotkeys> getHotKeysBasedOnType(String type) {
		NamedQueryExecutor<Hotkeys> executor = new NamedQueryExecutor<>("getHotKeys");

		addParameterToExecutor(executor, type, getSourceProductIdList());

		List<Hotkeys> data = entityDao.executeQuery(executor);

		for (Hotkeys hk : data) {
			Hibernate.initialize(hk.getElementMapping());
		}

		return data;

	}

	private List<Long> getSourceProductIdList() {
		SourceProduct sourceProduct = genericParameterService.findByCode(ProductInformationLoader.getProductName(),
				SourceProduct.class);
		List<Long> sourceProductIdList = new ArrayList<>();
		if (sourceProduct != null) {
			sourceProductIdList.add(sourceProduct.getId());
		}
		
		/* Adding null in sourceProductIdList is for giving the provision 
		 * that in future if some hotkeys are provided by FW will be 
		 * available in every product module by default.
		 */
		sourceProductIdList.add(null);
		return sourceProductIdList;
	}

	private void addParameterToExecutor(NamedQueryExecutor<Hotkeys> executor, String type,
			List<Long> sourceProductIdList) {
		executor.addParameter("hotKeyType", Integer.parseInt(type))
				.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
				.addParameter("sourceProduct", sourceProductIdList);
	}

	@Override
	public List<Hotkeys> getAllHotKeys() {
		List<Hotkeys> hotkeysData = new ArrayList<>();
		for (HotKeyType hotKeyType : HotKeyType.values()) {
			if (!(HotKeyType.NOT_ALLOWED.getHotKeyTypeCode().equals(hotKeyType.getHotKeyTypeCode()))) {
			  hotkeysData.addAll(getHotKeysBasedOnType(hotKeyType.getHotKeyTypeCode()));
			}
		}
		return hotkeysData;
	}
}
