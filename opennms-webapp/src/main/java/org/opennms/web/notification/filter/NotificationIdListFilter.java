/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificationIdListFilter implements Filter {
    
    private int[] m_notificationIds;
    
    public NotificationIdListFilter(int[] notificationIds){
        m_notificationIds = notificationIds;
    }
    
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
        for(int i = 0; i < m_notificationIds.length; i++){
            ps.setInt(parameterIndex + i, m_notificationIds[i]);
        }
        return m_notificationIds.length;
    }

    public String getDescription() {
        StringBuilder buf = new StringBuilder("notificationId in ");
        appendIdList(buf);
        return buf.toString();
    }

    public String getParamSql() {
        StringBuilder buf = new StringBuilder(m_notificationIds.length*3 + 20);
        
        buf.append(" NOTIFICATIONS.NOTIFYID IN ");
        
        buf.append('(');
        for(int i = 0; i < m_notificationIds.length; i++){
            if(i != 0){
                buf.append(", ");
            }
            buf.append('?');
        }
        
        buf.append(')');
        
        return buf.toString();
    }

    public String getSql() {
        StringBuilder buf = new StringBuilder(m_notificationIds.length*5 + 20);
        
        buf.append(" NOTIFICATIONS.NOTIFYID IN ");
        
        appendIdList(buf);
        
        return buf.toString();
    }

    public String getTextDescription() {
        return getDescription();
    }
    
    private void appendIdList(StringBuilder buf) {
        buf.append("(");
        for(int i = 0; i < m_notificationIds.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(m_notificationIds[i]);
        }
        
        buf.append(")");
    }

}
