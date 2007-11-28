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
public abstract class DefinedTermBase extends TermBase{
	static Logger logger = Logger.getLogger(DefinedTermBase.class);

	private TermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet();
	private TermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet();
	private Set<Media> media = new HashSet();

	private TermVocabulary vocabulary;
	
	
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String term, String label) {
		super(term, label);
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TermBase getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(TermBase kindOf){
		this.kindOf = kindOf;
	}

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}
	public void setGeneralizationOf(Set<DefinedTermBase> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TermBase getPartOf(){
		return this.partOf;
	}
	public void setPartOf(TermBase partOf){
		this.partOf = partOf;
	}

	@OneToMany
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

	
	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public TermVocabulary getVocabulary() {
		return this.vocabulary;
	}
	public void setVocabulary(TermVocabulary newVocabulary) {
		if (this.vocabulary != null) { 
			this.vocabulary.terms.remove(this);
		}
		if (newVocabulary!= null) { 
			newVocabulary.terms.add(this);
		}
		this.vocabulary = newVocabulary;		
	}
	
}