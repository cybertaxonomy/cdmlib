/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * list of languages according to current internet best practices as given by IANA
 * or ISO codes.  http://www.ietf.org/rfc/rfc4646.txt 
 * http://www.loc.gov/standards/iso639-2/php/English_list.php
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Language")
@XmlRootElement(name = "Language")
@Entity
@Audited
public class Language extends DefinedTermBase<Language> {
	private static final long serialVersionUID = -5030610079904074217L;
	private static final Logger logger = Logger.getLogger(Language.class);

	private static final UUID uuidChinese = UUID.fromString("a9fc2782-5b2a-466f-b9c3-64d9ca6614c4");
	public static final UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
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
	private static final UUID uuidLatin = UUID.fromString("160a5b6c-87f5-4422-9bda-78cd404c179e");
	private static Language ENGLISH;
	private static Language LATIN;
	private static Language POLISH;
	private static Language DUTCH;
	private static Language ITALIAN;
	private static Language FRENCH;
	private static Language GERMAN;
	private static Language JAPANESE;
	private static Language PORTUGUESE;
	private static Language RUSSIAN;
	private static Language ARABIC;
	private static Language HINDI;
	private static Language SPANISH;
	private static Language CHINESE;
	
	public static Language NewInstance(){
		return new Language();
	}
	
	public static Language NewInstance(UUID uuid){
		return new Language(uuid);
	}
	
	@XmlAttribute(name = "iso639_1")
	//TODO create userDefinedType ?
	@Column(length=2)
	private String iso639_1;
	
	@XmlAttribute(name = "iso639_2")
	//TODO create userDefinedType ?
	@Column(length=3)
	private String iso639_2;
	
	public Language() {
		super();
	}
	public Language(UUID uuid) {
		super();
		this.setUuid(uuid);
	}
	public Language(String iso639_1, String iso639_2, String englishLabel, String frenchLabel) throws Exception {
		super();
		if(iso639_1 != null && iso639_1.length() > 2){
			logger.warn("iso639_1 too long: "+iso639_1.toString());
		}
		if(iso639_1 != null && iso639_2.length() > 3){
			logger.warn("iso639_2 too long: "+iso639_2.toString());
		}
		this.iso639_1=iso639_1;
		this.iso639_2=iso639_2;
		String textEnglish = englishLabel;
		String textFrench = englishLabel;
		String label = iso639_2;
		String labelAbbrev = null;
		this.addRepresentation(new Representation(textEnglish, label, labelAbbrev, Language.ENGLISH()));
		this.addRepresentation(new Representation(textFrench, label, labelAbbrev, Language.FRENCH()));
	}
	public Language(String text, String label, String labelAbbrev, Language lang) {
		super();
		this.addRepresentation(new Representation(text,label,labelAbbrev, lang));
	}
	public Language(String label, String text, String labelAbbrev) {
		this(label,text,labelAbbrev, DEFAULT());
	}
	
	public static final Language DEFAULT(){
		return ENGLISH;
	}

	public static final Language CHINESE(){
		return CHINESE;
	}

	public static final Language ENGLISH(){
		return ENGLISH;
	}

	public static final Language SPANISH(){
		return SPANISH;
	}

	public static final Language HINDI(){
		return HINDI;
	}

	public static final Language ARABIC(){
		return ARABIC;
	}

	public static final Language RUSSIAN(){
		return RUSSIAN;
	}

	public static final Language PORTUGUESE(){
		return PORTUGUESE;
	}

	public static final Language JAPANESE(){
		return JAPANESE;
	}

	public static final Language GERMAN(){
		return GERMAN;
	}
	
	public static final Language FRENCH(){
		return FRENCH;
	}

	public static final Language ITALIAN(){
		return ITALIAN;
	}

	public static final Language DUTCH(){
		return DUTCH;
	}

	public static final Language POLISH(){
		return POLISH;
	}
	
	public static final Language LATIN(){
		return LATIN;
	}
	
	/**
	 * Get the according iso639-1 alpha-2 language code 
	 * http://www.loc.gov/standards/iso639-2/
	 * 
	 * @return the iso639 alpha-2 language code or null if not available
	 */
	public String getIso639_1() {
		return iso639_1;
	}

	public void setIso639_1(String iso639_1) {
		iso639_1 = iso639_1.trim();
		if(iso639_1.length() > 2){
			logger.warn("Iso639-1: "+iso639_1+" too long");
		}
		this.iso639_1 = iso639_1;
	}

	/**
	 * Get the according iso639-2 alpha-3 language code 
	 * http://www.loc.gov/standards/iso639-2/
	 * 
	 * @return the iso639 alpha-3 language code or null if not available
	 */
	public String getIso639_2() {
		return iso639_2;
	}

	public void setIso639_2(String iso639_2) {
		iso639_2 = iso639_2.trim();
		if(iso639_2.length() > 3 ){
			logger.warn("Iso639-2: "+iso639_2+" too long");
		}
		this.iso639_2 = iso639_2;
	}
 
	@Override 
	public Language readCsvLine(Class<Language> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		try {
		    Language newInstance =  Language.class.newInstance();
		    if ( UUID.fromString(csvLine.get(0).toString()).equals(Language.uuidEnglish)){
			    DefinedTermBase.readCsvLine(newInstance, csvLine, newInstance);
		    }else{
			    DefinedTermBase.readCsvLine(newInstance,csvLine,(Language)terms.get(Language.uuidEnglish));
		    }
		
		    newInstance.setIso639_1(csvLine.get(5).trim());
		    newInstance.setIso639_2(csvLine.get(4).trim());
		    //TODO could replace with generic validation
		    if(iso639_1 != null && iso639_1.length() > 2){
			    logger.warn("Iso639-1: "+ newInstance.getIso639_1() +" from "+csvLine.get(3)+" ,"+csvLine.get(2)+" too long");
		    }
		    if(iso639_2 != null &&  iso639_2.length() > 3 ){
			    logger.warn("Iso639-2: "+newInstance.getIso639_2()+" from "+csvLine.get(3)+" ,"+csvLine.get(2)+" too long");
		    }
		
		    return newInstance;
		} catch (Exception e) {
			logger.error(e);
			return null;
		} 
	}
	
	@Override
	public void writeCsvLine(CSVWriter writer, Language language) {
		String [] line = new String[6];
		line[0] = language.getUuid().toString();
		line[1] = language.getUri();
		line[2] = language.getLabel(Language.ENGLISH());
		line[3] = language.getDescription(Language.ENGLISH());
		line[4] = language.getIso639_1();
		line[5] = language.getIso639_2();
		writer.writeNext(line);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.TermBase#toString()
	 */
	@Override
	public String toString() {
		if (this.getLabel() != null){
			return this.getLabel();
		}else{
			return super.toString();
		}
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<Language> termVocabulary) {
		Language.ARABIC = termVocabulary.findTermByUuid(Language.uuidArabic);
		Language.CHINESE = termVocabulary.findTermByUuid(Language.uuidChinese);
		Language.DUTCH = termVocabulary.findTermByUuid(Language.uuidDutch);
		Language.ENGLISH = termVocabulary.findTermByUuid(Language.uuidEnglish);
		Language.FRENCH = termVocabulary.findTermByUuid(Language.uuidFrench);
		Language.GERMAN = termVocabulary.findTermByUuid(Language.uuidGerman);
		Language.HINDI = termVocabulary.findTermByUuid(Language.uuidHindi);
		Language.ITALIAN = termVocabulary.findTermByUuid(Language.uuidItalian);
		Language.JAPANESE = termVocabulary.findTermByUuid(Language.uuidJapanese);
		Language.LATIN = termVocabulary.findTermByUuid(Language.uuidLatin);
		Language.POLISH = termVocabulary.findTermByUuid(Language.uuidPolish);
		Language.PORTUGUESE = termVocabulary.findTermByUuid(Language.uuidPortuguese);
		Language.RUSSIAN = termVocabulary.findTermByUuid(Language.uuidRussian);
		Language.SPANISH = termVocabulary.findTermByUuid(Language.uuidSpanish);
		addLanguageForVocabularyRepresentation(termVocabulary);
	}
	
	private void addLanguageForVocabularyRepresentation(TermVocabulary<Language> termVocabulary){
		for (Representation repr : termVocabulary.getRepresentations()){
			Language lang = repr.getLanguage();
			if (lang == null){
				repr.setLanguage(Language.DEFAULT());
			}
		}
	}
	
	
	
}