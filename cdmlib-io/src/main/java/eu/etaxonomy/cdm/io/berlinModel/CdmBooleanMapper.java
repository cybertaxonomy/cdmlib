/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

/**
 * @author a.mueller
 *
 */
public class CdmBooleanMapper extends CdmIOMapperBase {

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	public CdmBooleanMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}

	public Class getTypeClass(){
		return Boolean.class;
	}
}
