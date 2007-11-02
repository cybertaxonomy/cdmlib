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
 * workaround for enumerations, base type according to TDWG.
 * 
 * For linear ordering use partOf relation and BreadthFirst.
 * Default iterator order should therefore be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:03
 */
@Entity
public abstract class DefinedTermBase extends VersionableEntity {
	static Logger logger = Logger.getLogger(DefinedTermBase.class);

	//URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!
	@Description("URI used as an ID for the term. In the case of TDWG ontology derived terms the URL to the term!")
	private String uri;
	//The RDF ontology source defining the terms to be loaded when a database is created for the first time.
	//
	//Software can go and grap these terms incl labels and description. UUID needed? Furhter vocs can be setup through our
	//own ontology.
	@Description("The RDF ontology source defining the terms to be loaded when a database is created for the first time.
	
	Software can go and grap these terms incl labels and description. UUID needed? Furhter vocs can be setup through our own ontology.")
	private static final int initializationClassUri;
	private ArrayList representations;
	private DefinedTermBase kindOf;
	private ArrayList generalizationOf;
	private DefinedTermBase partOf;
	private ArrayList includes;
	private java.util.ArrayList media;

	public ArrayList getRepresentations(){
		return representations;
	}

	/**
	 * 
	 * @param representations
	 */
	public void setRepresentations(ArrayList representations){
		;
	}

	public DefinedTermBase getKindOf(){
		return kindOf;
	}

	/**
	 * 
	 * @param kindOf
	 */
	public void setKindOf(DefinedTermBase kindOf){
		;
	}

	public ArrayList getGeneralizationOf(){
		return generalizationOf;
	}

	/**
	 * 
	 * @param generalizationOf
	 */
	public void setGeneralizationOf(ArrayList generalizationOf){
		;
	}

	public DefinedTermBase getPartOf(){
		return partOf;
	}

	/**
	 * 
	 * @param partOf
	 */
	public void setPartOf(DefinedTermBase partOf){
		;
	}

	public ArrayList getIncludes(){
		return includes;
	}

	/**
	 * 
	 * @param includes
	 */
	public void setIncludes(ArrayList includes){
		;
	}

	public java.util.ArrayList getMedia(){
		return media;
	}

	/**
	 * 
	 * @param media
	 */
	public void setMedia(java.util.ArrayList media){
		;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param uri
	 */
	public void setUri(String uri){
		;
	}

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri
	 */
	public void setInitializationClassUri(initializationClassUri){
		;
	}

	/**
	 * 
	 * @param uri
	 */
	@Transient
	public static defined terms getDefinedTermByUri(String uri){
		return null;
	}

	/**
	 * add new terms from a vocabulary to which uri points. By default this is the
	 * initializationClassUri
	 * 
	 * @param uri
	 */
	public void addTermsFromInitializationClass(String uri){

	}

}