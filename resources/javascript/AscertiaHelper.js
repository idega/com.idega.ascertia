//depends on mootools

function embedAscertiaApplet(URLToParentJSFolder,formName){
		var initResults = GoSign_EmbedApplet(URLToParentJSFolder,'PDF', 'ZERO_FOOTPRINT', 'REMOTE'); 
		GoSign_SetFormName(formName);
		if( initResults != true){ 
		  alert(GoSign_GetErrorReason()); 
		} 
		
	
		var b_result = GoSign_ShowCertificates(); 
		if( b_result != true){ 
		  alert(GoSign_GetErrorReason()); 
		} 
	} 


function signDocument(successPage,errorPage, loadingMessage){
	showLoadingMessage(loadingMessage);
	var result = GoSign_SignDocument(); 
	console.log('signDoc called ' + result);
	if( result != true){ 
	  //alert(GoSign_GetErrorCode()+GoSign_GetErrorReason()); 
	  changeWindowLocationHrefAndCheckParameters(errorPage+'&errorReason='+GoSign_GetErrorReason(),true);
	 }else{
	  changeWindowLocationHrefAndCheckParameters(successPage,true);
	}
}

window.addEvent('domready', function() {
	
	

	/*
	var b_result = 
		Sign_SetFilterCriteria(GoSign_Constants.SUBJECT_DN_CONTAINS, 
		"O=Ascertia"); 
		if( b_result != true){ 
		 alert(GoSign_GetErrorReason} 
*/
		

/*	$(PARAMETER_SAVE).addEvents({
		'click': function(dwrEvent) {
			var source = 'iwscriptlet';
			var eventType = 'PARAMETER_SAVE';
			var eventData = new Object();
			eventData.fileName = $(PARAMETER_FILE_NAME).value;
			eventData.scriptString = $(PARAM_SCRIPT_STRING).value;
			eventData.scriptType = "bsh";
			var sendToAllSessionsOnPage = true;
			var saveEvent = new DWREvent(source, eventType, eventData, sendToAllSessionsOnPage);
			dwrFireEvent(saveEvent);
		}
	});
	
	$(PARAMETER_RUN).addEvents({
		'click': function(dwrEvent) {
			alert('test');
		}
	});
	
	window.addEvents({
		'showScriptletResult': function(dwrEvent) {
			replaceHtml($('iwscriptlet'),dwrEvent.eventData.html);
			alert('replaced');
		}
	});
	
	*/

});