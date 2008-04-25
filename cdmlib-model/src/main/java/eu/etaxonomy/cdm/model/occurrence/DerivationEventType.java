package eu.etaxonomy.cdm.model.occurrence;

import java.util.UUID;
import javax.persistence.Entity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@Entity
public class DerivationEventType extends DefinedTermBase {

	private static final String strUnknownUuid = "00000000-0000-0000-0000-000000000000";
	
	private static final UUID uuidDuplicate = UUID.fromString(strUnknownUuid);
	private static final UUID uuidGateringInSitu = UUID.fromString(strUnknownUuid);
	private static final UUID uuidTissueSampling = UUID.fromString(strUnknownUuid);
	private static final UUID uuidDnaExtraction = UUID.fromString(strUnknownUuid);
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEventType NewInstance(){
		return new DerivationEventType();
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static DerivationEventType NewInstance(String term, String label){
		return new DerivationEventType(term, label);
	}
	
	/**
	 * Constructor
	 */
	protected DerivationEventType() {
		super();
	}

	/**
	 * Constructor
	 */
	protected DerivationEventType(String term, String label) {
		super(term, label);
	}
	
	public static final DerivationEventType DUPLICATE(){
		return (DerivationEventType)findByUuid(uuidDuplicate);
	}
	public static final DerivationEventType GATHERING_IN_SITU(){
		return (DerivationEventType)findByUuid(uuidGateringInSitu);
	}
	public static final DerivationEventType TISSUE_SAMPLING(){
		return (DerivationEventType)findByUuid(uuidTissueSampling);
	}
	public static final DerivationEventType DNA_EXTRACTION(){
		return (DerivationEventType)findByUuid(uuidDnaExtraction);
	}
}
