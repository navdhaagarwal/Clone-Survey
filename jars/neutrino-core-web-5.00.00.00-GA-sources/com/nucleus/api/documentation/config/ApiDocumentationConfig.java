package com.nucleus.api.documentation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.nucleus.core.initialization.ProductInformationLoader;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class ApiDocumentationConfig {

	@Bean
	public Docket createDocketBean() {
		return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false)
				.groupName(ProductInformationLoader.getProductCode()).select().apis(apiAnnotations()).paths(paths())
				.build();

	}

	private Predicate<RequestHandler> apiAnnotations() {
		return Predicates.and(RequestHandlerSelectors.withClassAnnotation(Api.class),
				RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
	}

	private Predicate<String> paths() {
		return PathSelectors.any();
	}

}
