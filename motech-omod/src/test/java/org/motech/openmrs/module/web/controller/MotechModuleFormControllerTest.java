package org.motech.openmrs.module.web.controller;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.motech.model.Blackout;
import org.motech.model.TroubledPhone;
import org.motech.openmrs.module.ContextService;
import org.motech.openmrs.module.MotechService;
import org.motech.svc.RegistrarBean;
import org.springframework.ui.ModelMap;

public class MotechModuleFormControllerTest extends TestCase {

	RegistrarBean registrarBean;
	MotechModuleFormController controller;
	ContextService contextService;
	MotechService motechService;

	@Override
	protected void setUp() {
		registrarBean = createMock(RegistrarBean.class);
		motechService = createMock(MotechService.class);
		contextService = createMock(ContextService.class);
		controller = new MotechModuleFormController();
		controller.setRegistrarBean(registrarBean);
		controller.setContextService(contextService);
	}

	@Override
	protected void tearDown() {
		controller = null;
		registrarBean = null;
	}

	public void testRegisterClinicNoParent() throws Exception {
		String name = "Clinic Name";
		String parentId = "";
		Integer integerParentId = null;

		registrarBean.registerClinic(name, integerParentId);

		replay(registrarBean);

		controller.registerClinic(name, parentId);

		verify(registrarBean);
	}

	public void testRegisterClinicWithParent() throws Exception {
		String name = "Clinic Name";
		String parentId = "2";
		Integer integerParentId = 2;

		registrarBean.registerClinic(name, integerParentId);

		replay(registrarBean);

		controller.registerClinic(name, parentId);

		verify(registrarBean);
	}

	public void testRegiserNurse() throws Exception {
		String name = "Nurse Name", nurseId = "Nurse Id", nursePhone = "Nurse Phone";
		Integer clinicId = 1;

		registrarBean.registerNurse(name, nurseId, nursePhone, clinicId);

		replay(registrarBean);

		controller.registerNurse(name, nurseId, nursePhone, clinicId);

		verify(registrarBean);
	}

	public void testRecordMaternalVisit() throws Exception {
		Integer nurseId = 1, patientId = 2;
		String visitDate = "01/01/2009", tetanus = "true", ipt = "true", itn = "true", visitNumber = "1";
		String onARV = "true", prePMTCT = "true", testPMTCT = "true", postPMTCT = "true", hemoglobin = "1.1";

		Capture<Date> visitDateCapture = new Capture<Date>();
		Capture<Boolean> tetanusCapture = new Capture<Boolean>();
		Capture<Boolean> iptCapture = new Capture<Boolean>();
		Capture<Boolean> itnCapture = new Capture<Boolean>();
		Capture<Integer> visitNumberCapture = new Capture<Integer>();
		Capture<Boolean> onARVCapture = new Capture<Boolean>();
		Capture<Boolean> prePMTCTCapture = new Capture<Boolean>();
		Capture<Boolean> testPMTCTCapture = new Capture<Boolean>();
		Capture<Boolean> postPMTCTCapture = new Capture<Boolean>();
		Capture<Double> hemoglobin36Capture = new Capture<Double>();

		registrarBean.recordMaternalVisit(eq(nurseId),
				capture(visitDateCapture), eq(patientId),
				capture(tetanusCapture), capture(iptCapture),
				capture(itnCapture), capture(visitNumberCapture),
				capture(onARVCapture), capture(prePMTCTCapture),
				capture(testPMTCTCapture), capture(postPMTCTCapture),
				capture(hemoglobin36Capture));

		replay(registrarBean);

		controller.recordMaternalVisit(nurseId, visitDate, patientId, tetanus,
				ipt, itn, visitNumber, onARV, prePMTCT, testPMTCT, postPMTCT,
				hemoglobin);

		verify(registrarBean);

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

		assertEquals(visitDate, dateFormat.format(visitDateCapture.getValue()));
		assertEquals(tetanus, tetanusCapture.getValue().toString());
		assertEquals(ipt, iptCapture.getValue().toString());
		assertEquals(itn, itnCapture.getValue().toString());
		assertEquals(visitNumber, visitNumberCapture.getValue().toString());
		assertEquals(onARV, onARVCapture.getValue().toString());
		assertEquals(prePMTCT, prePMTCTCapture.getValue().toString());
		assertEquals(testPMTCT, testPMTCTCapture.getValue().toString());
		assertEquals(postPMTCT, postPMTCTCapture.getValue().toString());
		assertEquals(hemoglobin, hemoglobin36Capture.getValue().toString());
	}

	public void testViewBlackoutForm() throws ParseException {

		Time startTime = Time.valueOf("07:00:00"), endTime = Time
				.valueOf("19:00:00");

		Blackout interval = new Blackout(startTime, endTime);
		expect(contextService.getMotechService()).andReturn(motechService);
		expect(motechService.getBlackoutSettings()).andReturn(interval);

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller.viewBlackoutSettings(model);

		verify(contextService, motechService);

		assertEquals("/module/motechmodule/blackout", path);
		assertEquals(model.get("startTime"), startTime);
		assertEquals(model.get("endTime"), endTime);
	}

	public void testViewBlackoutFormNoData() throws ParseException {
		expect(contextService.getMotechService()).andReturn(motechService);
		expect(motechService.getBlackoutSettings()).andReturn(null);

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller.viewBlackoutSettings(model);

		verify(contextService, motechService);

		assertEquals("/module/motechmodule/blackout", path);
	}

	public void testSaveBlackoutSettings() throws ParseException {

		String startTime = "07:00:00", endTime = "19:00:00";

		Capture<Blackout> boCap = new Capture<Blackout>();

		expect(contextService.getMotechService()).andReturn(motechService);

		expect(motechService.getBlackoutSettings()).andReturn(null);
		motechService.setBlackoutSettings(capture(boCap));

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller
				.saveBlackoutSettings(startTime, endTime, model);

		verify(contextService, motechService);

		assertEquals("/module/motechmodule/blackout", path);
		assertEquals(startTime, model.get("startTime").toString());
		assertEquals(endTime, model.get("endTime").toString());
		assertEquals(startTime, boCap.getValue().getStartTime().toString());
		assertEquals(endTime, boCap.getValue().getEndTime().toString());
	}

	public void testUpdateBlackoutSettings() throws ParseException {

		String startTime = "07:00:00", endTime = "19:00:00";

		Capture<Blackout> boCap = new Capture<Blackout>();

		expect(contextService.getMotechService()).andReturn(motechService);

		Blackout blackout = new Blackout(null, null);
		expect(motechService.getBlackoutSettings()).andReturn(blackout);
		motechService.setBlackoutSettings(capture(boCap));

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller
				.saveBlackoutSettings(startTime, endTime, model);

		verify(contextService, motechService);

		assertEquals("/module/motechmodule/blackout", path);
		assertEquals(startTime, model.get("startTime").toString());
		assertEquals(endTime, model.get("endTime").toString());
		assertEquals(startTime, boCap.getValue().getStartTime().toString());
		assertEquals(endTime, boCap.getValue().getEndTime().toString());
	}

	public void testLookupTroubledPhoneNoPhone() {

		String phone = null;

		ModelMap model = new ModelMap();
		String path = controller.handleTroubledPhone(phone, null, model);

		assertNull(model.get("troubledPhone"));
		assertEquals("/module/motechmodule/troubledphone", path);
	}

	public void testLookupTroubledPhone() {

		String phone = "378378373";
		TroubledPhone tp = new TroubledPhone();
		tp.setId(38903L);
		tp.setPhoneNumber(phone);

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(motechService.getTroubledPhone(phone)).andReturn(tp);

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller.handleTroubledPhone(phone, null, model);

		verify(contextService, motechService);

		assertEquals(tp, model.get("troubledPhone"));
		assertEquals("/module/motechmodule/troubledphone", path);
	}

	public void testRemoveTroubledPhone() {
		String phone = "378378373";
		TroubledPhone tp = new TroubledPhone();
		tp.setId(38903L);
		tp.setPhoneNumber(phone);

		expect(contextService.getMotechService()).andReturn(motechService);
		expect(motechService.getTroubledPhone(phone)).andReturn(tp);
		motechService.removeTroubledPhone(phone);

		replay(contextService, motechService);

		ModelMap model = new ModelMap();
		String path = controller.handleTroubledPhone(phone, true, model);

		verify(contextService, motechService);

		assertNull(model.get("troubledPhone"));
		assertEquals("/module/motechmodule/troubledphone", path);
	}
}
