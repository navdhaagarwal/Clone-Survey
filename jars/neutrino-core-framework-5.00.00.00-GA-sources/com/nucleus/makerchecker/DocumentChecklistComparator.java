package com.nucleus.makerchecker;

import java.util.Comparator;

import com.nucleus.document.core.entity.DocumentChecklistDefinition;
import com.nucleus.entity.Entity;

public class DocumentChecklistComparator implements Comparator<DocumentChecklistDefinition> {
	
	String fieldForSorting1 = null;
	String fieldForSorting2 = null;
	
	private final Integer iSortCol_0;
	private final String sSortDir_0;
	
	 public DocumentChecklistComparator (Integer iSortCol_0,String sSortDir_0){
	         this.iSortCol_0 = iSortCol_0;
	         this.sSortDir_0 = sSortDir_0;
	     }

	@Override
	public int compare(DocumentChecklistDefinition documentChecklistDefinition1, DocumentChecklistDefinition documentChecklistDefinition2) {

		if (iSortCol_0 == 5) {
			fieldForSorting1 = documentChecklistDefinition1.getDocument().getDescription();
			fieldForSorting2 = documentChecklistDefinition2.getDocument().getDescription();
		} else if (iSortCol_0 == 6) {
			fieldForSorting1 = documentChecklistDefinition1.getClassificationType().getName();
			fieldForSorting2 = documentChecklistDefinition2.getClassificationType().getName();
		} else if (iSortCol_0 == 7) {
			fieldForSorting1 = documentChecklistDefinition1.getSource();
			fieldForSorting2 = documentChecklistDefinition2.getSource();
		}

		if (fieldForSorting1 == null && fieldForSorting2 == null) {
			return 0;
		} else if (fieldForSorting1 == null) {
			return 1;
		} else if (fieldForSorting2 == null) {
			return -1;
		} else if (fieldForSorting1.equalsIgnoreCase(fieldForSorting2)) {
			return 0;
		} else if (sSortDir_0.equalsIgnoreCase("ASC")) {
			return fieldForSorting1.compareToIgnoreCase(fieldForSorting2);
		} else {
			return fieldForSorting2.compareToIgnoreCase(fieldForSorting1);
		}
	}
	
	

}
