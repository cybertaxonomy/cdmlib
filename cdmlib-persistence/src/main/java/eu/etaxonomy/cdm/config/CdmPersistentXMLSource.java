/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;

import eu.etaxonomy.cdm.common.XmlHelp;

/**
 * Cdm Source class which represents a persisted CDM source object
 * as configured in the CDM sources config file.
 *
 * @author cmathew
 *
 */
public class CdmPersistentXMLSource {
	private static final Logger logger = Logger.getLogger(CdmPersistentXMLSource.class);


	/**
	 * CDM sources config file name
	 */
	public final static String CDMSOURCE_FILE_NAME = "cdm.datasources.xml";

	/**
	 * Directory path of the template CDM sources config file - this file is used
	 * in the case of the very first call to persist sources see {@link eu.etaxonomy.cdm.api.application.CdmApplicationUtils#getWritableResourceDir()}
	 */
	public final static String CDMSOURCE_PATH = "/eu/etaxonomy/cdm/";

	/**
	 * jDom Element represeting the CDM source as stored in the source config file
	 */
	private Element bean;


	/**
	 * Post fix represeting the type of source (data / remote)
	 */
	private String postFix;

	/**
	 * Enumeration containg all possible properties of all types of
	 * CDM Sources
	 *
	 *
	 */
	public enum CdmSourceProperties {
		URL,
		SERVER,
		PORT,
		FILEPATH,
		CONTEXTPATH,
		USERNAME,
		PASSWORD,
		DRIVER_CLASS,
		DATABASE,
		MODE;

		@Override
		public String toString(){
			switch (this){
				case URL:
					return "url";
				case SERVER:
					return "server";
				case PORT:
					return "port";
				case FILEPATH:
					return "filePath";
				case CONTEXTPATH:
					return "contextPath";
				case USERNAME:
					return "username";
				case PASSWORD:
					return "password";
				case DRIVER_CLASS:
					return "driverClassName";
				case DATABASE:
					return "database";
				case MODE:
					return "mode";
				default:
					throw new IllegalArgumentException( "Unknown enumeration type" );
			}
		}
	}

	private String cdmSourceName;

	/**
	 * Constructor which uses the given CDM source name and post fix to initialze the
	 * jDom element from the source config file.
	 *
	 * @param cdmSourceName
	 * @param postFix
	 */
	private CdmPersistentXMLSource(String cdmSourceName, String postFix) {
		this.cdmSourceName = cdmSourceName;
		this.postFix = postFix;
		bean = CdmPersistentSourceUtils.getCdmSourceBeanXml(cdmSourceName, postFix);
	}

	/**
	 * Constructor which uses the given CDM source name and post fix to initialze the
	 * jDom element from the source config file.
	 *
	 * @param strDataSourceName
	 * @param postFix
	 * @return new CdmPersistentXMLSource object
	 */
	public final static CdmPersistentXMLSource NewInstance(String strDataSourceName, String postFix) {
		return new CdmPersistentXMLSource(strDataSourceName, postFix);
	}


	/**
	 * Returns the loaded jDom element representing this object
	 *
	 * @return
	 */
	public Element getElement() {
		return bean;
	}

	/**
	 * Returns the name of the bean Element in the cdm source xml config file.
	 * @return bean name
	 */
	public String getBeanName(){
		return CdmPersistentSourceUtils.getBeanName(cdmSourceName, postFix);
	}

	/**
	 * Returns the list of attributes of this CDM source
	 * that are defined in the cdm source xml config file.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Attribute> getCdmSourceAttributes(){
		List<Attribute> result = new ArrayList<Attribute>();
		if (bean == null){
			return null;
		}else{
			result = bean.getAttributes();
		}
		return result;
	}

	/**
	 * Returns a defined property of the cdm source xml config file.
	 * @return the property of the cdm source or null if the datasource bean or the property does not exist.
	 */
	public String getCdmSourceProperty(String property){

		if (bean == null){
			return null;
		}else{
			Element elProperty = XmlHelp.getFirstAttributedChild(bean, "property", "name", property);
			if (elProperty == null){
				logger.warn("Unknown property: " + property);
		    	return null;
			}else{
				String strValue = elProperty.getAttributeValue("value");
				return strValue;
			}
		}
	}

	/**
	 * Returns the list of properties that are defined in the
	 * cdm source xml config file.
	 *
	 * @return
	 */
	public Properties getCdmSourceProperties(){
		Properties result = new Properties();
		if (bean == null){
			return null;
		}else{
			List<Element> elProperties = XmlHelp.getAttributedChildList(bean, "property", "name");
			Iterator<Element> iterator = elProperties.iterator();
			while(iterator.hasNext()){
				Element next = iterator.next();
				String strName = next.getAttributeValue("name");
				String strValue = next.getAttributeValue("value");
				result.put(strName, strValue);
			}
		}
		return result;
	}

}
