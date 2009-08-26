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
 * Records RecordsHtmlUpload component.
 *
 * Popups a YUI panel and displays a filelist and buttons to browse for files
 * and upload them. Files can be removed and uploads can be cancelled.
 * For single file uploads version input can be submitted.
 *
 * A multi file upload scenario could look like:
 *
 * var htmlUpload = Alfresco.getRecordsRecordsHtmlUploadInstance();
 * var multiUploadConfig =
 * {
 *    siteId: siteId, *    containerId: doclibContainerId,
 *    path: docLibUploadPath,
 *    filter: [],
 *    mode: htmlUpload.MODE_MULTI_UPLOAD,
 * }
 * this.htmlUpload.show(multiUploadConfig);
 *
 * @namespace Alfresco
 * @class Alfresco.RecordsRecordsHtmlUpload
 * @extends Alfresco.RecordsHtmlUpload
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      KeyListener = YAHOO.util.KeyListener;

   /**
    * RecordsRecordsHtmlUpload constructor.
    *
    * RecordsHtmlUpload is considered a singleton so constructor should be treated as private,
    * please use Alfresco.getRecordsHtmlUploadInstance() instead.
    *
    * @param htmlId {String} The HTML id of the parent element
    * @return {Alfresco.RecordsHtmlUpload} The new RecordsHtmlUpload instance
    * @constructor
    * @private
    */
   Alfresco.RecordsHtmlUpload = function(htmlId)
   {
      Alfresco.RecordsHtmlUpload.superclass.constructor.call(this, htmlId); 
      
      this.name = "Alfresco.RecordsHtmlUpload";
      Alfresco.util.ComponentManager.reregister(this);

      return this;
   };

   YAHOO.extend(Alfresco.RecordsHtmlUpload, Alfresco.HtmlUpload,
   {
      /**
       * Fired by YUI when parent element is available for scripting.
       * Initial History Manager event registration
       *
       * @method onReady
       */
      onReady: function RecordsHtmlUpload_onReady()
      {
         // Create and save a reference to the uploadButton so we can alter it later
         this.widgets.recordType = new YAHOO.widget.Button(
         {
            id: this.id + "-recordTypes-select-menu",
            name: "aspects",
            label: this.msg("recordType.default"),
            type: "menu",
            menu: this.id + "-recordTypes-select",
            lazyloadmenu: false,
            container: this.id + "-recordTypes-select-container"
         });

         /**
          * "selectedMenuItemChange" event handler for a Button that will set 
          * the Button's "label" attribute to the value of the "text" 
          * configuration property of the MenuItem that was clicked.
          */
         var fnRecordTypeItemChange = function RecordsFlashUpload_fnRecordTypeItemChange(event)
         {
            var oMenuItem = event.newValue;
            this.set("label", oMenuItem.cfg.getProperty("text"));
            this.createHiddenFields();
         };
         this.widgets.recordType.on("selectedMenuItemChange", fnRecordTypeItemChange);

         // Make sure we can evaluate selectedMenuItem even if no user interaction occurs
         var menu = this.widgets.recordType.getMenu();
         menu.initEvent.fire();
         menu.render();
         this.widgets.recordType.set("selectedMenuItem", menu.getItem(menu.srcElement.selectedIndex));

         Alfresco.RecordsHtmlUpload.superclass.onReady.call(this);
      },

      /**
       * Show can be called multiple times and will display the uploader dialog
       * in different ways depending on the config parameter.
       *
       * @method show
       * @param config {object} describes how the upload dialog should be displayed
       * The config object is in the form of:
       * {
       *    siteId: {string},        // site to upload file(s) to
       *    containerId: {string},   // container to upload file(s) to (i.e. a doclib id)
       *    uploadPath: {string},    // directory path inside the component to where the uploaded file(s) should be save
       *    updateNodeRef: {string}, // nodeRef to the document that should be updated
       *    updateFilename: {string},// The name of the file that should be updated, used to display the tip
       *    mode: {int},             // MODE_SINGLE_UPLOAD or MODE_SINGLE_UPDATE
       *    filter: {array},         // limits what kind of files the user can select in the OS file selector
       *    onFileUploadComplete: null, // Callback after upload
       *    overwrite: false         // If true and in mode MODE_XXX_UPLOAD it tells
       *                             // the backend to overwrite a versionable file with the existing name
       *                             // If false and in mode MODE_XXX_UPLOAD it tells
       *                             // the backend to append a number to the versionable filename to avoid
       *                             // an overwrite and a new version
       * }
       */
      show: function RecordsHtmlUpload_show(config)
      {
         Alfresco.RecordsHtmlUpload.superclass.show.call(this, config);
      },

      /**
       * Adjust the gui according to the config passed into the show method.
       *
       * @method _applyConfig
       * @private
       */
      _applyConfig: function RecordsHtmlUpload__applyConfig()
      {
         Alfresco.RecordsHtmlUpload.superclass._applyConfig.call(this);
      }
   });
})();