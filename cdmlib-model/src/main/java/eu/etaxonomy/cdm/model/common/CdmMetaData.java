// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.model.common.CdmBase;

import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author a.mueller
 * @created 07.09.2009
 * @version 1.0
 */
@Entity
public class CdmMetaData extends CdmBase{
	private static final long serialVersionUID = -3033376680593279078L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmMetaData.class);

	/* It is a little bit confusing that this specific information is located in
	 * a generic class for metadata. Think about moving the schema version 
	 *  
	 */
	/**
	 * The database schema version number.
	 * It is recommended to have the first two numbers equal to the CDM Library version number.
	 * But it is not obligatory as there may be cases when the library number changes but the
	 * schema version is not changing.
	 * The third should be incremented if the schema changes in a way that SCHEMA_VALIDATION.UPDATE
	 * will probably not work or will not be enough to transform old data into new data.
	 * The fourth number shoud be incremented when minor schema changes take place that can
	 * be handled by SCHEMA_VALIDATION.UPDATE
	 * The last number represents the date of change.
	 */
	private static final String dbSchemaVersion = "2.1.2.2.200909301715";
	

	/**
	 * @return a list of default metadata objects 
	 */
	public static final List<CdmMetaData> defaultMetaData(){
		List<CdmMetaData> result = new ArrayList<CdmMetaData>();
		// schema version
		result.add(new CdmMetaData(MetaDataPropertyName.DB_SCHEMA_VERSION, dbSchemaVersion));
		// database create time
		result.add(new CdmMetaData(MetaDataPropertyName.DB_CREATE_DATE, new DateTime().toString()));
		return result;	
	}
	/**
	 * The version number for the terms loaded by the termloader (csv-files)
	 * It is recommended to have the first two numbers equal to the CDM Library version number.
	 * But it is not obligatory as there may be cases when the library number changes but the
	 * schema version is not changing.
	 * The third should be incremented if the terms change in a way that is not compatible
	 * to the previous version (e.g. by changing the type of a term)
	 * The fourth number shoud be incremented when compatible term changes take place
	 * (e.g. when new terms were added)
	 * The last number represents the date of change.
	 */
	private static final String termsVersion = "2.1.2.3.201003091500";
	
	
	public enum MetaDataPropertyName{
		DB_SCHEMA_VERSION,
		TERMS_VERSION,
 		DB_CREATE_DATE,
		DB_CREATE_NOTE
	}
	
	/* END OF CONFUSION */
	private MetaDataPropertyName propertyName;
	private String value;

	
	/**
	 * Method to retrieve a CDM Libraries meta data
	 * @return
	 */
	public static final List<CdmMetaData> propertyList(){
		List<CdmMetaData> result = new ArrayList<CdmMetaData>();
		result.add(new CdmMetaData(MetaDataPropertyName.DB_SCHEMA_VERSION, dbSchemaVersion));
		result.add(new CdmMetaData(MetaDataPropertyName.TERMS_VERSION, termsVersion));
		result.add(new CdmMetaData(MetaDataPropertyName.DB_CREATE_DATE, new DateTime().toString()));
		return result;
	}

//********************* Constructor *********************************************/	

	/**
	 * Simple constructor to be used by Spring
	 */
	protected CdmMetaData(){
		super();
	}

	public CdmMetaData(MetaDataPropertyName propertyName, String value) {
		super();
		this.propertyName = propertyName;
		this.value = value;
	}

//****************** instance methods ****************************************/	
	
	/**
	 * @return the propertyName
	 */
	public MetaDataPropertyName getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(MetaDataPropertyName propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
//************************ STATIC SCHEMA VERSION METHODS ************************/
	
	public static String getCurrentSchemaVersion() {
		return dbSchemaVersion;
	}

	/**
	 * Gets the first i parts of the current CdmLibrary schema version.
	 * @param allCommonData
	 * @return current schema version.
	 */
	public static String getCurrentSchemaVersion(int i) {
		// Get current schema version
		String schemaVersion = CdmMetaData.getCurrentSchemaVersion();
		return getVersion(schemaVersion, i);
	}

	/**
	 * Gets the first i parts of the passed database schema version.
	 * @param allCommonData
	 * @return database schema version.
	 */
	public static String getDatabaseSchemaVersion(Map<MetaDataPropertyName, CdmMetaData> cdmMetaDataFromDatabase, int i) {
		// Get database schema version
		String schemaVersion = cdmMetaDataFromDatabase.get(MetaDataPropertyName.DB_SCHEMA_VERSION).getValue();
		return getVersion(schemaVersion, i);
	}
	
//************************ STATIC TERMS VERSION METHODS ************************/
	public static String getCurrentTermsVersion() {
		return dbSchemaVersion;
	}

	/**
	 * Gets the first i parts of the current CdmLibrary terms version.
	 * @param allCommonData
	 * @return current schema version.
	 */
	public static String getCurrentTermsVersion(int i) {
		// Get current schema version
		String schemaVersion = CdmMetaData.getCurrentTermsVersion();
		return getVersion(schemaVersion, i);
	}

	/**
	 * Gets the first i parts of the passed database schema version.
	 * @param allCommonData
	 * @return database schema version.
	 */
	public static String getDatabaseTermsVersion(Map<MetaDataPropertyName, CdmMetaData> cdmMetaDataFromDatabase, int i) {
		// Get database schema version
		String termsVersion = cdmMetaDataFromDatabase.get(MetaDataPropertyName.TERMS_VERSION).getValue();
		return getVersion(termsVersion, i);
	}
	
	
//************************ helping methods ************************/

	/**
	 * @param versionProperty
	 * @return Version number as string.
	 */
	private static String getVersion(String versionProperty, int i) {
		return versionProperty.substring(0, nthIndexOf(versionProperty, ".", i));
	}

	/**
	 * Calculates the n-th occurrence of a string.
	 * @param versionProperty
	 * @return Index of N-th occurence of a string.
	 */
	private static int nthIndexOf(String versionProperty, String pattern, int n) {
		int currentIndex = -1;
		for (int i=0; i<n; i++) {
			currentIndex = versionProperty.indexOf(pattern, currentIndex + 1);
		}
		return currentIndex;
	}

	
	
}
