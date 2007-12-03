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
	static Language langEN = new Language("English","EN");
	static Language langCH = new Language("Chinese","CH");
	static Language langGE = new Language("German","DE");


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

	public static final Language DEFAULT(){
		return langEN;
	}
	
	public static final Language CHINESE(){
		return langCH;
	}

	public static final Language ENGLISH(){
		return langEN;
	}

	public static final Language SPANISH(){
		return null;
	}

	public static final Language HINDI(){
		return null;
	}

	public static final Language ARABIC(){
		return null;
	}

	public static final Language BENGALI(){
		return null;
	}

	public static final Language RUSSIAN(){
		return null;
	}

	public static final Language PORTUGUESE(){
		return null;
	}

	public static final Language JAPANESE(){
		return null;
	}

	public static final Language GERMAN(){
		return langGE;
	}
	
	public static final Language FRENCH(){
		return langGE;
	}

	
	public void readCsvLine(List<String> csvLine) {
		this.iso639_1=csvLine.get(0).trim().toCharArray();
		this.iso639_2=csvLine.get(2).trim().toCharArray();
		this.addRepresentation(new Representation(csvLine.get(3).trim(), String.valueOf(iso639_2), Language.ENGLISH()));
		this.addRepresentation(new Representation(csvLine.get(4).trim(), String.valueOf(iso639_2), Language.FRENCH()));
		logger.debug("Created "+this.getClass().getSimpleName() + " term: "+this.toString());
	}
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[5];
		line[0] = getUuid();
		line[1] = getUri();
		line[2] = getLabel(Language.ENGLISH());
		line[3] = getDescription();
		line[4] = getLabel(Language.FRENCH());
		line[5] = String.valueOf(this.iso639_1);
		line[6] = String.valueOf(this.iso639_2);
		writer.writeNext(line);
	}
	
}