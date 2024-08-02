/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author a.mueller
 * @since 23.10.2008
 */
public class SimpleSpecimen {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private GatheringEvent gatheringEvent;
	private FieldUnit fieldUnit;
	private DerivationEvent derivationEvent;
	private DerivedUnit specimen;
	private Collection collection;
	private TaxonName storedUnderName;
	private String titleCache;


	public static SimpleSpecimen NewInstance(){
		return new SimpleSpecimen();
	}

	private SimpleSpecimen(){
		gatheringEvent = GatheringEvent.NewInstance();

		//observation
		fieldUnit = FieldUnit.NewInstance();
		fieldUnit.setGatheringEvent(gatheringEvent);

		//derivationEvent
		derivationEvent = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
		derivationEvent.addOriginal(fieldUnit);

		//derivedUnit
		specimen = DerivedUnit.NewPreservedSpecimenInstance();
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

	public Collection getCollection() {
		return collection;
	}
	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public TaxonName getStoredUnderName() {
		return storedUnderName;
	}
	public void setStoredUnderName(TaxonName storedUnderName) {
		this.storedUnderName = storedUnderName;
	}

	public AgentBase getCollector() {
		return gatheringEvent.getCollector();
	}
	public void setCollector(AgentBase collector){
		gatheringEvent.setCollector(collector);
	}

	//TODO not fully clear if collectors number is meant to be the
	//   field number or the barcode/accessionNumber/catalogueNumber
	//see #2362
	public String getCollectorsNumber() {
		return specimen.getCollectorsNumber();
	}
	public void setCollectorsNumber(String collectorsNumber) {
		this.specimen.setCollectorsNumber(collectorsNumber);
	}

	public DerivedUnit getSpecimen() {
		return specimen;
	}
	public void setSpecimen(DerivedUnit specimen) {
		this.specimen = specimen;
	}

	public String getTitleCache() {
		return this.specimen.getTitleCache();
	}
	public void setTitleCache(String titleCache) {
		this.specimen.setTitleCache(titleCache, true);
	}
}