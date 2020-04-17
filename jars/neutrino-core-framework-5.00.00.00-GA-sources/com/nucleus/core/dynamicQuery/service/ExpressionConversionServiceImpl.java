/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.dynamicQuery.service;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.nucleus.core.dynamicQuery.crossToken.CrossTokenValueResolver;
import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.support.ExpressionChangeVisiter;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("expressionConversionService")
public class ExpressionConversionServiceImpl implements ExpressionConversionService, BeanPostProcessor {

    @Inject
    @Named(value = "dynamicQueryMetadataService")
    private DynamicQueryMetadataService         dynamicQueryMetadataService;

    private final List<CrossTokenValueResolver> crossTokenValueResolverRegistry = new LinkedList<CrossTokenValueResolver>();

    public boolean convertExpression(ArrayDeque<Expression> expressions, Expression oldExp, QueryContext context,
            QueryToken queryToken, PlainSelect plianSelect) {

        CrossTokenValueResolver tokenValueResolver = null;

        for (CrossTokenValueResolver tokenValueResolver1 : crossTokenValueResolverRegistry) {
            if (tokenValueResolver1.canResolve(queryToken, context)) {
                tokenValueResolver = tokenValueResolver1;
                break;
            }
        }
        if (tokenValueResolver == null) {
            throw new IllegalStateException(
                    "No resolver(an bean of type CrossTokenValueResolver) found for dyanmic query cross  token:"
                            + queryToken);
        }

        Iterator<Expression> iterator = expressions.descendingIterator();
        while (iterator.hasNext()) {
            Expression parentExp = iterator.next();
            ExpressionChangeVisiter changeVisiter = new ExpressionChangeVisiter(oldExp, dynamicQueryMetadataService,
                    tokenValueResolver, context, queryToken, plianSelect);
            parentExp.accept(changeVisiter);
            if (changeVisiter.isDone()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CrossTokenValueResolver) {
            CrossTokenValueResolver tokenValueResolver = (CrossTokenValueResolver) bean;
            // All Spring proxies implement TargetClassAware interface
            String resolverClass = AopUtils.getTargetClass(tokenValueResolver).getName();
            BaseLoggers.eventLogger.info("Registered CrossTokenValueResolver : -> {}", resolverClass);
            crossTokenValueResolverRegistry.add(tokenValueResolver);
        }
        return bean;
    }

}
