//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.repository.ibatis;

import java.util.ArrayList;
import java.util.List;

import org.opennms.acl.model.NodeONMSDTO;
import org.opennms.acl.repository.ItemAclRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapClient;
/** 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Repository("categoryNodeRepository")
public class CategoryNodeAclRepositoryIbatis extends SqlMapClientTemplate implements ItemAclRepository {

    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("onmsSqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    public List<?> getItems() {
        return queryForList("selectCategoryNodes");
    }

    public List<?> getAuthorityItems(List<Integer> items) {
        return items.size() > 0 ? queryForList("selectAuthorityCategories", items) : new ArrayList<NodeONMSDTO>(0);
    }

    public List<?> getFreeItems(List<Integer> items) {
        return items.size() > 0 ? queryForList("selectFreeCategoryNodes", items) : queryForList("selectCategoryNodes");
    }
}