package com.seamfix.nano.tools;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageModel.
 *
 * @author segz
 */

@SuppressWarnings("serial")
public class MessageModel implements Serializable {
	
	private Map<String, Object> model = new HashMap<String, Object>();
	
	public MessageModel(){
		//TODO: 
	}
	
	public void addModel(String key, Object item){
		model.put(key, item);
	}
	
	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

}
