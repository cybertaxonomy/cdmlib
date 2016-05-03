/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import static eu.etaxonomy.cdm.common.XmlHelp.getBeansRoot;
import static eu.etaxonomy.cdm.common.XmlHelp.saveToXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.common.XmlHelp;

/**
 * Utility class which manages the persistent source settings
 * in the datasource datasource config
 * 
 * @author cmathew
 *
 */
public class CdmPersistentSourceUtils {
	private static final Logger logger = Logger.getLogger(CdmPersistentSourceUtils.class);
		
	
	
	/**
	 * Returns the directory containing the datasource config file 
	 * @return
	 */
	public static String getResourceDirectory(){
		try {
			File f = CdmApplicationUtils.getWritableResourceDir();
			return f.getPath();
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the datasource config file input stream.
	 * 
	 * @return data source config file input stream
	 */
	public static FileInputStream getCdmSourceInputStream(){
		String dir = CdmPersistentSourceUtils.getResourceDirectory();
		File file = new File(dir + File.separator +  CdmPersistentXMLSource.CDMSOURCE_FILE_NAME);
		return fileInputStream(file);
	}
	
	/**
	 * Returns the datasource config file outputStream.
	 * 
	 * @return data source config file outputStream
	 */
	public static FileOutputStream getCdmSourceOutputStream(){
		String dir = CdmPersistentSourceUtils.getResourceDirectory();
		File file = new File(dir + File.separator +  CdmPersistentXMLSource.CDMSOURCE_FILE_NAME);
		return fileOutputStream(file);
	}

	/**
	 * Returns a FileInputStream object from a File object
	 * 
	 * @param file 
	 * @return FileInputStream object
	 */
	public static FileInputStream fileInputStream(File file){
		try {
			FileInputStream fis = new FileInputStream(file);
			return fis;
		} catch (FileNotFoundException e) {
			logger.warn("File " + file == null?"null":file.getAbsolutePath() + " does not exist in the file system");
			return null;
		}
	}
	
	/**
	 * Returns a FileOututStream object from a File object
	 * 
	 * @param file 
	 * @return FileOututStream object
	 */
	public static FileOutputStream fileOutputStream(File file){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			return fos;
		} catch (FileNotFoundException e) {
			logger.warn("File " + (file == null?"null":file.getAbsolutePath()) + " does not exist in the file system");
			return null;
		}
	}
	

	/**
	 * Returns the jdom Element representing the data source bean in the config file.
	 * 
	 * @param beanName , of the element to be retrieved
	 * @return jdom Element representing the data source bean in the config file.
	 */
	public static Element getCdmSourceBeanXml(String beanName){
		FileInputStream inStream = getCdmSourceInputStream();
		Element root = getBeansRoot(inStream);
		if (root == null){
			return null;
		}else{
	    	Element xmlBean = XmlHelp.getFirstAttributedChild(root, "bean", "id", beanName);
			if (xmlBean == null){
				//TODO warn or info
				logger.debug("Unknown Element 'bean id=" + beanName + "' ");
			}
			return xmlBean;
		}
	}
	

	/**
	 * Returns the jdom Element corresponding to the cdm source name and type in the config file.
	 * 
	 * @param cdmSourceName , of the element to be retrieved
	 * @param postFix , indicating the type of cdm source
	 * @return jdom Element corresponding to the cdm source name and type in the config file.
	 */
	public static Element getCdmSourceBeanXml(String cdmSourceName, String postFix){
		return getCdmSourceBeanXml(getBeanName(cdmSourceName, postFix));
	}
	


	/**
	 * Converts input parameters to bean name.
	 * 
	 * @param cdmSourceName , of the element to be retrieved
	 * @param postFix , indicating the type of cdm source
	 * @return bean name corresponding to the cdm source name and type in the config file.
	 */
	public static String getBeanName(String cdmSourceName, String postFix){
		return cdmSourceName == null? null : cdmSourceName + postFix;
	}
	
	
	/**
	 * Deletes a CDM persistent source from the source config file.
	 * 
	 * @param cdmPersistentSource , CDM persistent source object
	 */
	public static void delete (ICdmPersistentSource cdmPersistentSource) {
		delete(cdmPersistentSource.getBeanName());
	}
	

	/**
	 * Deletes a CDM persistent source from the source config file.
	 * 
	 * @param beanName , CDM persistent source bean name
	 */
	public static void delete (String beanName){
		Element bean = getCdmSourceBeanXml(beanName);
		if (bean != null){
			Document doc = bean.getDocument();
			bean.detach();
			saveToXml(doc, 
					CdmPersistentSourceUtils.getCdmSourceOutputStream(), 
					XmlHelp.prettyFormat );
		}
	}
	
	
	/**
	 * Returns the property value of a CDM persistent source object bean
	 * from the source config file.
	 * 
	 * @param bean , CDM persistent source object bean
	 * @param property , whose value is to be retrieved
	 * @return value of requested property
	 */
	public static String getPropertyValue(Element bean, String property){
		Element driverProp = XmlHelp.getFirstAttributedChild(bean, "property", "name", property);
		if (driverProp == null){
			logger.debug("Unknown property: " + property);
	    	return null;
		}else{
			String strProperty = driverProp.getAttributeValue("value");
			return strProperty;
		}
	}

}
