package com.nucleussoft.reactive.init;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import reactor.core.publisher.Mono;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class DeligatingHttpHandler implements HttpHandler{

	private HttpHandler actualHttpHandler;
		
	@Override
	public Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response) {
		if(this.actualHttpHandler==null){
			this.actualHttpHandler=initActualHttpHandler();
		}
		return this.actualHttpHandler.handle(request, response);
	}

	protected HttpHandler initActualHttpHandler() {
		return WebHttpHandlerBuilder.applicationContext(ReactiveContextUtil.getReactiveAppContext()).build();
	}

}
