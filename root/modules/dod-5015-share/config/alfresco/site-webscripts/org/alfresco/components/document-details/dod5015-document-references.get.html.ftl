<script type="text/javascript">//<![CDATA[
   new Alfresco.RecordsDocumentReferences("${args.htmlid}").setOptions(
   {
      siteId: "${page.url.templateArgs.site!""}",
      containerId: "${template.properties.container!"documentLibrary"}",
      nodeRef : "${page.url.args.nodeRef}",
   }).setMessages(
      ${messages}
   );
//]]></script>

<div id="${args.htmlid}-body" class="document-references">

   <div class="heading">${msg("label.heading")}</div>
   <button id="manageRef" class="manageRef">${msg('label.manage-references')}</button>
   <div class="reflist">
      <#if (references?size > 0)>
      <ul>
<#list references as ref>
         <li><a href="${ref.id}" title="${ref.displayName}"><span>${ref.displayName}</span></a></li>
</#list>
      </ul>
      <#else>
      <p id="no-ref-messages">${msg('message.no-messages')}</p>
      </#if>
   </div>

</div>