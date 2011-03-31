/*
 * Ext JS Library 2.2
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.namespace('Ext.ux');

/**
 * @class Ext.ux.ComponentLoader
 * Provides an easy way to load components dynamically. If you provide these
 * components with an id you can use Ext.ComponentMgr's onAvailable function
 * to manipulate the components as they are added.
 * @singleton
 */
Ext.ux.ComponentLoader = {
        /*
         *  
         */
        /*
         * Load components from a server resource, config options include
         * anything available in @link Ext.data.Connect#request
         * Note: Always uses the connection of Ext.Ajax 
         */
        load : function(config) {
            Ext.apply(config, {
                callback: this.onLoad.createDelegate(this, [config], true),
                scope: this
            }); 
            if (config) {
                Ext.apply(config.params, {
                    container: config
                });
            }
            Ext.Ajax.request(config);
        },
        // private
        onLoad : function(opts, success, response, ct) {            
            var config = Ext.decode(response.responseText);
            Ext.ComponentMgr.create(config);
        }
};

