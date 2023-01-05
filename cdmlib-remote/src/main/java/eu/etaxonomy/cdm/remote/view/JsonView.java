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

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.View;

import eu.etaxonomy.cdm.opt.config.DataSourceProperties;
import eu.etaxonomy.cdm.remote.json.JsonUtil;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.xml.XMLSerializer;


public class JsonView extends BaseView implements View {

    private static final Logger logger = LogManager.getLogger();

    static {
        optOut_BEANUTILS_520();
    }

    /**
     * BEANUTILS-520 Fixes CVE-2014-0114: https://nvd.nist.gov/vuln/detail/CVE-2014-0114
     * This patch by default enables the SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS.
     *
     * see https://github.com/apache/commons-beanutils/pull/7
     */
    static private void optOut_BEANUTILS_520() {
        final BeanUtilsBean bub = new BeanUtilsBean();
        bub.getPropertyUtils().removeBeanIntrospector(SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS);
        BeanUtilsBean.setInstance(bub);
    }

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

    @Override
    public String getContentType() {
        return type.getContentType();
    }

    @Override
    public void render(Object entity, PrintWriter writer, String jsonpCallback, HttpServletRequest request, HttpServletResponse response) {

        String contextPath = null;

        if (request != null) {
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
                if(isCollectionType && entity != null){  //can never be null here but there is an IDE warning otherwise
                    xmlSerializer.setArrayName(entity.getClass().getSimpleName());
                    Class<?> elementType = Object.class;
                    Collection<?> c = (Collection<?>)entity;
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
                   // writer.append(jsonpCallback).append("(").append(jsonObj.toString()).append(");");
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

    @Override
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Retrieve data from model
        Object entity = getResponseData(model);

        // set content type
        response.setContentType(type.getContentType());

        PrintWriter writer = response.getWriter();

        // read jsonp parameter from the request
        String jsonpCallback = JsonUtil.readJsonpCallback(request);

        try {
            // render
            render(entity, writer, jsonpCallback, request, response);
        } catch (Exception e) {
            if (request.getParameter("debug") != null){
                writer.write("Error when rendering a response object of type " + (entity == null? "null" :entity.getClass().getCanonicalName()) + System.lineSeparator() + System.lineSeparator());
                if (e.getCause() != null) {
                    //leave out the wrapping JSONException
                    e.getCause().printStackTrace(writer);
                }else {
                    writer.write("No stacktrace");
                }
                throw e;
            }
        }
    }
}