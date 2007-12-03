package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;

@Entity
public class DerivationEventType extends DefinedTermBase {

	public DerivationEventType() {
		// TODO Auto-generated constructor stub
	}

	public DerivationEventType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}
	
	public static final DerivationEventType DUPLICATE(){
		return (DerivationEventType)dao.findByUuid("1234-8765-21341");
	}
	public static final DerivationEventType GATHERING_IN_SITU(){
		return (DerivationEventType)dao.findByUuid("1234-8765-21341");
	}
	public static final DerivationEventType TISSUE_SAMPLING(){
		return (DerivationEventType)dao.findByUuid("1234-8765-21341");
	}
	public static final DerivationEventType DNA_EXTRACTION(){
		return (DerivationEventType)dao.findByUuid("1234-8765-21341");
	}
}
