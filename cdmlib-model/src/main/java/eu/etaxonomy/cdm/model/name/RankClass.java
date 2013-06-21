// $Id$
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
import eu.etaxonomy.cdm.model.media.Media;

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
public enum RankClass implements IDefinedTerm<RankClass>, Serializable{
	
	//0
	/**
	 * Unknown rank class is to be used if no information is available about the rank class.
	 * In the current model this type should never be used. However, it is a placeholder in case
	 * we find an appropriate usage in future and in case one needs a short term dummy.
	 */
	@XmlEnumValue("Unknown")
	@Deprecated Unknown(UUID.fromString("8c99ba63-2904-4dbb-87cb-3d3d7467e95d"), "Unknown rank class","UN"),

	//1
	@XmlEnumValue("Suprageneric")
	Suprageneric(UUID.fromString("439a7897-9e0d-4560-b238-459d827f8a70"), "Suprageneric", "SG"),
	
	//2
	@XmlEnumValue("Genus")
	Genus(UUID.fromString("86de25dc-3594-462f-a716-6d008caf2662"), "Genus", "GE"),

	//3
	@XmlEnumValue("Infrageneric")
	Infrageneric(UUID.fromString("37d5b535-3bf9-4749-af66-1a1c089dc0ae"), "Rank", "IG"),	
	
	//4
	@XmlEnumValue("SpeciesGroup")
	SpeciesGroup(UUID.fromString("702edcb7-ee53-45b7-8635-efcbbfd69bca"), "Species group or aggr.", "AG"),
	
	//5
	@XmlEnumValue("Species")
	Species(UUID.fromString("74cc173b-788e-4b01-9d70-a988498458b7"), "Species", "SP"),
	
	//6
	@XmlEnumValue("Infraspecific")
	Infraspecific(UUID.fromString("25915b4c-7f07-442f-bdaa-9d0223f6be42"), "Infraspecific", "IS"),

	;
	
	
	private static final Logger logger = Logger.getLogger(RankClass.class);

	private String readableString;
	private UUID uuid;
	private String key;
	private static final Map<String,RankClass> lookup = new HashMap<String, RankClass>();

	static {
		for (RankClass t : RankClass.values()){
			if (lookup.containsKey(t.key)){
				throw new RuntimeException("Key must be unique in rank class but was not for " + t.key);
			}
			lookup.put(t.key, t);
		}
	}
	
	private RankClass(UUID uuid, String defaultString, String key){
		this.uuid = uuid;
		readableString = defaultString;
		this.key = key;
	}

	public String getKey(){
		return key;
	}
	
	public static RankClass byKey(String key){
		return lookup.get(key);
	}
	
	
	@Transient
	public String getMessage(){
		return getMessage(eu.etaxonomy.cdm.model.common.Language.DEFAULT());
	}
	public String getMessage(eu.etaxonomy.cdm.model.common.Language language){
		//TODO make multi-lingual
		return readableString;
	}
	
	
	
	
	@Override
	public RankClass readCsvLine(Class<RankClass> termClass, List<String> csvLine, Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
    public void writeCsvLine(CSVWriter writer, RankClass term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public RankClass getByUuid(UUID uuid) {
		for (RankClass type : RankClass.values()){
			if (type.getUuid().equals(uuid)){
				return type;
			}
		}
		return null;
	}


	@Override
    public RankClass getKindOf() {
		return null;
	}


	@Override
    public Set<RankClass> getGeneralizationOf() {
		return new HashSet<RankClass>();
	}


	@Override
    public RankClass getPartOf() {
		return null;
	}


	@Override
    public Set<RankClass> getIncludes() {
		return new HashSet<RankClass>();
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
