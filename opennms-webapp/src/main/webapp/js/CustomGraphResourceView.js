Ext.BLANK_IMAGE_URL = "extJS/resources/images/default/s.gif";

function customResourceViewInit(elementId, dataArray, destURL){
	var pagesize = 10;
	var fields = [
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      		 ]
	 
	var simpleStore = new Ext.data.Store({
		autoLoad:true,
		proxy: new OpenNMS.ux.LocalPageableProxy(dataArray, pagesize),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, fields)
	});

	
	
	var resourcesGrid = new OpenNMS.ux.PageableGrid({
		id:"resourceGrid",
		columns:[{
		 			id: 'resource',
		 			header :'Resources',
		 			align :'left'
		 		}],
		selectionModel:new Ext.grid.RowSelectionModel({
			singleSelect:true
		}),
		pageSize:pagesize,
		store:simpleStore,
		width:"100%",
		height:250,
		region:'west',
		enableDragDrop: true,
		stripeRows: true,
		onDoubleClick:viewChildResource
		
	});
	
	
	var gridPanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:300,
		items:[
				resourcesGrid,
				{
					xtype:"panel",
					border:false,
					height:10
				},{
					xtype:"button",
					text:"View Child Resource",
					style:'padding-top:10px;',
					handler:viewChildResource
				},{
					xtype:"button",
					text:"Choose Child Resource",
					handler:chooseChildResource
				}
				
		],
		
		renderTo:elementId
	});
	
	function viewChildResource(){
		if(resourcesGrid.getSelectionModel().getSelected() != undefined){
				window.location = getBase() + "customGraphChooseResource.htm?selectedResourceId=&resourceId=" + resourcesGrid.getSelectionModel().getSelected().data.id;
		}else{
			alert("Please Select a Resource");
		}
	};
	
	function chooseChildResource(){
		if(resourcesGrid.getSelectionModel().getSelected() != undefined){
				window.location = getBase() + "customGraphEditDetails.htm?resourceId=" + resourcesGrid.getSelectionModel().getSelected().data.id;
		}else{
			alert("Please Select a Resource");
		}
	};
	
	function getBase() {
		var base = (document.getElementsByTagName ('BASE')[0] && document.getElementsByTagName('BASE')[0].href);
		if (base) {
        base = base + "KSC/";
      } else {
			if (Ext.isIE) {
				base = "";
			} else {
				base = "KSC/";
			}
		}
		return base;
	}

	function getResourceIds(){
		if(graphStore.getCount() > 0){
			var query = new Array();
			var records = graphStore.getRange(0, graphStore.getCount() - 1 );
			
			for(var i = 0; i < records.length; i++){
				query.push("resourceId=" + records[i].data.id);
			}
			
			var params = query.join("&");
			return params;
		}else{
			return "fail";
		}	
	};
	
};
