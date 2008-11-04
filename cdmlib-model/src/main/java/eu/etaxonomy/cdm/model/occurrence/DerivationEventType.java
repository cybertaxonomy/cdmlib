package eu.etaxonomy.cdm.model.occurrence;

import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivationEventType")
@XmlRootElement(name = "DerivationEventType")
@Entity
public class DerivationEventType extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(DerivationEventType.class);

	private static final UUID uuidDuplicate = UUID.fromString("8f54c7cc-eb5e-4652-a6e4-3a4ba429b327");
	private static final UUID uuidGateringInSitu = UUID.fromString("1cb2bd40-5c9c-459b-89c7-4d9c2fca7432");
	private static final UUID uuidTissueSampling = UUID.fromString("9dc1df08-1f31-4008-a4e2-1ddf7c9115da");
	private static final UUID uuidDnaExtraction = UUID.fromString("f9f957b6-88c0-4531-9a7f-b5fb1c9daf66");
	private static final UUID uuidVegetativPropagation = UUID.fromString("a4a8e4ce-0e58-462a-be67-a7f567d96da1");
	private static final UUID uuidDuplicateSegregation = UUID.fromString("661e7292-6bcb-495d-a3cc-140024ae3471");
	private static final UUID uuidAccessioning = UUID.fromString("3c7c0929-0528-493e-9e5f-15e0d9585fa1");
	private static final UUID uuidSexualReproduction = UUID.fromString("aa79baac-165d-47ad-9e80-52a03776d8ae");

	
	
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
	public static DerivationEventType NewInstance(String term, String label, String labelAbbrev){
		return new DerivationEventType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public DerivationEventType() {
		super();
	}

	
	
	/**
	 * Constructor
	 */
	public DerivationEventType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	

	public static final DerivationEventType getByUuid(UUID uuid){
		return (DerivationEventType)findByUuid(uuid);
	}

	public static final DerivationEventType DUPLICATE(){
		return (DerivationEventType)getByUuid(uuidDuplicate);
	}
	public static final DerivationEventType GATHERING_IN_SITU(){
		return (DerivationEventType)getByUuid(uuidGateringInSitu);
	}
	public static final DerivationEventType TISSUE_SAMPLING(){
		return (DerivationEventType)getByUuid(uuidTissueSampling);
	}
	public static final DerivationEventType DNA_EXTRACTION(){
		return (DerivationEventType)getByUuid(uuidDnaExtraction);
	}
	public static final DerivationEventType VEGETATIVE_PROPAGATION(){
		return (DerivationEventType)getByUuid(uuidVegetativPropagation);
	}
	public static final DerivationEventType DUPLICATE_SEGREGATEION(){
		return (DerivationEventType)getByUuid(uuidDuplicateSegregation);
	}
	public static final DerivationEventType ACCESSIONING(){
		return (DerivationEventType)getByUuid(uuidAccessioning);
	}
	public static final DerivationEventType SEXUAL_REPRODUCTION(){
		return (DerivationEventType)getByUuid(uuidSexualReproduction);
	}


}
