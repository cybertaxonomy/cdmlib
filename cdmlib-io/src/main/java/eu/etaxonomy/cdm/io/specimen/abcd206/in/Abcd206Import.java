/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenUserInteraction;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
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
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {
    private static final Logger logger = Logger.getLogger(Abcd206Import.class);


    private final boolean DEBUG = false;

    private static final String SEC = "sec. ";
    private static final String PREFERRED = "_preferred_";
    private static final String CODE = "_code_";
    private static final String COLON = ":";
    private static final String SPLITTER = "--";


    private static String prefix = "";

    //TODO make all fields ABCD206ImportState variables
    private Classification classification = null;
    private Reference<?> ref = null;

    private Abcd206DataHolder dataHolder;
    private DerivedUnit derivedUnitBase;

    public Abcd206Import() {
        super();
    }

    @Override
    protected boolean doCheck(Abcd206ImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }


    @Override
    @SuppressWarnings("rawtypes")
    public void doInvoke(Abcd206ImportState state) {
        state.setTx(startTransaction());
        logger.info("INVOKE Specimen Import from ABCD2.06 XML ");

        SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

        List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);

        if (state.getConfig().isInteractWithUser()){
            Map<String,Reference> refMap = new HashMap<String, Reference>();
            for (Reference tree : references) {
                if (! StringUtils.isBlank(tree.getTitleCache())) {
                    refMap.put(tree.getTitleCache(),tree);
                }
            }
            ref = sui.askForReference(refMap);

            if (ref == null){
                String cla = sui.createNewReference();
                if (refMap.get(cla)!= null) {
                    ref = refMap.get(cla);
                } else {
                    ref = ReferenceFactory.newGeneric();
                    ref.setTitle(cla);
                }
            }
            else{
                ref = getReferenceService().find(ref.getUuid());
            }
        }else{
            if (ref==null){
                String name = NB(state.getConfig().getSourceReferenceTitle());
                for (Reference tree : references) {
                    if (! StringUtils.isBlank(tree.getTitleCache())) {
                        if (tree.getTitleCache().equalsIgnoreCase(name)) {
                            ref=tree;
                            System.out.println("FIND SAME REFERENCE");
                        }
                    }
                }
                if (ref == null){
                    ref = ReferenceFactory.newGeneric();
                    ref.setTitle("ABCD classic");
                }
            }
        }
        getReferenceService().saveOrUpdate(ref);
        state.getConfig().setSourceReference(ref);

        List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, null);
        if (state.getConfig().isUseClassification() && state.getConfig().isInteractWithUser()){
            Map<String,Classification> classMap = new HashMap<String, Classification>();
            for (Classification tree : classificationList) {
                if (! StringUtils.isBlank(tree.getTitleCache())) {
                    classMap.put(tree.getTitleCache(),tree);
                }
            }
            classification = sui.askForClassification(classMap);
            if (classification == null){
                String cla = sui.createNewClassification();
                if (classMap.get(cla)!= null) {
                    classification = classMap.get(cla);
                } else {
                    classification = Classification.NewInstance(cla, ref, Language.DEFAULT());
                }
            }
            getClassificationService().saveOrUpdate(classification);
        }
        else{
            if (classification == null) {
                String name = NB(state.getConfig().getClassificationName());
                for (Classification classif : classificationList){
                    if (classif.getTitleCache().equalsIgnoreCase(name) && classif.getCitation().equals(ref)) {
                        classification=classif;
                        System.out.println("FIND SAME CLASSIF");
                    }
                }
                if (classification == null){
                    classification = Classification.NewInstance(name, ref, Language.DEFAULT());
                }
//                if (state.getConfig().getClassificationUuid() != null) {
//                    classification.setUuid(state.getConfig().getClassificationUuid());
//                }
                getClassificationService().saveOrUpdate(classification);
            }
        }

        URI sourceName = state.getConfig().getSource();
        NodeList unitsList = getUnitsNodeList(sourceName);

        if (unitsList != null) {
            String message = "nb units to insert: " + unitsList.getLength();
            logger.info(message);
            updateProgress(state, message);

            dataHolder = new Abcd206DataHolder();

            Abcd206XMLFieldGetter abcdFieldGetter = new Abcd206XMLFieldGetter(dataHolder, prefix);

            prepareCollectors(state, unitsList, abcdFieldGetter);

            for (int i = 0; i < unitsList.getLength(); i++) {
                this.setUnitPropertiesXML( (Element) unitsList.item(i), abcdFieldGetter);
                //				refreshTransaction(state);
                this.handleSingleUnit(state);

                // compare the ABCD elements added in to the CDM and the
                // unhandled ABCD elements
                //compareABCDtoCDM(sourceName, dataHolder.knownABCDelements, abcdFieldGetter);

                // reset the ABCD elements added in CDM
                // knownABCDelements = new ArrayList<String>();
                dataHolder.allABCDelements = new HashMap<String, String>();
            }
        }
        commitTransaction(state.getTx());
        return;

    }


    /**
     * Return the list of root nodes for an ABCD 2.06 XML file
     * @param fileName: the file's location
     * @return the list of root nodes ("Unit")
     */
    protected NodeList getUnitsNodeList(URI urlFileName) {
        NodeList unitList = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL url = urlFileName.toURL();
            Object o = url.getContent();
            InputStream is = (InputStream) o;
            Document document = builder.parse(is);
            Element root = document.getDocumentElement();
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

    /**
     * Handle a single unit
     * @param state
     */
    @SuppressWarnings("rawtypes")
    private void handleSingleUnit(Abcd206ImportState state) {
        if (DEBUG) {
            logger.info("handleSingleUnit "+ref);
        }
        try {
            updateProgress(state, "Importing data for unit: " + dataHolder.unitID);

            // create facade
            DerivedUnitFacade derivedUnitFacade = getFacade();
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

            /**
             * GATHERING EVENT
             */
            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(getTermService(), dataHolder.locality, dataHolder.languageIso,
                    dataHolder.longitude, dataHolder.latitude, dataHolder.gatheringAgentList, dataHolder.gatheringTeamList,state.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
            //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
            unitsGatheringArea.setParams(dataHolder.isocountry, dataHolder.country, state.getConfig(), getTermService(), getOccurrenceService());

            DefinedTermBase<?> areaCountry =  unitsGatheringArea.getCountry();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea();
            //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());
            unitsGatheringArea.setAreas(dataHolder.namedAreaList,state.getConfig(), getTermService());
            ArrayList<DefinedTermBase> nas = unitsGatheringArea.getAreas();
            for (DefinedTermBase namedArea : nas) {
                unitsGatheringEvent.addArea(namedArea);
            }

            // copy gathering event to facade
            GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
            derivedUnitFacade.setLocality(gatheringEvent.getLocality());
            derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
            derivedUnitFacade.setCollector(gatheringEvent.getCollector());
            derivedUnitFacade.setCountry((NamedArea)areaCountry);
            for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
                derivedUnitFacade.addCollectingArea((NamedArea) area);
            }
            //            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());
            // TODO exsiccatum

            // add fieldNumber
            derivedUnitFacade.setFieldNumber(NB(dataHolder.fieldNumber));

            // //add Multimedia URLs
            if (dataHolder.multimediaObjects.size() != -1) {
                for (String multimediaObject : dataHolder.multimediaObjects) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA, false);
                        derivedUnitFacade.addDerivedUnitMedia(media);
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }

            //			/*
            //			 * merge AND STORE DATA
            //			 */
            //			getTermService().saveOrUpdate(areaCountry);// TODO save area sooner
            //
            //			for (NamedArea area : otherAreas) {
            //				getTermService().saveOrUpdate(area);// merge it sooner (foreach area)
            //			}
            getTermService().saveLanguageData(unitsGatheringEvent.getLocality());

            // handle collection data
            setCollectionData(state.getConfig(), derivedUnitFacade);

            getOccurrenceService().saveOrUpdate(derivedUnitBase);

            // handle identifications
            handleIdentifications(state, derivedUnitFacade);

            if(DEBUG) {
                logger.info("saved ABCD specimen ...");
            }

        } catch (Exception e) {
            logger.warn("Error when reading record!!");
            e.printStackTrace();
            state.setUnsuccessfull();
        }

        return;
    }

    /**
     * setCollectionData : store the collection object into the
     * derivedUnitFacade
     *
     * @param config
     */
    private void setCollectionData(Abcd206ImportConfigurator config, DerivedUnitFacade derivedUnitFacade) {
        // set catalogue number (unitID)
        derivedUnitFacade.setCatalogNumber(NB(dataHolder.unitID));
        derivedUnitFacade.setAccessionNumber(NB(dataHolder.accessionNumber));
        // derivedUnitFacade.setCollectorsNumber(NB(dataHolder.collectorsNumber));

        /*
         * INSTITUTION & COLLECTION
         */
        // manage institution
        Institution institution = this.getInstitution(NB(dataHolder.institutionCode), config);
        // manage collection
        Collection collection = this.getCollection(institution, NB(dataHolder.collectionCode), config);
        // link specimen & collection
        derivedUnitFacade.setCollection(collection);
    }

    /**
     * getFacade : get the DerivedUnitFacade based on the recordBasis
     *
     * @return DerivedUnitFacade
     */
    private DerivedUnitFacade getFacade() {
		if(DEBUG) {
			logger.info("getFacade()");
		}
		SpecimenOrObservationType type = null;

		// create specimen
		if (NB((dataHolder.recordBasis)) != null) {
			if (dataHolder.recordBasis.toLowerCase().startsWith("s") || dataHolder.recordBasis.toLowerCase().contains("specimen")) {// specimen
				type = SpecimenOrObservationType.PreservedSpecimen;
			}
			if (dataHolder.recordBasis.toLowerCase().startsWith("o")) {
				type = SpecimenOrObservationType.Observation;
			}
			if (dataHolder.recordBasis.toLowerCase().contains("fossil")){
				type = SpecimenOrObservationType.Fossil;
			}
			if (dataHolder.recordBasis.toLowerCase().startsWith("l")) {
				type = SpecimenOrObservationType.LivingSpecimen;
			}
			if (type == null) {
				logger.info("The basis of record does not seem to be known: " + dataHolder.recordBasis);
				type = SpecimenOrObservationType.DerivedUnit;
			}
			// TODO fossils?
		} else {
			logger.info("The basis of record is null");
			type = SpecimenOrObservationType.DerivedUnit;
		}
		DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
		return derivedUnitFacade;
    }

    private void getCollectorsFromXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter) {
        NodeList group;

        group = root.getChildNodes();
        for (int i = 0; i < group.getLength(); i++) {
            if (group.item(i).getNodeName().equals(prefix + "Identifications")) {
                group = group.item(i).getChildNodes();
                break;
            }
        }
        dataHolder.gatheringAgentList = new ArrayList<String>();
        dataHolder.gatheringTeamList = new ArrayList<String>();
        abcdFieldGetter.getType(root);
        abcdFieldGetter.getGatheringPeople(root);
    }

    /**
     * Store the unit's properties into variables Look which unit is the
     * preferred one Look what kind of name it is supposed to be, for the
     * parsing (Botanical, Zoological)
     *
     * @param racine: the root node for a single unit
     */
    private void setUnitPropertiesXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter) {
        try {
            NodeList group;

            group = root.getChildNodes();
            for (int i = 0; i < group.getLength(); i++) {
                if (group.item(i).getNodeName().equals(prefix + "Identifications")) {
                    group = group.item(i).getChildNodes();
                    break;
                }
            }
            dataHolder.identificationList = new ArrayList<String>();
            dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
            dataHolder.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
            dataHolder.referenceList = new ArrayList<String>();
            dataHolder.multimediaObjects = new ArrayList<String>();

            abcdFieldGetter.getScientificNames(group);
            abcdFieldGetter.getType(root);

            if(DEBUG) {
                logger.info("this.identificationList "+dataHolder.identificationList.toString());
            }
            abcdFieldGetter.getIDs(root);
            abcdFieldGetter.getRecordBasis(root);
            abcdFieldGetter.getMultimedia(root);
            abcdFieldGetter.getNumbers(root);
            abcdFieldGetter.getGeolocation(root);
            abcdFieldGetter.getGatheringPeople(root);
            boolean referencefound = abcdFieldGetter.getReferences(root);
            if (!referencefound) {
                dataHolder.referenceList.add(ref.getTitleCache());
            }

        } catch (Exception e) {
            logger.info("Error occured while parsing XML file" + e);
        }
    }

    /**
     * Look if the Institution does already exist
     * @param institutionCode: a string with the institutioncode
     * @param config : the configurator
     * @return the Institution (existing or new)
     */
    @SuppressWarnings("rawtypes")
    private Institution getInstitution(String institutionCode, Abcd206ImportConfigurator config) {
        Institution institution=null;
        List<AgentBase> institutions;
        try {
            institutions = getAgentService().list(Institution.class, null, null, null, null);
        } catch (Exception e) {
            institutions = new ArrayList<AgentBase>();
            logger.warn(e);
        }
        if (institutions.size() > 0 && config.isReUseExistingMetadata()) {
            for (AgentBase inst:institutions){
                Institution institut = (Institution)inst;
                if (institut.getCode().equalsIgnoreCase(institutionCode)) {
                    institution=institut;
                }
            }
        }
        if(DEBUG) {
            logger.info("getinstitution " + institution.toString());
        }
        if (institution == null){
            // create institution
            institution = Institution.NewInstance();
            institution.setCode(institutionCode);
            institution.setTitleCache(institutionCode);
        }
        getAgentService().saveOrUpdate(institution);
        return institution;
    }

    /**
     * Look if the Collection does already exist
     * @param collectionCode
     * @param collectionCode: a string
     * @param config : the configurator
     * @return the Collection (existing or new)
     */
    private Collection getCollection(Institution institution, String collectionCode, Abcd206ImportConfigurator config) {
        Collection collection = null;
        List<Collection> collections;
        try {
            collections = getCollectionService().list(Collection.class, null, null, null, null);
        } catch (Exception e) {
            collections = new ArrayList<Collection>();
        }
        if (collections.size() > 0 && config.isReUseExistingMetadata()) {
            for (Collection coll:collections){
                if (coll.getInstitute() != null) {
                    if (coll.getCode().equalsIgnoreCase(collectionCode) && coll.getInstitute().equals(institution)) {
                        collection=coll;
                    }
                }
            }
        }

        if(collection == null){
            collection =Collection.NewInstance();
            collection.setCode(collectionCode);
            collection.setInstitute(institution);
            collection.setTitleCache(collectionCode);
        }
        getCollectionService().saveOrUpdate(collection);
        return collection;
    }


    /**
     * join DeterminationEvent to the Taxon Object
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param preferredFlag :if the current name is preferred
     * @param derivedFacade : the derived Unit Facade
     */
    @SuppressWarnings("rawtypes")
    private void linkDeterminationEvent(Abcd206ImportState state, Taxon taxon, boolean preferredFlag,  DerivedUnitFacade derivedFacade) {
        Abcd206ImportConfigurator config = state.getConfig();
        if(DEBUG){
            logger.info("start linkdetermination with taxon:" + taxon.getUuid()+", "+taxon);
        }

        DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
        determinationEvent.setTaxon(taxon);
        determinationEvent.setPreferredFlag(preferredFlag);

        determinationEvent.setIdentifiedUnit(derivedUnitBase);

        derivedUnitBase.addDetermination(determinationEvent);

        try {
            if(DEBUG){
                logger.info("NB TYPES INFO: "+ dataHolder.statusList.size());
            }
            for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : dataHolder.statusList) {
                if (specimenTypeDesignationstatus != null) {
                    if(DEBUG){
                        logger.info("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
                    }

                    specimenTypeDesignationstatus = (SpecimenTypeDesignationStatus) getTermService().find(specimenTypeDesignationstatus.getUuid());
                    //Designation
                    TaxonNameBase<?,?> name = taxon.getName();
                    SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

                    designation.setTypeStatus(specimenTypeDesignationstatus);
                    designation.setTypeSpecimen(derivedUnitBase);
                    name.addTypeDesignation(designation, true);
                }
            }
        } catch (Exception e) {
            logger.warn("PB addding SpecimenType " + e);
        }

        for (String strReference : dataHolder.referenceList) {
            List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);
            if (isNotBlank(strReference)){
                Reference<?> reference = null;
                for (Reference<?> refe: references) {
                    if (refe.getTitleCache().equalsIgnoreCase(strReference)) {
                        reference =refe;
                    }
                }
                if (reference ==null){
                    reference = ReferenceFactory.newGeneric();
                    reference.setTitleCache(strReference, true);
                    getReferenceService().saveOrUpdate(reference);
                }
                determinationEvent.addReference(reference);
            }
        }
        getOccurrenceService().saveOrUpdate(derivedUnitBase);

        if (config.isAddIndividualsAssociationsSuchAsSpecimenAndObservations()) {
            if(DEBUG){
                logger.info("isDoCreateIndividualsAssociations");
            }

            makeIndividualsAssociation(state, taxon, determinationEvent);
            getOccurrenceService().saveOrUpdate(derivedUnitBase);
        }
    }

    /**
     * create and link each association (specimen, observation..) to the accepted taxon
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param determinationEvent:the determinationevent
     */
    @SuppressWarnings("unused")
    private void makeIndividualsAssociation(Abcd206ImportState state, Taxon taxon, DeterminationEvent determinationEvent) {
        if (DEBUG) {
            System.out.println("MAKE INDIVIDUALS ASSOCIATION");
        }

        TaxonDescription taxonDescription = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        for (TaxonDescription description : descriptions){
            Set<IdentifiableSource> sources =  description.getTaxon().getSources();
            sources.addAll(description.getSources());
            for (IdentifiableSource source:sources){
                if(ref.equals(source.getCitation())) {
                    taxonDescription = description;
                }
            }
        }
        if (taxonDescription == null){
            taxonDescription = TaxonDescription.NewInstance(taxon, false);
            taxonDescription.addSource(OriginalSourceType.Import, null, null, ref, null);
            taxon.addDescription(taxonDescription);
        }


        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        Feature feature = makeFeature(derivedUnitBase);
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);

        for (Reference<?> citation : determinationEvent.getReferences()) {
            indAssociation.addSource(DescriptionElementSource.NewInstance(OriginalSourceType.Import, null, null, citation, null));
        }

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(taxon);

        getDescriptionService().saveOrUpdate(taxonDescription);
        getTaxonService().saveOrUpdate(taxon);
    }

    /**
     * look for the Feature object (FieldObs, Specimen,...)
     * @param unit : a specimen or obersvation base
     * @return the corresponding Feature
     */
    private Feature makeFeature(SpecimenOrObservationBase<?> unit) {
		SpecimenOrObservationType type = unit.getRecordBasis();
		if (type.isFeatureObservation()){
			return Feature.OBSERVATION();
		}else if (type.isFeatureSpecimen()){
			return Feature.SPECIMEN();
		}else if (type == SpecimenOrObservationType.DerivedUnit){
			return Feature.INDIVIDUALS_ASSOCIATION();
		}else{
			String message = "Unhandled record basis '%s' for defining individuals association feature type. Use default.";
			logger.warn(String.format(message, type.getMessage()));
			return Feature.INDIVIDUALS_ASSOCIATION();
		}
	}


    /**
     *  getTaxon : search for an existing taxon in the database
     * @param state : the ABCD import state
     * @param scientificName : the name (string)
     * @param i : the current unit position in the abcd file
     * @param rank : the rank for the taxon
     * @return a Taxon
     */
    @SuppressWarnings("rawtypes")
    private Taxon getTaxon(Abcd206ImportState state, String scientificName, int i, Rank rank) {
        Abcd206ImportConfigurator config = state.getConfig();
        Taxon taxon = null;
        NonViralName<?> taxonName = null;

        SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

        System.out.println("config.isReuseExistingTaxaWhenPossible() :"+config.isReuseExistingTaxaWhenPossible());
        if (config.isReuseExistingTaxaWhenPossible()){
            List<TaxonBase> c = null;
            try {
                List<TaxonBase> taxonbaseList = getTaxonService().listByTitle(Taxon.class, scientificName+" sec", MatchMode.BEGINNING, null, null, null, null, null);
                if (taxonbaseList.size()>0){
                    if(config.isInteractWithUser() && config.isAllowReuseOtherClassifications()){
                        taxon = sui.askWhereToFixData(scientificName,taxonbaseList, classification);
                    } else {
                        taxon = sui.lookForTaxaIntoCurrentClassification(taxonbaseList, classification);
                    }
                }
                else{
                    c = getTaxonService().searchTaxaByName(scientificName, ref);
                    if(config.isInteractWithUser() && config.isAllowReuseOtherClassifications()){
                        taxon = sui.askWhereToFixData(scientificName,c, classification);
                    }
                    else{
                        taxon = sui.lookForTaxaIntoCurrentClassification(c, classification);
                    }
                }
            } catch (Exception e) {
                logger.info("Searchtaxabyname failed" + e);
                taxon = null;
            }
        }
        if (!config.isReuseExistingTaxaWhenPossible() || taxon == null){
            System.out.println("create new taxonName instance "+i+", "+config.isParseNameAutomatically());
            if (config.isParseNameAutomatically()){
                taxonName = parseScientificName(scientificName);
            }
            else{
                if (i>=0 && (dataHolder.atomisedIdentificationList != null || dataHolder.atomisedIdentificationList.size() > 0)) {
                    taxonName = setTaxonNameByType(dataHolder.atomisedIdentificationList.get(i), scientificName);
                } else {
                    taxonName=null;
                }
            }
            //            if (taxonName != null) {
            //                System.out.println(taxonName.getTitleCache());
            //            } else {
            //                System.out.println("taxonname: "+taxonName);
            //            }
            if(taxonName == null){
                taxonName = NonViralName.NewInstance(rank);
                taxonName.setFullTitleCache(scientificName,true);
                taxonName.setTitleCache(scientificName, true);
            }
            System.out.println("ADD NEW TAXON *"+taxonName.getRank()+"*"+taxonName.getTitleCache());
            if (rank != null && (taxonName.getRank() ==null || taxonName.getRank().toString().trim().isEmpty())) {
                taxonName.setRank(rank);
            }
            getNameService().save(taxonName);
            taxon = Taxon.NewInstance(taxonName, ref); //sec set null
            getTaxonService().save(taxon);
        }
        return taxon;
    }







    /**
     * HandleIdentifications : get the scientific names present in the ABCD
     * document and store link them with the observation/specimen data
     * @param state: the current ABCD import state
     * @param derivedUnitFacade : the current derivedunitfacade
     */
    private void handleIdentifications(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
        System.out.println("The reference from handleidentification "+ref);
        Abcd206ImportConfigurator config = state.getConfig();

        String fullScientificNameString;
        Taxon taxon = null;
        Rank.GENUS();
        Rank.FAMILY();

        String scientificName = "";
        boolean preferredFlag = false;

        List<String> scientificNames = new ArrayList<String>();
        if (dataHolder.nomenclatureCode == ""){
            dataHolder.nomenclatureCode = config.getNomenclaturalCode().toString();
        }

        for (int i = 0; i < dataHolder.identificationList.size(); i++) {

            fullScientificNameString = dataHolder.identificationList.get(i);
            fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");

            if (fullScientificNameString.indexOf(PREFERRED) != -1) {
                scientificName = fullScientificNameString.split(PREFERRED)[0];
                String pTmp = fullScientificNameString.split(PREFERRED)[1].split(CODE)[0];
                if (pTmp.equals("1") || pTmp.toLowerCase().indexOf("true") != -1) {
                    preferredFlag = true;
                }
                else {
                    preferredFlag = false;
                }
            }
            else {
                scientificName = fullScientificNameString;
            }
            if(DEBUG) {
                logger.info("fullscientificname " + fullScientificNameString + ", *" + dataHolder.nomenclatureCode + "*");
            }
            if (fullScientificNameString.indexOf(CODE) != -1) {
                if (fullScientificNameString.indexOf(':') != -1) {
                    dataHolder.nomenclatureCode = fullScientificNameString.split(CODE)[1].split(COLON)[1];
                }
                else{
                    dataHolder.nomenclatureCode = fullScientificNameString.split(CODE)[1];
                }
            }
            scientificNames.add(scientificName+SPLITTER+preferredFlag+SPLITTER+i);
        }
        for (String name:scientificNames) {
            scientificName = name.split(SPLITTER)[0];
            String pref = name.split(SPLITTER)[1];
            String index = name.split(SPLITTER)[2];
            if (pref.equalsIgnoreCase("true") || scientificNames.size()==1) {
                preferredFlag = true;
            } else {
                preferredFlag =false;
            }
            taxon = getTaxon(state, scientificName,Integer.parseInt(index),null);
            addTaxonNode(taxon, state);
            linkDeterminationEvent(state, taxon, preferredFlag, derivedUnitFacade);
        }
    }

    /**
     * @param taxon : a taxon to add as a node
     * @param state : the ABCD import state
     */
    private void addTaxonNode(Taxon taxon, Abcd206ImportState state) {
        logger.info("link taxon to a taxonNode "+taxon.getTitleCache());
        boolean exist = false;
        for (TaxonNode p : classification.getAllNodes()){
            if(p.getTaxon().equals(taxon)) {
                exist =true;
            }
        }
        if (!exist){
            addParentTaxon(taxon, state);
        }
    }

    /**
     * Add the hierarchy for a Taxon(add higher taxa)
     * @param taxon: a taxon to add as a node
     * @param state: the ABCD import state
     */
    private void addParentTaxon(Taxon taxon, Abcd206ImportState state){
        System.out.println("addParentTaxon "+taxon.getTitleCache());

        NonViralName<?>  nvname = CdmBase.deproxy(taxon.getName(), NonViralName.class);
        Rank rank = nvname.getRank();
        Taxon genus =null;
        Taxon subgenus =null;
        Taxon species = null;
        Taxon subspecies = null;
        Taxon parent = null;
        if (rank.isLower(Rank.GENUS() )){
            String prefix = nvname.getGenusOrUninomial();
            genus = getTaxon(state, prefix, -1, Rank.GENUS());
            parent = saveOrUpdateClassification(null, genus);

        }
        if (rank.isLower(Rank.SUBGENUS())){
            String prefix = nvname.getGenusOrUninomial();
            String name = nvname.getInfraGenericEpithet();
            if (name != null){
                subgenus = getTaxon(state, prefix+" "+name, -1, Rank.SUBGENUS());
                parent = saveOrUpdateClassification(genus, subgenus);
            }
        }
        if (rank.isLower(Rank.SPECIES())){
            if (subgenus!=null){
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getInfraGenericEpithet();
                String spe = nvname.getSpecificEpithet();
                if (spe != null){
                    species = getTaxon(state, prefix+" "+name+" "+spe, -1, Rank.SPECIES());
                    parent = 	saveOrUpdateClassification(subgenus, species);
                }
            }
            else{
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getSpecificEpithet();
                if (name != null){
                    species = getTaxon(state, prefix+" "+name, -1, Rank.SPECIES());
                    parent = 	saveOrUpdateClassification(genus, species);
                }
            }
        }
        if (rank.isInfraSpecific()){
            subspecies = getTaxon(state, nvname.getFullTitleCache(), -1, Rank.SUBSPECIES());
            parent = 	saveOrUpdateClassification(species, subspecies);
        }
        saveOrUpdateClassification(parent, taxon);
    }

    /**
     * Link a parent to a child and save it in the current classification
     * @param parent: the higher Taxon
     * @param child : the lower (or current) Taxon
     * return the Taxon from the new created Node
     */
    private Taxon saveOrUpdateClassification(Taxon parent, Taxon child) {
		System.out.println("ADD CLASSIFICATION parent child "+parent+"," +child);
		TaxonNode node =null;
		if (parent != null) {
			parent = (Taxon) getTaxonService().find(parent.getUuid());
			child = (Taxon) getTaxonService().find(child.getUuid());
			node = classification.addParentChild(parent, child, ref, "");
		}
		if (parent == null) {
			child = (Taxon) getTaxonService().find(child.getUuid());
			node =classification.addChildTaxon(child, ref, null);
		}
		getClassificationService().saveOrUpdate(classification);
		return node.getTaxon();
    }
    
    /**
     * Parse automatically the scientific name
     * @param scientificName: the scientific name to parse
     * @return a parsed name
     */
    private NonViralName<?> parseScientificName(String scientificName) {
        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
        NonViralName<?> taxonName = null;
        boolean problem = false;

        if(DEBUG){
            logger.info("parseScientificName " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.toString().equals("Zoological") || dataHolder.nomenclatureCode.toString().contains("ICZN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICZN, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Botanical") || dataHolder.nomenclatureCode.toString().contains("ICBN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNAFP, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Bacterial") || dataHolder.nomenclatureCode.toString().contains("ICBN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNB, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Cultivar") || dataHolder.nomenclatureCode.toString().contains("ICNCP")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNCP, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (problem) {
            logger.info("Parsing with problem in parseScientificName " + scientificName);
            return null;
        }
        return taxonName;

    }

    /**
     * Create the name without automatic parsing, either because it failed, or because the user deactivated it.
     * The name is built upon the ABCD fields
     * @param atomisedMap : the ABCD atomised fields
     * @param fullName : the full scientific name
     * @return the corresponding Botanical or Zoological or... name
     */
    private NonViralName<?> setTaxonNameByType(
            HashMap<String, String> atomisedMap, String fullName) {
        boolean problem = false;
        if(DEBUG) {
            logger.info("settaxonnamebytype " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.equals("Zoological")) {
            NonViralName<ZoologicalName> taxonName = ZoologicalName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap,"SpeciesEpithet")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap,"SubspeciesEpithet")));

            if (taxonName.getGenusOrUninomial() != null){
                taxonName.setRank(Rank.GENUS());
            }

            else if (taxonName.getInfraGenericEpithet() != null){
                taxonName.setRank(Rank.SUBGENUS());
            }

            else if (taxonName.getSpecificEpithet() != null){
                taxonName.setRank(Rank.SPECIES());
            }

            else if (taxonName.getInfraSpecificEpithet() != null){
                taxonName.setRank(Rank.SUBSPECIES());
            }

            Team team = null;
            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
            }
            else {
                if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                    team = Team.NewInstance();
                    team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
                }
            }
            if (team != null) {
                taxonName.setBasionymAuthorTeam(team);
            }
            else {
                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
                }
                else if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamAndYear"));
                }
            }
            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICZN");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Botanical")) {
            BotanicalName taxonName = (BotanicalName) parseScientificName(fullName);
            if (taxonName != null){
                return taxonName;
            }
            else{
                taxonName = BotanicalName.NewInstance(null);
            }
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "FirstEpithet")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "InfraSpeEpithet")));
            try {
                taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap, "Rank")));
            } catch (Exception e) {
                if (taxonName.getGenusOrUninomial() != null){
                    taxonName.setRank(Rank.GENUS());
                }
                else if (taxonName.getInfraGenericEpithet() != null){
                    taxonName.setRank(Rank.SUBGENUS());
                }
                else if (taxonName.getSpecificEpithet() != null){
                    taxonName.setRank(Rank.SPECIES());
                }
                else if (taxonName.getInfraSpecificEpithet() != null){
                    taxonName.setRank(Rank.SUBSPECIES());
                }
            }
            Team team = null;
            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
                taxonName.setBasionymAuthorTeam(team);
            }
            if (getFromMap(atomisedMap, "AuthorTeam") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeam"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (team == null) {
                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
                }
                else if (getFromMap(atomisedMap, "AuthorTeam") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeam"));
                }
            }
            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICBN");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Bacterial")) {
            NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap, "Species")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "SubspeciesEpithet")));

            if (taxonName.getGenusOrUninomial() != null){
                taxonName.setRank(Rank.GENUS());
            }
            else if (taxonName.getInfraGenericEpithet() != null){
                taxonName.setRank(Rank.SUBGENUS());
            }
            else if (taxonName.getSpecificEpithet() != null){
                taxonName.setRank(Rank.SPECIES());
            }
            else if (taxonName.getInfraSpecificEpithet() != null){
                taxonName.setRank(Rank.SUBSPECIES());
            }

            if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                Team team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear") != null) {
                Team team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear"), true);
                taxonName.setBasionymAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICNB");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Cultivar")) {
            CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

            if (taxonName.hasProblem()) {
                logger.info("pb ICNCP");
                problem = true;
            }
            else {
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


    /**
     * Get a formated string from a hashmap
     * @param atomisedMap
     * @param key
     * @return
     */
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
                    }
                    else {
                        value = null;
                    }
                }
                else {
                    value = null;
                }
            }
        }
        catch (Exception e) {
            value = null;
        }
        return value;
    }

    //    private void compareABCDtoCDM(URI urlFileName, List<String> knownElts, Abcd206XMLFieldGetter abcdFieldGetter) {
    //        try {
    //            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //            DocumentBuilder constructeur = factory.newDocumentBuilder();
    //            URL url = urlFileName.toURL();
    //            Object o = url.getContent();
    //            InputStream is = (InputStream) o;
    //            Document document = constructeur.parse(is);
    //            Element root = document.getDocumentElement();
    //            abcdFieldGetter.traverse(root);
    //        }
    //        catch (ParserConfigurationException e){
    //            e.printStackTrace();
    //        }
    //        catch (SAXException e) {
    //            e.printStackTrace();
    //        }
    //        catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        Set<String> elts = dataHolder.allABCDelements.keySet();
    //        Iterator<String> it = elts.iterator();
    //        String elt;
    //        while (it.hasNext()) {
    //            elt = it.next();
    //            if (knownElts.indexOf(elt) == -1) {
    //                if(DEBUG) {
    //                    logger.info("Unmerged ABCD element: " + elt + " - "+ dataHolder.allABCDelements.get(elt));
    //                }
    //            }
    //        }
    //    }

    /**
     * Load the list of names from the ABCD file and save them
     * @param state : the current ABCD import state
     * @param unitsList : the unit list from the ABCD file
     * @param abcdFieldGetter : the ABCD parser
     */
    private void prepareCollectors(Abcd206ImportState state, NodeList unitsList, Abcd206XMLFieldGetter abcdFieldGetter) {
        List<String> collectors = new ArrayList<String>();
        List<String> teams = new ArrayList<String>();
        List<List<String>> collectorinteams = new ArrayList<List<String>>();

        for (int i = 0; i < unitsList.getLength(); i++) {
            this.getCollectorsFromXML((Element) unitsList.item(i), abcdFieldGetter);
            for (String agent : dataHolder.gatheringAgentList) {
                collectors.add(agent);
            }
            List<String> tmpTeam = new ArrayList<String>(new HashSet<String>(dataHolder.gatheringTeamList));
            if(!tmpTeam.isEmpty()) {
                teams.add(StringUtils.join(tmpTeam.toArray()," & "));
            }
            for (String agent:tmpTeam) {
                collectors.add(agent);
            }
        }

        List<String> collectorsU = new ArrayList<String>(new HashSet<String>(collectors));
        List<String> teamsU = new ArrayList<String>(new HashSet<String>(teams));


        //existing teams in DB
        Map<String,Team> titleCacheTeam = new HashMap<String, Team>();
        List<UuidAndTitleCache<Team>> hiberTeam = getAgentService().getTeamUuidAndTitleCache();

        Set<UUID> uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Team> hibernateT:hiberTeam){
            uuids.add(hibernateT.getUuid());
        }
        if (!uuids.isEmpty()){
            List<AgentBase> existingTeams = getAgentService().find(uuids);
            for (AgentBase<?> existingP:existingTeams){
                titleCacheTeam.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Team.class));
            }
        }


        Map<String,UUID> teamMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Team> uuidt:hiberTeam){
            teamMap.put(uuidt.getTitleCache(), uuidt.getUuid());
        }

        //existing persons in DB
        List<UuidAndTitleCache<Person>> hiberPersons = getAgentService().getPersonUuidAndTitleCache();
        Map<String,Person> titleCachePerson = new HashMap<String, Person>();
        uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
            uuids.add(hibernateP.getUuid());
        }

        if (!uuids.isEmpty()){
            List<AgentBase> existingPersons = getAgentService().find(uuids);
            for (AgentBase<?> existingP:existingPersons){
                titleCachePerson.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Person.class));
            }
        }

        Map<String,UUID> personMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Person> person:hiberPersons){
            personMap.put(person.getTitleCache(), person.getUuid());
        }

        java.util.Collection<AgentBase> personToadd = new ArrayList<AgentBase>();
        java.util.Collection<AgentBase> teamToAdd = new ArrayList<AgentBase>();

        for (String collector:collectorsU){
            Person p = Person.NewInstance();
            p.setTitleCache(collector,true);
            if (!personMap.containsKey(p.getTitleCache())){
                personToadd.add(p);
            }
        }
        for (String team:teamsU){
            Team p = Team.NewInstance();
            p.setTitleCache(team,true);
            if (!teamMap.containsKey(p.getTitleCache())){
                teamToAdd.add(p);
            }
        }

        if(!personToadd.isEmpty()){
            Map<UUID, AgentBase> uuuidPerson = getAgentService().save(personToadd);
            for (UUID u:uuuidPerson.keySet()){
                titleCachePerson.put(uuuidPerson.get(u).getTitleCache(),CdmBase.deproxy(uuuidPerson.get(u),Person.class) );
            }
        }

        Person ptmp ;
        Map <String,Integer>teamdone = new HashMap<String, Integer>();
        for (List<String> collteam: collectorinteams){
            if (!teamdone.containsKey(StringUtils.join(collteam.toArray(),"-"))){
                Team team = new Team();
                boolean em =true;
                for (String collector:collteam){
                    ptmp = Person.NewInstance();
                    ptmp.setTitleCache(collector,true);
                    Person p2 = titleCachePerson.get(ptmp.getTitleCache());
                    team.addTeamMember(p2);
                    em=false;
                }
                if (!em) {
                    teamToAdd.add(team);
                }
                teamdone.put(StringUtils.join(collteam.toArray(),"-"),0);
            }
        }

        if(!teamToAdd.isEmpty()){
            Map<UUID, AgentBase> uuuidTeam =  getAgentService().save(teamToAdd);
            for (UUID u:uuuidTeam.keySet()){
                titleCacheTeam.put(uuuidTeam.get(u).getTitleCache(), CdmBase.deproxy( uuuidTeam.get(u),Team.class) );
            }
        }

        state.getConfig().setTeams(titleCacheTeam);
        state.getConfig().setPersons(titleCachePerson);
    }

    @Override
    protected boolean isIgnore(Abcd206ImportState state) {
        return false;
    }


}
