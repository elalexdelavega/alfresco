package org.alfresco.web.bean.dialog;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;

/**
 * Bean that manages the dialog framework
 * 
 * @author gavinc
 */
public class DialogManager
{
   protected DialogConfig currentDialogConfig;
   protected IDialogBean currentDialog;
   
   /**
    * Sets the current dialog
    * 
    * @param config The configuration for the dialog to set
    */
   public void setCurrentDialog(DialogConfig config)
   {
      this.currentDialogConfig = config;
      
      String beanName = this.currentDialogConfig.getManagedBean();
      this.currentDialog = (IDialogBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), beanName);
      
      if (this.currentDialog == null)
      {
         throw new AlfrescoRuntimeException("Failed to find managed bean '" + beanName + "'");
      }
      
      // initialise the managed bean
      this.currentDialog.init();
   }
   
   /**
    * Returns the config for the current dialog
    * 
    * @return The current dialog config
    */
   public DialogConfig getCurrentDialog()
   {
      return this.currentDialogConfig;
   }
   
   /**
    * Returns the current dialog bean being managed
    * 
    * @return The current managed bean
    */
   public IDialogBean getBean()
   {
      return this.currentDialog;
   }
   
   /**
    * Returns the icon to use for the current dialog
    * 
    * @return The icon
    */
   public String getIcon()
   {
      return this.currentDialogConfig.getIcon();
   }
   
   /**
    * Returns the resolved title to use for the dialog
    * 
    * @return The title
    */
   public String getTitle()
   {
      String title = this.currentDialogConfig.getTitleId();
      
      if (title != null)
      {
         title = Application.getMessage(FacesContext.getCurrentInstance(), title);
      }
      else
      {
         title = this.currentDialogConfig.getTitle();
      }
      
      return title;
   }
   
   /**
    * Returns the resolved description to use for the dialog
    * 
    * @return The description
    */
   public String getDescription()
   {
      String desc = this.currentDialogConfig.getDescriptionId();
      
      if (desc != null)
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), desc);
      }
      else
      {
         desc = this.currentDialogConfig.getDescription();
      }
      
      return desc;
   }
   
   /**
    * Returns the page the dialog will use
    * 
    * @return The page
    */
   public String getPage()
   {
      return this.currentDialogConfig.getPage();
   }
   
   /**
    * Returns the label to use for the cancel button
    * 
    * @return The cancel button label
    */
   public String getCancelButtonLabel()
   {
      return this.currentDialog.getCancelButtonLabel();
   }
   
   /**
    * Returns the label to use for the finish button
    * 
    * @return The finish button label
    */
   public String getFinishButtonLabel()
   {
      return this.currentDialog.getFinishButtonLabel();
   }
   
   /**
    * Determines whether the finish button on the dialog should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getFinishButtonDisabled()
   {
      return this.currentDialog.getFinishButtonDisabled();
   }
   
   /**
    * Method handler called when the finish button of the dialog is pressed
    * 
    * @return The outcome
    */
   public String finish()
   {
      return this.currentDialog.finish();
   }
   
   /**
    * Method handler called when the cancel button of the dialog is pressed
    * 
    * @return The outcome
    */
   public String cancel()
   {
      return this.currentDialog.cancel();
   }
}
