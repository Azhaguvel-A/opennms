	// https://m.sceur.ch/opennms/rss.jsp?feed=notification

	function loadproperties() {
		<!--
		var prefsfile = air.File.applicationStorageDirectory;
		prefsFile = prefsFile.resolvePath("preferences.xml");
		readXML();
		stream = new air.FileStream();
		if (prefsFile.exists) {
    		stream.open(prefsFile, air.FileMode.READ);
    		processXMLData();
			}
		-->
	}

	function init(){
		var request=new air.URLRequest("http://demo.opennms.org/opennms/rss.jsp?feed=outage");
		var loader=new air.URLLoader();
		loader.addEventListener(air.Event.COMPLETE, outagesCompleteHandler);
		loader.load(request);
	}

	function outagesCompleteHandler(event)
	{ 
		mydataobj=document.getElementById("mydata");
		var loader2 = event.target;
        	air.trace("loaded" + loader2.data);
		var parser=new DOMParser();
		var xmldoc=parser.parseFromString(loader2.data,"text/xml");
		air.trace("calling parser" + xmldoc);

// 
		var entries=xmldoc.getElementsByTagName("entry");
		air.trace("found " + entries.length + " entries.");
		var position = document.getElementById("results");
		var oldtable = document.getElementById("resultstable");
		oldtable.parentNode.removeChild(oldtable);
		
		
	
			
			

		var table = document.createElement("table");
		table.setAttribute("id","resultstable");
		position.appendChild(table);

		for(Index = 0; Index < entries.length; Index++) {
			var title=entries[Index].getElementsByTagName("title")[0].textContent;
			var published=entries[Index].getElementsByTagName("published")[0].textContent;
			var link = entries[Index].getElementsByTagName("link")[0].getAttribute("href");
			//var myline = "" + title + " " + published + "\n";
			//mydataobj.appendChild(document.createTextNode(myline));
			writeFileResult(title,published,link);

			}


		//var date = items[0].getElementsByTagName("published");
		air.trace("finished the rss magic");

//

		// output
		//mydataobj.appendChild(document.createTextNode(titles[1].textContent)); //loader2.data

		function writeFileResult(title, published, link) 
			{
		
    			var tr = document.createElement("tr");
    			tr.setAttribute("class", "grid");
			
    			var td = document.createElement("td");
			td.innerHTML= "<a href=\""+ link + "&nonavbar=true&quiet=true\">" + title + "</a>";
    			tr.appendChild(td);
			
    			td = document.createElement("td");
			td.innerHTML= "<a href=\""+ link + "&nonavbar=true&quiet=true\">" + published + "</a>";
    			tr.appendChild(td);
			
			
    			table.appendChild(tr); 
			}
		} 

