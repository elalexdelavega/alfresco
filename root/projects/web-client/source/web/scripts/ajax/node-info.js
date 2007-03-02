//
// Supporting JavaScript for the NodeInfo component
// Gavin Cornwell 17-07-2006
// Kevin Roast 21-02-2007 (rewrite to use individual panel objects and convert to YUI)
//
// NOTE: This script requires common.js - which needs to be loaded
//       prior to this one on the containing HTML page.

/**
 * Node Info Manager constructor
 */
Alfresco.NodeInfoManager = function()
{
   //YAHOO.util.Event.addListener(window, "resize", this.resize);
}

/**
 * Definition of the Node Info Manager class.
 * Responsible for open/closing NodeInfoPanel dynamic summary panel objects.
 */
Alfresco.NodeInfoManager.prototype =
{
   panels: [],
   displayed: [],
   
   /**
    * Request toggle of the open/close state of a node info panel
    */
   toggle: function(nodeRef, launchElement)
   {
      if (this.displayed[nodeRef] == undefined || this.displayed[nodeRef] == null)
      {
         var panel = this.panels[nodeRef];
         if (panel == undefined || panel == null)
         {
            panel = new Alfresco.NodeInfoPanel(nodeRef, launchElement);
            this.panels[nodeRef] = panel;
         }
         this.displayed[nodeRef] = true;
         panel.showNodeInfo();
      }
      else
      {
         this.close(nodeRef);
      }
   },
   
   /**
    * Request a Close of the node info panel
    */
   close: function(nodeRef)
   {
      var panel = this.panels[nodeRef];
      if (panel != undefined && panel != null)
      {
         this.displayed[nodeRef] = null;
         panel.hideNodeInfo();
      }
   },
   
   /**
    * Return if a given node info panel is currently displayable
    */
   displayable: function(nodeRef)
   {
      return (this.displayed[nodeRef] != undefined && this.displayed[nodeRef] != null);
   }
}

/**
 * Construct the single Node Info Manager instance
 */
var AlfNodeInfoMgr = new Alfresco.NodeInfoManager();


/**
 * Constructor for the Node Info Panel object
 */
Alfresco.NodeInfoPanel = function(nodeRef, launchElement)
{
   this.nodeRef = nodeRef;
   this.launchElement = launchElement;
}

/**
 * Definition of the Node Info Panel object
 */
Alfresco.NodeInfoPanel.prototype = 
{
   nodeRef: null,
   launchElement: null,
   popupElement: null,
   visible: false,
   
   /**
    * Makes the AJAX request back to the server to get the node info.
    */
   showNodeInfo: function()
   {
      if (this.popupElement == null)
      {
         var elImg = Alfresco.Dom.getElementByTagName(this.launchElement, "img");
         if (elImg != null)
         {
            elImg.src = getContextPath() + "/images/icons/ajax_anim.gif";
         }
         
         YAHOO.util.Connect.asyncRequest(
            "POST",
            getContextPath() + '/ajax/invoke/NodeInfoBean.sendNodeInfo',
            { 
               success: this.loadNodeInfoHandler,
               failure: handleErrorYahoo,    // global error handler
               argument: [this.nodeRef, this]
            }, 
            "noderef=" + this.nodeRef);
      }
      else
      {
         this.displayNodeInfo();
      }
   },
   
   /**
    * Callback function for showNodeInfo() above
    */
   loadNodeInfoHandler: function(response)
   {
      var panel = response.argument[1];
      
      // create a 'div' to hold the summary table
      var div = document.createElement("div");
      
      // setup the div with the correct appearance
      div.innerHTML = response.responseText;
      div.setAttribute("class", "summaryPopupPanel");
      // NOTE: use className for IE
      div.setAttribute("className", "summaryPopupPanel");
      div.style.position = "absolute";
      div.style.zIndex = 99;
      div.style.display = "none";
      div.style.left = 0;
      div.style.top = 0;
      
      var body = document.getElementsByTagName("body")[0];
      body.appendChild(div);
      
      // keep track of the div element we created
      panel.popupElement = div;
      
      // display the div for the first time
      panel.displayNodeInfo();
   },
   
   /**
    * Display the summary info panel for the node
    */
   displayNodeInfo: function()
   {
      var elImg = Alfresco.Dom.getElementByTagName(this.launchElement, "img");
      if (elImg != null)
      {
         elImg.src = getContextPath() + "/images/icons/popup.gif";
      }
      
      if (AlfNodeInfoMgr.displayable(this.nodeRef) == true)
      {
         if (this.popupElement != null && this.visible == false)
         {
            // set opacity in browser independant way
            YAHOO.util.Dom.setStyle(this.popupElement, "opacity", 0.0);
            this.popupElement.style.display = "block";
            
            Alfresco.Dom.smartAlignElement(this.popupElement, this.launchElement, 700);
            
            var anim = new YAHOO.util.Anim(
               this.popupElement, { opacity: { to: 1.0 } }, 0.333, YAHOO.util.Easing.easeOut);
            anim.animate();
            
            // drag-drop object
            new YAHOO.util.DD(this.popupElement);
            
            this.visible = true;
         }
      }
   },
   
   /**
    * Hide the summary info panel for the node
    */
   hideNodeInfo: function()
   {
      if (this.popupElement != null && this.visible == true)
      {
         this.visible = false;
         
         YAHOO.util.Dom.setStyle(this.popupElement, "opacity", 0.0);
         this.popupElement.style.display = "none";
      }
   }
}
