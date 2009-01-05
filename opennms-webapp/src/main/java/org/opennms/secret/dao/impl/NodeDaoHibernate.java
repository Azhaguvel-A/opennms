//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.secret.dao.impl;


import java.util.Collection;

import org.opennms.secret.dao.NodeDao;
import org.opennms.secret.model.Node;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Ted Kazmark
 * @author David Hustace
 *
 */
public class NodeDaoHibernate extends HibernateDaoSupport implements NodeDao {

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#initialize(java.lang.Object)
     */
    public void initialize(Object obj) {
        getHibernateTemplate().initialize(obj);

    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#getNode(java.lang.Long)
     */
    public Node getNode(Long id) {
        return (Node)getHibernateTemplate().load(Node.class, id);
    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#createNode(org.opennms.secret.model.Node)
     */
    public void createNode(Node node) {
        getHibernateTemplate().save(node);
    }

    public Collection getInterfaceCollection(Node node) {
        return getHibernateTemplate().find("from NodeInterface interface where interface.nodeId = ?", node.getNodeId());
     
    }

    public Collection<Node> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

}
