/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Apr 09: Expose data source indexes in threshold-derived events  - jeffg@opennms.org
 * 2008 Feb 10: Organize imports. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.threshd.ThresholdEvaluatorRelativeChange.ThresholdEvaluatorStateRelativeChange;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.ThrowableAnticipator;

public class ThresholdEvaluatorRelativeChangeTest extends AbstractThresholdEvaluatorTest {

    public void testConstructor() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       new ThresholdEvaluatorStateRelativeChange(wrapper);
    }
    
    public void testConstructorThresholdNull() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold argument cannot be null"));
        
        try {
            new ThresholdEvaluatorStateRelativeChange(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testEvaluateOnce() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
    }
    
    public void testEvaluateTwiceNoTrigger() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
    }
    
    public void testEvaluateTwiceTriggerLowBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(8.0));
    }
    
    public void testEvaluateTwiceTriggerLowEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(9.0));
    }
    
    public void testEvaluateTwiceNoTriggerLowAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(0.9);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(9.5));
    }
    
    public void testEvaluateTwiceTriggerHighAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(12.0));
    }
    
    public void testEvaluateTwiceTriggerHighEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(11.0));
    }
    
    public void testEvaluateTwiceNoTriggerHighBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.5));
    }
    
    public void testEvaluateTwiceNoTriggerHighFirstZero() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger on first evaluate", Status.NO_CHANGE, evaluator.evaluate(0.0));
        assertEquals("should not trigger on second evaluate", Status.NO_CHANGE, evaluator.evaluate(1000.0));
    }

    public void testEvaluateThriceTriggerHighFirstZero() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger on first evaluate", Status.NO_CHANGE, evaluator.evaluate(0.0));
        assertEquals("should not trigger on second evaluate", Status.NO_CHANGE, evaluator.evaluate(1000.0));
        assertEquals("should trigger on third evaluate", Status.TRIGGERED, evaluator.evaluate(1200.0));
    }
    
    public void testGetEventForStateNoChange() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);

        assertNull("should not have created an event", evaluator.getEventForState(Status.NO_CHANGE, new Date(), 10.0, null));
    }
    
    public void testGetEventForStateTriggered() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);

        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(8.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(10.0));
        
        // Do it once with a null instance
        Event event = evaluator.getEventForState(Status.TRIGGERED, new Date(), 10.0, null);
        assertNotNull("should have created an event", event);
        assertEquals("UEIs should be the same", EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
        assertNotNull("event should have parms", event.getParms());
        parmPresentAndValueNonNull(event, "instance");
        parmPresentWithValue(event, "value", "10.0");
        parmPresentWithValue(event, "previousValue", "8.0");
        parmPresentWithValue(event, "multiplier", "1.1");
        
        // And again with a non-null instance
        event = evaluator.getEventForState(Status.TRIGGERED, new Date(), 10.0, new MockCollectionResourceWrapper("testInstance"));
        assertNotNull("should have created an event", event);
        assertEquals("UEIs should be the same", EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
        assertNotNull("event should have parms", event.getParms());
        parmPresentWithValue(event, "instance", "testInstance");
        parmPresentWithValue(event, "value", "10.0");
        parmPresentWithValue(event, "previousValue", "8.0");
        parmPresentWithValue(event, "multiplier", "1.1");
    }
    
    public void testGetEventForStateDefaultUEIS() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateRelativeChange item = new ThresholdEvaluatorStateRelativeChange(wrapper);
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, null);
        assertEquals("UEI should be the relativeChangeThresholdTriggerd", EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
    }

    public void testGetEventForStateCustomUEIS() {
        String triggeredUEI="uei.opennms.org/custom/relativeChangeThresholdTriggered";
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        threshold.setTriggeredUEI(triggeredUEI);
        
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateRelativeChange item = new ThresholdEvaluatorStateRelativeChange(wrapper);
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, null);
        assertEquals("UEI should be the uei.opennms.org/custom/relativeChangeThresholdTriggered", triggeredUEI, event.getUei());
       
    }
    
    public void testNegativeNumberTriggers() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(-10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(-12.0));   	
    }
    
    public void testNegativeNumberNotTriggers() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(1.1);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(wrapper);
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(-10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(-10.5));   	
    }
    	 
    public void testNegativeValueNoChange() {
    	Threshold threshold = new Threshold();
		threshold.setType("relativeChange");
		threshold.setDsName("ds-name");
		threshold.setDsType("ds-type");
		threshold.setValue(0.9);
		threshold.setRearm(0.5);
		threshold.setTrigger(1);
		ThresholdConfigWrapper wrapper = new ThresholdConfigWrapper(threshold);
		ThresholdEvaluatorStateRelativeChange evaluator = new ThresholdEvaluatorStateRelativeChange(
				wrapper);

		assertEquals("should not trigger", Status.NO_CHANGE, evaluator
				.evaluate(-10.0));
		assertEquals("should not trigger", Status.NO_CHANGE, evaluator
				.evaluate(-10.0));
    }


    @SuppressWarnings("unchecked")
    private static Collection<Parm> getParmCollection(Event event) {
        if (event.getParms() == null) {
            return new ArrayList<Parm>();
        }
        
        return event.getParms().getParmCollection();
    }
}
