// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
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
