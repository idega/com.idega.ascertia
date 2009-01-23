//depends on mootools

function embedAscertiaApplet(URLToParentJSFolder,formName, note){
	GoSign_EmbedApplet(URLToParentJSFolder,'PDF', 'ZERO_FOOTPRINT', 'REMOTE',note); 
		if( !GoSign_IsAvailable() ){
		     alert(GoSign_GetErrorReason());
		}

		GoSign_SetFormName(formName);
			
		var b_result = GoSign_ShowCertificates(); 
		if( b_result != true){ 
		  alert(GoSign_GetErrorReason()); 
		} 
	} 


function signDocument(successPage,errorPage, loadingMessage){
	showLoadingMessage(loadingMessage);
	
	var id = window.setTimeout(function() {
		window.clearTimeout(id);
		
		var result = GoSign_SignDocument(); 
		
		if( result != true){ 
		  //alert(GoSign_GetErrorCode()+GoSign_GetErrorReason()); 
			window.location.href = errorPage+'&errorReason='+GoSign_GetErrorReason();
		 }else{
			window.location.href = successPage;
		}
	},10);
	
}

window.addEvent('domready', function() {
	

});