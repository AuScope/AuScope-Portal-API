

/*
 * Is a combination of a MarkerManager with the added extension for generic overlays too
 */
OverlayManager = function(map) {
	this.serviceOverlayList = [];
	this.customOverlayList = [];
	this.markerManager = new MarkerManager(map);
	this.map = map;
};



/**
 * Removes all service overlays and markers and hides all custom overlays
 * (that are managed by this instance) from the map.
 * @return
 */
OverlayManager.prototype.clearOverlays = function() {
	for (var i = 0; i < this.serviceOverlayList.length; i++) {
		this.map.removeOverlay(this.serviceOverlayList[i]);
	}
	this.serviceOverlayList = [];
	this.markerManager.clearMarkers();

	// hide the custom overlays rather then removing them
	for (var i = 0; i < this.customOverlayList.length; i++) {
		this.customOverlayList[i].hide();
	}
};

/**
 * Adds a single overlay to the map and this instance
 * @param overlay
 * @return
 */
OverlayManager.prototype.addOverlay = function(overlay) {
	this.map.addOverlay(overlay);
	this.serviceOverlayList.push(overlay);
};

/**
 * Iterates through every layer in this manager and updates the overlay zOrder
 * @param newZOrder
 * @return
 */
OverlayManager.prototype.updateZOrder = function(newZOrder) {
	for (var i = 0; i < this.serviceOverlayList.length; i++) {
		this.serviceOverlayList[i].zPriority = newZOrder;
        this.map.removeOverlay(this.serviceOverlayList[i]);
        this.map.addOverlay(this.serviceOverlayList[i]);
	}
};

/**
 * Iterates through every WMS layer sets the opacity to the specified value
 * @param newOpacity
 * @return
 */
OverlayManager.prototype.updateOpacity = function(newOpacity) {
	for (var i = 0; i < this.serviceOverlayList.length; i++) {
		if (this.serviceOverlayList[i] instanceof GTileLayerOverlay) {
			this.serviceOverlayList[i].getTileLayer().opacity = newOpacity;
	        this.map.removeOverlay(this.serviceOverlayList[i]);
	        this.map.addOverlay(this.serviceOverlayList[i]);
		}
	}
};

/**
 * Adds a custom overlay to the list
 * @param overlay
 */
OverlayManager.prototype.addCustomOverlay = function(overlay) {
	this.customOverlayList.push(overlay);
};

/**
 * Show all custom overlays
 */
OverlayManager.prototype.showCustomOverlays = function() {
	for (var i = 0; i < this.customOverlayList.length; i++) {
		this.customOverlayList[i].show();
	}
};


