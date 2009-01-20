package eu.etaxonomy.cdm.model.common;

import org.hibernate.search.bridge.StringBridge;

public class StripHtmlBridge implements StringBridge {

	public String objectToString(Object object) {
		if(object != null) {
		  String string = (String) object;
		  return string.replaceAll("\\<.*?\\>", "");
		} else {
		  return null;
		}
	}

}
