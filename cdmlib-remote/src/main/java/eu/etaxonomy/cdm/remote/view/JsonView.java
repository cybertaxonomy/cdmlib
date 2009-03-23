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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;


public class JsonView extends BaseView implements View{
	Log log = LogFactory.getLog(JsonView.class);

	private JsonConfig jsonConfig;
	
	public enum Type{
		JSON, XML
	}

	private Type type = Type.JSON;
	
	public Type getType() {
		return type;
	}

	/**
	 * Default is Type.JSON
	 * @param type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	public void setJsonConfig(JsonConfig jsonConfig) {
		this.jsonConfig = jsonConfig;
	}
	
	public String getContentType() {
		return "application/json";
	}

	public void render(Map model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
//		// configure the serialisation
//		// TODO implement a more generic approach as done in CATE: http://forge.nesc.ac.uk/cgi-bin/cvsweb.cgi/cate-view/src/main/java/org/cateproject/view/json/JsonConfigFactoryBean.java?rev=1.1&content-type=text/x-cvsweb-markup&cvsroot=cate
//		jsonConfig.registerJsonValueProcessor(org.joda.time.DateTime.class, new DateTimeJSONValueProcessor());
//		jsonConfig.registerJsonValueProcessor(java.util.Calendar.class, new CalendarJSONValueProcessor());
//		jsonConfig.registerJsonValueProcessor(Partial.class, new PartialJSONValueProcessor());
//		jsonConfig.registerJsonValueProcessor(UUID.class, new UUIDJSONValueProcessor());
//		jsonConfig.setJsonPropertyFilter(new InitializedHibernatePropertyFilter());
//		jsonConfig.setIgnoreJPATransient(true);
//		//jsonConfig.setJsonBeanProcessorMatcher(new CGLibEnhancedBeanProcessorMatcher());
		
		
		// Retrieve data from model
		Object dto = getResponseData(model);
		
		// prepare writer
		// TODO determine preferred charset from HTTP Accept-Charset header
		Writer out = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(),  "UTF-8"));
		// create JSON Object
		JSON jsonObj;
		if (dto != null && Collection.class.isAssignableFrom(dto.getClass())){
			jsonObj = JSONArray.fromObject(dto, jsonConfig);
		}else if(dto instanceof Class){
			StringBuffer jsonStr = new StringBuffer().append("{\"name\":\"").append(((Class)dto).getName()).append("\", \"simpleName\": \"").append(((Class)dto).getSimpleName()).append("\"}");
			jsonObj = JSONObject.fromObject(jsonStr);
		}else{
			jsonObj = JSONObject.fromObject(dto, jsonConfig);
		}
		
		if(type.equals(Type.XML)){
			XMLSerializer xmlSerializer = new XMLSerializer();
			xmlSerializer.setObjectName(dto.getClass().getSimpleName());
			String xml = xmlSerializer.write( jsonObj );
			out.append(xml);
		} else {
			// assuming json
			out.append(jsonObj.toString());
		}
		out.flush();
	}
}
