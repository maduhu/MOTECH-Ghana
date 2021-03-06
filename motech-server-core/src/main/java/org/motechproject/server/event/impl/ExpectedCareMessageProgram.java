/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2010-11 The Trustees of Columbia University in the City of
 * New York and Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, Columbia University, or
 * their respective contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
 * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
 * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.motechproject.server.event.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.server.model.*;
import org.motechproject.server.svc.RegistrarBean;
import org.motechproject.server.time.TimePeriod;
import org.openmrs.Patient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExpectedCareMessageProgram implements MessageProgram {

    private static Log log = LogFactory.getLog(ExpectedCareMessageProgram.class);

    private List<ExpectedCareMessageDetails> careMessageDetails = new ArrayList<ExpectedCareMessageDetails>();
    private RegistrarBean registrarBean;
    private String name;

    public MessageProgramState determineState(
            MessageProgramEnrollment enrollment, Date currentDate) {

        // Calculate date 1 day in future to use for message dates calculated in
        // past
        Date nextDate = calculateDateBasedOnTimePeriodAndTimeValue(currentDate, 1, TimePeriod.day);

        Integer maxPatientReminders = registrarBean
                .getMaxPatientCareReminders();

        // Get patient from enrollment person Id
        Patient patient = registrarBean
                .getPatientById(enrollment.getPersonId());
        if (patient == null) {
            log.debug("Person of enrollment is not a patient: "
                    + enrollment.getPersonId());
            return null;
        }

        // Get all active expected care for patient (obs and encounter)
        List<ExpectedEncounter> expectedEncounters = registrarBean.getExpectedEncounters(patient);
        List<ExpectedObs> expectedObservations = registrarBean.getExpectedObs(patient);
        // Get all messages for enrollment (includes sent and not sent)
        List<ScheduledMessage> scheduledMessages = registrarBean.getScheduledMessages(enrollment);

        // Create predicates for expected care (by group) and scheduled messages
        // (by key)
        ExpectedEncounterPredicate expectedEncounterPredicate = new ExpectedEncounterPredicate();
        ExpectedObsPredicate expectedObsPredicate = new ExpectedObsPredicate();
        ScheduledMessagePredicate scheduledMessagePredicate = new ScheduledMessagePredicate();

        for (ExpectedCareMessageDetails careDetails : careMessageDetails) {

            // Set predicates for care details
            expectedEncounterPredicate.setGroup(careDetails.getName());
            expectedObsPredicate.setGroup(careDetails.getName());
            scheduledMessagePredicate.resetKeys(careDetails.getUpcomingMessageKey(), careDetails.getOverdueMessageKey());

            // Get scheduled messages for care, removing from enrollment list
            List<ScheduledMessage> careScheduledMessages = getScheduledMessages(
                    scheduledMessages, scheduledMessagePredicate);
            scheduledMessages.removeAll(careScheduledMessages);

            // Get expected obs and encounters for care details
            List<ExpectedEncounter> careExpectedEncounters = getExpectedEncounters(
                    expectedEncounters, expectedEncounterPredicate);
            List<ExpectedObs> careExpectedObs = getExpectedObs(
                    expectedObservations, expectedObsPredicate);

            List<ScheduledMessage> verifiedScheduledMessages = new ArrayList<ScheduledMessage>();

            // Schedule messages for expected care (encounters and obs) and add
            // message to verified list
            for (ExpectedEncounter expectedEncounter : careExpectedEncounters) {
                Date dueDate = expectedEncounter.getDueEncounterDatetime();
                Date lateDate = expectedEncounter.getLateEncounterDatetime();
                String care = expectedEncounter.getName();

                // Create new scheduled message or return existing matching
                ScheduledMessage message = scheduleCareMessage(currentDate,
                        dueDate, lateDate, careDetails, care,
                        enrollment, careScheduledMessages, maxPatientReminders, registrarBean);
                if (message != null) {
                    verifiedScheduledMessages.add(message);
                }
            }
            for (ExpectedObs expectedObs : careExpectedObs) {
                Date dueDate = expectedObs.getDueObsDatetime();
                Date lateDate = expectedObs.getLateObsDatetime();
                String care = expectedObs.getName();

                // Create new scheduled message or return existing matching
                ScheduledMessage message = scheduleCareMessage(currentDate,
                        dueDate, lateDate, careDetails, care,
                        enrollment, careScheduledMessages, maxPatientReminders, registrarBean);
                if (message != null) {
                    verifiedScheduledMessages.add(message);
                }
            }

            // Cancel unsent messages for care (not matching the newly
            // scheduled)
            careScheduledMessages.removeAll(verifiedScheduledMessages);
            if (!careScheduledMessages.isEmpty()) {
                registrarBean.removeUnsentMessages(careScheduledMessages);
            }
        }

        // Cancel any unsent messages for enrollment (for unhandled care)
        if (!scheduledMessages.isEmpty()) {
            registrarBean.removeUnsentMessages(scheduledMessages);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected List<ExpectedEncounter> getExpectedEncounters(
            List<ExpectedEncounter> expectedEncounterList,
            ExpectedEncounterPredicate expectedEncounterPredicate) {
        return (List<ExpectedEncounter>) CollectionUtils.select(
                expectedEncounterList, expectedEncounterPredicate);
    }

    @SuppressWarnings("unchecked")
    protected List<ExpectedObs> getExpectedObs(
            List<ExpectedObs> expectedObsList,
            ExpectedObsPredicate expectedObsPredicate) {
        return (List<ExpectedObs>) CollectionUtils.select(expectedObsList,
                expectedObsPredicate);
    }

    @SuppressWarnings("unchecked")
    protected List<ScheduledMessage> getScheduledMessages(
            List<ScheduledMessage> scheduledMessagesList,
            ScheduledMessagePredicate scheduledMessagePredicate) {
        return (List<ScheduledMessage>) CollectionUtils.select(
                scheduledMessagesList, scheduledMessagePredicate);
    }

    protected ScheduledMessage getMatchingScheduledMessage(
            List<ScheduledMessage> scheduledMessagesList,
            ScheduledMessagePredicate scheduledMessagePredicate) {
        return (ScheduledMessage) CollectionUtils.find(scheduledMessagesList,
                scheduledMessagePredicate);
    }

    protected boolean isUpcoming(Date currentDate, Date expectedCareDate,
                                 ExpectedCareMessageDetails careDetails, String care) {
        // Upcoming care is considered twice the value for the care
        Date endDate = calculateDateBasedOnTimePeriodAndTimeValue(currentDate, 2 * careDetails
                .getTimeValue(care), careDetails.getTimePeriod());
        return currentDate.before(expectedCareDate)
                && endDate.after(expectedCareDate);
    }

    protected boolean isOverdue(Date currentDate, Date expectedCareDate) {
        return currentDate.after(expectedCareDate);
    }

    private ScheduledMessage scheduleCareMessage(Date currentDate, Date dueDate, Date lateDate,
                                                 ExpectedCareMessageDetails careDetails, String care,
                                                 MessageProgramEnrollment enrollment,
                                                 List<ScheduledMessage> careScheduledMessages,
                                                 Integer maxPatientReminders, RegistrarBean registrarBean) {

        if (isUpcoming(currentDate, dueDate, careDetails, care)) {
            // Schedule reminder value/period before care due date
            Date messageDate = calculateDateBasedOnTimePeriodAndTimeValue(dueDate, (-1 * careDetails
                    .getTimeValue(care)), careDetails.getTimePeriod());

            // Set predicate and get upcoming scheduled message if exists
            ScheduledMessagePredicate scheduledMessagePredicate = new ScheduledMessagePredicate();
            scheduledMessagePredicate.resetKeys(careDetails
                    .getUpcomingMessageKey());
            scheduledMessagePredicate.setCare(care);
            scheduledMessagePredicate.setDate(messageDate);
            ScheduledMessage upcomingMessage = getMatchingScheduledMessage(
                    careScheduledMessages, scheduledMessagePredicate);

            // Create new scheduled message only if not already exist
            if (upcomingMessage == null) {
                return registrarBean
                        .scheduleCareMessage(careDetails
                                .getUpcomingMessageKey(), enrollment,
                                messageDate, careDetails
                                        .getUserPreferenceBased(), care,
                                currentDate);
            } else {
                // Check if unsent message attempt date needs adjusting for
                // blackout or preference changes
                registrarBean.verifyMessageAttemptDate(upcomingMessage,
                        careDetails.getUserPreferenceBased(), currentDate);
                return upcomingMessage;
            }

        } else if (isOverdue(currentDate, lateDate)) {
            // Schedule reminder value/period after care late date
            Date reminderDate = calculateDateBasedOnTimePeriodAndTimeValue(lateDate, careDetails
                    .getTimeValue(care), careDetails.getTimePeriod());

            // Set predicate and get previous reminder scheduled message if
            // exists
            ScheduledMessagePredicate scheduledMessagePredicate = new ScheduledMessagePredicate();
            scheduledMessagePredicate.resetKeys(careDetails
                    .getOverdueMessageKey());
            scheduledMessagePredicate.setCare(care);
            scheduledMessagePredicate.setDate(reminderDate);
            ScheduledMessage reminderMessage = getMatchingScheduledMessage(
                    careScheduledMessages, scheduledMessagePredicate);

            if (reminderMessage == null) {
                // Schedule reminder if no previous reminders
                return registrarBean
                        .scheduleCareMessage(
                                careDetails.getOverdueMessageKey(), enrollment,
                                reminderDate, careDetails
                                        .getUserPreferenceBased(), care,
                                currentDate);
            } else {
                // Check if unsent message attempt date needs adjusting for
                // blackout or preference changes
                registrarBean.verifyMessageAttemptDate(reminderMessage,
                        careDetails.getUserPreferenceBased(), currentDate);

                // Determine last reminder date
                List<Message> attempts = reminderMessage.getMessageAttempts();
                Date previousReminderDate = null;
                if (!attempts.isEmpty()) {
                    previousReminderDate = attempts.get(0).getAttemptDate();
                } else {
                    previousReminderDate = lateDate;
                }

                // Schedule reminder value/period after most recent reminder
                // if number of previous reminders less than max property
                if (attempts.size() < maxPatientReminders) {
                    Date newReminderDate = calculateDateBasedOnTimePeriodAndTimeValue(previousReminderDate,
                            careDetails.getTimeValue(care), careDetails
                                    .getTimePeriod());
                    Date maxReminderDate = calculateDateBasedOnTimePeriodAndTimeValue(currentDate,
                            careDetails.getTimeValue(care), careDetails
                                    .getTimePeriod());

                    if (!newReminderDate.after(maxReminderDate)) {
                        registrarBean.addMessageAttempt(reminderMessage,
                                newReminderDate, maxReminderDate, careDetails
                                        .getUserPreferenceBased(), currentDate);
                    }
                }
                return reminderMessage;
            }
        }
        return null;
    }

    protected Date calculateDateBasedOnTimePeriodAndTimeValue(Date date, Integer value, TimePeriod period) {
        if (date == null || value == null || period == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (period.equals(TimePeriod.week))
            calendar.add(period.getCalendarPeriod(), value * 7);
        else
            calendar.add(period.getCalendarPeriod(), value);

        return calendar.getTime();
    }

    public MessageProgramState getEndState() {
        return null;
    }

    public MessageProgramState getStartState() {
        return null;
    }

    public List<ExpectedCareMessageDetails> getCareMessageDetails() {
        return careMessageDetails;
    }

    public void setCareMessageDetails(
            List<ExpectedCareMessageDetails> careMessageDetails) {
        this.careMessageDetails = careMessageDetails;
    }


    public Boolean hasMessageCareDetails() {
        return !careMessageDetails.isEmpty();
    }

    public RegistrarBean getRegistrarBean() {
        return registrarBean;
    }

    public void setRegistrarBean(RegistrarBean registrarBean) {
        this.registrarBean = registrarBean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
