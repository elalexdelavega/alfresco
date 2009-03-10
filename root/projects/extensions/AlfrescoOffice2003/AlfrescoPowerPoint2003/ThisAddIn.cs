using System;
using System.Drawing;
using System.Windows.Forms;
using Microsoft.VisualStudio.Tools.Applications.Runtime;
using PowerPoint = Microsoft.Office.Interop.PowerPoint;
using Office = Microsoft.Office.Core;

namespace AlfrescoPowerPoint2003
{
   public partial class ThisAddIn
   {
      private const string COMMANDBAR_NAME = "Alfresco";
      private const string COMMANDBAR_BUTTON_CAPTION = "Alfresco";
      private const string COMMANDBAR_BUTTON_DESCRIPTION = "Show/hide the Alfresco Add-In window";

      private AlfrescoPane m_AlfrescoPane;
      private string m_DefaultTemplate = "wcservice/office/";
      private AppWatcher m_AppWatcher;
      private Office.CommandBar m_CommandBar;
      private Office.CommandBarButton m_AlfrescoButton;

      private void ThisAddIn_Startup(object sender, System.EventArgs e)
      {
         m_DefaultTemplate = Properties.Settings.Default.DefaultTemplate;

         // Register event interest with the PowerPoint Application
         Application.WindowActivate += new Microsoft.Office.Interop.PowerPoint.EApplication_WindowActivateEventHandler(Application_WindowActivate);
         Application.PresentationClose += new Microsoft.Office.Interop.PowerPoint.EApplication_PresentationCloseEventHandler(Application_PresentationClose);

         // PowerPoint doesn't raise an event when it loses or gains app focus
         m_AppWatcher = new AppWatcher();
         m_AppWatcher.OnWindowHasFocus += new WindowHasFocus(AppWatcher_OnWindowHasFocus);
         m_AppWatcher.OnWindowLostFocus += new WindowLostFocus(AppWatcher_OnWindowLostFocus);
         m_AppWatcher.Start(Application.HWND);
         
         // Add the button to the Office toolbar
         AddToolbar();
      }

      void AppWatcher_OnWindowHasFocus(int hwnd)
      {
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.OnWindowActivate();
         }
      }

      void AppWatcher_OnWindowLostFocus(int hwnd)
      {
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.OnWindowDeactivate();
         }
      }

      private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
      {
         SaveToolbarPosition();
         CloseAlfrescoPane();
         m_AlfrescoPane = null;
      }

      #region VSTO generated code

      /// <summary>
      /// Required method for Designer support - do not modify
      /// the contents of this method with the code editor.
      /// </summary>
      private void InternalStartup()
      {
         this.Startup += new System.EventHandler(ThisAddIn_Startup);
         this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
      }

      #endregion

      /// <summary>
      /// Adds commandBars to PowerPoint Application
      /// </summary>
      private void AddToolbar()
      {
         // VSTO API uses object-wrapped booleans
         object falseValue = false;
         object trueValue = true;

         // Try to get a handle to an existing COMMANDBAR_NAME CommandBar
         try
         {
            m_CommandBar = Application.CommandBars[COMMANDBAR_NAME];

            // If we found the CommandBar, then it's a permanent one we need to delete
            // Note: if the user has manually created a toolbar called COMMANDBAR_NAME it will get torched here
            if (m_CommandBar != null)
            {
               m_CommandBar.Delete();
            }
         }
         catch
         {
            // Benign - the CommandBar didn't exist
         }

         // Create a temporary CommandBar named COMMANDBAR_NAME
         m_CommandBar = Application.CommandBars.Add(COMMANDBAR_NAME, Office.MsoBarPosition.msoBarTop, falseValue, trueValue);

         if (m_CommandBar != null)
         {
            // Load any saved toolbar position
            LoadToolbarPosition();

            // Add our button to the command bar (as a temporary control) and an event handler.
            m_AlfrescoButton = (Office.CommandBarButton)m_CommandBar.Controls.Add(Office.MsoControlType.msoControlButton, missing, missing, missing, trueValue);
            if (m_AlfrescoButton != null)
            {
               m_AlfrescoButton.Style = Office.MsoButtonStyle.msoButtonCaption;
               m_AlfrescoButton.Caption = COMMANDBAR_BUTTON_CAPTION;
               m_AlfrescoButton.DescriptionText = COMMANDBAR_BUTTON_DESCRIPTION;
               Bitmap bmpButton = new Bitmap(GetType(), "toolbar.ico");
               m_AlfrescoButton.Picture = new ToolbarPicture(bmpButton);
               Bitmap bmpMask = new Bitmap(GetType(), "toolbar_mask.ico");
               m_AlfrescoButton.Mask = new ToolbarPicture(bmpMask);
               m_AlfrescoButton.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
               m_AlfrescoButton.Tag = "AlfrescoButton";

               // Finally add the event handler and make sure the button is visible
               m_AlfrescoButton.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(m_AlfrescoButton_Click);
               m_CommandBar.Visible = true;
            }

            // We need to find this toolbar later, so protect it from user changes
            m_CommandBar.Protection = Microsoft.Office.Core.MsoBarProtection.msoBarNoCustomize;
         }
      }

      /// <summary>
      /// Save CommandBar position
      /// </summary>
      private void SaveToolbarPosition()
      {
         if (m_CommandBar != null)
         {
            Properties.Settings.Default.ToolbarPosition = (int)m_CommandBar.Position;
            Properties.Settings.Default.ToolbarRowIndex = m_CommandBar.RowIndex;
            Properties.Settings.Default.ToolbarTop = m_CommandBar.Top;
            Properties.Settings.Default.ToolbarLeft = m_CommandBar.Left;
            Properties.Settings.Default.ToolbarSaved = true;
            Properties.Settings.Default.Save();
         }
      }

      /// <summary>
      /// Load CommandBar position
      /// </summary>
      private void LoadToolbarPosition()
      {
         if (m_CommandBar != null)
         {
            if (Properties.Settings.Default.ToolbarSaved)
            {
               m_CommandBar.Position = (Microsoft.Office.Core.MsoBarPosition)Properties.Settings.Default.ToolbarPosition;
               m_CommandBar.RowIndex = Properties.Settings.Default.ToolbarRowIndex;
               m_CommandBar.Top = Properties.Settings.Default.ToolbarTop;
               m_CommandBar.Left = Properties.Settings.Default.ToolbarLeft;
            }
         }
      }

      /// <summary>
      /// Alfresco toolbar button event handler
      /// </summary>
      /// <param name="Ctrl"></param>
      /// <param name="CancelDefault"></param>
      void m_AlfrescoButton_Click(Office.CommandBarButton Ctrl, ref bool CancelDefault)
      {
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.OnToggleVisible();
         }
         else
         {
            OpenAlfrescoPane(true);
         }
      }

      /// <summary>
      /// Fired when active presentation gets focus
      /// </summary>
      /// <param name="Pres"></param>
      /// <param name="Wn"></param>
      void Application_WindowActivate(Microsoft.Office.Interop.PowerPoint.Presentation Pres, Microsoft.Office.Interop.PowerPoint.DocumentWindow Wn)
      {
         System.Diagnostics.Debug.Print("Event: WindowActivate {0}", Pres.FullName);
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.OnDocumentChanged();
         }
      }

      /// <summary>
      /// Fired when active presentation is closed
      /// </summary>
      /// <param name="Pres"></param>
      void Application_PresentationClose(Microsoft.Office.Interop.PowerPoint.Presentation Pres)
      {
         System.Diagnostics.Debug.Print("Event: PresentationClose {0}", Pres.FullName);
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.OnDocumentBeforeClose();
         }
      }

      public void OpenAlfrescoPane()
      {
         OpenAlfrescoPane(true);
      }
      public void OpenAlfrescoPane(bool Show)
      {
         if (m_AlfrescoPane == null)
         {
            m_AlfrescoPane = new AlfrescoPane();
            m_AlfrescoPane.PowerPointApplication = Application;
            m_AlfrescoPane.DefaultTemplate = m_DefaultTemplate;
         }

         if (Show)
         {
            m_AlfrescoPane.Show();
            if (Application.Presentations.Count > 0)
            {
               m_AlfrescoPane.showDocumentDetails();
            }
            else
            {
               m_AlfrescoPane.showHome(false);
            }
            Application.Activate();
            m_AppWatcher.AlfrescoWindow = m_AlfrescoPane.Handle.ToInt32();
         }
      }

      public void CloseAlfrescoPane()
      {
         if (m_AlfrescoPane != null)
         {
            m_AlfrescoPane.Hide();
         }
      }
   }
}
