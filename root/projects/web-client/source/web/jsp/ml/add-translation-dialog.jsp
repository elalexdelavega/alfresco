<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page import="org.alfresco.web.bean.ml.AddTranslationDialog" %>
<%
boolean fileUploaded = false;

AddTranslationDialog dialog = (AddTranslationDialog)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "AddTranslationDialog");

if (dialog != null && dialog.getFileName() != null)
{
   fileUploaded = true;
}
%>

<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<r:page titleId="title_add_content">

<f:view>

   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

      <h:form acceptcharset="UTF-8" id="add-content-upload-start">

         <%-- Main outer table --%>
         <table cellspacing="0" cellpadding="2">

            <%-- Title bar --%>
            <tr>
               <td colspan="2"><%@ include file="../parts/titlebar.jsp"%>
               </td>
            </tr>

            <%-- Main area --%>
            <tr valign="top">
               <%-- Shelf --%>
               <td><%@ include file="../parts/shelf.jsp"%>
               </td>

               <%-- Work Area --%>
               <td width="100%">
               <table cellspacing="0" cellpadding="0" width="100%">
                  <%-- Breadcrumb --%>
                  <%@ include file="../parts/breadcrumb.jsp"%>

                  <%-- Status and Actions --%>
                  <tr>
                     <td
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)"
                        width="4"></td>
                     <td bgcolor="#dfe6ed"><%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32"><h:graphicImage id="dialog-logo"
                              url="/images/icons/add_tranlsation_large.gif" /></td>
                           <td>
                           <div class="mainTitle"><h:outputText
                              value="#{msg.add_translation}" /></div>
                           <div class="mainSubText"><h:outputText
                              value="#{msg.add_translation_description}" /></div>
                           </td>
                        </tr>
                     </table>

                     </td>
                     <td
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)"
                        width="4"></td>
                  </tr>

                  <%-- separator row with gradient shadow --%>
                  <tr>
                     <td><img
                        src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif"
                        width="4" height="9"></td>
                     <td
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                     <td><img
                        src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif"
                        width="4" height="9"></td>
                  </tr>

                  </h:form>

                  <%-- Details --%>
                  <tr valign=top>
                     <td
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)"
                        width="4"></td>
                     <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top"><a:errors
                              message="#{msg.error_add_translation_dialog}"
                              styleClass="errorMessage" /> <%
                              if (fileUploaded)
                              {
                                 PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");
                                 out.write("<img alt='' align='absmiddle' src='");
                                 out.write(request.getContextPath());
                                 out.write("/images/icons/info_icon.gif' />&nbsp;&nbsp;");
                                 out.write(dialog.getFileUploadSuccessMsg());
                                 PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");
                                 out.write("<div style='padding:2px;'></div>");
                              }
                              %> <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>

                           <table cellpadding="2" cellspacing="2" border="0" width="100%">
                              <% if (fileUploaded == false) { %>
                              <tr>
                                 <td colspan="3" class="wizardSectionHeading"><h:outputText
                                    value="#{msg.upload_content}" /></td>
                              </tr>
                              <tr>
                                 <td class="paddingRow"></td>
                              </tr>
                              <tr>
                                 <td class="mainSubText" colspan="3"><h:outputText
                                    id="text1" value="1. #{msg.locate_content}" /></td>
                              </tr>
                              <tr>
                                 <td class="paddingRow"></td>
                              </tr>

                              <r:uploadForm>
                                 <tr>
                                    <td colspan="3"><input style="margin-left:12px;"
                                       type="file" size="75" name="alfFileInput" /></td>
                                 </tr>
                                 <tr>
                                    <td class="paddingRow"></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText" colspan="3">2. <%=Application.getMessage(FacesContext.getCurrentInstance(), "click_upload")%>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="3"><input style="margin-left:12px;"
                                       type="submit"
                                       value="<%=Application.getMessage(FacesContext.getCurrentInstance(), "upload")%>" />
                                    </td>
                                 </tr>
                              </r:uploadForm>
                              <% } %>

                              <h:form acceptcharset="UTF-8" id="add-content-upload-end" onsubmit="return validate();">
                                 <% if (fileUploaded) { %>
                                 <tr>
                                    <td colspan="3">
                                    <table border="0" cellspacing="2" cellpadding="2"
                                       class="selectedItems">
                                       <tr>
                                          <td colspan="2" class="selectedItemsHeader"><h:outputText
                                             id="text2" value="#{msg.uploaded_content}" />
                                          </td>
                                       </tr>
                                       <tr>
                                          <td class="selectedItemsRow"><h:outputText id="text3"
                                             value="#{AddTranslationDialog.fileName}" /></td>
                                          <td><a:actionLink image="/images/icons/delete.gif"
                                             value="#{msg.remove}"
                                             action="#{AddTranslationDialog.removeUploadedFile}"
                                             showLink="false" id="link1" /></td>
                                       </tr>
                                    </table>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td class="paddingRow"></td>
                                 </tr>
                                 <tr>
                                    <td colspan="3" class="wizardSectionHeading">&nbsp;<h:outputText
                                       id="text4" value="#{msg.general_properties}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="paddingRow"></td>
                                 </tr>
                                 <tr>
                                    <td align="middle"><h:graphicImage
                                       value="/images/icons/required_field.gif"
                                       alt="#{msg.required_field}" /></td>
                                    <td><h:outputText id="text5" value="#{msg.name}:" /></td>
                                    <td width="85%">
                                       <h:inputText id="file-name" value="#{AddTranslationDialog.fileName}" maxlength="1024" size="35"
                                             onkeyup="checkButtonState();"
                                             onchange="checkButtonState();" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td></td>
                                    <td><h:outputText id="text6" value="#{msg.type}:" /></td>
                                    <td><h:selectOneMenu id="object-type" value="#{AddTranslationDialog.objectType}">
                                       <f:selectItems value="#{AddTranslationDialog.objectTypes}" />
                                    </h:selectOneMenu></td>
                                 </tr>
                                 <tr>
                                    <td></td>
                                    <td><h:outputText id="text7"
                                       value="#{msg.content_type}:" /></td>
                                    <td><r:mimeTypeSelector id="mime-type"
                                       value="#{AddTranslationDialog.mimeType}" /></td>
                                 </tr>
                                 <%-- language selection drop-down --%>
                                 <tr>
                                    <td><h:graphicImage
                                       value="/images/icons/required_field.gif"
                                       alt="#{msg.required_field}" /></td>
                                    <td><h:outputText id="text25" value="#{msg.language}:" />
                                    </td>
                                    <td>
                                       <h:selectOneMenu id="language" value="#{AddTranslationDialog.language}" immediate="false"  onchange="checkButtonState();" onkeydown="checkButtonState();" onkeyup="checkButtonState();">
                                          <f:selectItem  itemLabel="#{msg.select_language}" itemValue="null"/>
                                          <f:selectItems value="#{AddTranslationDialog.unusedLanguages}" />
                                       </h:selectOneMenu>
                                    </td>
                                 </tr>


                                 <% if (dialog.getOtherPropertiesChoiceVisible()) { %>
                                 <tr>
                                    <td class="paddingRow"></td>
                                 </tr>
                                 <tr>
                                    <td colspan="3" class="wizardSectionHeading">&nbsp;<h:outputText
                                       id="text8" value="#{msg.other_properties}" /></td>
                                 </tr>
                                 <tr>
                                    <td colspan="3">
                                    <table style="padding-top: 2px;">
                                       <tr>
                                          <td colspan="3"><h:outputText id="text9"
                                             value="#{msg.modify_props_help_text}" /></td>
                                       </tr>
                                       <tr>
                                          <td class="paddingRow"></td>
                                       </tr>
                                       <tr>
                                          <td>
                                             <h:selectBooleanCheckbox value="#{AddTranslationDialog.showOtherProperties}" />
                                          </td>
                                          <td width="100%">
                                             <h:outputText id="text10" value="#{msg.modify_props_when_page_closes}" />
                                          </td>
                                       </tr>

                                    </table>
                                    </td>
                                 </tr>
                                 <% } } %>

                           </table>
                           <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>

                           <td valign="top">
                           <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                           <table cellpadding="1" cellspacing="1" border="0">
                              <tr>
                                 <td align="center">
                                    <h:commandButton id="finish-button"
                                       styleClass="wizardButton"
                                       value="#{msg.ok}"
                                       action="#{AddTranslationDialog.finish}"
                                       disabled="#{AddTranslationDialog.finishButtonDisabled}"
                                    />
                                  </td>
                              </tr>
                              <tr>
                                 <td align="center">
                                    <h:commandButton id="cancel-button"
                                       styleClass="wizardButton"
                                       value="#{msg.cancel}"
                                       action="#{AddTranslationDialog.cancel}"
                                    />
                                 </td>
                              </tr>
                           </table>
                           <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                        </tr>
                     </table>
                     </td>
                     <td
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)"
                        width="4"></td>
                  </tr>

                  <%-- separator row with bottom panel graphics --%>
                  <tr>
                     <td><img
                        src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif"
                        width="4" height="4"></td>
                     <td width="100%" align="center"
                        style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                     <td><img
                        src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif"
                        width="4" height="4"></td>
                  </tr>

               </table>
               </td>
            </tr>
         </table>

   <script type="text/javascript">
      var finishButtonPressed = false;
      window.onload = pageLoaded;

      function pageLoaded()
      {
         document.getElementById("add-content-upload-end:finish-button").onclick = function() {finishButtonPressed = true; clear_add_2Dcontent_2Dupload_2Dend();}
         checkButtonState();
      }

      function checkButtonState()
      {
         if (document.getElementById("add-content-upload-end:file-name").value.length == 0
            || document.getElementById("add-content-upload-end:language").selectedIndex == 0)
         {
            document.getElementById("add-content-upload-end:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("add-content-upload-end:finish-button").disabled = false;
         }
      }

      function validate()
      {
         if (finishButtonPressed)
         {
            finishButtonPressed = false;
            return validateName(document.getElementById("add-content-upload-end:file-name"),
                                '<a:outputText id="text11" value="#{msg.validation_invalid_character}" />', true);
         }
         else
         {
            return true;
         }
      }

         </script>
      </h:form>
   </f:view>
</r:page>