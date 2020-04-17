package com.nucleus.core.transaction;

public interface TransactionPostCommitWorkFailureHandler extends TransactionPostCommitWork{

	public void handleFailure(Object argument);
	
}
