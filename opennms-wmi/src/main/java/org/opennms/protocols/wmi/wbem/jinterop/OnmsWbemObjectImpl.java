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
package org.opennms.protocols.wmi.wbem.jinterop;

import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.common.JIException;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;

public class OnmsWbemObjectImpl implements OnmsWbemObject {
    private IJIDispatch wbemObjectDispatch;

    public OnmsWbemObjectImpl(IJIDispatch wbemObjectDispatch) {
        this.wbemObjectDispatch = wbemObjectDispatch;
    }

    public OnmsWbemObjectImpl wmiExecMethod(String methodName, List params, List namedValueSet) {
        return null; // TODO IMPLEMENT THIS METHOD
    }

    public List<String> wmiInstances() {
        return null; // TODO IMPLEMENT THIS METHOD
    }

    public String wmiPut() {
        return ""; // TODO IMPLEMENT THIS METHOD
    }

    public OnmsWbemMethodSet getWmiMethods() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            IJIComObject methodComObject = wbemObjectDispatch.get("Methods_").getObjectAsComObject();
            IJIDispatch methodsSet_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(methodComObject);

            return new OnmsWbemMethodSetImpl(methodsSet_dispatch);
        } catch (JIException e) {
            throw new WmiException("Failed to retrieve list of methods: " + e.getMessage(), e);
        }
    }

    public OnmsWbemObjectPath getWmiPath() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            IJIComObject pathComObject = wbemObjectDispatch.get("Path_").getObjectAsComObject();
            IJIDispatch path_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(pathComObject);

            return new OnmsWbemObjectPathImpl(path_dispatch);
        } catch (JIException e) {
            throw new WmiException("Failed to retrieve object path: " + e.getMessage(), e);
        }
    }

    public String getWmiObjectText() throws WmiException {
        try {
            JIVariant variant = (wbemObjectDispatch.callMethodA("GetObjectText_", new Object[]{1}))[0];

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath GetObjectText_ attribute: " + e.getMessage(), e);
        }
    }

    public OnmsWbemPropertySet getWmiProperties() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            IJIComObject propsSetComObject = wbemObjectDispatch.get("Properties_").getObjectAsComObject();
            IJIDispatch propSet_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(propsSetComObject);

            return new OnmsWbemPropertySetImpl(propSet_dispatch);
        } catch (JIException e) {
            throw new WmiException("Failed to retrieve object property set: " + e.getMessage(), e);
        }
    }

}
