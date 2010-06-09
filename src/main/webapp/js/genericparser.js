/*!
 * Ext JS Library 3.1.0
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 */

Ext.onReady(function() {
	var XML_NODE_ELEMENT = 1;
    var XML_NODE_TEXT = 3;
	
    var getUrlParams = function(name) {
      name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
      var regexS = "[\\?&]"+name+"=([^&#]*)";
      var regex = new RegExp( regexS );
      var results = regex.exec( window.location.href );
      if( results == null )
        return "";
      else
        return results[1];
    }
    
    var isLeafNode = function(documentNode) {
    	// Figure out if we are a leaf or not
        // (Leaves have no nodes from XML_NODE_ELEMENT)
        var isLeaf = true;
        for (var i = 0; i < documentNode.childNodes.length && isLeaf; i++) {
        	isLeaf = documentNode.childNodes[i].nodeType != XML_NODE_ELEMENT;
        }
        
        return isLeaf;
    }
    
    //This is for creating a TreeNode from a DOM Node
    var createTreeNode = function(documentNode) {
        var treeNode = null;
        
        //We have a leaf
        if (isLeafNode(documentNode)) {
        	treeNode = new Ext.tree.TreeNode({text: 	documentNode.tagName + " = " + documentNode.textContent});
        } else { //we have a parent node
        	var parentName = documentNode.tagName;
        	if (documentNode.attributes.length > 0) {
        		parentName += '(';
	        	for (var i = 0; i < documentNode.attributes.length; i++) {
	        		parentName += ' ' + documentNode.attributes[i].nodeName + '=' + documentNode.attributes[i].value; 
	    		}
	        	parentName += ')';
        	}
        	treeNode = new Ext.tree.TreeNode({text:parentName}); 
        }

        return treeNode;
    }
    
    //Given a DOM tree starting at xmlDocNode, this function returns the equivelant tree in ExtJs Tree Nodes
	var parseXmlTree = function(xmlDocNode, treeNode) {
	    var nodes = [];
	    Ext.each(xmlDocNode.childNodes, function(docNodeChild){
	    	if (docNodeChild.nodeType == XML_NODE_ELEMENT) {
	    		var treeChildNode = createTreeNode(docNodeChild);
            	treeNode.appendChild(treeChildNode);
            	nodes.push(treeNode);
            	parseXmlTree(docNodeChild, treeChildNode);
	    	}
	    }, this);
	
	    return nodes;
	}
	
	//Given a DOM tree starting at xmlDocNode, this function returns a html string representation of the document tree
	var parseXmlToHtml = function(xmlDocNode, indent) {
		var htmlText;
		var isLeaf = isLeafNode(xmlDocNode);
		
		//Create opening tag with attributes
		htmlText = indent + '<span class="tag">&lt;' + xmlDocNode.tagName;
		for (var i = 0; i < xmlDocNode.attributes.length; i++) {
			htmlText += ' ' + xmlDocNode.attributes[i].nodeName + '=&quot;' + xmlDocNode.attributes[i].value + '&quot;'; 
		}
		htmlText += '&gt;</span>';
		if (isLeaf)
			htmlText += xmlDocNode.textContent;
		else
			htmlText += '<br/>';
		
		//Write any children
		Ext.each(xmlDocNode.childNodes, function(docNodeChild) {
			if (docNodeChild.nodeType == XML_NODE_ELEMENT) {
				htmlText += parseXmlToHtml(docNodeChild, indent + '    ');
			}
	    }, this);
		
		//Write closing tag
		htmlText += '<span class="tag">'+ (isLeaf ? '' : indent)+ '&lt;/' + xmlDocNode.tagName + '&gt;</span><br/>';
		
		return htmlText; 
	}
	
	var serviceUrl = getUrlParams('serviceUrl');
	var featureTypeName = getUrlParams('typeName');
	var featureId = getUrlParams('featureId');
	
	var url = 'requestFeature.do'; 
	url += '?serviceUrl=' + serviceUrl;
	url += '&typeName=' + featureTypeName;
	url += '&featureId=' + featureId;
	
	GDownloadUrl(url, function (data, responseCode) {
		var loadingMessage = document.getElementById('loading-message');
		
		if (responseCode != 200) { 
			loadingMessage.textContent = "There was an error requesting information for this marker (" + responseCode + ")";
		} else {
			//Hide the loading message
			loadingMessage.style.display = "none";
			
	        var jsonResponse = eval('(' + data + ')');
	        if (!jsonResponse.success) {
	        	loadingMessage.style.display = "";
	        	loadingMessage.textContent = "There was an error requesting information for this marker";
	        } else {
				//Load our xml document
				var xmlString = jsonResponse.data.gml;
				var parser=new DOMParser();
				var xmlDocument = parser.parseFromString(xmlString,"text/xml");
		
				//Load our rootNode with its children
				var rootNode = createTreeNode(xmlDocument.documentElement);
				parseXmlTree(xmlDocument.documentElement, rootNode);
				rootNode.expanded = true;
				
				//Continuously expand child nodes until we hit a node with something "interesting"
				if (rootNode.childNodes.length == 1)
				{
					var childNode = rootNode.childNodes[0];
					while (childNode != null) {
						childNode.expanded = true;
						
						if (childNode.childNodes.length > 1)
							break;
						else
							childNode = childNode.childNodes[0];
					}
				}
				
				//Display the panel
			    var panel = new Ext.Panel({
			        title: 'Marker Information',
				    renderTo: 'tree',
			        layout: 'border',
			        height: 300,
			        items: [{
			            xtype: 'treepanel',
			            id: 'tree-panel',
			            region: 'center',
			            margins: '2 2 0 2',
			            autoScroll: true,
				        rootVisible: true,
				        root:rootNode
			        }]
			    });
			    
			    var p = new Ext.Panel({ 
			    	title: 'Raw XML', 
			    	collapsible: true,
			    	collapsed: true,
			    	renderTo:'text-panel',
			    	bwrapCfg: {tag: 'pre'},
			        bodyCfg: {tag: 'code'},
			    	html: '<p>' + parseXmlToHtml(xmlDocument.documentElement, '') + '</p>'
			    });
	        }
		}
	});
	
	
    
    //document.write('<hr/>');
    //document.write('<p>' + xmlString + '</p>');
});