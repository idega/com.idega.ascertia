function GoSign_OpenDocument(x){	
    window.open(x, '_self');        
}

function GoSign_ReadHtmlFields(){

	var elems = document.forms[0].elements;	
	var result = elems[0].name;	
	
	for(i=1; i<document.forms[0].length; i++){
		result = result + "," + elems[i].name;
	}	
	return result;
}

function GoSign_SetFilterCriteria(CriteriaIdentifier, CriteriaValue) {
	var gosign = GetGoSignObject();
 	var value = gosign.setFilterCriteria(CriteriaIdentifier, CriteriaValue);
	return value;
}

function GoSign_ShowCertificates(){
	var gosign = GetGoSignObject();
	return gosign.populateCertificateList();
}

function GoSign_PopulateCertificates(certs, aliases){
	
	var select_certs = document.forms[0].elements.GoSignCertificateList;
	var array_certs = certs.split('~&~');
	var array_aliases = aliases.split('~&~');	
	for(var i=0; i<array_certs.length; i++){
		if(array_certs[i] == null || array_certs[i] == ""){
		}else{
		select_certs.options[select_certs.options.length] = new Option(array_certs[i], array_aliases[i]);	
		}
	}
}

function GoSign_SignDocument() {
	var gosign = GetGoSignObject();
	return	gosign.signDocument();
}

function GoSign_SetTargetURL(TargetUrl) {
	var gosign = GetGoSignObject();
	gosign.setTargetUrl(TargetUrl);
}

function GoSign_SetResultPage(ResultPage) {
	var gosign = GetGoSignObject();
	gosign.setResultPage(ResultPage);
}

function GoSign_SetErrorPage(ErrorPage) {
	var gosign = GetGoSignObject();	
	gosign.setErrorPage(ErrorPage);
}

function GoSign_ResetErrorCode(){
	var gosign = GetGoSignObject();	
	gosign.resetErrorCode();
}

function GetGoSignObject(){		
		if(document.getElementsByTagName('object').length > 1)
			return document.GoSign[0];
		else
			return document.GoSign;
}

function GoSign_GetCertificateList(){
	var list_exists = true;
	var list_certs = document.getElementById("GoSignCertificateList");
	if(list_certs == null){
		list_exists = false;
	}else{
		list_exists = true;
	}
	return list_exists;
}

function GoSign_GetErrorCode(){
	var gosign = GetGoSignObject();	
	var errorCode = gosign.getErrorCode();
	return errorCode;
}

function GoSign_GetErrorReason(){
	var gosign = GetGoSignObject();	
	var errorReason = gosign.getErrorReason();
	return errorReason;
}

function GoSign_EmbedApplet(GoSignRootFolderURL, SignatureType, SignatureMechanism, ContentSource) {
	document.write('<p>' +
	'' +
	'<!--[if !IE]> Firefox and others will use outer object -->\n' +
	'      <object classid=\"java:com.ascertia.zfp.gosign.applet.ASC_GoSignApplet.class\" ' +
	'               name=\"GoSign\" type=\"application/x-java-applet\"' +
	'              archive=\"asc_gosign.jar,assembla_msks_jce.jar\" ' +
	'              height=\"0\" width=\"0\" >\n' +
	'        <!-- Konqueror browser needs the following param --> ' +
	'		<param name=\"codebase\" value=\"'+GoSignRootFolderURL+'/lib\" />\n' +
	'        <param name=\"archive\" value=\"asc_gosign.jar,assembla_msks_jce.jar\" />\n		' +
	'    <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>\n' +
	'    <param name=\"SignatureType\" value=' + SignatureType + '>\n' +
	'    <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>\n' +
	'    <param name=\"ContentSource\" value=' + ContentSource + '>\n' +	
	'		<param name=\"name\" value=\"Ascertia GoSign\" />\n' +
	'		<param name=\"mayscript\" value=\"true\">\n' +
	'      <!--<![endif]-->\n' +
	'        <!-- MSIE (Microsoft Internet Explorer) will use inner object -->\n ' +
	'        <object name=\"GoSign\" title=\"Ascertia GoSign Applet\" classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"' +
	'    width=\"0\" height=\"0\" hspace=\"0\" vspace=\"0\" align=\"baseline\"' +
	'    codebase=\"http://javadl-esd.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=1,5,0,0\">\n' +
	'		' +
	'    <param name=\"codebase\" value=\"'+GoSignRootFolderURL+'/lib\">\n' +
	'    <param name=\"code\" value=\"com.ascertia.zfp.gosign.applet.ASC_GoSignApplet.class\">\n' +
	'    <param name=\"name\" value=\"Ascertia GoSign\">\n' +
	'    <param name=\"archive\" value=\"asc_gosign.jar,assembla_msks_jce.jar\">\n' +
	'    <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>\n' +
	'    <param name=\"SignatureType\" value=' + SignatureType + '>\n' +
	'    <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>\n' +
	'    <param name=\"ContentSource\" value=' + ContentSource + '>\n' +	
	'    <param name=\"mayscript\" value=\"true\">\n' +
	'      <font color=\"#FF0000\">\n' +
	'      No Java Runtime Environment (JRE) found, get the correct Java plug-in from ' +
	' <a href=\"http://www.java.com/en/download/\"><font color=\"#0000FF\">here.</font></a>\n' +
	'  </object>\n  ' +
	'      <!--[if !IE]> close outer object -->\n' +
	'' +
	'      </object>\n' +
	'      <!--<![endif]-->\n  ' +
	'' +
	'</p>\n' +
	'' +
	'<p>\n' +
	'      <strong>NOTE:</strong> Java Runtime Environment (JRE) version 5.0 (or latest) is needed to run the GoSign applet. If the applet does not load properly please get the correct Java plug-in from' +
	'             ' +
	'      <a href=\"http://www.java.com/en/download/\">' +
	'' +
	'        <font color=\"#0000FF\">here.</font>\n' +
	'      </a>\n' +
	'    </p>\n');
	
	var gosign = GetGoSignObject();	
	var error_code = gosign.getErrorCode();
	while( error_code == "undefined" || error_code == -1 ){
		GoSign_Sleep(1000);
		error_code = gosign.getErrorCode();
	}
	if( error_code == 0 ){
		return true;
	}
	else{
		return false;
	}
}

function GoSign_Sleep(interval) {		
	setTimeout(true == true, interval);
}     

function GoSign_Constants() {}     

// Define Constants  
GoSign_Constants.TRUE = "TRUE";  
GoSign_Constants.FALSE = "FALSE";  
GoSign_Constants.PDF = "PDF";  
GoSign_Constants.XML = "XML";  
GoSign_Constants.PKCS7 = "PKCS7";  
GoSign_Constants.DETACHED = "DETACHED";  
GoSign_Constants.ENVELOPED = "ENVELOPED";  
GoSign_Constants.ENVELOPING = "ENVELOPING";  
GoSign_Constants.ZERO_FOOTPRINT = "ZERO_FOOTPRINT";  
GoSign_Constants.LOCAL = "LOCAL";  
GoSign_Constants.REMOTE = "REMOTE"; 
GoSign_Constants.SUBJECT_DN_CONTAINS = "SUBJECT_DN";   
GoSign_Constants.ISSUER_DN_CONTAINS = "ISSUER_DN";   
GoSign_Constants.SHOW_EXPIRED_CERTIFICATES = "SHOW_EXPIRED_CERTIFICATES";    
GoSign_Constants.KEY_USAGE_CONTAINS = "KEY_USAGE";   
GoSign_Constants.POLICY_OID_CONTAINS = "POLICY_OID";   
GoSign_Constants.ALGORITHM_OID_CONTAINS = "ALGORITHM_OID";
GoSign_Constants.DIGITAL_SIGNATURE = "DIGITAL_SIGNATURE";   
GoSign_Constants.NON_REPUDIATION = "NON_REPUDIATION";
GoSign_Constants.KEY_ENCIPHERMENT = "KEY_ENCIPHERMENT";   
GoSign_Constants.DATA_ENCIPHERMENT = "DATA_ENCIPHERMENT";
GoSign_Constants.KEY_AGREEMENT = "KEY_AGREEMENT";   
GoSign_Constants.KEY_CERT_SIGN = "KEY_CERT_SIGN";
GoSign_Constants.CRL_SIGN = "CRL_SIGN";   
GoSign_Constants.ENCIPHER_ONLY = "ENCIPHER_ONLY";
GoSign_Constants.DECIPHER_ONLY = "DECIPHER_ONLY";