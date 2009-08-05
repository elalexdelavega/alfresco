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
 * RecordsMetaData tool component.
 * 
 * @namespace Alfresco
 * @class Alfresco.RecordsMetaData
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       Element = YAHOO.util.Element;
   
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;
   
   /**
    * RecordsMetaData constructor.
    * 
    * @param {String} htmlId The HTML id �of the parent element
    * @return {Alfresco.RecordsMetaData} The new RecordsMetaData instance
    * @constructor
    */
   Alfresco.RecordsMetaData = function(htmlId)
   {
      this.name = "Alfresco.RecordsMetaData";
      Alfresco.RecordsMetaData.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);
      
      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["button", "container", "json", "history"], this.onComponentsLoaded, this);
      
      /* Define panel handlers */
      var parent = this;
      
      // NOTE: the panel registered first is considered the "default" view and is displayed first
      
      /* View Panel Handler */
      ViewPanelHandler = function ViewPanelHandler_constructor()
      {
         // Initialise prototype properties
         
         ViewPanelHandler.superclass.constructor.call(this, "view");
      };
      
      YAHOO.extend(ViewPanelHandler, Alfresco.ConsolePanelHandler,
      {
         /**
          * Object container for storing YUI button instances, indexed by property name.
          * 
          * @property propEditButtons
          * @type object
          */
         propEditButtons: null,
         
         /**
          * Object container for storing YUI button instances, indexed by property name.
          * 
          * @property propDeleteButtons
          * @type object
          */
         propDeleteButtons: null,
         
         /**
          * onLoad ConsolePanel event handler
          * 
          * @method onLoad
          */
         onLoad: function onLoad()
         {
            // widgets
            parent.widgets.newPropertyButton = Alfresco.util.createYUIButton(parent, "newproperty-button", this.onNewPropertyClick);
            
            // attach onclick events for list objects
            var onClickListObject = function(e, id)
            {
               // apply highlight to appropriate item
               var listEls = Dom.getChildren(Dom.get(parent.id + "-object-list"));
               for (var i=0; i<listEls.length; i++)
               {
                  if (listEls[i].id === id)
                  {
                     Dom.addClass(listEls[i].id, "theme-bg-color-3");
                  }
                  else
                  {
                     Dom.removeClass(listEls[i].id, "theme-bg-color-3");
                  }
               }
               
               // update message in right-hand panel and create panel
               var msg = Dom.get(id).innerHTML;
               Dom.get(parent.id + "-view-metadata-item").innerHTML = msg;
               Dom.get(parent.id + "-create-metadata-item").innerHTML = msg;
               
               // selected object handling
               var itemType = id.substring(id.lastIndexOf("-") + 1);
               
               // update the currently selected item context e.g. "record"
               parent.currentType = itemType;
               
               // clear the list of meta-data items
               var elPropList = Dom.get(parent.id + "-property-list");
               elPropList.innerHTML = "";
               
               // reset widget references
               this.propDeleteButtons = {};
               this.propEditButtons = {};
               
               // perform ajax call to get the custom props for the object type
               Alfresco.util.Ajax.request(
               {
                  method: Alfresco.util.Ajax.GET,
                  url: Alfresco.constants.PROXY_URI + "api/rma/admin/custompropertydefinitions?element=" + itemType,
                  successCallback:
                  {
                     fn: this.onCustomPropertiesLoaded,
                     scope: this
                  },
                  failureMessage: parent._msg("message.getpropertiesfail")
               });
            };
            var listEls = Dom.getChildren(Dom.get(parent.id + "-object-list"));
            for (var i=0; i<listEls.length; i++)
            {
               var el = new Element(listEls[i]);
               el.addListener("click", onClickListObject, listEls[i].id, this);
            }
            
            // set initially selected object - fake the event call so handler code is invoked
            onClickListObject.call(this, null, parent.id + "-recordSeries");
         },
         
         /**
          * Custom properties for a type - ajax handler callback
          *
          * @method onCustomPropertiesLoaded
          * @param res {object} Response
          */
         onCustomPropertiesLoaded: function onCustomPropertiesLoaded(res)
         {
            var json = Alfresco.util.parseJSON(res.serverResponse.responseText);
            
            // sort data before display
            var customProperties = [];
            var props = json.data.customProperties;
            for (var propName in props)
            {
               var prop = props[propName];
               
               // insert 'name' field into object
               prop.name = propName;
               
               // add to array for sorting
               customProperties.push(prop);
            }
            customProperties.sort(parent._sortByTitle);
            
            // update the current custom properties list context
            parent.currentProperties = customProperties;
            
            // build the table of values describing the properties and add the action buttons
            var elPropList = Dom.get(parent.id + "-property-list");
            
            if (customProperties.length !== 0)
            {
               for (var index in customProperties)
               {
                  var prop = customProperties[index];
                  
                  // dynamically generated button ids
                  var editBtnContainerId = parent.id + '-edit-' + index;
                  var deleteBtnContainerId = parent.id + '-delete-' + index;
                  
                  // create a div element for each item then construct the inner HTML directly
                  /*
                     <div class="property-item">
                        <div class="property-actions">
                           <span id="btn">Edit Button</span>
                           <span id="btn">Delete Button</span>
                        </div>
                        <div>
                           <p class="property-title">Project Name</p>
                           <p>Type: Text</p>
                           <p>Selection list: regions</p>
                        </div>
                     </div>
                  */
                  var div = document.createElement("div");
                  Dom.addClass(div, "theme-bg-color-2");
                  Dom.addClass(div, "property-item");
                  var html = '<div class="property-actions"><span id="' + editBtnContainerId + '"></span><span id="' + deleteBtnContainerId + '"></span>';
                  html += '</div><div><p class="property-title">' + $html(prop.title) + '</p>';
                  html += '<p>' + parent._msg('label.type') + ': ' + parent._dataTypeLabel(prop.dataType) + '</p>';
                  // TODO: display selection list constraint
                  //html += '<p>' + this._msg('label.selection-list') + ': ' + prop.xx + '</p>';
                  html += '</div></div>';
                  div.innerHTML = html;
                  
                  // insert into the DOM for display
                  elPropList.appendChild(div);
                  
                  // generate buttons (NOTE: must occur after DOM insertion)
                  var editBtn = new YAHOO.widget.Button(
                  {
                     type: "button",
                     label: parent._msg("button.edit"),
                     name: parent.id + '-editButton-' + index,
                     container: editBtnContainerId,
                     onclick:
                     {
                        fn: this.onClickEditProperty,
                        obj: index,
                        scope: this
                     }
                  });
                  this.propEditButtons[index] = editBtn;
                  var deleteBtn = new YAHOO.widget.Button(
                  {
                     type: "button",
                     label: parent._msg("button.delete"),
                     name: parent.id + '-deleteButton-' + index,
                     container: deleteBtnContainerId,
                     onclick:
                     {
                        fn: this.onClickDeleteProperty,
                        obj: index,
                        scope: this
                     }
                  });
                  this.propDeleteButtons[index] = deleteBtn;
               }
            }
            else
            {
               // no properties found, display message
               var div = document.createElement("div");
               Dom.addClass(div, "no-property-item");
               div.innerHTML = parent._msg("message.noproperties");
               
               // insert into the DOM for display
               elPropList.appendChild(div);
            }
            Alfresco.util.Anim.fadeIn(elPropList);
         },
         
         /**
          * Edit Property button click handler
          *
          * @method onClickEditProperty
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onClickEditProperty: function onClickEditProperty(e, obj)
         {
            // update the current property context
            parent.currentProperty = parent.currentProperties[obj];
         },
         
         /**
          * Delete Property button click handler
          *
          * @method onClickDeleteProperty
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onClickDeleteProperty: function onClickDeleteProperty(e, obj)
         {
            // update the current property context
            parent.currentProperty = parent.currentProperties[obj];
         },
         
         /**
          * onShow ConsolePanel event handler
          * 
          * @method onShow
          */
         onShow: function onShow()
         {
         },
         
         /**
          * onUpdate ConsolePanel event handler
          * 
          * @method onUpdate
          */
         onUpdate: function onUpdate()
         {
         },
         
         /**
          * New Property button click handler
          *
          * @method onNewPropertyClick
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onNewPropertyClick: function onNewPropertyClick(e, obj)
         {
            parent.showPanel("create");
         }
      });
      new ViewPanelHandler();
      
      /* Create Metadata Panel Handler */
      CreatePanelHandler = function CreatePanelHandler_constructor()
      {
         // Initialise prototype properties
         
         CreatePanelHandler.superclass.constructor.call(this, "create");
      };
      
      YAHOO.extend(CreatePanelHandler, Alfresco.ConsolePanelHandler,
      {
         /**
          * onLoad ConsolePanel event handler
          * 
          * @method onLoad
          */
         onLoad: function onLoad()
         {
            // Buttons
            parent.widgets.createpropertyButton = Alfresco.util.createYUIButton(parent, "createproperty-button", this.onClickCreateProperty);
            parent.widgets.cancelcreatepropertyButton = Alfresco.util.createYUIButton(parent, "cancelcreateproperty-button", this.onClickCancelCreateProperty);
            
            // Form definition
            var form = new Alfresco.forms.Form(parent.id + "-create-form");
            form.setSubmitElements(parent.widgets.createpropertyButton);
            form.setShowSubmitStateDynamically(true);
            
            // Form field validation
            form.addValidation(parent.id + "-create-label", Alfresco.forms.validation.mandatory, null, "keyup");
            
            // Initialise the form
            form.init();
            
            // additional events
            Event.on(parent.id + "-create-type", "change", this.onCreateTypeChanged, null, this);
         },
         
         /**
          * onBeforeShow ConsolePanel event handler
          * 
          * @method onBeforeShow
          */
         onBeforeShow: function onBeforeShow()
         {
            // clear form
            Dom.get(parent.id + "-create-label").value = "";
            Dom.get(parent.id + "-create-type").selectedIndex = 0
            Dom.get(parent.id + "-create-use-list").checked = false;
            Dom.get(parent.id + "-create-use-list").disabled = false;
            Dom.get(parent.id + "-create-mandatory").checked = false;
            Dom.get(parent.id + "-create-list").selectedIndex = 0;
         },
         
         /**
          * onShow ConsolePanel event handler
          * 
          * @method onShow
          */
         onShow: function onShow()
         {
            Dom.get(parent.id + "-create-label").focus();
         },
         
         /**
          * onUpdate ConsolePanel event handler
          * 
          * @method onUpdate
          */
         onUpdate: function onUpdate()
         {
         },
         
         /**
          * Create Property button click handler
          * 
          * @method onClickCreateProperty
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onClickCreateProperty: function onClickCreateProperty(e, obj)
         {
         },
         
         /**
          * Cancel Create Property button click handler
          * 
          * @method onClickCancelCreateProperty
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onClickCancelCreateProperty: function onClickCancelCreateProperty(e, obj)
         {
            parent.showPanel("view");
         },
         
         /**
          * Create Type down-drop changed event handler
          * 
          * @method onCreateTypeChanged
          * @param e {object} DomEvent
          * @param obj {object} Object passed back from addListener method
          */
         onCreateTypeChanged: function onCreateTypeChanged(e, obj)
         {
            var disableList = (Dom.get(parent.id + "-create-type").selectedIndex !== 0);
            Dom.get(parent.id + "-create-use-list").disabled = disableList;
            Dom.get(parent.id + "-create-list").disabled = disableList;
         }
      });
      new CreatePanelHandler();
   };
   
   YAHOO.extend(Alfresco.RecordsMetaData, Alfresco.ConsoleTool,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         
      },
      
      /**
       * Current selected item type. e.g. record
       * 
       * @property currentType
       * @type string
       */
      currentType: null,
      
      /**
       * Current selected type properties object.
       * 
       * @property currentProperties
       * @type object
       */
      currentProperties: null,
      
      /**
       * Current selected property object.
       * 
       * @property currentProperty
       * @type object
       */
      currentProperty: null,
      
      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function RecordsMetaData_onComponentsLoaded()
      {
         Event.onContentReady(this.id, this.onReady, this, true);
      },
      
      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function RecordsMetaData_onReady()
      {
         // Call super-class onReady() method
         Alfresco.RecordsMetaData.superclass.onReady.call(this);
      },
      
      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */
      
      /**
       * History manager state change event handler (override base class)
       *
       * @method onStateChanged
       * @param e {object} DomEvent
       * @param args {array} Event parameters (depends on event type)
       */
      onStateChanged: function RecordsMetaData_onStateChanged(e, args)
      {
         var state = this.decodeHistoryState(args[1].state);
         
         // test if panel has actually changed?
         if (state.panel)
         {
            this.showPanel(state.panel);
         }
         
         // TODO: add history state
      },
      
      /**
       * Encode state object into a packed string for use as url history value.
       * Override base class.
       * 
       * @method encodeHistoryState
       * @param obj {object} state object
       * @private
       */
      encodeHistoryState: function RecordsMetaData_encodeHistoryState(obj)
      {
         // wrap up current state values
         var stateObj = {};
         if (this.currentPanelId !== "")
         {
            stateObj.panel = this.currentPanelId;
         }
         
         // TODO: add history state
         
         // convert to encoded url history state - overwriting with any supplied values
         var state = "";
         if (obj.panel || stateObj.panel)
         {
            state += "panel=" + encodeURIComponent(obj.panel ? obj.panel : stateObj.panel);
         }
         
         return state;
      },
      
      /**
       * PRIVATE FUNCTIONS
       */
      
      /**
       * Helper to Array.sort() by the 'title' field of an object.
       *
       * @method _sortByTitle
       * @return {Number}
       * @private
       */
      _sortByTitle: function RecordsMetaData__sortByTitle(s1, s2)
      {
         var ss1 = s1.title.toLowerCase(), ss2 = s2.title.toLowerCase();
         return (ss1 > ss2) ? 1 : (ss1 < ss2) ? -1 : 0;
      },
      
      /**
       * Helper to convert a repository datatype string to a label
       *
       * @method _dataTypeLabel
       * @param datatype {string} Repository datatype e.g. "d:text"
       * @return {string} I18N label for the datatype
       * @private
       */
      _dataTypeLabel: function RecordsMetaData__dataTypeLabel(dataType)
      {
         // convert datatype to readable label
         var dataTypeMsgId;
         switch (dataType)
         {
            case "d:text":
               dataTypeMsgId = "label.datatype.text";
               break;
            case "d:boolean":
               dataTypeMsgId = "label.datatype.boolean";
               break;
            case "d:date":
               dataTypeMsgId = "label.datatype.date";
               break;
            default:
               dataTypeMsgId = "label.datatype.unknown";
         }
         return this._msg(dataTypeMsgId);
      },
      
      /**
       * Gets a custom message
       *
       * @method _msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @private
       */
      _msg: function RecordsMetaData__msg(messageId)
      {
         return Alfresco.util.message.call(this, messageId, "Alfresco.RecordsMetaData", Array.prototype.slice.call(arguments).slice(1));
      }
   });
})();