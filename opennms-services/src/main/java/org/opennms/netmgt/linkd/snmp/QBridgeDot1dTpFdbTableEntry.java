//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 *<P>The QbridgeDot1dTpFdbTableEntry class is designed to hold all the MIB-II
 * information for one entry in the MIB II dot1dBridge.dot1dTp.dot1dTpFdbTable. 
 * The table effectively contains a list of these entries, each entry having information
 * about bridge forwarding table.</P>
 *
 * <P>This object is used by the Dot1dTpFdbTable to hold infomation
 * single entries in the table. See the Dot1dTpFdbTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 *
 * @see Dot1dTpFdbTable  
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class QBridgeDot1dTpFdbTableEntry extends SnmpTableEntry {
	// Lookup strings for specific table entries
	//

	public final static String FDB_ADDRESS = "dot1dTpFdbAddress";

	public final static String FDB_ADDRESS_OID = ".1.3.6.1.2.1.17.7.1.2.2.1.1";

	public final static String FDB_PORT = "dot1dTpFdbPort";

	public final static String FDB_STATUS = "dot1dTpFdbStatus";

	private boolean hasFdbAddressFromBase = false;
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the Dot1dTpFbTable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] ms_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		ms_elemList = new NamedSnmpVar[3];
		int ndx = 0;

		/**
		 * A unicast MAC address for which the bridge has
		 * forwarding and/or filtering information.
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				FDB_ADDRESS, ".1.3.6.1.2.1.17.7.1.2.2.1.1", 1);
		
		/**
		 * Either the value '0', or the port number of the
 		 * port on which a frame having a source address
 		 * equal to the value of the corresponding instance
 		 * of dot1dTpFdbAddress has been seen. A value of
 		 * '0' indicates that the port number has not been
 		 * learned but that the bridge does have some
 		 * forwarding/filtering information about this
 		 * address (e.g. in the dot1dStaticTable).
 		 * Implementors are encouraged to assign the port
 		 * value to this object whenever it is learned even
 		 * for addresses for which the corresponding value of
 		 * dot1dTpFdbStatus is not learned(3).
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				FDB_PORT, ".1.3.6.1.2.1.17.7.1.2.2.1.2", 2);
		
		/**
		 * The status of this entry. The meanings of the
 		 * values are:
 		 * other(1) : none of the following. This would
 		 * include the case where some other
 		 * MIB object (not the corresponding
 		 * instance of dot1dTpFdbPort, nor an
 		 * entry in the dot1dStaticTable) is
 		 * being used to determine if and how
 		 * frames addressed to the value of
 		 * the corresponding instance of
 		 * dot1dTpFdbAddress are being
 		 * forwarded.
 		 * invalid(2) : this entry is not longer valid
 		 * (e.g., it was learned but has since
 		 * aged-out), but has not yet been
 		 * flushed from the table.
 		 * learned(3) : the value of the corresponding
 		 * instance of dot1dTpFdbPort was
 		 * learned, and is being used.
 		 * self(4) : the value of the corresponding
 		 * instance of dot1dTpFdbAddress
 		 * represents one of the bridge's
 		 * addresses. The corresponding
 		 * instance of dot1dTpFdbPort
 		 * indicates which of the bridge's
 		 * ports has this address.
		 * mgmt(5) : the value of the corresponding
 		 * instance of dot1dTpFdbAddress is
 		 * also the value of an existing
 		 * instance of dot1dStaticAddress.
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				FDB_STATUS, ".1.3.6.1.2.1.17.7.1.2.2.1.3", 3);
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the Bridge Forward table in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.2.1.17.7.1.2.2"; // start of table (GETNEXT)


	public QBridgeDot1dTpFdbTableEntry() {
		super(ms_elemList);
	}

	@Override
	public void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
		super.storeResult(base, inst, val);
		if (!SnmpObjId.get(FDB_ADDRESS_OID).isPrefixOf(base) && !hasFdbAddressFromBase) {
			int[] identifiers = inst.getIds();
			String mac = "";
			for (int i = identifiers.length-6; i<identifiers.length; i++) if (identifiers[i] >= 16 )
				mac+= Integer.toHexString(identifiers[i]);
			else
				mac+= "0"+Integer.toHexString(identifiers[i]);
			super.storeResult(SnmpObjId.get(FDB_ADDRESS_OID), inst, 
						SnmpUtils.getValueFactory().getOctetString(mac.getBytes()));
			hasFdbAddressFromBase = true;
		}
	}
	
	public String getQBridgeDot1dTpFdbAddress() {
		if (hasFdbAddressFromBase) return getDisplayString(QBridgeDot1dTpFdbTableEntry.FDB_ADDRESS);
		return getHexString(QBridgeDot1dTpFdbTableEntry.FDB_ADDRESS);
	}

	public int getQBridgeDot1dTpFdbPort() {
		Integer val = getInt32(QBridgeDot1dTpFdbTableEntry.FDB_PORT);
		if (val == null) return -1;
		return val;
	}

	public int getQBridgeDot1dTpFdbStatus() {
		Integer val = getInt32(QBridgeDot1dTpFdbTableEntry.FDB_STATUS);
		if (val == null) return -1;
		return val;
	}
}