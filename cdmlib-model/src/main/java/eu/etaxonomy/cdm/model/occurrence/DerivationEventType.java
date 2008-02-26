package eu.etaxonomy.cdm.model.occurrence;

import java.util.UUID;

import javax.persistence.Entity;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;

@Entity
public class DerivationEventType extends DefinedTermBase {

	private static final String strUnknownUuid = "00000000-0000-0000-0000-000000000000";
	
	private static final UUID uuidDuplicate = UUID.fromString(strUnknownUuid);
	private static final UUID uuidGateringInSitu = UUID.fromString(strUnknownUuid);
	private static final UUID uuidTissueSampling = UUID.fromString(strUnknownUuid);
	private static final UUID uuidDnaExtraction = UUID.fromString(strUnknownUuid);
	
	
	public DerivationEventType() {
		// TODO Auto-generated constructor stub
	}

	public DerivationEventType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
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
