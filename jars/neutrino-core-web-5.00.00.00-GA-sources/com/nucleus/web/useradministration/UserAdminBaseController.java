package com.nucleus.web.useradministration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;

import com.nucleus.address.Address;
import com.nucleus.address.Area;
import com.nucleus.address.City;
import com.nucleus.address.Country;
import com.nucleus.address.District;
import com.nucleus.address.IntraCountryRegion;
import com.nucleus.address.State;
import com.nucleus.businessmapping.service.UserBPMappingService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.persistence.EntityDao;
import com.nucleus.person.entity.SalutationType;
import com.nucleus.user.DeviationLevel;
import com.nucleus.user.User;
import com.nucleus.user.UserBranchProductService;
import com.nucleus.user.UserCategory;
import com.nucleus.user.UserClassification;
import com.nucleus.user.UserDepartment;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserModificationAuditVO;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserSecurityQuestionAnswer;
import com.nucleus.user.UserServiceImpl;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.usermgmt.UserManagementForm;

import flexjson.JSONSerializer;

@Controller
public class UserAdminBaseController extends BaseController {

    @Inject
    @Named("entityDao")
    protected EntityDao               entityDao;

    @Inject
    @Named("userService")
    protected UserServiceImpl         userService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore userManagementService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService   genericParameterService;
    
    @Inject
    @Named("userBPMappingService")
    private UserBPMappingService userBPMappingService;
    
    @Inject
    @Named("userBranchProductService")
    UserBranchProductService userBranchProductService;

    protected String populateUserAuditDetails(UserManagementForm userManagementForm) {
        UserModificationAuditVO userAuditVO = new UserModificationAuditVO();
        /*populate Personal Information of user */
        userAuditVO = populatePersonalInformationForAudit(userManagementForm, userAuditVO);
        /*populate Address & Communication of User */
        userAuditVO = populateAddressAndCommunicationDetailsForAudit(userManagementForm, userAuditVO);
        /*populate Role & Mobility Information of User */
        userAuditVO = populateRoleAndMobilityInformationForAudit(userManagementForm, userAuditVO);
        /*populate Branches & Branch Admin & Teams of User */
        userAuditVO = populateBranchesAndProductForAudit(userManagementForm, userAuditVO);
        JSONSerializer iSerializer = new JSONSerializer();

        String userModificationAuditString = iSerializer.deepSerialize(userAuditVO);
        return userModificationAuditString;
    }

    protected UserModificationAuditVO populatePersonalInformationForAudit(UserManagementForm userManagementForm,
            UserModificationAuditVO userAuditVO) {
        if (userManagementForm != null) {
            UserProfile userProfile = null;
            User associatedUser = null;
            if (userManagementForm.getUserprofile() != null) {
                if (userManagementForm.getUserprofile().getId() == null) {
                    userProfile = userManagementForm.getUserprofile();
                } else {
                    userProfile = entityDao.find(UserProfile.class, userManagementForm.getUserprofile().getId());
                }
            }
            if (userProfile != null) {
                userAuditVO.setFirstName(userProfile.getFirstName());
                userAuditVO.setMiddleName(userProfile.getMiddleName());
                userAuditVO.setLastName(userProfile.getLastName());
                userAuditVO.setFourthName(userProfile.getFourthName());
                userAuditVO.setAliasName(userProfile.getAliasName());
                if (userProfile.getSalutation() != null && userProfile.getSalutation().getId() !=null) {
                    SalutationType salutationType=genericParameterService.findById(userProfile.getSalutation().getId(), SalutationType.class);
                    if(salutationType !=null){
                    userAuditVO.setSalutation(salutationType.getCode());
                }
                }
                if (userProfile.getAddressRange() != null) {
                    userAuditVO.setIpAddress(userProfile.getAddressRange().getIpaddress());
                    userAuditVO.setFromIpAddress(userProfile.getAddressRange().getFromIpAddress());
                    userAuditVO.setToIpAddress(userProfile.getAddressRange().getToIpAddress());
                }
            }
            if (userManagementForm.getAssociatedUser() != null) {
                if (userManagementForm.getAssociatedUser().getId() == null) {
                    associatedUser = userManagementForm.getAssociatedUser();
                } else {
                    associatedUser = entityDao.find(User.class, userManagementForm.getAssociatedUser().getId());
                }
            }
            if (associatedUser != null) {
                userAuditVO.setUserName(associatedUser.getUsername());
                userAuditVO.setPassword(associatedUser.getPassword());
                userAuditVO.setPasswordExpiration(associatedUser.getPasswordExpiresInDays());
                userAuditVO.setEmailId(associatedUser.getMailId());
                userAuditVO.setIsBusinessPartner(associatedUser.isBusinessPartner());
                userAuditVO.setIsSupervisor(associatedUser.isSupervisor());
                userAuditVO.setIsSuperAdmin(associatedUser.isSuperAdmin());
                userAuditVO.setIsRelationshipOfficer(associatedUser.isRelationshipOfficer());
                userAuditVO.setPasswordHintQuestion(associatedUser.getPasswordHintQuestion());
                userAuditVO.setPasswordHintAnswer(associatedUser.getPasswordHintAnswer());
                if (associatedUser.getDeviationLevel() != null && associatedUser.getDeviationLevel().getId() != null) {
                    DeviationLevel deviationLevel = entityDao.find(DeviationLevel.class, associatedUser.getDeviationLevel()
                            .getId());
                    if (deviationLevel != null) {
                        userAuditVO.setDeviationLevel(deviationLevel.getName());
                    }
                }
                
                
                if (associatedUser.getUserClassification() != null && associatedUser.getUserClassification().getId() != null) {
                    UserClassification userClassification = entityDao.find(UserClassification.class, associatedUser.getUserClassification()
                            .getId());
                    if (userClassification != null) {
                        userAuditVO.setUserClassification(userClassification.getName());
                    }
                }
                
                if (associatedUser.getUserDepartment() != null && associatedUser.getUserDepartment().getId() != null) {
                	UserDepartment userDepartment = entityDao.find(UserDepartment.class, associatedUser.getUserDepartment()
                            .getId());
                    if (userDepartment != null) {
                        userAuditVO.setUserDepartment(userDepartment.getName());
                    }
                }
                
                if (associatedUser.getUserCategory() != null && associatedUser.getUserCategory().getId() != null) {
                    UserCategory userCategory = entityDao.find(UserCategory.class, associatedUser.getUserCategory()
                            .getId());
                    if (userCategory != null) {
                        userAuditVO.setUserCategory(userCategory.getName());
                    }
                }
                
                
               
                
                
                if (associatedUser.getSanctionedLimit() != null) {
                    userAuditVO.setSanctionedAmount(associatedUser.getSanctionedLimit().getBaseAmount().getValue());
                }
                userAuditVO.setIsTeamLead(associatedUser.getTeamLead());
                if (associatedUser.getSysName() != null && associatedUser.getSysName().getId() != null) {
                    SystemName systemName = genericParameterService.findById(associatedUser.getSysName().getId(),
                            SystemName.class);
                    if (systemName != null) {
                        userAuditVO.setModuleName(systemName.getCode());
                    }
                }
                if (associatedUser.getAccessToAllBranches() != null
                        && associatedUser.getAccessToAllBranches().toString().equalsIgnoreCase("Y")) {
                    userAuditVO.setAccessToAllBranches(associatedUser.getAccessToAllBranches());
                } else {
                    userAuditVO.setAccessToAllBranches('N');
                }
                if (associatedUser.getAccessToAllProducts() != null
                        && associatedUser.getAccessToAllProducts().toString().equalsIgnoreCase("Y")) {
                    userAuditVO.setAccessToAllProducts(associatedUser.getAccessToAllProducts());
                } else {
                    userAuditVO.setAccessToAllProducts('N');
                }
                List<UserSecurityQuestionAnswer> securityQuestionAnswer = associatedUser.getSecurityQuestionAnswers();
                if (CollectionUtils.isNotEmpty(securityQuestionAnswer) && securityQuestionAnswer.size() > 0) {
                    UserSecurityQuestionAnswer firstSecurityQuestionAnswer = securityQuestionAnswer.get(0);
                    if (firstSecurityQuestionAnswer != null) {
                        UserSecurityQuestion firstQuestion = firstSecurityQuestionAnswer.getQuestion();
                        if (firstQuestion != null && firstQuestion.getId() != null) {
                            firstQuestion = genericParameterService.findById(firstQuestion.getId(),
                                    UserSecurityQuestion.class);
                            if (firstQuestion != null) {
                                userAuditVO.setSecurityHintQuestion0(firstQuestion.getCode());
                            }
                        }
                        String firstAnswer = firstSecurityQuestionAnswer.getAnswer();
                        userAuditVO.setSecurityHintAnswer0(firstAnswer);
                    }

                    UserSecurityQuestionAnswer secondSecurityQuestionAnswer = securityQuestionAnswer.get(1);
                    if (secondSecurityQuestionAnswer != null) {
                        UserSecurityQuestion secondQuestion = secondSecurityQuestionAnswer.getQuestion();
                        if (secondQuestion != null && secondQuestion.getId() != null)  {
                            secondQuestion = genericParameterService.findById(secondQuestion.getId(),
                                    UserSecurityQuestion.class);
                            if (secondQuestion != null) {
                            userAuditVO.setSecurityHintQuestion1(secondQuestion.getCode());
                        }
                        }
                        String secondAnswer = firstSecurityQuestionAnswer.getAnswer();
                        userAuditVO.setSecurityHintAnswer1(secondAnswer);
                    }

                }

            }
        }
        return userAuditVO;
    }

    protected UserModificationAuditVO populateBranchesAndProductForAudit(UserManagementForm userManagementForm,
            UserModificationAuditVO userAuditVO) {
        if (userManagementForm != null) {
            if (userManagementForm.getAssociatedUser() != null) {
                List<String> branchCode = userService
                        .getBranchCodeFromUserId(userManagementForm.getAssociatedUser().getId());
                if (CollectionUtils.isNotEmpty(branchCode)) {
                    userAuditVO.setBranchCode(branchCode);
                }
                List<String> productCode = userBranchProductService.getProductCodeFromUserId(userManagementForm.getAssociatedUser()
                        .getId());
                if (CollectionUtils.isNotEmpty(productCode)) {
                    userAuditVO.setProductCode(productCode);
                }
                List<String> teamName = userService.getTeamNameFromUserId(userManagementForm.getAssociatedUser().getId());
                if (CollectionUtils.isNotEmpty(teamName)) {
                    userAuditVO.setTeamNames(teamName);
                }
                userAuditVO.setDefaultBranch(userService.getDefaultBranchCodeFromUserId(userManagementForm
                        .getAssociatedUser().getId()));
                List<String> branchAdminCode = userService.getBranchCodeWhereUserIsBranchAdmin(userManagementForm
                        .getAssociatedUser().getId());
                if (CollectionUtils.isNotEmpty(branchAdminCode)) {
                    userAuditVO.setBranchAdminCode(branchAdminCode);
                }
                userAuditVO.setAssociatedBusinessPartner(userBPMappingService
                        .getBusinessPartnerNameByUserId(userManagementForm.getAssociatedUser().getId()));
            }
        }
        return userAuditVO;
    }

    protected UserModificationAuditVO populateRoleAndMobilityInformationForAudit(UserManagementForm userManagementForm,
            UserModificationAuditVO userAuditVO) {
        List<String> roleName = userService.getRoleNamesFromUserId(userManagementForm.getAssociatedUser().getId());
        if (roleName != null) {
            userAuditVO.setRoleNames(roleName);
        }
        UserMobilityInfo mobilityInfo = null;
        if (userManagementForm.getUserMobilityInfo() != null) {
            if (userManagementForm.getUserMobilityInfo().getId() == null) {
                mobilityInfo = userManagementForm.getUserMobilityInfo();
            } else {
                mobilityInfo = entityDao.find(UserMobilityInfo.class, userManagementForm.getUserMobilityInfo().getId());
            }
            userAuditVO.setMobilityEnabled(mobilityInfo.getIsMobileEnabled());
            userAuditVO.setChallengeEnabled(mobilityInfo.getIsChallengeEnabled());
            userAuditVO.setChallenge(mobilityInfo.getChallenge());
            userAuditVO.setIsDeviceAuthEnabled(mobilityInfo.getIsDeviceAuthEnabled());
            userAuditVO.setRegisteredDeviceList(mobilityInfo.getRegisteredDeviceList());
        }
        return userAuditVO;
    }

    protected UserModificationAuditVO populateAddressAndCommunicationDetailsForAudit(UserManagementForm userManagementForm,
            UserModificationAuditVO userAuditVO) {
        if (userManagementForm != null) {
            UserProfile userProfile = null;
            if (userManagementForm.getUserprofile() != null) {
                if (userManagementForm.getUserprofile().getId() == null) {
                    userProfile = userManagementForm.getUserprofile();
                } else {
                    userProfile = entityDao.find(UserProfile.class, userManagementForm.getUserprofile().getId());
                }
            }
            if (userProfile != null) {
                SimpleContactInfo simpleContactInfo = userProfile.getSimpleContactInfo();
                if (simpleContactInfo != null) {
                    Address address = simpleContactInfo.getAddress();
                    if (address != null) {
                        if (address.getCountry() != null && address.getCountry().getId() != null) {
                            Country country = entityDao.find(Country.class, address.getCountry().getId());
                            if (country != null) {
                                userAuditVO.setCountry(country.getCountryISOCode());
                            }
                        }
                        userAuditVO.setFlatNumber(address.getAddressLine1());
                        userAuditVO.setAddressLine2(address.getAddressLine2());
                        userAuditVO.setAddressLine3(address.getAddressLine3());
                        if (address.getRegion() != null && address.getRegion().getId() != null) {
                            IntraCountryRegion intraCountryRegion = entityDao.find(IntraCountryRegion.class, address
                                    .getRegion().getId());
                            if (intraCountryRegion != null) {
                                userAuditVO.setRegion(intraCountryRegion.getIntraRegionCode());
                            }
                        }
                        if (address.getState() != null && address.getState().getId() != null) {
                            State state = entityDao.find(State.class, address.getState().getId());
                            if (state != null) {
                                userAuditVO.setState(state.getStateCode());
                            }
                        }
                        if (address.getCity() != null && address.getCity().getId() != null) {
                            City city = entityDao.find(City.class, address.getCity().getId());
                            if (city != null) {
                                userAuditVO.setCity(city.getCityCode());
                            }
                        }
                        if (address.getDistrict() != null && address.getDistrict().getId() != null) {
                            District district = entityDao.find(District.class, address.getDistrict().getId());
                            if (district != null) {
                                userAuditVO.setDistrict(district.getDistrictCode());
                            }
                        }
                        if (address.getArea() != null && address.getArea().getId() != null) {
                            Area area = entityDao.find(Area.class, address.getArea().getId());
                            if (area != null) {
                                userAuditVO.setArea(area.getAreaCode());
                            }
                        }
                        userAuditVO.setTaluka(address.getTaluka());
                        userAuditVO.setVillage(address.getVillage());
                        if (address.getZipcode() != null) {
                            userAuditVO.setPincode(address.getZipcode().getId());
                        }
                    }
                    /*communication Details */
                    if (simpleContactInfo.getPhoneNumber() != null) {
                        String stdCode = simpleContactInfo.getPhoneNumber().getStdCode();
                        String phoneNumber = simpleContactInfo.getPhoneNumber().getPhoneNumber();
                        String extensionNumber = simpleContactInfo.getPhoneNumber().getExtension();
                        String phoneNumberLandline = "";
                        if (phoneNumber != null && stdCode != null) {
                            phoneNumberLandline = stdCode.concat(phoneNumber);
                        }
                        if (phoneNumber != null && extensionNumber != null) {
                            phoneNumberLandline = phoneNumberLandline.concat(extensionNumber);
                        }
                        // phoneNumberLandline = stdCode.concat(phoneNumber.concat(extensionNumber));
                        userAuditVO.setPhoneNumber(phoneNumberLandline);
                    }
                    if (simpleContactInfo.getMobileNumber() != null) {
                        userAuditVO.setMobileNumber(simpleContactInfo.getMobileNumber().getPhoneNumber());
                    }
                    if (simpleContactInfo.getEmail() != null) {
                        userAuditVO.setComminicationEmail(simpleContactInfo.getEmail().getEmailAddress());
                    }
                }
            }
        }
        return userAuditVO;
    }
}