/**
 * 
 */
package org.opennms.features.vaadin.topology;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.vaadin.topology.gwt.client.GWTEdge;
import org.opennms.features.vaadin.topology.gwt.client.GWTVertex;

@XmlRootElement(name="edge")
public class SimpleEdge {
	String m_id;
	SimpleVertex m_source;
	SimpleVertex m_target;
	
	public SimpleEdge() {}
	
	
	public SimpleEdge(String id, SimpleVertex source, SimpleVertex target) {
		m_id = id;
		m_source = source;
		m_target = target;
		
		m_source.addEdge(this);
		m_target.addEdge(this);
	}

	@XmlID
	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}
	
	@XmlIDREF
	public SimpleVertex getSource() {
		return m_source;
	}

	public void setSource(SimpleVertex source) {
		m_source = source;
		m_source.addEdge(this);
	}

	@XmlIDREF
	public SimpleVertex getTarget() {
		return m_target;
	}

	public void setTarget(SimpleVertex target) {
		m_target = target;
		m_target.addEdge(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEdge other = (SimpleEdge) obj;
		if (m_id == null) {
			if (other.m_id != null)
				return false;
		} else if (!m_id.equals(other.m_id))
			return false;
		return true;
	}
		
}