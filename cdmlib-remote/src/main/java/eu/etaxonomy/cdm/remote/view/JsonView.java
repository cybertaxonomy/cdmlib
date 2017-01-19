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

import eu.etaxonomy.cdm.remote.config.DataSourceProperties;


public class JsonView extends BaseView implements View{

    public static final Logger logger = Logger.getLogger(JsonView.class);

    private JsonConfig jsonConfig;

    private DataSourceProperties dataSourceProperties;

    public DataSourceProperties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public enum Type{
        JSON("application/json"),
        XML("application/xml");

        private final String contentType;

        Type(String contentType){
            this.contentType = contentType;
        }

        public String getContentType(){
            return contentType;
        }
    }

    private Type type = Type.JSON;

    private String xsl = null;

    public void setXsl(String xsl) {
        this.xsl = xsl;
    }

    public String getXsl() {
        return xsl;
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
    @Override
    public String getContentType() {
        return type.getContentType();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.view.BaseView#render(java.lang.Object, java.io.PrintWriter, java.lang.String, java.lang.String)
     */
    @Override
    public void render(Object entity, PrintWriter writer, String jsonpCallback, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String contextPath = null;

        if (request != null)
        	{
        	contextPath = request.getContextPath();
        	}

        if(jsonConfig == null){
            logger.error("The jsonConfig must not be null. It must be set in the applicationContext.");
        }

        // option to skip json processing for debugging purposes, see #4925
        if(System.getProperty("SkipJSON") == null) {

        // create JSON Object

//        long start = System.currentTimeMillis();
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
            jsonObj = JSONObject.fromObject("{\"Integer\":"+((Integer)entity).intValue()+"}");
        } else if(entity instanceof Boolean){
            jsonObj = JSONObject.fromObject("{\"Boolean\":"+((Boolean)entity).toString()+"}");
        } else {
            jsonObj = JSONObject.fromObject(entity, jsonConfig);
        }
//        System.err.println("create JSON Object " + (System.currentTimeMillis() - start));

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
            if(type.equals(Type.XML) && xsl != null){

                if(contextPath == null){
                    contextPath = "";
                }
                String basepath = dataSourceProperties.getXslBasePath(contextPath + "/xsl");
                String replace = "\r\n<?xml-stylesheet type=\"text/xsl\" href=\"" + basepath + "/" + xsl + "\"?>\r\n";
                xml = xml.replaceFirst("\r\n", replace);
            }
            writer.append(xml);
        } else {
            // assuming json
            if(jsonpCallback != null){
                writer.append(jsonpCallback).append("(").append(jsonObj.toString()).append(")");
            } else {
                writer.append(jsonObj.toString());
            }
        }
        //TODO resp.setContentType(type);

        } else {
            writer.append("SkipJSON mode detected, this is for debugging only! Please contact the adminitrator.");
            // END SkipJSON
        }
        writer.flush();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Retrieve data from model
        Object entity = getResponseData(model);

        // set content type
        response.setContentType(type.getContentType());

        PrintWriter writer = response.getWriter();

        // read jsonp parameter from query string
        String jsonpCallback = extractJsonpCallback(request);

        // render
        render(entity, writer, jsonpCallback, request, response);
    }

    /**
     * @param request
     * @return
     */
    private String extractJsonpCallback(HttpServletRequest request) {
        String jsonpCallback= null;
        String queryString = request.getQueryString();
        if(queryString != null){
            String[] tokens = request.getQueryString().split("&", 0);
            String jsonpParamName = "callback";
            for (int i = 0; i < tokens.length; i++) {
                if(tokens[i].startsWith(jsonpParamName)){
                    jsonpCallback = tokens[i].substring(jsonpParamName.length() + 1);
                    break;
                }
            }
        }
        return jsonpCallback;
    }
}
