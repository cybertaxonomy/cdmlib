package eu.etaxonomy.cdm.model.occurrence;

import java.util.UUID;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivationEventType")
@XmlRootElement(name = "DerivationEventType")
@Entity
public class DerivationEventType extends DefinedTermBase {

	private static final UUID uuidDuplicate = UUID.fromString("67d2f161-ea1a-4a0c-8142-5f688b82cfbe");
	private static final UUID uuidGateringInSitu = UUID.fromString("4287d10e-6ef0-47c1-aece-f2de040895d9");
	private static final UUID uuidTissueSampling = UUID.fromString("06f3cbb2-87c6-4487-8926-3f901ede1689");
	private static final UUID uuidDnaExtraction = UUID.fromString("16493ed9-b953-4e2e-98ec-140326080b2f");
	
	
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
