/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType;

import au.com.bytecode.opencsv.CSVWriter;

import java.util.*;
import javax.persistence.*;

/**
 * list of languages according to current internet best practices as given by IANA
 * or ISO codes.  http://www.ietf.org/rfc/rfc4646.txt 
 * http://www.loc.gov/standards/iso639-2/php/English_list.php
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@Entity
public class Language extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Language.class);

	private char[] iso639_1 = new char[2];
	private char[] iso639_2 = new char[3];
	
	public Language() {
		super();
	}
	public Language(char[] iso639_1, char[] iso639_2, String englishLabel, String frenchLabel) {
		super();
		this.iso639_1=iso639_1;
		this.iso639_2=iso639_2;
		this.addRepresentation(new Representation(englishLabel, String.valueOf(iso639_2), Language.ENGLISH()));
		this.addRepresentation(new Representation(frenchLabel, String.valueOf(iso639_2), Language.FRENCH()));
	}
	public Language(String text, String label, Language lang) {
		super();
		this.addRepresentation(new Representation(text,label,lang));
	}
	public Language(String label, String text) {
		this(label,text, DEFAULT());
	}

	public static final Language getUUID(String uuid){
		return (Language) dao.findByUuid(uuid);
	}

	
	
	public static final Language DEFAULT(){
		return ENGLISH();
	}
	
	public static final Language CHINESE(){
		return getUUID("a9fc2782-5b2a-466f-b9c3-64d9ca6614c4");
	}

	public static final Language ENGLISH(){
		return getUUID("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
	}

	public static final Language SPANISH(){
		return getUUID("511d8125-f5e6-445d-aee2-6327375238be");
	}

	public static final Language HINDI(){
		return getUUID("0a1d9d1d-135d-4575-b172-669b51673c39");
	}

	public static final Language ARABIC(){
		return getUUID("4d3ec2eb-536f-4aab-81c5-34e37a3edbba");
	}

	public static final Language RUSSIAN(){
		return getUUID("64ea9354-cbf8-40de-9f6e-387d24896f50");
	}

	public static final Language PORTUGUESE(){
		return getUUID("c2c08339-2405-4d7d-bd25-cbe01fb7ce09");
	}

	public static final Language JAPANESE(){
		return getUUID("6778c7fb-c195-4dc1-ae3f-164201314e51");
	}

	public static final Language GERMAN(){
		return getUUID("d1131746-e58b-4e80-a865-f5182c9c3073");
	}
	
	public static final Language FRENCH(){
		return getUUID("7759a1d8-a5ea-454a-8c93-1dcfaae8cc21");
	}

	public static final Language ITALIAN(){
		return getUUID("fecbf0c7-fea9-465b-8a16-950517c5c0c4");
	}

	public static final Language DUTCH(){
		return getUUID("9965d79a-acf9-4921-a2c0-863b8c16c056");
	}

	public static final Language POLISH(){
		return getUUID("3fdca387-f1b0-4ec1-808f-1bc3dc482194");
	}

	public void readCsvLine(List<String> csvLine) {
		// read UUID, URI, english label+description
		super.readCsvLine(csvLine);
		// iso codes extra
		this.iso639_1=csvLine.get(4).trim().toCharArray();
		this.iso639_2=csvLine.get(5).trim().toCharArray();
	}
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[6];
		line[0] = getUuid();
		line[1] = getUri();
		line[2] = getLabel(Language.ENGLISH());
		line[3] = getDescription(Language.ENGLISH());
		line[4] = String.valueOf(this.iso639_1);
		line[5] = String.valueOf(this.iso639_2);
		writer.writeNext(line);
	}
	
}