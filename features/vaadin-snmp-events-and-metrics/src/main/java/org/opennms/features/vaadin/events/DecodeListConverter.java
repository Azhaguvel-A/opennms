/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.vaadin.addon.customfield.PropertyConverter;

/**
 * The Varbind's Decode List Converter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class DecodeListConverter extends PropertyConverter<ArrayList<Decode>, String> {

    /**
     * The Class DecodeList.
     */
    public class DecodeList extends ArrayList<Decode> {}

    /**
     * Instantiates a new decode list converter.
     */
    public DecodeListConverter() {
        super(DecodeList.class);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.PropertyConverter#format(java.lang.Object)
     */
    @Override
    public String format(ArrayList<Decode> propertyValue) {
        final List<String> values = new ArrayList<String>();
        for (Decode d : propertyValue) {
            values.add(d.getVarbindvalue() + '=' + d.getVarbinddecodedstring());
        }
        return StringUtils.join(values, ',');
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.PropertyConverter#parse(java.lang.Object)
     */
    @Override
    public ArrayList<Decode> parse(String fieldValue) {
        ArrayList<Decode> list = new ArrayList<Decode>();
        for (String s : fieldValue.split(",")) {
            String[] parts = s.split("=");
            Decode d = new Decode();
            d.setVarbindvalue(parts[0].trim());
            d.setVarbinddecodedstring(parts[1].trim());
            list.add(d);
        }
        return list;
    }

}
