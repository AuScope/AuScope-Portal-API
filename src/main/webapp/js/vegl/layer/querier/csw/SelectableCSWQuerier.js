/**
 * Class for parsing a set of portal.csw.CSWRecord objects request/response
 * using the Querier interface
 *
 * The resulting Querier allows the selection of the CSWRecord online resources
 * such that they will be downloaded by a VGL job.
 */
Ext.define('vegl.layer.querier.csw.SelectableCSWQuerier', {
    extend: 'portal.layer.querier.Querier',

    constructor: function(config){
        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    query : function(queryTarget, callback) {
        var cswRecord = queryTarget.get('cswRecord');
        if (!cswRecord) {
            callback(this, [], queryTarget);
            return;
        }

        var keywordsString = "";
        var keywords = cswRecord.get('descriptiveKeywords');
        for (var i = 0; i < keywords.length; i++) {
            keywordsString += keywords[i];
            if (i < (keywords.length - 1)) {
                keywordsString += ', ';
            }
        }

        var panel = Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                layout : 'fit',
                items : [{
                    xtype : 'fieldset',
                    items : [{
                        xtype : 'displayfield',
                        fieldLabel : 'Source',
                        value : Ext.util.Format.format('<a target="_blank" href="{0}">Link back to registry</a>', cswRecord.get('recordInfoUrl'))
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Title',
                        anchor : '100%',
                        value : cswRecord.get('name')
                    }, {
                        xtype : 'textarea',
                        fieldLabel : 'Abstract',
                        anchor : '100%',
                        value : cswRecord.get('description'),
                        readOnly : true
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Keywords',
                        anchor : '100%',
                        value : keywordsString
                    },{
                        xtype : 'displayfield',
                        fieldLabel : 'Contact Org',
                        anchor : '100%',
                        value : cswRecord.get('contactOrg')
                    },{
                        fieldLabel : 'Resources',
                        xtype : 'onlineresourcepanel',
                        cswRecords : cswRecord,
                        columns : [{
                            xtype : 'actioncolumn',
                            width : 50,
                            menuDisabled: true,
                            sortable: false,
                            items : [{
                                icon : 'img/disk.png',
                                tooltip : 'Make this resource available to the next job you create.',
                                scope : this,
                                handler : function(grid, rowIndex, colIndex) {
                                    var orpRow = grid.getStore().getAt(rowIndex);
                                    this.showSelectionWindow(orpRow.get('onlineResource'), orpRow.get('cswRecord'));
                                }
                            }]
                        }]
                    }]
                }]
            }]
        });

        callback(this, [panel], queryTarget);
    },

    showSelectionWindow : function(resource, cswRecord) {
        var bboxes = cswRecord.get('geographicElements');
        var bbox = null;
        if (bboxes.length > 0) {
            bbox = bboxes[0];
        }

        var popup = Ext.create('Ext.window.Window', {
            layout : 'fit',
            width : 700,
            height : 400,
            modal : true,
            title : 'Download from ' + cswRecord.get('name'),
            items : [{
                xtype : 'filedownloadpanel',
                itemId : 'download-panel',
                region : bbox,
                url : resource.get('url'),
                localPath : resource.get('name'),
                name : resource.get('name'),
                description : resource.get('description')
            }],
            buttons : [{
                text : 'Capture File',
                iconCls : 'add',
                align : 'right',
                handler : function(btn) {
                    var parentWindow = btn.findParentByType('window');
                    var panel = parentWindow.getComponent('download-panel');
                    var params = panel.getForm().getValues();

                    var myMask = new Ext.LoadMask(parentWindow, {msg:"Saving download request..."});
                    myMask.show();
                    Ext.Ajax.request({
                        url : 'addDownloadRequestToSession.do',
                        params : params,
                        callback : function(options, success, response) {
                            myMask.hide();
                            if (!success) {
                                portal.widgets.window.ErrorWindow.show('Communications Error', 'Unable to communicate with server. Please try again in a few minutes');
                                return;
                            }

                            var responseObj = Ext.JSON.decode(response.responseText);
                            if (!responseObj.success) {
                                portal.widgets.window.ErrorWindow.show(responseObj);
                                return;
                            }

                            parentWindow.close();
                            Ext.Msg.alert('Request Saved', 'Your download request has been saved. You can either continue selecting more data or <a href="jobbuilder.html">create a job</a> to process your existing selections.');
                        }
                    });
                }
            }]
        });

        popup.show();
    }
});