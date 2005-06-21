package org.alfresco.web.app.portlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import net.sf.acegisecurity.Authentication;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationFilter;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.log4j.Logger;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.apache.myfaces.portlet.PortletUtil;

public class AlfrescoFacesPortlet extends MyFacesGenericPortlet
{
   public static final String INSTANCE_NAME = "AlfrescoClientInstance";
   public static final String WINDOW_NAME = "AlfrescoClientWindow";
   public static final String MANAGED_BEAN_PREFIX = "javax.portlet.p." + INSTANCE_NAME + 
                                                    "." + WINDOW_NAME + "?";
   
   private static final String ERROR_PAGE_PARAM = "error-page";
   private static final String ERROR_OCCURRED = "error-occurred";
   
   private static Logger logger = Logger.getLogger(AlfrescoFacesPortlet.class);

   /**
    * Called by the portlet container to allow the portlet to process an action request.
    */
   public void processAction(ActionRequest request, ActionResponse response) 
      throws PortletException, IOException 
   {      
      boolean isMultipart = PortletFileUpload.isMultipartContent(request);
      
      try
      {
         // NOTE: Due to filters not being called within portlets we can not make use
         //       of the MyFaces file upload support, therefore we are using a pure
         //       portlet request/action to handle file uploads until there is a 
         //       solution.
         
         if (isMultipart)
         {
            if (logger.isDebugEnabled())
               logger.debug("Handling multipart request...");
            
            PortletSession session = request.getPortletSession();
            
            // get the file from the request and put it in the session
            DiskFileItemFactory factory = new DiskFileItemFactory();
            PortletFileUpload upload = new PortletFileUpload(factory);
            List<FileItem> fileItems = upload.parseRequest(request);
            Iterator<FileItem> iter = fileItems.iterator();
            FileUploadBean bean = new FileUploadBean();
            while(iter.hasNext())
            {
               FileItem item = iter.next();
               String filename = item.getName();
               if(item.isFormField() == false)
               {
                  // workaround a bug in IE where the full path is returned
                  int idx = filename.lastIndexOf(File.separator);
                  if (idx != -1)
                  {
                     filename = filename.substring(idx + File.separator.length());
                  }
                  
                  File tempFile = File.createTempFile("alfresco", ".upload");
                  item.write(tempFile);
                  bean.setFile(tempFile);
                  bean.setFileName(filename);
                  bean.setFilePath(tempFile.getAbsolutePath());
                  session.setAttribute(FileUploadBean.FILE_UPLOAD_BEAN_NAME, bean, 
                                       PortletSession.PORTLET_SCOPE);
               }
            }
            
            // it doesn't matter what the value is we just need the VIEW_ID parameter
            // to tell the faces portlet bridge to treat the request as a JSF request,
            // this will send us back to the same page we came from, which is fine for
            // most scenarios.
            response.setRenderParameter(VIEW_ID, "a-jsf-page");
         }
         else
         {
            // do the normal JSF processing
            super.processAction(request, response);
         }
      }
      catch (Throwable e)
      {
         if (Application.getErrorPage(getPortletContext()) != null)
         {
            handleError(request, response, e);
         }
         else
         {
            logger.warn("No error page configured, re-throwing exception");
            
            if (e instanceof PortletException)
            {
               throw (PortletException)e;
            }
            else if (e instanceof IOException)
            {
               throw (IOException)e;
            }
            else
            {
               throw new PortletException(e);
            }
         }
      }
   }
   
   private void handleError(ActionRequest request, ActionResponse response, Throwable error)
      throws PortletException, IOException
   {
      // get the error bean from the session and set the error that occurred.
      PortletSession session = request.getPortletSession();
      ErrorBean errorBean = (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME, 
                             PortletSession.PORTLET_SCOPE);
      if (errorBean == null)
      {
         errorBean = new ErrorBean();
         session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean, PortletSession.PORTLET_SCOPE);
      }
      errorBean.setLastError(error);
      
      response.setRenderParameter(ERROR_OCCURRED, "true");
   }

   /**
    * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#facesRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
    */
   protected void facesRender(RenderRequest request, RenderResponse response) 
      throws PortletException, IOException
   {
      if (request.getParameter(ERROR_OCCURRED) != null)
      {
         String errorPage = Application.getErrorPage(getPortletContext());
         
         if (logger.isDebugEnabled())
            logger.debug("An error has occurred, redirecting to error page: " + errorPage);
         
         response.setContentType("text/html");
         PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(errorPage);
         dispatcher.include(request, response);
      }
      else
      {
         // if we have no User object in the session then a timeout must have occured
         // use the viewId to check that we are not already on the login page
         String viewId = request.getParameter(VIEW_ID);
         User user = (User)request.getPortletSession().getAttribute(AuthenticationFilter.AUTHENTICATION_USER);
         if (user == null && (viewId == null || viewId.equals(getLoginPage(getPortletContext())) == false))
         {
            if (logger.isDebugEnabled())
               logger.debug("No valid login, requesting login page. ViewId: " + viewId);
            
            // login page redirect
            response.setContentType("text/html");
            request.getPortletSession().setAttribute(PortletUtil.PORTLET_REQUEST_FLAG, "true");
            nonFacesRequest(request, response);
         }
         else
         {
            // TODO: Add error handling around here so errors in getter's get caught!
            super.facesRender(request, response);
         }
      }
   }
   
   private String getLoginPage(PortletContext context)
   {
      if (this.loginPage == null)
      {
         this.loginPage = Application.getLoginPage(context);
      }
      
      return this.loginPage;
   }
   
   
   private String loginPage = null;
}
