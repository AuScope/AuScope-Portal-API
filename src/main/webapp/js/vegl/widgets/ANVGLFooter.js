/**
  * the footer 
  */
Ext.define('vegl.widgets.ANVGLFooter', {
	alias	: 'widget.ANVGLFooter',
	extend	: 'Ext.panel.Panel',
	height	: 90,
	html	: '<ul>'
        +     '<li><img height="90" width="90" id="img-csiro" src="img/logos/CSIRO_Grad_RGB_lr.jpg" alt="CSIRO" /></li>'
        +     '<li><img height="90" width="281" id="img-auscope" src="img/logos/auscope.jpg" alt="CSIRO" /></li>'
			+ '</ul>',
	id		: "anvgl-footer"
});