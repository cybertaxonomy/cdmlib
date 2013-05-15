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
 * @author a.mueller
 * @created 15.05.2013
 */
@XmlEnum
public enum OriginalSourceType implements IDefinedTerm<OriginalSourceType>, Serializable{
	//0
	/**
	 * Data Lineage describes the data life cycle of electronically available data. A typical use-case for data lineage
	 * is a data import from one database to another. Sources of type data lineage will store information about the
	 * original database and the identifier and table (->namespace) used in the original database.
	 * There are multiple types of data lineage: Blackbox, Dispatcher, Aggregator ({@link http://de.wikipedia.org/wiki/Data-Lineage})  
	 */
	@XmlEnumValue("Data Lineage")
	Lineage(UUID.fromString("4f9fdf9a-f3b5-490c-96f0-90e050599b0e"), "Data Lineage"),
	
	//1
	/**
	 * Content describes the sources a taxonomist uses to gather certain information. E.g. a taxonomist may have used
	 * three books/articles/other references to gather information about the distribution status of a taxon.
	 * He/she will store these references as original source of type Content.  
	 */
	@XmlEnumValue("Content")
	Content(UUID.fromString("c990beb3-3bc9-4dad-bbdf-9c11683493da"), "Content"),
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
