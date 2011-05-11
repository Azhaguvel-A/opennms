<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
//
// Modifications:
//
// 2009 Sep 01: Restored non-IP functionality
// 2004 Jan 15: Added node admin function.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added response time link (Bug #684) and HTTP link (Bug #469).
// 2002 Nov 26: Fixed breadcrumbs issue.
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
// Foundation, Inc.:
// 51 Franklin Street
// 5th Floor
// Boston, MA 02110-1301
// USA
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%@page
	language="java"
	contentType="text/html"
	session="true"
	import="
		java.net.*,
		java.util.*,
		org.springframework.web.context.WebApplicationContext,
		org.springframework.web.context.support.WebApplicationContextUtils,
		org.opennms.core.utils.InetAddressUtils,
		org.opennms.netmgt.model.OnmsNode,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.event.*,
		org.opennms.web.springframework.security.Authentication,
		org.opennms.web.svclayer.ResourceService
	"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%!
    protected int telnetServiceId;
    protected int httpServiceId;
    protected int dellServiceId;
    protected int snmpServiceId;
    private ResourceService m_resourceService;

    public void init() throws ServletException {

        NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());
        try {
            this.telnetServiceId = factory.getServiceIdFromName("Telnet");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }        

        try {
            this.httpServiceId = factory.getServiceIdFromName("HTTP");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

        try {
            this.dellServiceId = factory.getServiceIdFromName("Dell-OpenManage");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the Dell-OpenManage service ID", e );
        }

        try {
            this.snmpServiceId = factory.getServiceIdFromName("SNMP");
        }
        catch (Throwable e) {
            throw new ServletException( "Could not determine the SNMP service ID", e );
        }

        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);
    }%>

<%
    NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());

    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    //get the database node info
    OnmsNode node_db = factory.getNode( nodeId );
    if( node_db == null ) {
	throw new ElementNotFoundException("No such node in database", "node", "element/linkednode.jsp", "node", "element/nodeList.htm");
    }

    //get the child interfaces
    Interface[] intfs = factory.getActiveInterfacesOnNode( nodeId );
    Interface[] snmpIntfs = factory.getAllSnmpInterfacesOnNode( nodeId );

    if( intfs == null ) { 
        intfs = new Interface[0]; 
    }

    if( snmpIntfs == null ) { 
        snmpIntfs = new Interface[0]; 
    }

    //See if node has any ifAliases
    boolean hasIfAliases = factory.nodeHasIfAliases(nodeId);

    //find the telnet interfaces, if any
    String telnetIp = null;
    Service[] telnetServices = factory.getServicesOnNode(nodeId, this.telnetServiceId);
    
    if( telnetServices != null && telnetServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < telnetServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(telnetServices[i].getIpAddress()));
        }
        
        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);
        
        if( lowest != null ) {
            telnetIp = lowest.getHostAddress();
        }
    }    

    //find the HTTP interfaces, if any
    String httpIp = null;
    Service[] httpServices = factory.getServicesOnNode(nodeId, this.httpServiceId);

    if( httpServices != null && httpServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < httpServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(httpServices[i].getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if( lowest != null ) {
            httpIp = lowest.getHostAddress();
        }
    }

    //find the Dell-OpenManage interfaces, if any
    String dellIp = null;
    Service[] dellServices = factory.getServicesOnNode(nodeId, this.dellServiceId);

    if( dellServices != null && dellServices.length > 0 ) {
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        for( int i=0; i < dellServices.length; i++ ) {
            ips.add(InetAddressUtils.addr(dellServices[i].getIpAddress()));
        }

        InetAddress lowest = InetAddressUtils.getLowestInetAddress(ips);

        if( lowest != null ) {
            dellIp = lowest.getHostAddress();
        }
    }

    //find if SNMP is on this node 
    boolean isSnmp = false;
    Service[] snmpServices = factory.getServicesOnNode(nodeId, this.snmpServiceId);

    if( snmpServices != null && snmpServices.length > 0 ) 
	isSnmp = true;

    // find links
    Map<Integer,Vector<Interface>> linkMap = new HashMap<Integer,Vector<Interface>>();
    DataLinkInterface[] dl_if = factory.getDataLinksOnNode(nodeId);

    for (int i=0; i<dl_if.length;i++){
	   int nodelinkedId = 0;
  	   int nodelinkedIf = -1;
  	   Integer ifindexmap = null;
  	   String iplinkaddress = null;
       Vector<Interface> ifs = new Vector<Interface>();

       nodelinkedId = dl_if[i].get_nodeparentid();
       nodelinkedIf = dl_if[i].get_parentifindex();
   	   iplinkaddress = dl_if[i].get_parentipaddr();
   	   ifindexmap = new Integer(dl_if[i].get_ifindex());

       Interface iface = null;
       if (iplinkaddress != null) {
	       if (nodelinkedIf == -1 ) {
		       iface = factory.getInterface(nodelinkedId,iplinkaddress);
	   		} else {
	   		   iface = factory.getInterface(nodelinkedId,iplinkaddress,nodelinkedIf);
	   		}
		   if (linkMap.containsKey(ifindexmap)){
			   ifs = linkMap.get(ifindexmap);
	   		} 
	   	ifs.addElement(iface);
	   	linkMap.put(ifindexmap,ifs);
	} else {
	    iface = factory.getSnmpInterface(nodelinkedId,nodelinkedIf);
	    if (linkMap.containsKey(ifindexmap)){
		ifs = linkMap.get(ifindexmap);
            } 
	    ifs.addElement(iface);
	    linkMap.put(ifindexmap,ifs);
	}
	   
   }

    boolean isBridge = factory.isBridgeNode(nodeId);
    boolean isRouteIP = factory.isRouteInfoNode(nodeId);

%>
<script type="text/javascript">
  function setDown(node, intf){
	document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+2;
	document.setStatus.submit();
	}
  function setUp(node, intf){
        document.setStatus.action="element/ManageSnmpIntf?node="+node+"&intf="+intf+"&status="+1;
        document.setStatus.submit();
	}

</script>


<% pageContext.setAttribute("nodeId", nodeId); %>
<% pageContext.setAttribute("nodeLabel", node_db.getLabel()); %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="headTitle" value="${nodeLabel}" />
  <jsp:param name="headTitle" value="Linked Node Info" />
  <jsp:param name="title" value="Linked Node Info" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${nodeId}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Links" />
</jsp:include>



<!-- Body -->

        <h2>Node: <%=node_db.getLabel()%></h2>

      <div id="linkbar">
      <ul>
        <li>
		<a href="event/list.htm?filter=node%3D<%=nodeId%>">View Events</a>
	</li>
    <li>
		<a href="asset/modify.jsp?node=<%=nodeId%>">Asset Info</a>
	</li>
		<% if( telnetIp != null ) { %>
          <li>
          <a href="telnet://<%=telnetIp%>">Telnet</a>
          </li>
        <% } %>

        <% if( httpIp != null ) { %>
           <li>
           <a href="http://<%=httpIp%>">HTTP</a>
           </li>
        <% } %>

        <% if( dellIp != null ) { %>
          <li>
          <a href="https://<%=dellIp%>:1311">OpenManage</a>
          </li>
        <% } %>

        <% if (m_resourceService.findNodeChildResources(nodeId).size() > 0) { %>
	  <li>
        <c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
          <c:param name="parentResourceType" value="node"/>
          <c:param name="parentResource" value="<%= Integer.toString(nodeId) %>"/>
          <c:param name="reports" value="all"/>
        </c:url>
          <a href="${fn:escapeXml(resourceGraphsUrl)}">Resource Graphs</a>
	  </li>
        <% } %>
        
         <li>
         <a href="element/rescan.jsp?node=<%=nodeId%>">Rescan</a>
         </li>
        <% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
           <li>
           <a href="admin/nodemanagement/index.jsp?node=<%=nodeId%>">Admin</a>
           </li>
        <% } %>

           <% if ( isSnmp && request.isUserInRole( Authentication.ROLE_ADMIN ))  { %>
              <% for( int i=0; i < intfs.length; i++ ) { %>
                <% if( "P".equals( intfs[i].getIsSnmpPrimary() )) { %>
                       <c:url var="updateSnmpLink" value="admin/updateSnmp.jsp">
                           <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                           <c:param name="ipaddr" value="<%=intfs[i].getIpAddress()%>"/>
                       </c:url>
                       <li>
                       <a href="<c:out value="${updateSnmpLink}"/>">Update SNMP</a>
                       </li>
                <% } %>
              <% } %>
           <% } %>
	  </ul>
	  </div>
	  
	<div class="TwoColLeft">
            <!-- general info box -->
			<h3>General (Status: <%=(node_db == null ? "Unknown" : ElementUtil.getNodeStatusString(node_db))%>)</h3>
			<% if( isRouteIP || isBridge ) { %>
			<div class="boxWrapper">
			     <ul class="plain">
		            <% if( isRouteIP ) { %>
		            <li>
		            	<a href="element/routeipnode.jsp?node=<%=nodeId%>">View Node IP Route Info</a>
		            </li>
		            <% }%>
		         
		            <% if( isBridge ) { %>
		            <li>
						<a href="element/bridgenode.jsp?node=<%=nodeId%>">View Node Bridge/STP Info</a>
					</li>
		            <% }%>		
		         </ul>	     
			</div>
			<% }%>
	</div>
<hr />        

<h3>Interfaces</h3>
		
		<!-- Interface box -->
		<table class="standard">
		
		<thead>
			<tr>
			<th>Interface</th> 
                        <th>Index</th>
                        <th>Description</th>
                        <% if (hasIfAliases) { %>
                            <th>IfAlias</th>
                        <% } %>
			<th width="10%">ifStatus (Adm/Op)</th> 
<%--
			// TODO - turning this off until the SET is verified.
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
			<th width="10%">Set Admin Status</th> 
			<% } %>
--%>

			<th>Links</th>
			</tr>
		</thead>
		
		<% for( int i=0; i < intfs.length; i++ ) { 
		
			Vector<Interface> ifl =null;
			if (intfs[i].getIfIndex() == 0 ) {
		 		ifl = linkMap.get(-1);
			} else {
		 		ifl = linkMap.get(intfs[i].getIfIndex());
			}
		%>
		
		<tr>
		
		<td class="standard">
        <c:url var="interfaceLink" value="element/interface.jsp">
            <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
            <c:param name="intf" value="<%=intfs[i].getIpAddress()%>"/>
        </c:url>
		<a href="<c:out value="${interfaceLink}"/>"><%=intfs[i].getIpAddress()%></a>
		<!-- 
			This is OK for now since getIpAddress() returns a String but if we refactor
			NetworkElementFactory to return OnmsIpInterface instances, we will need to make
			this comparison work between an InetAddress object and a String.
		-->
		<c:out value="<%=intfs[i].getIpAddress().equals(intfs[i].getHostname()) ? "" : "(" + intfs[i].getHostname() + ")"%>"/>

		</td>
                <td>
                    <% if (intfs[i].getIfIndex() > 0) { %>
                        <%=intfs[i].getIfIndex()%>
                    <% } else { %>
                        &nbsp;
                    <% } %>
                </td>
                <td>
                    <% if (intfs[i].getSnmpIfDescription() != null && !intfs[i].getSnmpIfDescription().equals("")) { %>
                        <c:out value="<%=intfs[i].getSnmpIfDescription()%>"/>
                    <% } else if (intfs[i].getSnmpIfName() != null && !intfs[i].getSnmpIfName().equals("")) { %>
                        <c:out value="<%=intfs[i].getSnmpIfName()%>"/>
                    <% } else { %>
                        &nbsp;
                    <% } %>
                </td>
                <% if (hasIfAliases) { %>
                    <td>
                        <% if (intfs[i].getSnmpIfAlias() != null && !intfs[i].getSnmpIfAlias().equals("")) { %>
                            <c:out value="<%=intfs[i].getSnmpIfAlias()%>"/>
		        <% } else {%>
                            &nbsp;
		        <% } %>
                    </td>
		<% } %>
		<td class="standard">
			<% if( intfs[i].getSnmpIfAdminStatus() < 1 && intfs[i].getSnmpIfOperStatus() < 1 ) { %>
			&nbsp; 
			<% } else { %>
			&nbsp;
			<%=OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[intfs[i].getSnmpIfOperStatus()]%>
			<% } %>
		</td>
					
<%--
		// TODO - turning this off until the SET is verified.
		<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
			<% if(OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
		<td align="center"> <input type="button" value="Down" onClick="setDown(<%=intfs[i].getNodeId()%>,<%=intfs[i].getIfIndex()%>);"> </td>
			<% } else if (OPER_ADMIN_STATUS[intfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
		<td align="center"> <input type="button" value="Up" onClick="setUp(<%=intfs[i].getNodeId()%>,<%=intfs[i].getIfIndex()%>);"> </td> 
			<% } else { %>
		<td><b>&nbsp;</b></td> 
			<% } %>
		<% } %>
--%>
				
		<td class="standard">
		<% if (ifl == null || ifl.size() == 0) {%>
		&nbsp;
		<% } else {
		// Don't bother creating a table if all the interfaces in ifl are null
		Boolean emptyTable = true;
		for (int j=0; j<ifl.size();j++) {
		    Interface curlkif =(Interface)ifl.elementAt(j);
		    if (curlkif != null) {
		        emptyTable = false;
		        break;
		    }
		}
		if ( emptyTable ) { %>
		    &nbsp;
		<% } else { %>
		<table>
		
		<thead>
			<tr>
				<th style="font-size:70%" width="35%">Linked Node</th>
				<th style="font-size:70%" width="35%">Interface</th> 
				<th style="font-size:70%" width="15%">ifStatus (Adm/Op)</th>
			
<%--
			// TODO - turning this off until the SET is verified.
				<th style="font-size:70%">
				<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
					Set Admin Status
				<% } else { %>
					&nbsp;
				<% } %>
				</th> 	
--%>
				</tr>
		</thead>					
			<% for (int j=0; j<ifl.size();j++) { 
				Interface curlkif =(Interface)ifl.elementAt(j); 
			%>
		    <% if (curlkif != null) { %>    
			<tr>
			<td class="standard" style="font-size:70%" width="35%">
		       	<a href="element/linkednode.jsp?node=<%=curlkif.getNodeId()%>"><c:out value="<%=factory.getNodeLabel(curlkif.getNodeId())%>"/></a>
			</td>
			<td class="standard" style="font-size:70%" width="35%">
		       	<% if(curlkif.getIpAddress() == null ||  "0.0.0.0".equals( curlkif.getIpAddress() )) { %>
		        <a href="element/snmpinterface.jsp?node=<%=curlkif.getNodeId()%>&ifindex=<%=curlkif.getSnmpIfIndex()%>"><%=curlkif.getSnmpIfName()%></a>
		        <% } else { %>  
                <c:url var="interfaceLink" value="element/interface.jsp">
                    <c:param name="node" value="<%=String.valueOf(curlkif.getNodeId())%>"/>
                    <c:param name="intf" value="<%=curlkif.getIpAddress()%>"/>
                </c:url>
		        <a href="${interfaceLink}"><%=curlkif.getIpAddress()%></a>
		        <% } %>
		       	<% if( curlkif.getIfIndex() != 0 ) { %>
		          <c:out value="<%=" (ifIndex: "+curlkif.getIfIndex()+"-"+curlkif.getSnmpIfDescription()+")"%>"/>
		        <% } else if( curlkif.getSnmpIfIndex() != 0 ) { %>
		          <c:out value="<%=" (ifIndex: "+curlkif.getSnmpIfIndex()+"-"+curlkif.getSnmpIfDescription()+")"%>"/>
		        <% } %>
		    </td>
			<td class="standard" style="font-size:70%" width="15%">
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN ) && curlkif != null) { %>
				<% if( curlkif.getSnmpIfAdminStatus() < 1 && curlkif.getSnmpIfOperStatus() < 1 ) { %>
				&nbsp; 
				<% } else { %>
				(<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfOperStatus()]%>)
				<% } %>
			<% } else { %>
				&nbsp;
			<% } %>
			</td>
<%--
			// TODO - turning this off until the SET is verified.
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN ) && curlkif != null) { %>
				<% if(OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
				<td class="standard" style="font-size:70%" align="center"><input type="button" value="Down" onClick="setDown(<%=curlkif.getNodeId()%>,<%=curlkif.getIfIndex()%>);"></td>
				<% } else if (OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
				<td class="standard" style="font-size:70%" align="center"><input type="button" value="Up" onClick="setUp(<%=curlkif.getNodeId()%>,<%=curlkif.getIfIndex()%>);"> </td> 
				<% } else { %>
				<td><b>&nbsp;</b></td> 
				<% } %>
			<% } else {%>              
			<td>&nbsp;</td> 
			<% } %>
--%>
			<% } %>
		    </tr>
		    <% } %>
		
		</table>
		
		<%}%>
		<%}%>
		</td>
		
		
		</tr>
		<% } %>

		<% for( int i=0; i < snmpIntfs.length; i++ ) {
		
			Vector<Interface> ifl =null;
			if (snmpIntfs[i].getSnmpIfIndex() == 0 ) {
		 		ifl = linkMap.get(-1);
			} else {
		 		ifl = linkMap.get(snmpIntfs[i].getSnmpIfIndex());
			}

		%>
		
		
		<% if(("0.0.0.0".equals( snmpIntfs[i].getIpAddress())) || (snmpIntfs[i].getIpAddress() == null)) { %>
		    <tr>
		    <td class="standard">
                    <a href="element/snmpinterface.jsp?node=<%=nodeId%>&ifindex=<%=snmpIntfs[i].getSnmpIfIndex()%>">
                        <% if (snmpIntfs[i].getSnmpIfName() != null && !snmpIntfs[i].getSnmpIfName().equals("")) { %>
                            <c:out value="<%=snmpIntfs[i].getSnmpIfName()%>"/>
                        <% } else if (snmpIntfs[i].getSnmpIfDescription() != null && !snmpIntfs[i].getSnmpIfDescription().equals("")) { %>
                            <c:out value="<%=snmpIntfs[i].getSnmpIfDescription()%>"/>
                        <% } else { %>
                            Non-IP
                        <% } %>
                    </a>

                    </td>
                    <td>
                    <% if (snmpIntfs[i].getSnmpIfIndex() > 0) { %>
                        <%=snmpIntfs[i].getSnmpIfIndex()%>
                    <% } else { %>
                        &nbsp;
                    <% } %>
                    </td>
                    <td>
                    <% if (snmpIntfs[i].getSnmpIfDescription() != null && !snmpIntfs[i].getSnmpIfDescription().equals("")) { %>
                        <c:out value="<%=snmpIntfs[i].getSnmpIfDescription()%>"/>
                    <% } else if (snmpIntfs[i].getSnmpIfName() != null && !snmpIntfs[i].getSnmpIfName().equals("")) { %>
                        <c:out value="<%=snmpIntfs[i].getSnmpIfName()%>"/>
                    <% } else { %>
                        &nbsp;
                    <% } %>
                    </td>
                    <% if (hasIfAliases) { %>
                        <td>
                        <% if (snmpIntfs[i].getSnmpIfAlias() != null && !snmpIntfs[i].getSnmpIfAlias().equals("")) { %>
                            <c:out value="<%=snmpIntfs[i].getSnmpIfAlias()%>"/>
                        <% } else {%>
                            &nbsp;
                        <% } %>
                        </td>
		    <% } %>
		    <td class="standard">
		    <% if( snmpIntfs[i].getSnmpIfAdminStatus() < 1 && snmpIntfs[i].getSnmpIfOperStatus() < 1 ) { %>
			&nbsp; 
		    <% } else { %>
			&nbsp;
			<%=OPER_ADMIN_STATUS[snmpIntfs[i].getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[snmpIntfs[i].getSnmpIfOperStatus()]%>
		    <% } %>
		    </td>
					
<%--
		    // TODO - turning this off until the SET is verified.
		    <% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %>
			<% if(OPER_ADMIN_STATUS[snmpIntfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
		            <td align="center"> <input type="button" value="Down" onClick="setDown(<%=snmpIntfs[i].getNodeId()%>,<%=snmpIntfs[i].getSnmpIfIndex()%>);"> </td>
			<% } else if (OPER_ADMIN_STATUS[snmpIntfs[i].getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
		            <td align="center"> <input type="button" value="Up" onClick="setUp(<%=snmpIntfs[i].getNodeId()%>,<%=snmpIntfs[i].getSnmpIfIndex()%>);"> </td> 
			<% } else { %>
		            <td><b>&nbsp;</b></td> 
			<% } %>
		    <% } %>
--%>
				
		    <td class="standard">
		    <% if (ifl == null || ifl.size() == 0) {%>
			&nbsp;
		    <% } else {
		    // Don't bother creating a table if all the interfaces in ifl are null
		    Boolean emptyTable = true;
		    for (int j=0; j<ifl.size();j++) {
		        Interface curlkif =(Interface)ifl.elementAt(j);
		        if (curlkif != null) {
		            emptyTable = false;
		            break;
		        }
		    }
		    if ( emptyTable ) { %>
		        &nbsp;
		    <% } else { %>
		            
			<table>
		
			<thead>
			<tr>
				<th style="font-size:70%" width="35%">Linked Node</th>
				<th style="font-size:70%" width="35%">Interface</th> 
				<th style="font-size:70%" width="15%">If Status (Adm/Op)</th>
			
<%--
			// TODO - turning this off until the SET is verified.
				<th style="font-size:70%">
				<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
					Set Admin Status
				<% } else { %>
					&nbsp;
				<% } %>
				</th> 	
--%>
			</tr>
			</thead>					
			<% for (int j=0; j<ifl.size();j++) { 
				Interface curlkif =(Interface)ifl.elementAt(j); 
			%>
		    <% if (curlkif != null) { %>    
			<tr>
			<td class="standard" style="font-size:70%" width="35%">
		       	<a href="element/linkednode.jsp?node=<%=curlkif.getNodeId()%>"><%=factory.getNodeLabel(curlkif.getNodeId())%></a>
			</td>
			<td class="standard" style="font-size:70%" width="35%">
		       	<% if( "0.0.0.0".equals( curlkif.getIpAddress() ) || curlkif.getIpAddress() == null) { %>
			    <a href="element/snmpinterface.jsp?node=<%=curlkif.getNodeId()%>&ifindex=<%=curlkif.getSnmpIfIndex()%>"><%=curlkif.getSnmpIfName()%></a>
		        <% } else { %>  
                <c:url var="interfaceLink" value="element/interface.jsp">
                    <c:param name="node" value="<%=String.valueOf(curlkif.getNodeId())%>"/>
                    <c:param name="intf" value="<%=curlkif.getIpAddress()%>"/>
                </c:url>
			    <a href="${interfaceLink}"><%=curlkif.getIpAddress()%></a>
		        <% } %>
		       	<% if( curlkif.getSnmpIfIndex() != 0 ) { %>
			      <c:out value="<%=" (ifIndex: "+curlkif.getSnmpIfIndex()+"-"+curlkif.getSnmpIfDescription()+")"%>"/>
		        <% } else if( curlkif.getIfIndex() != 0 ) { %>
			      <c:out value="<%=" (ifIndex: "+curlkif.getIfIndex()+"-"+curlkif.getSnmpIfDescription()+")"%>"/>
		        <% } %>
		        </td>
			<td class="standard" style="font-size:70%" width="15%">
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN ) && curlkif != null) { %>
			    <% if( curlkif.getSnmpIfAdminStatus() < 1 && curlkif.getSnmpIfOperStatus() < 1 ) { %>
				&nbsp; 
			    <% } else { %>
				(<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()]%>/<%=OPER_ADMIN_STATUS[curlkif.getSnmpIfOperStatus()]%>)
			    <% } %>
			<% } else { %>
			    &nbsp;
			<% } %>
			</td>
<%--
			// TODO - turning this off until the SET is verified.
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN ) && curlkif != null) { %>
			    <% if(OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Up") ){ %>
				<td class="standard" style="font-size:70%" align="center"><input type="button" value="Down" onClick="setDown(<%=curlkif.getNodeId()%>,<%=curlkif.getSnmpIfIndex()%>);"></td>
			    <% } else if (OPER_ADMIN_STATUS[curlkif.getSnmpIfAdminStatus()].equalsIgnoreCase("Down") ){ %>
				<td class="standard" style="font-size:70%" align="center"><input type="button" value="Up" onClick="setUp(<%=curlkif.getNodeId()%>,<%=curlkif.getSnmpIfIndex()%>);"> </td> 
			    <% } else { %>
				<td><b>&nbsp;</b></td> 
			    <% } %>
			<% } else {%>              
			    <td>&nbsp;</td> 
			<% } %>
--%>
		    <% } %>
		    </tr>
		    <% } %>
		    
		    </table>
		
		    <%}%>
		    <%}%>
		    </td>
		
		
		    </tr>
		    <% } %>
		<% } %>

		</table>


<form method="post" name="setStatus" />

<jsp:include page="/includes/footer.jsp" flush="false" />



<%!
  public static final String[] OPER_ADMIN_STATUS = new String[] {
    "&nbsp;",          //0 (not supported)
    "Up",              //1
    "Down",            //2
    "Testing",         //3
    "Unknown",         //4
    "Dormant",         //5
    "NotPresent",      //6
    "LowerLayerDown"   //7
  };
%>
