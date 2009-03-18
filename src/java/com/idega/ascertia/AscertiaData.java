package com.idega.ascertia;


//TODO: remove input stream, put variable hash and task instance id, so the servlets could resolve it
public class AscertiaData {

	private String documentName;	
	private byte [] byteDocument;
	
	
	public byte[] getByteDocument() {
		return byteDocument;
	}
	public void setByteDocument(byte[] byteDocument) {
		this.byteDocument = byteDocument;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	
	
	
	
}
