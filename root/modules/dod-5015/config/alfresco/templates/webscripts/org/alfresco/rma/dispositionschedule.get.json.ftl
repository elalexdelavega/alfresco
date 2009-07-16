<#import "dispositionactiondefinition.lib.ftl" as actionDefLib/>

<@scheduleJSON schedule=schedule/>

<#macro scheduleJSON schedule>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data" :
   {
      "url" : "${schedule.url}",
      <#if schedule.authority??>"authority" : "${schedule.authority}",</#if>
      <#if schedule.instructions??>"instructions" : "${schedule.instructions}",</#if>
      "recordLevelDisposition" : ${schedule.recordLevelDisposition?string},
      "actionsUrl" : "${schedule.actionsUrl}",
      "actions" : 
      [
         <#list schedule.actions as action>
            <@actionDefLib.actionJSON action=action/>
            <#if action_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>
</#macro>