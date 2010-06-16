/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.taxonx;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.Specimen;

/**
 * @author a.mueller
 * @created 23.10.2008
 * @version 1.0
 */
public class SimpleSpecimen {
	private static final Logger logger = Logger.getLogger(SimpleSpecimen.class);
	
	private GatheringEvent gatheringEvent;
	private FieldObservation fieldObservation;
	private DerivationEvent derivationEvent;
	private Specimen specimen;
	private Collection collection;
	private TaxonNameBase<?, ?> storedUnderName;
	private String titleCache;

	
	public static SimpleSpecimen NewInstance(){
		return new SimpleSpecimen();
	}

	private SimpleSpecimen(){
		gatheringEvent = GatheringEvent.NewInstance();
		
		//observation
		fieldObservation = FieldObservation.NewInstance();
		fieldObservation.setGatheringEvent(gatheringEvent);
		
		//derivationEvent
		derivationEvent = DerivationEvent.NewInstance();
		derivationEvent.addOriginal(fieldObservation);
		
		//derivedUnit
		specimen = Specimen.NewInstance();
		derivationEvent.addDerivative(specimen);
	}
	
	public LanguageString getLocality(){
		return gatheringEvent.getLocality();
	}
	
	
	/**
	 * Sets the locality string in the default language
	 * @param locality
	 */
	public void setLocality(String locality){
		Language lang = Language.DEFAULT();
		LanguageString langString = LanguageString.NewInstance(locality, lang);
		setLocality(langString);
	}

	public void setLocality(LanguageString locality){
		gatheringEvent.setLocality(locality);
	}
	
	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(Collection collection) {
		this.collection = collection;
	}
	
		/**
	 * @return the storedUnderName
	 */
	public TaxonNameBase<?, ?> getStoredUnderName() {
		return storedUnderName;
	}

	/**
	 * @param storedUnderName the storedUnderName to set
	 */
	public void setStoredUnderName(TaxonNameBase<?, ?> storedUnderName) {
		this.storedUnderName = storedUnderName;
	}
	
	/**
	 * @return the collection
	 */
	public AgentBase getCollector() {
		return gatheringEvent.getCollector();
	}
	
	public void setCollector(AgentBase collector){
		gatheringEvent.setCollector(collector);
	}

	/**
	 * @return the collectorsNumber
	 */
	public String getCollectorsNumber() {
		return specimen.getCollectorsNumber();
	}

	/**
	 * @param collectorsNumber the collectorsNumber to set
	 */
	public void setCollectorsNumber(String collectorsNumber) {
		this.specimen.setCollectorsNumber(collectorsNumber);
	}
	

	/**
	 * @return the specimen
	 */
	public Specimen getSpecimen() {
		return specimen;
	}

	/**
	 * @param specimen the specimen to set
	 */
	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	/**
	 * @return the titleCache
	 */
	public String getTitleCache() {
		return this.specimen.getTitleCache();
	}

	/**
	 * @param titleCache the titleCache to set
	 */
	public void setTitleCache(String titleCache) {
		this.specimen.setTitleCache(titleCache, true);
	}
	
	
	
}
