/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 * @since 07.09.2009
 */
@Entity
public class CdmMetaData extends CdmBase{

    private static final long serialVersionUID = -3033376680593279078L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmMetaData.class);

	private static final String UNNAMED = "- UNNAMED -";


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
	 * The fourth number should be incremented when minor schema changes take place that can
	 * be handled by SCHEMA_VALIDATION.UPDATE
	 * The last number represents the date of change.
	 */
//	private static final String dbSchemaVersion = "4.1.0.0.201607300000";
//  private static final String dbSchemaVersion = "4.7.0.0.201710040000";
     private static final String dbSchemaVersion = "4.9.0.0.20170710";


	/* END OF CONFUSION */

	   /**
     * The {@link TermType type} of this term. Needs to be the same type in a {@link DefinedTermBase defined term}
     * and in it's {@link TermVocabulary vocabulary}.
     */
    @XmlAttribute(name ="PropertyName")
    @Column(name="propertyName", length=20)
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName")}
    )
	private CdmMetaDataPropertyName propertyName;
	private String value;


//********************* Constructor *********************************************/

	/**
	 * Simple constructor to be used by Spring
	 */
	protected CdmMetaData(){
		super();
	}

	public CdmMetaData(CdmMetaDataPropertyName propertyName, String value) {
		super();
		this.propertyName = propertyName;
		this.value = value;
	}

// ******************** STATIC **********************************/


    /**
     * @return a list of default metadata objects
     */
    public static final List<CdmMetaData> defaultMetaData(){
        List<CdmMetaData> result = new ArrayList<>();
        // schema version
        result.add(new CdmMetaData(CdmMetaDataPropertyName.DB_SCHEMA_VERSION, dbSchemaVersion));
        // database create time
        result.add(new CdmMetaData(CdmMetaDataPropertyName.DB_CREATE_DATE, new DateTime().toString()));
        result.add(new CdmMetaData(CdmMetaDataPropertyName.INSTANCE_ID, UUID.randomUUID().toString()));
        result.add(new CdmMetaData(CdmMetaDataPropertyName.INSTANCE_NAME, UNNAMED));
        return result;
    }



//****************** instance methods ****************************/

	/**
	 * @return the propertyName
	 */
	public CdmMetaDataPropertyName getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(CdmMetaDataPropertyName propertyName) {
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

//******************** Version comparator **********************************/

	public static class VersionComparator implements Comparator<String>{
		Integer depth;
		IProgressMonitor monitor;

		public VersionComparator(Integer depth, IProgressMonitor monitor){
			this.depth = depth;
			this.monitor = monitor;
		}

		@Override
        public int compare(String version1, String version2) {
			int result = 0;

			if (version1.equals(version2)){
			    return 0;
			}

			String[] version1Split = version1.split("\\.");
			String[] version2Split = version2.split("\\.");

			if(version1Split.length == 1 || version2Split.length == 1){
				throwException("Tried to compare version but given Strings don't seem to " +
						"contain version numbers. version1: " + version1 + ", version2:" + version2);
			}

			if(depth != null && (version1Split.length < depth || version2Split.length < depth )){
				throwException("Desired depth can not be achieved with the given strings. depth: " + depth  + ", version1: " + version1 + ", version2:" + version2);
			}
			//use the shorter version to avoid arrayOutOfBoundsException, if version2Split.length < version1Split.length but > depth
			int length = (version1Split.length < version2Split.length) ? version1Split.length: version2Split.length;
			if (depth != null){
			    length = length<depth?length:depth;
			}
			for (int i = 0; i < length; i++){
				Long version1Part = Long.valueOf(version1Split[i]);
				Long version2Part = Long.valueOf(version2Split[i]);
				int partCompare = version1Part.compareTo(version2Part);
				if (partCompare != 0){
					return partCompare;
				}
			}
			return result;
		}

		private Throwable throwException(String message){
			RuntimeException exception =  new RuntimeException(message);
			if (monitor != null){
				monitor.warning(message, exception);
			}
			throw exception;
		}

	}

	/**
	 * Compares two version string. If version1 is higher than version2 a positive result is returned.
	 * If both are equal 0 is returned, otherwise -1 is returned.
	 * @see Comparator#compare(Object, Object)
	 * @param version1
	 * @param version2
	 * @param depth
	 * @param monitor
	 * @return
	 */
	public static int compareVersion(String version1, String version2, Integer depth, IProgressMonitor monitor){
		VersionComparator versionComparator = new VersionComparator(depth, monitor);
		return versionComparator.compare(version1, version2);
	}

	public static boolean isDbSchemaVersionCompatible(String version){
		return compareVersion(dbSchemaVersion, version, 3, null) == 0;
	}

	public static String getDbSchemaVersion() {
		return dbSchemaVersion;
	}

}
