/**
 * A Ext.panel.Panel specialisation for rendering details about a single job or series
 *
 */
Ext.define('vegl.widgets.DetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widgets.detailspanel',

    constructor : function(config) {
        Ext.apply(config, {
            items: [{
                xtype: 'label',
                itemId: 'title',
                cls: 'jobdesc-title'
            },{
                xtype: 'displayfield',
                itemId: 'description',
                fieldLabel: 'Description',
                labelCls: 'jobdesc-key',
                labelWidth: 120,
                margin: '20 0 0 0'
            },{
                xtype: 'displayfield',
                itemId: 'submitDate',
                labelCls: 'jobdesc-key',
                labelWidth: 120,
                fieldLabel: 'Submitted on',
            },{
                xtype: 'displayfield',
                itemId: 'registeredUrl',
                labelCls: 'jobdesc-key',
                labelWidth: 120,
                fieldLabel: 'GeoNetwork url',
            }]
        });

        this.callParent(arguments);
    },

    /**
     * Updates the body of this panel with the contents of a vegl.models.Job
     */
    showDetailsForJob : function(job) {
        this.down('#title').setText(job.get('name'));
        this.down('#description').setValue(job.get('description'));
        this.down('#submitDate').setValue(job.get('submitDate'));
        this.down('#submitDate').setVisible(true);
        this.down('#registeredUrl').setValue(job.get('registeredUrl'));
        this.down('#registeredUrl').setVisible(true);
    },

    /**
     * Updates the body of this panel with the contents of a vegl.models.Series
     */
    showDetailsForSeries : function(series) {
        this.down('#title').setText(series.get('name'));
        this.down('#description').setValue(series.get('description'));
        this.down('#submitDate').setVisible(false);
        this.down('#submitDate').setValue('');
        this.down('#registeredUrl').setVisible(false);
        this.down('#registeredUrl').setValue('');
    },

    /**
     * Updates the body of this panel with an empty string
     */
    cleanupDetails : function() {
        this.down('#title').setText('');
        this.down('#description').setValue('');
        this.down('#submitDate').setValue('');
        this.down('#registeredUrl').setValue('');
    }
});