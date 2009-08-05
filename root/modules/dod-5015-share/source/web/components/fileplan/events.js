/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing
 */

/**
 * Events component.
 *
 * @namespace Alfresco
 * @class Alfresco.Events
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
         Event = YAHOO.util.Event,
         Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * Internal date formats
    */
   var DATE_LONG = "dddd, d mmmm yyyy",
      DATE_SHORT = "yyyy/mm/dd";

   /**
    * Events constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.Events} The new component instance
    * @constructor
    */
   Alfresco.Events = function Events_constructor(htmlId)
   {
      Alfresco.Events.superclass.constructor.call(this, "Alfresco.Events", htmlId, ["button", "container"]);

      return this;
   };

   YAHOO.extend(Alfresco.Events, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * The nodeRef to the object that owns the disposition schedule that is configured
          *
          * @property nodeRef
          * @type {string}
          */
         nodeRef: null
      },

      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function Events_onReady()
      {
         // Save a reference to important elements
         this.widgets.messageEl = Dom.get(this.id + "-message");
         this.widgets.completedEl = Dom.get(this.id + "-completed");
         this.widgets.completedEventsEl = Dom.get(this.id + "-completed-events");
         this.widgets.incompleteEl = Dom.get(this.id + "-incomplete");
         this.widgets.incompleteEventsEl = Dom.get(this.id + "-incomplete-events");

         // Get the templates and remove them from the DOM
         this.widgets.completedEventTemplate = Dom.get(this.id + "-completedEventTemplate");
         this.widgets.completedEventTemplate.parentNode.removeChild(this.widgets.completedEventTemplate);
         this.widgets.incompleteEventTemplate = Dom.get(this.id + "-incompleteEventTemplate");
         this.widgets.incompleteEventTemplate.parentNode.removeChild(this.widgets.incompleteEventTemplate);

         // Setup complete event dialog
         this._setupEventDialog();

         // Load events data
         this._refreshEvents();
      },

      /**
       * Refresh the events list
       *
       * @method _refreshEvents
       * @private
       */
      _refreshEvents: function Events__refreshEvents(response)
      {
         Alfresco.util.Ajax.jsonGet(
         {
            url: Alfresco.constants.PROXY_URI_RELATIVE + "api/node/" + this.options.nodeRef.replace(":/", "") + "/nextdispositionaction",
            successCallback:
            {
               fn: function(response)
               {
                  var nextDispositionAction = response.json.data;
                  if (nextDispositionAction && nextDispositionAction.events.length === 0 && nextDispositionAction.label)
                  {
                     this._displayMessage(this.msg("label.noEventsInDispositionSchedule", nextDispositionAction.label));
                  }
                  else
                  {
                     Dom.addClass(this.widgets.messageEl, "hidden");
                     this._onEventsLoaded(nextDispositionAction);
                  }
               },
               scope: this
            },
            failureCallback:
            {
               fn: function(response)
               {
                  if (response.serverResponse.status == 404)
                  {
                     this._displayMessage(this.msg("label.noDispositionSchedule"));
                  }
                  else{
                     Alfresco.util.PopupManager.displayPrompt(
                     {
                        text: this.msg("message.getEventFailure")
                     });
                  }
               },
               scope: this
            }
         });
      },

      /**
       * Called when the events information has been loaded
       *
       * @method _onEventsLoaded
       * @param msg THe message to display
       * @private
       */
      _displayMessage: function Events__onEventsLoaded(msg)
      {
         Dom.removeClass(this.widgets.messageEl, "hidden");
         this.widgets.messageEl.innerHTML = this.msg(msg);
         Dom.addClass(this.widgets.completedEl, "hidden");
         Dom.addClass(this.widgets.incompleteEl, "hidden");
      },

      /**
       * Called when the events information has been loaded
       *
       * @method _onEventsLoaded
       * @private
       */
      _onEventsLoaded: function Events__onEventsLoaded(nextDispositionAction)
      {
         if (this.widgets.feedbackMessage)
         {
            this.widgets.feedbackMessage.destroy();
            this.widgets.feedbackMessage = null;
         }
         this.widgets.completedEventsEl.innerHTML = "";
         this.widgets.incompleteEventsEl.innerHTML = "";
         var events = nextDispositionAction.events ? nextDispositionAction.events : [],
            completed = 0,
            incomplete = 0,
            ev, completedAt, eventEl, asOf;
         
         for (var i = 0, ii = events.length; i < ii; i++)
         {
            ev = events[i];
            if (ev.complete)
            {
               completedAt = Alfresco.util.fromISO8601(event.completedAt);
               eventEl = this._createEvent(event, [
                  { "name" : event.label },
                  { "automatic" : event.automatic ? this.msg("label.automatic") : this.msg("label.manual") },
                  { "completed-at" : completedAt ? Alfresco.util.formatDate(completedAt) : "" },
                  { "completed-by" : event.completedByFirstName + " " + event.completedByLastName }
               ],
                  "undo-button", this.onUndoEventButtonClick, this.widgets.completedEventTemplate);
               
               eventEl = this.widgets.completedEventsEl.appendChild(eventEl);
               completed++;
            }
            else
            {
               asOf = Alfresco.util.fromISO8601(nextDispositionAction.asOf);
               eventEl = this._createEvent(event, [
                  { "name" : event.label },
                  { "automatic" : event.automatic ? this.msg("label.automatic") : this.msg("label.manual") },
                  { "asof" : asOf ? Alfresco.util.formatDate(asOf) : "" }
               ],
                  "complete-button", this.onCompleteEventButtonClick, this.widgets.incompleteEventTemplate);
               
               eventEl = this.widgets.incompleteEventsEl.appendChild(eventEl);
               incomplete++;
            }
         }
         if (completed === 0)
         {
            Dom.addClass(this.widgets.completedEl, "hidden");
         }
         else
         {
            Dom.removeClass(this.widgets.completedEl, "hidden");
         }
         if (incomplete === 0)
         {
            Dom.addClass(this.widgets.incompleteEl, "hidden");
         }
         else
         {
            Dom.removeClass(this.widgets.incompleteEl, "hidden");
         }
      },

      /**
       * Create an event
       *
       * @method _createEvent
       * @param event The event info object
       * @private
       */
      _createEvent: function Events__createEvent(event, attributes, buttonClass, clickHandler, template)
      {
         // Clone template
         var eventEl = template.cloneNode(true),
            attribute;

         Alfresco.util.generateDomId(eventEl);

         // Display data
         for (var i = 0, ii = attributes.length; i < ii; i++)
         {
            attribute = attributes[i];
            for (var key in attribute)
            {
               if (attribute.hasOwnProperty(key))
               {
                  Selector.query("." + key + " .value", eventEl, true).innerHTML = attribute[key];
                  break;
               }
            }
         }

         // Create button
         var buttonEl = Dom.getElementsByClassName(buttonClass, "span", eventEl)[0],
            eventButton = Alfresco.util.createYUIButton(this, buttonClass, null, {}, buttonEl);
         
         eventButton.on("click", clickHandler,
         {
            event: event,
            button: eventButton
         }, this);

         return eventEl;
      },

      /**
       * Fired when the user clicks the complete button for an event.
       *
       * @method onCompleteEventButtonClick
       * @param e {object} a "click" event
       * @param obj.event {object} object with event info
       * @param obj.button {YAHOO.widget.Button} The button that was clicked
       */
      onCompleteEventButtonClick: function Events_onCompleteEventButtonClick(e, obj)
      {
         Dom.get(this.id + "-eventName").value = obj.event.name;
         Dom.get(this.id + "-completedAtTime").value = "12:00";
         Dom.get(this.id + "-completedAtDate").value = Alfresco.util.formatDate(new Date(), DATE_LONG);

         this.widgets.completeEventPanel.show();
      },

      /**
       * Fired when the user clicks the undo button for an event.
       *
       * @method onUndoEventButtonClick
       * @param e {object} a "click" event
       * @param obj.event {object} object with event info
       * @param obj.button {YAHOO.widget.Button} The button that was clicked
       */
      onUndoEventButtonClick: function Events_onUndoEventButtonClick(e, obj)
      {
         // Disable buttons to avoid double submits or cancel during post
         obj.button.set("disabled", true);

         // Undo event and refresh events afterwards
         this._doEventAction("undoEvent",
         {
            eventName: obj.event.name
         }, "message.revokingEvent", "message.revokeEventFailure");

      },

      /**
       * Fired when the user clicks the undo button for an event.
       *
       * @method _doEventAction
       * @param params the params to the action
       * @param action The name of action the action to be invoked
       * @param pendingMessage Message displayed durint action invocation and
       *        the event data is refreshed afterwards
       * @param failureMessage DIsplayed if the action failed
       * @private
       */
      _doEventAction: function Events__doEventAction(action, params, pendingMessage, failureMessage)
      {
         this.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg(pendingMessage),
            spanClass: "wait",
            displayTime: 0
         });

         Alfresco.util.Ajax.jsonPost(
         {
            url: Alfresco.constants.PROXY_URI_RELATIVE + "api/rma/actions/ExecutionQueue",
            dataObj:
            {
               nodeRef : this.options.nodeRef,
               name : action,
               params : params
            },
            successCallback:
            {
               fn: this._refreshEvents,
               scope: this
            },
            failureCallback:
            {
               fn: function()
               {
                  this.widgets.feedbackMessage.destroy();
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: this.msg(failureMessage)
                  });                  
               },
               scope: this
            }
         });
      },

      /**
       * Setup UI components and form for event dialog
       *
       * @method _setupEventDialog
       * @private
       */
      _setupEventDialog: function Events__setupEventDialog()
      {
         // TODO stop using a static id after RM, needed now so the text-align in #Share .yui-panel .bd .yui-u.first can be overriden
         // The panel is created from the HTML returned in the XHR request, not the container
         this.widgets.completeEventPanel = Alfresco.util.createYUIPanel("complete-event-panel")

         // Buttons
         this.widgets.completeEventOkButton = Alfresco.util.createYUIButton(this, "completeEvent-ok-button", this.onCompleteEventOkClick);
         this.widgets.completeEventCancelButton = Alfresco.util.createYUIButton(this, "completeEvent-cancel-button", this.onCompleteEventCancelClick);
         var completedAtPickerEl = Dom.get(this.id + "-completedAtPicker");
         Event.addListener(completedAtPickerEl, "click", this.onCompletedAtPickerButtonClick, completedAtPickerEl, this);

         // Form definition
         var form = new Alfresco.forms.Form(this.id + "-completeEvent-form");
         form.setSubmitElements(this.widgets.completeEventOkButton);
         form.setShowSubmitStateDynamically(true);

         // Setup date validation
         form.addValidation(this.id + "-completedAtTime", Alfresco.forms.validation.time, null, "keyup");         

         // Initialise the form
         form.init();

         // Register the ESC key to close the panel
         var escapeListener = new YAHOO.util.KeyListener(document,
         {
            keys: YAHOO.util.KeyListener.KEY.ESCAPE
         },
         {
            fn: function(id, keyEvent)
            {
               this.onCompleteEventCancelClick();
            },
            scope: this,
            correctScope: true
         });
         escapeListener.enable();         

         this.widgets.completeEventForm = form;
      },

      /**
       * Event handler that gets fired when a user clicks on the date selection
       * button in the compelte event form. Displays a mini YUI calendar.
       *
       * @method onCompletedAtPickerButtonClick
       * @param e {object} DomEvent
       */
      onCompletedAtPickerButtonClick: function Events_onCompletedAtPickerButtonClick(e, completedAtPickerEl)
      {
         var me = this;
         var oCalendarMenu = new YAHOO.widget.Overlay(this.id + "-calendarmenu");
         oCalendarMenu.setBody("&#32;");
         oCalendarMenu.body.id = this.id + "-calendarcontainer";

         // Render the Overlay instance into the Button's parent element
         oCalendarMenu.render(completedAtPickerEl.parentNode);

         // Align the Overlay to the Button instance
         oCalendarMenu.align();

         var oCalendar = new YAHOO.widget.Calendar("buttoncalendar", oCalendarMenu.body.id);
         oCalendar.render();

         oCalendar.changePageEvent.subscribe(function()
         {
            window.setTimeout(function()
            {
               oCalendarMenu.show();
            }, 0);
         });

         oCalendar.selectEvent.subscribe(function (type, args)
         {
            if (args)
            {
               var date = args[0][0],
                  selectedDate = new Date(date[0], (date[1]-1), date[2]),
                  elem = Dom.get(me.id + "-completedAtDate");
               
               elem.value = Alfresco.util.formatDate(selectedDate, DATE_LONG);
            }
            oCalendarMenu.hide();
         }, this);
      },

      /**
       * Event handler that gets fired when a user clicks
       * on the ok button in the complete event panel.
       *
       * @method onCompleteEventOkClick
       * @param e {object} DomEvent
       * @param obj {object} Object passed back from addListener method
       */
      onCompleteEventOkClick: function AddEvent_onCompleteEventOkClick(e, obj)
      {
         // Get values and Hide panel
         var completedAt = Alfresco.util.formatDate(Dom.get(this.id + "-completedAtDate").value, DATE_SHORT);
         completedAt = new Date(completedAt + " " + Dom.get(this.id + "-completedAtTime").value);
         var completedAtIso = Alfresco.util.toISO8601(completedAt);

         var eventName = Dom.get(this.id + "-eventName").value;
         this.widgets.completeEventPanel.hide();

         // Complete event and refresh events afterwards
         this._doEventAction("completeEvent",
         {
            eventName: eventName,
            eventCompletedBy: Alfresco.constants.USERNAME,
            dateParam:
            {
               iso8601: completedAtIso
            }
         }, "message.completingEvent", "message.completeEventFailure");

      },

      /**
       * Event handler that gets fired when a user clicks
       * on the cancel button in the complete event panel.
       *
       * @method onCompleteEventCancelClick
       * @param e {object} DomEvent
       * @param obj {object} Object passed back from addListener method
       */
      onCompleteEventCancelClick: function AddEvent_onCompleteEventCancelClick(e, obj)
      {
         // Hide panel
         this.widgets.completeEventPanel.hide();
      }
   });
})();