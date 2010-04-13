package org.motechproject.server.ws;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.LogManager;

import org.easymock.Capture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.motechproject.server.model.ExpectedEncounter;
import org.motechproject.server.model.ExpectedObs;
import org.motechproject.server.svc.BirthOutcomeChild;
import org.motechproject.server.svc.OpenmrsBean;
import org.motechproject.server.svc.RegistrarBean;
import org.motechproject.ws.BirthOutcome;
import org.motechproject.ws.Care;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.DeliveredBy;
import org.motechproject.ws.Gender;
import org.motechproject.ws.HIVStatus;
import org.motechproject.ws.LogType;
import org.motechproject.ws.Patient;
import org.motechproject.ws.server.RegistrarService;
import org.motechproject.ws.server.ValidationError;
import org.motechproject.ws.server.ValidationException;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RegistrarServiceTest {

	static ApplicationContext ctx;
	static RegistrarService regWs;
	static RegistrarBean registrarBean;
	static OpenmrsBean openmrsBean;
	static WebServiceModelConverter modelConverter;

	@BeforeClass
	public static void setUpClass() throws Exception {
		LogManager.getLogManager().readConfiguration(
				RegistrarServiceTest.class
						.getResourceAsStream("/jul-test.properties"));
		registrarBean = createMock(RegistrarBean.class);
		openmrsBean = createMock(OpenmrsBean.class);
		modelConverter = createMock(WebServiceModelConverter.class);
		ctx = new ClassPathXmlApplicationContext("test-context.xml");
		RegistrarWebService regService = (RegistrarWebService) ctx
				.getBean("registrarService");
		regService.setRegistrarBean(registrarBean);
		regService.setOpenmrsBean(openmrsBean);
		regService.setModelConverter(modelConverter);
		regWs = (RegistrarService) ctx.getBean("registrarClient");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		ctx = null;
		regWs = null;
		registrarBean = null;
		openmrsBean = null;
		modelConverter = null;
		LogManager.getLogManager().readConfiguration();
	}

	@Before
	public void setup() {
	}

	@After
	public void tearDown() throws Exception {
		reset(registrarBean, modelConverter, openmrsBean);
	}

	@Test
	public void testRecordMotherANCVisit() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer visitNumber = 1, ttDose = 1, iptDose = 1;
		Boolean itnUse = true;
		Date date = new Date();
		HIVStatus hivStatus = HIVStatus.NA;

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordMotherANCVisit(nurse, date, patient, visitNumber,
				ttDose, iptDose, itnUse, hivStatus);

		replay(registrarBean, openmrsBean);

		regWs.recordMotherANCVisit(chpsId, date, patientId, visitNumber,
				ttDose, iptDose, itnUse, hivStatus);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordMotherANCVisitInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer visitNumber = 1, ttDose = 1, iptDose = 1;
		Boolean itnUse = true;
		Date date = new Date();
		HIVStatus hivStatus = HIVStatus.NA;

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordMotherANCVisit(chpsId, date, patientId, visitNumber,
					ttDose, iptDose, itnUse, hivStatus);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother ANC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordPregnancyTermination() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer abortionType = 1, complication = 1;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordPregnancyTermination(nurse, date, patient,
				abortionType, complication);

		replay(registrarBean, openmrsBean);

		regWs.recordPregnancyTermination(chpsId, date, patientId, abortionType,
				complication);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordPregnancyTerminationInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer abortionType = 1, complication = 1;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordPregnancyTermination(chpsId, date, patientId,
					abortionType, complication);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Pregnancy Termination request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordPregnancyDelivery() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		String child1Id = "246Test", child2Id = "468Test", child3Id = "579Test", child1Name = "Child1First", child2Name = "Child2First", child3Name = "Child3First";
		Integer method = 1, outcome = 2, location = 1, cause = 1;
		Boolean maternalDeath = false, child1opv = true, child1bcg = false, child2opv = false, child2bcg = true, child3opv = false, child3bcg = true;
		Date date = new Date();
		DeliveredBy deliveredBy = DeliveredBy.CHO;
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		BirthOutcome child2birthOutcome = BirthOutcome.FSB;
		BirthOutcome child3birthOutcome = BirthOutcome.MSB;
		Gender child1Sex = Gender.FEMALE;
		Gender child2Sex = Gender.MALE;
		Gender child3Sex = Gender.MALE;

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		Capture<List<BirthOutcomeChild>> outcomesCapture = new Capture<List<BirthOutcomeChild>>();

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordPregnancyDelivery(eq(nurse), eq(date), eq(patient),
				eq(method), eq(outcome), eq(location), eq(deliveredBy),
				eq(maternalDeath), eq(cause), capture(outcomesCapture));

		replay(registrarBean, openmrsBean);

		regWs.recordPregnancyDelivery(chpsId, date, patientId, method, outcome,
				location, deliveredBy, maternalDeath, cause,
				child1birthOutcome, child1Id, child1Sex, child1Name, child1opv,
				child1bcg, child2birthOutcome, child2Id, child2Sex, child2Name,
				child2opv, child2bcg, child3birthOutcome, child3Id, child3Sex,
				child3Name, child3opv, child3bcg);

		verify(registrarBean, openmrsBean);

		List<BirthOutcomeChild> outcomes = outcomesCapture.getValue();
		assertEquals(3, outcomes.size());

		BirthOutcomeChild child1 = outcomes.get(0);
		assertEquals(child1birthOutcome, child1.getOutcome());
		assertEquals(child1Id, child1.getPatientId());
		assertEquals(child1Name, child1.getFirstName());
		assertEquals(child1Sex, child1.getSex());
		assertEquals(child1bcg, child1.getBcg());
		assertEquals(child1opv, child1.getOpv());

		BirthOutcomeChild child2 = outcomes.get(1);
		assertEquals(child2birthOutcome, child2.getOutcome());
		assertEquals(child2Id, child2.getPatientId());
		assertEquals(child2Name, child2.getFirstName());
		assertEquals(child2Sex, child2.getSex());
		assertEquals(child2bcg, child2.getBcg());
		assertEquals(child2opv, child2.getOpv());

		BirthOutcomeChild child3 = outcomes.get(2);
		assertEquals(child3birthOutcome, child3.getOutcome());
		assertEquals(child3Id, child3.getPatientId());
		assertEquals(child3Name, child3.getFirstName());
		assertEquals(child3Sex, child3.getSex());
		assertEquals(child3bcg, child3.getBcg());
		assertEquals(child3opv, child3.getOpv());

	}

	@Test
	public void testRecordPregnancyDeliveryOneChild()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		String child1Id = "246Test", child1Name = "Child1First";
		Integer method = 1, outcome = 2, location = 1, cause = 1;
		Boolean maternalDeath = false, child1opv = true, child1bcg = false;
		Date date = new Date();
		DeliveredBy deliveredBy = DeliveredBy.CHO;
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		Gender child1Sex = Gender.FEMALE;

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		Capture<List<BirthOutcomeChild>> outcomesCapture = new Capture<List<BirthOutcomeChild>>();

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordPregnancyDelivery(eq(nurse), eq(date), eq(patient),
				eq(method), eq(outcome), eq(location), eq(deliveredBy),
				eq(maternalDeath), eq(cause), capture(outcomesCapture));

		replay(registrarBean, openmrsBean);

		regWs.recordPregnancyDelivery(chpsId, date, patientId, method, outcome,
				location, deliveredBy, maternalDeath, cause,
				child1birthOutcome, child1Id, child1Sex, child1Name, child1opv,
				child1bcg, null, null, null, null, null, null, null, null,
				null, null, null, null);

		verify(registrarBean, openmrsBean);

		List<BirthOutcomeChild> outcomes = outcomesCapture.getValue();
		assertEquals(1, outcomes.size());

		BirthOutcomeChild child1 = outcomes.get(0);
		assertEquals(child1birthOutcome, child1.getOutcome());
		assertEquals(child1Id, child1.getPatientId());
		assertEquals(child1Name, child1.getFirstName());
		assertEquals(child1Sex, child1.getSex());
		assertEquals(child1bcg, child1.getBcg());
		assertEquals(child1opv, child1.getOpv());
	}

	@Test
	public void testRecordPregnancyDeliveryInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		String child1Id = "246Test", child2Id = "468Test", child3Id = "579Test", child1Name = "Child1First", child2Name = "Child2First", child3Name = "Child3First";
		Integer method = 1, outcome = 2, location = 1, cause = 1;
		Boolean maternalDeath = false, child1opv = true, child1bcg = false, child2opv = false, child2bcg = true, child3opv = false, child3bcg = true;
		Date date = new Date();
		DeliveredBy deliveredBy = DeliveredBy.CHO;
		BirthOutcome child1birthOutcome = BirthOutcome.A;
		BirthOutcome child2birthOutcome = BirthOutcome.FSB;
		BirthOutcome child3birthOutcome = BirthOutcome.MSB;
		Gender child1Sex = Gender.FEMALE;
		Gender child2Sex = Gender.MALE;
		Gender child3Sex = Gender.MALE;

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordPregnancyDelivery(chpsId, date, patientId, method,
					outcome, location, deliveredBy, maternalDeath, cause,
					child1birthOutcome, child1Id, child1Sex, child1Name,
					child1opv, child1bcg, child2birthOutcome, child2Id,
					child2Sex, child2Name, child2opv, child2bcg,
					child3birthOutcome, child3Id, child3Sex, child3Name,
					child3opv, child3bcg);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Pregnancy Delivery request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordMotherPPCVisit() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer visitNumber = 1, ttDose = 1;
		Boolean vitaminA = true;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordMotherPPCVisit(nurse, date, patient, visitNumber,
				vitaminA, ttDose);

		replay(registrarBean, openmrsBean);

		regWs.recordMotherPPCVisit(chpsId, date, patientId, visitNumber,
				vitaminA, ttDose);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordMotherPPCVisitInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer visitNumber = 1, ttDose = 1;
		Boolean vitaminA = true;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordMotherPPCVisit(chpsId, date, patientId, visitNumber,
					vitaminA, ttDose);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother PPC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordDeath() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer cause = 1;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordDeath(nurse, date, patient, cause);

		replay(registrarBean, openmrsBean);

		regWs.recordDeath(chpsId, date, patientId, cause);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordDeathInvalidPatientId() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer cause = 1;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordDeath(chpsId, date, patientId, cause);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Death request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordChildPNCVisit() throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer opvDose = 1, pentaDose = 1;
		Boolean bcg = true, yellowFever = true, csm = true, measles = true, ipti = true, vitaminA = true;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordChildPNCVisit(nurse, date, patient, bcg, opvDose,
				pentaDose, yellowFever, csm, measles, ipti, vitaminA);

		replay(registrarBean, openmrsBean);

		regWs.recordChildPNCVisit(chpsId, date, patientId, bcg, opvDose,
				pentaDose, yellowFever, csm, measles, ipti, vitaminA);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordChildPNCVisitInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", patientId = "123Test";
		Integer opvDose = 1, pentaDose = 1;
		Boolean bcg = true, yellowFever = true, csm = true, measles = true, ipti = true, vitaminA = true;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordChildPNCVisit(chpsId, date, patientId, bcg, opvDose,
					pentaDose, yellowFever, csm, measles, ipti, vitaminA);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Child PNC Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRegisterChild() throws ValidationException {
		Date childDob = new Date(), nhisExpires = new Date();
		String nurseId = "FGH267", motherRegNum = "ABC123", childRegNum = "DEF456", childFirstName = "Sarah", nhis = "14567";
		Gender childGender = Gender.FEMALE;

		User nurse = new User(1);
		org.openmrs.Patient mother = new org.openmrs.Patient(2);
		org.openmrs.Patient child = null;

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(motherRegNum))
				.andReturn(mother);
		expect(openmrsBean.getPatientByMotechId(childRegNum)).andReturn(child);

		expect(
				registrarBean.registerChild(nurse, mother, childRegNum,
						childDob, childGender, childFirstName, nhis,
						nhisExpires)).andReturn(new org.openmrs.Patient());

		replay(registrarBean, openmrsBean);

		regWs.registerChild(nurseId, motherRegNum, childRegNum, childDob,
				childGender, childFirstName, nhis, nhisExpires);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRegisterChildAllErrors() {
		Date nhisExpires = new Date();
		Calendar dobCal = new GregorianCalendar();
		dobCal.set(Calendar.YEAR, dobCal.get(Calendar.YEAR) - 6);
		Date childDob = dobCal.getTime();
		String nurseId = "FGH267", motherRegNum = "ABC123", childRegNum = "DEF456", childFirstName = "Sarah", nhis = "14567";
		Gender childGender = Gender.FEMALE;

		User nurse = null;
		org.openmrs.Patient mother = null;
		org.openmrs.Patient child = new org.openmrs.Patient(3);

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(motherRegNum))
				.andReturn(mother);
		expect(openmrsBean.getPatientByMotechId(childRegNum)).andReturn(child);

		replay(registrarBean, openmrsBean);

		try {
			regWs.registerChild(nurseId, motherRegNum, childRegNum, childDob,
					childGender, childFirstName, nhis, nhisExpires);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Register Child request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(4, errors.size());
			ValidationError nurseError = errors.get(0);
			assertEquals(1, nurseError.getCode());
			assertEquals("CHPSID", nurseError.getField());
			ValidationError motherError = errors.get(1);
			assertEquals(1, motherError.getCode());
			assertEquals("MotherMotechID", motherError.getField());
			ValidationError childError = errors.get(2);
			assertEquals(2, childError.getCode());
			assertEquals("ChildMotechID", childError.getField());
			ValidationError dobError = errors.get(3);
			assertEquals(2, dobError.getCode());
			assertEquals("DoB", dobError.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testEditPatient() throws ValidationException {
		String nurseId = "FGH267", patientRegNum = "ABC123", primaryPhone = "12075557894", secondaryPhone = "12075557895", nhis = "125";
		ContactNumberType primaryPhoneType = ContactNumberType.PERSONAL, secondaryPhoneType = ContactNumberType.PUBLIC;
		Date nhisExpires = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientRegNum)).andReturn(
				patient);

		registrarBean.editPatient(nurse, patient, primaryPhone,
				primaryPhoneType, secondaryPhone, secondaryPhoneType, nhis,
				nhisExpires);
		replay(registrarBean, openmrsBean);

		regWs.editPatient(nurseId, patientRegNum, primaryPhone,
				primaryPhoneType, secondaryPhone, secondaryPhoneType, nhis,
				nhisExpires);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testEditPatientAllErrors() {
		String nurseId = "FGH267", patientRegNum = "ABC123", primaryPhone = "12075557894", secondaryPhone = "12075557895", nhis = "125";
		ContactNumberType primaryPhoneType = ContactNumberType.PERSONAL, secondaryPhoneType = ContactNumberType.PUBLIC;
		Date nhisExpires = new Date();

		User nurse = null;
		org.openmrs.Patient patient = null;

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientRegNum)).andReturn(
				patient);

		replay(registrarBean, openmrsBean);

		try {
			regWs.editPatient(nurseId, patientRegNum, primaryPhone,
					primaryPhoneType, secondaryPhone, secondaryPhoneType, nhis,
					nhisExpires);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Edit Patient request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			ValidationError nurseError = errors.get(0);
			assertEquals(1, nurseError.getCode());
			assertEquals("CHPSID", nurseError.getField());
			ValidationError patientError = errors.get(1);
			assertEquals(1, patientError.getCode());
			assertEquals("MotechID", patientError.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testStopPregnancyProgram() throws ValidationException {
		String nurseId = "FGH267", patientRegNum = "ABC123";

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientRegNum)).andReturn(
				patient);

		registrarBean.stopPregnancyProgram(nurse, patient);

		replay(registrarBean, openmrsBean);

		regWs.stopPregnancyProgram(nurseId, patientRegNum);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testStopPregnancyProgramAllErrors() {
		String nurseId = "FGH267", patientRegNum = "ABC123";

		User nurse = null;
		org.openmrs.Patient patient = null;

		expect(openmrsBean.getNurseByCHPSId(nurseId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientRegNum)).andReturn(
				patient);

		replay(registrarBean, openmrsBean);

		try {
			regWs.stopPregnancyProgram(nurseId, patientRegNum);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Stop Pregnancy Program request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(2, errors.size());
			ValidationError nurseError = errors.get(0);
			assertEquals(1, nurseError.getCode());
			assertEquals("CHPSID", nurseError.getField());
			ValidationError patientError = errors.get(1);
			assertEquals(1, patientError.getCode());
			assertEquals("MotechID", patientError.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testGeneralVisit() throws ValidationException {
		String chpsId = "Facility1", serial = "Test123";
		Gender gender = Gender.MALE;
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referral = false;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);

		registrarBean.recordGeneralVisit(chpsId, date, serial, gender, date,
				insured, newCase, diagnosis, secondDiagnosis, referral);

		replay(registrarBean, openmrsBean);

		regWs.recordGeneralVisit(chpsId, date, serial, gender, date, insured,
				newCase, diagnosis, secondDiagnosis, referral);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testGeneralVisitInvalidNurseId() {
		String chpsId = null, serial = "Test123";
		Gender gender = Gender.MALE;
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean insured = true, newCase = true, referral = false;
		Date date = new Date();

		try {
			regWs.recordGeneralVisit(chpsId, date, serial, gender, date,
					insured, newCase, diagnosis, secondDiagnosis, referral);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in General Visit request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("CHPSID", error.getField());
		}
	}

	@Test
	public void testRecordChildVisit() throws ValidationException {
		String chpsId = "CHPSId", serial = "Test123", patientId = "123Test";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean newCase = true, referral = false;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordChildVisit(nurse, date, patient, serial, newCase,
				diagnosis, secondDiagnosis, referral);

		replay(registrarBean, openmrsBean);

		regWs.recordChildVisit(chpsId, date, serial, patientId, newCase,
				diagnosis, secondDiagnosis, referral);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordChildVisitInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", serial = "Test123", patientId = "123Test";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean newCase = true, referral = false;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordChildVisit(chpsId, date, serial, patientId, newCase,
					diagnosis, secondDiagnosis, referral);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Child Visit request", e.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordMotherVisit() throws ValidationException {
		String chpsId = "CHPSId", serial = "Test123", patientId = "123Test";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean newCase = true, referral = false;
		Date date = new Date();

		User nurse = new User(1);
		org.openmrs.Patient patient = new org.openmrs.Patient(2);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(patient);
		registrarBean.recordMotherVisit(nurse, date, patient, serial, newCase,
				diagnosis, secondDiagnosis, referral);

		replay(registrarBean, openmrsBean);

		regWs.recordMotherVisit(chpsId, date, serial, patientId, newCase,
				diagnosis, secondDiagnosis, referral);

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testRecordMotherVisitInvalidPatientId()
			throws ValidationException {
		String chpsId = "CHPSId", serial = "Test123", patientId = "123Test";
		Integer diagnosis = 5, secondDiagnosis = 6;
		Boolean newCase = true, referral = false;
		Date date = new Date();

		User nurse = new User(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(nurse);
		expect(openmrsBean.getPatientByMotechId(patientId)).andReturn(null);

		replay(registrarBean, openmrsBean);

		try {
			regWs.recordMotherVisit(chpsId, date, serial, patientId, newCase,
					diagnosis, secondDiagnosis, referral);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Errors in Record Mother Visit request", e
					.getMessage());
			assertNotNull("Validation Exception FaultBean is Null", e
					.getFaultInfo());
			List<ValidationError> errors = e.getFaultInfo().getErrors();
			assertNotNull("Validation Errors is Null", errors);
			assertEquals(1, errors.size());
			ValidationError error = errors.get(0);
			assertEquals(1, error.getCode());
			assertEquals("MotechID", error.getField());
		}

		verify(registrarBean, openmrsBean);
	}

	@Test
	public void testQueryANCDefaulters() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		Capture<String[]> encounterGroups = new Capture<String[]>();
		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();
		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] defaultedCares = { encounterCare, obsCare };

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(
				registrarBean
						.getDefaultedExpectedEncounters(capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(registrarBean.getDefaultedExpectedObs(capture(obsGroups)))
				.andReturn(expectedObs);
		expect(
				modelConverter.defaultedToWebServiceCares(expectedEncounters,
						expectedObs)).andReturn(defaultedCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Care[] cares = regWs.queryANCDefaulters(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("ANC", encounterGroups.getValue()[0]);

		assertEquals(2, obsGroups.getValue().length);
		assertEquals("TT", obsGroups.getValue()[0]);
		assertEquals("IPT", obsGroups.getValue()[1]);

		assertNotNull("Care result array is null", cares);
		assertEquals(2, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
		assertEquals(obsCare.getName(), cares[1].getName());
	}

	@Test
	public void testQueryTTDefaulters() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] obsCares = { obsCare };

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(registrarBean.getDefaultedExpectedObs(capture(obsGroups)))
				.andReturn(expectedObs);
		expect(modelConverter.defaultedObsToWebServiceCares(expectedObs))
				.andReturn(obsCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Care[] cares = regWs.queryTTDefaulters(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertEquals(1, obsGroups.getValue().length);
		assertEquals("TT", obsGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(obsCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryPPCDefaulters() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		Capture<String[]> encounterGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care[] encounterCares = { encounterCare };

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(
				registrarBean
						.getDefaultedExpectedEncounters(capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(
				modelConverter
						.defaultedEncountersToWebServiceCares(expectedEncounters))
				.andReturn(encounterCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Care[] cares = regWs.queryPPCDefaulters(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("PPC", encounterGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryPNCDefaulters() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		Capture<String[]> encounterGroups = new Capture<String[]>();

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();

		Care encounterCare = new Care();
		encounterCare.setName("EncounterCare");
		Care[] encounterCares = { encounterCare };

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(
				registrarBean
						.getDefaultedExpectedEncounters(capture(encounterGroups)))
				.andReturn(expectedEncounters);
		expect(
				modelConverter
						.defaultedEncountersToWebServiceCares(expectedEncounters))
				.andReturn(encounterCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Care[] cares = regWs.queryPNCDefaulters(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertEquals(1, encounterGroups.getValue().length);
		assertEquals("PNC", encounterGroups.getValue()[0]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(encounterCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryCWCDefaulters() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		Capture<String[]> obsGroups = new Capture<String[]>();

		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Care obsCare = new Care();
		obsCare.setName("ObsCare");
		Care[] obsCares = { obsCare };

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(registrarBean.getDefaultedExpectedObs(capture(obsGroups)))
				.andReturn(expectedObs);
		expect(modelConverter.defaultedObsToWebServiceCares(expectedObs))
				.andReturn(obsCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Care[] cares = regWs.queryCWCDefaulters(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertEquals(7, obsGroups.getValue().length);
		assertEquals("OPV", obsGroups.getValue()[0]);
		assertEquals("BCG", obsGroups.getValue()[1]);
		assertEquals("Penta", obsGroups.getValue()[2]);
		assertEquals("YellowFever", obsGroups.getValue()[3]);
		assertEquals("Measles", obsGroups.getValue()[4]);
		assertEquals("VitaA", obsGroups.getValue()[5]);
		assertEquals("IPTI", obsGroups.getValue()[6]);

		assertNotNull("Care result array is null", cares);
		assertEquals(1, cares.length);
		assertEquals(obsCare.getName(), cares[0].getName());
	}

	@Test
	public void testQueryUpcomingDeliveries() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		List<Obs> pregnancies = new ArrayList<Obs>();
		pregnancies.add(new Obs());

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(registrarBean.getUpcomingPregnanciesDueDate()).andReturn(
				pregnancies);
		expect(modelConverter.dueDatesToWebServicePatients(pregnancies))
				.andReturn(new Patient[1]);

		replay(registrarBean, modelConverter, openmrsBean);

		Patient[] patients = regWs.queryUpcomingDeliveries(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryRecentDeliveries() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		List<Encounter> deliveries = new ArrayList<Encounter>();
		deliveries.add(new Encounter());

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(registrarBean.getRecentDeliveries()).andReturn(deliveries);
		expect(modelConverter.deliveriesToWebServicePatients(deliveries))
				.andReturn(new Patient[1]);

		replay(registrarBean, modelConverter, openmrsBean);

		Patient[] patients = regWs.queryRecentDeliveries(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryOverdueDeliveries() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId";

		List<Obs> pregnancies = new ArrayList<Obs>();
		pregnancies.add(new Obs());

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(registrarBean.getOverduePregnanciesDueDate()).andReturn(
				pregnancies);
		expect(modelConverter.dueDatesToWebServicePatients(pregnancies))
				.andReturn(new Patient[1]);

		replay(registrarBean, modelConverter, openmrsBean);

		Patient[] patients = regWs.queryOverdueDeliveries(facilityId, chpsId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertNotNull("Patient result array is null", patients);
		assertEquals(1, patients.length);
	}

	@Test
	public void testQueryUpcomingCare() throws ValidationException {
		String facilityId = "FacilityId", chpsId = "CHPSId", motechId = "MotechId";

		List<ExpectedEncounter> expectedEncounters = new ArrayList<ExpectedEncounter>();
		List<ExpectedObs> expectedObs = new ArrayList<ExpectedObs>();

		Calendar calendar = Calendar.getInstance();

		Care encounterCare1 = new Care();
		encounterCare1.setName("EncounterCare1");
		calendar.set(2010, Calendar.APRIL, 4);
		encounterCare1.setDate(calendar.getTime());
		Care encounterCare2 = new Care();
		encounterCare2.setName("EncounterCare2");
		calendar.set(2010, Calendar.DECEMBER, 12);
		encounterCare2.setDate(calendar.getTime());
		Care obsCare1 = new Care();
		obsCare1.setName("ObsCare1");
		calendar.set(2010, Calendar.OCTOBER, 10);
		obsCare1.setDate(calendar.getTime());
		Care obsCare2 = new Care();
		obsCare2.setName("ObsCare2");
		calendar.set(2010, Calendar.JANUARY, 1);
		obsCare2.setDate(calendar.getTime());
		Care[] upcomingCares = { obsCare2, encounterCare1, obsCare1,
				encounterCare2 };

		org.openmrs.Patient patient = new org.openmrs.Patient(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(openmrsBean.getPatientByMotechId(motechId)).andReturn(patient);
		expect(modelConverter.patientToWebService(eq(patient), eq(true)))
				.andReturn(new Patient());

		expect(registrarBean.getUpcomingExpectedEncounters(patient)).andReturn(
				expectedEncounters);
		expect(registrarBean.getUpcomingExpectedObs(patient)).andReturn(
				expectedObs);
		expect(
				modelConverter.upcomingToWebServiceCares(expectedEncounters,
						expectedObs, true)).andReturn(upcomingCares);

		replay(registrarBean, modelConverter, openmrsBean);

		Patient wsPatient = regWs.queryUpcomingCare(facilityId, chpsId,
				motechId);

		verify(registrarBean, modelConverter, openmrsBean);

		assertNotNull("Patient result is null", wsPatient);
		Care[] cares = wsPatient.getCares();
		assertNotNull("Patient cares is null", cares);
		assertEquals(4, cares.length);
		assertEquals(obsCare2.getName(), cares[0].getName());
		assertEquals(obsCare2.getDate(), cares[0].getDate());
		assertEquals(encounterCare1.getName(), cares[1].getName());
		assertEquals(encounterCare1.getDate(), cares[1].getDate());
		assertEquals(obsCare1.getName(), cares[2].getName());
		assertEquals(obsCare1.getDate(), cares[2].getDate());
		assertEquals(encounterCare2.getName(), cares[3].getName());
		assertEquals(encounterCare2.getDate(), cares[3].getDate());
	}

	@Test
	public void testQueryMotechId() throws ValidationException {
		String chpsId = "CHPSId", firstName = "FirstName", lastName = "LastName", prefName = "PrefName";
		String nhis = "NHIS", phone = "Phone";
		Date birthDate = new Date();

		List<org.openmrs.Patient> patients = new ArrayList<org.openmrs.Patient>();
		patients.add(new org.openmrs.Patient(1));

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(
				registrarBean.getPatients(firstName, lastName, prefName,
						birthDate, null, phone, nhis)).andReturn(patients);
		expect(modelConverter.patientToWebService(patients, true)).andReturn(
				new Patient[1]);

		replay(registrarBean, modelConverter, openmrsBean);

		Patient[] wsPatients = regWs.queryMotechId(chpsId, firstName, lastName,
				prefName, birthDate, nhis, phone);

		verify(registrarBean, modelConverter, openmrsBean);

		assertNotNull("Patient result array is null", wsPatients);
		assertEquals(1, wsPatients.length);
	}

	@Test
	public void testQueryPatient() throws ValidationException {
		String chpsId = "CHPSId", motechId = "MotechId";

		org.openmrs.Patient patient = new org.openmrs.Patient(1);

		expect(openmrsBean.getNurseByCHPSId(chpsId)).andReturn(new User(1));
		expect(openmrsBean.getPatientByMotechId(motechId)).andReturn(patient);
		expect(modelConverter.patientToWebService(eq(patient), eq(false)))
				.andReturn(new Patient());

		replay(registrarBean, modelConverter, openmrsBean);

		regWs.queryPatient(chpsId, motechId);

		verify(registrarBean, modelConverter, openmrsBean);
	}

	@Test
	public void testLog() {
		LogType type = LogType.SUCCESS;
		String msg = "logging over ws is slow";

		registrarBean.log(type, msg);

		replay(registrarBean);

		regWs.log(type, msg);

		verify(registrarBean);
	}

	@Test
	public void testSetMessageStatus() {
		String messageId = "12345678-1234-1234-1234-123456789012";
		Boolean success = true;

		registrarBean.setMessageStatus(messageId, success);

		replay(registrarBean);

		regWs.setMessageStatus(messageId, success);

		verify(registrarBean);
	}

	@Test
	public void testRegistrarBeanProperty() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		RegistrarWebService regWs = new RegistrarWebService();

		Field regBeanField = regWs.getClass().getDeclaredField("registrarBean");
		regBeanField.setAccessible(true);

		regWs.setRegistrarBean(registrarBean);
		assertEquals(registrarBean, regBeanField.get(regWs));

		regWs.setRegistrarBean(null);
		assertEquals(null, regBeanField.get(regWs));
	}
}