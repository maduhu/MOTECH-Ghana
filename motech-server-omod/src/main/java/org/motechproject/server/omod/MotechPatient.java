package org.motechproject.server.omod;

import org.motechproject.server.util.MotechConstants;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

public class MotechPatient {

    private Patient patient;

    public MotechPatient(Patient patient){
        this.patient = patient;
    }

    public String getMotechId(){
        if(patient != null){
            PatientIdentifier patientId = patient
                    .getPatientIdentifier(MotechConstants.PATIENT_IDENTIFIER_MOTECH_ID);
            if (patientId != null) {
                return patientId.getIdentifier();
            }
        }
        return null;
    }
}
