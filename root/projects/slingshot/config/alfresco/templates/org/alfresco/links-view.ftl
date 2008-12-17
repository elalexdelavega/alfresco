<#import "import/alfresco-template.ftl" as template />
<@template.header>
   <script type="text/javascript" src="${url.context}/templates/links/links-view.css"></script>
   <script type="text/javascript" src="${url.context}/templates/links/links-view.js"></script>

   <!-- General Links Assets -->
   <script type="text/javascript" src="${page.url.context}/components/links/linksdiscuss-common.js"></script>
   <script type="text/javascript" src="${page.url.context}/components/links/links-common.js"></script>
</@>

<@template.body>
   <div id="hd">
      <@region id="header" scope="global" protected=true />
      <@region id="title" scope="template" protected=true />
      <@region id="navigation" scope="template" protected=true />
   </div>
   <div id="bd">
       <@region id="linksview" scope="template" protected=true />
       <@region id="comments" scope="template" protected=true />
       <@region id="createcomment" scope="template" protected=true />
   </div>
</@>

<@template.footer>
   <div id="ft">
      <@region id="footer" scope="global" protected=true />
   </div>
</@>