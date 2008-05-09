// Ensure Alfresco.forms and validation objects exist
Alfresco.forms = Alfresco.forms || {};
Alfresco.forms.validation = Alfresco.forms.validation || {};

/**
 * Class to represent the forms runtime.
 * 
 * @namespace Alfresco.forms
 * @class Alfresco.forms.Form
 */
(function()
{
   /**
    * Constructor for a form.
    * 
    * @param {String} formId The HTML id of the form to be managed
    * @return {Alfresco.forms.Form} The new Form instance
    * @constructor
    */
   Alfresco.forms.Form = function(formId)
   {
      this.formId = formId;
      this.validateOnSubmit = true;
      this.showSubmitStateDynamically = false;
      this.ajaxSubmit = false;
      this.submitIds = [];
      this.validations = [];
      
      return this;
   };
   
   Alfresco.forms.Form.prototype =
   {
      /**
       * HTML id of the form being represented.
       * 
       * @property formId
       * @type string
       */
      formId: null,
      
      /**
       * List of ids of elements being used to submit the form.
       * 
       * @property submitIds
       * @type string[]
       */
      submitIds: null,
      
      /**
       * Flag to indicate whether the form will validate upon submission, true
       * by default.
       * 
       * @property validateOnSubmit
       * @type boolean
       */
      validateOnSubmit: null,
      
      /**
       * Flag to determine whether the submit elements dynamically update
       * their state depending on the current values in the form.
       * 
       * @property showSubmitStateDynamically
       * @type boolean
       */
      showSubmitStateDynamically: null,
      
      /**
       * Flag to determine whether the form will be submitted using an AJAX request.
       * 
       * @property ajaxSubmit
       * @type boolean
       */
      ajaxSubmit: null,
      
      /**
       * List of validations to execute when the form is submitted.
       * 
       * @property validations
       * @type object[]
       */
      validations: null,
      
      /**
       * Sets up the required event handlers and prepares the form for use.
       * NOTE: This must be called after all other setup methods.
       * 
       * @method init
       */
      init: function()
      {
         // TODO: determine what event handlers need to be setup depending on the
         //       current state of the form object. Check the form enctype attribute
         //       if it's set to application/json ajax submit is implied.
      
         var form = document.getElementById(this.formId);
         if (form != null)
         {
            // add the event to the form and make the scope of the handler this form.
            YAHOO.util.Event.addListener(form, "submit", this._submitInvoked, this, true);
            
            // setup the submit elements if the feature is enabled
            if (this.showSubmitStateDynamically)
            {
               // find the default submit elements if there are no submitIds set
               if (this.submitIds.length == 0)
               {
                  // use a selector to find any submit elements for the form
                  var nodes = YAHOO.util.Selector.query('#' + this.formId + ' > input[type="submit"]');
                  for (var x = 0; x < nodes.length; x++)
                  {
                     var elem = nodes[x];
                     this.submitIds.push(elem.id);
                  }
               }
               
               // make sure the submit elements start in the correct state
               this._updateSubmitElements();
            }
            
            if (Alfresco.logger.isDebugEnabled())
               Alfresco.logger.debug("Initialised form: ", this);
         }
         else
         {
            this._showInternalError("form with id of '" + this.formId + 
                  "' could not be located, ensure the form is created after the form element is available.");
         }
      },
      
      /**
       * Enables or disables validation when the form is submitted.
       * 
       * @method setValidateOnSubmit
       * @param validate {boolean} true to validate on submission, false
       *        to avoid validation
       */
      setValidateOnSubmit: function(validate)
      {
         this.validateOnSubmit = validate;
      },
      
      /**
       * Sets the list of ids of elements being used to submit the form.
       * By default the forms runtime will look for and use the first
       * input field of type submit found in the form being managed.
       * 
       * @method setSubmitIds
       * @param submitIds {string | string[]} Single id or array of string ids
       */
      setSubmitIds: function(submitIds)
      {
         if (typeof submitIds == "string")
         {
            this.submitIds[0] = submitIds;
         }
         else
         {
            this.submitIds = submitIds;
         }
      },
      
      /**
       * Sets the position where errors will be displayed.
       * 
       * @method setErrorPosition
       * @param position {string} Position of errors, can be one of:
       *        'alert', 'popup', 'afterField' or 'beforeForm'.
       */
      setErrorPosition: function(position)
      {
         alert("not implemented yet");
      },
      
      /**
       * Sets a field as being repeatable, this adds a 'plus' sign after the field 
       * thus allowing multiple values to be entered.
       * 
       * @method setRepeatable
       * @param fieldId {string} Id of the field the validation is for
       * @param containerId {string} Id of the element representing the 
       *        field 'prototype' i.e. the item that will get cloned.
       */
      setRepeatable: function(fieldId, containerId)
      {
         alert("not implemented yet");
      },
      
      /**
       * Sets whether the submit elements dynamically update
       * their state depending on the current values in the form.
       * 
       * @method setShowSubmitStateDynamically
       * @param show {boolean} true to have the elements update dynamically
       */
      setShowSubmitStateDynamically: function(show)
      {
         this.showSubmitStateDynamically = show;
      },
      
      /**
       * Enables or disables whether the form submits via an AJAX call.
       * 
       * @method enableAJAXSubmit
       * @param ajaxSubmit {boolean} true to submit using AJAX, false to submit
       *        using the browser's default behaviour
       * @param callbacks {object} Optional object representing callback handlers 
       *        to use, for example
       *        { 
       *           preSubmitCallback: yourHandler,
       *           successCallback: yourHandler,
       *           failedCallback: yourHandler,
       *        }
       */
      enableAJAXSubmit: function(ajaxSubmit, callbacks)
      {
         alert("not implemented yet");
      },
      
      /**
       * Adds validation for a specific field on the form.
       * 
       * @method addValidation
       * @param fieldId {string} Id of the field the validation is for
       * @param validationHandler {function} Function to call to handle the 
       *        actual validation
       * @param validationArgs {object} Optional object representing the 
       *        arguments to pass to the validation handler function
       * @param when {string} Name of the event the validation should fire on
       *        can be any event applicable for the field for example on a text
       *        field "blur" can be used to fire the validation handler as the 
       *        user leaves the field. If null, the validation is only called
       *        upon form submission. 
       */
      addValidation: function(fieldId, validationHandler, validationArgs, when)
      {
         var field = document.getElementById(fieldId);
         if (field == null)
         {
            this._showInternalError("element with id of '" + fieldId + "' could not be located.");
            return;
         }
         
         // create object representation of validation
         var validation = {
            fieldId: fieldId,
            args: validationArgs,
            handler: validationHandler
         };
         
         // add to list of validations
         this.validations.push(validation);
         
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Added submit validation for field: " + fieldId +
                                  ", using handler: " + 
                                  (validationHandler.name || YAHOO.lang.dump(validationHandler)) + 
                                  ", args: " + YAHOO.lang.dump(validationArgs));
      
         // if an event has been specified attach an event handler
         if (when != null && when.length > 0)
         {
            // add the event to the field, pass the validation as a paramter 
            // to the handler and make the scope of the handler this form.
            YAHOO.util.Event.addListener(field, when, this._validationEventFired, validation, this);
            
            if (Alfresco.logger.isDebugEnabled())
               Alfresco.logger.debug("Added field validation for field: " + fieldId +
                                     ", using handler: " + 
                                     (validationHandler.name || YAHOO.lang.dump(validationHandler)) + 
                                     ", args: " + YAHOO.lang.dump(validationArgs) +
                                     ", on event: " + when);
         }
      },
      
      /**
       * Adds an error to the form.
       * 
       * @method addError
       * @param msg {string} The error message to display
       * @param field {object} The element representing the field the error occurred on
       * @param showNow {boolean} Indicates whether the error should be shown immediately
       */
      addError: function(msg, field, showNow)
      {
         // TODO: Add the error next to the field if supplied otherwise
         //       at the top of the form
         
         if (showNow)
         {
            alert(msg);
         }
      },
      
      /**
       * Event handler called when a validation event is fired by any registered field.
       * 
       * @method _validationEventFired
       * @param event {object} The event
       * @param validation {object} Object representing the validation to execute, consists of 
       *        'fieldId', 'args' and 'handler' properties.
       * @private
       */
      _validationEventFired: function(event, validation)
      {
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Event has been fired for field: " + validation.fieldId);
         
         // call handler
         validation.handler(YAHOO.util.Event.getTarget(event), validation.args, this);
         
         // update submit elements state, if required
         if (this.showSubmitStateDynamically)
         {
            this._updateSubmitElements();
         }
      },
      
      /**
       * Event handler called when the form is submitted.
       * 
       * @method _submitInvoked
       * @param event {object} The event
       * @private
       */
      _submitInvoked: function(event)
      {
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Submit invoked on form: ", this);
         
         if (this.validateOnSubmit)
         {
            if (!this._runValidations(false))
            {
               // stop the event from continuing and sending the form.
               YAHOO.util.Event.stopEvent(event);
            }
         }
         else
         {
            if (Alfresco.logger.isDebugEnabled())
               Alfresco.logger.debug("Ignoring validations as submission validation is disabled");
         }
      },
      
      /**
       * Executes all registered validations and returns result.
       * 
       * @method _runValidations
       * @param silent {boolean} Determines whether the validation checks are run silently
       * @private
       */
      _runValidations: function(silent)
      {
         var valid = true;
         
         // iterate through the validations
         for (var x = 0; x < this.validations.length; x++)
         {
            var val = this.validations[x];
                  
            var field = document.getElementById(val.fieldId);
            if (field != null)
            {
               if (val.handler(field, val.args, this, silent) == false)
               {
                  if (Alfresco.logger.isDebugEnabled())
                     Alfresco.logger.debug("Validation failed, ignoring any remaining rules");
                  
                  valid = false;
                  break;
               }
            }
         }
         
         return valid;
      },
      
      /**
       * Updates the state of all submit elements.
       * 
       * @method _updateSubmitElements
       * @private
       */
      _updateSubmitElements: function()
      {
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Determining whether submit elements can be enabled...");
         
         // run all validations silently to see if submit elements can be enabled
         var valid = this._runValidations(true);
         
         // make sure all submit elements show correct state
         for (var x = 0; x < this.submitIds.length; x++)
         {
            var elem = document.getElementById(this.submitIds[x]);
            elem.disabled = !valid;
         }
      },
      
      /**
       * Displays an internal form error message.
       * 
       * @method _showInternalError
       * @param msg {string} The error message to display
       * @param field {object} The element representing the field the error occurred on
       * @private 
       */
      _showInternalError: function(msg, field)
      {
         this.addError("Internal Form Error: " + msg, field, true);
      }
   };
})();

/**
 * Mandatory validation handler, tests that the given field has a value.
 * 
 * @method mandatory
 * @param field {object} The element representing the field the validation is for
 * @param args {object} Not used
 * @param form {object} The forms runtime class instance the field is being managed by
 * @param silent {boolean} Determines whether the user should be informed upon failure
 * @static
 */
Alfresco.forms.validation.mandatory = function mandatory(field, args, form, silent)
{
   if (Alfresco.logger.isDebugEnabled())
      Alfresco.logger.debug("Validating mandatory state of field '" + field.id + "'");
   
   var valid = true; 
      
   if (field.type && field.type == "radio")
   {
      // TODO: Do we actually need to support this scenario?
      //       wouldn't a radio button normally have a default
      //       'checked' option?
      
      var formElem = document.getElementById(form.formId);
      var radios = formElem[field.name];
      var anyChecked = false;
      for (var x = 0; x < radios.length; x++)
      {
         if (radios[x].checked)
         {
            anyChecked = true;
            break;
         }
      }
      
      valid = anyChecked;
   }
   else
   {
      valid = field.value.length != 0;
   }
   
   if (!valid && !silent && form !== null)
   {
      form.addError(field.id + " is mandatory.", field, true);
   }
   
   return valid; 
};

/**
 * Length validation handler, tests that the given field's value has either
 * a minimum and/or maximum length.
 * 
 * @method length
 * @param field {object} The element representing the field the validation is for
 * @param args {object} Object representing the minimum and maximum length, for example
 *        {
 *           min: 3;
 *           max: 10;
 *        }
 * @param form {object} The forms runtime class instance the field is being managed by
 * @param silent {boolean} Determines whether the user should be informed upon failure
 * @static
 */
Alfresco.forms.validation.length = function length(field, args, form, silent)
{
   if (Alfresco.logger.isDebugEnabled())
      Alfresco.logger.debug("Validating length of field '" + field.id +
                            "' using args: " + YAHOO.lang.dump(args));
   
   var valid = true;
   
   // TODO: Use merge to determine arg values.
   
   var min = -1;
   var max = -1;
   
   if (args.min)
   {
      min = args.min;
   }
   
   if (args.max)
   {
      max = args.max;
   }
   
   var length = field.value.length;
   
   if (min != -1 && length < min)
   {
      valid = false;
   }
   
   if (max != -1 && length > max)
   {
      valid = false;
   }
   
   if (!valid && !silent && form !== null)
   {
      form.addError(field.id + " is not the correct length.", field, true);
   }
   
   return valid;
};

/**
 * Number validation handler, tests that the given field's value is a number.
 * 
 * @method number
 * @param field {object} The element representing the field the validation is for
 * @param args {object} Not used
 * @param form {object} The forms runtime class instance the field is being managed by
 * @param silent {boolean} Determines whether the user should be informed upon failure
 * @static
 */
Alfresco.forms.validation.number = function number(field, args, form, silent)
{
   if (Alfresco.logger.isDebugEnabled())
      Alfresco.logger.debug("Validating field '" + field.id + "' is a number");
   
   var valid = (isNaN(field.value) == false);
   
   if (!valid && !silent && form !== null)
   {
      form.addError(field.id + " is not a number.", field, true);
   }
   
   return valid;
};

/**
 * Number range validation handler, tests that the given field's value has either
 * a minimum and/or maximum value.
 * 
 * @method numberRange
 * @param field {object} The element representing the field the validation is for
 * @param args {object} Object representing the minimum and maximum value, for example
 *        {
 *           min: 18;
 *           max: 30;
 *        }
 * @param form {object} The forms runtime class instance the field is being managed by
 * @param silent {boolean} Determines whether the user should be informed upon failure
 * @static
 */
Alfresco.forms.validation.numberRange = function numberRange(field, args, form, silent)
{
   if (Alfresco.logger.isDebugEnabled())
      Alfresco.logger.debug("Validating number range of field '" + field.id +
                            "' using args: " + YAHOO.lang.dump(args));
   
   var valid = true;
   var value = field.value;
   
   if (value.length > 0)
   {
      if (isNaN(value))
      {
         valid = false;
         
         if (!silent && form !== null)
         {
            form.addError(field.id + " is not a number.", field, true);
         }
      }
      else
      {
         var min = -1;
         var max = -1;
         
         if (args.min)
         {
            min = args.min;
         }
         
         if (args.max)
         {
            max = args.max;
         }
         
         if (min != -1 && value < min)
         {
            valid = false;
         }
         
         if (max != -1 && length > max)
         {
            valid = false;
         }
         
         if (!valid && !silent && form !== null)
         {
            form.addError(field.id + " is not within the allowable range.", field, true);
         }
      }
   }
   
   return valid;
};

/**
 * Regular expression validation handler, tests that the given field's value matches
 * the supplied regular expression.
 * 
 * @method regexMatch
 * @param field {object} The element representing the field the validation is for
 * @param args {object} Object representing the expression, for example to validate
 *        a field represents an email address:
 *        {
 *           pattern: /(\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,6})/}
 *        }
 * @param form {object} The forms runtime class instance the field is being managed by
 * @param silent {boolean} Determines whether the user should be informed upon failure
 * @static
 */
Alfresco.forms.validation.regexMatch = function regexMatch(field, args, form, silent)
{
   if (Alfresco.logger.isDebugEnabled())
      Alfresco.logger.debug("Validating regular expression of field '" + field.id +
                            "' using args: " + YAHOO.lang.dump(args));
   
   var valid = true;
   
   if (field.value.length > 0)
   {
      var pattern = new RegExp(args.pattern);
      valid = pattern.test(field.value);
      
      if (!valid && !silent && form !== null)
      {
         form.addError(field.id + " is invalid.", field, true);
      }
   }
   
   return valid;
};