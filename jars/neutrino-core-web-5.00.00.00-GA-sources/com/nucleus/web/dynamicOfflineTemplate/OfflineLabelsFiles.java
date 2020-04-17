package com.nucleus.web.dynamicOfflineTemplate;

import java.util.ArrayList;
import java.util.List;

public class OfflineLabelsFiles {

	public static List<String> offlineLablesList;

	static {
		offlineLablesList = new ArrayList<String>();
		offlineLablesList.add("JS_Messages");
		offlineLablesList.add("JS_cas_Messages");
		offlineLablesList.add("JS_master_Messages");
		offlineLablesList.add("JS_rule_Messages");

	}
}
