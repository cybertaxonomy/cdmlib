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
import eu.etaxonomy.cdm.model.Description;

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
public abstract class DefinedTermBase extends VersionableEntity{
	public DefinedTermBase() {
		super();
	}
	public DefinedTermBase(String englishTerm) {
		this();
		this.addRepresentation(new Representation(englishTerm, Language.ENGLISH()) );
	}

	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	//URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!
	private String uri;
	private Set<Representation> representations = new HashSet();
	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet();
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet();
	private Set<Media> media = new HashSet();
	
	
	@OneToMany
	public Set<Representation> getRepresentations(){
		return this.representations;
	}
	public void setRepresentations(Set<Representation> representations) {
		this.representations = representations;
	}
	public void addRepresentation(Representation representation) {
		this.representations.add(representation);
	}
	public void removeRepresentation(Representation representation) {
		this.representations.remove(representation);
	}

	@Transient
	public Representation getRepresentation(Language lang) {
		for (Representation repr : representations){
			if (repr.getLanguage() == lang){
				return repr;
			}
		}
		return null;
	}


	@ManyToOne
	public DefinedTermBase getKindOf(){
		return this.kindOf;
	}

	/**
	 * 
	 * @param kindOf    kindOf
	 */
	public void setKindOf(DefinedTermBase kindOf){
		this.kindOf = kindOf;
	}

	@OneToMany
	public Set<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}

	/**
	 * 
	 * @param generalizationOf    generalizationOf
	 */
	public void setGeneralizationOf(Set<DefinedTermBase> generalizationOf) {
		this.generalizationOf = generalizationOf;
	}


	@ManyToOne
	public DefinedTermBase getPartOf(){
		return this.partOf;
	}

	/**
	 * 
	 * @param partOf    partOf
	 */
	public void setPartOf(DefinedTermBase partOf){
		this.partOf = partOf;
	}

	@OneToMany
	public Set<DefinedTermBase> getIncludes(){
		return this.includes;
	}
	public void setIncludes(Set<DefinedTermBase> includes) {
		this.includes = includes;
	}
	public void addIncludes(DefinedTermBase includes) {
		this.includes.add(includes);
	}
	public void removeIncludes(DefinedTermBase includes) {
		this.includes.remove(includes);
	}


	@OneToMany
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

	
	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

	
	/**
	 * 
	 * @param uri    uri
	 */
	@Transient
	public static DefinedTermBase getDefinedTermByUri(String uri){
		return null;
	}

}