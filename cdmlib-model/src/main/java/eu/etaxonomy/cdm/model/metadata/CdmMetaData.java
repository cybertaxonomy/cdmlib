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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author a.mueller
 * @since 07.09.2009
 */
@Entity
public class CdmMetaData extends CdmBase{

    private static final long serialVersionUID = -3033376680593279078L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

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
	private static final String dbSchemaVersion = CdmVersion.V_05_46_00.versionString;

	public enum CdmVersion {
	    V_05_12_00("5.12.0.0.20191202"),
        V_05_15_00("5.15.0.0.20200510"),
	    V_05_15_01("5.15.1.0.20200610"),
	    V_05_15_02("5.15.2.0.20200611"),
	    V_05_18_00("5.18.0.0.20200902"),
        V_05_18_01("5.18.1.0.20200914"),
        V_05_18_02("5.18.2.0.20200921"),
        V_05_18_03("5.18.3.0.20200924"),
        V_05_18_04("5.18.4.0.20201020"),
        V_05_18_05("5.18.5.0.20201103"),
        V_05_18_06("5.18.6.0.20201124"),
        V_05_22_00("5.22.0.0.20210315"),
        V_05_23_00("5.23.0.0.20210422"),
        V_05_25_00("5.25.0.0.20210609"),
        V_05_25_01("5.25.1.0.20210702"),
        V_05_27_00("5.27.0.0.20210913"),
        V_05_27_01("5.27.1.0.20210922"),
        V_05_29_00("5.29.0.0.20211122"),
        V_05_32_00("5.32.0.0.20220807"),
        V_05_33_00("5.33.0.0.20220807"),
        V_05_35_00("5.35.0.0.20221202"),
        V_05_35_01("5.35.1.0.20221218"),
        V_05_36_00("5.36.0.0.20230106"),
        V_05_36_01("5.36.1.0.20230323"),
        V_05_38_00("5.38.0.0.20230510"),
        V_05_40_00("5.40.0.0.20230627"),
        V_05_40_01("5.40.1.0.20230829"),
        V_05_43_00("5.43.0.0.20240531"),
        V_05_43_01("5.43.1.0.20240605"),
        V_05_44_00("5.44.0.0.20240704"),
        V_05_46_00("5.46.0.0.20241009")
        ;

        private String versionString;
	    private CdmVersion(String versionString){
	        this.versionString = versionString;
	    }
	    public String versionString(){
	        return versionString;
	    }
	}

    private static final String UNNAMED = "- UNNAMED -";

	/**
     * The {@link TermType type} of this term. Needs to be the same type in a {@link DefinedTermBase defined term}
     * and in it's {@link TermVocabulary vocabulary}.
     */
    @XmlAttribute(name ="PropertyName")
    @Column(name="propertyName", length=20)
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName")}
    )
	private CdmMetaDataPropertyName propertyName;
	private String value;


//********************* Constructor *********************************************/

	/**
	 * Simple constructor to be used by Spring
	 */
	protected CdmMetaData(){
	}

	public CdmMetaData(CdmMetaDataPropertyName propertyName, String value) {
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

	public CdmMetaDataPropertyName getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(CdmMetaDataPropertyName propertyName) {
		this.propertyName = propertyName;
	}

	public String getValue() {
		return value;
	}
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
