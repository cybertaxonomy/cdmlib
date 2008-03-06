package eu.etaxonomy.cdm.remote.view;

import java.util.Map;

public abstract class BaseView {
	
	protected Object getResponseData(Map model){
		// Retrieve data from model
		Object data = null;
		if (model!=null && model.values().size()>0){
			data = model.values().toArray()[0];
		}
		return data;
	}
}
