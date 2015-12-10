/**
  * the footer 
  */
Ext.define('vegl.widgets.ANVGLFooter', {
	alias	: 'widget.ANVGLFooter',
	extend	: 'Ext.panel.Panel',
	height	: 90,
	html	: '<ul>'
			+ 	'<li><img id="img-gswa" src="img/logos/geographical-survey-of-western-australia.jpg" alt="Geographical Survey of Western Australia" /></li>'
			+ 	'<li><img id="img-eic" src="img/logos/exploration-incentive-scheme.jpg" alt="Exploration Incentive Scheme" /></li>'
			+ '</ul>',
	id		: "anvgl-footer"
});