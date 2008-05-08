/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

/**
 * @author a.mueller
 *
 */
public class CdmIOMapperBase {

	private Object dbValue;
	private Object cdmValue;

	protected CdmIOMapperBase(String dbAttributString, String cdmAttributeString){
		this.dbValue = dbAttributString;
		this.cdmValue = cdmAttributeString;
	}
	


}
