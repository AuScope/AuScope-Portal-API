/**
 * Configures the help messages depending on what VGL page is or isn't loaded.
 *
 * This is seperated from the top level JS files so there is a physical distinction between
 * this help logic and the logic that runs the actual page.
 */
Ext.define('vegl.HelpHandler', {
    statics : {
        manager : Ext.create('portal.util.help.InstructionManager', {}),

        getMainHelp : function() {
            return [Ext.create('portal.util.help.Instruction', {
                highlightEl : 'vgl-tabs-panel',
                title : 'Find data/layers',
                description : 'In this panel a list of all available datasets in the form of layers will be presented to you. To visualise a layer, select it and press the "Add Layer to Map" button.<br/><br/>Further information about the data behind each layer can be displayed by clicking the icons alongside the layer name.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'vgl-layers-panel',
                title : 'Manage Layers',
                description : 'Whenever you add a layer to the map, it will be listed in this window. Layers can be removed by selecting them and pressing "Remove Layer". Selecting a layer will also bring up any advanced filter options in the window below.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'vgl-filter-panel',
                title : 'Apply filters',
                description : 'Some layers allow you to filter what data will get visualised on the map. If the layer supports filtering, additional options will be displayed in this window. Select "Apply Filter" to update the visualised data on the map'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'center_region',
                anchor : 'right',
                title : 'Visualise Data',
                description : 'The map panel here is where all of the currently added layers will be visualised. You can pan and zoom the map to an area of interest if required.'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'gmap-subset-control',
                anchor : 'right',
                title : 'Select Data',
                description : 'After reviewing one or more layers you can draw a region of interest using this button. All layers with data in the region you draw will be selected for use in a processing job. If the layer supports it, the data will be constrained to the region you select'
            }),Ext.create('portal.util.help.Instruction', {
                highlightEl : 'help-button',
                anchor : 'bottom',
                title : 'More information',
                description : 'For futher information, please consult the online <a target="_blank" href="https://www.seegrid.csiro.au/wiki/NeCTARProjects/VglUserGuide">VGL wiki</a>.'
            })];
        }
    }

}, function() {
    Ext.onReady(function() {
        var helpButtonEl = Ext.get('help-button');

        //Load help for main page
        if (window.location.pathname.endsWith('/gmap.html')) {
            helpButtonEl.on('click', function() {
                vegl.HelpHandler.manager.showInstructions(vegl.HelpHandler.getMainHelp());
            });
        } else if (window.location.pathname.endsWith('/jobbuilder.html')) {
            helpButtonEl.on('click', function() {
                var panel = Ext.getCmp('job-wizard-panel');
                var instructions = panel.getLayout().activeItem.items.get(0).getHelpInstructions();
                if (instructions && instructions.length) {
                    vegl.HelpHandler.manager.showInstructions(instructions);
                } else {
                    Ext.MessageBox.alert('No help', 'The current form doesn\'t have any help. For futher information, please consult the online <a target="_blank" href="https://www.seegrid.csiro.au/wiki/NeCTARProjects/VglUserGuide">VGL wiki</a>.');
                }
            });
        } else {
            helpButtonEl.hide();
        }
    });
});