/*
 * This file is part of the AuScope Virtual Exploration Geophysics Lab (VEGL) project.
 * Copyright (c) 2011 CSIRO Earth Science and Resource Engineering
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
Ext.namespace("ScriptBuilder");

/**
 * This is an 'abstract' base component that provides a number of helper methods useful for generating
 * python source 
 */
ScriptBuilder.BasePythonComponent = Ext.extend(ScriptBuilder.BaseComponent, {

    constructor: function(container, compTitle, compId, compType) {
        ScriptBuilder.BasePythonComponent.superclass.constructor.apply(this, 
                [ container, compTitle, compId, compType]);
    },
    
    /**
     * The tab string used for generating our python script
     */
    _tab : '    ',
    
    /**
     * The new line string used for generating our python script
     */
    _newLine : '\n',
    
    /**
     * Capitalises the first character of a String s
     */
    _capitaliseFirst : function(s) {
        if (s.length == 0) {
            return '';
        }
        
        return s.charAt(0).toUpperCase() + s.substring(1);
    },
    
    /**
     * Generates a Python snippet as a String.
     * 
     *  The snippet will be a 'Get' function for fieldName that returns value.
     */
    _getPrimitiveFunction : function(fieldName, value, baseIndent) {
        var functionText = '';
        
        functionText += baseIndent + 'def get' + this._capitaliseFirst(fieldName) + '(self):' + this._newLine;
        
        //Ext JS forms can't return non string values so we need to be very sure we dont have
        //and integer/float encoded as a string
        var isString = Ext.isString(value);
        if (isString && value.length > 0) {
            if (value.match(/[0-9]/i)) {
                isString = isNaN(parseFloat(value));
            }
        }
        
        if (isString) {
            functionText += baseIndent + this._tab + 'return \'' + value + '\'' + this._newLine;
        } else {
            functionText += baseIndent + this._tab + 'return ' + value + '' + this._newLine;
        }
        functionText += this._newLine;
        
        
        return functionText;
    }
});
