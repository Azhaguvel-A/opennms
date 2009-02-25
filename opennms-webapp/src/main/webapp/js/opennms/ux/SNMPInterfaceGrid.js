Ext.namespace("OpenNMS.ux")
OpenNMS.ux.SNMPInterfaceGrid = Ext.extend(OpenNMS.ux.PageableGrid, {

	title:'Physical Interfaces',
	urlTemplate:"rest/nodes/{nodeId}/snmpinterfaces",
	xmlNodeToRecord:'snmpInterface',
	columns:[
	     	{
	    		header:"ID",
	    		dataIndex:"theId",
	    		width:50,
	    		sortable:false,
	    		align:"left",
	    		hidden:true
	    	},{
	    		header:"Index",
	    		dataIndex:"ifIndex",
	    		sortable: true,
	    		width:30,
	    		align:"right"
	    	},{
	    		header:"SNMP ifDescr",
	    		dataIndex:"ifDescr",
	    		width:100,
	    		sortable:true,
	    		align:"left"
	    	},{
	    		header:"SNMP ifName",
	    		dataIndex:"ifName",
	    		sortable: true,
	    		width:90,
	    		align:"left"	
	    	},{
	    		header:"SNMP ifAlias",
	    		dataIndex:"ifAlias",
	    		sortable: true,
	    		width:90,
	    		align:"left"	
	    	},{
	    		header:"SNMP ifSpeed",
	    		dataIndex:"ifSpeed",
	    		sortable: true,
	    		width:100,
	    		align:"right"
	    	},{
	    		header :'SNMP ifAdminStatus',
	    		dataIndex :'ifAdminStatus',
	    		width :100,
	    		sortable :true,
	    		align :'left',
	    		hidden:true
	    	},{
	    		header:"SNMP ifOperStatus",
	    		dataIndex:"ifOperStatus",
	    		sortable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	},{
	    		header:"SNMP ifType",
	    		dataIndex:"ifType",
	    		sortable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	},{
	    		header:"IP Address",
	    		dataIndex:"ipAddress",
	    		sortable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	},{
	    		header:"SNMP ifPhysAddr",
	    		dataIndex:"physAddr",
	    		sortable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	}
	    ],
	    recordTag:'snmpInterface',
	    recordMap:[
					{name:"theId", mapping:"id"},
					{name:"ifAdminStatus", mapping:"ifAdminStatus"},
					{name:"ifDescr", mapping:"ifDescr"},
					{name:"ifIndex", mapping:"ifIndex"},
					{name:"ifName", mapping:"ifName"},
					{name:"ifAlias", mapping:"ifAlias"},
					{name:"ifOperStatus", mapping:"ifOperStatus"},
					{name:"ifSpeed", mapping:"ifSpeed"},
					{name:"ifType", mapping:"ifType"},
					{name:"ipAddress", mapping:"ipAddress"},
					{name:"physAddr", mapping:"physAddr"}
		],
	

	initComponent:function(){
	
		if (!this.nodeId) {
			throw "nodeId must be set in the config for SNMPInterfaceGrid";
		}
		
		OpenNMS.ux.SNMPInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	}

});

Ext.reg('o-snmpgrid', OpenNMS.ux.SNMPInterfaceGrid);