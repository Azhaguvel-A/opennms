/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 24, 2008  - rdk@krupczak.org
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/** 
   XmpCollectionAttribute is an actual data point collected via XMP;
   what this means in English is that we've finally arrived at an
   actual Xmp variable -- something that has a MIB object name, type,
   and value.
   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
   @version $Id: XmpCollectionAttribute.java 38 2008-07-24 13:39:32Z rdk $
 **/

package org.opennms.netmgt.collectd;

import org.krupczak.Xmp.Xmp;
import org.krupczak.Xmp.XmpVar;

public class XmpCollectionAttribute extends AbstractCollectionAttribute 
implements CollectionAttribute 
{
    /* class variables and methods *********************** */

    /* instance variables ******************************** */
    private XmpVar aVar;
    private String alias;
    private XmpCollectionResource resource;
    private CollectionAttributeType attribType;

    /* constructors  ************************************* */
    XmpCollectionAttribute() { aVar = null; }

    XmpCollectionAttribute(XmpVar aVar, String alias, XmpCollectionResource res)
    {
        this.aVar = aVar;
        this.alias = alias;
        this.resource = res;
    }

    XmpCollectionAttribute(XmpCollectionResource res, 
                           CollectionAttributeType attribType, 
                           String alias, XmpVar aVar) 
                           {
        this(aVar,alias,res);
        this.attribType = attribType;
                           }

    /* private methods *********************************** */

    /* public methods ************************************ */

    public CollectionAttributeType getAttributeType() { return attribType; }

    public void setAttributeType(CollectionAttributeType attribType)
    {
        this.attribType = attribType;
    }

    public String getName() 
    { 
        return new String(alias);
    }

    public CollectionResource getResource() { return resource; }

    public String getNumericValue() { return aVar.getValue(); }

    public String getType() { return Xmp.syntaxToString(aVar.xmpSyntax); }

    public boolean shouldPersist(ServiceParameters params) { return true; }

    //public void visit(CollectionSetVisitor visitor) { super(visitor); }

    public String toString() 
    { 
        return "XmpCollectionAttribute "+alias+"="+aVar.getValue()+" attribType="+attribType; 
    }

    public String getStringValue() { return aVar.getValue(); }

}
