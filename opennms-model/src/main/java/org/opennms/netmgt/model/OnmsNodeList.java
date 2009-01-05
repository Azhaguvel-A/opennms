package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "nodes")
public class OnmsNodeList extends LinkedList<OnmsNode> {

    private static final long serialVersionUID = 8031737923157780179L;
    
    public OnmsNodeList() {
        super();
    }

    public OnmsNodeList(Collection<? extends OnmsNode> c) {
        super(c);
    }

    @XmlElement(name = "node")
    public List<OnmsNode> getNodes() {
        return this;
    }
    
    public void setNodes(List<OnmsNode> nodes) {
        clear();
        addAll(nodes);
    }

}
