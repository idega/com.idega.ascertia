
var formName;


function GoSign_OpenDocument(x){
    window.open(x, '_self');
}

function GoSign_SetFormName(a_formName){
	formName = a_formName;
}

function GoSign_GetFormName(){
	return formName;
}


function GoSign_ReadHtmlFields(){

	var elems = document[formName].elements;
	var result = "";
	for(i=0; i<document[formName].length; i++){
		if(elems[i].type == 'text' || elems[i].type == 'select-one' || elems[i].type == 'textarea' || elems[i].type == 'hidden'){
			result = result+elems[i].name+",";
		}
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

function GoSign_PopulateCertificates(certs){
	var select_certs = document[formName].elements.GoSignCertificateList;
	var array_certs = certs.split('~!~');
	for(var i=0; i<array_certs.length-1; i++){
		var array_alias = array_certs[i].split('~&~');
		var array_displayValue = new Array();
		for(var j=0;j<array_alias.length;j++){
			array_displayValue[j] = array_alias[j];
		}
		select_certs.options[select_certs.options.length] = new Option(array_displayValue[0], array_displayValue[1]);
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

function GoSign_SetHashAlgorithm(HashAlgorithm){
	var gosign = GetGoSignObject();
	gosign.setHashAlgorithm(HashAlgorithm);
}

function GoSign_SetSignatureMechanism(SignatureMechanism){
	var gosign = GetGoSignObject();
	gosign.setSignatureMechanism(SignatureMechanism);
}

function GoSign_SetContentSource(ContentSource){
	var gosign = GetGoSignObject();
	gosign.setContentSource(ContentSource);
}

function GetGoSignObject(){
		if(navigator.appName == "Netscape"){
			return document.GoSign_FF;
		}else{
			return document.GoSign_IE;
		}
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

function GoSign_SetAliasDisplayTemplate(aliasTemplate){
	var gosign = GetGoSignObject();
	gosign.setAliasDisplayTemplate(aliasTemplate);
}
function GoSign_SetAliasDisplayValueMissing(aliasDisplayMissingValue){
	var gosign = GetGoSignObject();
	gosign.setAliasDisplayValueMissing(aliasDisplayMissingValue);
}

function GoSign_EmbedApplet(GoSignRootFolderURL, SignatureType, SignatureMechanism, ContentSource, note) {
	document.write(
        '<p>' +
	'<!--[if !IE]> Firefox and others will use outer object -->' +
	'      <object classid=\"java:com.ascertia.adss.applet.gosign.ASC_GoSignApplet.class\" ' +
	'           name=\"GoSign_FF\" '+
        '           id=\"GoSign_FF\" '+
        '           type=\"application/x-java-applet\"' +
	'           archive=\"asc_gosign.jar\" ' +
	'           height=\"0\" width=\"0\" ' +
	'       >' +
	'           <!-- Konqueror browser needs the following param --> ' +
	'	    <param name=\"codebase\" value=\"'+GoSignRootFolderURL+'/lib\" />' +
	'           <param name=\"archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_version\" value=\"0.0.0.2\" />		' +
	'           <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>' +
	'           <param name=\"SignatureType\" value=' + SignatureType + '>' +
	'           <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>' +
	'           <param name=\"ContentSource\" value=' + ContentSource + '>' +
	'	    <param name=\"name\" value=\"Ascertia GoSign\" />' +
	'	    <param name=\"mayscript\" value=\"true\">' +
	'<!--<![endif]-->' +
	'      <!-- MSIE (Microsoft Internet Explorer) will use inner object --> ' +
	'      <object classid=\"clsid:CAFEEFAC-0016-0000-FFFF-ABCDEFFEDCBA\" '+
        '           name=\"GoSign_IE\" '+
        '           id=\"GoSign_IE\" '+
        '           title=\"Ascertia GoSign\" ' +
	'           width=\"0\" height=\"0\" hspace=\"0\" vspace=\"0\" align=\"baseline\"' +
	'           codebase=\"http://java.sun.com/update/1.6.0/jinstall-6u10-windows-i586.cab#Version=1,6,0_10,33\"'+
        '       >' +
	'           <param name=\"codebase\" value=\"'+GoSignRootFolderURL+'/lib\">' +
	'           <param name=\"code\" value=\"com.ascertia.adss.applet.gosign.ASC_GoSignApplet.class\">' +
	'           <param name=\"name\" value=\"Ascertia GoSign\">' +
	'           <param name=\"archive\" value=\"asc_gosign.jar\">' +
	'           <param name=\"cache_archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_version\" value=\"0.0.0.2\" />		' +
	'           <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>' +
	'           <param name=\"SignatureType\" value=' + SignatureType + '>' +
	'           <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>' +
	'           <param name=\"ContentSource\" value=' + ContentSource + '>' +
	'           <param name=\"mayscript\" value=\"true\">' +
	'     <font color=\"#FF0000\">' +
	'      No Java Runtime Environment (JRE) found, get the correct Java plug-in from ' +
	' <a href=\"http://www.java.com/en/download/\"><font color=\"#0000FF\">here.</font></a>' +
	'  </object>  ' +
	'<!--[if !IE]> close outer object -->' +
	'      </object>' +
	'<!--<![endif]-->  ' +
	'</p>' +
	'<p>' + note +
	'</p>');
}

function GoSign_EmbedApplet_PKCS11(GoSignRootFolderURL, SignatureType, SignatureMechanism, ContentSource, DLL, PKCS11Name, Slot, Pin) {
	document.write(
        '<p>' +
	'<!--[if !IE]> Firefox and others will use outer object -->' +
	'      <object classid=\"java:com.ascertia.adss.applet.gosign.ASC_GoSignApplet.class\" ' +
	'           name=\"GoSign_FF\" '+
        '           id=\"GoSign_FF\" '+
        '           type=\"application/x-java-applet\"' +
	'           archive=\"asc_gosign.jar\" ' +
	'           height=\"0\" width=\"0\" ' +
	'       >' +
	'           <!-- Konqueror browser needs the following param --> ' +
	'	    <param name=\"codebase\" value=\"GoSign/lib\" />' +
	'           <param name=\"archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_version\" value=\"0.0.0.2\" />		' +
	'           <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>' +
	'           <param name=\"SignatureType\" value=' + SignatureType + '>' +
	'           <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>' +
	'           <param name=\"ContentSource\" value=' + ContentSource + '>' +
        '           <param name=\"DLL\" value=' + DLL + '>' +
        '           <param name=\"PKCS11Name\" value=' + PKCS11Name + '>' +
	'           <param name=\"Slot\" value=' + Slot + '>' +
	'           <param name=\"Pin\" value=' + Pin + '>' +
	'	    <param name=\"name\" value=\"Ascertia GoSign\" />' +
	'	    <param name=\"mayscript\" value=\"true\">' +
	'<!--<![endif]-->' +
	'      <!-- MSIE (Microsoft Internet Explorer) will use inner object --> ' +
	'      <object classid=\"clsid:CAFEEFAC-0016-0000-FFFF-ABCDEFFEDCBA\" '+
        '           name=\"GoSign_IE\" '+
        '           id=\"GoSign_IE\" '+
        '           title=\"Ascertia GoSign\" ' +
	'           width=\"0\" height=\"0\" hspace=\"0\" vspace=\"0\" align=\"baseline\"' +
	'           codebase=\"http://java.sun.com/update/1.6.0/jinstall-6u10-windows-i586.cab#Version=1,6,0_10,33\"'+
        '       >' +
	'           <param name=\"codebase\" value=\"GoSign/lib\">' +
	'           <param name=\"code\" value=\"com.ascertia.adss.applet.gosign.ASC_GoSignApplet.class\">' +
	'           <param name=\"name\" value=\"Ascertia GoSign\">' +
	'           <param name=\"archive\" value=\"asc_gosign.jar\">' +
	'           <param name=\"cache_archive\" value=\"asc_gosign.jar\" />		' +
	'           <param name=\"cache_version\" value=\"0.0.0.2\" />		' +
	'           <param name=\"GoSignRootFolderURL\" value=' + GoSignRootFolderURL + '>' +
	'           <param name=\"SignatureType\" value=' + SignatureType + '>' +
	'           <param name=\"SignatureMechanism\" value=' + SignatureMechanism + '>' +
	'           <param name=\"ContentSource\" value=' + ContentSource + '>' +
        '           <param name=\"DLL\" value=' + DLL + '>' +
        '           <param name=\"PKCS11Name\" value=' + PKCS11Name + '>' +
	'           <param name=\"Slot\" value=' + Slot + '>' +
	'           <param name=\"Pin\" value=' + Pin + '>' +
	'           <param name=\"mayscript\" value=\"true\">' +
	'     <font color=\"#FF0000\">' +
	'      No Java Runtime Environment (JRE) found, get the correct Java plug-in from ' +
	' <a href=\"http://www.java.com/en/download/\"><font color=\"#0000FF\">here.</font></a>' +
	'  </object>  ' +
	'<!--[if !IE]> close outer object -->' +
	'      </object>' +
	'<!--<![endif]-->  ' +
	'</p>' +
	'<p>' +
	'     <strong>NOTE:</strong> Java Runtime Environment (JRE) version 6.0 (or latest) is needed to run the Ascertia GoSign applet. If the applet does not load properly please get the correct Java plug-in from' +
	'      <a href=\"http://www.java.com/en/download/\">' +
	'        <font color=\"#0000FF\">here.</font>' +
	'      </a>' +
	'</p>');
}

function GoSign_IsAvailable(){
	GoSign_IsReady();
	var gosign = GetGoSignObject();
	var error_code = gosign.getErrorCode();
	while( error_code == "undefined" || error_code == -1 ){
		error_code = gosign.getErrorCode();
	}
	if( error_code == 0 ){
		formName = document.forms[0].name;
		return true;
	}
	else{
		return false;
	}
}


function GoSign_IsReady(){
	var status = false;
	var gosign = GetGoSignObject();
	status = gosign.isActive();
	while( status != true ){
		GoSign_Sleep(1000);
		status = gosign.isActive();
	}
}

function GoSign_Sleep(interval) {
	var gosign = GetGoSignObject();
	gosign.sleep(interval);
}

function GoSign_Constants() {}
// Define Constants
  GoSign_Constants.SHA1 = "SHA1";
  GoSign_Constants.SHA256 = "SHA256";
  GoSign_Constants.SHA384 = "SHA384";
  GoSign_Constants.SHA512 = "SHA512";
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
  GoSign_Constants.SUBJECT_CN = "SUBJECT_CN";
  GoSign_Constants.SUBJECT_C = "SUBJECT_C";
  GoSign_Constants.SUBJECT_OU = "SUBJECT_OU";
  GoSign_Constants.SUBJECT_O = "SUBJECT_O";
  GoSign_Constants.SUBJECT_S = "SUBJECT_S";
  GoSign_Constants.SUBJECT_L = "SUBJECT_L";
  GoSign_Constants.SUBJECT_E = "SUBJECT_E";
  GoSign_Constants.ISSUER_CN = "ISSUER_CN";
  GoSign_Constants.ISSUER_C = "ISSUER_C";
  GoSign_Constants.ISSUER_OU = "ISSUER_OU";
  GoSign_Constants.ISSUER_O = "ISSUER_O";
  GoSign_Constants.ISSUER_S = "ISSUER_S";
  GoSign_Constants.ISSUER_L = "ISSUER_L";
  GoSign_Constants.ISSUER_E = "ISSUER_E";
