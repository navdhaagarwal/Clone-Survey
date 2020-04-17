package com.nucleussoft.reactive.dummy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.BaseStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;

import com.nucleus.address.Country;
import com.nucleus.core.notification.service.NotificationService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserInfo;

import flexjson.JSONSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequestMapping(value = "/test-flux")
@Transactional
public class ReactiveController {

	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Autowired
	private ReactorService reactorService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ReactiveNotificationServiceImpl reactiveNotificationService;

    @Value("${nio.pathToRead}")
    private String pathToRead;

	
	@Inject
	@Named("notificationService")
	private NotificationService notificationService;
	
	private static ClientHttpConnector httpConnector = new ReactorClientHttpConnector();

	@GetMapping("/test")
	@ResponseBody
	public Publisher<String> handler(@RequestParam("throwError") String throwError) throws InterruptedException {
		return Mono.create((sink) -> {

			TransactionDefinition def = new DefaultTransactionDefinition();
			TransactionStatus status = transactionManager.getTransaction(def);
			BaseLoggers.flowLogger.error("Called ReactiveController");
			Country country = new Country();
			country.setCountryName(UUID.randomUUID().toString());
			entityDao.persist(country);
			if (StringUtils.isNoneBlank(throwError)) {
				throw new RuntimeException();
			}
			transactionManager.commit(status);

			sink.success("Hi There " + country.getId() + " " + Thread.currentThread().getName());
		});
		// return Mono.just("Hi There"+country.getId());
	}

	@GetMapping("/selectWithMono")
	@ResponseBody
	public Publisher<String> selectQuery(@RequestParam("throwError") String throwError) throws InterruptedException {
		return Mono.create((sink) -> {
			sink.success("Hi There " + reactorService.getCountryById(5000000L).getCountryName() + " "
					+ Thread.currentThread().getName());
		});
	}

	@GetMapping("/select")
	@ResponseBody
	public String simpleSelectQuery(@RequestParam("throwError") String throwError) throws InterruptedException {
		return "Hi There " + reactorService.getCountryById(5000000L).getCountryName() + " "
				+ Thread.currentThread().getName();
	}

	@GetMapping(value = "/notification", produces = "application/octet-stream")
	@ResponseBody
	public Publisher<String> notification(@RequestParam("throwError") String throwError) throws InterruptedException {
		BaseLoggers.flowLogger.error("Called ReactiveController");
		Country country = new Country();
		country.setCountryName(UUID.randomUUID().toString());
		entityDao.persist(country);
		if (StringUtils.isNoneBlank(throwError)) {
			throw new RuntimeException();
		}
		Flux<String> flux1 = Flux.just("{1}", "{2}", "{3}", "{4}", "{1}", "{2}", "{3}", "{4}", "{1}", "{2}", "{3}",
				"{4}", "{1}", "{2}", "{3}", "{4}");
		Flux<String> intervalFlux1 = Flux.interval(Duration.ofMillis(500)).zipWith(flux1,
				(i, string) -> "->" + string + "" + Thread.currentThread().getName());
		return intervalFlux1;
	}

	@RequestMapping(value = "/getNotifications", method = RequestMethod.GET)
	public @ResponseBody Mono<Object> getNotifications(Locale locale,
			@RequestParam("number") Integer notificationsToShow,
			@RequestParam("currentUserUri") String currentUserUri) {
		return Mono.create((sink) -> {
			List<Map<String, Object>> localizedNotificationList = null;
			if (currentUserUri != null) {
				localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
						notificationsToShow);
			}
			JSONSerializer serializer = new JSONSerializer();
			sink.success(serializer.serialize(localizedNotificationList));
		}).publishOn(Schedulers.parallel());
	}

	@RequestMapping(value = "/getNotificationsNbioDao", method = RequestMethod.GET)
	public @ResponseBody Mono<Object> getNotificationsNbioDao(Locale locale,
			@RequestParam("number") Integer notificationsToShow,
			@RequestParam("currentUserUri") String currentUserUri) {

		return reactiveNotificationService
				.getLocalizedUserNotificationsReact(currentUserUri, locale, notificationsToShow).collectList()
				.flatMap(localizedNotificationList -> {
					JSONSerializer serializer = new JSONSerializer();
					return Mono.just(serializer.serialize(localizedNotificationList));
				});
	}

	@RequestMapping(value = "/getNotificationsNbioDaoAsync", method = RequestMethod.GET)
	public @ResponseBody Mono<String> getNotificationsNbioDaoAsync(Locale locale,
			@RequestParam("number") Integer notificationsToShow,
			@RequestParam("currentUserUri") String currentUserUri) {
		return reactiveNotificationService
				.getLocalizedUserNotificationsReactAsync(currentUserUri, locale, notificationsToShow)
				.map((listOfNot) -> {
					JSONSerializer serializer = new JSONSerializer();
					return serializer.serialize(listOfNot);
				});
	}		
		
		@RequestMapping(value = "/getNotificationsBigString", method = RequestMethod.GET)
		public @ResponseBody Mono<String> getNotificationsBigString(Locale locale,
				@RequestParam("number") Integer notificationsToShow,
				@RequestParam("currentUserUri") String currentUserUri) {
			return Mono.just("WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
					"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
					"WEB-INF/tiles-core.xml\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
					"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
					"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
					";");


	}
		
		
		@RequestMapping(value = "/getNotificationsDataAsync", method = RequestMethod.GET)
		public @ResponseBody Mono<Object> getNotificationsDataAsync(Locale locale,
				@RequestParam("number") Integer notificationsToShow,@RequestParam("multiplyFactor") Integer multiplyFactor,
				@RequestParam("currentUserUri") String currentUserUri) {
			return reactiveNotificationService
					.getLocalizedUserNotificationsReactAsync(currentUserUri, locale, notificationsToShow)
					.map((listOfNot) -> {
						JSONSerializer serializer = new JSONSerializer();
						String result=serializer.serialize(listOfNot);
						StringBuilder stringBuilder=new StringBuilder();
						for(int i=0;i<multiplyFactor;i++){
							stringBuilder.append(result);
						}
						return stringBuilder.toString();
					});
		}
		
		
		@RequestMapping(value = "/getNotificationsDataAsyncFlux", method = RequestMethod.GET)
		public @ResponseBody Flux<Object> getNotificationsDataAsyncFlux(Locale locale,
				@RequestParam("number") Integer notificationsToShow,@RequestParam("multiplyFactor") Integer multiplyFactor,
				@RequestParam("currentUserUri") String currentUserUri) {
			return Flux.fromIterable(getDataAsList(locale,multiplyFactor,notificationsToShow,currentUserUri));
		}

		
		
		private Iterable getDataAsList(Locale locale, Integer multiplyFactor, Integer notificationsToShow, String currentUserUri) {

			List<Map<String, Object>> localizedNotificationList = null;
			if (currentUserUri != null) {
				localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
						notificationsToShow);
			}
			JSONSerializer serializer = new JSONSerializer();
			String result = serializer.serialize(localizedNotificationList);
			List<String> list = new ArrayList<>();
			for (int i = 0; i < multiplyFactor; i++) {
				list.add(result);
			}
			return list;
		}

		@RequestMapping(value = "/getNotificationsData", method = RequestMethod.GET,produces="text/plain")
		public @ResponseBody Mono<String> getNotificationsData(Locale locale,@RequestParam("multiplyFactor") Integer multiplyFactor,
				@RequestParam("number") Integer notificationsToShow,
				@RequestParam("currentUserUri") String currentUserUri) {
			return Mono.create((sink)->{
				List<Map<String, Object>> localizedNotificationList = null;
				if (currentUserUri != null) {
					localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
							notificationsToShow);
				}
				JSONSerializer serializer = new JSONSerializer();
				String result=serializer.serialize(localizedNotificationList);
				StringBuilder stringBuilder=new StringBuilder();
				for(int i=0;i<multiplyFactor;i++){
					stringBuilder.append(result);
				}
				sink.success(stringBuilder.toString());
			});
		}
		
		
		@RequestMapping(value = "/getNotificationsJust", method = RequestMethod.GET)
		public @ResponseBody Mono<Object> getNotificationsJust(Locale locale,@RequestParam("multiplyFactor") Integer multiplyFactor,
				@RequestParam("number") Integer notificationsToShow,
				@RequestParam("currentUserUri") String currentUserUri) {
			return Mono.just(getData(locale,multiplyFactor,notificationsToShow,currentUserUri));
		}


		@RequestMapping(value = "/getNotificationsJustNew", method = RequestMethod.GET)
		public @ResponseBody Mono<Object> getNotificationsJustNew(Locale locale,@RequestParam("multiplyFactor") Integer multiplyFactor,
				@RequestParam("number") Integer notificationsToShow,
				@RequestParam("currentUserUri") String currentUserUri) {
			return Mono.just(getDataLowProcessing(locale,multiplyFactor,notificationsToShow,currentUserUri));
		}

		

	private Object getDataLowProcessing(Locale locale, Integer multiplyFactor, Integer notificationsToShow,
				String currentUserUri) {
			return null;
		}

	private Object getData(Locale locale, Integer multiplyFactor, Integer notificationsToShow, String currentUserUri) {

		List<Map<String, Object>> localizedNotificationList = null;
		if (currentUserUri != null) {
			localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
					notificationsToShow);
		}
		JSONSerializer serializer = new JSONSerializer();
		String result = serializer.serialize(localizedNotificationList);
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < multiplyFactor; i++) {
			stringBuilder.append(result);
		}
		return stringBuilder.toString()+"WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";WEB-INF/jsp/activity/userActivity.jsp\r\n" + 
				"WEB-INF/jsp/activity/userActivityAccordion.jsp\r\n" + 
				"WEB-INF/tiles-core.xml\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#com/nucleus/web/comment/UserActivityController.class\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_ar_SA.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_IN.properties\r\n" + 
				"neutrino-core-web-2.00.11.00-GA.jar#resource-bundles/JS_Messages/JS_Messages_en_US.properties\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivity.js\r\n" + 
				"static-resources/neutrino/neutrino-common/js/activity/userActivityBase.js\r\n" + 
				";";
	}

	@RequestMapping(value = "/getNotificationsSync", method = RequestMethod.GET)
	public @ResponseBody Mono<Object> getNotificationsSync(Locale locale,
			@RequestParam("number") Integer notificationsToShow,
			@RequestParam("currentUserUri") String currentUserUri) {
		return Mono.create((sink) -> {
			List<Map<String, Object>> localizedNotificationList = null;
			if (currentUserUri != null) {
				localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
						notificationsToShow);
			}
			JSONSerializer serializer = new JSONSerializer();
			sink.success(serializer.serialize(localizedNotificationList));
		});
	}

	
	@RequestMapping(value = "/getRemoteReport", method = RequestMethod.GET)
	public @ResponseBody Mono<String> getRemoteReport() {
/*		WebClient client = WebClient.create("http://10.1.60.165:19084/neutrino-rules-webapp/index.html");
		client.method(HttpMethod.GET);
		return client.get().retrieve().bodyToMono(String.class);
*/		
		
		WebClient client=WebClient.builder().clientConnector(httpConnector).baseUrl("http://10.1.60.165:8090/")
				.build();
/*		WebClient client = WebClient.create("http://10.1.60.165:19084/neutrino-rules-webapp/index.html");
		client.
*/		
//		client.method(HttpMethod.GET).uri("http://10.1.60.165:8090/test/");
		return client.get().uri("/test/").retrieve().bodyToMono(String.class);
		//return client.get().retrieve().bodyToMono(String.class);

	}

	
	
	@RequestMapping(value = "/fileDownload", method = RequestMethod.GET)
	public Flux<String> fileDownload(HttpServletRequest request, HttpServletResponse response) {
		
		return fromPath(Paths.get(pathToRead));
	}
	
	private static Flux<String> fromPath(Path path) {
		return Flux.using(() -> Files.lines(path),
				Flux::fromStream,
				BaseStream::close
		);
	}

	public UserInfo getUserDetails() {
		UserInfo userInfo = null;
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null && securityContext.getAuthentication() != null) {
			Object principal = securityContext.getAuthentication().getPrincipal();
			if (UserInfo.class.isAssignableFrom(principal.getClass())) {
				userInfo = (UserInfo) principal;
			}
		}
		return userInfo;
	}

	private String getCurrentUserUri() {
		if (getUserDetails() != null && getUserDetails().getUserEntityId() != null) {
			return getUserDetails().getUserEntityId().getUri();
		} else
			return null;
	}

}
