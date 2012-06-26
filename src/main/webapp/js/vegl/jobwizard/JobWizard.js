/**
 * A GUI widget for stringing together a collection BaseJobWizardForm objects into a Job Wizard for VEGL
 *
 * A JobWizard is a specialised Ext.Panel
 */

Ext.define('vegl.jobwizard.JobWizard', {
    extend : 'Ext.Panel',
    /**
     * The internal state of the wizard (managed by internal forms)
     */
    wizardState : null,

    /**
     * Creates a new JobObjectForm form configured to write/read to the specified global state
     */
    constructor: function() {
        var jobWizard = this;
        jobWizard.wizardState = {};
        var baseJobWizardForms = [Ext.create('vegl.jobwizard.forms.JobSeriesForm', jobWizard.wizardState),
                                  Ext.create('vegl.jobwizard.forms.JobObjectForm', jobWizard.wizardState),
                                  Ext.create('vegl.jobwizard.forms.JobUploadForm', jobWizard.wizardState),
                                  Ext.create('vegl.jobwizard.forms.ScriptBuilderForm', jobWizard.wizardState),
                                  Ext.create('vegl.jobwizard.forms.JobSubmitForm', jobWizard.wizardState)];

        //Handler for whenever a form can't load data
        var loadingError = function() {
            Ext.Msg.alert('Internal Error', 'An error has occured whilst loading data from the server. Please try again in a few minutes.');
        };

        //Function for handling internal step changes
        var gotoStep = function(newStep, requireValidation) {
            var layout = jobWizard.getLayout();
            var setNewStep = function(layout, step) {
                //Deactivate our current form
                layout.activeItem.items.get(0).fireEvent('jobWizardDeactive');

                //Move to our next step (if it is a valid value)
                if (step >= 0) {
                    layout.setActiveItem(step);
                    layout.activeItem.items.get(0).fireEvent('jobWizardActive');
                }
            };

            if (requireValidation) {
                var currentCard = layout.activeItem;
                var currentForm = currentCard.items.get(0);

                currentForm.beginValidation(function(success) {
                    if (success) {
                        setNewStep(layout, newStep);
                    }
                });
            } else {
                setNewStep(layout, newStep);
            }
        };

        //Wrap our forms in cards to be displayed
        var items = [];
        for (var i = 0; i < baseJobWizardForms.length; i++) {
            frm = baseJobWizardForms[i];
            frm.on('jobWizardLoadException', loadingError); //subscribe to all loading errors

            buttons = [];

            if (i != 0) {
                buttons.push({
                    text: frm.getPreviousText(),
                    handler: Ext.bind(gotoStep, this, [i - 1, false])
                });
            }

            //Always show our "next step" but set it to < 0 if there is no more cards
            //This way the last card can still be validated and submitted. (it can
            //decide what to do from there)
            var nextStep = i + 1;
            if (nextStep >= baseJobWizardForms.length) {
                nextStep = -1;
            }
            buttons.push({
                text: frm.getNextText(),
                handler: Ext.bind(gotoStep, this, [nextStep, true])
            });

            items.push({
                title : 'Step ' + (i + 1) + ': ' + frm.getTitle(),
                defaults: { border: false },
                buttons : buttons,
                items : [frm]
            });
        }

        var startingJobIndex = 0;

        this.callParent([{
            layout: 'card',
            activeItem: startingJobIndex,
            defaults: { layout:'fit', frame: true, buttonAlign: 'right' },
            //bodyStyle: 'padding:20px 200px;',
            items: items,
            listeners: {
                //When we load init our first card with the 'active' event
                render : function() {
                    var activeItem = baseJobWizardForms[startingJobIndex];
                    activeItem.fireEvent('jobWizardActive');
                }
            }
        }]);
    }
});