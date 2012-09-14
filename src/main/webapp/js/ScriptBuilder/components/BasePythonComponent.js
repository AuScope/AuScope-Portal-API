/**
 * This is an 'abstract' base component that provides a number of helper methods useful for generating
 * python source
 */
Ext.define('ScriptBuilder.components.BasePythonComponent', {
    extend : 'ScriptBuilder.components.BaseComponent',

    constructor: function(config) {
        this.callParent(arguments);
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
     * Given an array of field names as vegl.models.Parameter objects or strings, generate a simple
     * Plain old Python Object (POPO) class that can get the required fields
     */
    _popoClass : function(className, fields) {
        var classText = '';
        var getName = function(obj) {
            if (obj instanceof vegl.models.Parameter) {
                return obj.get('name');
            }
            return obj;
        }

        classText += 'class ' + className + ':' + this._newLine;

        //Local variables
        for (var i = 0; i < fields.length; i++) {
            classText += this._tab + '_' + getName(fields[i]) + ' = None' + this._newLine;
        }
        classText += this._newLine;

        //Constructor definition
        classText += this._tab + 'def __init__(self';
        for (var i = 0; i < fields.length; i++) {
            classText += ', ' + getName(fields[i]);
        }
        classText += '):' + this._newLine;

        //Constructor body
        for (var i = 0; i < fields.length; i++) {
            classText += this._tab + this._tab + 'self._' + getName(fields[i]) + ' = ' + getName(fields[i]) + this._newLine;
        }
        classText += this._newLine;

        //Getter fields
        for (var i = 0; i < fields.length; i++) {
            classText += this._getPrimitiveFunction(getName(fields[i]), 'self._' + getName(fields[i]), this._tab, true);
        }
        classText += this._newLine;

        return classText;
    },

    /**
     * given a value of a primitive return the (possibly quoted) equivalent in python that can be assigned
     * to a variable or passed as an argument literal.
     *
     * forceUnquotedValue - [Optional] if true value will be forcibly printed without quotes
     */
    _getPrimitiveValue : function(value, forceUnquotedValue) {

        //Ext JS forms can't return non string values so we need to be very sure we dont have
        //and integer/float encoded as a string
        var isString = false;
        if (!forceUnquotedValue) {
            isString = Ext.isString(value)
            if (isString && value.length > 0) {
                if (value.match(/[0-9]/i)) {
                    isString = isNaN(parseFloat(value));
                }
            }
        }

        if (isString) {
            return "'" + value + "'";
        }

        return value;
    },

    /**
     * Generates a Python snippet as a String.
     *
     *  The snippet will be a 'Get' function for fieldName that returns value.
     *
     *  forceUnquotedValue - [Optional] if true value will be forcibly printed without quotes
     */
    _getPrimitiveFunction : function(fieldName, value, baseIndent, forceUnquotedValue) {
        var functionText = '';

        functionText += baseIndent + 'def get' + this._capitaliseFirst(fieldName) + '(self):' + this._newLine;
        functionText += baseIndent + this._tab + 'return ' + this._getPrimitiveValue(value, forceUnquotedValue) + '' + this._newLine;
        functionText += this._newLine;


        return functionText;
    }
});
