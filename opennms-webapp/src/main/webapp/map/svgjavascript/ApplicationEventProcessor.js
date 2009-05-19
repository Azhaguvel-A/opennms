//if double-click on an element (map) open the map 
function onClickMapElement(evt)
{
	if ((typeof map) == "object")
	{	
		map.startSelectionRectangle = null;
		map.endSelectionRectangle = null;
						
	}
	
	//select the element
	resetSelectedObjects();
	map.selectedObjects=new Array();
	map.selectedObjects.push( evt.target.parentNode );
	evt.target.parentNode.setAttributeNS(null,"opacity", "0.5");
	
	var id = evt.target.parentNode.getAttributeNS(null,"id");
	var mapElement = map.mapElements[id];
		
	// view info node
	clearDownInfo();			
	writeTopInfoText(mapElement.getInfo());

	setContextMenuForElement(evt, mapElement);

	if (evt.detail == 2)
	{

		if(mapElement.isNode())
		{
			//openContextMenu(mapElement)	;
			openLink('element/node.jsp?node='+mapElement.getNodeId(),'','left=0,top=0, width='+screen.width+',height='+screen.height+',toolbar=no,menubar=no,location=no,scrollbars=1,resize=1,minimize=1');
			
		}
	
		if(mapElement.isMap())
		{
			openMap(mapElement.getMapId());
		}
			
	}
}

function openContextMenu(mapElement) {
	//create new window instance and add it to the Windows array with the windowId as a key
	var transx = mapElement.getX() + mapElemDimension;
	var transy = mapElement.getY() ;
	var textEl = document.createElementNS(svgNS,"text");
	textEl.setAttributeNS(null, "x", transx);
	textEl.setAttributeNS(null, "y",transy);
	textEl.setAttributeNS(null, "id",id+"elementlink");
	textEl.setAttributeNS(null, "font-size",titleFontSize);
	textEl.setAttributeNS(null,"font-family",textFamily);
	textEl.setAttributeNS(null, "cursor","pointer");
    //textEl.appendChild(document.createTextNode());
	//textEl.addEventListener("click", "openLink('element/node.jsp?node='"+mapElement.getNodeId()+",'','left=0,top=0, width='"+screen.width+"',height='"+screen.height+"',toolbar=no,menubar=no,location=no,scrollbars=1,resize=1,minimize=1');", false);
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "dy","12");
	var tspanContent = document.createTextNode(mapElement.getLabel()+": Node Page");
	tspan.appendChild(tspanContent);
	textEl.appendChild(tspan);
	
	return document.getElementById("Windows").appendChils(textEl);
}

function onMouseDownOnMapElement(evt)
{	
	if ((typeof map) == "object")
	{ 
  		var mapElement = map.mapElements[evt.target.parentNode.getAttributeNS(null,"id")];		
		setContextMenuForElement(evt, mapElement);

		var matrix;
		// track the origin
		map.draggableObject = null;
		if(map.selectedObjects==null){	
			//add the element to the selection
			map.selectedObjects=new Array();
			map.selectedObjects.push( evt.target.parentNode );
			evt.target.parentNode.setAttributeNS(null,"opacity", "0.5");
		}else{
			var found=false;
			for(selObj in  map.selectedObjects){
				if(map.selectedObjects[selObj] == evt.target.parentNode){
					found=true;
					break;
				}
			}
			if(!found){
				resetSelectedObjects();
				resetDraggableObject();
				map.selectedObjects=new Array();
				map.selectedObjects.push( evt.target.parentNode );					
			}
						
		}
		
		if(map.selectedObjects.length==1){
			// view info node
			clearDownInfo();			
			writeTopInfoText(mapElement.getInfo());
		}
		map.draggableObject =  evt.target.parentNode;
		// get the relative position
		var matrix = evt.target.parentNode.getCTM();
  		var mouse = getMouse(evt);

  		map.offset.x = (matrix.e - mouse.x); //document.getFirstChild().currentScale;
  		map.offset.y = (matrix.f - mouse.y); //document.getFirstChild().currentScale;
		
	
		
		//delete the element if flag 'deletingMapElem' is true
		if(deletingMapElem==true){
			deleteMapElement(mapElement);
			deleteMapElementSetUp();
		}
		
		//add the element neighbors if flag 'addingMapElemNeighbors' is true
		if(addingMapElemNeighbors==true){
			var elemMap = mapElement;
			if(elemMap.isMap()){
				writeDownInfo("Cannot add neighbors to a map");
			} else {
				addMapElemNeighSetUp(elemMap.getNodeId());
			}
		}

		//set the icon selected into the relative selection list to the selected element
		if(settingMapElemIcon==true){
			mapElement.icon=selectedMEIconInList;
			mapElement.image.setAttributeNS(xlinkNS, "xlink:href", MEIconsSortAss[selectedMEIconInList]);
			map.render();
			setIconSetUp();
		}
	}
}


function removeSelectionRect(){
		var selectionRectangle = document.getElementById("selectionRect");
		if(selectionRectangle!=undefined)
			selectionRectangle.parentNode.removeChild(selectionRectangle);
}


function createSelectionRect(evt, x, y, width, height){
	//security check
	
	if(map.startSelectionRectangle!=null){
		var selectionRectangle = document.getElementById("selectionRect");
		if(selectionRectangle!=undefined)
			selectionRectangle.parentNode.removeChild(selectionRectangle);
		selectionRectangle = document.createElementNS("http://www.w3.org/2000/svg","rect");
		selectionRectangle.setAttributeNS(null, "onmouseup", "onMouseUp(evt);" );
		selectionRectangle.setAttributeNS(null, "onmousemove", "onMouseMove(evt);" );
		selectionRectangle.setAttributeNS(null,"x", x);
		selectionRectangle.setAttributeNS(null,"y", y);
		selectionRectangle.setAttributeNS(null,"width", width);
		selectionRectangle.setAttributeNS(null,"height", height);
		selectionRectangle.setAttributeNS(null,"id", "selectionRect");
		selectionRectangle.setAttributeNS(null,"style","fill:white; fill-opacity:0; stroke:black;stroke-width:1;stroke-opacity:0.5;stroke-dasharray:1");		
		map.getSvgNode().appendChild(selectionRectangle);	
	}else{
		removeSelectionRect();
	}
	
}

function onMouseDownOnMap(evt)
{

	if ((typeof map) == "object" && currentMapId != MAP_NOT_OPENED){
		map.startSelectionRectangle = getMouse(evt);
		
	}
	
	disableContextMenu(evt);
	
	// remove node information
	clearTopInfo();
	clearDownInfo();
	
	//close the menu
	closeAllMenu();
	
	//clear the actions started
	clearActionsStarted();	

	resetSelectedObjects();
	resetDraggableObject();
	

}

function resetSelectedObjects(){
	
	if ((typeof map) == "object")
	{	
		if(map.selectedObjects != null){
			for(selObj in map.selectedObjects)
				map.selectedObjects[selObj].setAttributeNS(null,"opacity", "0.9");		
		}

		map.selectedObjects = null;
	}
}


function resetDraggableObject(){
	
	if ((typeof map) == "object")
	{
		map.draggableObject = null;
	}
}		
		
function onMouseDownOnLink(evt)
{
	
	disableContextMenu(evt);
	resetSelectedObjects();
	if ((typeof map) == "object")
	{
		clearDownInfo();
		clearActionsStarted();
		
		var mapLink = map.mapLinks[evt.target.getAttributeNS(null,"id")];
		writeTopInfoText(mapLink.getInfo());
		
	}		
}

function onMouseMove(evt)
{
	var mapsvgRoot = document.documentElement;
	var pan = mapsvgRoot.currentTranslate;
	var zoom = mapsvgRoot.currentScale;
	var mouse = getMouse(evt);
	if((typeof map) == "object" && map.startSelectionRectangle!=null ){
		var minX = map.startSelectionRectangle.x;
		var maxX = mouse.x;
		if(map.startSelectionRectangle.x > mouse.x){
			minX = mouse.x;
			maxX = map.startSelectionRectangle.x;
		}
		var minY = map.startSelectionRectangle.y;
		var maxY = mouse.y;
		if(map.startSelectionRectangle.y > mouse.y){
			minY = mouse.y;
			maxY = map.startSelectionRectangle.y;
		}		
		minX = (minX- pan.x)/zoom;
		maxX = (maxX- pan.x)/zoom;
		minY = (minY- pan.y)/zoom;
		maxY = (maxY- pan.y)/zoom;
		
		
		createSelectionRect(evt, minX, minY, maxX-minX, maxY-minY)
		

	}
	
	if ((typeof map) == "object" && map.selectedObjects != null && map.draggableObject!=null)
	{
	var mapElements = new Array();
	if(map.selectedObjects != null){
		for(selObj in map.selectedObjects){
			map.selectedObjects[selObj].setAttributeNS(null,"opacity", "0.5");
			mapElements.push( map.mapElements[map.selectedObjects[selObj].getAttributeNS(null,"id")] );
		}
	}
	var transScaledPoint = mapsvgRoot.createSVGPoint();
	//alert(evt.target.parentNode+" "+evt.target.parentNode.getAttributeNS(null,"id"));
	

	var movingElement = map.mapElements[map.draggableObject.getAttributeNS(null,"id")];
	
	var dX = mouse.x - movingElement.x*zoom;	
	var dY = mouse.y - movingElement.y*zoom;
		
  	if(mapElements.length!=0)
  		for(drObj in mapElements){
  			
			transScaledPoint.x = parseInt(((mapElements[drObj].x*zoom+dX+map.offset.x) - pan.x) / zoom);
			transScaledPoint.y = parseInt(((mapElements[drObj].y*zoom+dY+map.offset.y) - pan.y) / zoom);
			
			if(transScaledPoint.x<=0){
				transScaledPoint.x=0;
			}
			if(transScaledPoint.x>=(map.getWidth()-(mapElemDimension))){
				transScaledPoint.x=map.getWidth()-(mapElemDimension);
			}			
			if(transScaledPoint.y<=0){
				transScaledPoint.y=0;
			}	
			if(transScaledPoint.y>=(map.getHeight()-(mapElemDimension))){
				transScaledPoint.y=map.getHeight()-(mapElemDimension);
			}			
			
			mapElements[drObj].move(transScaledPoint.x, transScaledPoint.y);
			map.redrawLinkOnElement(mapElements[drObj].id);
		}
	}

}

function onMouseUp(evt)
{
	//alert("mouse up!");
	var mapsvgRoot = document.documentElement;
	var zoom = mapsvgRoot.currentScale;
	var pan = mapsvgRoot.currentTranslate;
	disableContextMenu(evt);
	removeSelectionRect();
	//reset the selection rectangle

	
	if ((typeof map) == "object")
	{
		//resetSelectedObjects();
		resetDraggableObject();
		
		//
		if(map.startSelectionRectangle!=null){
			map.endSelectionRectangle = getMouse(evt);
			
			var minX = (map.startSelectionRectangle.x < map.endSelectionRectangle.x)?map.startSelectionRectangle.x:map.endSelectionRectangle.x;
			var maxX = (map.startSelectionRectangle.x > map.endSelectionRectangle.x)?map.startSelectionRectangle.x:map.endSelectionRectangle.x;
			var minY = (map.startSelectionRectangle.y < map.endSelectionRectangle.y)?map.startSelectionRectangle.y:map.endSelectionRectangle.y;
			var maxY = (map.startSelectionRectangle.y > map.endSelectionRectangle.y)?map.startSelectionRectangle.y:map.endSelectionRectangle.y;
			
			map.selectedObjects = new Array();
			
			var count=0;
			for(mEl in map.mapElements){
				
				if(( ((map.mapElements[mEl].x+mapElemDimension/2)*zoom) +pan.x )>=minX && ( ((map.mapElements[mEl].x+mapElemDimension/2)*zoom) +pan.x)<=maxX && ( ((map.mapElements[mEl].y+mapElemDimension/2)*zoom) +pan.y)>=minY && ( ((map.mapElements[mEl].y+mapElemDimension/2)*zoom) +pan.y)<=maxY){
					map.mapElements[mEl].getSvgNode().setAttributeNS(null,"opacity", "0.5");
					map.selectedObjects.push(map.mapElements[mEl].getSvgNode());
					count++;
				}
			}
			if(count>0)
			writeDownInfo(count+" element/s selected.")
			map.startSelectionRectangle=null;
			map.endSelectionRectangle=null;	
		}			
		
	}
}


