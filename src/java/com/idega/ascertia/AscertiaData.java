package com.idega.ascertia;

import java.io.InputStream;

public class AscertiaData {

	private String documentName;
	private InputStream inputStream;
	
	
	
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	
	
	
	
}
