// ActionScript filepackage{
	import util.authentication.AuthenticationService;
	import util.authentication.LoginCompleteEvent;
	import util.authentication.LogoutCompleteEvent;
	import app.logout.logout;
	import util.error.ErrorService;
	import util.error.ErrorRaisedEvent;
	import mx.controls.Alert;
	import mx.core.Application;
	import mx.containers.Canvas;
	import mx.containers.Panel;
	import app.login.login;
	/**	 * Main class	 */	public class MainClass extends Application	{		/** UI components */		public var mainCanvas:Canvas;		public var loginPanel:login;				/**		 * Constructor		 */		public function MainClass():void		{			// Register interest in the error service events			ErrorService.instance.addEventListener(ErrorRaisedEvent.ERROR_RAISED, onErrorRaised);						// Register interest in authentication service events			AuthenticationService.instance.addEventListener(LoginCompleteEvent.LOGIN_COMPLETE, doLoginComplete);			AuthenticationService.instance.addEventListener(LogoutCompleteEvent.LOGOUT_COMPLETE, doLogoutComplete);			
		}
				/**		 * Event handler called when login is successfully completed		 * 		 * @param	event	login complete event		 */
		private function doLoginComplete(event:LoginCompleteEvent):void
		{
			mainCanvas.visible = true;
		    loginPanel.visible=false;
		}
				/**		 * Event handler called when logout is successfully completed		 * 		 * @param	event	logout complete event		 */
		private function doLogoutComplete(event:LogoutCompleteEvent):void
		{
			mainCanvas.visible = false;
		    loginPanel.visible=true;
		
		    //myframe.source='';
		    //resultsDispPanel.width=maxWidth;    
		    //searchResults.dataProvider="";  
		    //cb1.selected=true; 
		}				/**		 * Event handler called when error is raised		 * 		 */		private function onErrorRaised(event:ErrorRaisedEvent):void		{			// TODO figure out how we react to ApplicationError's raised			if (event.errorType == ErrorService.APPLICATION_ERROR)			{				Alert.show("Application Error: " + event.error.message);			}
		}	}}

