<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#assign queryString = "(TEXT:\"${searchString}\") OR (@cm\\:name:*${searchString}*)" >
      <#-- assign queryString = "@\\{http\\://www.alfresco.org/model/content/1.0\\}name:*${searchString}*" -->
   </#if>
<#else>
   <#assign searchString = "">
   <#assign queryString = "">
</#if>

<#if searchString != "">
   <#if args.maxresults?exists>
      <#assign maxresults=args.maxresults?number>
   <#else>
      <#assign maxresults=10>
   </#if>
   <#assign resCount=0>
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
   <div>
      <span class="noItems">(No results found)</span>
   </div>
   <#else>
      <#assign totalResults = results?size>
      <#list results as child>
         <#assign resCount=resCount + 1>
         <#if child.isDocument>
            <#if child.name?ends_with(".doc")>
               <#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
               <#assign openURL = "#">
               <#assign hrefExtra = " onClick=\"window.external.openDocument('${webdavPath}')\"">
            <#else>
               <#assign openURL = "${url.context}${child.url}">
               <#assign hrefExtra = " target=\"_blank\"">
            </#if>
         <#else>
            <#assign openURL = "${url.serviceContext}/office/navigation?p=${args.p?url}&amp;n=${child.id}&amp;search=${searchString?url}&amp;maxresults=${maxresults}">
            <#assign hrefExtra = "">
         </#if>
   <div class="documentItem ${(resCount % 2 = 0)?string("odd", "even")}"">
      <span class="documentItemIcon">
         <a href="${openURL}" ${hrefExtra}><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      </span>
      <span class="documentItemDetails">
         <a href="${openURL}" ${hrefExtra} title="Open ${child.name}">${child.name}</a><br />
         <#if child.properties.description?exists>
            <#if (child.properties.description?length > 0)>
               ${child.properties.description}<br />
            </#if>
         </#if>
         <#if child.isDocument>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br/>
         </#if>
      </span>
   </div>
         <#if resCount = maxresults>
            <#break>
         </#if>
      </#list>
   </#if>
</#if>
<script type="text/javascript">
   OfficeSearch.itemsFound(${resCount}, ${totalResults});
</script>