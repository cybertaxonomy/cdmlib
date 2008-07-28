/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

/**
 * @author a.mueller
 *
 */
public abstract class CdmIOMapperBase {

	private String dbValue;
	private String cdmValue;

	protected CdmIOMapperBase(String dbAttributString, String cdmAttributeString){
		this.dbValue = dbAttributString;
		this.cdmValue = cdmAttributeString;
	}
	
	public String getSourceAttribute(){
		return dbValue;
	}

	public String getDestinationAttribute(){
		return cdmValue;
	}
	
	public abstract Class getTypeClass();
}
