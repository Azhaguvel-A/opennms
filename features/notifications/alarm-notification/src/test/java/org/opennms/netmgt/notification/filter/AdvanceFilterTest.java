package org.opennms.netmgt.notification.filter;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.notification.NBIAlarm;
import org.opennms.netmgt.notification.parser.Errorhandling;
import org.opennms.netmgt.notification.parser.Script;


public class AdvanceFilterTest {

	@Test
	@Ignore
	public void testCallAdvanceFilter() {
		new DroolsFileLoader();
		AdvanceFilter advanceFilter = new AdvanceFilter();
		NBIAlarm nbiAlarm = new NBIAlarm();
		nbiAlarm.setAlarmid("1");
		Script script = new Script();
		Errorhandling errorhandling = new Errorhandling();
		errorhandling.setEnable(false);
		script.setErrorhandling(errorhandling);

		Assert.assertNotNull(advanceFilter.callAdvanceFilter(nbiAlarm, "CBUNotification",
				"alarmXML", script));
	}

}
