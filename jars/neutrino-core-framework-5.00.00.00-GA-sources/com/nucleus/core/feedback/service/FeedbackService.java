package com.nucleus.core.feedback.service;

import java.util.List;

import com.nucleus.core.feedback.entity.Feedback;
import com.nucleus.core.feedback.entity.FeedbackType;
import com.nucleus.service.BaseService;

public interface FeedbackService extends BaseService {

   public Feedback getfeedbackbyId(Long id);
    
   public Feedback saveFeedback(Feedback feedback);
    
   public Feedback updateFeedback(Feedback feedback);
    
   public List<Feedback> getFeebacksByType(FeedbackType feedbackType);
    
   public void deleteFeedback(Long id);

   public List<Feedback> getAllFeedBacks(); 
}
