<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#assign paging = doclist.paging>
<#assign user = doclist.user>
<#assign itemCount = doclist.itemCount>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${paging.totalRecords?c},
   "startIndex": ${paging.startIndex?c},
   "metadata":
   {
      <#if doclist.filePlan??>"filePlan": "${doclist.filePlan.nodeRef}",</#if>
      "parent":
      {
      <#if doclist.parent??>
         "nodeRef": "${doclist.parent.nodeRef}",
         "type": "${doclist.parent.type}",
      </#if>
         "permissions":
         {
            "userRole": "${user.role!""}",
            "userAccess":
            {
            <#list user.permissions?keys as perm>
               <#if user.permissions[perm]?is_boolean>
               "${perm?string}": ${user.permissions[perm]?string}<#if perm_has_next>,</#if>
               </#if>
            </#list>
            }
         }
      }
   },
   "items":
   [
   <#list doclist.items as item>
      <#assign d = item.asset>
      <#assign version = "1.0">
      <#if d.hasAspect("cm:versionable") && d.versionHistory?size != 0><#assign version = d.versionHistory[0].versionLabel></#if>
      <#if item.createdBy??>
         <#assign createdBy = ((item.createdBy.properties.firstName!"") + " " + (item.createdBy.properties.lastName!""))?trim>
         <#assign createdByUser = item.createdBy.properties.userName>
      <#else>
         <#assign createdBy="" createdByUser="">
      </#if>
      <#if item.modifiedBy??>
         <#assign modifiedBy = ((item.modifiedBy.properties.firstName!"") + " " + (item.modifiedBy.properties.lastName!""))?trim>
         <#assign modifiedByUser = item.modifiedBy.properties.userName>
      <#else>
         <#assign modifiedBy="" modifiedByUser="">
      </#if>
      <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
      {
         "index": ${item_index},
         "nodeRef": "${d.nodeRef}",
         "type": "${item.type}",
         "isFolder": ${d.isContainer?string},
         "mimetype": "${d.mimetype!""}",
         "fileName": "${d.name}",
         "displayName": "${d.name?replace(workingCopyLabel, "")}",
         "status": "<#list item.status?keys as s><#if item.status[s]?is_boolean && item.status[s] == true>${s}<#if s_has_next>,</#if></#if></#list>",
         "title": "${d.properties.title!""}",
         "description": "${d.properties.description!""}",
         "author": "${d.properties.author!""}",
         "createdOn": "<@dateFormat d.properties.created />",
         "createdBy": "${createdBy}",
         "createdByUser": "${createdByUser}",
         "modifiedOn": "<@dateFormat d.properties.modified />",
         "modifiedBy": "${modifiedBy}",
         "modifiedByUser": "${modifiedByUser}",
         "size": "${d.size?c}",
         "version": "${version}",
         "contentUrl": "api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}",
         "actionSet": "${item.actionSet}",
         "tags": <#noescape>[${tags}]</#noescape>,
         "location":
         {
            "site": "${item.location.site!""}",
            "container": "${item.location.container!""}",
            "path": "${item.location.path!""}",
            "file": "${item.location.file!""}"
         },
         "permissions":
         {
            "inherited": ${d.inheritsPermissions?string},
         <#if !item.suppressRoles>
            "roles":
            [
            <#list d.permissions as permission>
               "${permission?string}"<#if permission_has_next>,</#if>
            </#list>
            ],
         </#if>
            "userAccess":
            {
            <#list item.actionPermissions?keys as actionPerm>
               <#if item.actionPermissions[actionPerm]?is_boolean>
               "${actionPerm?string}": ${item.actionPermissions[actionPerm]?string}<#if actionPerm_has_next>,</#if>
               </#if>
            </#list>
            }
         },
         "custom": {},
         "dod5015": <#noescape>${item.dod5015}</#noescape>
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>
