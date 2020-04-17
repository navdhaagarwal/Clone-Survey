/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.fileupload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.feedback.entity.Feedback;
import com.nucleus.core.feedback.entity.FeedbackType;
import com.nucleus.core.feedback.service.FeedbackService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.RenderImageUtility;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited TODO -> amit.parashar Add
 *         documentation to class
 */

@Controller
@Transactional
@RequestMapping(value = "upload")
public class FileUploadController extends BaseController{

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("feedbackService")
	private FeedbackService feedbackService;

	@Inject
	@Named("userService")
	private UserService userService;

	/* @ExceptionHandler(Exception.class) */
	public @ResponseBody
	String hadleFileUploadException(MaxUploadSizeExceededException e) {
		BaseLoggers.exceptionLogger.error("Exception in FileUploadController",
				e);
		return "The attaached image size exceeds maximum permissible image size."; // Internationalization
																					// TBD
	}

	private static final String CUSTOMERIMAGESBUCKET = "CUSTOMERIMAGES";
	private static final String CUSTOMERFEEDBACKBUCKET = "CUSTOMERFEEDBACKIMAGES";
	/*
	 * @Inject
	 * 
	 * @Named("mongoDatastoreService") private DatastoreService docService;
	 */

	@Inject
	@Named("couchDataStoreDocumentService")
	private DatastorageService docService2;

	 	@Inject
	    @Named("renderImageUtility")
	    private RenderImageUtility renderImageUtility;

	
	@RequestMapping(value = "/saveimage", method = RequestMethod.POST)
	public @ResponseBody
	String create(UploadItem uploadItem, BindingResult result)
			throws IOException {
		if (result.hasErrors()) {
			for (ObjectError error : result.getAllErrors()) {
				BaseLoggers.webLogger.error("Exception FileUploadController",
						error);
			}
			return "";
		}
		// String imageId =
		// docService.saveDocument(uploadItem.getFileData().getInputStream(),
		// CUSTOMERIMAGESBUCKET);
		String imageId = docService2.saveDocument(uploadItem.getFileData()
				.getInputStream(), uploadItem.getFileData()
				.getOriginalFilename().split("\\.")[0], uploadItem
				.getFileData().getOriginalFilename().split("\\.")[1]);

		return imageId;
	}

	@RequestMapping(value = "/renderimage/{imageId}", method = RequestMethod.GET)
	public void renderImage(@PathVariable("imageId") String imageId,
			HttpServletResponse response) throws IOException {
		renderImageUtility.renderImage(imageId, response);
	}

	@RequestMapping(value = "/saveFeedbackimage", method = RequestMethod.POST)
	public @ResponseBody
	String saveFeedbackimage(@RequestParam("issue") String issue, String imgData)
			throws IOException {

		String comment = issue;

		int firstIndex = comment.indexOf(":\"");
		int secIndex = comment.indexOf("\"", firstIndex) + 1;
		int thirdIndex = comment.lastIndexOf("\"}");

		comment = comment.substring(secIndex, thirdIndex);

		imgData = imgData.replace("\"", "");
		String imgBase64Data = imgData.split(",")[1];    //Getting image base64 value.

		InputStream stream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(imgBase64Data));

		// String imageId = docService.saveDocument(stream,
		// CUSTOMERFEEDBACKBUCKET);
		String imageId = docService2.saveDocument(stream, "attachmentName","PNG");

		Feedback feedback = new Feedback();
		FeedbackType problemFeedbackType = genericParameterService.findByCode(
				FeedbackType.PROBLEM, FeedbackType.class);

		feedback.setFeedbackType(problemFeedbackType);
		feedback.setText(comment);
		feedback.setImageID(imageId);
		UserInfo userInfo = getUserDetails();

		feedback.setEmailAddress(userService.findUserByUsername(
				userInfo.getUsername()).getMailId());
		feedback.setUserName(userInfo.getDisplayName());

		feedbackService.saveFeedback(feedback);

		return imageId;

	}

	@RequestMapping(value = "/renderFeedbackImage/{imageId}", method = RequestMethod.GET)
	public @ResponseBody
	String renderFeedbackImage(@PathVariable("imageId") String imageId,
			HttpServletResponse response) throws IOException {
		
		byte[] imageFile = docService2.retriveDocumentAsByteArray(imageId);
		byte[] imageBase64 = Base64.encodeBase64(imageFile);
                          
        return new String(imageBase64, "UTF-8");
	
	}
}
