/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * The rank class defines the category of ranks a certain rank belongs to. This information is
 * usually needed for correct formatting of taxon name text representations by using e.g. 
 * {@link Rank#isSupraGeneric()} . Prior to v3.3 this was computed by comparison of ranks.
 * The current solution makes such methods less dependend on term loading.<BR>
 * @see http://dev.e-taxonomy.eu/trac/ticket/3521
 * 
 * @author a.mueller
 * @created 11.06.2013
 */
@XmlEnum
public enum RankClass implements IEnumTerm<RankClass>, Serializable{
	
	//0
	/**
	 * Unknown rank class is to be used if no information is available about the rank class.
	 * In the current model this type should never be used. However, it is a placeholder in case
	 * we find an appropriate usage in future and in case one needs a short term dummy.
	 */
	@XmlEnumValue("Unknown")
	@Deprecated Unknown(UUID.fromString("8c99ba63-2904-4dbb-87cb-3d3d7467e95d"), "Unknown rank class","UN", null),

	//1
	/**
	 * Class of ranks higher than {@link Rank#GENUS()}
	 */
	@XmlEnumValue("Suprageneric")
	Suprageneric(UUID.fromString("439a7897-9e0d-4560-b238-459d827f8a70"), "Suprageneric", "SG", null),
	
	//2
	/**
	 * Class of ranks equal to {@link Rank#GENUS()}. It is expected that there is only 1 such rank.
	 */
	@XmlEnumValue("Genus")
	Genus(UUID.fromString("86de25dc-3594-462f-a716-6d008caf2662"), "Genus", "GE", null),

	//3
	/**
	 * Class of ranks below {@link Rank#GENUS()} and above {@link Rank#SPECIES()}.
	 * This class includes {@link #SpeciesGroup species groups}
	 */
	@XmlEnumValue("Infrageneric")
	Infrageneric(UUID.fromString("37d5b535-3bf9-4749-af66-1a1c089dc0ae"), "Rank", "IG", null),	
	
	//4
	/**
	 * Class of ranks directly above {@link Rank#SPECIES()} which are used to group certain species
	 * e.g. for better usability.
	 * This class is part of the {@link #Infrageneric} class but different nomenclatural rules are applied.
	 */
	@XmlEnumValue("SpeciesGroup")
	SpeciesGroup(UUID.fromString("702edcb7-ee53-45b7-8635-efcbbfd69bca"), "Species group or aggr.", "AG", Infrageneric),
	
	//5
	/**
	 * Class of ranks equal to {@link Rank#SPECIES()}. It is expected that there is only 1 such rank.
	 */
	@XmlEnumValue("Species")
	Species(UUID.fromString("74cc173b-788e-4b01-9d70-a988498458b7"), "Species", "SP", null),
	
	//6
	/**
	 * Class of ranks lower than {@link Rank#SPECIES()}
	 */
	@XmlEnumValue("Infraspecific")
	Infraspecific(UUID.fromString("25915b4c-7f07-442f-bdaa-9d0223f6be42"), "Infraspecific", "IS", null),

	;
	
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RankClass.class);


	
	private RankClass(UUID uuid, String defaultString, String key, RankClass parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}
	
// *************************** DELEGATE **************************************/	

	private static EnumeratedTermVoc<RankClass> delegateVoc;
	private IEnumTerm<RankClass> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(RankClass.class);
	}
	
	@Override
	public String getKey(){return delegateVocTerm.getKey();}
	
	@Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	@Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}
		
	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public RankClass getKindOf() {return delegateVocTerm.getKindOf();}
	
	@Override
    public Set<RankClass> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(RankClass ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<RankClass> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


	
	public static RankClass getByKey(String key){return delegateVoc.getByKey(key);}
    public static RankClass getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


}
