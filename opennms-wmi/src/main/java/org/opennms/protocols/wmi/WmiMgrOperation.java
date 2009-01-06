//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum WmiMgrOperation {
	EQ(0), NEQ(1), GT(2), LT(3);

	private int m_OperationType;

	WmiMgrOperation(int opType) {
		m_OperationType = opType;
	}

	public int getOpNumber() {
		return (m_OperationType);
	}

	public boolean compareString(Object comp1, String comp2)
			throws WmiException {
		if (comp1 instanceof String) {

			try {
				DateFormat fmt2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				Date date1 = WmiClient.convertWmiDate((String) comp1);
				Date date2 = fmt2.parse(comp2);

				return compareToDate(date1, date2);
			} catch (ParseException e) {
				// ignore this exception and continue with string comparison.
			}
			return compareToString((String) comp1, comp2);
		} else if (comp1 instanceof Integer) {
			Integer compInt1 = (Integer) comp1;
			Integer compInt2 = Integer.parseInt(comp2);

			return compareToInteger(compInt1, compInt2);
		} else if (comp1 instanceof Boolean) {
			Boolean bool1 = (Boolean) comp1;
			Boolean bool2 = Boolean.parseBoolean(comp2);

			return compareToBoolean(bool1, bool2);
		} else if (comp1 instanceof Float) {
			Float fl1 = (Float) comp1;
			Float fl2 = Float.parseFloat(comp2);

			return compareToFloat(fl1, fl2);
		} else if (comp1 instanceof Date) {
			DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date date1 = (Date) comp1;
			Date date2;
			try {
				date2 = fmt.parse(comp2);
			} catch (ParseException e) {
				throw new WmiException("Parsing date '" + comp2 + "' failed: "
						+ e.getMessage(), e);
			}

			return compareToDate(date1, date2);

		}

		// No root type found. Return false. Maybe instead we should throw an exception.
		// A potential bug to fix?
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToString(String comp1, String comp2) {
		switch (this) {
		case EQ:
			return (comp2.equals(comp1));
		case NEQ:
			return !(comp2.equals(comp1));
		case GT:
			return (comp2.length() < ((String) comp1).length());
		case LT:
			return (comp2.length() > ((String) comp1).length());
		}

		// catch-all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToInteger(Integer comp1, Integer comp2) {
		switch (this) {
		case EQ:
			if (comp2.compareTo(comp1) == 0) {
				return true;
			} else {
				return false;
			}
		case NEQ:
			if (comp2.compareTo(comp1) != 0) {
				return true;
			} else {
				return false;
			}
		case GT:
			if (comp2.compareTo(comp1) < 0) {
				return true;
			} else {
				return false;
			}
		case LT:
			if (comp2.compareTo(comp1) > 0) {
				return true;
			} else {
				return false;
			}
		}

		// catch all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToBoolean(Boolean bool1, Boolean bool2) {
		switch (this) {
		case EQ:
			return bool1.equals(bool2);
		case NEQ:
		case GT:
		case LT:
			return !(bool1.equals(bool2));
		}
		
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToFloat(Float comp1, Float comp2) {
		switch (this) {
		case EQ:
			if (comp2.compareTo(comp1) == 0) {
				return true;
			} else {
				return false;
			}
		case NEQ:
			if (comp2.compareTo(comp1) != 0) {
				return true;
			} else {
				return false;
			}
		case GT:
			if (comp2.compareTo(comp1) < 0) {
				return true;
			} else {
				return false;
			}
		case LT:
			if (comp2.compareTo(comp1) > 0) {
				return true;
			} else {
				return false;
			}
		}

		// catch all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToDate(Date date1, Date date2) {
		switch (this) {
		case EQ:
			if (date1.equals(date2)) {
				return true;
			}
		case NEQ:
			if (!(date1.equals(date2))) {
				return true;
			}
		case GT:
			if (date1.after(date2)) {
				return true;
			}
		case LT:
			if (date1.before(date2)) {
				return true;
			}
		}

		// catch-all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}
}
