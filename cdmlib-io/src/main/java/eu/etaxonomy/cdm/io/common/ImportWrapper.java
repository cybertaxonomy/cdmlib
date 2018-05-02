/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;

/**
 * @author a.mueller
 * @since 12.11.2008
 * @version 1.0
 */
public class ImportWrapper {
	private static final Logger logger = Logger.getLogger(ImportWrapper.class);
	
	private String label;
	
	private Class<? extends ICdmImporter> importClass = CdmApplicationAwareDefaultImport.class;
	
	private IImportConfigurator configuration;
	
	public static List<ImportWrapper> list(){
		logger.debug("Create ImportWrapper list");
		List<ImportWrapper> result = new ArrayList<ImportWrapper>();
		//abcd
		ImportWrapper wrapper = Abcd206WrapperInstance();
		result.add(wrapper);
		wrapper = TcsXml101WrapperInstance();
		result.add(wrapper);
		wrapper = Jaxb10WrapperInstance();
		result.add(wrapper);
		return result;
	}
	
	public static ImportWrapper Abcd206WrapperInstance(){
		String label = "ABCD 2.06";
		Class<? extends ICdmImporter> clazz = CdmApplicationAwareDefaultImport.class;
		IImportConfigurator config = Abcd206ImportConfigurator.NewInstance(null, null);
		ImportWrapper wrapper = new ImportWrapper();
		wrapper.setLabel(label);
		wrapper.setImportClass(clazz);
		wrapper.setConfiguration(config);
		return wrapper;
	}

	
	public static ImportWrapper TcsXml101WrapperInstance(){
		String label = "TcsXML 1.1";
		Class<? extends ICdmImporter> clazz = CdmApplicationAwareDefaultImport.class;
		IImportConfigurator config = TcsXmlImportConfigurator.NewInstance(null, null);
		ImportWrapper wrapper = new ImportWrapper();
		wrapper.setLabel(label);
		wrapper.setImportClass(clazz);
		wrapper.setConfiguration(config);
		return wrapper;
	}
	
	
	public static ImportWrapper Jaxb10WrapperInstance(){
		String label = "CDM 1.0 XML";
		Class<? extends ICdmImporter> clazz = CdmApplicationAwareDefaultImport.class;
		IImportConfigurator config = JaxbImportConfigurator.NewInstance(null, null);
		ImportWrapper wrapper = new ImportWrapper();
		wrapper.setLabel(label);
		wrapper.setImportClass(clazz);
		wrapper.setConfiguration(config);
		return wrapper;
	}

	/**
	 * 
	 * @param source
	 * @param destination
	 * @param secUUID
	 * @return
	 */
	public boolean invoke(Object source, ICdmDataSource destination, UUID secUUID){
		try {
			Method methodInvoke = importClass.getDeclaredMethod("invoke", IImportConfigurator.class);
			Method methodSetSource = configuration.getClass().getMethod("setSource", String.class);
			methodSetSource.setAccessible(true);
			
			this.configuration.setDestination(destination);
			methodSetSource.invoke(configuration, source);
			
			if (this.importClass == null){
				return false;
			}else{
				return (Boolean)methodInvoke.invoke(importClass.newInstance(), configuration);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	
// ***************  GETTER / SETTER *******************************************/	
	
	/**
	 * @return the configuration
	 */
	public IImportConfigurator getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(IImportConfigurator configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the ioClazz
	 */
	public Class<? extends ICdmImporter> getImportClass() {
		return importClass;
	}

	/**
	 * @param ioClazz the ioClazz to set
	 */
	public void setImportClass(Class<? extends ICdmImporter> importClass) {
		this.importClass = importClass;
	}
	
//******* TEST **************************************//
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ImportWrapper.list().get(1).invoke("", null, UUID.randomUUID());
	}
	
}
