/**
 * A Ext.grid.Panel specialisation for rendering the series
 * available to the current user.
 *
 * Adds the following events
 * selectseries : function(vegl.widgets.SeriesPanel panel, vegl.models.Series selection) - fires whenever a new Series is selected
 * refreshDetailsPanel : function(vegl.widgets.SeriesPanel panel, vegl.models.Series series) - fires whenever a Series is successfully deleted
 * error : function(vegl.widgets.SereisPanel panel, String message) - fires whenever a comms error occurs
 */
Ext.define('vegl.widgets.FolderPanel', {
    extend : 'Ext.tree.Panel',
    alias : 'widgets.seriesfolderpanel',

    cancelSeriesAction : null,
    deleteSeriesAction : null,
    contextMenu : null,
    seriesStore : null,

    constructor : function(config) {
        
      
        
         this.seriesStore =Ext.create('Ext.data.Store', {
            model : 'vegl.models.Series',
            proxy : {
                type : 'ajax',
                url : 'secure/querySeries.do',
                reader : {
                    type : 'json',
                    rootProperty : 'data'
                },
                listeners : {
                    exception : function(proxy, response, operation) {
                        responseObj = Ext.JSON.decode(response.responseText);
                        errorMsg = responseObj.msg;
                        errorInfo = responseObj.debugInfo;
                        portal.widgets.window.ErrorWindow.showText('Error', errorMsg, errorInfo);
                    }
                }
            },               
            autoLoad : true
        });
        
        
        var treeStore = this.getBaseTreeStore();
        
        this.seriesStore.on('load',function(store, records, successful, operation, node, eOpts){
            var folderRoot = treeStore.getRoot().firstChild
            folderRoot.set('text',records[0].get('user'))
            for(var i=0; i < records.length;i++){
                folderRoot.insertChild(0, { id:records[i].get('id') ,text: records[i].get('name'), leaf: true })
            }
            folderRoot.expand(true);           
        })


        Ext.apply(config, {
            store : treeStore,
            rootVisible: false,
            buttons: [{
            text: 'Add Folder',
            tooltip: 'Add a folder for catagorizing',
            handler: Ext.bind(this.addFolderForm, this),
            cls: 'x-btn-text-icon',
            iconCls: 'add'      
          }]
        });   

        this.callParent(arguments);

        this.on('select', this.onSeriesSelection, this);      
        this.on('viewready', function(folderPanel,eOpts){            
            folderPanel.store.on('nodeexpand', function(store, records, successful, eOpts){                
                folderPanel.getSelectionModel().select(store.lastChild);
            });            
            
        });
       
    },
    
    getBaseTreeStore : function(){
        return Ext.create('Ext.data.TreeStore', {
            root: {
                expanded: true,
                children: [                 
                    { text: "User Email", expanded: false },
                  
                ]
            }
        });
    },
   
    
    refresh : function(){
        this.store.getRoot().firstChild.removeAll();
        this.seriesStore.load();
    },
    
    addFolderForm : function(){
        var me = this;
        var win = Ext.create('Ext.window.Window', {
            title: 'Folder Name',
            height: 150,
            width: 400,
            frameHeader : false,
            layout: 'fit',
            items: {
                xtype: 'form',                
                defaultType: 'textfield',
                items: [{
                    fieldLabel: 'Folder Name',
                    margin : '5 5 5 5',
                    name: 'seriesName',
                    allowBlank: false  ,
                    width : '90%'
                },{
                    fieldLabel: 'Description',
                    name: 'seriesDesc',
                    margin : '5 5 5 5',
                    allowBlank: false,
                    width : '90%'
                }],
                buttons  : [{
                    text: 'Add',
                    handler: function() {
                        var form = this.up('form').getForm();                        
                        me.createFolder(form.getFieldValues().seriesName,form.getFieldValues().seriesDesc);
                        this.up('window').close();
                    }
                }]
            }
        });
        
        win.show();
    },
    
    createFolder : function(seriesName, seriesDesc) {        
       var me = this;
        Ext.Ajax.request({
            url: 'secure/createFolder.do',
            params: {
                'seriesName': seriesName,
                'seriesDescription': seriesDesc
            },
            callback : function(options, success, response) {
                if (success) {
                    me.refresh();
                  return;
                } else {
                    errorMsg = "There was an internal error saving your series.";
                    errorInfo = "Please try again in a few minutes or report this error to cg_admin@csiro.au.";
                }

                portal.widgets.window.ErrorWindow.showText('Create new series', errorMsg, errorInfo);
               
                return;
            }
        });
        
    },

    onSeriesSelection : function(sm, series) {
        if(series.isLeaf()){
            var name = series.get('text');
            var returnSeries = this.seriesStore.findRecord('name',name)
            this.fireEvent('selectseries', this, returnSeries);
        }
        
    }
});