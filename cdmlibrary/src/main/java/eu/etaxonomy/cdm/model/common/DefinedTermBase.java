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
@MappedSuperclass
public abstract class DefinedTermBase extends VersionableEntity{
	static Logger logger = Logger.getLogger(DefinedTermBase.class);

	//URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!
	private String uri;
	private Set<Representation> representations = new HashSet();
	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf = new HashSet();
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes = new HashSet();
	private Set<Media> media = new HashSet();
	
	public DefinedTermBase(String term, String label) {
		super();
		this.addRepresentation(new Representation(term, label, Language.DEFAULT()) );
	}

	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
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
	@Cascade({CascadeType.SAVE_UPDATE})
	public DefinedTermBase getKindOf(){
		return this.kindOf;
	}
	public void setKindOf(DefinedTermBase kindOf){
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
	public DefinedTermBase getPartOf(){
		return this.partOf;
	}
	public void setPartOf(DefinedTermBase partOf){
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
	public void removeIncludes(DefinedTermBase includes) {
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

	
	public String getUri(){
		return this.uri;
	}
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
	
	public String toString(){
		String result="DT<"+uri+">:";
		for (Representation r : representations){
			result += r.getLabel()+"("+r.getLanguage().getTermLabel()+")";
		}
		return result;
	}
	
	@Transient
	public String getTermLabel(){
		return this.getRepresentation(Language.DEFAULT()).getLabel();
	}
	@Transient
	public String getTermLabel(Language lang){
		return this.getRepresentation(lang).getLabel();
	}
	@Transient
	public String getTermText(){
		return this.getRepresentation(Language.DEFAULT()).getLabel();
	}
	@Transient
	public String getTermText(Language lang){
		return this.getRepresentation(lang).getLabel();
	}


	@Override
	// equals if UUIDs are the same, no matter where/when created!
	public boolean equals(Object obj) {
		if (DefinedTermBase.class.isAssignableFrom(obj.getClass())){
			DefinedTermBase dtb = (DefinedTermBase)obj;
			if (dtb.getUuid().equals(this.getUuid())){
				return true;
			}
		}
		return false;
	}
	
}