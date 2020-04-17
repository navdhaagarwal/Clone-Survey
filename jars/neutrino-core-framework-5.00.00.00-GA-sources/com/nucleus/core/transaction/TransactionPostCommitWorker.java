package com.nucleus.core.transaction;
import java.util.concurrent.Executor;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.thread.support.MdcRetainingRunnable;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;

public class TransactionPostCommitWorker {

	public static void  handlePostCommit(final TransactionPostCommitWork postCommitWork, final Object argument, final boolean async){
		handlePostCommit( postCommitWork,   argument,   async,NeutrinoSpringAppContextUtil.getBeanByName("neutrinoThreadPoolExecutor", Executor.class));
	}
	
	public static void  handlePostCommitAsyncExecutor(final TransactionPostCommitWork postCommitWork, final Object argument, final boolean async){
		handlePostCommit( postCommitWork,   argument,   async,NeutrinoSpringAppContextUtil.getBeanByName("neutrinoThreadPoolExecutor", Executor.class));//neutrinoAsyncTaskExecutor
	}

	public static void handlePostCommit(final TransactionPostCommitWork postCommitWork, final Object argument,
			final boolean async, final Executor taskExecutor) {
		if (taskExecutor == null) {
			throw new SystemException("task Executor cannot be null");
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCommit() {
				BaseLoggers.flowLogger.debug("Executing postcommit for -->{}", postCommitWork.getClass().getName());
				if (async) {
					asyncExecutePostCommit(postCommitWork, argument, taskExecutor);
				} else {
					try {
						postCommitWork.work(argument);
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.error(
								"Exception in postcommit work, handling failure if implemented, for -->{}",
								e);
						if (TransactionPostCommitWorkFailureHandler.class.isAssignableFrom(postCommitWork.getClass())){
							((TransactionPostCommitWorkFailureHandler) postCommitWork).handleFailure(argument);
						}
					}
				}

				BaseLoggers.flowLogger.debug("Leaving postcommit for -->{}", postCommitWork.getClass().getName());
			}
		});
	}
	
	private static void asyncExecutePostCommit(final TransactionPostCommitWork postCommitWork, final Object argument, final Executor taskExecutor)
	{
		try {
			taskExecutor.execute(new MdcRetainingRunnable() {
				@Override
				protected void runWithMdc() {
					try {
						postCommitWork.work(argument);
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.error(
								"Exception in postcommit work (async), handling failure if implemented, for -->{}",
								e);
						if (TransactionPostCommitWorkFailureHandler.class.isAssignableFrom(postCommitWork.getClass())){
							((TransactionPostCommitWorkFailureHandler) postCommitWork).handleFailure(argument);
						}
					}
				}
			});
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(
					"Exception in postcommit task execution, handling failure if implemented, for -->{}",
					e);
			if (TransactionPostCommitWorkFailureHandler.class.isAssignableFrom(postCommitWork.getClass())){
				((TransactionPostCommitWorkFailureHandler) postCommitWork).handleFailure(argument);
			}
		}
	}

}
