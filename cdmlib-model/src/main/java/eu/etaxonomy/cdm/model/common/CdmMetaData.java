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

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 07.09.2009
 * @version 1.0
 */
@Entity
public class CdmMetaData extends CdmBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmMetaData.class);

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
	private static final String dbSchemaVersion = "2.1.0.0.200909071123";
	
	public enum MetaDataPropertyName{
		DB_SCHEMA_VERSION
	}
	
	private MetaDataPropertyName propertyName;
	private String value;
	
	
	public static final List<CdmMetaData> propertyList(){
		List<CdmMetaData> result = new ArrayList<CdmMetaData>();
		result.add(new CdmMetaData(MetaDataPropertyName.DB_SCHEMA_VERSION, dbSchemaVersion));
		return result;
	}
	
	private CdmMetaData() {
		super();
	}
	
	public CdmMetaData(MetaDataPropertyName propertyName, String value) {
		super();
		this.propertyName = propertyName;
		this.value = value;
	}

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

	
	
}
