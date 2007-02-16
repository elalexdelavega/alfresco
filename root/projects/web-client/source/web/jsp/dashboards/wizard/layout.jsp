<%--
  Copyright (C) 2006 Alfresco, Inc.
 
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGrid columns="1" cellpadding="3" cellspacing="0" border="0" width="100%">
   <h:outputText value="#{msg.select_layout}" />
   
   <a:imagePickerRadioPanel id="layout-type" columns="4" spacing="4" value="#{WizardManager.bean.layout}"
                       onclick="javascript:itemSelected(this);" panelBorder="greyround" panelBgcolor="#F5F5F5">
      <a:listItems value="#{WizardManager.bean.layoutIcons}" />
   </a:imagePickerRadioPanel>
   
   <h:panelGroup>
      <f:verbatim>
      <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
      <table border='0'>
         <tr>
            <td valign='top'>
               </f:verbatim>
               <h:graphicImage id="info-icon" url="/images/icons/info_icon.gif" />
               <f:verbatim>
            </td>
            <td valign='top' align='left'>
               </f:verbatim>
               <a:dynamicDescription selected="#{WizardManager.bean.layout}">
                  <a:descriptions value="#{WizardManager.bean.layoutDescriptions}" />
               </a:dynamicDescription>
               <f:verbatim>
            </td>
         </tr>
      </table>
      <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
      </f:verbatim>
   </h:panelGroup>
   
</h:panelGrid>
