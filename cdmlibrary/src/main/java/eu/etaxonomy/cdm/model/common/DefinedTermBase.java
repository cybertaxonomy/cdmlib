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
public abstract class DefinedTermBase extends VersionableEntity{
	public DefinedTermBase() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	//URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!
	private String uri;
	//The RDF ontology source defining the terms to be loaded when a database is created for the first time.  Software can go
	//and grap these terms incl labels and description. UUID needed? Furhter vocs can be setup through our own ontology.
	private static String initializationClassUri;
	private Set<Representation> representations;
	private DefinedTermBase kindOf;
	private Set<DefinedTermBase> generalizationOf;
	private DefinedTermBase partOf;
	private Set<DefinedTermBase> includes;
	private Set<Media> media;
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

	public String getInitializationClassUri(){
		return this.initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri    initializationClassUri
	 */
	public void setInitializationClassUri(String initializationClassUri){
		this.initializationClassUri = initializationClassUri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	@Transient
	public static DefinedTermBase getDefinedTermByUri(String uri){
		return null;
	}

	/**
	 * add new terms from a vocabulary to which uri points. By default this is the
	 * initializationClassUri
	 * 
	 * @param uri    uri
	 */
	public void addTermsFromInitializationClass(String uri){

	}

}