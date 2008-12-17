/**
 * CommentList component.
 *
 * Displays a list of comments.
 *
 * @namespace Alfresco
 * @class Alfresco.CommentList
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
    * CommentList constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.CommentList} The new Comment instance
    * @constructor
    */
   Alfresco.CommentList = function(htmlId)
   {
      /* Mandatory properties */
      this.name = "Alfresco.CommentList";
      this.id = htmlId;

      /* Initialise prototype properties */
      this.editData =
      {
         editDiv : null,
         viewDiv : null,
         row : -1,
         data : null,
         widgets : {}
      };
      this.widgets = {};

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["event", "editor", "element", "dom", "datatable"], this.onComponentsLoaded, this);

      /* Decoupled event listeners */
      YAHOO.Bubbling.on("setCommentedNode", this.onSetCommentedNode, this);
      YAHOO.Bubbling.on("refreshComments", this.refreshComments, this);
      return this;
   };

   Alfresco.CommentList.prototype =
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
          * Current siteId.
          *
          * @property siteId
          * @type string
          */
         siteId: "",

         /**
          * ContainerId representing root container
          *
          * @property containerId
          * @type string
          * @default ""
          */
         containerId: "",

         /**
          * Node reference of the item to comment about
          */
         itemNodeRef: null,

         /**
          * Title of the item to comment about for activites service.
          */
         activityTitle: null,

         /**
          * Page for activities link.
          */
         activityPage: null,

         /**
          * Params for activities link.
          */
         activityPageParams: null,

         /**
          * Width to use for comment editor
          */
         width: 700,

         /**
          * Height to use for comment editor
          */
         height: 180,

         /**
          * Number of items per page
          *
          * @property pageSize
          * @type int
          */
         pageSize: 10
      },

      /**
       * Object container for storing YUI widget instances.
       *
       * @property widgets
       * @type object
       */
      widgets: {},

      /**
       * Object containing data about the currently edited
       * comment.
       */
      editData: null,

      /**
       * Comments data
       */
      commentsData: null,

      /**
       * Tells whether an action is currently ongoing.
       *
       * @property busy
       * @type boolean
       * @see _setBusy/_releaseBusy
       */
      busy: false,

      /**
       * Set multiple initialization options at once.
       *
       * @method setOptions
       * @param obj {object} Object literal specifying a set of options
       */
      setOptions: function CommentList_setOptions(obj)
      {
         this.options = YAHOO.lang.merge(this.options, obj);
         return this;
      },

      /**
       * Set messages for this component.
       *
       * @method setMessages
       * @param obj {object} Object literal specifying a set of messages
       * @return {Alfresco.DocumentList} returns 'this' for method chaining
       */
      setMessages: function CommentList_setMessages(obj)
      {
         Alfresco.util.addMessages(obj, this.name);
         return this;
      },

      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function CommentList_onComponentsLoaded()
      {
         YAHOO.util.Event.onContentReady(this.id, this.onReady, this, true);
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function CommentList_onReady()
      {   
         var me = this;
         // YUI Paginator definition
         var paginator = new YAHOO.widget.Paginator(
         {
            containers: [this.id + "-paginator"],
            rowsPerPage: this.options.pageSize,
            initialPage: 1,
            template: this._msg("pagination.template"),
            pageReportTemplate: this._msg("pagination.template.page-report"),
            previousPageLinkLabel : this._msg("pagination.previousPageLinkLabel"),
            nextPageLinkLabel     : this._msg("pagination.nextPageLinkLabel")
         });
         paginator.subscribe('changeRequest', this.onPaginatorChange, this, true);
         paginator.set('recordOffset', 0);
         paginator.set('totalRecords', 0);
         paginator.render();
         this.widgets.paginator = paginator;


         // Hook action events for the comments
         var fnActionHandlerDiv = function CommentList_fnActionHandlerDiv(layer, args)
         {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
            if (owner !== null)
            {
               var action = owner.className;
               //var target = args[1].target;
               if (typeof me[action] == "function")
               {
                  var commentElem = Dom.getAncestorByClassName(owner, 'comment');
                  var index = parseInt(commentElem.id.substring((me.id + '-comment-view-').length),10);
                  me[action].call(me, index);
                  args[1].stop = true;
               }
            }

            return true;
         };
         YAHOO.Bubbling.addDefaultAction("linkcomment-action", fnActionHandlerDiv);

         // initialize the mouse over listener
         Alfresco.util.rollover.registerHandlerFunctions(this.id, this.onCommentElementMouseEntered, this.onCommentElementMouseExited, this);
      },

      /**
       * Called by the paginator when a user has clicked on next or prev.
       * Dispatches a call to the server and reloads the comment list.
       *
       * @method onPaginatorChange
       * @param state {object} An object describing the required page changing
       */
      onPaginatorChange : function CommentList_onPaginatorChange(state) {
         this._loadCommentsList(state.recordOffset);
      },

      /**
       * Called by another component to set the node for which comments should be displayed
       */
      onSetCommentedNode: function CommentList_onSetCommentedNode(layer, args)
      {
         var obj = args[1];
         if ((obj !== null) && (obj.nodeRef !== null) && (obj.title !== null) && (obj.page !== null))
         {
            this.options.itemNodeRef = obj.nodeRef;
            this.options.activityTitle = obj.title;
            this.options.activityPage = obj.page;
            this.options.activityPageParams = obj.pageParams;
            this._loadCommentsList(0);
         }
      },

      /**
       * Forces the comments list to fresh by reloading the data from the server
       */
      refreshComments: function CommentList_onFilterChanged(layer, args)
      {
         if (this.options.itemNodeRef && this.options.activityTitle)
         {
            var p = this.widgets.paginator;
            var obj = args[1];

            // Find appropriate index
            var startIndex = 0;
            var tr = p.getTotalRecords();
            var ps = this.options.pageSize;
            if(obj.reason == "deleted")
            {
               // Make sure we dont use n invalid startIndex now that one is removed
               var newTotalPages = Math.floor((tr - 1) / ps) + (((tr - 1) % ps) > 0 ? 1 : 0);
               var currentPage = p.getCurrentPage();
               if(newTotalPages < currentPage)
               {
                  // the deletion was done of the last comment in the current page
                  currentPage = currentPage > 1 ? currentPage - 1 : 1;
               }
               var record = p.getPageRecords(currentPage);
               startIndex = record ? record[0] : 0;
            }
            if(obj.reason == "created")
            {
               startIndex = Math.floor(tr/ps) * ps;
            }
            this._loadCommentsList(startIndex);
         }
      },

      /**
       * Loads the comments for the provided nodeRef and refreshes the ui
       */
      _loadCommentsList: function CommentList__loadCommentsList(startIndex)
      {
         // construct the url to call
         var url = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + "api/node/{nodeRef}/comments",
         {
            nodeRef: this.options.itemNodeRef.replace(":/", "")
         });

         // execute ajax request
         Alfresco.util.Ajax.request(
         {
            url: url,
            dataObj: {
               startIndex: startIndex,
               pageSize: this.options.pageSize
            },
            successCallback:
            {
               fn: this.loadCommentsSuccess,
               scope: this
            },
            failureMessage: this._msg("message.loadComments.failure")
         });

      },

      /**
       * Load comments ajax request success handler.
       */
      loadCommentsSuccess: function CommentList_loadCommentsSuccess(response)
      {
         // make sure any edit data is cleared
         this._hideEditView();

         var comments = response.json.items;

         // Get the elements to update
         var bodyDiv = Dom.get(this.id + "-body");
         var titleDiv = Dom.get(this.id + "-title");
         var commentDiv = Dom.get(this.id + "-comments");

         // temporarily hide the container node
         bodyDiv.setAttribute("style", "display:none");

         // update the list name
         if (comments.length > 0)
         {
            titleDiv.innerHTML =  Alfresco.util.message("label.comments", this.name, {"0": comments.length, "1": response.json.total});
         }
         else
         {
            titleDiv.innerHTML = Alfresco.util.message("label.noComments", this.name);
         }

         // Update the list elements
         var html = '';
         for (var x=0; x < comments.length; x++)
         {
            html += this.renderComment(x, comments[x]);
         }
         commentDiv.innerHTML = html;
         bodyDiv.removeAttribute("style");

         // init mouse over
         Alfresco.util.rollover.registerListenersByClassName(this.id, 'comment', 'div');

         // keep a reference to the loaded data
         this.commentsData = comments;

         // inform the create comment component of whether the user can create a comment
         var eventData =
         {
            canCreateComment: response.json.nodePermissions.create
         };
         YAHOO.Bubbling.fire("setCanCreateComment", eventData);

         this._updatePaginator(response.json.startIndex, response.json.total);
      },

      /**
       * Called by loadCommentsSuccess when it has rendered the comments.
       * Since this componenent listens for the event "setCommentedNode" that can be displayed
       * before this component has created its own widgets and paginator it must wait until the paginator
       * has been created and then update it.
       *
       * @method updatePaginator
       * @param page {int} The page of comments in the paging list that is displayed
       * @param total {int} The totla number of comments in the paging
       */
      _updatePaginator: function CommentList__updatePaginator(page, total)
      {
         if(this.widgets && this.widgets.paginator)
         {
            this.widgets.paginator.set('totalRecords', total);
            this.widgets.paginator.set('recordOffset', page);

         }
         else
         {
            YAHOO.lang.later(100, this, this._updatePaginator, [page, total]);
         }
      },

      /**
       * Edit comment action links handler.
       */
      onEditComment: function CommentList_onEditComment(row)
      {   
         var data = this.commentsData[row];
         this._loadForm(row, data);
      },

      /**
       * Delete comment action links handler.
       */
      onDeleteComment: function CommentList_onDeleteComment(row)
      {
         var me = this;
         Alfresco.util.PopupManager.displayPrompt(
         {
            text: this._msg("message.confirm.delete"),
            buttons: [
            {
               text: this._msg("button.delete"),
               handler: function CommentList_onDeleteComment_delete()
               {
                  this.destroy();
                  me._onDeleteCommentConfirm.call(me, row);
               },
               isDefault: true
            },
            {
               text: this._msg("button.cancel"),
               handler: function CommentList_onDeleteComment_cancel()
               {
                  this.destroy();
               }
            }]
         });
      },

      /**
       * Delete comment implementation.
       */
      _onDeleteCommentConfirm: function CommentList__onDeleteCommentConfirm(row)
      {
         var data = this.commentsData[row];
         this._deleteComment(row, data);
      },

      // Action implementation

      /**
       * Implementation of the comment deletion action
       */
      _deleteComment: function CommentList__deleteComment(row, data)
      {
         // show busy message
         if (! this._setBusy(this._msg('message.wait')))
         {
            return;
         }

         // ajax request success handler
         var success = function CommentList__deleteComment_success(response, object)
         {
            // remove busy message
            this._releaseBusy();

            // reload the comments list
            YAHOO.Bubbling.fire("refreshComments", {reason: "deleted"});
         };

         // ajax request success handler
         var failure = function CommentList__deleteComment_failure(response, object)
         {
            // remove busy message
            this._releaseBusy();
         };

         // put together the request url to delete the comment
         var url = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + "api/comment/node/{nodeRef}/?site={site}&itemTitle={itemTitle}&page={page}&pageParams={pageParams}",
         {
            site: this.options.siteId,
            container: this.options.containerId,
            nodeRef: data.nodeRef.replace(":/", ""),
            itemTitle: this.options.activityTitle,
            page: this.options.activityPage,
            pageParams: YAHOO.lang.JSON.stringify(this.options.activityPageParams)
         });

         // execute ajax request
         Alfresco.util.Ajax.request(
         {
            url: url,
            method: "DELETE",
            responseContentType : "application/json",
            successMessage: this._msg("message.delete.success"),
            successCallback:
            {
               fn: success,
               scope: this
            },
            failureMessage: this._msg("message.delete.failure"),
            failureCallback:
            {
               fn: failure,
               scope: this
            }
         });
      },


      // Form management

      /**
       * Loads the comment edit form
       */
      _loadForm: function CommentList__loadForm(row, data)
      {
         // we always load the template through an ajax request
         var formId = this.id + "-edit-comment-" + row;

         // Load the UI template from the server
         Alfresco.util.Ajax.request(
         {
            url: Alfresco.constants.URL_SERVICECONTEXT + "modules/links/comments/edit-comment",
            dataObj:
            {
               htmlid: formId
            },
            successCallback:
            {
               fn: this.onFormLoaded,
               scope: this,
               obj: {formId: formId, row: row, data: data}
            },
            failureMessage: this._msg("message.loadeditform.failure"),
            execScripts: true
         });
      },

      /**
       * Event callback when comment form has been loaded
       *
       * @method onFormLoaded
       * @param response {object} Server response from load form XHR request
       */
      onFormLoaded: function CommentList_onFormLoaded(response, obj)
      {
         // get the data and formId of the loaded form
         var row = 0;
         row = obj.row;
         var data = obj.data;
         var formId = obj.formId;

         // make sure no other forms are displayed
         this._hideEditView();

         // find the right divs to insert the html into
         var viewDiv = Dom.get(this.id + "-comment-view-" + row);
         var editDiv = Dom.get(this.id + "-comment-edit-" + row);

         // insert the html
         editDiv.innerHTML = response.serverResponse.responseText;

         // insert current values into the form
         var actionUrl = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + "api/comment/node/{nodeRef}",
         {
            nodeRef: data.nodeRef.replace(':/', '')
         });
         Dom.get(formId + "-form").setAttribute("action", actionUrl);
         Dom.get(formId + "-site").setAttribute("value", this.options.siteId);
         Dom.get(formId + "-container").setAttribute("value", this.options.containerId);
         Dom.get(formId + "-itemTitle").setAttribute("value", this.options.activityTitle);
         Dom.get(formId + "-page").setAttribute("value", this.options.activityPage);
         Dom.get(formId + "-pageParams").setAttribute("value", YAHOO.lang.JSON.stringify(this.options.activityPageParams));
         Dom.get(formId + "-content").value = Alfresco.util.stripUnsafeHTMLTags(data.content);

         // show the form and hide the view
         Dom.addClass(viewDiv, "hidden");
         Dom.removeClass(editDiv, "hidden");

         // store the edit data locally
         this.editData =
         {
            viewDiv: viewDiv,
            editDiv: editDiv,
            row: row,
            widgets : {},
            formId: formId
         };

         // and finally register the form handling
         this._registerEditCommentForm(row, data, formId);
      },

      /**
       * Registers the form with the html (that should be available in the page)
       * as well as the buttons that are part of the form.
       */
      _registerEditCommentForm: function CommentList__registerEditCommentForm(row, data, formId)
      {
         // register the okButton
         this.editData.widgets.okButton = new YAHOO.widget.Button(formId + "-submit", {type: "submit"});

         // register the cancel button
         this.editData.widgets.cancelButton = new YAHOO.widget.Button(formId + "-cancel", {type: "button"});
         this.editData.widgets.cancelButton.subscribe("click", this.onEditFormCancelButtonClick, this, true);

         // instantiate the simple editor we use for the form
         this.editData.widgets.editor = new YAHOO.widget.SimpleEditor(formId + '-content', {
            height: this.options.height + 'px',
            width: this.options.width + 'px',
            dompath: false, //Turns on the bar at the bottom
            animate: false, //Animates the opening, closing and moving of Editor windows
            toolbar:  Alfresco.util.editor.getTextOnlyToolbarConfig(this._msg)
         });
         this.editData.widgets.editor._render();

         // create the form that does the validation/submit
         var commentForm = new Alfresco.forms.Form(formId + "-form");
         commentForm.setShowSubmitStateDynamically(true, false);
         commentForm.setSubmitElements(this.editData.widgets.okButton);
         commentForm.setAjaxSubmitMethod(Alfresco.util.Ajax.PUT);
         commentForm.setAJAXSubmit(true,
         {
            successMessage: this._msg("message.savecomment.success"),
            successCallback:
            {
               fn: this.onEditFormSubmitSuccess,
               scope: this
            },
            failureMessage: this._msg("message.savecomment.failure"),
            failureCallback:
            {
               fn: this.onEditFormSubmitFailure,
               scope: this
            }
         });
         commentForm.setSubmitAsJSON(true);
         commentForm.doBeforeFormSubmit =
         {
            fn: function(form, obj)
            {
               this.editData.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage(
               {
                  text: Alfresco.util.message("message.creating", this.name),
                  spanClass: "wait",
                  displayTime: 0
               });

               this.editData.widgets.okButton.set("disabled", true);
               this.editData.widgets.cancelButton.set("disabled", true);
               this.editData.widgets.editor._disableEditor(true);

               //Put the HTML back into the text area
               this.editData.widgets.editor.saveHTML();
            },
            scope: this
         };
         commentForm.init();
      },

      /**
       * Edit form submit success handler
       */
      onEditFormSubmitSuccess: function CommentList_onCreateFormSubmitSuccess(response, object)
      {
         this.editData.widgets.feedbackMessage.destroy();

         // the response contains the new data for the comment. Render the comment html
         // and insert it into the view element
         this.commentsData[this.editData.row] = response.json.item;
         var html = this.renderCommentView(this.editData.row, response.json.item);
         this.editData.viewDiv.innerHTML = html;

         // hide the form and display an information message
         this._hideEditView();
      },

      /**
       * Edit form submit failure handler
       */
      onEditFormSubmitFailure: function CommentList_onEditFormSubmitFailure(response, args)
      {
         this.editData.widgets.feedbackMessage.destroy();
         this.editData.widgets.okButton.set("disabled", false);
         this.editData.widgets.cancelButton.set("disabled", false);
         this.editData.widgets.editor._disableEditor(false);
      },

      /**
       * Form cancel button click handler
       */
      onEditFormCancelButtonClick: function CommentList_onEditFormCancelButtonClick(type, args)
      {
          this._hideEditView();
      },

      /**
       * Renders a comment element.
       * Each comment element consists of an edit and a view div.
       */
      renderComment: function CommentList_renderComment(index, data)
      {
         // add a div for the comment edit form
         var html = '';
         html += '<div id="' + this.id + '-comment-edit-' + index + '" class="hidden"></div>';

         // output the view
         var rowClass = index % 2 === 0 ? "even" : "odd";
         html += '<div class="comment ' + rowClass + '" id="' + this.id + '-comment-view-' + index + '">';
         html += this.renderCommentView(index, data);
         html += '</div>';

         return html;
      },

      /**
       * Renders the content of the comment view div.
       */
      renderCommentView: function CommentList_renderCommentView(index, data)
      {
         var html = '';

         // actions
         html += '<div class="nodeEdit">';
         if (data.permissions.edit)
         {
            html += '<div class="onEditComment"><a href="#" class="linkcomment-action">' + this._msg("action.edit") + '</a></div>';
         }
         if (data.permissions["delete"])
         {
            html += '<div class="onDeleteComment"><a href="#" class="linkcomment-action">' + this._msg("action.delete") + '</a></div>';
         }
         html += '</div>';

         // avatar image
         html += '<div class="authorPicture">' + Alfresco.util.people.generateUserAvatarImg(data.author) + '</div>';

         // comment info and content
         html += '<div class="nodeContent"><div class="userLink">' + Alfresco.util.people.generateUserLink(data.author);
         html += ' ' + this._msg("comment.said") + ':';
         if (data.isUpdated)
         {
            html += '<span class="nodeStatus">(' + this._msg("comment.updated") + ')</span>';
         }
         html += '</div>';
         html += '<div class="content yuieditor">' + Alfresco.util.stripUnsafeHTMLTags(data.content) + '</div>';
         html += '</div>';

         // footer
         html += '<div class="commentFooter">';
         html += '<span class="nodeFooterBlock">';
         html += '<span class="nodeAttrLabel">' + this._msg("comment.postedOn") + ': ';
         html += Alfresco.util.formatDate(data.createdOn);
         html += '</span></span></div>';

         return html;
      },


      // mouse over

      /** Called when the mouse enters into a list item. */
      onCommentElementMouseEntered: function CommentList_onCommentElementMouseEntered(layer, args)
      {
         // find out whether the user has actions, otherwise we won't show an overlay
         var id = args[1].target.id;
         var index = id.substring((this.id + '-comment-view-').length);
         var permissions = this.commentsData[index].permissions;
         if (! (permissions.edit || permissions["delete"]))
         {
            return;
         }

         var elem = args[1].target;
         YAHOO.util.Dom.addClass(elem, 'overNode');
      },

      /** Called whenever the mouse exits a list item. */
      onCommentElementMouseExited: function CommentList_onCommentElementMouseExited(layer, args)
      {
         var elem = args[1].target;
         YAHOO.util.Dom.removeClass(elem, 'overNode');
      },


      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Makes sure that all forms get removed and if available the hidden content
       * elements displayed again.
       */
      _hideEditView: function CommentList__hideEditView()
      {
         if (this.editData.editDiv !== null)
         {
            // hide edit div and remove form
            Dom.addClass(this.editData.editDiv, "hidden");
            this.editData.editDiv.innerHTML = "";
            this.editData.editDiv = null;
         }
         if (this.editData.viewDiv !== null)
         {
            // display view div
            Dom.removeClass(this.editData.viewDiv, "hidden");
            this.editData.viewDiv = null;
         }
      },

      /**
       * Displays the provided busyMessage but only in case
       * the component isn't busy set.
       *
       * @return true if the busy state was set, false if the component is already busy
       */
      _setBusy: function CommentList__setBusy(busyMessage)
      {
         if (this.busy)
         {
            return false;
         }
         this.busy = true;
         this.widgets.busyMessage = Alfresco.util.PopupManager.displayMessage(
         {
            text: busyMessage,
            spanClass: "wait",
            displayTime: 0
         });
         return true;
      },

      /**
       * Removes the busy message and marks the component as non-busy
       */
      _releaseBusy: function CommentList__releaseBusy()
      {
         if (this.busy)
         {
            this.widgets.busyMessage.destroy();
            this.busy = false;
            return true;
         }
         else
         {
            return false;
         }
      },

      /**
       * Gets a custom message
       *
       * @method _msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @private
       */
      _msg: function CommentList__msg(messageId)
      {
         return Alfresco.util.message.call(this, messageId, "Alfresco.CommentList", Array.prototype.slice.call(arguments).slice(1));
      }

   };
})();