package com.nucleus.core.transaction;

@FunctionalInterface
public interface TransactionPostCommitWork {
	
	public void work(Object argument);

}
