<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Sep 04: Fixed display issue when issnmpprimary is null.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Sep 24: Added a "select" option for SNMP data and a config web page.
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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.io.File,
		java.util.*,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.admin.nodeManagement.*
	"
%>

<%!
    int interfaceIndex;
%>

<%
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    String nodeLabel = request.getParameter( "nodelabel" );

    if( nodeLabel == null ) {
        throw new org.opennms.web.MissingParameterException( "nodelabel" );
    }

    HttpSession userSession = request.getSession(false);
    List interfaces = null;
    
    interfaceIndex = 0;
    
    if (userSession != null)
    {
  	interfaces = (List)userSession.getAttribute("listInterfacesForNode.snmpselect.jsp");
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Select SNMP Interfaces" />
  <jsp:param name="headTitle" value="Select SNMP Interfaces" />
  <jsp:param name="headTitle" value="Admin"/>
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Select SNMP Interfaces" />
</jsp:include>

<script type="text/javascript" >

  function applyChanges()
  {
      if (confirm("Are you sure you want to proceed? This action can be undone by returning to this page."))
      {
          document.chooseSnmpNodes.submit();
      }
  }
  
  function cancel()
  {
      document.chooseSnmpNodes.action="admin/index.jsp";
      document.chooseSnmpNodes.submit();
  }
  
  function checkAll()
  {
      for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++)
      {  
          if (document.chooseSnmpNodes.elements[c].type == "checkbox")
          {
              document.chooseSnmpNodes.elements[c].checked = true;
          }
      }
  }
  
  function uncheckAll()
  {
      for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++)
      {  
          if (document.chooseSnmpNodes.elements[c].type == "checkbox")
          {
              
              document.chooseSnmpNodes.elements[c].checked = false;
          }
      }
  }
  
</script>


<form method="post" name="chooseSnmpNodes" action="admin/changeCollectStatus">

<input type="hidden" name="node" value="<%=nodeId%>" />

<h3>Choose SNMP Interfaces for Data Collection</h3>

<p>
  Listed below are all the interfaces discovered for the selected node. If
  snmpStorageFlag is set to "select" for a collection scheme that includes
  the interface marked as "Primary", only the interfaces checked below will have
  their collected SNMP data stored. This has no effect if snmpStorageFlag is
  set to "primary" or "all".
</p>

<p>
  In order to change what interfaces are scheduled for collection, simple check
  or uncheck the box beside the interface(s) you wish to change, and then
  select "Update Collection".
</p>

<p>
  <strong>Note:</strong>
  Interfaces marked as Primary or Secondary will always be selected
  for data collection.  To remove them, edit the IP address range in the
  collectd configuration file.
</p>

<%=listNodeName(nodeId, nodeLabel)%>

<br/>
      
<% if (interfaces.size() > 0) { %>
          		<table class="standardfirst">
            		<tr>
              			<td class="standardheader" width="5%" align="center">ifIndex</td>
              			<td class="standardheader" width="10%" align="center">IP Address</td>
              			<td class="standardheader" width="10%" align="center">IP Hostname</td>
              			<td class="standardheader" width="5%" align="center">ifType</td>
              			<td class="standardheader" width="10%" align="center">ifDescription</td>
              			<td class="standardheader" width="10%" align="center">ifName</td>
              			<td class="standardheader" width="10%" align="center">ifAlias</td>
              			<td class="standardheader" width="10%" align="center">SNMP Status</td>
              			<td class="standardheader" width="5%" align="center">Collect?</td>
            		</tr>
            		<%=buildTableRows(interfaces, nodeId, interfaces.size())%>
            
          		</table>
          		<% } /*end if*/ %>

		<br/>

          	<input type="button" value="Update Collection" onClick="applyChanges()">
          	<input type="button" value="Cancel" onClick="cancel()"> 
          	<input type="button" value="Select All" onClick="checkAll()">
          	<input type="button" value="Unselect All" onClick="uncheckAll()">
          	<input type="reset">
</form>


<jsp:include page="/includes/footer.jsp" flush="true"/>

<%!
      public String listNodeName(int intnodeid, String nodelabel)
      {
         StringBuffer nodename = new StringBuffer();
                
         nodename.append("<strong>Node ID</strong>: ");
         nodename.append(intnodeid);
         nodename.append("<br/>");
         nodename.append("<strong>Node Label</strong>: ");
         nodename.append(nodelabel);
      	 nodename.append("<br/>\n");
          
         return nodename.toString();
      }
      
%>

<%!
      public String buildTableRows(List interfaces, int intnodeid, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = 0; i < stop; i++)
          {
                
                SnmpManagedInterface curInterface = (SnmpManagedInterface)interfaces.get(i);
		int curnodeid = curInterface.getNodeid();
		String collstatus = null;
		String chkstatus = null;
                if (curnodeid == intnodeid)
		{
		String statustest = curInterface.getStatus();
		if (statustest == null)
                {
                        statustest = "N";
                }
		String key = intnodeid + "+" + curInterface.getIfIndex();
		if (statustest.equals("P"))
		{
			collstatus = "Primary";
			chkstatus = "checked";
		}
		else if (statustest.equals("S"))
		{
			collstatus = "Secondary";
			chkstatus = "checked";
		}
		else if (statustest.equals("C"))
		{
			collstatus = "Collected";
			chkstatus = "checked";
		}
		else
		{
			collstatus = "Not Collected";
			chkstatus = "unchecked";
		}
          	row.append("<tr>\n");
          	row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
                int ifIndex = curInterface.getIfIndex();
                if ( ifIndex > 0 ) {
                  row.append(ifIndex);
                } else {
                  row.append("&nbsp");
                }
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getAddress());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"20%\" align=\"left\">");
	  	row.append(curInterface.getIpHostname());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
	  	row.append(curInterface.getIfType());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getIfDescr());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getIfName());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getIfAlias());
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"10%\" align=\"center\">");
	  	row.append(collstatus);
          	row.append("</td>\n");
          	row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
          	row.append("<input type=\"checkbox\" name=\"collTypeCheck\" value=\"").append(key).append("\" ").append(chkstatus).append(" >");
          	row.append("</td>\n");
      	   	row.append("</tr>\n");
		}
          } /* end i for */
          
          return row.toString();
      }
      
%>
