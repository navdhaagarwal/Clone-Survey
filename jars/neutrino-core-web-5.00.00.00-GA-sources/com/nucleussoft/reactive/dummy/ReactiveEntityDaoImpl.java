package com.nucleussoft.reactive.dummy;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.dao.query.QueryExecutor;
import com.nucleus.persistence.EntityDao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Named("reactEntityDao")
public class ReactiveEntityDaoImpl implements ReactiveEntityDao{

	@Inject
    @Named("entityDao")
    protected EntityDao         entityDao;
    
	@Value("${spring.webflux.scheduler.count}")
	private String count;
	
	Scheduler scheduler;//=Schedulers.newParallel("DAO", 500);
	
	@Override
	public <T> Flux<T> executeQuery(QueryExecutor<T> executor) {
		return Flux.fromIterable(entityDao.executeQuery(executor));
	}

	@Override
	public <T> Mono<List<T>> executeQueryAsync(QueryExecutor<T> executor) {
		return  Mono.fromCallable(() -> entityDao.executeQuery(executor)).publishOn(scheduler);
		//return Flux.fromIterable(entityDao.executeQuery(executor)).publishOn(Schedulers.parallel());
		/*
		 * 		return  Mono.fromCallable(() -> {
		      TransactionDefinition def = new DefaultTransactionDefinition();
		      TransactionStatus status = transactionManager.getTransaction(def);
		      List<T> list=entityDao.executeQuery(executor);
		      transactionManager.commit(status);
			return list;
		}).subscribeOn(Schedulers.elastic());

		 */
	}
	
	@PostConstruct
	public void callOnPostConstuct(){
		scheduler=Schedulers.newParallel("DAO", Integer.parseInt(count));
	}

	
	@PreDestroy
	public void callOnPreDesctroy(){
		Schedulers.shutdownNow();
	}


	@Override
	public <T> Flux<T> executeQuery(QueryExecutor<T> executor, Integer startIndex, Integer pageSize) {
		return Flux.fromIterable(entityDao.executeQuery(executor, startIndex, pageSize));
	}

	@Override
	public <T> Mono<List<T>> executeQueryAsync(QueryExecutor<T> executor, Integer startIndex, Integer pageSize) {
		return  Mono.fromCallable(() -> entityDao.executeQuery(executor, startIndex, pageSize)).publishOn(scheduler);
//		return Flux.fromIterable(entityDao.executeQuery(executor, startIndex, pageSize)).publishOn(Schedulers.parallel());
	}

}
