/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 14, 2007
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.nfunk.jep.JEP;
import org.opennms.netmgt.config.threshd.Expression;

/**
 * 
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class ExpressionConfigWrapper extends BaseThresholdDefConfigWrapper {

    private Expression m_expression;
    private Collection<String> m_datasources;
    private JEP m_parser;
    public ExpressionConfigWrapper(Expression expression) throws ThresholdExpressionException {
        super(expression);
        m_expression=expression;
        m_datasources=new ArrayList<String>();
        m_parser = new JEP();
        m_parser.setAllowUndeclared(true); //This is critical - we allow undelared vars, then ask the parser for what vars are used
        m_parser.parseExpression(m_expression.getExpression());
        if(m_parser.hasError()) {
            throw new ThresholdExpressionException("Could not parse threshold expression:"+m_parser.getErrorInfo());
        }
        m_datasources.addAll(m_parser.getSymbolTable().keySet());
    }
    
    @Override
    public String getDatasourceExpression() {
        return m_expression.getExpression();
    }
    @Override
    public Collection<String> getRequiredDatasources() {
       return m_datasources;
    }

    @Override
    public double evaluate(Map<String, Double> values) throws ThresholdExpressionException {
        for(String valueName : values.keySet()) {
            m_parser.addVariable(valueName, values.get(valueName));
        }
        double result=m_parser.getValue();
        if(m_parser.hasError()) {
            throw new ThresholdExpressionException("Error while evaluating expression "+m_expression.getExpression()+": "+m_parser.getErrorInfo());
        }
        return result;
    }

}
