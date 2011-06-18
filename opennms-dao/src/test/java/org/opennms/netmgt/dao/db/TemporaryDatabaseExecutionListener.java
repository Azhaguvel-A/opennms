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
package org.opennms.netmgt.dao.db;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DataSourceFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * This {@link TestExecutionListener} creates a temporary database and then
 * registers it as the default datasource inside {@link DataSourceFactory} by
 * using {@link DataSourceFactory#setInstance(DataSource)}.
 * 
 * To change the settings for the temporary database, use the 
 * {@link JUnitTemporaryDatabase} annotation on the test class or method.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabaseExecutionListener extends
		AbstractTestExecutionListener {

	private TemporaryDatabase m_database;

	public void afterTestMethod(final TestContext testContext) throws Exception {
		try {
			System.err.printf("TemporaryDatabaseExecutionListener.afterTestMethod(%s)\n", testContext);

			final JUnitTemporaryDatabase jtd = findAnnotation(testContext);
			if (jtd == null) return;

			final DataSource dataSource = DataSourceFactory.getInstance();
			final TemporaryDatabase tempDb = findTemporaryDatabase(dataSource);
			if (tempDb != null) {
				tempDb.drop();
			}
		} finally {
			testContext.markApplicationContextDirty();
			testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
		}

	}

	private TemporaryDatabase findTemporaryDatabase(final DataSource dataSource) {
		if (dataSource instanceof TemporaryDatabase) {
			return (TemporaryDatabase) dataSource;
		} else if (dataSource instanceof DelegatingDataSource) {
			return findTemporaryDatabase(((DelegatingDataSource) dataSource).getTargetDataSource());
		} else {
			return null;
		}

	}

	private JUnitTemporaryDatabase findAnnotation(final TestContext testContext) {
		JUnitTemporaryDatabase jtd = null;
		final Method testMethod = testContext.getTestMethod();
		if (testMethod != null) {
			jtd = testMethod.getAnnotation(JUnitTemporaryDatabase.class);
		}
		if (jtd == null) {
			final Class<?> testClass = testContext.getTestClass();
			jtd = testClass.getAnnotation(JUnitTemporaryDatabase.class);
		}
		return jtd;
	}

	@SuppressWarnings("unchecked")
	public void beforeTestMethod(final TestContext testContext) throws Exception {
		System.err.printf("TemporaryDatabaseExecutionListener.beforeTestMethod(%s)\n", testContext);

		// FIXME: Is there a better way to inject the instance into the test class?
		if (testContext.getTestInstance() instanceof TemporaryDatabaseAware) {
			System.err.println("injecting TemporaryDatabase into TemporaryDatabaseAware test: "
							+ testContext.getTestInstance().getClass().getSimpleName() + "."
							+ testContext.getTestMethod().getName());
			((TemporaryDatabaseAware) testContext.getTestInstance()).setTemporaryDatabase(m_database);
		}
	}

	public void prepareTestInstance(final TestContext testContext) throws Exception {
		System.err.printf("TemporaryDatabaseExecutionListener.prepareTestInstance(%s)\n", testContext);
		final JUnitTemporaryDatabase jtd = findAnnotation(testContext);

		if (jtd == null) return;

		boolean useExisting = false;
		if (jtd.useExistingDatabase() != null) {
			useExisting = !jtd.useExistingDatabase().equals("");
		}

		final String dbName = useExisting ? jtd.useExistingDatabase() : getDatabaseName(testContext);
		m_database = ((jtd.tempDbClass()).getConstructor(String.class, Boolean.TYPE).newInstance(dbName, useExisting));
		m_database.setPopulateSchema(jtd.createSchema() && !useExisting);
		try {
			m_database.create();
		} catch (final Throwable e) {
			System.err.printf("TemporaryDatabaseExecutionListener.prepareTestInstance: error while creating database: %s\n", e.getMessage());
		}

		final LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy(m_database);
		DataSourceFactory.setInstance(proxy);
		System.err.printf("TemporaryDatabaseExecutionListener.prepareTestInstance(%s) prepared db %s\n", testContext, dbName);
	}

	private String getDatabaseName(final TestContext testContext) {
		// Append the current object's hashcode to make this value truly unique
		return String.format("opennms_test_%s_%s", System.currentTimeMillis(), this.hashCode());
	}

}
