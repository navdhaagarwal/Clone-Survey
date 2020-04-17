package com.nucleus.person.service;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import com.nucleus.person.entity.ApplicantCreditCardDetail;


@JsonAutoDetect
public class ApplicantCreditCardDetailsVO {

	private List<ApplicantCreditCardDetail> applicantCreditCardDetail;

	public List<ApplicantCreditCardDetail> getApplicantCreditCardDetail() {
		return applicantCreditCardDetail;
	}

	public void setApplicantCreditCardDetail(
			List<ApplicantCreditCardDetail> applicantCreditCardDetail) {
		this.applicantCreditCardDetail = applicantCreditCardDetail;
	}

}
