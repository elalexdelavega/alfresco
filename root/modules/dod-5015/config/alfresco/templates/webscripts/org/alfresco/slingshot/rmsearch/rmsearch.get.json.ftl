<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"name": "${item.name}",
			"title": "${item.title!''}",
			"description": "${item.description!''}",
			"modifiedOn": "<@dateFormat item.modifiedOn />",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"createdOn": "<@dateFormat item.createdOn />",
			"createdByUser": "${item.createdByUser}",
			"createdBy": "${item.createdBy}",
			"size": ${item.size?c},
			"browseUrl": "${item.browseUrl}",
			"properties":
			{
			<#assign first=true>
			<#list item.properties?keys as k>
				<#if !first>,<#else><#assign first=false></#if>"${k}":
				<#assign prop = item.properties[k]>
				<#if prop?is_date>"<@dateFormat prop />"
				<#elseif prop?is_boolean>${prop?string("true", "false")}
				<#elseif prop?is_enumerable>[]<#-- TODO: iterate list values -->
				<#elseif prop?is_number>${prop?c}
				<#else>"${prop}"
				</#if>
			</#list>
			}
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>