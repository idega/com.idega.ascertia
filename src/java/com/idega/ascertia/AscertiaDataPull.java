package com.idega.ascertia;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Scope("singleton")
@Service
public class AscertiaDataPull {
	
	Map<String,AscertiaData> data;
	
	public AscertiaDataPull(){
		data = new HashMap<String, AscertiaData>();
	}
	
	private Map<String, AscertiaData> getData() {
		return data;
	}
	
	private void setData(Map<String, AscertiaData> data) {
		this.data = data;
	}

	public void push(String conversationId, AscertiaData AscertiaData){
		
		getData().put(conversationId, AscertiaData);
	}
	
	public AscertiaData pop(String conversationId){
		AscertiaData data = getData().get(conversationId);
		getData().remove(conversationId);
		return data;
	}
}
