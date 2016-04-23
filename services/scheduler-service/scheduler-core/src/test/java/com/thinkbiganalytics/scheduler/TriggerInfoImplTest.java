package com.thinkbiganalytics.scheduler;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.TriggerInfo;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo;
import com.thinkbiganalytics.scheduler.support.JavaBeanTester;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by matthutton on 3/11/16.
 */
public class TriggerInfoImplTest {

    private TriggerInfo trigger;
    private Date today;

    @Before
    public void setUp() throws Exception {

        today = new Date();

        trigger = new DefaultTriggerInfo(Mockito.mock(JobIdentifier.class), Mockito.mock(TriggerIdentifier.class));
        trigger.setJobIdentifier(Mockito.mock(JobIdentifier.class));
        trigger.setState(TriggerInfo.TriggerState.BLOCKED);
        trigger.setTriggerClass(Object.class);
        trigger.setTriggerIdentifier(Mockito.mock(TriggerIdentifier.class));
    }

    @Test
    public void test() throws Exception {
        JavaBeanTester.test(DefaultTriggerInfo.class, "jobIdentifier", "state", "triggerClass", "triggerIdentifier");
        assertNotNull(trigger.getState());
        assertNotNull(trigger.getJobIdentifier());
        assertNotNull(trigger.getState());
        assertNotNull(trigger.getTriggerClass());
        assertNotNull(trigger.getTriggerIdentifier());
    }


}