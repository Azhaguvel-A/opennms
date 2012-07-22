package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.ResourceGraphsWindow;

public class ResourceGraphsOperation implements Operation {

	/*Test Data*/
	private Node testNode1 = new Node(9,"172.20.1.10","Cartman");
	private Node testNode2 = new Node(43, "172.20.1.14", "Butters");
	private String resourceGraphsURL;
	
	public boolean display(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		// TODO Auto-generated method stub
		return true;
	}

	public Undoer execute(List<Object> targets, OperationContext operationContext) {
		try {
			operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(testNode1, getResourceGraphsURL()));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return "resourceGraphs";
	}

	public String getResourceGraphsURL() {
		return resourceGraphsURL;
	}

	public void setResourceGraphsURL(String resourceGraphsURL) {
		this.resourceGraphsURL = resourceGraphsURL;
	}

}
