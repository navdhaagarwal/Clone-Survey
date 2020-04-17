package com.nucleus.web.feedback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nucleus.core.feedback.entity.Feedback;
import com.nucleus.core.feedback.entity.FeedbackType;
import com.nucleus.core.feedback.service.FeedbackService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/Feedback")
public class FeedbackController extends BaseController {

	@Inject
	@Named("feedbackService")
	private FeedbackService feedbackService;
	
	@Inject
	@Named("userService")
	private UserService userService;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
	
	@PreAuthorize("hasAuthority('MAKER_FEEDBACK')")
	@RequestMapping(value = "/openFeedBackModal")
	public String createFeedback(ModelMap map) {
		UserInfo loggedUserInfo = getUserDetails();
		
		map.put("eMail", (userService.findUserByUsername(loggedUserInfo.getUsername())).getMailId());
		map.put("feedbackTypes", genericParameterService.retrieveTypes(FeedbackType.class));
		map.put("loggedUserInfo", loggedUserInfo);
		map.put("feedback", new Feedback());
		return "feedbackForm";
	}
	
	@PreAuthorize("hasAuthority('MAKER_FEEDBACK') or hasAuthority('VIEW_FEEDBACK') or hasAuthority('CHECKER_FEEDBACK')")
	@RequestMapping(value = "/openFeedBackModal/{id}")
	public String viewFeedback(ModelMap map,@PathVariable Long id) {
		map.put("feedbackTypes", genericParameterService.retrieveTypes(FeedbackType.class));
		Feedback fb = feedbackService.getfeedbackbyId(id);
		if(fb != null) {
			HibernateUtils.initializeAndUnproxy(fb.getFeedbackType());
		}
		map.put("feedback", fb);
		return "feedbackFormView";
	}
	
	@PreAuthorize("hasAuthority('MAKER_FEEDBACK')")
	@RequestMapping(value = "/saveFeedback")
	public String saveFeedback(Feedback feedback) {
		feedbackService.saveFeedback(feedback);
		return "feedbackForm";
	}
	
	@PreAuthorize("hasAuthority('MAKER_FEEDBACK') or hasAuthority('VIEW_FEEDBACK') or hasAuthority('CHECKER_FEEDBACK')")
	@RequestMapping(value = "/getFeedbackFromType/{id}")
	public String getFeedbackFromType(@PathVariable Long id, ModelMap map) {
		FeedbackType feedbackType = genericParameterService.findById(id, FeedbackType.class);
		map.put("feedbackType", genericParameterService.retrieveTypes(FeedbackType.class));
		List<Feedback> feedbackList = feedbackService.getFeebacksByType(feedbackType);
		if (ValidatorUtils.hasElements(feedbackList)) {
			for (Feedback fb : feedbackList) {
				HibernateUtils.initializeAndUnproxy(fb.getFeedbackType());
			}
		}
		map.put("feedbackList", feedbackList);
		return "feedback/feedbackTypeGrid";
	}
	@PreAuthorize("hasAuthority('MAKER_FEEDBACK') or hasAuthority('CHECKER_FEEDBACK') or hasAuthority('VIEW_FEEDBACK')")
	@RequestMapping(value = "/openFeedBackPage")
	public String open(ModelMap map) {
		map.put("feedbackType", genericParameterService.retrieveTypes(FeedbackType.class));
		List<Feedback> feedbackList = feedbackService.getAllFeedBacks();
		if (ValidatorUtils.hasElements(feedbackList)) {
			for (Feedback fb : feedbackList) {
				HibernateUtils.initializeAndUnproxy(fb.getFeedbackType());
			}
		}
		map.put("feedbackList", feedbackList);
		return "feedbackGrid";
	}
}
