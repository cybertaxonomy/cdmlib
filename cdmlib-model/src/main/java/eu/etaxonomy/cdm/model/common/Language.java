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
import org.springframework.stereotype.Component;
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
@Component
public class Language extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Language.class);

	private static final UUID uuidChinese = UUID.fromString("a9fc2782-5b2a-466f-b9c3-64d9ca6614c4");
	private static final UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
	private static final UUID uuidSpanish = UUID.fromString("511d8125-f5e6-445d-aee2-6327375238be");
	private static final UUID uuidHindi = UUID.fromString("0a1d9d1d-135d-4575-b172-669b51673c39");
	private static final UUID uuidArabic = UUID.fromString("4d3ec2eb-536f-4aab-81c5-34e37a3edbba");
	private static final UUID uuidRussian = UUID.fromString("64ea9354-cbf8-40de-9f6e-387d24896f50");
	private static final UUID uuidPortuguese = UUID.fromString("c2c08339-2405-4d7d-bd25-cbe01fb7ce09");
	private static final UUID uuidJapanese = UUID.fromString("6778c7fb-c195-4dc1-ae3f-164201314e51");
	private static final UUID uuidGerman = UUID.fromString("d1131746-e58b-4e80-a865-f5182c9c3073");
	private static final UUID uuidFrench = UUID.fromString("7759a1d8-a5ea-454a-8c93-1dcfaae8cc21");
	private static final UUID uuidItalian = UUID.fromString("fecbf0c7-fea9-465b-8a16-950517c5c0c4");
	private static final UUID uuidDutch = UUID.fromString("9965d79a-acf9-4921-a2c0-863b8c16c056");
	private static final UUID uuidPolish = UUID.fromString("3fdca387-f1b0-4ec1-808f-1bc3dc482194");
	
	
	
	private char[] iso639_1 = new char[2];
	private char[] iso639_2 = new char[3];
	

	public Language() {
		super();
	}
	public Language(UUID uuid) {
		super();
		this.setUuid(uuid);
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

	public static final Language getUUID(UUID uuid){
		return (Language)findByUuid(uuid);
	}

	
	public static final Language DEFAULT(){
		return ENGLISH();
	}
	

	public static final Language CHINESE(){
		return getUUID(uuidChinese);
	}

	public static final Language ENGLISH(){
		return getUUID(uuidEnglish);
	}

	public static final Language SPANISH(){
		return getUUID(uuidSpanish);
	}

	public static final Language HINDI(){
		return getUUID(uuidHindi);
	}

	public static final Language ARABIC(){
		return getUUID(uuidArabic);
	}

	public static final Language RUSSIAN(){
		return getUUID(uuidRussian);
	}

	public static final Language PORTUGUESE(){
		return getUUID(uuidPortuguese);
	}

	public static final Language JAPANESE(){
		return getUUID(uuidJapanese);
	}

	public static final Language GERMAN(){
		return getUUID(uuidGerman);
	}
	
	public static final Language FRENCH(){
		return getUUID(uuidFrench);
	}

	public static final Language ITALIAN(){
		return getUUID(uuidItalian);
	}

	public static final Language DUTCH(){
		return getUUID(uuidDutch);
	}

	public static final Language POLISH(){
		return getUUID(uuidPolish);
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
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel(Language.ENGLISH());
		line[3] = getDescription(Language.ENGLISH());
		line[4] = String.valueOf(this.iso639_1);
		line[5] = String.valueOf(this.iso639_2);
		writer.writeNext(line);
	}
	
}