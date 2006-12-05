/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.WebResources;

/**
 * @author Kevin Roast
 */
public class SubmitDialog extends BaseDialogBean
{
   private String comment;
   private String label;
   private String[] workflowSelectedValue;
   
   private List<ItemWrapper> submitItems;
   private List<ItemWrapper> warningItems;
   private HashSet<FormWorkflowWrapper> workflows;
   private Map<String, FormWorkflowWrapper> formWorkflowMap;
   private List<UIListItem> workflowItems;
   
   protected AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   protected WorkflowService workflowService;
   protected AVMSyncService avmSyncService;
   
   /**
    * @param avmService       The AVM Service to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.comment = null;
      this.label = null;
      this.submitItems = null;
      this.warningItems = null;
      this.workflowItems = null;
      this.workflows = new HashSet<FormWorkflowWrapper>(4);
      
      // walk all the web forms attached the website, and lookup the workflow defaults for each
      NodeRef websiteRef = this.avmBrowseBean.getWebsite().getNodeRef();
      List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
            websiteRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      this.formWorkflowMap = new HashMap<String, FormWorkflowWrapper>(webFormRefs.size(), 1.0f);
      for (ChildAssociationRef ref : webFormRefs)
      {
         NodeRef webFormRef = ref.getChildRef();
         String form = (String)this.nodeService.getProperty(webFormRef, WCMAppModel.PROP_FORMNAME);
         List<ChildAssociationRef> wfRefs = this.nodeService.getChildAssocs(
               webFormRef, WCMAppModel.TYPE_WORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
         if (wfRefs.size() == 1)
         {
            NodeRef wfDefaultsRef = wfRefs.get(0).getChildRef();
            String wfName = (String)this.nodeService.getProperty(wfDefaultsRef, WCMAppModel.PROP_WORKFLOW_NAME);
            Map<QName, Serializable> params = (Map<QName, Serializable>)EditWebsiteWizard.deserializeWorkflowParams(
                  wfDefaultsRef);
            this.formWorkflowMap.put(form, new FormWorkflowWrapper(wfName, params));
         }
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get the defaults from the workflow configuration attached to the selected workflow
      Map<QName, Serializable> params = null;
      String workflowName = this.workflowSelectedValue[0];
      for (FormWorkflowWrapper wrapper : this.workflows)
      {
         if (wrapper.Name.equals(workflowName))
         {
            params = wrapper.Params;
         }
      }
      
      if (params != null)
      {
         // create container for our avm workflow package
         NodeRef workflowPackage = createWorkflowPackage();
         params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
         
         // start the workflow to get access to the start task
         WorkflowDefinition wfDef = workflowService.getDefinitionByName("jbpm$" + workflowName);
         WorkflowPath path = this.workflowService.startWorkflow(wfDef.id, params);
         if (path != null)
         {
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
               WorkflowTask startTask = tasks.get(0);
               
               if (startTask.state == WorkflowTaskState.IN_PROGRESS)
               {
                  // end the start task to trigger the first 'proper' task in the workflow
                  this.workflowService.endTask(startTask.id, null);
               }
            }
         }
      }
      else
      {
         // TODO: jump to dialog and allow user to finish wf properties config!
         throw new AlfrescoRuntimeException("Workflow has not been configured correctly, cannot submit items.");
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return (getWorkflowSelectedValue() == null || getSubmitItemsSize() == 0);
   }
   
   /**
    * @return Returns the workflow comment.
    */
   public String getComment()
   {
      return this.comment;
   }

   /**
    * @param comment    The workflow comment to set.
    */
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   /**
    * @return Returns the snapshot label.
    */
   public String getLabel()
   {
      return this.label;
   }

   /**
    * @param label      The snapshot label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
      
   /**
    * @return Returns the workflow Selected Value.
    */
   public String[] getWorkflowSelectedValue()
   {
      // TODO: select default!
      return this.workflowSelectedValue;
   }

   /**
    * @param workflowSelectedValue The workflow Selected Value to set.
    */
   public void setWorkflowSelectedValue(String[] workflowSelectedValue)
   {
      this.workflowSelectedValue = workflowSelectedValue;
   }

   /**
    * @return List of UIListItem object representing the available workflows for the website
    */
   public List<UIListItem> getWorkflowList()
   {
      if (this.workflowItems == null)
      {
         // ensure all workflows have been collected from any form generated assets
         calculateSubmitItems();
         
         // add the list of workflows for the website itself to the set
         NodeRef websiteRef = this.avmBrowseBean.getWebsite().getNodeRef();
         List<ChildAssociationRef> webWorkflowRefs = this.nodeService.getChildAssocs(
               websiteRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : webWorkflowRefs)
         {
            NodeRef wfDefaultsRef = ref.getChildRef();
            String wfName = (String)this.nodeService.getProperty(wfDefaultsRef, WCMAppModel.PROP_WORKFLOW_NAME);
            Map<QName, Serializable> params = (Map<QName, Serializable>)EditWebsiteWizard.deserializeWorkflowParams(
                  wfDefaultsRef);
            this.workflows.add(new FormWorkflowWrapper(wfName, params));
         }
         
         // build a UI item for each available workflow
         List<UIListItem> items = new ArrayList<UIListItem>(this.workflows.size());
         for (FormWorkflowWrapper wrapper : this.workflows)
         {
            WorkflowDefinition workflowDef = this.workflowService.getDefinitionByName("jbpm$" + wrapper.Name);
            UIListItem item = new UIListItem();
            item.setValue(workflowDef.getName());
            item.setLabel(workflowDef.getTitle());
            item.setDescription(workflowDef.getDescription());
            item.setImage(WebResources.IMAGE_WORKFLOW_32);
            items.add(item);
            // add first as default TODO: what is correct here?
            if (workflowSelectedValue == null)
            {
               workflowSelectedValue = new String[]{workflowDef.getName()};
            }
         }
         this.workflowItems = items;
      }
      
      return this.workflowItems;
   }
   
   public List<ItemWrapper> getSubmitItems()
   {
      if (this.submitItems == null)
      {
         // this method builds all submit and warning item data structures
         calculateSubmitItems();
      }
      return this.submitItems;
   }
   
   public int getSubmitItemsSize()
   {
      return getSubmitItems().size();
   }
   
   public List<ItemWrapper> getWarningItems()
   {
      if (this.warningItems == null)
      {
         // this method builds all submit and warning item data structures
         calculateSubmitItems();
      }
      return this.warningItems;
   }
   
   public int getWarningItemsSize()
   {
      return this.getWarningItems().size();
   }
   
   private void calculateSubmitItems()
   {
      // TODO: start txn here?
      List<AVMNodeDescriptor> selected;
      if (this.avmBrowseBean.getSubmitAll())
      {
         String userStore = this.avmBrowseBean.getSandbox() + ":/";
         String stagingStore = this.avmBrowseBean.getStagingStore() + ":/";
         List<AVMDifference> diffs = avmSyncService.compare(-1, userStore, -1, stagingStore);
         selected = new ArrayList<AVMNodeDescriptor>(diffs.size());
         for (AVMDifference diff : diffs)
         {
            AVMNodeDescriptor node = this.avmService.lookup(-1, diff.getSourcePath());
            selected.add(node);
         }
      }
      else
      {
         selected = this.avmBrowseBean.getSelectedSandboxItems();
      }
      if (selected != null)
      {
         this.submitItems = new ArrayList<ItemWrapper>(selected.size());
         this.warningItems = new ArrayList<ItemWrapper>(selected.size() >> 1);
         for (AVMNodeDescriptor node : selected)
         {
            if (hasAssociatedWorkflow(AVMNodeConverter.ToNodeRef(-1, node.getPath())) == false)
            {
               // TODO: lookup if this item was created via a FORM - then need to lookup the workflow defaults
               //       for that form (and associated artifacts!) and then save that in list of workflows
               NodeRef ref = AVMNodeConverter.ToNodeRef(-1, node.getPath());
               if (this.nodeService.hasAspect(ref, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
               {
                  // found an XML form instance data file
                  String formName = (String)this.nodeService.getProperty(ref, WCMAppModel.PROP_PARENT_FORM_NAME);
                  FormWorkflowWrapper wrapper = this.formWorkflowMap.get(formName);
                  if (wrapper.Params != null)
                  {
                     // found a workflow with params attached to the form
                     this.workflows.add(wrapper);
                  }
               }
               this.submitItems.add(new ItemWrapper(node));
            }
            else
            {
               this.warningItems.add(new ItemWrapper(node));
            }
         }
      }
      else
      {
         this.submitItems = Collections.<ItemWrapper>emptyList();
         this.warningItems = Collections.<ItemWrapper>emptyList();
      }
   }
   
   private boolean hasAssociatedWorkflow(NodeRef ref)
   {
      // TODO: does not appear to work for AVM - need a specific impl instead
      return (this.workflowService.getWorkflowsForContent(ref, true).size() != 0);
   }
   
   /**
    * Construct a workflow package as a layered directory over the users sandbox. The items for
    * submission are pushed into the layer and the package constructed around it.
    * 
    * @return Reference to the package
    */
   private NodeRef createWorkflowPackage()
   {
      // TODO: add to common util class for all AVM workflows
      String packagesRoot = "workflow-system:/packages";
      AVMNodeDescriptor packagesDesc = avmService.lookup(-1, packagesRoot);
      if (packagesDesc == null)
      {
         avmService.createAVMStore("workflow-system");
         avmService.createDirectory("workflow-system:/", "packages");
      }
      
      List<ItemWrapper> items = getSubmitItems();
      
      // create package paths (layered to user sandbox area as target)
      String sandboxPath = AVMConstants.buildAVMStoreRootPath(avmBrowseBean.getSandbox());
      String packageName = GUID.generate();
      String packagesPath = packagesRoot + "/" + packageName;
      avmService.createLayeredDirectory(sandboxPath, packagesRoot, packageName);
      
      // construct diffs for selected items
      List<AVMDifference> diffs = new ArrayList<AVMDifference>(this.submitItems.size());
      for (ItemWrapper wrapper : this.submitItems)
      {
         String srcPath = sandboxPath + wrapper.getPath();
         String destPath = packagesPath + wrapper.getPath();
         AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
         diffs.add(diff);
      }
      
      // write changes to layer so files are marked as modified
      avmSyncService.update(diffs, true, true, false, false, null, null);
      
      // convert package to workflow package
      AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath);
      NodeRef packageNodeRef = workflowService.createPackage(AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
      nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);
      
      return packageNodeRef;
   }
   
   
   /**
    * Simple structure class to wrap form workflow name and default parameter values
    */
   private static class FormWorkflowWrapper
   {
      public String Name;
      public Map<QName, Serializable> Params;
      
      FormWorkflowWrapper(String name, Map<QName, Serializable> params)
      {
         this.Name = name;
         this.Params = params;
      }

      @Override
      public int hashCode()
      {
         return this.Name.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof FormWorkflowWrapper)
         {
            return this.Name.equals( ((FormWorkflowWrapper)obj).Name );
         }
         else
         {
            return false;
         }
      }
   }
   
   /**
    * Wrapper class to provide UI RichList component getters for an AVM node descriptor 
    */
   public class ItemWrapper
   {
      private static final String rootPath = '/' + AVMConstants.DIR_WEBAPPS;
      private AVMNodeDescriptor descriptor;
      
      public ItemWrapper(AVMNodeDescriptor descriptor)
      {
         this.descriptor = descriptor;
      }
      
      public String getName()
      {
         return descriptor.getName();
      }
      
      public String getModifiedDate()
      {
         return ISO8601DateFormat.format(new Date(descriptor.getModDate()));
      }
      
      public String getDescription()
      {
         return (String)nodeService.getProperty(
               AVMNodeConverter.ToNodeRef(-1, descriptor.getPath()), ContentModel.PROP_DESCRIPTION);
      }
      
      public String getPath()
      {
         return descriptor.getPath().substring(descriptor.getPath().indexOf(rootPath) + rootPath.length());
      }
      
      public String getUrl()
      {
         return DownloadContentServlet.generateBrowserURL(
               AVMNodeConverter.ToNodeRef(-1, descriptor.getPath()), descriptor.getName());
      }
      
      public String getIcon()
      {
         return Utils.getFileTypeImage(descriptor.getName(), true);
      }
   }
}
