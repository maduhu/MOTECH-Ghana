package org.motechproject.server.omod.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.server.model.rct.RCTFacility;
import org.motechproject.server.service.ContextService;
import org.motechproject.server.svc.RCTService;
import org.motechproject.server.util.RCTError;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.Patient;
import org.motechproject.ws.rct.RCTRegistrationConfirmation;
import org.openmrs.User;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class RCTServiceImplTest extends BaseModuleContextSensitiveTest {

    private static String RCT_DATA = "rct-dataset.xml";


    @Autowired
    @Qualifier("rctService")
    private RCTService service;

    @Autowired
    private ContextService contextService;

    @Before
    public void setUp() throws Exception {
        executeDataSet(RCT_DATA);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldRegisterRCTPatient() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 2);
        Date deliveryDate = calendar.getTime();
        Patient patient = new Patient();
        patient.setMotechId("123654");
        patient.setEstimateDueDate(deliveryDate);
        patient.setContactNumberType(ContactNumberType.PERSONAL);
        RCTRegistrationConfirmation confirmation = service.register(patient, user(), facility(11117));
        assertFalse(confirmation.getErrors());
    }


    @Test
    public void shouldDetermineIfPatientIsRCT() {
        assertTrue(service.isPatientRegisteredIntoRCT(1234567));
        assertFalse(service.isPatientRegisteredIntoRCT(1234568));
    }

    @Test
    public void shouldDetermineIfFacilityIsCoveredInRCT() {
        assertNotNull(service.getRCTFacilityById(11117));
        assertNotNull(service.getRCTFacilityById(11118));
        assertNull(service.getRCTFacilityById(11121));
    }

    @Test
    public void shouldNotRegisterPatientInRCTIfPregnancyNotRegistered() {
        Patient patient = new Patient();
        assertFalse(patient.isPregnancyRegistered());
        RCTRegistrationConfirmation confirmation = service.register(patient, user(), facility(11117));
        assertFailedConfirmation(confirmation, RCTError.PREGNANCY_NOT_REGISTERED);
    }

    @Test
    public void shouldNotRegisterPatientInRCTIfFirstTrimesterPregnant() {
        Patient patient = new Patient();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 8);
        patient.setEstimateDueDate(calendar.getTime());
        assertTrue(patient.isPregnancyRegistered());
        assertTrue(patient.isInFirstTrimesterOfPregnancy());
        RCTRegistrationConfirmation confirmation = service.register(patient, user(), facility(11117));
        assertFailedConfirmation(confirmation, RCTError.FIRST_TRIMESTER_PREGNANCY);
    }

    @Test
    public void shouldNotRegisterPatientInRCTIfStratumNotFound() {
        Patient patient = new Patient();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 2);
        patient.setEstimateDueDate(calendar.getTime());
        RCTRegistrationConfirmation confirmation = service.register(patient, user(), facility(11119));
        assertFailedConfirmation(confirmation, RCTError.RCT_STRATUM_NOT_FOUND);
    }


    @Test
    public void shouldNotRegisterPatientWhenControlGroupsNotConfigured() {
        Patient patient = new Patient();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 2);
        patient.setEstimateDueDate(calendar.getTime());
        RCTRegistrationConfirmation confirmation = service.register(patient, user(), facility(11120));
        assertFailedConfirmation(confirmation, RCTError.RCT_CONTROL_GROUP_NOT_FOUND);
    }

    @Test
    public void shouldReturnRCTPatientForPatientRegisteredInRCT() {
        assertNotNull(service.getRCTPatient(1234567));
    }

    @Test
    public void shouldReturnNullForPatientNotRegisteredInRCT() {
        assertNull(service.getRCTPatient(1234568));
    }

    private void assertFailedConfirmation(RCTRegistrationConfirmation confirmation, String error) {
        assertTrue(confirmation.getErrors());
        assertEquals(error, confirmation.getText());
    }

    private User user() {
        return contextService.getUserService().getUser(1);
    }

    private RCTFacility facility(Integer id) {
        return service.getRCTFacilityById(id);
    }

}
