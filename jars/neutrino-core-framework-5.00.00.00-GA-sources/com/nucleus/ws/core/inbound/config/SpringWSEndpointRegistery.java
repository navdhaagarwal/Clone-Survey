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
package com.nucleus.ws.core.inbound.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public final class SpringWSEndpointRegistery extends ApplicationObjectSupport implements Ordered {

    private static final Logger                              LOGGER             = LoggerFactory
                                                                                        .getLogger(SpringWSEndpointRegistery.class);

    private final static Map<MethodEndpoint, WSEndpointInfo> securedServicesMap = new HashMap<MethodEndpoint, WSEndpointInfo>();
    private final static Set<String>                         allAuthorities     = new HashSet<String>();

    private int                                              order              = Integer.MAX_VALUE;                                // default:
                                                                                                                                     // same
                                                                                                                                     // as
                                                                                                                                     // non-Ordered
    private boolean                                          detectEndpointsInAncestorContexts;

    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Looking for endpoints in application context: " + getApplicationContext());
        }
        String[] beanNames = (this.detectEndpointsInAncestorContexts ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                getApplicationContext(), Object.class) : getApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {
            Class<?> endpointClass = getApplicationContext().getType(beanName);
            if (endpointClass != null && AnnotationUtils.findAnnotation(endpointClass, getEndpointAnnotationType()) != null) {
                registerMethods(beanName);
            }
        }
    }

    /** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
    protected Class<? extends Annotation> getEndpointAnnotationType() {
        return Endpoint.class;
    }

    /**
     * Helper method that registers the methods of the given class. This method iterates over the methods of the class,
     * and calls {@link #getLookupKeyForMethod(Method)} for each. If this returns a string, the method is registered
     * using {@link #registerEndpoint(Object, MethodEndpoint)}.
     *
     * @see #getLookupKeyForMethod(Method)
     */
    protected void registerMethods(String beanName) {
        Assert.hasText(beanName, "'beanName' must not be empty");
        Class<?> endpointType = getApplicationContext().getType(beanName);
        endpointType = ClassUtils.getUserClass(endpointType);

        Set<Method> methods = findEndpointMethods(endpointType, new ReflectionUtils.MethodFilter() {
            public boolean matches(Method method) {
                return getLookupKeyForMethod(method) != null;
            }
        });

        for (Method method : methods) {
            MethodEndpoint methodEndpoint = new MethodEndpoint(beanName, getApplicationContext(), method);
            Set<String> authoritiesAllowed = new HashSet<String>();
            NeutrinoSecuredWebService securityInfo = AnnotationUtils.findAnnotation(method, NeutrinoSecuredWebService.class);
            if (securityInfo != null) {
                String serviceId = securityInfo.serviceId();
                NeutrinoValidator.notEmpty(serviceId, "Service id can not be null or empty for NeutrinoSecuredWebService ["
                        + endpointType + "]");
                String[] authoritiesAllowedArr = securityInfo.authoritiesAllowed();
                if (authoritiesAllowedArr != null && authoritiesAllowedArr.length > 0) {
                    for (int i = 0 ; i < authoritiesAllowedArr.length ; i++) {
                        authoritiesAllowed.add(authoritiesAllowedArr[i]);
                        allAuthorities.add(authoritiesAllowedArr[i]);
                    }
                }
                LOGGER.info(
                        "Found endpoint [{}] annoted with NeutrinoSecuredWebService.Endpoint is secured and access will be allowed only to users having authorities [{}].",
                        methodEndpoint, authoritiesAllowed);
                registerEndpoint(methodEndpoint, new WSEndpointInfo(serviceId, authoritiesAllowed, true));
            } else {
                // register non annotated services but keep them un-secure initially.
                LOGGER.warn("Endpoint [{}] not annoted with NeutrinoSecuredWebService.This endpoint is not secure.",
                        methodEndpoint);
                String defaultNameForService = WordUtils.capitalize(method.getName());
                String defaultServiceName = (defaultNameForService.concat("_") + (securedServicesMap.size() + 1));
                String defaultAuthNameForService = defaultServiceName.concat("_AUTH");
                authoritiesAllowed.add(defaultAuthNameForService);
                allAuthorities.add(defaultAuthNameForService);
                registerEndpoint(methodEndpoint, new WSEndpointInfo(defaultServiceName, authoritiesAllowed, false));
            }

        }

    }

    private void registerEndpoint(MethodEndpoint key, WSEndpointInfo wsEndpointInfo) {

        securedServicesMap.put(key, wsEndpointInfo);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Registered ws secured endpoint method  [" + key + "] for service info [" + wsEndpointInfo + "]");
        }
    }

    private Object getLookupKeyForMethod(Method method) {
        PayloadRoot annotation = AnnotationUtils.findAnnotation(method, PayloadRoot.class);
        if (annotation != null) {
            QName qname;
            if (StringUtils.hasLength(annotation.localPart()) && StringUtils.hasLength(annotation.namespace())) {
                qname = new QName(annotation.namespace(), annotation.localPart());
            } else {
                qname = new QName(annotation.localPart());
            }
            return qname;
        } else {
            SoapAction soapAction = AnnotationUtils.findAnnotation(method, SoapAction.class);
            return soapAction != null ? soapAction.value() : null;
        }
    }

    private Set<Method> findEndpointMethods(Class<?> endpointType, final ReflectionUtils.MethodFilter endpointMethodFilter) {
        final Set<Method> endpointMethods = new LinkedHashSet<Method>();
        Set<Class<?>> endpointTypes = new LinkedHashSet<Class<?>>();
        Class<?> specificEndpointType = null;
        if (!Proxy.isProxyClass(endpointType)) {
            endpointTypes.add(endpointType);
            specificEndpointType = endpointType;
        }
        endpointTypes.addAll(Arrays.asList(endpointType.getInterfaces()));
        for (Class<?> currentEndpointType : endpointTypes) {
            final Class<?> targetClass = (specificEndpointType != null ? specificEndpointType : currentEndpointType);
            ReflectionUtils.doWithMethods(currentEndpointType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (endpointMethodFilter.matches(specificMethod)
                            && (bridgedMethod == specificMethod || !endpointMethodFilter.matches(bridgedMethod))) {
                        endpointMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return endpointMethods;
    }

    @Override
    public int getOrder() {
        return order;
    }

    // These methods are kept static to make them accessible in other contexts
    public static WSEndpointInfo getInfoForEndpoint(MethodEndpoint endpoint) {
        return securedServicesMap.get(endpoint);
    }

    public static ArrayList<WSEndpointInfo> getAllDetectedServices() {
        return new ArrayList<WSEndpointInfo>(securedServicesMap.values());
    }

    public static Set<String> getAllowedAuthoritiesForAllEndpoints() {
        return new HashSet<String>(allAuthorities);
    }

}
