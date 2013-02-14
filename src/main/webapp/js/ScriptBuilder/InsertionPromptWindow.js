/**
 * A Window for showing a user the possible ways a code snippet can be inserted into
 * a code window
 */
Ext.define('ScriptBuilder.InsertionPromptWindow', {
    extend : 'Ext.window.Window',

    statics : {
        /** Don't select*/
        OPTION_CANCEL : 0,
        /** Replace the current window*/
        OPTION_REPLACE : 1,
        /** Create a new window*/
        OPTION_CREATE : 2,
        /** Insert at current caret location*/
        OPTION_INSERT : 3
    },

    constructor : function(config) {
        this.addEvents({
            'select' : true
        });

        Ext.apply(config, {
            title : 'Insertion Options',
            buttonAlign : 'right',
            width : 400,
            height : 170,
            modal : true,
            layout : {
                type : 'anchor'
            },
            buttons : [{
               text : 'Add this snippet',
               iconCls : 'add',
               handler : function(button) {
                   var bboxJson = '';
                   var popup = button.ownerCt.ownerCt;
                   var fieldSet = popup.items.getAt(1); //our second item is the fieldset
                   var radioGroup = fieldSet.items.getAt(0);
                   var checkedRadio = radioGroup.getChecked()[0]; //there should always be a checked radio

                   var selectedVal = checkedRadio.inputValue;
                   popup.fireEvent('select', popup, selectedVal);
                   popup.close();
               }
            }],
            items : [{
                xtype : 'label',
                anchor : '100%',
                style : 'font-size: 12px;',
                text : Ext.util.Format.format('What would you like to do with this code snippet? It\'s {0} characters in length.', config.script.length)
            },{
                xtype : 'fieldset',
                anchor : '100%',
                layout : 'fit',
                border : 0,
                items : [{
                    //Our radiogroup can see its item list vary according to the presence of bounding boxes
                    xtype : 'radiogroup',
                    columns : [0.999],
                    items : [{
                        boxLabel : 'Replace the entire script window with this snippet.',
                        name : 'insertion-radio',
                        inputValue : ScriptBuilder.InsertionPromptWindow.OPTION_REPLACE,
                        checked : true
                    },{
                        boxLabel : 'Insert the snippet at the current cursor location.',
                        name : 'insertion-radio',
                        inputValue : ScriptBuilder.InsertionPromptWindow.OPTION_INSERT
                    }]
                }]
            }]
        });

        this.callParent(arguments);

        this.on('close', this.onClose, this);
    },

    onClose : function() {
        this.fireEvent('select', this, ScriptBuilder.InsertionPromptWindow.OPTION_CANCEL);
    }
});
