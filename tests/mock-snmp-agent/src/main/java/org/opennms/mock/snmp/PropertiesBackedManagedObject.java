/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


package org.opennms.mock.snmp;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.snmp4j.agent.DefaultMOScope;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.springframework.core.io.Resource;

/**
 * <p>PropertiesBackedManagedObject class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class PropertiesBackedManagedObject implements ManagedObject, MockSnmpMOLoader, Updatable, MOAccess {
    
    
    private TreeMap<OID, Object> m_vars = null;
    
    private MOScope m_scope = null;
    
    /** {@inheritDoc} */
    public List<ManagedObject> loadMOs(Resource moFile) {
        Properties props = PropsMockSnmpMOLoaderImpl.loadProperties(moFile);

        m_vars = new TreeMap<OID, Object>();

        for(Entry<Object, Object> e : props.entrySet()) {
            String key = (String)e.getKey();
            Object value = e.getValue();
            try {
                m_vars.put(new OID(key), value);
            } catch (Throwable ex) {
                // Catch any malformed OIDs and create a more descriptive error message
                IllegalArgumentException nfe = new IllegalArgumentException("Could not load OID value: [" + key + "] [" + value + "]");
                nfe.initCause(ex);
                throw nfe;
            }
        }


        m_scope = new DefaultMOScope(m_vars.firstKey(),
                                     true,
                                     m_vars.lastKey(),
                                     true
        );
        
        return Collections.singletonList((ManagedObject)this);
    }
    
    /** {@inheritDoc} */
    public void cleanup(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    /** {@inheritDoc} */
    public void commit(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    /** {@inheritDoc} */
    public OID find(MOScope range) {
        if (!m_scope.isOverlapping(range)) {
            return null;
        }
        
        OID first = range.getLowerBound();
        
        if (range.isLowerIncluded()) {
            first = first.successor();
        }

        SortedMap<OID, Object> tail = m_vars.tailMap(first);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey(); // skip the leading '.'
    }
    
    /**
     * <p>findNextOid</p>
     *
     * @param given a {@link org.snmp4j.smi.OID} object.
     * @return a {@link org.snmp4j.smi.OID} object.
     */
    public OID findNextOid(OID given) {
        
        OID next = given.successor();
        
        SortedMap<OID, Object> tail = m_vars.tailMap(next);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey();
    }
    
    private Variable findValueForOID(OID oid) {
        Object val = m_vars.get(oid);
        if (val == null) {
            return null;
        } else if (val instanceof Variable) {
            return (Variable)val;
        }
        return PropsMockSnmpMOLoaderImpl.getVariableFromValueString(oid.toString(), (String)val);
    }

    /** {@inheritDoc} */
    public void get(SubRequest request) {
        getVariable(request, request.getVariableBinding().getOid());
    }

    private void getVariable(SubRequest request, OID oid) {
        Variable value = findValueForOID(oid);
        VariableBinding vb = request.getVariableBinding();
        vb.setOid(oid);
        vb.setVariable(value == null ? Null.noSuchObject : value);
        request.completed();
    }

    /**
     * <p>getScope</p>
     *
     * @return a {@link org.snmp4j.agent.MOScope} object.
     */
    public MOScope getScope() {
        return m_scope;
    }

    /** {@inheritDoc} */
    public boolean next(SubRequest request) {
        OID nextOid = findNextOid(request.getVariableBinding().getOid());
        if (nextOid == null) {
            return false;
        }
        getVariable(request, nextOid);
        return true;
    }

    /** {@inheritDoc} */
    public void prepare(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    /** {@inheritDoc} */
    public void undo(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    /** {@inheritDoc} */
    public void updateValue(OID oid, Variable value) {
        m_vars.put(oid, value);
    }

    /**
     * <p>isAccessibleForCreate</p>
     *
     * @return a boolean.
     */
    public boolean isAccessibleForCreate() {
        return false;
    }

    /**
     * <p>isAccessibleForNotify</p>
     *
     * @return a boolean.
     */
    public boolean isAccessibleForNotify() {
        return false;
    }

    /**
     * <p>isAccessibleForRead</p>
     *
     * @return a boolean.
     */
    public boolean isAccessibleForRead() {
        return true;
    }

    /**
     * <p>isAccessibleForWrite</p>
     *
     * @return a boolean.
     */
    public boolean isAccessibleForWrite() {
        return false;
    }

}
