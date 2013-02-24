/**
 * Configures the help messages depending on what VGL page is or isn't loaded.
 *
 * This is seperated from the top level JS files so there is a physical distinction between
 * this help logic and the logic that runs the actual page.
 */
Ext.define('vegl.HelpHandler', {
    statics : {
        manager : Ext.create('portal.util.help.InstructionManager', {}),

        /**
         * Asynchronously start an animation (with the specified delay in ms) that will highlight
         * the specified element with an animated arrow
         *
         * @param delay The delay in milli seconds
         * @param element An Ext.Element to highlight
         */
        highlightElement : function(delay, element) {
            var task = new Ext.util.DelayedTask(function() {
                var arrowEl = Ext.getBody().createChild({
                    tag : 'img',
                    src : 'img/right-arrow.png',
                    width : '32',
                    height : '32',
                    style : {
                        'z-index' : 999999
                    }
                });

                //Figure out the x location of the element (in absolute page coords)
                var xLocation = element.getLeft();

                Ext.create('Ext.fx.Animator', {
                    target: arrowEl,
                    duration: 7000,
                    keyframes: {
                        0: {
                            opacity: 1
                        },
                        20: {
                            x: xLocation - 32
                        },
                        30: {
                            x: xLocation - 52
                        },
                        40: {
                            x: xLocation - 32
                        },
                        50: {
                            x: xLocation - 52
                        },
                        60: {
                            x: xLocation - 32
                        },
                        120: {

                        },
                        160: {
                            opacity : 0
                        }
                    },
                    listeners : {
                        afteranimate : Ext.bind(function(arrowEl) {
                            arrowEl.destroy();
                        }, this, [arrowEl])
                    }
                });
            });

            task.delay(delay);
        }
    }

}, function() {
    Ext.onReady(function() {
        var helpButtonEl = Ext.get('help-button');

        //If its a new user - highlight the help button an in unobtrusive manner
        if (window['NEW_SESSION'] === 'true') {
            vegl.HelpHandler.highlightElement(5000, helpButtonEl);
        }

        //Load help for main page
        if (window.location.pathname.endsWith('/gmap.html')) {
            helpButtonEl.on('click', function() {
                vegl.HelpHandler.manager.showInstructions([Ext.create('portal.util.help.Instruction', {
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
                })]);
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
        } else if (window.location.pathname.endsWith('/joblist.html')) {
            helpButtonEl.on('click', function() {
                var vp = Ext.getCmp('vgl-joblist-viewport');
                var seriesP = vp.queryById('vgl-series-panel');
                var jobsP = vp.queryById('vgl-jobs-panel');
                var detailsP = vp.queryById('vgl-details-panel');
                var registerB = vp.queryById('btnRegister');

                vegl.HelpHandler.manager.showInstructions([Ext.create('portal.util.help.Instruction', {
                    highlightEl : seriesP.getEl(),
                    title : 'Choose a Series',
                    anchor : 'right',
                    description : 'This panel will display every series that you\'ve created. Selecting a series will display all jobs owned by that series. You can manage an entire series by right clicking it or by selecting the desired series and pressing \'Actions\''
                }),Ext.create('portal.util.help.Instruction', {
                    highlightEl : jobsP.getEl(),
                    title : 'Manage Jobs',
                    anchor : 'right',
                    description : 'After selecting a series, a list of all jobs belonging to that series will be displayed here. Selecting a job will bring up information about that job in the details panel. You can manage individual jobs by right clicking them or by selecting a job and pressing \'Actions\'<br/><br/>If you\'d like more information about the job states, please consult the <a target="_blank" href="https://www.seegrid.csiro.au/wiki/NeCTARProjects/VglUserGuide">VGL wiki</a>.'
                }),Ext.create('portal.util.help.Instruction', {
                    highlightEl : detailsP.getEl(),
                    title : 'Inspect Job',
                    anchor : 'left',
                    description : 'When you select a job, information about the job\'s metadata, logs and input/output files will be displayed here.<br/><br/>The description tab will provide a rough overview of the job. It will also contain a link to a remote provenance record (if one has been published).<br/><br/>The logs tab will display the entire console logs of a finished job. The logs will be divided into relevant sections. Please note that logs will not be available until after the job has finished processing.<br/><br/>The files tab contains all input/output files. Files from this list can be downloaded individually or as an archived zip file.'
                }),Ext.create('portal.util.help.Instruction', {
                    highlightEl : registerB.getEl(),
                    title : 'Register Job',
                    anchor : 'top',
                    description : 'If a job contains information that you would like to publish, selecting this button on a completed job will publish all provenance information to a remote Geonetwork registry. A link to the published record will be displayed in the \'Details\' Panel, under the \'Description\' tab.<br/><br/>Please note that this button will disabled unless you select a job whose status is \'Done\''
                })]);
            });
        } else {
            helpButtonEl.hide();
        }
    });
});