/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;


public class JsonView extends BaseView implements View{
	Log log = LogFactory.getLog(JsonView.class);

	public String getContentType() {
		return "application/json";
	}

	public void render(Map model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		// Retrieve data from model
		Object dto = getResponseData(model);
		
		// prepare writer
		// TODO determine preferred charset from HTTP Accept-Charset header
		Writer out = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(),  "UTF-8"));
		// create JSON Object
		if (dto != null && Collection.class.isAssignableFrom(dto.getClass())){
			JSONArray jObj = JSONArray.fromObject(dto);
			out.append(jObj.toString());
		}else if(dto instanceof Class){
			out.append("{\"name\":\"").append(((Class)dto).getName()).append("\", \"simpleName\": \"").append(((Class)dto).getSimpleName()).append("\"}");
		}else{
			
		
			JSONObject jObj = JSONObject.fromObject(dto);
			out.append(jObj.toString());
		}
		out.flush();
	}
}
