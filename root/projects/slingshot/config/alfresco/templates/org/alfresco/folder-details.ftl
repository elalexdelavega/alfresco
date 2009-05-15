<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <@link rel="stylesheet" type="text/css" href="${url.context}/components/blog/postlist.css" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/components/blog/postview.css" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/templates/folder-details/folder-details.css" />
   <@script type="text/javascript" src="${page.url.context}/components/blog/blogdiscussions-common.js"></@script>
   <@script type="text/javascript" src="${page.url.context}/components/blog/blog-common.js"></@script>
   <@script type="text/javascript" src="${url.context}/modules/documentlibrary/doclib-actions.js"></@script>
   <@script type="text/javascript" src="${page.url.context}/templates/folder-details/folder-details.js"></@script>   
   <@templateHtmlEditorAssets />
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global" protected=true />
      <@region id="title" scope="template" protected=true />
      <@region id="navigation" scope="template" protected=true />
   </div>
   <div id="bd">
      <@region id="path" scope="template" protected=true />
      
      <div class="yui-g">
         <div class="yui-g first">
            <@region id="web-preview" scope="template" protected=true />
            <div class="folder-details-comments">
               <@region id="comments" scope="template" protected=true />
               <@region id="createcomment" scope="template" protected=true />
            </div>
         </div>
         <div class="yui-g"> 
            <div class="yui-u first">
               <@region id="folder-metadata-header" scope="template" protected=true />
               <@region id="folder-metadata" scope="template" protected=true />
               <@region id="folder-info" scope="template" protected=true />
            </div>
            <div class="yui-u">
               <@region id="folder-actions" scope="template" protected=true />
               <@region id="folder-links" scope="template" protected=true />
            </div>
         </div>
      </div>
   </div>
   
   <script type="text/javascript">//<![CDATA[
   new Alfresco.FolderDetails().setOptions(
   {
      nodeRef: "${url.args.nodeRef}",
      siteId: "${page.url.templateArgs.site!""}"
   });
   //]]></script>

</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>