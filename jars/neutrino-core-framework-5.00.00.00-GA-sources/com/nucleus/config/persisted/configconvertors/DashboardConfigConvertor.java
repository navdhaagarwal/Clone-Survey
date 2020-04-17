package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class DashboardConfigConvertor extends AbstractConfigConvertor {

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {

        if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.commentWidget")) {
            configurationVO.setCommentWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.streamWidget")) {
            configurationVO.setStreamWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.appCountByProductTypeWidget")) {
            configurationVO.setAppCountByProductTypeWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.appCountByStageWidget")) {
            configurationVO.setAppCountByStageWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.notesWidget")) {
            configurationVO.setNotesWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.leadCountByCityWidget")) {
            configurationVO.setLeadCountByCityWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.leadCountByConversionWidget")) {
            configurationVO.setLeadCountByConversionWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.leadCountByTatWidget")) {
            configurationVO.setLeadCountByTatWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.leadCountByDueTodayWidget")) {
            configurationVO.setLeadCountByDueTodayWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.leadCountByStatusWidget")) {
            configurationVO.setLeadCountByStatusWidget(configuration.getPropertyValue());
        } else if (configuration.getPropertyKey().equalsIgnoreCase("config.dashboard.recentMails")) {
            configurationVO.setRecentMails(configuration.getPropertyValue());
        }

    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {


           if (configurationVO.getCommentWidget()!= null && configurationVO.getCommentWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getCommentWidget();
            } else if (configurationVO.getStreamWidget()!= null && configurationVO.getStreamWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getStreamWidget();
            } else if (configurationVO.getAppCountByProductTypeWidget()!= null && configurationVO.getAppCountByProductTypeWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getAppCountByProductTypeWidget();
            } else if (configurationVO.getAppCountByStageWidget()!= null && configurationVO.getAppCountByStageWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getAppCountByStageWidget();
            } else if (configurationVO.getNotesWidget()!= null && configurationVO.getNotesWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getNotesWidget();
            } else if (configurationVO.getLeadCountByCityWidget()!= null && configurationVO.getLeadCountByCityWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getLeadCountByCityWidget();
            }else if (configurationVO.getLeadCountByConversionWidget()!= null && configurationVO.getLeadCountByConversionWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getLeadCountByConversionWidget();
            }else if (configurationVO.getLeadCountByDueTodayWidget()!= null && configurationVO.getLeadCountByDueTodayWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getLeadCountByDueTodayWidget();
            }else if (configurationVO.getLeadCountByStatusWidget()!= null && configurationVO.getLeadCountByStatusWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getLeadCountByStatusWidget();
            }else if (configurationVO.getRecentMails()!= null && configurationVO.getRecentMails().equalsIgnoreCase("enable")) {
                return configurationVO.getRecentMails();
            }else if (configurationVO.getLeadCountByTatWidget()!= null && configurationVO.getLeadCountByTatWidget().equalsIgnoreCase("enable")) {
                return configurationVO.getLeadCountByTatWidget();
            }
            else {
                return "disable";
            }
        
        
        

    }

}
