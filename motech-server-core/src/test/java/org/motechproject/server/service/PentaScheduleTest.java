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

import junit.framework.TestCase;
import org.easymock.Capture;
import org.motechproject.server.model.ExpectedObs;
import org.motechproject.server.service.impl.ExpectedObsSchedule;
import org.motechproject.server.svc.RegistrarBean;
import org.motechproject.server.util.MotechConstants;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

import static org.easymock.EasyMock.*;

public class PentaScheduleTest extends TestCase {

	ApplicationContext ctx;

	RegistrarBean registrarBean;
	ExpectedObsSchedule pentaSchedule;
	ExpectedCareEvent penta1Event;
	ExpectedCareEvent penta2Event;
	ExpectedCareEvent penta3Event;

	@Override
	protected void setUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext(new String[] {
				"test-common-program-beans.xml",
				"services/child-penta-service.xml" });
		pentaSchedule = (ExpectedObsSchedule) ctx.getBean("childPentaSchedule");
		penta1Event = pentaSchedule.getEvents().get(0);
		penta2Event = pentaSchedule.getEvents().get(1);
		penta3Event = pentaSchedule.getEvents().get(2);

		// EasyMock setup in Spring config
		registrarBean = (RegistrarBean) ctx.getBean("registrarBean");
	}

	@Override
	protected void tearDown() throws Exception {
		ctx = null;
		pentaSchedule = null;
		penta1Event = null;
		penta2Event = null;
		penta3Event = null;
		registrarBean = null;
	}

	public void testCreateExpected() {
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -2); // age is 2 months

		Patient patient = new Patient();
		patient.setBirthdate(calendar.getTime());
        patient.setDateCreated(calendar.getTime());

		Capture<Date> minDateCapture = new Capture<Date>();
		Capture<Date> dueDateCapture = new Capture<Date>();
		Capture<Date> lateDateCapture = new Capture<Date>();
		Capture<Date> maxDateCapture = new Capture<Date>();

		List<Obs> obsList = new ArrayList<Obs>();
		List<ExpectedObs> expectedObsList = new ArrayList<ExpectedObs>();

		expect(
				registrarBean.getObs(patient, pentaSchedule.getConceptName(),
						pentaSchedule.getValueConceptName(), patient
								.getBirthdate())).andReturn(obsList);
		expect(registrarBean.getExpectedObs(patient, pentaSchedule.getName()))
				.andReturn(expectedObsList);
        expect(
				registrarBean.createExpectedObs(eq(patient), eq(pentaSchedule
						.getConceptName()), eq(pentaSchedule
						.getValueConceptName()), eq(penta1Event.getNumber()),
						capture(minDateCapture), capture(dueDateCapture),
						capture(lateDateCapture), capture(maxDateCapture),
						eq(penta1Event.getName()), eq(pentaSchedule.getName())))
				.andReturn(new ExpectedObs());

        calendar.set(2010, 03, 10);
        expect(registrarBean.getChildRegistrationDate()).andReturn(calendar.getTime());

        calendar.set(2011, 03, 10);
        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(calendar.getTime());
        expect(registrarBean.getEncounters(patient, MotechConstants.ENCOUNTER_TYPE_PATIENTREGVISIT, patient.getBirthdate())).andReturn(Arrays.asList(encounter));

		replay(registrarBean);

		pentaSchedule.updateSchedule(patient, date);

		verify(registrarBean);

		Date dueDate = dueDateCapture.getValue();
		Date lateDate = lateDateCapture.getValue();

		assertNotNull("Due date is null", dueDate);
		assertNotNull("Late date is null", lateDate);
		assertTrue("Late date is not after due date", lateDate.after(dueDate));
	}

	public void testUpdateExpected() {
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -2); // age is 2 months

		Patient patient = new Patient();
		patient.setBirthdate(calendar.getTime());
        patient.setDateCreated(calendar.getTime());

		Capture<ExpectedObs> expectedObsCapture = new Capture<ExpectedObs>();

		List<Obs> obsList = new ArrayList<Obs>();

        List<ExpectedObs> expectedObsList = new ArrayList<ExpectedObs>();
        ExpectedObs expectedObs = new ExpectedObs();
        expectedObs.setId(1L);
        expectedObs.setName(penta1Event.getName());
        expectedObsList.add(expectedObs);

		expect(
				registrarBean.getObs(patient, pentaSchedule.getConceptName(),
						pentaSchedule.getValueConceptName(), patient
								.getBirthdate())).andReturn(obsList);
		expect(registrarBean.getExpectedObs(patient, pentaSchedule.getName()))
				.andReturn(expectedObsList);
		expect(registrarBean.saveExpectedObs(capture(expectedObsCapture)))
				.andReturn(new ExpectedObs());

        calendar.set(2010, 03, 10);
        expect(registrarBean.getChildRegistrationDate()).andReturn(calendar.getTime());

        calendar.set(2011, 03, 10);
        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(calendar.getTime());
        expect(registrarBean.getEncounters(patient, MotechConstants.ENCOUNTER_TYPE_PATIENTREGVISIT, patient.getBirthdate())).andReturn(Arrays.asList(encounter));

		replay(registrarBean);

		pentaSchedule.updateSchedule(patient, date);

		verify(registrarBean);

		ExpectedObs capturedExpectedObs = expectedObsCapture.getValue();

		assertNotNull("Expected Obs due date is null", capturedExpectedObs
				.getDueObsDatetime());
		assertNotNull("Expected Obs late date is null", capturedExpectedObs
				.getLateObsDatetime());
		assertEquals(Boolean.FALSE, capturedExpectedObs.getVoided());
	}

	public void testSatisfyExpected() {
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -2); // age is 2 months

		Patient patient = new Patient();
		patient.setBirthdate(calendar.getTime());
        patient.setDateCreated(calendar.getTime());

        Capture<Date> minDateCapture = new Capture<Date>();
        Capture<Date> dueDateCapture = new Capture<Date>();
        Capture<Date> lateDateCapture = new Capture<Date>();
        Capture<Date> maxDateCapture = new Capture<Date>();
        Capture<ExpectedObs> expectedObsCapture = new Capture<ExpectedObs>();

        List<Obs> obsList = new ArrayList<Obs>();
        Obs obs = new Obs();
        obs.setObsDatetime(date);
        obs.setValueNumeric(new Double(1));
        obsList.add(obs);

        List<ExpectedObs> expectedObsList = new ArrayList<ExpectedObs>();
        ExpectedObs expectedObs = new ExpectedObs();
        expectedObs.setId(2L);
        expectedObs.setName(penta1Event.getName());
        expectedObsList.add(expectedObs);

        expect(
                registrarBean.getObs(patient, pentaSchedule.getConceptName(),
                        pentaSchedule.getValueConceptName(), patient
                        .getBirthdate())).andReturn(obsList);
        expect(registrarBean.getExpectedObs(patient, pentaSchedule.getName()))
                .andReturn(expectedObsList);
        expect(registrarBean.saveExpectedObs(capture(expectedObsCapture)))
                .andReturn(new ExpectedObs());
        expect(
                registrarBean.createExpectedObs(eq(patient), eq(pentaSchedule
                        .getConceptName()), eq(pentaSchedule
                        .getValueConceptName()), eq(penta2Event.getNumber()),
                        capture(minDateCapture), capture(dueDateCapture),
                        capture(lateDateCapture), capture(maxDateCapture),
                        eq(penta2Event.getName()), eq(pentaSchedule.getName())))
                .andReturn(new ExpectedObs());

        calendar.set(2010, 03, 10);
        expect(registrarBean.getChildRegistrationDate()).andReturn(calendar.getTime());

        calendar.set(2011, 03, 10);
        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(calendar.getTime());
        expect(registrarBean.getEncounters(patient, MotechConstants.ENCOUNTER_TYPE_PATIENTREGVISIT, patient.getBirthdate())).andReturn(Arrays.asList(encounter));

		replay(registrarBean);

		pentaSchedule.updateSchedule(patient, date);

		verify(registrarBean);

		ExpectedObs capturedExpectedObs = expectedObsCapture.getValue();

		assertEquals(Boolean.TRUE, capturedExpectedObs.getVoided());
		assertEquals(obs, capturedExpectedObs.getObs());

		Date dueDate = dueDateCapture.getValue();
		Date lateDate = lateDateCapture.getValue();

		assertNotNull("Due date is null", dueDate);
		assertNotNull("Late date is null", lateDate);
		assertTrue("Late date is not after due date", lateDate.after(dueDate));
	}

	public void testRemoveExpected() {
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -6); // age is 6 years

		Patient patient = new Patient();
		patient.setBirthdate(calendar.getTime());
        patient.setDateCreated(calendar.getTime());

		Capture<ExpectedObs> expectedObsCapture = new Capture<ExpectedObs>();

		List<Obs> obsList = new ArrayList<Obs>();
		Obs obs = new Obs();
		obs.setObsDatetime(date);
		obsList.add(obs);

		List<ExpectedObs> expectedObsList = new ArrayList<ExpectedObs>();
		ExpectedObs expectedObs = new ExpectedObs();
		expectedObs.setName(penta1Event.getName());
		expectedObsList.add(expectedObs);

		expect(registrarBean.getExpectedObs(patient, pentaSchedule.getName()))
				.andReturn(expectedObsList);
		expect(registrarBean.saveExpectedObs(capture(expectedObsCapture)))
				.andReturn(new ExpectedObs());

		replay(registrarBean);

		pentaSchedule.updateSchedule(patient, date);

		verify(registrarBean);

		ExpectedObs capturedExpectedObs = expectedObsCapture.getValue();

		assertEquals(Boolean.TRUE, capturedExpectedObs.getVoided());
	}

	public void testNoAction() {
		Date date = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -6); // age is 6 years

		Patient patient = new Patient();
		patient.setBirthdate(calendar.getTime());
        patient.setDateCreated(calendar.getTime());

		List<Obs> obsList = new ArrayList<Obs>();
		Obs obs = new Obs();
		obs.setObsDatetime(date);
		obsList.add(obs);

		List<ExpectedObs> expectedObsList = new ArrayList<ExpectedObs>();

		expect(registrarBean.getExpectedObs(patient, pentaSchedule.getName()))
				.andReturn(expectedObsList);
		replay(registrarBean);

		pentaSchedule.updateSchedule(patient, date);

		verify(registrarBean);
	}

}
