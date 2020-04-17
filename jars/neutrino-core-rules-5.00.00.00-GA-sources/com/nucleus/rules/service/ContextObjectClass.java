package com.nucleus.rules.service;

import java.util.HashMap;
import java.util.Map;

public class ContextObjectClass {
    private static final Map<String, String> ognlWthClassName = new HashMap<String, String>();

    static {
        ognlWthClassName.put("contextObjectLoanApplication", "com.nucleus.core.loan.LoanApplication");
        ognlWthClassName
                .put("contextObjectInternetChannelForm", "com.nucleus.cas.internetchannel.beans.InternetChannelForm");
        ognlWthClassName.put("contextObjectLoanProduct", "com.nucleus.core.loanproduct.LoanProduct");
        ognlWthClassName.put("contextObjectLoanScheme", "com.nucleus.core.loanproduct.LoanScheme");
        ognlWthClassName.put("contextObjectInternetChannelProposal",
                "com.nucleus.web.internetchannel.InternetChannelProposal");
        ognlWthClassName.put("contextObjectParty", "com.nucleus.core.party.Party");
        ognlWthClassName.put("contextObjectSubLoan", "com.nucleus.core.loan.SubLoan");
        ognlWthClassName.put("contextObjectOccupationInfo", "com.nucleus.core.occupation.entity.OccupationInfo");
        ognlWthClassName.put("contextObjectCollateral", "com.nucleus.core.collateral.Collateral");
        ognlWthClassName.put("contextObjectCreditCardType", "com.nucleus.core.loan.creditcard.CreditCardType");
        ognlWthClassName.put("contextObjectProcessingStageName",
                "com.nucleus.core.workflowconfig.entity.ProcessingStageType");
        ognlWthClassName.put("contextObjectPriority",
                "com.nucleus.core.loan.creditcard.PriorityType");
        ognlWthClassName.put("contextObjectShowroom",
                "com.nucleus.core.businesspartner.entity.Showroom");
        
        ognlWthClassName.put("contextObjectSalesOfficer",
                "com.nucleus.core.dsaDetail.DirectSellingAgencyDetails");
        ognlWthClassName.put("contextObjectDealer",
                "com.nucleus.core.businesspartner.entity.Dealer");
        ognlWthClassName.put("contextObjectDSE",
                "com.nucleus.core.businesspartner.entity.DirectSalesExecutive");
        ognlWthClassName.put("contextObjectDSA",
                "com.nucleus.core.businesspartner.entity.DirectSellingAgency");
        ognlWthClassName.put("contextObjectDST",
                "com.nucleus.cas.salesTeamHierarchy.entity.CasSalesTeamHierarchy");
        ognlWthClassName.put("contextObjectReportingSupervisorName",
                "com.nucleus.user.User");
        ognlWthClassName.put("contextObjectAlternateChannelMode",
                "com.nucleus.core.loan.AlternateChannelMode");
        ognlWthClassName.put("contextObjectSourcingChannel",
                "com.nucleus.core.genericparameter.entity.Channel");
        ognlWthClassName.put("contextObjectCRE",
                "com.nucleus.cas.salesTeamHierarchy.entity.CasCreMapping");
        ognlWthClassName.put("contextObjectProductType",
                "com.nucleus.core.loanproduct.ProductType");
        ognlWthClassName.put("contextObjectLoanProduct",
                "com.nucleus.core.loanproduct.LoanProduct");
        ognlWthClassName.put("contextObjectScheme",
                "com.nucleus.core.loanproduct.LoanScheme");
        ognlWthClassName.put("contextObjectInternalChannel",
                "com.nucleus.core.loan.creditcard.ChannelType");
        
        ognlWthClassName.put("contextObjectFieldInvestigationEntry", "com.nucleus.core.loan.fi.FieldInvestigationEntry");
        ognlWthClassName.put("contextObjectCountry", "com.nucleus.address.Country");
        ognlWthClassName.put("contextObjectUserOrgBranchMapping", "com.nucleus.businessmapping.entity.UserOrgBranchMapping");
        ognlWthClassName.put("contextObjectAgency", "com.nucleus.core.businesspartner.entity.Agency");
        ognlWthClassName.put("contextObjectCreditCardDetail", "com.nucleus.core.loan.creditcard.CreditCardDetail");
        ognlWthClassName.put("contextObjectBankDetail", "com.nucleus.person.entity.BankDetail");
        ognlWthClassName.put("contextObjectExternalBank", "com.nucleus.core.bank.entity.ExternalBank");
        ognlWthClassName.put("contextObjectCreditPolicyAction", "com.nucleus.creditPolicy.entity.CreditPolicyAction");
        ognlWthClassName.put("contextObjectAddress", "com.nucleus.address.Address");
        ognlWthClassName.put("contextObjectLeadApplicant", "com.nucleus.core.loan.LeadApplicant");
        ognlWthClassName.put("contextObjectFinancialPerformanceDetail", "com.nucleus.core.loan.FinancialPerformanceDetail");
        ognlWthClassName.put("contextObjectLoanBranch", "com.nucleus.core.organization.entity.OrganizationBranch");
        ognlWthClassName.put("contextObjectSourceChannel", "com.nucleus.core.genericparameter.entity.Channel");
        ognlWthClassName.put("contextObjectNearestBranch", "com.nucleus.core.organization.entity.OrganizationBranch");
        ognlWthClassName.put("contextObjectUser", "com.nucleus.user.User");
        ognlWthClassName.put("contextObjectPricingMatrix_DisbursalType", "com.nucleus.core.disbursal.model.DisbursalType");
        ognlWthClassName.put("contextObjectPricingMatrix_RepaymentFrequency",
                "com.nucleus.core.payment.entity.RepayPolicyRepaymentFrequency");
        ognlWthClassName.put("contextObjectPricingMatrix_InstallmentType",
                "com.nucleus.cas.repayment.entity.RepayPolicyInstallmentType");
        ognlWthClassName.put("contextObjectPricingMatrix_RepaymentScheduleBasedOn",
                "com.nucleus.cas.repayment.entity.RepayScheduleBasedOnMethod");
        ognlWthClassName.put("contextObjectPricingMatrix_InstallmentMode",
                "com.nucleus.cas.repayment.entity.RepayPolicyInstallmentMode");
        ognlWthClassName.put("contextObjectPricingMatrix_AnchorType", "com.nucleus.finnone.pro.lmsbase.domainobject.Anchor");
        ognlWthClassName.put("contextObjectPricingMatrix_InterestChargeMethod",
                "com.nucleus.cas.repayment.entity.RepayPolicyInterestChargeMode");
        ognlWthClassName.put("contextObjectPricingMatrix_RateType", "com.nucleus.cas.repayment.entity.RepayPolicyRateType");
        ognlWthClassName.put("contextObjectSourcingRM", "com.nucleus.officer.Officer");
        ognlWthClassName.put("contextObjectCCSourcingChannelNew", "com.nucleus.core.loan.creditcard.CCSourcingChannel");
        ognlWthClassName.put("contextObjectOccupationFinancialPerformanceDetail", "com.nucleus.core.loan.OccupationsFinancialPerformanceDetail");
        ognlWthClassName.put("contextObjectLastTeam", "com.nucleus.core.team.entity.Team");
        

    }

    public static String getClassName(String ognl) {
        if (ognlWthClassName.containsKey(ognl)) {
            return ognlWthClassName.get(ognl);
        }
        return null;
    }
    
    public static void populate(String ognl, String className) {
    	ognlWthClassName.put(ognl, className);
    }
}
