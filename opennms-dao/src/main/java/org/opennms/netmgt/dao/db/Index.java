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
package org.opennms.netmgt.dao.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class Index {

    private String m_name;
    private String m_table;
    private String m_using;
    private List<String> m_columns;
    private boolean m_unique;
    private String m_where;

    private static Pattern m_pattern =
            Pattern.compile("(?i)create"
                            + "(\\s+unique)?"
                            + "\\s+index\\s+(\\S+)"
                            + "\\s+on\\s+(\\S+)"
                            + "(?:\\s+USING\\s+(\\S+))?"
                            + "\\s*\\(([^)]+)\\)"
                            + "(?:\\s+WHERE\\s+(.*?))?"
                            + "\\s*(?:;|$)");

    public Index(String name, String table, String using, List<String> columns,
            boolean unique, String where) {
        m_name = name;
        m_table = table;
        m_using = using;
        m_columns = columns;
        m_unique = unique;
        m_where = where;
    }
    
    public static Index findIndexInString(String create) {
        Matcher m = m_pattern.matcher(create.toString());
        if (!m.find()) {
            return null;
        }
        
        boolean unique = (m.group(1) != null);
        String name = m.group(2);
        String table = m.group(3);
        String using = m.group(4);
        String columnList = m.group(5);
        String where = m.group(6);
        
        String[] columns = columnList.split("\\s*,\\s*");

        return new Index(name, table, using, Arrays.asList(columns), unique,
                         where);
    }
    
    public boolean isOnDatabase(Connection connection) throws SQLException {
        boolean exists;
    
        Statement st = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT relname FROM pg_class "
                                 + "WHERE relname = '" + m_name.toLowerCase()
                                 + "'");
            exists = rs.next();
        } finally {
            if (rs != null) {
                rs.close();
            }
            st.close();
        }
        
        return exists;
    }

    public void removeFromDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP INDEX " + getName());
        } finally{
            st.close();
        }
    }
    
    public void addToDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute(getSql());
        } finally{
            st.close();
        }
    }
    
    public String getSql() {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE ");
        if (m_unique) {
            sql.append("UNIQUE ");
            
        }
        sql.append("INDEX ");
        sql.append(m_name);
        sql.append(" ON ");
        sql.append(m_table);
        if (m_using != null) {
            sql.append(" USING ");
            sql.append(m_using);
        }
        sql.append(" ( ");
        sql.append(StringUtils.collectionToDelimitedString(m_columns, ", "));
        sql.append(" )");
        if (m_where != null) {
            sql.append(" WHERE ");
            sql.append(m_where);
        }
        
        return sql.toString();
    }


    public String getName() {
        return m_name;
    }

    public String getTable() {
        return m_table;
    }
    
    public boolean isUnique() {
        return m_unique;
    }
    
    public List<String> getColumns() {
        return m_columns;
    }

    public String getIndexUniquenessQuery() throws Exception {
        String firstColumn = getColumns().get(0);
        String columnList = StringUtils.collectionToDelimitedString(getColumns(), ", ");
        
        /*
         * E.g. select * from foo where (a, b) in (select a, b from foo
         *      group by a, b having count(a) > 1 order by a, b);
         */
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM " + getTable() + " WHERE ( "
                   + columnList + " ) IN ( SELECT "  + columnList + " FROM "
                   + getTable() + " GROUP BY " + columnList + " HAVING count("
                   + firstColumn + ") > 1");
        if (m_where != null) {
            sql.append(" AND ( " + m_where + " )");
        }
        sql.append(" ORDER BY " + columnList + " ) "
                   + "ORDER BY " + columnList);
        
        return sql.toString();

        /*
        List<String> whereComponents =
            new ArrayList<String>(getColumns().size() + 2);
        for (String column : getColumns()) {
            whereComponents.add("a." + column + " = b." + column);
        }

        String lowerTable = getTable().toLowerCase();
        if ("snmpinterface".equals(lowerTable)) {
            whereComponents.add("a.ipAddr != b.ipAddr");
        } else if ("ifservices".equals(lowerTable)) {
            whereComponents.add("a.lastGood != b.lastGood");
        } else {
            return null;
        }
        
        if (m_where != null) {
            whereComponents.add("( " + m_where + " )");
        }
        return "SELECT DISTINCT a.* FROM "
            + getTable() + " a, " + getTable() + " b WHERE "
            + Installer.join(" AND ", whereComponents);
            */
    }

}
