/**
 * A Ext.panel.Panel specialisation for rendering details about a single job or series
 *
 */
Ext.define('vegl.widgets.DetailsPanel', {
    extend : 'Ext.panel.Panel',
    alias : 'widgets.detailspanel',

    jobTemplate : null,
    seriesTemplate : null,

    constructor : function(config) {

        this.seriesTemplate = new Ext.Template(
            '<p class="jobdesc-title">{name}</p><br/>',
            '<p class="jobdesc-key">Description:</p><br/><p>{description}</p>');
        this.seriesTemplate.compile();

        this.jobTemplate = new Ext.Template(
            '<p class="jobdesc-title">{name}</p>',
            '<table width="100%"><col width="150px"></col><col class="jobdesc-content"></col>',
            '<tr><td class="jobdesc-key">Description:</td><td>{description}</td></tr>',
            '<tr><td class="jobdesc-key">Submitted on:</td><td>{submitDate}</td></tr>',
            '<tr><td class="jobdesc-key">Geonetwork url:</td><td><a href="{registeredUrl}" target="_blank">{registeredUrl}</a></td></tr></table><br/>');
            //'<p class="jobdesc-key">Description:</p><br/><p>{description}</p>');
        this.jobTemplate.compile();

        config.html = "";

        this.callParent(arguments);
    },

    /**
     * Updates the body of this panel with the contents of a vegl.models.Job
     */
    showDetailsForJob : function(job) {
        this.jobTemplate.overwrite(this.getEl(), job.data);
    },

    /**
     * Updates the body of this panel with the contents of a vegl.models.Series
     */
    showDetailsForSeries : function(series) {
        this.seriesTemplate.overwrite(this.getEl(), series.data);
    }
});