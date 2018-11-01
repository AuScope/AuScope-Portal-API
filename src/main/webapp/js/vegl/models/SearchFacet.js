/**
 * A SearchFacet is a search filter query to be applied to an element within a CSWRecord
 */
Ext.define('vegl.models.SearchFacet', {
    extend: 'Ext.data.Model',

    statics: {
        /**
         * Tests field equal to value
         */
        CMP_EQUAL: 'eq',
        /**
         * Tests field greater than value
         */
        CMP_GREATER_THAN: 'gt',
        /**
         * Tests field less than value
         */
        CMP_LESS_THAN: 'lt',

        /**
         * Value should be interpreted as a OnlineResourceType enum
         */
        TYPE_SERVICETYPE: 'servicetype',
        /**
         * Value should be interpreted as a string
         */
        TYPE_STRING: 'string',
        /**
         * Value should be interpreted as a bounding box object.
         */
        TYPE_BBOX: 'bbox',
        /**
         * Value should be interpreted as a number object (number of milliseconds since Unix epoch).
         */
        TYPE_DATE: 'date'
    },


    fields: [
        { name: 'field', type: 'string' }, //The field or CSW keyword to actually filter on
        { name: 'value', type: 'auto'}, //The value to filter the field against. Could be string
        { name: 'type', type: 'string'}, //The type of the value.
        { name: 'comparison', type: 'string' } //type of comparison to be made
    ]
});
