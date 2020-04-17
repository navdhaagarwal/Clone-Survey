package com.nucleus.process.beans;

import com.nucleus.master.BaseMasterEntity;

public interface EntityApprovalPreProcessor {

    void handleApprovalForModification(BaseMasterEntity originalRecord, 
			BaseMasterEntity toBeDeletedRecord, 
			BaseMasterEntity toBeHistoryRecord, 
			Long reviewerId);
    void handleApprovalForNew(BaseMasterEntity originalRecord, 
			BaseMasterEntity toBeDeletedRecord, 
			BaseMasterEntity toBeHistoryRecord, 
			Long reviewerId);

	void handleDeclineForModification(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			Long reviewerId);
	void handleDeclineForNew(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			Long reviewerId);

	void handleSendBackForNew(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			BaseMasterEntity toBeHistoryRecord,
			Long reviewerId);
	void handleSendBackForModification(BaseMasterEntity originalRecord,
			BaseMasterEntity toBeDeletedRecord,
			BaseMasterEntity toBeHistoryRecord,
			Long reviewerId);



	default void handleApprovalForDeletion(BaseMasterEntity originalRecord,
														   BaseMasterEntity toBeDeletedRecord,
														   BaseMasterEntity toBeHistoryRecord,
														   Long reviewerId){

	}

}
