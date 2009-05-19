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
package org.opennms.web.alarm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.alarm.AlarmFactory.AcknowledgeType;
import org.opennms.web.alarm.AlarmFactory.SortStyle;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.alarm.filter.AlarmIdListFilter;
import org.opennms.web.alarm.filter.Filter;
import org.opennms.web.alarm.filter.AlarmCriteria.AlarmCriteriaVisitor;
import org.opennms.web.alarm.filter.AlarmCriteria.BaseAlarmCriteriaVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * WebAlarmDao
 *
 * @author brozow
 */
public class JdbcWebAlarmRepository implements WebAlarmRepository {
    
    @Autowired
    SimpleJdbcTemplate m_simpleJdbcTemplate;
    
    private String getSql(final String selectClause, final AlarmCriteria criteria) {
        final StringBuilder buf = new StringBuilder(selectClause);

        criteria.visit(new AlarmCriteriaVisitor<RuntimeException>() {

            boolean first = true;

            public void and(StringBuilder buf) {
                if (first) {
                    buf.append(" WHERE ");
                    first = false;
                } else {
                    buf.append(" AND ");
                }
            }

            public void visitAckType(AcknowledgeType ackType) {
                and(buf);
                buf.append(ackType.getAcknowledgeTypeClause());
            }


            public void visitFilter(Filter filter) {
                and(buf);
                buf.append(filter.getParamSql());
            }

            public void visitSortStyle(SortStyle sortStyle) {
                buf.append(" ");
                buf.append(sortStyle.getOrderByClause());
            }

            public void visitLimit(int limit, int offset) {
                buf.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
            }

        });

        return buf.toString();
    }
    
    private PreparedStatementSetter paramSetter(final AlarmCriteria criteria, final Object... args) {
        return new PreparedStatementSetter() {
            int paramIndex = 1;
            public void setValues(final PreparedStatement ps) throws SQLException {
                for(Object arg : args) {
                    ps.setObject(paramIndex, args);
                    paramIndex++;
                }
                criteria.visit(new BaseAlarmCriteriaVisitor<SQLException>() {
                    @Override
                    public void visitFilter(Filter filter) throws SQLException {
                        paramIndex =+ filter.bindParam(ps, paramIndex);
                    }
                });
            }
        };
    }
    
    private static class AlarmMapper implements ParameterizedRowMapper<Alarm> {
        public Alarm mapRow(ResultSet rs, int rowNum) throws SQLException {
            Alarm alarm = new Alarm();
            alarm.id = rs.getInt("alarmID");
            alarm.uei = rs.getString("eventUei");
            alarm.dpName = rs.getString("dpName");

            // node id can be null, in which case nodeID will be 0
            alarm.nodeID = new Integer(rs.getInt("nodeID"));
            alarm.ipAddr = rs.getString("ipAddr");

            // This causes serviceID to be null if the column in the database is null
            alarm.serviceID = ((Integer) rs.getObject("serviceID"));
            alarm.reductionKey = rs.getString("reductionKey");
            alarm.count = rs.getInt("counter");
            alarm.severity = OnmsSeverity.get(rs.getInt("severity"));
            alarm.lastEventID = rs.getInt("lastEventID");
            alarm.firsteventtime = new Date(rs.getTimestamp("firsteventtime").getTime());
            alarm.lasteventtime = new Date(rs.getTimestamp("lasteventtime").getTime());
            alarm.description = rs.getString("description");
            alarm.logMessage = rs.getString("logmsg");
            alarm.operatorInstruction = rs.getString("OperInstruct");
            alarm.troubleTicket = rs.getString("TTicketID");
            
            Integer stateCode = (Integer) rs.getObject("TTicketState");
            for (TroubleTicketState state : TroubleTicketState.values()) {
                if (stateCode != null && state.ordinal() == stateCode.intValue()) {
                    alarm.troubleTicketState = state;
                }
            }

            alarm.mouseOverText = rs.getString("MouseOverText");
            alarm.suppressedUntil = new Date(rs.getTimestamp("suppressedUntil").getTime());
            alarm.suppressedUser = rs.getString("suppressedUser");
            alarm.suppressedTime = new Date(rs.getTimestamp("suppressedTime").getTime());
            alarm.acknowledgeUser = rs.getString("alarmAckUser");

            Timestamp alarmAckTime = rs.getTimestamp("alarmAckTime");
            if (alarmAckTime != null) {
                alarm.acknowledgeTime = new Date(alarmAckTime.getTime());
            }

            alarm.nodeLabel = rs.getString("nodeLabel");
            alarm.serviceName = rs.getString("serviceName");
            
            return alarm;
            
        }
    }
    
   
    public int countMatchingAlarms(AlarmCriteria criteria) {
        String sql = getSql("SELECT COUNT(ALARMID) as ALARMCOUNT FROM ALARMS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID)", criteria);
        return queryForInt(sql, paramSetter(criteria));
    }
    
    public int[] countMatchingAlarmsBySeverity(AlarmCriteria criteria) {
        String selectClause = "SELECT SEVERITY, COUNT(ALARMID) AS ALARMCOUNT FROM ALARMS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID)";
        String sql = getSql(selectClause, criteria);
        sql = sql + " GROUP BY SEVERITY";

        final int[] alarmCounts = new int[8];
        jdbc().query(sql, paramSetter(criteria), new RowCallbackHandler() {

            public void processRow(ResultSet rs) throws SQLException {
                int severity = rs.getInt("SEVERITY");
                int alarmCount = rs.getInt("ALARMCOUNT");

                alarmCounts[severity] = alarmCount;

            }
            
        });
        
        return alarmCounts;
    }
    
    public Alarm getAlarm(int alarmId) {
        Alarm[] alarms = getMatchingAlarms(new AlarmCriteria(new AlarmIdFilter(alarmId)));
        if (alarms.length < 1) {
            return null;
        } else {
            return alarms[0];
        }
    }
    
    public Alarm[] getMatchingAlarms(AlarmCriteria criteria) {
        String sql = getSql("SELECT ALARMS.*, NODE.NODELABEL, SERVICE.SERVICENAME", criteria);
        return getAlarms(sql, paramSetter(criteria));
    }
    

    private Alarm[] getAlarms(String sql, PreparedStatementSetter setter) {
        List<Alarm> alarms = queryForList(sql, setter, new AlarmMapper());
        return alarms.toArray(new Alarm[0]);
    }

    
    void acknowledgeAlarms(String user, Date timestamp, int[] alarmIds) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }

    public void acknowledgeMatchingAlarms(String user, Date timestamp, AlarmCriteria criteria) {
        String sql = getSql("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=?", criteria);
        jdbc().update(sql, paramSetter(criteria, user, new Timestamp(timestamp.getTime())));
    }
    
    public void acknowledgeAll(String user, Date timestamp) {
        m_simpleJdbcTemplate.update("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=? WHERE ALARMACKUSER IS NULL", user, new Timestamp(timestamp.getTime()));
    }

    void unacknowledgeAlarms(int[] alarmIds) {
        unacknowledgeMatchingAlarms(new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }

    public void unacknowledgeMatchingAlarms(AlarmCriteria criteria) {
        String sql = getSql("UPDATE ALARMS SET ALARMACKUSER=NULL, ALARMACKTIME=NULL", criteria);
        jdbc().update(sql, paramSetter(criteria));
    }
    
    public void unacknowledgeAll() {
        m_simpleJdbcTemplate.update("UPDATE ALARMS SET ALARMACKUSER=NULL ALARMACKTIME=NULL WHERE ALARMACKUSER IS NOT NULL");
    }
    

    private int queryForInt(String sql, PreparedStatementSetter setter) throws DataAccessException {
        Number number = (Number) queryForObject(sql, setter, new SingleColumnRowMapper(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    
    @SuppressWarnings("unchecked")
    private Object queryForObject(String sql, PreparedStatementSetter setter, RowMapper rowMapper) throws DataAccessException {
        return DataAccessUtils.requiredSingleResult((List) jdbc().query(sql, setter, new RowMapperResultSetExtractor(rowMapper, 1)));
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> queryForList(String sql, PreparedStatementSetter setter, ParameterizedRowMapper<T> rm) {
        return (List<T>) jdbc().query(sql, setter, new RowMapperResultSetExtractor(rm));
    }
    
    private JdbcOperations jdbc() {
        return m_simpleJdbcTemplate.getJdbcOperations();
    }

}
