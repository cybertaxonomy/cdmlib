/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
@Component
public class Abcd206Import extends
SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState>
implements ICdmIO<Abcd206ImportState> {
	private static final Logger logger = Logger.getLogger(Abcd206Import.class);
	private static String prefix = "";

	private Classification classification = null;
	private Reference<?> ref = null;

	private Abcd206ImportState abcdstate;
	private Abcd206DataHolder dataHolder;
	private DerivedUnitBase derivedUnitBase;

	private TransactionStatus tx;

	private Abcd206XMLFieldGetter abcdFileGetter;

	public Abcd206Import() {
		super();
	}

	@Override
	protected boolean doCheck(Abcd206ImportState state) {
		logger.warn("Checking not yet implemented for "
				+ this.getClass().getSimpleName());
		this.abcdstate = state;
		return true;
	}

	/**
	 * getClassification : get the classification declared in the ImportState
	 * 
	 * @param state
	 * @return
	 */
	private void setClassification(Abcd206ImportState state) {
		if (classification == null) {
			String name = state.getConfig().getClassificationName();

			classification = Classification.NewInstance(name, ref,
					Language.DEFAULT());
			if (state.getConfig().getClassificationUuid() != null) {
				classification.setUuid(state.getConfig()
						.getClassificationUuid());
			}
			getClassificationService().saveOrUpdate(classification);
			refreshTransaction();
		}
	}

	@Override
	public void doInvoke(Abcd206ImportState state) {
		abcdstate = state;
		tx = startTransaction();

		logger.info("INVOKE Specimen Import from ABCD2.06 XML ");
		URI sourceName = this.abcdstate.getConfig().getSource();
		NodeList unitsList = getUnitsNodeList(sourceName);

		this.abcdstate.getConfig().getClassificationName();
		this.abcdstate.getConfig().getClassificationUuid();

		ref = this.abcdstate.getConfig().getSourceReference();
		setClassification(abcdstate);

		if (unitsList != null) {
			String message = "nb units to insert: " + unitsList.getLength();
			logger.info(message);
			updateProgress(this.abcdstate, message);
			dataHolder = new Abcd206DataHolder();

			abcdFileGetter = new Abcd206XMLFieldGetter(dataHolder, prefix);

			for (int i = 0; i < unitsList.getLength(); i++) {

				// logger.info("UNIT NUMBER " + i);
				this.setUnitPropertiesXML((Element) unitsList.item(i));
				refreshTransaction();
				this.handleSingleUnit(i);

				// compare the ABCD elements added in to the CDM and the
				// unhandled ABCD elements
				compareABCDtoCDM(sourceName, dataHolder.knownABCDelements);

				// reset the ABCD elements added in CDM
				// knownABCDelements = new ArrayList<String>();
				dataHolder.allABCDelements = new HashMap<String, String>();

				refreshTransaction();

			}
		}
		commitTransaction(tx);
		return;

	}

	/*
	 * Return the list of root nodes for an ABCD 2.06 XML file
	 * 
	 * @param fileName: the file's location
	 * 
	 * @return the list of root nodes ("Unit")
	 */
	protected NodeList getUnitsNodeList(URI urlFileName) {
		NodeList unitList = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			URL url = urlFileName.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream) o;
			Document document = builder.parse(is);
			Element root = document.getDocumentElement();
			// logger.info("root nodename " + root.getNodeName());
			unitList = root.getElementsByTagName("Unit");
			if (unitList.getLength() == 0) {
				unitList = root.getElementsByTagName("abcd:Unit");
				prefix = "abcd:";
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		return unitList;
	}

	/*
	 * Stores the unit with its Gathering informations in the CDM
	 */
	private void handleSingleUnit(int i) {
		logger.info("handleSingleUnit");

		try {
			updateProgress(this.abcdstate, "Importing data for unit: "
					+ dataHolder.unitID);


			// create facade
			DerivedUnitFacade derivedUnitFacade = getFacade();
			derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

			//			getOccurrenceService().saveOrUpdate(derivedUnitBase);
			//			refreshTransaction();

			/**
			 * GATHERING EVENT
			 */

			// gathering event
			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(
					getTermService(), dataHolder.locality, dataHolder.languageIso,
					dataHolder.longitude, dataHolder.latitude,
					dataHolder.gatheringAgentList);

			// country
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(
					dataHolder.isocountry, dataHolder.country,
					getOccurrenceService());
			NamedArea areaCountry = unitsGatheringArea.getArea();

			// other areas
			unitsGatheringArea = new UnitsGatheringArea(dataHolder.namedAreaList);
			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
			for (NamedArea namedArea : nas) {
				unitsGatheringEvent.addArea(namedArea);
			}

			// copy gathering event to facade
			GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
			derivedUnitFacade.setLocality(gatheringEvent.getLocality());
			derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
			derivedUnitFacade.setCollector(gatheringEvent.getCollector());
			derivedUnitFacade.setCountry(areaCountry);
			derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());

			// TODO exsiccatum

			// add fieldNumber
			derivedUnitFacade.setFieldNumber(dataHolder.fieldNumber);

			// //add Multimedia URLs
			if (dataHolder.multimediaObjects.size() != -1) {
				for (String multimediaObject : dataHolder.multimediaObjects) {
					Media media;
					try {
						media = getImageMedia(multimediaObject, READ_MEDIA_DATA,
								false);
						derivedUnitFacade.addDerivedUnitMedia(media);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

			/**
			 * merge AND STORE DATA
			 */
			getTermService().saveOrUpdate(areaCountry);// TODO save area sooner

			for (NamedArea area : nas) {
				getTermService().saveOrUpdate(area);// merge it sooner (foreach
				// area)
			}
			getTermService().saveLanguageData(unitsGatheringEvent.getLocality());

			// handle collection data
			setCollectionData(this.abcdstate.getConfig(), derivedUnitFacade);


			getOccurrenceService().saveOrUpdate(derivedUnitBase);
			refreshTransaction();

			// getOccurrenceService().saveOrUpdate(derivedUnitFacade.innerFieldObservation());

			// handle identifications
			handleIdentifications(this.abcdstate.getConfig(), derivedUnitFacade);

			/*
			 * getOccurrenceService().saveOrUpdate(
			 * derivedUnitFacade.innerFieldObservation());
			 */


			logger.info("saved ABCD specimen ...");

		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			this.abcdstate.setUnsuccessfull();
		}

		return;
	}

	/**
	 * setCollectionData : store the collection object into the
	 * derivedUnitFacade
	 * 
	 * @param config
	 */
	private void setCollectionData(Abcd206ImportConfigurator config,
			DerivedUnitFacade derivedUnitFacade) {
		// set catalogue number (unitID)
		derivedUnitFacade.setCatalogNumber(dataHolder.unitID);
		derivedUnitFacade.setAccessionNumber(dataHolder.accessionNumber);
		// derivedUnitFacade.setCollectorsNumber(dataHolder.collectorsNumber);

		/**
		 * INSTITUTION & COLLECTION
		 */
		// manage institution
		Institution institution = this.getInstitution(
				dataHolder.institutionCode, config);
		// manage collection
		Collection collection = this.getCollection(dataHolder.collectionCode,
				institution, config);
		// link specimen & collection
		derivedUnitFacade.setCollection(collection);
	}

	/**
	 * getFacade : get the DerivedUnitFacade based on the recordBasis
	 * 
	 * @return DerivedUnitFacade
	 */
	private DerivedUnitFacade getFacade() {
		// logger.info("GETFACADE");
		/**
		 * SPECIMEN OR OBSERVATION OR LIVING
		 */
		// DerivedUnitBase derivedThing = null;
		DerivedUnitType type = null;

		// create specimen
		if (dataHolder.recordBasis != null) {
			if (dataHolder.recordBasis.toLowerCase().startsWith("s")
					|| dataHolder.recordBasis.toLowerCase()
					.contains("specimen")) {// specimen
				type = DerivedUnitType.Specimen;
			}
			if (dataHolder.recordBasis.toLowerCase().startsWith("o")) {
				type = DerivedUnitType.Observation;
			}
			if (dataHolder.recordBasis.toLowerCase().contains("fossil"))
				type = DerivedUnitType.Fossil;

			if (dataHolder.recordBasis.toLowerCase().startsWith("l")) {
				type = DerivedUnitType.LivingBeing;
			}
			if (type == null) {
				logger.info("The basis of record does not seem to be known: "
						+ dataHolder.recordBasis);
				type = DerivedUnitType.DerivedUnit;
			}
			// TODO fossils?
		} else {
			logger.info("The basis of record is null");
			type = DerivedUnitType.DerivedUnit;
		}
		DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade
				.NewInstance(type);
		return derivedUnitFacade;
	}

	/*
	 * Store the unit's properties into variables Look which unit is the
	 * preferred one Look what kind of name it is supposed to be, for the
	 * parsing (Botanical, Zoological)
	 * 
	 * @param racine: the root node for a single unit
	 */
	private void setUnitPropertiesXML(Element root) {
		try {
			NodeList group;

			// try{afficherInfos(racine, 0);}catch (Exception e)
			// {logger.info(e);}
			group = root.getChildNodes();
			// logger.info("prefix et ident : "+prefix+"Identifications");
			// logger.info("ABCD ELEMENT not stored: "+group.item(i).getNodeName().toString()+" - value: "+group.item(i).getTextContent());
			for (int i = 0; i < group.getLength(); i++) {
				if (group.item(i).getNodeName()
						.equals(prefix + "Identifications")) {
					group = group.item(i).getChildNodes();
					break;
				}
			}
			dataHolder.identificationList = new ArrayList<String>();
			dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
			dataHolder.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
			dataHolder.referenceList = new ArrayList<String>();
			dataHolder.multimediaObjects = new ArrayList<String>();

			abcdFileGetter.getScientificNames(group);
			abcdFileGetter.getType(root);

			// logger.info("this.identificationList "+this.identificationList.toString());
			abcdFileGetter.getIDs(root);
			abcdFileGetter.getRecordBasis(root);
			abcdFileGetter.getMultimedia(root);
			abcdFileGetter.getNumbers(root);
			abcdFileGetter.getGeolocation(root);
			abcdFileGetter.getGatheringPeople(root);
			boolean referencefound = abcdFileGetter.getReferences(root);
			if (!referencefound) {
				dataHolder.referenceList.add(ref.getTitleCache());
			}

		} catch (Exception e) {
			logger.info("Error occured while parsing XML file" + e);
		}
	}

	private Institution getInstitution(String institutionCode,
			Abcd206ImportConfigurator config) {
		Institution institution;
		List<Institution> institutions;
		try {
			// logger.info(dataHolder.institutionCode);
			institutions = getAgentService().searchInstitutionByCode(
					dataHolder.institutionCode);
		} catch (Exception e) {
			institutions = new ArrayList<Institution>();
		}
		if (institutions.size() == 0 || !config.isReUseExistingMetadata()) {
			// logger.info("Institution (agent) unknown or not allowed to reuse existing metadata");
			// create institution
			institution = Institution.NewInstance();
			institution.setCode(dataHolder.institutionCode);
		} else {
			// logger.info("Institution (agent) already in the db");
			institution = institutions.get(0);
		}
		// logger.info("getinstitution " + institution.toString());
		return institution;
	}

	/*
	 * Look if the Collection does already exists
	 * 
	 * @param collectionCode: a string
	 * 
	 * @param institution: the current Institution
	 * 
	 * @param app
	 * 
	 * @return the Collection (existing or new)
	 */
	private Collection getCollection(String collectionCode,
			Institution institution, Abcd206ImportConfigurator config) {
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try {
			collections = getCollectionService().searchByCode(
					dataHolder.collectionCode);
		} catch (Exception e) {
			collections = new ArrayList<Collection>();
		}
		if (collections.size() == 0 || !config.isReUseExistingMetadata()) {
			// logger.info("Collection not found or do not reuse existing metadata  "
			//		+ dataHolder.collectionCode);
			// create new collection
			collection.setCode(dataHolder.collectionCode);
			collection.setCodeStandard("GBIF");
			collection.setInstitute(institution);
		} else {
			boolean collectionFound = false;
			for (int i = 0; i < collections.size(); i++) {
				collection = collections.get(i);
				try {
					if (collection.getInstitute().getCode()
							.equalsIgnoreCase(institution.getCode())) {
						// found a collection with the same code and the same
						// institution
						collectionFound = true;
						break;
					}
				} catch (NullPointerException e) {
				}
			}
			if (!collectionFound) {
				collection.setCode(dataHolder.collectionCode);
				collection.setCodeStandard("GBIF");
				collection.setInstitute(institution);
			}

		}
		return collection;
	}

	/**
	 * join DeterminationEvent to the Taxon Object
	 * 
	 * @param taxon
	 *            : current Taxon Object
	 * @param preferredFlag
	 *            : preferred name, boolean
	 * @param config
	 *            : current ABCD Import configurator
	 */

	private void linkDeterminationEvent(Taxon taxon, boolean preferredFlag,
			Abcd206ImportConfigurator config, 
			DerivedUnitFacade derivedFacade) {
		// logger.info("start linkdetermination with taxon:" + taxon.getUuid()+", "+taxon);

		refreshTransaction();


		try {
			taxon = (Taxon) getTaxonService().find(taxon.getUuid());
		} catch (Exception e) {
			// logger.info("taxon uptodate");
		}

		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
		determinationEvent.setTaxon(taxon);
		determinationEvent.setPreferredFlag(preferredFlag);
		
		determinationEvent.setIdentifiedUnit(derivedUnitBase);

		derivedUnitBase.addDetermination(determinationEvent);
		refreshTransaction();
		////		
		//		Set<DerivationEvent> dset = derivedUnitBase.getDeterminations();
		//		logger.info("dset: "+dset);

		try {
			// logger.info("NB TYPES INFO: "+ dataHolder.statusList.size());
			for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : dataHolder.statusList) {
				if (specimenTypeDesignationstatus != null) {
					// logger.info("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
					try {
						taxon = (Taxon) getTaxonService().find(taxon.getUuid());
						
					} catch (Exception e) {
						// logger.info("taxon uptodate");
					}
					specimenTypeDesignationstatus = (SpecimenTypeDesignationStatus) getTermService().find(specimenTypeDesignationstatus.getUuid());
					//Designation
					TaxonNameBase<?,?> name = taxon.getName();
					SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();
					
					designation.setTypeStatus(specimenTypeDesignationstatus);
					designation.setTypeSpecimen(derivedUnitBase);
					name.addTypeDesignation(designation, true);
					refreshTransaction();

				}
			}
		} catch (Exception e) {
			logger.warn("PB addding SpecimenType " + e);
		}

		for (String strReference : dataHolder.referenceList) {
			Reference<?> reference = ReferenceFactory.newGeneric();
			reference.setTitleCache(strReference, true);
            getReferenceService().saveOrUpdate(reference);

			determinationEvent.addReference(reference);
		}

        getOccurrenceService().saveOrUpdate(derivedUnitBase);
		refreshTransaction();


		if (config.isDoCreateIndividualsAssociations()) {
			// logger.info("isDoCreateIndividualsAssociations");

			// TaxonDescription taxonDescription = null;
			if (config.isDoMatchToExistingDescription()) {
				logger.warn("The import option 'DoMatchToExistingDescription' is not yet implemented.");
			} else {
				// UUID taxonDescriptionUUID =
				// config.getTaxonToDescriptionMap().get(taxon.getUuid()); //
				// rather
				// logger.info("UUID taxondescription "+taxonDescriptionUUID);
				// if (taxonDescriptionUUID != null) {
				// try{
				// taxonDescription = (TaxonDescription)
				// getDescriptionService().load(taxonDescriptionUUID);
				// }catch(Exception
				// e){logger.warn("BLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				// taxonDescription = TaxonDescription.NewInstance(taxon);
				// }
				// }
				// if (taxonDescription == null) {
				// taxonDescription = TaxonDescription.NewInstance(taxon);
				// config.getTaxonToDescriptionMap().put(taxon.getUuid(),taxonDescription.getUuid());
				//
				// // taxonDescriptionUUID = config.getTaxonToDescriptionMap()
				// .get(taxon.getUuid());
				// }

				/*
				 * Set<TaxonDescription> tds = taxon.getDescriptions();
				 * TaxonDescription td = null; if (tds.size() >0) { td=
				 * tds.iterator().next(); taxonDescription =
				 * CdmBase.deproxy(td,TaxonDescription.class); }
				 * 
				 * if (taxonDescription == null){ // taxonDescription =
				 * TaxonDescription.NewInstance(); //
				 * logger.info("taxondescr "+taxonDescription.getUuid()); //
				 * //taxonDescription.setTaxon(taxon);
				 * //taxon.addDescription(taxonDescription);
				 * //getTaxonService().merge(taxon);
				 * 
				 * /*}
				 */
				// taxonDescription.setTaxon(taxon);
				// }
			}

			makeIndividualsAssociation(taxon, determinationEvent);

		}
	}

	private void makeIndividualsAssociation(Taxon taxon, DeterminationEvent determinationEvent) {

		TaxonDescription taxonDescription = TaxonDescription.NewInstance();
		taxonDescription.setTitleCache(ref.getTitleCache(), true);

		getDescriptionService().saveOrUpdate(taxonDescription);

		refreshTransaction();

		try{taxon = (Taxon) getTaxonService().find(taxon.getUuid());
		}catch(Exception e){
			//logger.info("taxon uptodate");
			}

		taxonDescription = (TaxonDescription) getDescriptionService().find(taxonDescription.getUuid());
		taxon.addDescription(taxonDescription);

		IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
		Feature feature = makeFeature(derivedUnitBase);
		indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
		indAssociation.setFeature(feature);

		for (Reference<?> citation : determinationEvent.getReferences()) {
			indAssociation.addSource(DescriptionElementSource.NewInstance(null,
					null, citation, null));
		}

		taxonDescription.addElement(indAssociation);

		getDescriptionService().saveOrUpdate(taxonDescription);

		getTaxonService().saveOrUpdate(taxon);
	}

	private Feature makeFeature(SpecimenOrObservationBase unit) {
		if (unit.isInstanceOf(DerivedUnit.class)) {
			return Feature.INDIVIDUALS_ASSOCIATION();
		} else if (unit.isInstanceOf(FieldObservation.class)
				|| unit.isInstanceOf(Observation.class)) {
			return Feature.OBSERVATION();
		} else if (unit.isInstanceOf(Fossil.class)
				|| unit.isInstanceOf(LivingBeing.class)
				|| unit.isInstanceOf(Specimen.class)) {
			return Feature.SPECIMEN();
		}
		logger.warn("No feature defined for derived unit class: "
				+ unit.getClass().getSimpleName());
		return null;
	}

	private void refreshTransaction(){
		commitTransaction(tx);
		tx = startTransaction();
		ref = getReferenceService().find(ref.getUuid());
		classification = getClassificationService().find(classification.getUuid());
		try{
			derivedUnitBase = (DerivedUnitBase) getOccurrenceService().find(derivedUnitBase.getUuid());
		}catch(Exception e){
			//logger.warn("derivedunit up to date or not created yet");
			}
	}

	/**
	 * getParentTaxon : get the taxonomic hierarchy for the current Taxon
	 * 
	 * @param taxon
	 * @param taxonName
	 * @param originalName
	 * @param config
	 * @return a map with the parenttaxon and the parenttaxonname
	 */
	private HashMap<Taxon, NonViralName<?>> getParentTaxon(Taxon taxon,
			NonViralName<?> taxonName, NonViralName<?> originalName,
			Abcd206ImportConfigurator config) {

		Taxon parenttaxon = null;
		NonViralName<?> parentName = null;
		List<TaxonBase> c = null;

		List<String> highername = new ArrayList<String>();
		Rank higherrank = null;
		Rank taxonrank = taxonName.getRank();

		// logger.info("getParentTaxon childname " + taxonName.getFullTitleCache()
		//		+ ", rank " + taxonrank + ", originalname "
		//		+ originalName.getFullTitleCache());

		HashMap<Taxon, NonViralName<?>> map = new HashMap<Taxon, NonViralName<?>>();


		refreshTransaction();

		taxon = (Taxon) getTaxonService().find(taxon.getUuid());

		if (taxonrank.isGenus()) {
			// to change and add test DoReusetaxa
			for (TaxonNode p : classification.getAllNodes()) {
				// logger.info("p UUID "+p.getUuid().toString());
				if (classification.getTopmostNode(p.getTaxon()) == null) {
					// logger.info("taxon1 "+p.getTaxon().getTitleCache());
					// logger.info("taxon2 "+taxon.getTitleCache());
					if (taxon.getTitleCache().contains(p.getTaxon().getTitleCache().split("sec. " + ref)[0])) {
						this.addParentChild(p.getTaxon(), taxon);

						refreshTransaction();
						taxon = (Taxon) getTaxonService().find(taxon.getUuid());

						break;
					}
				}// else
				// logger.info("getTopMostNode "+classification.getTopmostNode(p.getTaxon()));
			}
			// add the genus to the root of the classification
			TaxonNode p = this.addChildTaxon(taxon);
			this.addChildNode(p);

			refreshTransaction();

			taxon = (Taxon) getTaxonService().find(taxon.getUuid());

			return map;
		}

		if (taxonrank.isInfraGeneric()) {
			// logger.info("isInfrageneric");
			highername.add(originalName.getGenusOrUninomial());
			higherrank = Rank.GENUS();
		}

		if (taxonrank.isSpecies()) {
			// logger.info("isSpecies");
			if (originalName.getGenusOrUninomial() != null) {
				highername.add(originalName.getGenusOrUninomial());
				higherrank = Rank.GENUS();
			} else {
				highername.add(originalName.getFullTitleCache().trim()
						.split(" ")[0]);
				higherrank = Rank.GENUS();
			}
			if (originalName.getInfraGenericEpithet() != null) {
				highername.add(originalName.getInfraGenericEpithet());
				higherrank = Rank.INFRAGENUS();
			}
		}

		if (taxonrank.isInfraSpecific()) {
			// logger.info("isInfraSpecies");
			if (originalName.getGenusOrUninomial() != null)
				highername.add(originalName.getGenusOrUninomial());
			if (originalName.getInfraGenericEpithet() != null) {
				highername.add(originalName.getInfraGenericEpithet());
			}
			if (originalName.getSpecificEpithet() != null)
				highername.add(originalName.getSpecificEpithet());
			higherrank = Rank.SPECIES();
		}

		String highernamestr = StringUtils.join(highername.iterator(), " ")
				.split("sec. " + ref.getTitleCache())[0].trim();
		// logger.info("higherNamest :: " + highernamestr);
		if (config.isDoReUseTaxon() && highername != null
				&& highername.size() > 0 && highernamestr != "") {
			boolean parentFound = false;
			try {
				c = getTaxonService().searchTaxaByName(highernamestr, ref);

				for (TaxonBase b : c) {
					parenttaxon = (Taxon) b;
					Iterator it = parenttaxon.getTaxonNodes().iterator();
					// logger.warn("ICI2");
					while (it.hasNext()) {
						TaxonNode tmpNode = (TaxonNode) it.next();
						Taxon tmp = tmpNode.getTaxon();
						if (tmp.getTitleCache().split(
								"sec. " + ref.getTitleCache())[0].trim()
								.equalsIgnoreCase(highernamestr)) {
							parenttaxon = tmp;
							parentName = (NonViralName<?>) b.getName();
							parentFound = true;
						}
					}
//					if (!parentFound)
//						logger.info("parent pas trouvé");
//					else
//						logger.info("parent trouvé "
//								+ parenttaxon.getTitleCache());

				}
			} catch (Exception e) {
				logger.info("Problem while trying to reuse existing taxon" + e);
				parenttaxon = null;
			}
			if (!parentFound)
				parenttaxon = null;
		}

		if ((parenttaxon == null && highername != null && highername.size() > 0 && highernamestr != "")
				|| !config.isDoReUseTaxon()) {
			// logger.info("ICI BIS");
			parentName = NonViralName.NewInstance(null);
			parentName.setFullTitleCache(highernamestr);
			parentName.setNameCache(highernamestr);
			parentName.setRank(higherrank);
			if (higherrank == Rank.GENUS())
				parentName.setGenusOrUninomial(highernamestr.split(" ")[0]);

			getNameService().save(parentName);
			parenttaxon = Taxon.NewInstance(parentName, ref);
			getTaxonService().save(parenttaxon);

		}

		map.put(parenttaxon, parentName);
		return map;
		// return parenttaxon;
	}

	/**
	 * add the ParenTaxon to the current Classification, link it with the
	 * "child" taxon
	 * 
	 * @param child
	 * @param childName
	 * @param config
	 * @return the current child that was set as inputparameter (needed??)
	 */
	private Taxon addParentTaxon(Taxon child, NonViralName<?> childName,
			Abcd206ImportConfigurator config) {

		refreshTransaction();

		NonViralName<?> originalName = childName;
		HashMap<Taxon, Taxon> map = new HashMap<Taxon, Taxon>();
		HashMap<Taxon, NonViralName<?>> maptn = new HashMap<Taxon, NonViralName<?>>();

		HashMap<Taxon, NonViralName<?>> tmpmaptn = getParentTaxon(child,
				childName, originalName, config);

		Taxon tmpparent = (Taxon) getTaxonService().find(
				tmpmaptn.keySet().iterator().next().getUuid());
		child = (Taxon) getTaxonService().find(child.getUuid());
		NonViralName<?> parentname = tmpmaptn.get(tmpparent);

		map.put(tmpparent, child);
		maptn.put(child, childName);
		maptn.put(tmpparent, parentname);

		while (!tmpparent.getTitleCache().split("sec. " + ref.getTitleCache())[0]
				.trim().equalsIgnoreCase(
						child.getTitleCache().trim()
						.split("sec. " + ref.getTitleCache())[0])
						&& childName != null && tmpmaptn.keySet().size() > 0) {
			// logger.info("tmpparent.getTitleCache()  "
			//		+ tmpparent.getTitleCache());
			// logger.info("child.getTitleCache() " + child.getTitleCache());
			child = (Taxon) getTaxonService().find(tmpparent.getUuid());
			childName = maptn.get(child);
			// if (childName == null) {
			// logger.warn("OUPS " + tmpparent.getTitleCache());
			// }
			if (childName != null) {
				tmpmaptn = getParentTaxon(child, childName, originalName,
						config);
				try {
					if (tmpmaptn.keySet().size() > 0) {
						tmpparent = (Taxon) getTaxonService().find(
								tmpmaptn.keySet().iterator().next().getUuid());
						parentname = tmpmaptn.get(tmpparent);

						map.put(tmpparent, child);
						maptn.put(tmpparent, parentname);

						for (Taxon elt : map.keySet()) {
							NonViralName<?> t = maptn.get(elt);
							if (parentname.getRank() == Rank.GENUS())
								t.setGenusOrUninomial(parentname
										.getTitleCache());
							if (parentname.getRank() == Rank.SPECIES())
								t.setSpecificEpithet(parentname.getTitleCache());
							maptn.remove(elt);
							maptn.put(elt, t);
						}

					}

				} catch (Exception e) {
					logger.warn("ohooooooooooooooooooooooooooooooooooooooo "
							+ e);
				}
			}

		}

		// last child == higher rank found
		tmpparent = child;

		refreshTransaction();

		tmpparent = (Taxon) getTaxonService().find(tmpparent.getUuid());

		for (TaxonNode p : classification.getAllNodes()) {
			// logger.warn("ICI3");
			// logger.warn("ptmpparent UUID "+p.getUuid().toString());
			if (classification.getTopmostNode(p.getTaxon()) == null) {
				if (tmpparent.getTitleCache().contains(p.getTaxon().getTitleCache().split("sec. " + ref)[0])) {
					tmpparent = (Taxon) getTaxonService().find(p.getTaxon().getUuid());
					this.addParentChild(tmpparent, null);
					map.put(tmpparent, child);
					break;
				}
			}
		}

		ArrayList<Taxon> taxondone = new ArrayList<Taxon>();
		taxondone.add(tmpparent);

		refreshTransaction();

		TaxonNode childNode =null;
		while (taxondone.size() < map.size() + 1) {
			boolean exists = false;
			boolean childadded = false;
			tmpparent = (Taxon) getTaxonService().find(tmpparent.getUuid());
			child = (Taxon) getTaxonService().find(map.get(tmpparent).getUuid());


			if(child.getTaxonNodes().size()==0){
				childNode = this.addChildTaxon(child);
				this.addChildNode(childNode);
				childadded = true;
			}

			if (childadded)
				refreshTransaction();
			//			
			tmpparent = (Taxon) getTaxonService().find(tmpparent.getUuid());
			child = (Taxon) getTaxonService().find(map.get(tmpparent).getUuid());

			Set<TaxonNode> childnodes = classification.getChildNodes();

			List<TaxonNode> myList = new CopyOnWriteArrayList<TaxonNode>();
			for (TaxonNode n :childnodes){
				myList.add(n);
			}
			Iterator it = myList.iterator();
			while (it.hasNext()){
				TaxonNode tn2 = getTaxonNodeService().find(((TaxonNode) it.next()).getUuid());
				Taxon t2 = (Taxon) getTaxonService().find(tn2.getTaxon().getUuid());

				if (t2.getUuid().equals(tmpparent.getUuid())){
					if (tmpparent.getTaxonNodes().size()==0){
						//						logger.warn("A");
						this.addParentChild(t2,getTaxonNodeService().find(childNode.getUuid()).getTaxon());
					}
					else{
						//						logger.warn("B");
						TaxonNode tn = getTaxonNodeService().find(child.getTaxonNodes().iterator().next().getUuid());
						TaxonNode node = this.addChildNode(tn);
//						logger.warn("LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+tn.getTaxon());
						this.addParentChild(t2,node.getTaxon());

					}
					exists=true;
				}
			}
			refreshTransaction();

			try {
				if (tmpparent != child && !exists) {
					this.addParentChild(tmpparent, child);
				}
				tmpparent = child;
				taxondone.add(child);
			} catch (Exception e) {
				logger.warn("PB :" + e);
				logger.warn("tmpparent " + tmpparent);
				logger.warn("child " + child);
				System.exit(0);
			}
		}

		refreshTransaction();
		return child;
	}

	private TaxonNode addChildNode(TaxonNode childNode){
		TaxonNode re =null;
		boolean exists=false;
		if (abcdstate.getConfig().isDoReUseTaxon()){
			Taxon taxon = childNode.getTaxon();
			Set<TaxonNode> allNodes = classification.getAllNodes();
			Taxon tmp;
			if (allNodes.size()>0)
				for (TaxonNode tn:allNodes){
					tmp = tn.getTaxon();
					if (tmp.equals(taxon)){
						re= tn;
						exists=true;
						break;
					}
				}
		}
		if(!abcdstate.getConfig().isDoReUseTaxon() || !exists){
			refreshTransaction();
			childNode = getTaxonNodeService().find(childNode.getUuid());
			re= classification.addChildNode(childNode, ref, "", null);
		}
		return re;
	}

	private void addParentChild(Taxon parent, Taxon child){
		//logger.info("ADDPARENTCHILD");
		boolean exists = false;
		Taxon taxonFromHiber = null;
		if (abcdstate.getConfig().isDoReUseTaxon()){
			Set<TaxonNode> allNodes = classification.getAllNodes();	
			Taxon tmp;
			if (allNodes.size()>0)
				for (TaxonNode tn:allNodes){
					tmp = tn.getTaxon();
					if (tmp.equals(parent)){
						taxonFromHiber = tmp;
					}
				}
			if (taxonFromHiber != null){
				Set<TaxonNode> children = taxonFromHiber.getTaxonNodes();
				if (children.size()>0)
					for (TaxonNode cn:children){
						Taxon tmpt = cn.getTaxon();
						if (tmpt.equals(child))
							exists=true;
					}
			}
		}
		if (!abcdstate.getConfig().isDoReUseTaxon() || !exists){
			refreshTransaction();
			parent = (Taxon) getTaxonService().find(parent.getUuid());
			child = (Taxon) getTaxonService().find(child.getUuid());
			classification.addParentChild(parent,child, ref, null);
		}
	}

	private TaxonNode addChildTaxon(Taxon child){
		TaxonNode re =null;
		boolean exists=false;
		if (abcdstate.getConfig().isDoReUseTaxon()){
			Set<TaxonNode> allNodes = classification.getAllNodes();	
			Taxon tmp;
			if (allNodes.size()>0)
				for (TaxonNode tn:allNodes){
					tmp = tn.getTaxon();
					if (tmp.equals(child)){
						re= tn;
						exists=true;
						break;
					}
				}
		}
		if(!abcdstate.getConfig().isDoReUseTaxon() || !exists){
			refreshTransaction();
			child = (Taxon) getTaxonService().find(child.getUuid());
			re= classification.addChildTaxon(child, ref, "", null);
		}
		return re;
	}

	/**
	 * getTaxon : search for an existing taxon in the database, for the same
	 * reference
	 * 
	 * @param config
	 * @param scientificName
	 * @param taxonName
	 * @return
	 */
	private Taxon getTaxon(Abcd206ImportConfigurator config,
			String scientificName, NonViralName taxonName) {
		Taxon taxon = null;
		List<TaxonBase> c = null;

		if (config.isDoMatchTaxa()) {
			taxon = getTaxonService().findBestMatchingTaxon(scientificName);
		}

		if (taxon == null && config.isDoReUseTaxon()) {
			try {
				c = getTaxonService().searchTaxaByName(scientificName, ref);
				for (TaxonBase b : c) {
					taxon = (Taxon) b;
				}
			} catch (Exception e) {
				logger.info("does not work here either " + e);
				taxon = null;
			}
		} else {
			//logger.info("Matching to existing Taxon : " + taxon.getTitleCache());
		}

		if (!config.isDoReUseTaxon() && taxon == null) {
			getNameService().saveOrUpdate(taxonName);
			taxon = Taxon.NewInstance(taxonName, ref);
			getTaxonService().save(taxon);
		}
		if (taxon == null) {
			getNameService().saveOrUpdate(taxonName);
			taxon = Taxon.NewInstance(taxonName, ref);
			getTaxonService().save(taxon);

		}
//		logger.info("getTaxonService : "
//				+ getTaxonService().find(taxon.getUuid()));
		return (Taxon) getTaxonService().find(taxon.getUuid());
		// return taxonFromUUID()
	}

	/**
	 * HandleIdentifications : get the scientific names present in the ABCD
	 * document and store link them with the observation/specimen data
	 * 
	 * @param config
	 */
	private void handleIdentifications(Abcd206ImportConfigurator config,
			DerivedUnitFacade derivedUnitFacade) {

		// commitTransaction(tx);
		// tx =startTransaction();

		String fullScientificNameString;
		Taxon taxon = null;
		NonViralName<?> taxonName = null;
		Rank.GENUS();
		Rank.FAMILY();

		new HashMap<String, Taxon>();
		HashMap<Integer, Taxon> preferredtaxontoinsert = new HashMap<Integer, Taxon>();
		HashMap<Integer, NonViralName<?>> preferredtaxonnametoinsert = new HashMap<Integer, NonViralName<?>>();
		HashMap<Integer, Taxon> taxontoinsert = new HashMap<Integer, Taxon>();
		HashMap<Integer, NonViralName<?>> taxonnametoinsert = new HashMap<Integer, NonViralName<?>>();

		String scientificName = "";
		boolean preferredFlag = false;
		boolean onepreferred = false;

		if (dataHolder.nomenclatureCode == "")
			dataHolder.nomenclatureCode = config.getNomenclaturalCode()
			.toString();

		for (int i = 0; i < dataHolder.identificationList.size(); i++) {

			fullScientificNameString = dataHolder.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(
					" et ", " & ");

			if (fullScientificNameString.indexOf("_preferred_") != -1) {
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1]
						.split("_code_")[0];
				if (pTmp.equals("1")
						|| pTmp.toLowerCase().indexOf("true") != -1) {
					preferredFlag = true;
					onepreferred = true;
				} else {
					preferredFlag = false;
				}
			} else {
				scientificName = fullScientificNameString;
			}

			// logger.info("fullscientificname " + fullScientificNameString
			// + ", *" + dataHolder.nomenclatureCode + "*");

			if (fullScientificNameString.indexOf("_code_") != -1) {
				if (fullScientificNameString.indexOf(':') != -1) {
					dataHolder.nomenclatureCode = fullScientificNameString
							.split("_code_")[1].split(":")[1];
				} else
					dataHolder.nomenclatureCode = fullScientificNameString
					.split("_code_")[1];
			}

			if (config.isDoAutomaticParsing()
					|| dataHolder.atomisedIdentificationList == null
					|| dataHolder.atomisedIdentificationList.size() == 0) {
				taxonName = parseScientificName(scientificName);
				if (taxonName == null)
					taxonName = setTaxonNameByType(
							dataHolder.atomisedIdentificationList.get(i),
							scientificName);
			} else {
				if (dataHolder.atomisedIdentificationList != null
						|| dataHolder.atomisedIdentificationList.size() > 0) {
					taxonName = setTaxonNameByType(
							dataHolder.atomisedIdentificationList.get(i),
							scientificName);
				}
			}

			getNameService().saveOrUpdate(taxonName);

			taxon = getTaxon(config, scientificName, taxonName);

			if (preferredFlag) {
				preferredtaxonnametoinsert.put(i, taxonName);
				preferredtaxontoinsert.put(i, taxon);
			} else {
				taxonnametoinsert.put(i, taxonName);
				taxontoinsert.put(i, taxon);
			}
		}

		Iterator entries = preferredtaxontoinsert.keySet().iterator();
		while (entries.hasNext()) {

			int a = (Integer) entries.next();
//			logger.info("\n PREFERRED num." + a + " \n");
			taxonName = preferredtaxonnametoinsert.get(a);
			taxon = preferredtaxontoinsert.get(a);


			taxon = addParentTaxon(taxon, taxonName, config);
			refreshTransaction();

			taxon = (Taxon) getTaxonService().find(taxon.getUuid());

			linkDeterminationEvent(taxon, true, config,
					derivedUnitFacade);
		}

		if (onepreferred) {
			entries = taxontoinsert.keySet().iterator();

			while (entries.hasNext()) {
				int a = (Integer) entries.next();
//				logger.info("\n ADD non preferred " + a + " \n");
				// taxonName = taxonnametoinsert.get(a);
				taxon = taxontoinsert.get(a);
				// do not do addParentTaxon as the name is not the
				// preferred/accepted one
				// should not be displayed in the tree
				// taxon = addParentTaxon(taxon, taxonName, config, true);

				// do linkdetermination as it should be linked to the specimen,
				// though
				refreshTransaction();
				taxon = (Taxon) getTaxonService().find(taxon.getUuid());

				linkDeterminationEvent(taxon, false, config, 
						derivedUnitFacade);

			}

		}

		else {// no preferred name, have to add everything to the classification
			// logger.info("no preferred taxa");
			entries = taxontoinsert.keySet().iterator();

			while (entries.hasNext()) {
				int a = (Integer) entries.next();
				// logger.info("\n NORMAL num." + a + " \n");
				taxonName = taxonnametoinsert.get(a);
				taxon = taxontoinsert.get(a);


				taxon = addParentTaxon(taxon, taxonName, config);
				taxon = (Taxon) getTaxonService().find(taxon.getUuid());

				refreshTransaction();
				taxon = (Taxon) getTaxonService().find(taxon.getUuid());

				if (taxontoinsert.keySet().size() == 1)
					linkDeterminationEvent(taxon, true, config,
							derivedUnitFacade);
				else
					linkDeterminationEvent(taxon, false, config,
							derivedUnitFacade);

			}

		}

	}

	private NonViralName<?> parseScientificName(String scientificName) {
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?> taxonName = null;
		boolean problem = false;

		// logger.info("parseScientificName "
		//		+ dataHolder.nomenclatureCode.toString());

		if (dataHolder.nomenclatureCode.toString().equals("Zoological")
				|| dataHolder.nomenclatureCode.toString().contains("ICZN")) {
			taxonName = (ZoologicalName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICZN, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Botanical")
				|| dataHolder.nomenclatureCode.toString().contains("ICBN")) {
			taxonName = (BotanicalName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICBN, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Bacterial")
				|| dataHolder.nomenclatureCode.toString().contains("ICBN")) {
			taxonName = (BacterialName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICNB, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Cultivar")
				|| dataHolder.nomenclatureCode.toString().contains("ICNCP")) {
			taxonName = (CultivarPlantName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICNCP, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (problem) {
			// logger.info("Parsing with problem in parseScientificName "
			//		+ scientificName);
			return null;
		}
		return taxonName;

	}

	private NonViralName<?> setTaxonNameByType(
			HashMap<String, String> atomisedMap, String fullName) {
		boolean problem = false;
		// logger.info("settaxonnamebytype "
		//		+ dataHolder.nomenclatureCode.toString());

		if (dataHolder.nomenclatureCode.equals("Zoological")) {
			NonViralName<ZoologicalName> taxonName = ZoologicalName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName
			.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,
					"SpeciesEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"SubspeciesEpithet"));

			if (taxonName.getGenusOrUninomial() != null)
				taxonName.setRank(Rank.GENUS());

			if (taxonName.getInfraGenericEpithet() != null)
				taxonName.setRank(Rank.SUBGENUS());

			if (taxonName.getSpecificEpithet() != null)
				taxonName.setRank(Rank.SPECIES());

			if (taxonName.getInfraSpecificEpithet() != null)
				taxonName.setRank(Rank.SUBSPECIES());

			Team team = null;
			if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
			} else {
				if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
					team = Team.NewInstance();
					team.setTitleCache(
							getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
				}
			}
			if (team != null) {
				taxonName.setBasionymAuthorTeam(team);
			} else {
				if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamParenthesis"));
				} else if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamAndYear"));
				}
			}
			if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"),
						true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICZN");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Botanical")) {
			NonViralName<BotanicalName> taxonName = BotanicalName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,
					"FirstEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"InfraSpeEpithet"));
			try {
				taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap,
						"Rank")));
			} catch (Exception e) {
				if (taxonName.getGenusOrUninomial() != null)
					taxonName.setRank(Rank.GENUS());

				if (taxonName.getInfraGenericEpithet() != null)
					taxonName.setRank(Rank.SUBGENUS());

				if (taxonName.getSpecificEpithet() != null)
					taxonName.setRank(Rank.SPECIES());

				if (taxonName.getInfraSpecificEpithet() != null)
					taxonName.setRank(Rank.SUBSPECIES());
			}
			Team team = null;
			if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
				if (team != null) {
					taxonName.setBasionymAuthorTeam(team);
				}
			}
			if (getFromMap(atomisedMap, "AuthorTeam") != null) {
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap, "AuthorTeam"), true);
				if (team != null) {
					taxonName.setCombinationAuthorTeam(team);
				}
			}
			if (team == null) {
				if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamParenthesis"));
				} else if (getFromMap(atomisedMap, "AuthorTeam") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeam"));
				}
			}
			if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"),
						true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICBN");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Bacterial")) {
			NonViralName<BacterialName> taxonName = BacterialName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName
			.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap, "Species"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"SubspeciesEpithet"));

			if (taxonName.getGenusOrUninomial() != null)
				taxonName.setRank(Rank.GENUS());

			if (taxonName.getInfraGenericEpithet() != null)
				taxonName.setRank(Rank.SUBGENUS());

			if (taxonName.getSpecificEpithet() != null)
				taxonName.setRank(Rank.SPECIES());

			if (taxonName.getInfraSpecificEpithet() != null)
				taxonName.setRank(Rank.SUBSPECIES());

			if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
				Team team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear") != null) {
				Team team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap,
								"ParentheticalAuthorTeamAndYear"), true);
				taxonName.setBasionymAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICNB");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Cultivar")) {
			CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

			if (taxonName.hasProblem()) {
				logger.info("pb ICNCP");
				problem = true;
			} else {
				return taxonName;
			}
			return taxonName;
		}

		if (problem) {
			logger.info("Problem im setTaxonNameByType ");
			NonViralName<?> taxonName = NonViralName.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			return taxonName;
		}
		NonViralName<?> tn = NonViralName.NewInstance(null);
		return tn;
	}

	private String getFromMap(HashMap<String, String> atomisedMap, String key) {
		String value = null;
		if (atomisedMap.containsKey(key)) {
			value = atomisedMap.get(key);
		}

		try {
			if (value != null && key.matches(".*Year.*")) {
				value = value.trim();
				if (value.matches("[a-z A-Z ]*[0-9]{4}$")) {
					String tmp = value.split("[0-9]{4}$")[0];
					int year = Integer.parseInt(value.split(tmp)[1]);
					if (year >= 1752) {
						value = tmp;
					} else {
						value = null;
					}
				} else {
					value = null;
				}
			}
		} catch (Exception e) {
			value = null;
		}

		return value;
	}

	private void compareABCDtoCDM(URI urlFileName, ArrayList<String> knownElts) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder constructeur = factory.newDocumentBuilder();
			URL url = urlFileName.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream) o;
			Document document = constructeur.parse(is);
			Element root = document.getDocumentElement();
			abcdFileGetter.traverse(root);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> elts = dataHolder.allABCDelements.keySet();
		Iterator<String> it = elts.iterator();
		String elt;
		while (it.hasNext()) {
			elt = it.next();
			if (knownElts.indexOf(elt) == -1) {
				// logger.info("Unmerged ABCD element: " + elt + " - "+
				// dataHolder.allABCDelements.get(elt));
			}
		}
	}

	@Override
	protected boolean isIgnore(Abcd206ImportState state) {
		// return ! config.isDoNameFacts();
		return false;
	}

}
