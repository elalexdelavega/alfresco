<#if form.mode == "view">
<div class="viewmode-field">
   <span class="viewmode-label">${field.label?html}:</span>
   <span class="viewmode-value">${field.value?html}</span>
</div>
<#else>
<label for="${args.htmlid}_${field.id}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">*</span></#if></label>
<input id="${args.htmlid}_${field.id}" type="text" name="${field.name}" 
       <#if field.dataType == "d:int" || field.dataType == "d:long" || field.dataType == "d:double" || field.dataType == "d:float" >
       value="${field.value?c}"
       <#else>
       value="${field.value}"
       </#if>
       <#if field.description?exists>title="${field.description}"</#if>
       <#if field.control.params.maxLength?exists>maxlength="${field.control.params.maxLength}"</#if>
       <#if field.control.params.width?exists>style="width: ${field.control.params.width};"</#if> 
       <#if field.control.params.size?exists>size="${field.control.params.size}"</#if> 
       <#if field.disabled>disabled="true"</#if> />
</#if>