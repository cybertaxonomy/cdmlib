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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.common.init.TermLoader;

import java.util.*;

import javax.persistence.*;

/**
 * workaround for enumerations, base type according to TDWG.  For linear ordering
 * use partOf relation and BreadthFirst. Default iterator order should therefore
 * be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements IDefTerm{
	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	//public static IDefinedTermDao dao;
	//public static ITermService termService;
	//Map for preloading all DefinedTerms
	static protected Map<UUID, DefinedTermBase> definedTermsMap = null;

	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet<DefinedTermBase>();
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet<DefinedTermBase>();
	private Set<Media> media = new HashSet<Media>();
	protected TermVocabulary<T> vocabulary;
	
	public static void initTermList(ITermLister termLister){
		logger.debug("initTermList");
		if (definedTermsMap == null){
			if (termLister == null){   //e.g. when used in tests with no database connection
				definedTermsMap = new HashMap<UUID, DefinedTermBase>();
				try {
					String strUuidEnglish = "e9f8cdb7-6819-44e8-95d3-e2d0690c3523";
					UUID uuidEnglish = UUID.fromString(strUuidEnglish);
					Language english = new Language(uuidEnglish);
					definedTermsMap.put(english.getUuid(), english);
					TermLoader.setDefinedTermsMap(definedTermsMap);
					new TermLoader().loadAllDefaultTerms();
				} catch (Exception e) {
					logger.error("Error ocurred when loading terms");
				}				
				
			}else{
				List<DefinedTermBase> list = termLister.listTerms();
				definedTermsMap = new HashMap<UUID, DefinedTermBase>();
				for (DefinedTermBase dtb: list){
					definedTermsMap.put(dtb.getUuid(), dtb);
				}
			}
		}
		logger.debug("initTermList - end");
	}
	
	public static DefinedTermBase findByUuid(UUID uuid){
		//in tests tems may no be initialised by database access
		if (!isInitialized()){
			initTermList(null);
		}
		return definedTermsMap.get(uuid);
	}
	
	public static boolean isInitialized(){
		return (definedTermsMap != null);
	}
	
	@Transient
	public static Map<UUID, DefinedTermBase> getDefinedTerms(){
		return definedTermsMap;
	}
	
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label) {
		super(term, label);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#readCsvLine(java.util.List)
	 */
	public void readCsvLine(List<String> csvLine) {
		readCsvLine(csvLine, Language.ENGLISH());
	}
	public void readCsvLine(List<String> csvLine, Language lang) {
		this.setUuid(UUID.fromString(csvLine.get(0)));
		this.setUri(csvLine.get(1));
		this.addRepresentation(new Representation(csvLine.get(3), csvLine.get(2).trim(), lang) );
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#writeCsvLine(au.com.bytecode.opencsv.CSVWriter)
	 */
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[4];
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel();
		line[3] = getDescription();
		writer.writeNext(line);
	}
	
	@Transient
	//@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(DefinedTermBase kindOf){
		this.kindOf = kindOf;
	}

	@Transient
	//@OneToMany(fetch=FetchType.LAZY)
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}
	public void setGeneralizationOf(Set<DefinedTermBase> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}


	@Transient
	//@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getPartOf(){
		return this.partOf;
	}
	public void setPartOf(DefinedTermBase partOf){
		this.partOf = partOf;
	}

	@Transient
	//@OneToMany(fetch=FetchType.LAZY)
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getIncludes(){
		return this.includes;
	}
	public void setIncludes(Set<DefinedTermBase> includes) {
		this.includes = includes;
	}
	public void addIncludes(DefinedTermBase includes) {
		this.includes.add(includes);
	}
	public void removeIncludes(TermBase includes) {
		this.includes.remove(includes);
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia(){
		return this.media;
	}
	public void setMedia(Set<Media> media) {
		this.media = media;
	}
	public void addMedia(Media media) {
		this.media.add(media);
	}
	public void removeMedia(Media media) {
		this.media.remove(media);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#getVocabulary()
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade( { CascadeType.SAVE_UPDATE })
	public TermVocabulary getVocabulary() {
		return this.vocabulary;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	public void setVocabulary(TermVocabulary newVocabulary) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.vocabulary == newVocabulary){ return;}
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (newVocabulary!= null) { 
			newVocabulary.terms.add(this);
		}
		this.vocabulary = newVocabulary;		
	}
	
}