/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;

import java.util.List;
import java.util.UUID;

import org.jdom.Element;

/**
 * This interface provides a simplified interface to the API offered by the cdmlib-remote project.
 * 
 * @author n.hoffmann
 * @since Jul 16, 2010
 * @version 1.0
 */
public interface IXMLEntityFactory {

	/***************** Configuration methods *********************/
	
	/**
	 * Returns a list of classifications that are present in the CDM Server associated with this factory
	 * 
	 * @returna list of elements
	 */
	public List<Element> getClassifications();
	
	/**
	 * Returns taxonomically included taxa for a given taxon or classification.
	 * 
	 * @param treeNode
	 * @return a list of elements
	 */
	public List<Element> getChildNodes(Element treeNode);
	
	/**
	 * Returns the TaxonNode for the given uuid
	 * 
	 * @param taxonNodeUuid
	 * @return an element
	 */
	public Element getTaxonNode(UUID taxonNodeUuid);
	
	/*
	 * Returns the taxonNodes for a specific name
	 */
	public Element getTaxonNodesByName(String taxonName, String classification);
	
	/**
	 * Returns the {@link FeatureTree FeatureTrees} available with this CDM Server 
	 * 
	 * @return a list of elements
	 */
	public List<Element> getFeatureTrees();
	
	/**
	 * 
	 * 
	 * @return
	 */
	public Element getFeatureTree(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public Element getFeatureNode(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public Element getFeatureForFeatureNode(UUID uuid);
	
	/***************** Harvesting methods *********************/
	
	/**
	 * Retrieves to taxon associated with the given taxon node
	 * 
	 * @param taxonNodeElement
	 * @return an element
	 */
	public Element getTaxonForTaxonNode(Element taxonNodeElement);
	
	/**
	 * Fully initializes an accepted taxon
	 * 
	 * @param taxonElement
	 * @return an element
	 */
	public Element getAcceptedTaxonElement(Element taxonElement);
	
	/**
	 * Fully initializes a reference
	 * 
	 * @param referenceElement
	 * @return an element
	 */
	public List<Element> getReferences(Element referenceElement);
	
	/**
	 * Initializes the complete synonym for an accepted taxon
	 * 
	 * @param taxonElement
	 * @return a list of elements
	 */
	public List<Element> getSynonymy(Element taxonElement);
	
	/**
	 * Initialize type designations for a name elemt  
	 * 
	 * @param nameElement
	 * @return
	 */
	public List<Element> getTypeDesignations(Element nameElement);
	
	/**
	 * Initializes complete taxon descriptions for an accepted taxon
	 * 
	 * @param taxonElement
	 * @return a list of elements
	 */
	public Element getDescriptions(Element taxonElement);	
	
	/**
	 * Gets the PolytomousKey as a List of LinkedPolytomousKeyNodeRowDto objects.
	 * 
	 * @param taxonElement
	 * @return a list of elements
	 */
	public Element getPolytomousKey(Element taxonElement); 
	
	/**
	 * Gets the Media associated with a particular taxon
	 * @param taxonElement
	 * @return
	 */
	public List<Element> getMedia(Element taxonElement);
}
