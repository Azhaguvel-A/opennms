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

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.opennms.protocols.wmi.wbem.OnmsWbemFlagReturnEnum;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.jinterop.OnmsWbemObjectImpl;
import org.opennms.protocols.wmi.wbem.jinterop.OnmsWbemObjectSetImpl;

/**
 * <P>
 * This is a low-level WMI client harnessing DCOM to communicate with remote agents.
 * The interface provided is similar but not identical to that of the SWbemServices
 * interface.
 * </P>
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class WmiClient implements IWmiClient {

    private JIComServer m_ComStub = null;
    private IJIComObject m_ComObject = null;
    private IJIDispatch m_Dispatch = null;
    private String m_Address = null;
    private JISession m_Session = null;
    private IJIDispatch m_WbemServices = null;

    private static final String WMI_CLSID = "76A6415B-CB41-11d1-8B02-00600806D9B6";
    private static final String WMI_PROGID = "WbemScripting.SWbemLocator";

    public WmiClient(String address) throws WmiException {
        JISystem.setAutoRegisteration(true);
        JISystem.getLogger().setLevel(Level.OFF);
        m_Address = address;
    }

    public OnmsWbemObjectSet performInstanceOf(String wmiClass) throws WmiException {
        try {
            // Execute the InstancesOf method on the remote SWbemServices object.
            JIVariant results[] = m_WbemServices.callMethodA("InstancesOf", new Object[]{new JIString(wmiClass), 0, JIVariant.OPTIONAL_PARAM()});
            IJIDispatch wOSd = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(wOSd);

        } catch (JIException e) {
            throw new WmiException("Failed to perform WMI operation (\\\\"
                    + wmiClass + ") : "
                    + e.getMessage(), e);
        }
    }

    public OnmsWbemObjectSet performExecQuery(String strQuery) throws WmiException {
        return performExecQuery(strQuery, "WQL", OnmsWbemFlagReturnEnum.wbemFlagReturnImmediately.getReturnFlagValue());
    }
    
    public OnmsWbemObjectSet performExecQuery(String strQuery,
                                              String strQueryLanguage,
                                              Integer flags) throws WmiException {
        try {
            JIVariant results[] = m_WbemServices.callMethodA("ExecQuery", new Object[]{new JIString(strQuery), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM()});
            IJIDispatch wOSd = (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(wOSd);
        } catch(JIException e) {
            throw new WmiException("Failed to execute query '" + strQuery + "': " + e.getMessage(), e);
        }
    }

    public OnmsWbemObject performWmiGet(String strObjectPath) throws WmiException {
        try {
            JIVariant results[] = m_WbemServices.callMethodA("Get", new Object[]{new JIString(strObjectPath), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            IJIDispatch obj_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectImpl(obj_dsp);
        } catch (JIException e) {
            throw new WmiException("Failed to perform get '" + strObjectPath + "': " + e.getMessage(), e);
        }
    }

    public OnmsWbemObjectSet performSubclassOf(String strSuperClass) throws WmiException {
        try {
            JIVariant results[] = m_WbemServices.callMethodA("SubclassesOf", new Object[]{new JIString(strSuperClass), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            IJIDispatch objset_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
            
            return new OnmsWbemObjectSetImpl(objset_dsp);
        } catch (JIException e) {
            throw new WmiException("Failed to perform SubclassesOf '" + strSuperClass + "': " + e.getMessage(), e);
        }
    }

    public OnmsWbemObjectSet performSubclassOf() throws WmiException {
        try {
            JIVariant results[] = m_WbemServices.callMethodA("SubclassesOf", new Object[]{ JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            IJIDispatch objset_dsp = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

            return new OnmsWbemObjectSetImpl(objset_dsp);
        } catch (JIException e) {
            throw new WmiException("Failed to perform SubclassesOf: " + e.getMessage(), e);
        }
    }

    public static Object convertToNativeType(JIVariant type) throws WmiException {
        try {
            if (type.isArray()) {
                ArrayList<Object> objs = new ArrayList<Object>();
                Object [] array = (Object[])type.getObjectAsArray().getArrayInstance();

                for (Object element : array) {
                    objs.add(convertToNativeType((JIVariant)element));
                }
                
                return objs;
            }

            switch (type.getType()) {
                case JIVariant.VT_NULL:
                    return null;
                case JIVariant.VT_BSTR:
                    return type.getObjectAsString().getString();
                case JIVariant.VT_I2: // sint16
                    return type.getObjectAsShort();
                case JIVariant.VT_I4:
                    return type.getObjectAsInt();
                case JIVariant.VT_UI1: // uint8 (convert to Java Number)
                    return type.getObjectAsUnsigned().getValue();
                case JIVariant.VT_BOOL:
                    return type.getObjectAsBoolean();
                case JIVariant.VT_DECIMAL:
                    return type.getObjectAsFloat();
                case JIVariant.VT_DATE:
                    return type.getObjectAsDate();
                default:
                    throw new WmiException("Unknown type presented ("
                            + type.getType() + "), defaulting to Object: "
                            + type.toString());
            }
        } catch (JIException e) {
            throw new WmiException(
                    "Failed to conver WMI type to native object: "
                            + e.getMessage(), e);
        }
    }

    public void connect(String domain, String username, String password)
            throws WmiException {
        try {

            m_Session = JISession.createSession(domain, username, password);
            m_Session.useSessionSecurity(true);
            m_Session.setGlobalSocketTimeout(5000);

            m_ComStub = new JIComServer(JIProgId.valueOf(WMI_PROGID), m_Address, m_Session);

            IJIComObject unknown = m_ComStub.createInstance();
            m_ComObject = unknown.queryInterface(WMI_CLSID);

            // This will obtain the dispatch interface
            m_Dispatch = (IJIDispatch) JIObjectFactory.narrowObject(
                    m_ComObject.queryInterface(IJIDispatch.IID));
            JIVariant results[] = m_Dispatch.callMethodA("ConnectServer",new Object[]{new JIString(m_Address),JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM()
                            ,JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM(),0,JIVariant.OPTIONAL_PARAM()});


            m_WbemServices = (IJIDispatch) JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());

        } catch (JIException e) {
            throw new WmiException("Failed to connect to host '" + m_Address
                    + "': " + e.getMessage(), e);
        } catch (UnknownHostException e) {
            throw new WmiException("Unknown host '" + m_Address
                    + "'. Failed to connect to WMI agent.", e);
        }
    }

    public void disconnect() throws WmiException {
        try {
            JISession.destroySession(m_Session);
        } catch (JIException e) {
            throw new WmiException("Failed to destroy J-Interop session: "
                    + e.getMessage(), e);
        }
    }

    public static Date convertWmiDate(String dateStr) throws ParseException {
        DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss.ssssss+000");

        return fmt.parse(dateStr);
    }

    // TODO This needs to be completed.
    @SuppressWarnings("unused")
    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    // TODO this needs to be completed.
    @SuppressWarnings("unused")
    private static boolean isDate(String str) {
        DateFormat df = DateFormat.getDateInstance();
        Date dtm;

        // Parse the date.
        try {
            dtm = df.parse(str);
        } catch (ParseException e) {
            return false;
        }

        // If we caught an exception, we reset to null.
        if (dtm == null) {
            return false;
        }
        // If not null, then we parsed successfully.
        else {
            return true;
        }
    }
}
