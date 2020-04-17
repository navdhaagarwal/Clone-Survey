package com.nucleus.user;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
/**
 * This entity is used to hold the question and its answer 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class UserSecurityQuestionAnswer extends BaseEntity {
	
	private static final long serialVersionUID = 4720315033676581827L;
	
	@ManyToOne
	private UserSecurityQuestion question;
	
	private String answer;

	public UserSecurityQuestion getQuestion() {
		return question;
	}

	public void setQuestion(UserSecurityQuestion question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public void loadLazyFields() {
		super.loadLazyFields();
		if(getQuestion()!=null)
		{
			getQuestion().loadLazyFields();
		}
	}
	
	

}
