package com.nucleus.core.feedback.service;

import java.util.List;

import javax.inject.Named;

import com.nucleus.core.feedback.entity.Feedback;
import com.nucleus.core.feedback.entity.FeedbackType;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.service.BaseServiceImpl;

@Named(value = "feedbackService")
public class FeedbackServiceImpl extends BaseServiceImpl implements FeedbackService {

    @Override
    public Feedback getfeedbackbyId(Long id) {
        return entityDao.find(Feedback.class, id);
    }

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        entityDao.persist(feedback);
        return feedback;
    }

    @Override
    public Feedback updateFeedback(Feedback feedback) {
        return entityDao.update(feedback);
    }

	@Override
	public List<Feedback> getFeebacksByType(FeedbackType feedbackType) {
		NamedQueryExecutor<Feedback> feedbacks = new NamedQueryExecutor<Feedback>(
				"feedback.findFeebacksByType").addParameter("feedbackType",
				feedbackType);
		List<Feedback> feedbackList = entityDao.executeQuery(feedbacks);
		if (feedbackList.size() > 0)
			return feedbackList;
		else
			return null;
    }

    @Override
    public void deleteFeedback(Long id) {
    }

	@Override
	public List<Feedback> getAllFeedBacks() {
		List<Feedback> feedbackList = entityDao.findAll(Feedback.class);
		return feedbackList;
	}

}
