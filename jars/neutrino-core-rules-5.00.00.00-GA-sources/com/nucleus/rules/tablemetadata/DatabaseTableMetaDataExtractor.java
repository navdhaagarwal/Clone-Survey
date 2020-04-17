package com.nucleus.rules.tablemetadata;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

@Component
public class DatabaseTableMetaDataExtractor {

	@Inject
	@Named("databaseTableMetaDataExtractorEngine")
	private DatabaseTableMetaDataExtractorEngine metaDataEngine;

	public void init(List<String> rootTables) {
		metaDataEngine.start(rootTables);
	}

	public TableJoinPathResponse findPathBetweenTables(List<String> joinedTables) {
		final TableJoinPathResponse resonse = new TableJoinPathResponse();
		resonse.setPathFound(true);
		StringBuilder message = new StringBuilder();
		// validating all tables are avaliable or not
		joinedTables.forEach((t) -> {
			if (metaDataEngine.getAllLoadedTables().get(t) == null) {
				resonse.setPathFound(false);
				appendMessage(message, t + " : Not A Valid Table Name");
			}
		});
		if (resonse.isPathFound()) {

		}
		return resonse;
	}

	public void traverse(String rootTable, Set<String> tableNeeded, Set<String> tableFound,
			Set<String> tabledAlreadyparsed,TableJoinNodes currentNode,StringBuilder whereClause) {
		

	}

	private void appendMessage(StringBuilder messages, String message) {
		if (messages.length() > 0) {
			messages.append("\n").append(",");
		}
		messages.append(message);
	}
}
