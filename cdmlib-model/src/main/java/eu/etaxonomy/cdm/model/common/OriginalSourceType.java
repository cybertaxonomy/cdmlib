// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.media.Media;



/**
 * The original source type is used to define the type of an {@link OriginalSourceBasesource original source}.<BR>
 * It is used to distinguish e.g. data lineage when importing data from one database to another from e.g. content oriented
 * sources such as the citation in a book.
 * In future they may come further source types. 
 * @author a.mueller
 * @created 15.05.2013
 */
@XmlEnum
public enum OriginalSourceType implements IDefinedTerm<OriginalSourceType>, Serializable{
	
	//0
	/**
	 * Unknown Provenance is the type to be used if no information is available about the type
	 * of activity that happened.
	 *   
	 */
	@XmlEnumValue("Unknown")
	Unknown(UUID.fromString("b48a443c-05f2-47ff-b885-1d3bd31118e1"), "Unknown Provenance"),
	
	//1
	/**
	 * Primary Taxonomic Source describes the sources a taxonomist uses to gather certain information. 
	 * E.g. a taxonomist may have used three books/articles/other references to gather information
	 * about the distribution status of a taxon.
	 * He/she will store these references as original source of type Primary Taxonomic Source.
	 * This is a specification of PROV-O Primary Source 
	 * ({@link http://www.w3.org/TR/2013/REC-prov-o-20130430/#PrimarySource})
	 *   
	 */
	@XmlEnumValue("Primary Taxonomic Source")
	PrimaryTaxonomicSource(UUID.fromString("c990beb3-3bc9-4dad-bbdf-9c11683493da"), "Primary Taxonomic Source"),
	
	//2
	/**
	 * Data Lineage describes the data life cycle of electronically available data. A typical use-case for 
	 * data lineage is a data import from one database to another. Sources of type data lineage will store information about the
	 * original database and the identifier and table (->namespace) used in the original database.
	 * There are multiple types of data lineage: Blackbox, Dispatcher, Aggregator ({@link http://de.wikipedia.org/wiki/Data-Lineage})  
	 */
	@XmlEnumValue("Data Lineage")
	Lineage(UUID.fromString("4f9fdf9a-f3b5-490c-96f0-90e050599b0e"), "Data Lineage"),

	//3
	/**
	 * Database Import is a specialisation of {@value #Lineage}. It describes the electronic import of data 
	 * from an external datasource into the given datasource. This step may include data transformations also
	 * but the primary process is the import of data.
	*/
	@XmlEnumValue("Database Import")
	Import(UUID.fromString("2a3902ff-06a7-4307-b542-c743e664b8f2"), "Database Import"),

	//4
	/**
	 * Data Transformation is a specification of {@value #Lineage} and describes a data transformation process 
	 * that happens primarily  on the given dataset but may also include external data.
	 */
	@XmlEnumValue("Data Transformation")
	Transformation(UUID.fromString("d59e80e5-cbb7-4658-b74d-0626bbb0da7f"), "Data Transformation"),


	//5
	/**
	 * Data aggregation is a spcification of {@value #Lineage} and describes the data transformation process
	 * that primarily includes data aggregation processes but may also include data imports and transformations.
	 */
	@XmlEnumValue("Data Aggregation")
	Aggregation(UUID.fromString("944f2f40-5144-4c81-80d9-f61aa10507b8"), "Data Aggregation"),

	//6
	/**
	 * <code>Other</code> is the type to be used if none of the other types is applicable.
	 */
	@XmlEnumValue("Other")
	Other(UUID.fromString("b7c4b7fe-0aef-428a-bb7b-9153a11bf845"), "Other"),

	
	;
	
	
	private static final Logger logger = Logger.getLogger(OriginalSourceType.class);

	private String readableString;
	private UUID uuid;

	private OriginalSourceType(UUID uuid, String defaultString){
		this.uuid = uuid;
		readableString = defaultString;
	}

	@Transient
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
	

	@Override
    public OriginalSourceType readCsvLine(Class<OriginalSourceType> termClass,
			List<String> csvLine, java.util.Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
    public void writeCsvLine(CSVWriter writer, OriginalSourceType term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public OriginalSourceType getByUuid(UUID uuid) {
		for (OriginalSourceType referenceType : OriginalSourceType.values()){
			if (referenceType.getUuid().equals(uuid)){
				return referenceType;
			}
		}
		return null;
	}


	@Override
    public OriginalSourceType getKindOf() {
		return null;
	}


	@Override
    public Set<OriginalSourceType> getGeneralizationOf() {
		return new HashSet<OriginalSourceType>();
	}


	@Override
    public OriginalSourceType getPartOf() {
		return null;
	}


	@Override
    public Set<OriginalSourceType> getIncludes() {
		return new HashSet<OriginalSourceType>();
	}


	@Override
    public Set<Media> getMedia() {
		return new HashSet<Media>();
	}

}
