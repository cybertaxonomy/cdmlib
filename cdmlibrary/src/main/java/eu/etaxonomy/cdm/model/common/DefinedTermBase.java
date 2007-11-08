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
public abstract class DefinedTermBase extends VersionableEntity {
	static Logger logger = Logger.getLogger(DefinedTermBase.class);
	//URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!
	private String uri;
	//The RDF ontology source defining the terms to be loaded when a database is created for the first time.  Software can go
	//and grap these terms incl labels and description. UUID needed? Furhter vocs can be setup through our own ontology.
	private static String initializationClassUri;
	private ArrayList<Representation> representations;
	private DefinedTermBase kindOf;
	private ArrayList<DefinedTermBase> generalizationOf;
	private DefinedTermBase partOf;
	private ArrayList<DefinedTermBase> includes;
	private ArrayList<Media> media;

	public ArrayList<Representation> getRepresentations(){
		return this.representations;
	}

	/**
	 * 
	 * @param representations    representations
	 */
	public void setRepresentations(ArrayList representations){
		this.representations = representations;
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

	public ArrayList<DefinedTermBase> getGeneralizationOf(){
		return this.generalizationOf;
	}

	/**
	 * 
	 * @param generalizationOf    generalizationOf
	 */
	public void setGeneralizationOf(ArrayList generalizationOf){
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

	public ArrayList<DefinedTermBase> getIncludes(){
		return this.includes;
	}

	/**
	 * 
	 * @param includes    includes
	 */
	public void setIncludes(ArrayList includes){
		this.includes = includes;
	}

	public ArrayList<Media> getMedia(){
		return this.media;
	}

	/**
	 * 
	 * @param media    media
	 */
	public void setMedia(ArrayList media){
		this.media = media;
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