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

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;


import java.io.Serializable;
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
public abstract class DefinedTermBase extends TermBase implements IDefTerm{
	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	//public static IDefinedTermDao dao;
	//public static ITermService termService;
	//Map for preloading all DefinedTerms
	static protected Map<String, DefinedTermBase> definedTermsMap = null;

	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet<DefinedTermBase>();
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet<DefinedTermBase>();
	private Set<Media> media = new HashSet<Media>();
	private TermVocabulary vocabulary;
	
	public static void initTermList(ITermService termService){
		logger.debug("initTermList");
		if (definedTermsMap == null){
			List<DefinedTermBase> list = termService.listTerms();
			definedTermsMap = new HashMap<String, DefinedTermBase>();
			for (DefinedTermBase dtb: list){
				definedTermsMap.put(dtb.getUuid(), dtb);
			}
		}
		logger.debug("initTermList - end");
	}
	
	public static DefinedTermBase findByUuid(String uuid){
		return definedTermsMap.get(uuid);
	}
	
	public static boolean isInitialized(){
		return (definedTermsMap != null);
	}
	
	public static Map<String, DefinedTermBase> getDefinedTerms(){
		return definedTermsMap;
	}
	
	
//	public static void setDao(IDefinedTermDao dao) {
//		logger.warn("Setting DefinedTerm static dao");
//		DefinedTermBase.dao = dao;
//	}
//	
//	public static void setService(ITermService service) {
//		logger.warn("Setting DefinedTerm static service");
//		DefinedTermBase.termService = service;
//	}
	
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
		this.setUuid(csvLine.get(0));
		this.setUri(csvLine.get(1));
		this.addRepresentation(new Representation(csvLine.get(3), csvLine.get(2).trim(), lang) );
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#writeCsvLine(au.com.bytecode.opencsv.CSVWriter)
	 */
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[4];
		line[0] = getUuid();
		line[1] = getUri();
		line[2] = getLabel();
		line[3] = getDescription();
		writer.writeNext(line);
	}
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(DefinedTermBase kindOf){
		this.kindOf = kindOf;
	}

	@OneToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}
	public void setGeneralizationOf(Set<DefinedTermBase> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getPartOf(){
		return this.partOf;
	}
	public void setPartOf(DefinedTermBase partOf){
		this.partOf = partOf;
	}

	@OneToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
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
		if(this.vocabulary == newVocabulary) return;
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (newVocabulary!= null) { 
			newVocabulary.terms.add(this);
		}
		this.vocabulary = newVocabulary;		
	}
	
}