/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.strategy.cache.agent.InstitutionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.TermDefaultCacheStrategy;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermBase", propOrder = {
    "uri",
    "representations"
})
@XmlSeeAlso({
	DefinedTermBase.class,
	TermVocabulary.class,
	FeatureTree.class
})
@MappedSuperclass
public abstract class TermBase extends IdentifiableEntity {
	private static final long serialVersionUID = 1471561531632115822L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermBase.class);
	
	@XmlElement(name = "URI")
	@Field(index=Index.UN_TOKENIZED)
	private String uri;
	
	@XmlElementWrapper(name = "Representations")
	@XmlElement(name = "Representation")
    @OneToMany(fetch=FetchType.EAGER)
	@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	@IndexedEmbedded(depth = 2)
	private Set<Representation> representations = new HashSet<Representation>();
	
	public TermBase(){
		super();
		initCacheStrategy();
		
	}
	private void initCacheStrategy() {
		this.cacheStrategy = new TermDefaultCacheStrategy<TermBase>();
	}
	public TermBase(String term, String label, String labelAbbrev) {
		super();
		this.addRepresentation(new Representation(term, label, labelAbbrev, Language.DEFAULT()) );
		initCacheStrategy();
	}

	public Set<Representation> getRepresentations() {
		return this.representations;
	}

	public void addRepresentation(Representation representation) {
		this.representations.add(representation);
	}

	public void removeRepresentation(Representation representation) {
		this.representations.remove(representation);
	}

	public Representation getRepresentation(Language lang) {
		for (Representation repr : representations){
			Language reprLanguage = repr.getLanguage();
			if (reprLanguage != null && reprLanguage.equals(lang)){
				return repr;
			}
		}
		return null;
	}
	
	/**
	 * @see #getPreferredRepresentation(Language)
	 * @param language
	 * @return
	 */
	public Representation getPreferredRepresentation(Language language) {
		Representation repr = getRepresentation(language); 
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			repr = getRepresentations().iterator().next();
		}
		return repr;
	}
	
	/**
	 * Returns the Representation in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if necessary further falls back
	 * to return the first element found if any.
	 * 
	 * TODO think about this fall-back strategy & 
	 * see also {@link TextData#getPreferredLanguageString(List)}
	 * 
	 * @param languages
	 * @return
	 */
	public Representation getPreferredRepresentation(List<Language> languages) {
		Representation repr = null;
		if(languages != null){
			for(Language language : languages) {
				repr = getRepresentation(language); 
				if(repr != null){
					return repr;
				}
			}
		}
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			Iterator<Representation> it = getRepresentations().iterator();
			if(it.hasNext()){
				repr = getRepresentations().iterator().next();
			}
		}
		return repr;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Transient
	public String getLabel() {
		if(getLabel(Language.DEFAULT())!=null){
			Representation repr = getRepresentation(Language.DEFAULT());
			return (repr == null)? null :repr.getLabel();
		}else{
			for (Representation r : representations){
				return r.getLabel();
			}			
		}
		return super.getUuid().toString();
	}
	
	public String getLabel(Language lang) {
		Representation repr = this.getRepresentation(lang);
		return (repr == null) ? null : repr.getLabel();
	}	
	
	public void setLabel(String label){
		Language lang = Language.DEFAULT();
		setLabel(label, lang);
	}

	public void setLabel(String label, Language language){
		if (language != null){
			Representation repr = getRepresentation(language);
			if (repr != null){
				repr.setLabel(label);
			}else{
				repr = Representation.NewInstance(null, label, null, language);
			}
			this.addRepresentation(repr);
		}
	}

	@Transient
	public String getDescription() {
		return this.getDescription(Language.DEFAULT());
	}

	public String getDescription(Language lang) {
		Representation repr = this.getRepresentation(lang);
		return (repr == null) ? null :repr.getDescription();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (TermBase.class.isAssignableFrom(obj.getClass())){
			TermBase dtb = (TermBase)obj;
			if (dtb.getUuid().equals(this.getUuid())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		//TODO eliminate nasty LazyInitializationException loggings
		try {
			return super.toString(); 
		} catch (LazyInitializationException e) {
			return super.toString()+" "+this.getUuid();
		}
	}
	
//*********************** CLONE ********************************************************/
	
	/** 
	 * Clones <i>this</i> TermBase. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> TermBase by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()throws CloneNotSupportedException {
		
		TermBase result = (TermBase) super.clone();
		
		result.representations = new HashSet<Representation>();
		for (Representation rep : this.representations){
			result.representations.add((Representation)rep.clone());
		}
		
		
		
		return result;
		
	}

}
