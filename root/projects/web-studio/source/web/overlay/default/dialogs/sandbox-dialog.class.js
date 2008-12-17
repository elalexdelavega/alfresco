if (typeof WebStudio == "undefined" || !WebStudio)
{
	WebStudio = {};
}

WebStudio.SandboxDialog = function(index) 
{
	this.defaultContainer = document.body;
	this.injectObject = document.body;

	this.ID = index;
	
	this.defaultTemplateSelector = 'div[id=AlfrescoWebStudioSandboxTemplatePanel]';
	
	this.defaultElementsConfig = {
		CreateWebSite: {
			selector: 'div[id=create-web-site]'
		},
		CreateWebSiteImg: {
			selector: 'img[id=create-web-site-img]'
		},
		CreateWebSiteBody: {
			selector: 'div[id=create-web-site-body]'
		},
		LoadWebSite: {
			selector: 'div[id=load-web-site]'
		},
		LoadWebSiteImg: {
			selector: 'img[id=load-web-site-img]'
		},
		LoadWebSiteBody: {
			selector: 'div[id=load-web-site-body]'
		},
		ToolCreateWebSiteName: {
			selector: 'input[id=AlfrescoWebStudioSandboxTemplate_WebSiteName]'
		},
		ToolCreateWebSiteBasedOn: {
			selector: 'td[id=AlfrescoWebStudioSandboxTemplate_BasedOn]'
		},
		ToolCreateWebSiteCreate: {
			selector: 'input[id=AlfrescoWebStudioSandboxTemplate_Create]'
		},
		ToolCreateWebSiteImage: {
			selector: 'img[id=AlfrescoWebStudioSandboxTemplate_Image]'
		},
		ToolLoadWebSiteSearchText: {
			selector: 'input[id=AlfrescoWebStudioSandboxTemplate_SearchText]'
		},
		ToolLoadWebSiteSearchButton: {
			selector: 'input[id=AlfrescoWebStudioSandboxTemplate_SearchButton]'
		},
		ToolLoadWebSiteSearchResults: {
			selector: 'div[id=load-web-site-search-results]'
		},
		ToolLoadWebSiteLoad: {
			selector: 'input[id=AlfrescoWebStudioSandboxTemplate_Load]'
		},
		ToolLoadWebSiteSelectedId: {
			selector: 'input[id=load-web-site-selected]'
		}
	};

	this.events = {};	
	this.nodes = {};
	this.droppables = [];
};

WebStudio.SandboxDialog.prototype = new WebStudio.AbstractTemplater('WebStudio.SandboxDialog');

WebStudio.SandboxDialog.prototype.activate = function() 
{	
	var _this = this;
	
	this.buildGeneralLayer();

	// set up the create web site event	
	this.CreateWebSite.el.addEvents({
		'mouseenter': function(){ 
			_this.CreateWebSite.el.setStyle('cursor', 'pointer'); 
		}
		,
		'click': function(){ 
			_this.CreateWebSiteImg.el.src = "/studio/overlay/default/images/arrows/arrow_open.gif"; 
			_this.CreateWebSiteBody.el.setStyle('display', 'block');
			_this.LoadWebSiteImg.el.src = "/studio/overlay/default/images/arrows/arrow_closed.gif";
			_this.LoadWebSiteBody.el.setStyle('display', 'none');
			
			// update the "based on" drop down list
			_this.updateCreateWebSiteBasedOn();	
			
			// update the selected object UI
			_this.updateCreateSelected();			
		}
	});	

	// set up the load web site event	
	this.LoadWebSite.el.addEvents({
		'mouseenter': function(){ 
			_this.LoadWebSite.el.setStyle('cursor', 'pointer'); 
		}
		,
		'click': function() {
		
			_this.CreateWebSiteImg.el.src = "/studio/overlay/default/images/arrows/arrow_closed.gif"; 
			_this.CreateWebSiteBody.el.setStyle('display', 'none');
			_this.LoadWebSiteImg.el.src = "/studio/overlay/default/images/arrows/arrow_open.gif";
			_this.LoadWebSiteBody.el.setStyle('display', 'block');
			
			// update the list of web projects
			_this.updateLoadWebSiteSearchResults(null);
		}
	});

	// set up web site create click handler	s	
	this.ToolCreateWebSiteCreate.el.addEvent("click", this.webSiteCreateHandler);
	
	// set up web site load click handlers
	this.ToolLoadWebSiteLoad.el.addEvent("click", this.webSiteLoadHandler);	
};

WebStudio.SandboxDialog.prototype.updateCreateWebSiteBasedOn = function()
{
	var _this = this;
		
	// call over to login web script
	var url = WebStudio.ws.studio("/api/prebuilt/list", { } );
	this.call = YAHOO.util.Connect.asyncRequest('GET', url, 
	{	
		success: function(r) {
				
			// fault response into json
			var json = Json.evaluate(r.responseText);
			
			var html = "<select id='sandbox-create-template-selector' maxlength='256' style='width:200px' class='sandbox-input'>";
			var selectedId = null;
			for(var id in json.results)
			{
				if(json.results.hasOwnProperty(id))
				{
					var url = json.results[id].archiveUrl;
					
					html += "<option value=\"" + id + "\">";
					html += json.results[id].title;
					html += "</option>";
					html += "\n";
					
					if(!_this.createSelectedId)
					{
						_this.createSelectedId = id;
					}
				}
			}
			html += "</select>";
			
			_this.ToolCreateWebSiteBasedOn.el.setHTML(html);
			
			// set up an event handler for when the SELECT "create" control changes value
			var select = $('sandbox-create-template-selector');
			if(select)
			{
				select.addEvent("change", function(e)
				{
					_this.createSelectedId = select.value;		
					_this.updateCreateSelected();
				});
			}			
		}	
		,
		failure: function(r) {

			// TODO
					
		}		
	});
};

WebStudio.SandboxDialog.prototype.updateCreateSelected = function()
{
	var _this = this;
	
	// Set the Create Site preview image to a fun "loading" image
	_this.ToolCreateWebSiteImage.el.src = "/studio/overlay/default/images/sandbox-dialog-website-preview-loading.gif";
	_this.ToolCreateWebSiteImage.el.setStyle("width", "auto");
	_this.ToolCreateWebSiteImage.el.setStyle("height", "auto");
	_this.ToolCreateWebSiteImage.el.setStyle("border-top", "0");
	_this.ToolCreateWebSiteImage.el.setStyle("border-left", "0");
	_this.ToolCreateWebSiteImage.el.setStyle("border-bottom", "0");
	_this.ToolCreateWebSiteImage.el.setStyle("border-right", "0");		
	
	var url = WebStudio.ws.studio("/api/prebuilt/list", { } );
	this.call = YAHOO.util.Connect.asyncRequest('GET', url, 
	{	
		success: function(r) 
		{		
			// fault response into json
			var json = Json.evaluate(r.responseText);
			
			for(var id in json.results)
			{
				if(json.results.hasOwnProperty(id))
				{
					if(id == _this.createSelectedId)
					{
						var previewImageUrl = json.results[id].previewImageUrl;
						if(previewImageUrl.substring(0,1) == "/")
						{
							previewImageUrl = "/studio" + previewImageUrl;
						}

						_this.ToolCreateWebSiteImage.el.src = "";
						//_this.ToolCreateWebSiteImage.el.setStyle("width", "180px");
						//_this.ToolCreateWebSiteImage.el.setStyle("height", "164px");
						_this.ToolCreateWebSiteImage.el.setStyle("width", "150px");
						_this.ToolCreateWebSiteImage.el.setStyle("height", "135px");
						
						// flip to image
						_this.ToolCreateWebSiteImage.el.src = previewImageUrl;
						_this.ToolCreateWebSiteImage.el.setStyle("border-top", "1px white solid");
						_this.ToolCreateWebSiteImage.el.setStyle("border-left", "1px white solid");
						_this.ToolCreateWebSiteImage.el.setStyle("border-bottom", "1px black solid");
						_this.ToolCreateWebSiteImage.el.setStyle("border-right", "1px black solid");						
					}
				}
			}
		}
		,
		failure: function(r) {
	
			// TODO
					
		}
	});
};

WebStudio.SandboxDialog.prototype.updateLoadWebSiteSearchResults = function(text)
{
	var _this = this;
	
	// populate the web projects
	var url = WebStudio.ws.repo("/api/wcm/webprojects" );
	var callback = {
	
		success: function(oResponse) {
			
			var d = Json.evaluate(oResponse.responseText);
			if(!d)
			{
				return false;
			}
			
			var html = "<table width='100%' id='loadWebProjectSearchResultsEl' style='background-color: white'>";
			html += "<tr><td></td><td nowrap><B>Web Site</B></td><td width='100%'><B>Title</B></td></tr>";

			for (var i = 0; i < d.data.length; i++) 
			{
				var id = d.data[i].webprojectref;
				var title = d.data[i].title;
				
				var className = "sandbox-row-even";
				if(i % 2 == 1)
				{
					className = "sandbox-row-odd";
				}

				html += "<tr class='" + className + "'>";
				html += "<td><input type=\"radio\" name=\"sandboxSelectionId\" onClick=\"javascript:WebStudio.app.sandboxDialog.ToolLoadWebSiteSelectedId.el.value = '" + id + "';\" /></td>";
				html += "<td>" + id + "</td>";
				html += "<td>" + title + "</td>";
				html += "</tr>";

			}
			
			html += "</table>";
			
			_this.ToolLoadWebSiteSearchResults.el.setHTML(html);
		}
		,
		failure: function(oResponse) {
		
			// TODO
		}
		,
		timeout: 7000
	};
	YAHOO.util.Connect.asyncRequest('GET', url, callback);
};

WebStudio.SandboxDialog.prototype.setWebSiteCreateHandler = function(f)
{
	this.webSiteCreateHandler = f;
};

WebStudio.SandboxDialog.prototype.setWebSiteLoadHandler = function(f)
{
	this.webSiteLoadHandler = f;
};

WebStudio.SandboxDialog.prototype.setActive = function() 
{
	//Set this window active, bring to front and apply Active styles
	this.generalLayer.setStyle('z-index', WebStudio.WindowsZIndex + this.zIndexUpper);
	WebStudio.WindowsZIndex++;
	WebStudio.WindowsActive = this;
	return this;
};


WebStudio.SandboxDialog.prototype.popup = function() 
{	
	this.block();
	this.show();
	this.centered();
	this.zIndexUpper = 2000;
	this.setActive();
};

WebStudio.SandboxDialog.prototype.popout = function()
{	
    this.hide();
    this.unblock();
};