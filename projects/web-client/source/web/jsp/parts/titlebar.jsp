<%-- Title bar area --%>
<table cellspacing=0 cellpadding=2 width=100%>
   <tr>
      <%-- Top level toolbar and company logo area --%>
      <td width=100%>
         <table cellspacing=0 cellpadding=0 width=100%>
            <tr>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_begin.gif" width=28 height=30></td>
               <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/titlebar_bg.gif)">
                  <%-- Toolbar --%>
                  <a:modeList itemSpacing="3" iconColumnWidth="0" horizontal="true" value="1"
                        itemStyleClass="topToolbar" itemLinkStyleClass="topToolbarLink" selectedStyleClass="topToolbarHighlight" selectedLinkStyleClass="topToolbarLinkHighlight">
                     <a:listItem value="0" label="Company Space" />
                     <a:listItem value="1" label="My Home" />
                  </a:modeList>
               </td>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_end.gif" width=4 height=30></td>
            </tr>
         </table>
      </td>
      
      <%-- Help area --%>
      <td>
         <table cellspacing=2 cellpadding=0 width=100%>
            <tr>
               <td><a:actionLink value="#{msg.toggle_shelf}" image="/images/icons/shelf.gif" actionListener="#{NavigationBean.toggleShelf}" showLink="false" /></td>
               <td width=8>&nbsp;</td>
               <td><img src="<%=request.getContextPath()%>/images/icons/Help_icon.gif" width=15 height=15></td>
               <td><h:outputText value="#{msg.help}"/></td>
            </tr>
         </table>
      </td>
      
      <%-- Search area --%>
      <td>
         <table cellspacing=0 cellpadding=0 width=100%>
            <tr>
               <td><img src="<%=request.getContextPath()%>/images/parts/searchbar_begin.gif" width=4 height=30></td>
               <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/searchbar_bg.gif)">
                  <r:simpleSearch id="search" actionListener="#{BrowseBean.search}" />
               </td>
               <td><img src="<%=request.getContextPath()%>/images/parts/searchbar_end.gif" width=4 height=30></td>
            </tr>
         </table>
      </td>
   </tr>
</table>
