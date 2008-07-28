/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

/**
 * @author a.mueller
 *
 */
public class CdmStringMapper extends CdmIOMapperBase {

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmStringMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}
	
	public Class getTypeClass(){
		return String.class;
	}

}
