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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.xml.XMLSerializer;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.View;


public class JsonView extends BaseView implements View{
	
	public static final Logger logger = Logger.getLogger(JsonView.class);

	private JsonConfig jsonConfig;
	
	public enum Type{
		JSON("application/json"),
		XML("application/xml");
		
		private String contentType;
		
		Type(String contentType){
			this.contentType = contentType;
		}
		
		public String getContentType(){
			return contentType;
		}
	}

	private Type type = Type.JSON;

	private String xsl;
	
	public void setXsl(String xsl) {
		this.xsl = xsl;
	}

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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.View#getContentType()
	 */
	public String getContentType() {
		return type.getContentType();
	}
	
	public void render(Object entity, PrintWriter writer) throws Exception {
		
		
		// create JSON Object
		boolean isCollectionType = false;
		JSON jsonObj;
		if (entity == null){
		  jsonObj = JSONObject.fromObject("{}");
		} else if(Collection.class.isAssignableFrom(entity.getClass())){
			isCollectionType = true;
			jsonObj = JSONArray.fromObject(entity, jsonConfig);
		}else if(entity instanceof String){
			
			jsonObj = JSONObject.fromObject("{\"String\":\""+entity.toString().replace("\"", "\\\"")+"\"}");
		} else if(entity instanceof Integer){
			jsonObj = JSONObject.fromObject("{\"Integer\":\""+((Integer)entity).intValue()+"\"}");
		} else {
			jsonObj = JSONObject.fromObject(entity, jsonConfig);
		}
		
		if(type.equals(Type.XML)){
			XMLSerializer xmlSerializer = new XMLSerializer();
			if(isCollectionType){
				xmlSerializer.setArrayName(entity.getClass().getSimpleName());
				Class elementType = Object.class;
				Collection c = (Collection)entity;
				if(c.size() > 0){
					elementType = c.iterator().next().getClass();
				}
				xmlSerializer.setObjectName(elementType.getSimpleName());
			} else if(entity != null){
				xmlSerializer.setObjectName(entity.getClass().getSimpleName());
			}
			String xml = xmlSerializer.write( jsonObj );
			if(xsl != null){
				String xslInclude = "\r\n<?xml-stylesheet type=\"text/xsl\" href=\"human.xsl\"?>\r\n";
				xml = xml.replaceFirst("\r\n", xslInclude);
			}
			writer.append(xml);
		} else {
			// assuming json
			writer.append(jsonObj.toString());
		}
		//TODO resp.setContentType(type);
		writer.flush();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
				
		// Retrieve data from model
		Object entity = getResponseData(model);
		
		// set content type 
		response.setContentType(type.getContentType());
		
		// render
		render(entity, response.getWriter());
		
	}	
}
