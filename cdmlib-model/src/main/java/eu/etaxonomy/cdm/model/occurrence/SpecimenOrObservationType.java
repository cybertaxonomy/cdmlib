// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IDefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Media;


/**
 * The specimen or observation type is used to define what a {@link SpecimenOrObservationBase specimen or observation} 
 * instance describes, may it be a {@link FieldUnit field unit} or a 
 * {@link DerivedUnit derived unit}.<BR>
 * The vocabulary used derives mainly from the according vocabularies in ABCD, Darwin Core as well
 * as from the former (before v3.3) CDM subclasses of {@link SpecimenOrObservationBase}.
 * <BR><BR>
 * In future versions this enum may also be expressed as a term, not an enum. For now the general policy is to 
 * transform former classes to general classes with enums as their types.
 * 
 * @author a.mueller
 * @created 27.06.2013
 */
@XmlEnum
public enum SpecimenOrObservationType implements IDefinedTerm<SpecimenOrObservationType>, Serializable{
	
	//0
	/**
	 * Unknown specimen or observation the type to be used if no information is available about the type.
	 * This type should be avoided if possible. In terms of doubtful occurrences one may consider to
	 * use "DerivedUnit", "Observation" or "OtherSpecimen" instead.
	 */
	@XmlEnumValue("Unknown")
	Unknown(UUID.fromString("971a0c72-d4d2-4e41-8520-c9a87df34f48"), "Unknown term type","UN"),

	//1
	/**
	 * A physical object representing one or more organisms, part of organism, or artifact(s) of an organism. 
	 */
	@XmlEnumValue("PreservedSpecimen")
	PreservedSpecimen(UUID.fromString("95cd9246-4131-444f-ad2f-3b24ca294a1f"), "Preserved Specimen", "PS"),
	
	//2
	/**
	 * Specification of PreservedSpecimen, indicating that a specimen is a fossil or is derived 
	 * from a fossil.
	 */
	@XmlEnumValue("FossilSpecimen")
	Fossil(UUID.fromString("1b0f8534-35eb-4c64-8e53-69e734043bd6"), "Fossil Specimen", "FS"),

	//3
	/**
	 * An organism removed from its natural occurrence and now living in captivity or cultivation
	 */
	@XmlEnumValue("LivingSpecimen")
	LivingSpecimen(UUID.fromString("bc46169e-4d31-4eae-b5aa-1ddf0520c9a9"), "Living Specimen (ex situ)", "LS"),

	//4
	/**
	 * All specimen types not covered by existing types. 
	 *  
	 */
	//TODO does ABCD allow to use this also for Observations?
	@XmlEnumValue("OtherSpecimen")
	OtherSpecimen(UUID.fromString("b636da6a-b48f-4084-9594-25ea82429b70"), "Other Specimen", "OS"),
	
	//5
	/**
	 * Generalization for all specimen or observation except for FieldUnits. One should use DerivedUnit
	 * only if no further specification is available.
	 * Derived from the former (before v3.3.) CDM class "DerivedUnit".  
	 */
	@XmlEnumValue("DerivedUnit")
	DerivedUnit(UUID.fromString("da80443a-360b-4861-abeb-21e13beb5186"), "Derived Unit", "DU"),

	//6
	/**
	 * Generalization for all observations, may they be human observations or machine observations.
	 * Derived from the former (before v3.3) CDM class "Observation".
	 */
	@XmlEnumValue("Observation")
	Observation(UUID.fromString("a8a254f1-7bed-47ec-bbee-86a794819c3b"), "Observation", "OB"),

	//7
	/**
	 * Type for all observations made by a human.
	 * Specification of "Observation".
	 */
	@XmlEnumValue("HumanObservation")
	HumanObservation(UUID.fromString("b960c06d-4bfc-4bea-bc53-aec0600409b1"), "HumanObservation", "HO"),

	//8
	/**
	 * Type for all observations made by a machine.
	 * Specification of "Observation".
	 */
	@XmlEnumValue("MachineObservation")
	MachineObservation(UUID.fromString("b12a13fc-0f61-4055-b9b7-4eabd417c54c"), "MachineObservation", "MO"),

//	
//	//10
//	//TODO needed (isn't this also a Preserved Specimen)
//	@XmlEnumValue("DnaSample")
//	DnaSample(UUID.fromString("6a724560-bdfa-41c9-b459-ab0f1fc74902"), "Dna Sample", "DS"),

	//9
	/**
	 * Generalization for all types of media (StillImage, MovingImage, SoundRecording, MultiMedia).
	 * One should try to use a specification instead of using the general type Media.
	 */
	@XmlEnumValue("Media")
	Media(UUID.fromString("0efa6b3e-e67a-49d4-a758-f3fc688901a7"), "Media", "ME"),

	
	//10
	/**
	 * A photograph, drawing, painting or similar.
	 */
	@XmlEnumValue("StillImage")
	StillImage(UUID.fromString("a8d9ada5-7f22-4fcf-8693-ae68d527289b"), "Still Image", "SI"),

	//11
	/**
	 * A sequence of still images taken at regular intervals and intended to be played back as a moving image; 
	 * may include sound.
	 */
	@XmlEnumValue("MovingImage")
	MovingImage(UUID.fromString("56722418-9398-4367-afa1-46982fb93959"), "Moving Image", "MI"),

	//12
	/**
	 * An audio recording.
	 */
	@XmlEnumValue("SoundRecording")
	SoundRecording(UUID.fromString("2a39ec19-4aae-4b74-bc5c-578c5dc94e7d"), "Moving Image", "SR"),

	//13
	/**
	 * Any multi media object which is not covered by DrawingOrPhoto, MovingImage or SoundRecording.
	 */
	@XmlEnumValue("Multimedia")
	Multimedia(UUID.fromString("bfe3fef8-d294-4554-847a-c9d8a6b74313"), "Multimedia Object", "MM"),
	
	//14
	/**
	 * Type for all field data belonging to a particular object or observation or a set of these.
	 * Derived from the former (before v3.3 CDM class "FieldUnit".
	 */
	//TODO do we really need an own type for FieldUnit or is this covered by any of the other 
	//types (e.g. Observation)
	@XmlEnumValue("FieldUnit")
	FieldUnit(UUID.fromString("d38d22db-17f9-45ba-a32f-32393788726f"), "Field Unit", "FU"),	

	;
	
	
	private static final Logger logger = Logger.getLogger(SpecimenOrObservationType.class);

	private String readableString;
	private UUID uuid;
	private String key;
	private static final Map<String,SpecimenOrObservationType> lookup = new HashMap<String, SpecimenOrObservationType>();

	static {
		for (SpecimenOrObservationType t : SpecimenOrObservationType.values()){
			if (lookup.containsKey(t.key)){
				String message = "Key must be unique in %s but was not for %s";
				throw new RuntimeException(String.format(message, Unknown.getClass().getName(), t.key));
			}
			lookup.put(t.key, t);
		}
	}
	
	private SpecimenOrObservationType(UUID uuid, String defaultString, String key){
		this.uuid = uuid;
		readableString = defaultString;
		this.key = key;
	}

	public String getKey(){
		return key;
	}
	
	public static SpecimenOrObservationType byKey(String key){
		return lookup.get(key);
	}

	public boolean isFeatureObservation() {
		if (this == Observation || this == HumanObservation || this == MachineObservation 
			){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isFeatureSpecimen() {
		if (isPreservedSpecimen() || this == LivingSpecimen || this == SpecimenOrObservationType.OtherSpecimen
			){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @return true if this type represents a preserved specimen or one of its specifications.
	 */
	public boolean isPreservedSpecimen() {
		if (this == PreservedSpecimen || this == Fossil
			){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Any field unit. 
	 * @return
	 */
	public boolean isFieldUnit() {
		if (this == FieldUnit){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * All types except for field units.
	 * @return
	 */
	public boolean isAnyDerivedUnit() {
		return ! isFieldUnit();
	}
	
	/**
	 * Any Media, may it be images, movies, sound recordings or multi media objects
	 * @return
	 */
	public boolean isMedia() {
		if (this == Media || this == StillImage || this == MovingImage || this == Multimedia	|| this == SoundRecording		){
			return true;
		}else{
			return false;
		}
	}	

	
	
	@Transient
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
	

	/**
	 * This method was initially created in the former class
	 * "DerivedUnitFacade.DerivedUnitType" for some specimen imports .
	 * It may be extended in future. However, it may also be decoupled from
	 * SpecimenOrObservationType to serve different use-cases. 
	 * @param type
	 * @return
	 */
	public static SpecimenOrObservationType valueOf2(String type) {
		if (type == null) {
			return null;
		}
		type = type.replace(" ", "").toLowerCase();
		if (type.matches("(preserved)?specimen")) {
			return PreservedSpecimen;
		} else if (type.equals("living(being|specimen)")) {
			return LivingSpecimen;
		} else if (type.equals("observation")) {
			return Observation;
		} else if (type.equals("fossil")) {
			return Fossil;
		} else if (type.equals("field(observation|unit)")) {
			return FieldUnit;
		} else if (type.equals("unknown")) {
			return DerivedUnit;
		} else if (type.equals("derivedunit")) {
			return DerivedUnit;
		}
		return null;
	}
	

	@Override
	public SpecimenOrObservationType readCsvLine(Class<SpecimenOrObservationType> termClass,
			List<String> csvLine, Map<UUID, DefinedTermBase> terms) {
		String message = "readCsvLine not implemented";
		throw new RuntimeException(message);
	}	
	
	@Override
    public void writeCsvLine(CSVWriter writer, SpecimenOrObservationType term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public SpecimenOrObservationType getByUuid(UUID uuid) {
		for (SpecimenOrObservationType type : SpecimenOrObservationType.values()){
			if (type.getUuid().equals(uuid)){
				return type;
			}
		}
		return null;
	}


	@Override
    public SpecimenOrObservationType getKindOf() {
		return null;
	}


	@Override
    public Set<SpecimenOrObservationType> getGeneralizationOf() {
		return new HashSet<SpecimenOrObservationType>();
	}


	@Override
    public SpecimenOrObservationType getPartOf() {
		return null;
	}


	@Override
    public Set<SpecimenOrObservationType> getIncludes() {
		return new HashSet<SpecimenOrObservationType>();
	}


	@Override
    public Set<Media> getMedia() {
		return new HashSet<Media>();
	}
	
	@Override
	public String getIdInVocabulary() {
		return this.toString();
	}

	@Override
	public void setIdInVocabulary(String idInVocabulary) {
		//not applicable
	}




}
