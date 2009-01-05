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
package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.opennms.web.svclayer.PaletteTestUtils.assertPaletteEquals;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.AttributeSecretDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsSecretAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class PerfGraphBuilderServiceTest extends TestCase {
	
	DefaultPerfGraphBuilderService m_perfGraphBuilderService;
	NodeDao m_mockNodeDao;
	AttributeSecretDao m_mockAttributeDao;

	protected void setUp() throws Exception {
		
		m_mockNodeDao = createMock(NodeDao.class);
		m_mockAttributeDao = createMock(AttributeSecretDao.class);
		
		m_perfGraphBuilderService = new DefaultPerfGraphBuilderService();
		m_perfGraphBuilderService.setNodeDao(m_mockNodeDao);
		m_perfGraphBuilderService.setAttributeDao(m_mockAttributeDao);
	}
	
	public void testCreateGraphDefinition() {
		
		assertNotNull(m_perfGraphBuilderService.createGraphDefinition());
		
	}
	
	class AttributeBuilder {

		private Collection<OnmsSecretAttribute> m_attributes;

		public void addAttribute(String id, String label) {
			if (m_attributes == null)
				m_attributes = new LinkedList<OnmsSecretAttribute>();
			
			OnmsSecretAttribute attr = new OnmsSecretAttribute(id, label);
			m_attributes.add(attr);
		}

		public Collection<OnmsSecretAttribute> getAttributes() {
			return m_attributes;
		}
		
	}
	
	class NodeBuilder extends AttributeBuilder {
		private OnmsNode m_node;
		NodeBuilder(int id, String label) {
			m_node = new OnmsNode();
			m_node.setId(id);
			m_node.setLabel(label);
		}
		
		public OnmsNode getNode() {
			return m_node;
		}
		
		public IfBuilder addInterface(String ipAddr) {
			return new IfBuilder(ipAddr, m_node);
		}
	}
	
	class IfBuilder extends AttributeBuilder {
		
		private OnmsIpInterface m_iface;
		public IfBuilder(String ipAddr, OnmsNode node) {
			m_iface = new OnmsIpInterface(ipAddr, node);
		}

		public OnmsIpInterface getInterface() {
			return m_iface;
		}
		
		public OnmsMonitoredService addService(String svcName) {
			OnmsServiceType svcType = new OnmsServiceType(svcName);
			OnmsMonitoredService svc = new OnmsMonitoredService(m_iface, svcType);
			return svc;
		}
		
		
	}
	
	public void testGetAttributesForResource() {

		// the node
		NodeBuilder nodeBuilder = new NodeBuilder(1, "TestNode");
		OnmsNode node = nodeBuilder.getNode();
		
		// expect the node dao to be called to retrieve this node
		expect(m_mockNodeDao.get(1)).andReturn(node);

		// the expected palette for the node
		PaletteBuilder paletteBuilder = new PaletteBuilder("TestNode");
		
		Palette expectedPalette = paletteBuilder.getPalette();
		paletteBuilder.addCategory("Node Attributes");

		// the node attributes
		addAttribute(nodeBuilder, paletteBuilder, "node:1:avgBusy5", "Avg Busy 5");
		addAttribute(nodeBuilder, paletteBuilder, "node:1:tcpResets", "Tcp Resets");
		addAttribute(nodeBuilder, paletteBuilder, "node:1:avgBusy15", "Avg Busy 15");

		// expect the attributes dao to be called to retrived the attributes for the node
		expect(m_mockAttributeDao.getAttributesForNode(node)).andReturn(nodeBuilder.getAttributes());
		
		int ifCount = 3;
		// add ip interfaces
		String[] svcTypes = new String[] { "ICMP", "SSH", "HTTP" };
		for(int i = 1; i <= ifCount; i++) {
			
			IfBuilder ifBuilder = nodeBuilder.addInterface("192.168.1."+i);
			paletteBuilder.addCategory("Interface: 192.168.1."+i);
			
			addAttribute(ifBuilder, paletteBuilder, "if:192.168.1."+i+":ifInOctets", "If In Octets");
			addAttribute(ifBuilder, paletteBuilder, "if:192.168.1."+i+":ifInOctets", "If Out Octets");
			
			expect(m_mockAttributeDao.getAttributesForInterface(ifBuilder.getInterface())).andReturn(ifBuilder.getAttributes());
			
			paletteBuilder.addSpacer();
			
			for (String svcName : svcTypes) {
				
				OnmsMonitoredService svc = ifBuilder.addService(svcName);
				OnmsSecretAttribute attr = new OnmsSecretAttribute("svc:192.168.1."+i+":"+svcName+":responseTime", "responseTime");

				//expect(m_mockAttributeDao.getResponseTimeAttributeForService(svc)).andReturn(attr);
				//paletteBuilder.addItem("svc:192.168.1."+i+":"+svcName+":responseTime", svcName);
			}
			
		}		

		replay(m_mockNodeDao);
		replay(m_mockAttributeDao);
		
		Palette actualPalette = m_perfGraphBuilderService.getAttributePalette(1);
		assertNotNull(actualPalette);
		
		assertPaletteEquals(expectedPalette, actualPalette);
		
		verify(m_mockNodeDao);
		verify(m_mockAttributeDao);
	}

	private void addAttribute(AttributeBuilder attrBuilder, PaletteBuilder paletteBuilder, String id, String label) {
		attrBuilder.addAttribute(id, label);
		paletteBuilder.addItem(id, label);
	}

}

