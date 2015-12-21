/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author m.doering
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivationEventType")
@XmlRootElement(name = "DerivationEventType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class DerivationEventType extends DefinedTermBase<DerivationEventType> {
	private static final long serialVersionUID = 6895093454763415279L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivationEventType.class);

	private static final UUID uuidAccessioning = UUID.fromString("3c7c0929-0528-493e-9e5f-15e0d9585fa1");
	private static final UUID uuidDnaExtraction = UUID.fromString("f9f957b6-88c0-4531-9a7f-b5fb1c9daf66");
	private static final UUID uuidDuplicate = UUID.fromString("8f54c7cc-eb5e-4652-a6e4-3a4ba429b327");
	private static final UUID uuidDuplicateSegregation = UUID.fromString("661e7292-6bcb-495d-a3cc-140024ae3471");
	private static final UUID uuidGatheringInSitu = UUID.fromString("1cb2bd40-5c9c-459b-89c7-4d9c2fca7432");
	private static final UUID uuidGrouping = UUID.fromString("f7fbfbbb-86c6-4a1f-afb8-028becd2987f");
	private static final UUID uuidIndividualSelection = UUID.fromString("90a1062f-fa5b-4971-9990-4382e4ff70ba");
	private static final UUID uuidIndividualCultivation = UUID.fromString("f8febad5-4b4d-40fa-80e0-d1f82d4f510c");
	private static final UUID uuidTissueSampling = UUID.fromString("9dc1df08-1f31-4008-a4e2-1ddf7c9115da");
	private static final UUID uuidVegetativPropagation = UUID.fromString("a4a8e4ce-0e58-462a-be67-a7f567d96da1");
	private static final UUID uuidSexualReproduction = UUID.fromString("aa79baac-165d-47ad-9e80-52a03776d8ae");
	private static final UUID uuidPreparation = UUID.fromString("c868c472-4b60-4299-920a-a5698e4c26f4");
	private static final UUID uuidCultivationExSitu = UUID.fromString("39d6d69c-17aa-4ece-a137-5ac47ed5d737");

	protected static Map<UUID, DerivationEventType> termMap = null;



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

//********************************** Constructor *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected DerivationEventType() {
		super(TermType.DerivationEventType);
	}


	private DerivationEventType(String term, String label, String labelAbbrev) {
		super(TermType.DerivationEventType, term, label, labelAbbrev);
	}


//************************** METHODS ********************************


	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static DerivationEventType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(DerivationEventType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * TODO distinguish from {@link #DUPLICATE_SEGREGATEION()}
	 * @return
	 */
	public static final DerivationEventType DUPLICATE(){
		return getTermByUuid(uuidDuplicate);
	}
	public static final DerivationEventType GATHERING_IN_SITU(){
		return getTermByUuid(uuidGatheringInSitu);
	}
	public static final DerivationEventType TISSUE_SAMPLING(){
		return getTermByUuid(uuidTissueSampling);
	}
	public static final DerivationEventType DNA_EXTRACTION(){
		return getTermByUuid(uuidDnaExtraction);
	}
	public static final DerivationEventType VEGETATIVE_PROPAGATION(){
		return getTermByUuid(uuidVegetativPropagation);
	}
	/**
	 * TODO distinguish from {@link #DUPLICATE()}
	 * @return
	 */
	public static final DerivationEventType DUPLICATE_SEGREGATEION(){
		return getTermByUuid(uuidDuplicateSegregation);
	}
	/**
	 * The accessioning in a collection which usually results in an accession number.
	 * @return
	 */
	public static final DerivationEventType ACCESSIONING(){
		return getTermByUuid(uuidAccessioning);
	}
	public static final DerivationEventType SEXUAL_REPRODUCTION(){
		return getTermByUuid(uuidSexualReproduction);
	}
	/**
	 * Event which groups units (specimen, observations, samples).<BR>
	 * This may be used if the deriving
	 * unit is a composition of multiple single units (e.g. a specimen is based on mulitple
	 * herbaria sheets, which may be the case according to the {@link NomenclaturalCode#ICNAFP}).<BR>
	 * Another important usage is the grouping of specimen to create a common {@link DescriptionBase description} of this specimen group.
	 * This is a standard part of the scientific workflow when one works on a certain taxonomic group and examines
	 * the specimen.<BR>
	 * Also it may be used if a larger specimen (e.g. a dinosaur skeleton) is composed of multiple {@link FieldUnit field units}.
	 * @return
	 */
	public static final DerivationEventType GROUPING(){
		return getTermByUuid(uuidGrouping);
	}
	/**
	 * The extraction of 1 individual out of a sample including many individuals not necessarily all representing the
	 * same taxon. Example: individualization of 1 algae from of a water sample for further reasearch and determination)
	 *
	 * The extraction of 1 individual/organism out of any kind of sample (usually mixed samples with >1 taxa).
	 * The resulting individual may be used for a diversity of purposes like pure identification,
	 * image creation, further derivation, property assignment, ...)
	 * @return The indidivualization event
	 */
	public static final DerivationEventType INDIVIDUAL_SELECTION(){
		return getTermByUuid(uuidIndividualSelection);
	}
	/**
	 * Extraction of 1 individual out of a sample and cultivation of this individual
	 * (usually resulting in sample of many similar individuals).
	 * The individual cultivation event includes an {@link #INDIVIDUAL_SELECTION()} event
	 * @return The individual cultivation event
	 */
	public static final DerivationEventType INDIVIDUAL_CULTIVATION(){
		return getTermByUuid(uuidIndividualCultivation);
	}
	/**
	 * The preparation of e.g. a living culture into a preparation which can be stored in a collection
	 * (using certain preparation methods)
	 * @return
	 */
	public static final DerivationEventType PREPARATION(){
		return getTermByUuid(uuidPreparation);
	}
	/**
	 * Ex-situ cultivation.
	 * @return
	 */
	public static final DerivationEventType CULTIVATION_EX_SITU(){
		return getTermByUuid(uuidCultivationExSitu);
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<DerivationEventType> termVocabulary) {
		termMap = new HashMap<UUID, DerivationEventType>();
		for (DerivationEventType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
