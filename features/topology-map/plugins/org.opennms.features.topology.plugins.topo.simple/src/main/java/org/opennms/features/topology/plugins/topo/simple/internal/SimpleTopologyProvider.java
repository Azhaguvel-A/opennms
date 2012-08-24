package org.opennms.features.topology.plugins.topo.simple.internal;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.EditableTopologyProvider;
import org.opennms.features.topology.api.TopologyProvider;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class SimpleTopologyProvider implements TopologyProvider, EditableTopologyProvider{
    

    private SimpleVertexContainer m_vertexContainer;
    private BeanContainer<String, SimpleEdge> m_edgeContainer;
    private int m_counter = 0;
    private int m_edgeCounter = 0;
    private int m_groupCounter = 0;
    
    private URL m_topologyLocation = null;
    
    public SimpleTopologyProvider() {
    	System.err.println("Creating a new SimpleTopologyProvider");
        m_vertexContainer = new SimpleVertexContainer();
        m_edgeContainer = new BeanContainer<String, SimpleEdge>(SimpleEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
        
        URL defaultGraph = getClass().getResource("/saved-vmware-graph.xml");

        setTopologyLocation(defaultGraph);
    }
    
    public URL getTopologyLocation() {
		return m_topologyLocation;
	}

	public void setTopologyLocation(URL topologyLocation) {
		m_topologyLocation = topologyLocation;
		
		if (m_topologyLocation != null) {
			System.err.println("Loading Topology from " + m_topologyLocation);

			load(m_topologyLocation);
		} else {
			System.err.println("Setting topology location to null!");
		}
	}

	public SimpleVertexContainer getVertexContainer() {
        return m_vertexContainer;
    }

    public BeanContainer<String, SimpleEdge> getEdgeContainer() {
        return m_edgeContainer;
    }

    public Collection<?> getVertexIds() {
        return m_vertexContainer.getItemIds();
    }

    public Collection<?> getEdgeIds() {
        return m_edgeContainer.getItemIds();
    }

    public Item getVertexItem(Object vertexId) {
        return m_vertexContainer.getItem(vertexId);
    }

    public Item getEdgeItem(Object edgeId) {
        return m_edgeContainer.getItem(edgeId);
    }
    
    public Collection<?> getEndPointIdsForEdge(Object edgeId) {
        
        SimpleEdge edge = getRequiredEdge(edgeId);

        List<Object> endPoints = new ArrayList<Object>(2);
        
        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());

        return endPoints;
    }

    public Collection<?> getEdgeIdsForVertex(Object vertexId) {
        
        SimpleVertex vertex = getRequiredVertex(vertexId);
        
        List<Object> edges = new ArrayList<Object>(vertex.getEdges().size());
        
        for(SimpleEdge e : vertex.getEdges()) {
            
            Object edgeId = e.getId();
            
            edges.add(edgeId);

        }
        
        return edges;

    }
    
    private Item addVertex(String id, int x, int y, String label, String ipAddr, int nodeID) {
        if (m_vertexContainer.containsId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        System.err.println("Adding a vertex: " + id);
        SimpleVertex vertex = new SimpleLeafVertex(id, x, y);
        vertex.setIconKey("server");
        vertex.setLabel(label);
        vertex.setIpAddr(ipAddr);
        vertex.setNodeID(nodeID);
        return m_vertexContainer.addBean(vertex);
    }
    
    private Item addGroup(String groupId, String iconKey, String label) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        System.err.println("Adding a group: " + groupId);
        SimpleVertex vertex = new SimpleGroup(groupId);
        vertex.setLabel(label);
        vertex.setIconKey(iconKey);
        return m_vertexContainer.addBean(vertex);
        
    }
    private void connectVertices(String id, Object sourceVertextId, Object targetVertextId) {
        SimpleVertex source = getRequiredVertex(sourceVertextId);
        SimpleVertex target = getRequiredVertex(targetVertextId);
        
        SimpleEdge edge = new SimpleEdge(id, source, target);
        
        m_edgeContainer.addBean(edge);
        
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#removeVertex(java.lang.Object)
	 */
    @Override
	public void removeVertex(Object vertexId) {
        
        SimpleVertex vertex = getVertex(vertexId, false);
        if (vertex == null) return;
        
        m_vertexContainer.removeItem(vertexId);
        
        for(SimpleEdge e : vertex.getEdges()) {
            m_edgeContainer.removeItem(e.getId());
        }
                
        
    }

    private SimpleVertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private SimpleVertex getVertex(Object vertexId, boolean required) {
        BeanItem<SimpleVertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    private SimpleEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private SimpleEdge getEdge(Object edgeId, boolean required) {
        BeanItem<SimpleEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }
    

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=SimpleLeafVertex.class),
                @XmlElement(name="group", type=SimpleGroup.class)
        })
        List<SimpleVertex> m_vertices = new ArrayList<SimpleVertex>();
        
        @XmlElement(name="edge")
        List<SimpleEdge> m_edges = new ArrayList<SimpleEdge>();
        
        @SuppressWarnings("unused")
        public SimpleGraph() {}

        public SimpleGraph(List<SimpleVertex> vertices, List<SimpleEdge> edges) {
            m_vertices = vertices;
            m_edges = edges;
        }

    }
    
    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#save(java.lang.String)
	 */
    @Override
	public void save(String filename) {
        List<SimpleVertex> vertices = getBeans(m_vertexContainer);
        List<SimpleEdge> edges = getBeans(m_edgeContainer);

        SimpleGraph graph = new SimpleGraph(vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }
    
	public void load(URL url) {
        SimpleGraph graph = JAXB.unmarshal(url, SimpleGraph.class);
        
        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(graph.m_vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(graph.m_edges);
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#load(java.lang.String)
	 */
    @Override
	public void load(String filename) {
        SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);
        
        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(graph.m_vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(graph.m_edges);
    }
    
    private <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    public String getNextVertexId() {
        return "v" + m_counter++;
    }

    public String getNextEdgeId() {
        return "e" + m_edgeCounter ++;
    }
    
    public String getNextGroupId() {
        return "g" + m_groupCounter++;
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#resetContainer()
	 */
    @Override
	public void resetContainer() {
        getVertexContainer().removeAllItems();
        getEdgeContainer().removeAllItems();
        
        m_counter = 0;
        m_edgeCounter = 0;
    }

    public Collection<?> getPropertyIds() {
        return Collections.EMPTY_LIST;
    }

    public Property getProperty(String propertyId) {
        return null;
    }
    
    
    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#addVertex(int, int)
	 */
    @Override
	public Object addVertex(int x, int y) {
        String nextVertexId = getNextVertexId();
//        addVertex(nextVertexId, x, y, icon, "Vertex " + nextVertexId, "127.0.0.1", -1);
        /* 
         * Passing a nodeID of -1 will disable the Events/Alarms, Node Info, and
         * Resource Graphs windows in the context menus  
         */
        addVertex(nextVertexId, x, y, "Vertex " + nextVertexId, "64.146.64.214", -1);
        return nextVertexId;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#connectVertices(java.lang.Object, java.lang.Object)
	 */
    @Override
	public Object connectVertices(Object sourceVertextId, Object targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
        return nextEdgeId;
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#addGroup(java.lang.String)
	 */

    @Override
    public Object addGroup(String groupIconKey) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIconKey, "Group " + nextGroupId);
        return nextGroupId;
    }

    /* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.simple.internal.EditableTopologyProvider#containsVertexId(java.lang.Object)
	 */

	@Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);
    }
    
}
