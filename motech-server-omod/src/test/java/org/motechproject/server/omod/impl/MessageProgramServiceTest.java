package org.motechproject.server.omod.impl;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.server.event.impl.ExpectedCareMessageProgram;
import org.motechproject.server.model.ExpectedCareMessageDetails;
import org.motechproject.server.svc.MessageProgramService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.*;

public class MessageProgramServiceTest  extends BaseModuleContextSensitiveTest {

    @Autowired
    MessageProgramService messageProgramService ;

    @Before
    public void setUp() throws Exception {
      executeDataSet("message-program-dataset.xml");
    }

    @Test
    public void shouldGetExpectedMessageCareProgram() {
        ExpectedCareMessageProgram program = (ExpectedCareMessageProgram) messageProgramService.program("Expected Care Message Program");
        assertNotNull(program);
        assertTrue(program.hasMessageCareDetails());
        ExpectedCareMessageDetails pncDetail = getPNCMessageDetail(program);
        assertNotNull(pncDetail);
        assertPostNatalConditionTimeMap(pncDetail);
    }

    private void assertPostNatalConditionTimeMap(ExpectedCareMessageDetails pncDetail) {
        Map<String,Integer> careTimeMap = pncDetail.getCareTimeMap();
        assertTrue(careTimeMap.keySet().size() == 3);
        assertEquals(new Integer(6),careTimeMap.get("PNC1"));
        assertEquals(new Integer(24),careTimeMap.get("PNC2"));
        assertEquals(new Integer(24),careTimeMap.get("PNC3"));
    }

    private ExpectedCareMessageDetails getPNCMessageDetail(ExpectedCareMessageProgram careMessageProgram) {
        for (ExpectedCareMessageDetails details : careMessageProgram.getCareMessageDetails()) {
               if(details.getName().contains("PNC"))return details;
        }
        return null;
    }
}
