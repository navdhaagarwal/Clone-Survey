package com.nucleussoft.reactive.dummy;

import java.util.List;

import org.apache.poi.ss.formula.functions.T;

import com.nucleus.dao.query.QueryExecutor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveEntityDao {


    public <T> Flux<T> executeQuery(QueryExecutor<T> executor);

    public <T> Mono<List<T>> executeQueryAsync(QueryExecutor<T> executor);

    public <T> Flux<T> executeQuery(QueryExecutor<T> executor, Integer startIndex, Integer pageSize);

    public <T> Mono<List<T>> executeQueryAsync(QueryExecutor<T> executor, Integer startIndex, Integer pageSize);


}
