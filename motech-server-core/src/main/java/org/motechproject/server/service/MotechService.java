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

package org.motechproject.server.service;

import org.motechproject.server.messaging.MessageDefDate;
import org.motechproject.server.model.*;
import org.motechproject.server.model.ghana.Community;
import org.motechproject.server.model.ghana.Facility;
import org.motechproject.server.svc.OpenmrsBean;
import org.motechproject.server.svc.RCTService;
import org.motechproject.server.svc.RegistrarBean;
import org.motechproject.ws.Gender;
import org.openmrs.*;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * A service interface for much of 'real work' for the motech server OpenMRS
 * module.
 */
public interface MotechService extends OpenmrsService {

    RegistrarBean getRegistrarBean();

    RCTService getRctService();

    OpenmrsBean getOpenmrsBean();

    StaffMessageService getStaffMessageService();

    ScheduleMaintService getScheduleMaintService();

    @Transactional
    ScheduledMessage saveScheduledMessage(ScheduledMessage scheduledMessage);

    @Transactional
    Message saveMessage(Message message);

    @Transactional
    MessageDefinition saveMessageDefinition(MessageDefinition messageDefinition);

    @Transactional
    MessageAttribute saveMessageAttribute(MessageAttribute messageAttribute);

    @Transactional(readOnly = true)
    List<ScheduledMessage> getAllScheduledMessages();

    @Transactional(readOnly = true)
    List<ScheduledMessage> getScheduledMessages(Date startDate, Date endDate);

    @Transactional(readOnly = true)
    public List<ScheduledMessage> getScheduledMessages(Integer recipientId,
                                                       MessageDefinition definition, MessageProgramEnrollment enrollment,
                                                       Date messageDate);

    @Transactional(readOnly = true)
    List<Message> getAllMessages();

    @Transactional(readOnly = true)
    List<Message> getMessages(ScheduledMessage scheduledMessage);

    @Transactional(readOnly = true)
    List<Message> getMessages(Date startDate, Date endDate, MessageStatus status);

    @Transactional(readOnly = true)
    List<Message> getMessages(MessageProgramEnrollment enrollment,
                              MessageStatus status);

    @Transactional(readOnly = true)
    List<Message> getMessages(Integer recipientId,
                              MessageProgramEnrollment enrollment, MessageDefinition definition,
                              Date messageDate, MessageStatus status);

    @Transactional(readOnly = true)
    List<Message> getMessages(Integer recipientId,
                              MessageProgramEnrollment enrollment,
                              MessageDefDate[] messageDefDates, MessageStatus status);

    @Transactional(readOnly = true)
    Message getMessage(String publicId);

    @Transactional(readOnly = true)
    List<MessageDefinition> getAllMessageDefinitions();

    @Transactional(readOnly = true)
    MessageDefinition getMessageDefinition(String messageKey);

    @Transactional(readOnly = true)
    List<MessageAttribute> getAllMessageAttributes();

    @Transactional(readOnly = true)
    List<Integer> getUserIdsByPersonAttribute(
            PersonAttributeType personAttributeType, String value);

    @Transactional(readOnly = true)
    List<MessageProgramEnrollment> getActiveMessageProgramEnrollments(
            Integer personId, String program, Integer obsId,
            Long minExclusiveId, Integer maxResults);

    @Transactional
    MessageProgramEnrollment saveMessageProgramEnrollment(
            MessageProgramEnrollment enrollment);

    @Transactional(readOnly = true)
    Blackout getBlackoutSettings();

    @Transactional
    void setBlackoutSettings(Blackout blackout);

    @Transactional(readOnly = true)
    TroubledPhone getTroubledPhone(String phoneNumber);

    @Transactional
    void saveTroubledPhone(TroubledPhone troubledPhone);

    @Transactional
    void addTroubledPhone(String phoneNumber);

    @Transactional
    void removeTroubledPhone(String phoneNumber);

    @Transactional
    GeneralOutpatientEncounter saveGeneralOutpatientEncounter(
            GeneralOutpatientEncounter encounter);

    @Transactional(readOnly = true)
    List<String> getAllCountries();

    @Transactional(readOnly = true)
    List<String> getAllRegions();

    @Transactional(readOnly = true)
    List<String> getRegions(String country);

    @Transactional(readOnly = true)
    List<String> getAllDistricts();

    @Transactional(readOnly = true)
    List<String> getDistricts(String country, String region);

    @Transactional(readOnly = true)
    List<Obs> getActivePregnancies(Integer patientId, Concept pregnancyConcept,
                                   Concept pregnancyStatusConcept);

    @Transactional(readOnly = true)
    List<Obs> getActivePregnanciesDueDateObs(Facility facility,
                                             Date fromDueDate, Date toDueDate, Concept pregnancyDueDateConcept,
                                             Concept pregnancyConcept, Concept pregnancyStatusConcept,
                                             Integer maxResults);

    @Transactional(readOnly = true)
    List<Encounter> getEncounters(Facility facility,
                                  EncounterType encounterType, Date fromDate, Date toDate,
                                  Integer maxResults);

    @Transactional
    ExpectedObs saveExpectedObs(ExpectedObs expectedObs);

    @Transactional(readOnly = true)
    List<ExpectedObs> getExpectedObs(Patient patient, Facility facility,
                                     String[] groups, Date minDueDate, Date maxDueDate,
                                     Date maxLateDate, Date minMaxDate, Integer maxResults);

    @Transactional
    ExpectedEncounter saveExpectedEncounter(ExpectedEncounter expectedEncounter);

    @Transactional
    Facility saveFacility(Facility facility);

    @Transactional(readOnly = true)
    List<ExpectedEncounter> getExpectedEncounter(Patient patient,
                                                 Facility facility, String[] groups, Date minDueDate,
                                                 Date maxDueDate, Date maxLateDate, Date minMaxDate,
                                                 Integer maxResults);

    @Transactional(readOnly = true)
    List<Patient> getPatients(String firstName, String lastName,
                              String preferredName, Date birthDate, Integer facilityId,
                              String phoneNumber, PersonAttributeType phoneNumberAttrType,
                              String nhisNumber, PersonAttributeType nhisAttrType,
                              Integer communityId,
                              String patientId, PatientIdentifierType patientIdType,
                              Integer maxResults);

    @Transactional(readOnly = true)
    List<Patient> getDuplicatePatients(String firstName, String lastName,
                                       String preferredName, Date birthDate, Integer facilityId,
                                       String phoneNumber, PersonAttributeType phoneNumberAttrType,
                                       String nhisNumber, PersonAttributeType nhisAttrType,
                                       String patientId, PatientIdentifierType patientIdType,
                                       Integer maxResults);

    @Transactional(readOnly = true)
    Facility getFacilityById(Integer facilityId);

    @Transactional(readOnly = true)
    List<Facility> getAllFacilities();

    @Transactional(readOnly = true)
    Community getCommunityById(Integer communityId);

    @Transactional(readOnly = true)
    List<Community> getAllCommunities(boolean includeRetired);

    @Transactional(readOnly = true)
    List<Community> getCommunities(String country, String region,
                                   String district, boolean includeRetired);

    @Transactional(readOnly = true)
    Community getCommunityByPatient(Patient patient);

    @Transactional
    Community saveCommunity(Community community);

    @Transactional(readOnly = true)
    Community getCommunityByFacilityIdAndName(Integer facilityId, String name);

    @Transactional(readOnly = true)
    List<Patient> getAllDuplicatePatients();

    @Transactional(readOnly = true)
    Location getLocationByName(String name);

    @Transactional
    void deletePatientIdentifier(Integer patientId);

    @Transactional
    void stopEnrollmentFor(Integer patientId);

    @Transactional
    Facility facilityFor(Patient patient);

    @Transactional(readOnly = true)
    List<MessageLanguage> getAllLanguages();

    @Transactional(readOnly = true)
    MotechConfiguration getConfigurationFor(String name);

    @Transactional(readOnly = true)
    Facility unknownFacility();

    @Transactional(readOnly = true)
    DefaultedExpectedEncounterAlert getDefaultedEncounterAlertFor(ExpectedEncounter expectedEncounter);

    @Transactional(readOnly = true)
    DefaultedExpectedObsAlert getDefaultedObsAlertFor(ExpectedObs expectedObs);

    @Transactional(readOnly = true)
    CareConfiguration getCareConfigurationFor(String careName);

    @Transactional
    void saveOrUpdateDefaultedEncounterAlert(DefaultedExpectedEncounterAlert encounterAlert);

    @Transactional
    void saveOrUpdateDefaultedObsAlert(DefaultedExpectedObsAlert obsAlert);

    @Transactional
    List<GeneralOutpatientEncounter> getOutPatientVisitEntryBy(Integer facilityId, String serialNumber, Gender sex, Date dob, Boolean newCase, Integer diagnosis);
}
