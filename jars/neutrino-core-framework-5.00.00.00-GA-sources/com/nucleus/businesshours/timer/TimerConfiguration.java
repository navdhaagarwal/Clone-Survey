/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.businesshours.timer;

import java.util.List;

/**
 * A class to represent timer configuration settings to display TAT effectively on UI.
 * @author Nucleus Software Exports Limited
 * 
 */
public class TimerConfiguration {

    private long       taskTotalTime;
    private long       taskRemainingTime;
    private String     taskDueDate;
    private boolean    running;
    private List<Long> playPausePoints;

    private String[]   lifeCycleColorCodes;

    private int[]      lifeCyclePhasePercentages;

    public TimerConfiguration() {
    }

    public List<Long> getPlayPausePoints() {
        return playPausePoints;
    }

    public void setPlayPausePoints(List<Long> playPausePoints) {
        this.playPausePoints = playPausePoints;
    }

    public long getTaskTotalTime() {
        return taskTotalTime;
    }

    public void setTaskTotalTime(long taskTotalTime) {
        this.taskTotalTime = taskTotalTime;
    }

    public long getTaskRemainingTime() {
        return taskRemainingTime;
    }

    public void setTaskRemainingTime(long taskRemainingTime) {
        this.taskRemainingTime = taskRemainingTime;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public boolean getRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String[] getLifeCycleColorCodes() {
        return lifeCycleColorCodes;
    }

    /**
        * An an array containing HTML color codes for different colors used in life-cycle of timer.
        * Colors will be used index wise for different life cycle phases of timer as following
        * 0-->Normal Duration first phase eg-green.
        * 1-->Warning Duration second phase eg-yellow.
        * 2-->Danger Duration(Last Phase of countdown timer.) third phase eg-pink.
        * 3-->Escalated Duration(Timer start running in count up mode) last phase eg-dark red.
        * 4-->This is a special color used for gray shading during timer pause.use any color eg gray.
        * by default lifeCycleColorCodes will use["#D6E9C6","#FBEED5","#EED3D7","#DD514C","#fffff"];
        * <b>lifeCyclePhasePercentages<b> represents percentage of time which will be used for each phase.
     *
     * @param lifeCycleColorCodes
     */
    public void setLifeCycleColorCodes(String[] lifeCycleColorCodes) {
        this.lifeCycleColorCodes = lifeCycleColorCodes;
    }

    public int[] getLifeCyclePhasePercentages() {
        return lifeCyclePhasePercentages;
    }

    /**
     * <b>lifeCyclePhasePercentages<b> represents percentage of time which will be used for each phase.
     * for example 50% for normal,30% for warning,remaining 20% for third phase.So use an array [50,30,20]
     * @param lifeCyclePhasePercentages
     */
    public void setLifeCyclePhasePercentages(int[] lifeCyclePhasePercentages) {
        this.lifeCyclePhasePercentages = lifeCyclePhasePercentages;
    }

    public static class ConfigurationBuilder {
        private long       taskTotalTime;
        private long       taskRemainingTime;
        private String     taskDueDate;
        private boolean    running;
        private List<Long> playPausePoints;
        private String[]   lifeCycleColorCodes;
        private int[]      lifeCyclePhasePercentages;

        public ConfigurationBuilder taskTotalTime(long taskTotalTime) {
            this.taskTotalTime = taskTotalTime;
            return this;
        }

        public ConfigurationBuilder taskRemainingTime(long taskRemainingTime) {
            this.taskRemainingTime = taskRemainingTime;
            return this;
        }

        public ConfigurationBuilder taskDueDate(String taskDueDate) {
            this.taskDueDate = taskDueDate;
            return this;
        }

        public ConfigurationBuilder running(boolean running) {
            this.running = running;
            return this;
        }

        public ConfigurationBuilder playPausePoints(List<Long> playPausePoints) {
            this.playPausePoints = playPausePoints;
            return this;
        }

        public ConfigurationBuilder lifeCycleColorCodes(String[] lifeCycleColorCodes) {
            this.lifeCycleColorCodes = lifeCycleColorCodes;
            return this;
        }

        public ConfigurationBuilder lifeCyclePhasePercentages(int[] lifeCyclePhasePercentages) {
            this.lifeCyclePhasePercentages = lifeCyclePhasePercentages;
            return this;
        }

        public TimerConfiguration build() {
            return new TimerConfiguration(this);
        }
    }

    private TimerConfiguration(ConfigurationBuilder builder) {
        this.taskTotalTime = builder.taskTotalTime;
        this.taskRemainingTime = builder.taskRemainingTime;
        this.taskDueDate = builder.taskDueDate;
        this.running = builder.running;
        this.playPausePoints = builder.playPausePoints;
        this.lifeCycleColorCodes = builder.lifeCycleColorCodes;
        this.lifeCyclePhasePercentages = builder.lifeCyclePhasePercentages;
    }
}
