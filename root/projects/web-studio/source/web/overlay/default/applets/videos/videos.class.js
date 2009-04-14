WebStudio.Applets.Videos = WebStudio.Applets.Abstract.extend({
});

WebStudio.Applets.Videos.prototype.getDependenciesConfig = function()
{
	return {
		"images" : {
			"title" : "applet dependencies",
			"loader" : {
				"CSS" : {
					"name" : "CSS",
					"path" : WebStudio.overlayPath + "/applets/videos/videos.class.css.jsp"
				}
			}
		}
	};
};

WebStudio.Applets.Videos.prototype.bindSliderControl = function(container) 
{
	if(!this.VideosView)
	{
		var controlTemplate = this.instantiateControlTemplate(this.getId(), 'AlfrescoContentViewTemplate');
		
		this.videosView = new WebStudio.ContentView('Control_' + this.getId());
		this.videosView.setTemplate(controlTemplate);
		this.videosView.setInjectObject(container);
		this.videosView.application = this.getApplication();
		
		// set up content
		var items = [
			{
				url : "/studio/images/common/filetypes/avi-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/avi-128.png",
				title : "AVI-128",
				description : "AVI-128 description",
				mimetype: "png",
				endpoint: "http"				
			}
			,
			{
				url : "/studio/images/common/filetypes/css-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/css-128.png",
				title : "CSS-128",
				description : "CSS-128 description",
				mimetype: "png",
				endpoint: "http"				
			}
			,
			{
				url : "/studio/images/common/filetypes/folder-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/folder-128.png",
				title : "FOLDER-128",
				description : "FOLDER-128 description",
				mimetype: "png",
				endpoint: "http"				
			}
			,
			{
				url : "/studio/images/common/filetypes/excel-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/excel-128.png",
				title : "EXCEL-128",
				description : "EXCEL-128 description",
				mimetype: "png",
				endpoint: "http"				
			}
			,
			{
				url : "/studio/images/common/filetypes/html-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/html-128.png",
				title : "HTML-128",
				description : "HTML-128 description",
				mimetype: "png",
				endpoint: "http"								
			}
			,
			{
				url : "/studio/images/common/filetypes/jpg-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/jpg-128.png",
				title : "JPG-128",
				description : "JPG-128 description",
				mimetype: "png",
				endpoint: "http"								
			}
			,
			{
				url : "/studio/images/common/filetypes/text-128.png",
				thumbnailUrl : "/studio/images/common/filetypes/text-128.png",
				title : "TEXT-128",
				description : "TEXT-128 description",
				mimetype: "png",
				endpoint: "http"				
			}
		];
		this.videosView.setItems(items);
		
		// set up the videos to be draggable
		this.videosView.draggable = true;
		this.videosView.draggableScope = "region";
		this.videosView.draggableType = "contentViewVideo";		
		
		// activate the control
		this.videosView.activate();
		
		var _this = this;

		// TODO		
	}
	
	return this.VideosView;
};

WebStudio.Applets.Videos.prototype.onShowApplet = function()
{
	if (this.VideosView)
	{
		this.VideosView.onResize();
	}

	// hide all designers
	this.getApplication().hideAllDesigners();
	   
	// show the page editor
	this.getApplication().showPageEditor();
};

WebStudio.Applets.Videos.prototype.onHideApplet = function()
{
	// hide the page editor
	this.getApplication().hidePageEditor();
};