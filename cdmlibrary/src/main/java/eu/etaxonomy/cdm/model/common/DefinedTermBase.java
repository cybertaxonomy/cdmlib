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

/**
 * workaround for enumerations, base type according to TDWG.
 * 
 * For linear ordering use partOf relation and BreadthFirst.
 * Default iterator order should therefore be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:14
 */
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
	private DefinedTermBase partOf;
	private ArrayList includes;
	private ArrayList representations;
	private DefinedTermBase kindOf;
	private ArrayList generalizationOf;
	private java.util.ArrayList media;

	public ArrayList getRepresentations(){
		return representations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRepresentations(ArrayList newVal){
		representations = newVal;
	}

	public DefinedTermBase getKindOf(){
		return kindOf;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setKindOf(DefinedTermBase newVal){
		kindOf = newVal;
	}

	public ArrayList getGeneralizationOf(){
		return generalizationOf;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setGeneralizationOf(ArrayList newVal){
		generalizationOf = newVal;
	}

	public DefinedTermBase getPartOf(){
		return partOf;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPartOf(DefinedTermBase newVal){
		partOf = newVal;
	}

	public ArrayList getIncludes(){
		return includes;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIncludes(ArrayList newVal){
		includes = newVal;
	}

	public java.util.ArrayList getMedia(){
		return media;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMedia(java.util.ArrayList newVal){
		media = newVal;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUri(String newVal){
		uri = newVal;
	}

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInitializationClassUri(newVal){
		initializationClassUri = newVal;
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