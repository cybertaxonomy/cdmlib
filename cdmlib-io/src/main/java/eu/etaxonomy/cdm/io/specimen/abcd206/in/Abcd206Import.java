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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.SpecimenIoBase;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
@Component
public class Abcd206Import extends SpecimenIoBase<Abcd206ImportConfigurator, Abcd206ImportState> implements ICdmIO<Abcd206ImportState> {
	private static final Logger logger = Logger.getLogger(Abcd206Import.class);


	public Abcd206Import() {
		super();
	}

	
	@Override
	protected boolean doCheck(Abcd206ImportState state) {
		logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
		return true;
	}
	
	
	@Override
	public boolean doInvoke(Abcd206ImportState state){
		logger.info("INVOKE Specimen Import from ABCD2.06 XML File");
		boolean result = true;
		Abcd206ImportConfigurator config = state.getConfig();
		//AbcdIO test = new AbcdIO();
		String sourceName = config.getSource();
		NodeList unitsList = getUnitsNodeList(sourceName);
		if (unitsList != null){
			String message = "nb units to insert: "+unitsList.getLength();
			logger.info(message);
			config.updateProgress(message);
			
			Abcd206DataHolder dataHolder = new Abcd206DataHolder();
			
			for (int i=0 ; i<unitsList.getLength() ; i++){
				this.setUnitPropertiesXML((Element)unitsList.item(i), dataHolder);
				result &= this.handleSingleUnit(config, dataHolder);
				
				//compare the ABCD elements added in to the CDM and the unhandled ABCD elements
				compareABCDtoCDM(sourceName, dataHolder.knownABCDelements, dataHolder);
								
				//reset the ABCD elements added in CDM
				//knownABCDelements = new ArrayList<String>();
				dataHolder.allABCDelements = new HashMap<String,String>();
			}
		}

		return result;

	}
	
	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	private boolean handleSingleUnit(Abcd206ImportConfigurator config, Abcd206DataHolder dataHolder){
		boolean result = true;

		TransactionStatus tx = startTransaction();
		try {
			config.updateProgress("Importing data for unit: " + dataHolder.unitID);
			
//			ReferenceBase sec = Database.NewInstance();
//			sec.setTitleCache("XML DATA");
			ReferenceBase sec = config.getTaxonReference();

			//create facade
			DerivedUnitFacade derivedUnitFacade = getFacade(dataHolder);
			
			
			//handle identifications
			handleIdentifications(config, derivedUnitFacade, sec, dataHolder);

			//handle collection data
			setCollectionData(config, dataHolder, derivedUnitFacade);

			/**
			 * GATHERING EVENT
			 */

			//gathering event
			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(
					getTermService(), dataHolder.locality, dataHolder.languageIso, dataHolder.longitude, 
					dataHolder.latitude, dataHolder.gatheringAgentList);
			
			//country
			UnitsGatheringArea unitsGatheringArea = 
				new UnitsGatheringArea(dataHolder.isocountry, dataHolder.country, getOccurrenceService());
			NamedArea areaCountry = unitsGatheringArea.getArea();
			
			//other areas
			unitsGatheringArea = new UnitsGatheringArea(dataHolder.namedAreaList);
			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
			for (NamedArea namedArea : nas){
				unitsGatheringEvent.addArea(namedArea);
			}
			
			//copy gathering event to facade
			GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
			derivedUnitFacade.setLocality(gatheringEvent.getLocality());
			derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
			derivedUnitFacade.setCollector(gatheringEvent.getCollector());
			derivedUnitFacade.addCollectingArea(areaCountry);
			//FIXME setCountry
			derivedUnitFacade.addCollectingArea(areaCountry);
			derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());
			
			//TODO exsiccatum
			
			
			//add fieldNumber
			derivedUnitFacade.setFieldNumber(dataHolder.fieldNumber);
			
			//join gatheringEvent to fieldObservation

//			//add Multimedia URLs
			if(dataHolder.multimediaObjects.size() > 0){
				MediaRepresentation representation;
				Media media;
				ImageMetaData imd ;
				URL url ;
				ImageFile imf;
				for (String multimediaObject : dataHolder.multimediaObjects){
					if( multimediaObject != null){
						imd = ImageMetaData.newInstance();
						url = new URL(multimediaObject);
						try {
							imd.readMetaData(url.toURI(), 0);
						} catch (Exception e) {
							String message = "An error occurred when trying to read image meta data: " +  e.getMessage();
							logger.warn(message);
						}
						//TODO do we really want to check the url?
						if (imd != null){
							imf = ImageFile.NewInstance(multimediaObject, null, imd);
							representation = MediaRepresentation.NewInstance();
							representation.addRepresentationPart(imf);
							media = Media.NewInstance();
							media.addRepresentation(representation);
							
							derivedUnitFacade.addFieldObjectMedia(media);
						}
					}
				}
			}
			
			/**
			 * SAVE AND STORE DATA
			 */			
			getTermService().save(areaCountry);//TODO save area sooner
			for (NamedArea area : nas){
				getTermService().save(area);//save it sooner (foreach area)
			}
			getTermService().saveLanguageData(unitsGatheringEvent.getLocality());//TODO needs to be saved ?? save it sooner
			
			getOccurrenceService().save(derivedUnitFacade.getDerivedUnit());
			logger.info("saved ABCD specimen ...");


		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		commitTransaction(tx);

		return result;
	}


	private void setCollectionData(Abcd206ImportConfigurator config,
			Abcd206DataHolder dataHolder, DerivedUnitFacade derivedUnitFacade) {
		//set catalogue number (unitID)
		derivedUnitFacade.setCatalogNumber(dataHolder.unitID);
		derivedUnitFacade.setAccessionNumber(dataHolder.accessionNumber);
		derivedUnitFacade.setCollectorsNumber(dataHolder.collectorsNumber);


		/**
		 * INSTITUTION & COLLECTION
		 */
		//manage institution
		Institution institution = this.getInstitution(dataHolder.institutionCode, config, dataHolder);
		//manage collection
		Collection collection = this.getCollection(dataHolder.collectionCode, institution, config, dataHolder); 
		//link specimen & collection
		derivedUnitFacade.setCollection(collection);
	}


	private DerivedUnitFacade getFacade(Abcd206DataHolder dataHolder) {
		/**
		 * SPECIMEN OR OBSERVATION OR LIVING
		 */
//			DerivedUnitBase derivedThing = null;
		DerivedUnitType type = null;
		
		//create specimen
		if (dataHolder.recordBasis != null){
			if (dataHolder.recordBasis.toLowerCase().startsWith("s")) {//specimen
				type = DerivedUnitType.Specimen;
			}else if (dataHolder.recordBasis.toLowerCase().startsWith("o")) {//observation
				type = DerivedUnitType.Observation;	
			}else if (dataHolder.recordBasis.toLowerCase().startsWith("l")) {//living -> fossil, herbarium sheet....???
				type = DerivedUnitType.LivingBeing;
			}
			if (type == null){
				logger.info("The basis of record does not seem to be known: " + dataHolder.recordBasis);
				type = DerivedUnitType.DerivedUnit;
			}
			//TODO fossils?
		}else{
			logger.info("The basis of record is null");
			type = DerivedUnitType.DerivedUnit;
		}
		DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
		return derivedUnitFacade;
	}


	/*
	 * Return the list of root nodes for an ABCD 2.06 XML file
	 * @param fileName: the file's location
	 * @return the list of root nodes ("Unit")
	 */
	private static NodeList getUnitsNodeList(String urlFileName){
		NodeList unitList = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			URL url = new URL(urlFileName);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Document document = builder.parse(is);
			Element root = document.getDocumentElement();
			unitList = root.getElementsByTagName("Unit");

		}catch(Exception e){
			logger.warn(e);
		}
		return unitList;
	}


	/*
	 * Store the unit's properties into variables
	 * Look which unit is the preferred one
	 * Look what kind of name it is supposed to be, for the parsing (Botanical, Zoological)
	 * @param racine: the root node for a single unit
	 */
	private void setUnitPropertiesXML(Element root, Abcd206DataHolder dataHolder){
		try{
			NodeList group;
			
//			try{afficherInfos(racine, 0);}catch (Exception e) {logger.info(e);}
			group = root.getChildNodes();
//			logger.info("ABCD ELEMENT not stored: "+group.item(i).getNodeName().toString()+" - value: "+group.item(i).getTextContent());
			for (int i = 0; i < group.getLength(); i++){
				if (group.item(i).getNodeName().equals("Identifications")){
					group = group.item(i).getChildNodes();
					break;
				}
			}
			dataHolder.identificationList = new ArrayList<String>();
			dataHolder.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
			dataHolder.referenceList = new ArrayList<String>();
			dataHolder.multimediaObjects = new ArrayList<String>();

			this.getScientificNames(group, dataHolder);

//			logger.info("this.identificationList "+this.identificationList.toString());
			this.getIDs(root, dataHolder);
			this.getRecordBasis(root, dataHolder);
			this.getMultimedia(root, dataHolder);
			this.getNumbers(root, dataHolder);
			this.getGeolocation(root, dataHolder);
			this.getGatheringPeople(root, dataHolder);

		} catch (Exception e) {
			logger.info("Error occured while parsing XML file"+e);
		}
	}

	String path= "";
	private void getHierarchie(Node node){
		while (node != null && node.getNodeName() != "DataSets"){
//			logger.info(node.getParentNode().getNodeName());
			path = node.getParentNode().getNodeName()+"/"+path; 
			node = node.getParentNode();
		}
//		logger.info("path: "+path);
	}

	private void getScientificNames(NodeList group, Abcd206DataHolder dataHolder){
		NodeList identifications,results;
		String tmpName = null;
		for (int j=0; j< group.getLength(); j++){
			if(group.item(j).getNodeName().equals("Identification")){
				dataHolder.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName().equals("Result")){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++){
							if (results.item(k).getNodeName().equals("TaxonIdentified")){
								tmpName=this.getScientificName(results.item(k), dataHolder);
							}
						}
					}else if(identifications.item(m).getNodeName().equals("PreferredFlag")){
						if (dataHolder.nomenclatureCode != null && dataHolder.nomenclatureCode !=""){
							dataHolder.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent()+"_code_" + dataHolder.nomenclatureCode);
						}else{
							dataHolder.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent());
						}
						path=identifications.item(m).getNodeName();
						getHierarchie(identifications.item(m));
						dataHolder.knownABCDelements.add(path);
						path="";
					}else if (identifications.item(m).getNodeName().equals("References")){
						this.getReferences(identifications.item(m), dataHolder);
					}
				}
			}
		}
		boolean hasPref=false;
		for (int j=0; j< group.getLength(); j++){
			if(group.item(j).getNodeName().equals("Identification")){
				dataHolder.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName().equals("Result")){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++){
							if (results.item(k).getNodeName().equals("TaxonIdentified")){
								tmpName=this.getScientificName(results.item(k), dataHolder);
							}
						}
					}
					if(identifications.item(m).getNodeName().equals("PreferredFlag")){
						hasPref=true;
					}
				}
				if ( !hasPref && tmpName != null){
					if (dataHolder.nomenclatureCode != null && dataHolder.nomenclatureCode !=""){
						dataHolder.identificationList.add(tmpName+"_preferred_"+"0"+"_code_" + dataHolder.nomenclatureCode);
					} else {
						dataHolder.identificationList.add(tmpName+"_preferred_"+"0");
					}
				}
			}
		}
	}



	private void getReferences(Node result, Abcd206DataHolder dataHolder){
		NodeList results,reference;
		results = result.getChildNodes();
		for(int k=0; k<results.getLength();k++){
			if (results.item(k).getNodeName().equals("Reference")){
				reference = results.item(k).getChildNodes();
				for(int l=0;l<reference.getLength();l++){
					if (reference.item(l).getNodeName().equals("TitleCitation")){
						path = reference.item(l).getNodeName();
						dataHolder.referenceList.add(reference.item(l).getTextContent());
						getHierarchie(reference.item(l));
						dataHolder.knownABCDelements.add(path);
						path="";
					}
				}
			}
		}
	}

	private String getScientificName(Node result, Abcd206DataHolder dataHolder){
		NodeList taxonsIdentified, scnames, atomised;
		String tmpName = "";
		dataHolder.atomisedStr = "";
		taxonsIdentified = result.getChildNodes();
		for (int l=0; l<taxonsIdentified.getLength(); l++){
			if (taxonsIdentified.item(l).getNodeName().equals("ScientificName")){
				scnames = taxonsIdentified.item(l).getChildNodes();
				for (int n=0;n<scnames.getLength();n++){
					if (scnames.item(n).getNodeName().equals("FullScientificNameString")){
						path=scnames.item(n).getNodeName();
						tmpName = scnames.item(n).getTextContent();
						getHierarchie(scnames.item(n));
						dataHolder.knownABCDelements.add(path);
						path="";
					}
					if (scnames.item(n).getNodeName().equals("NameAtomised")){
						try {
							if (scnames.item(n).hasChildNodes()){
								dataHolder.nomenclatureCode = scnames.item(n).getChildNodes().item(1).getNodeName();
							}
						} catch (Exception e) {
							dataHolder.nomenclatureCode ="";
						}
						atomised = scnames.item(n).getChildNodes().item(1).getChildNodes();
						dataHolder.atomisedIdentificationList.add(this.getAtomisedNames(dataHolder.nomenclatureCode, atomised, dataHolder));
					}
				}
			}
		}
		return tmpName;
	}

	private HashMap<String,String> getAtomisedNames(String code, NodeList atomised, Abcd206DataHolder dataHolder){
		if (code.equals("Botanical")){
			return this.getAtomisedBotanical(atomised, dataHolder);
		}
		if (code.equals("Bacterial")){
			return this.getAtomisedBacterial(atomised, dataHolder);
		}
		if (code.equals("NameViral")){
			return this.getAtomisedViral(atomised, dataHolder);
		}
		if (code.equals("NameZoological")){
			return this.getAtomisedZoological(atomised,dataHolder);
		}
		return new HashMap<String,String>();
	}

	private HashMap<String,String> getAtomisedZoological(NodeList atomised, Abcd206DataHolder dataHolder){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";	
			}
			if(atomised.item(i).getNodeName().equals("Subgenus")){
				atomisedMap.put("Subgenus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet")){
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet")){
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamOriginalAndYear")){
				atomisedMap.put("AuthorTeamOriginalAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesisAndYear")){
				atomisedMap.put("AuthorTeamParenthesisAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("CombinationAuthorTeamAndYear")){
				atomisedMap.put("CombinationAuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Breed")){
				atomisedMap.put("Breed",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("NamedIndividual")){
				atomisedMap.put("NamedIndividual",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedViral(NodeList atomised, Abcd206DataHolder dataHolder){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("ViralSpeciesDesignation")){
				atomisedMap.put("ViralSpeciesDesignation", atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Acronym")){
				atomisedMap.put("Acronym",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBotanical(NodeList atomised, Abcd206DataHolder dataHolder){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("FirstEpithet")){
				atomisedMap.put("FirstEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("InfraspecificEpithet")){
				atomisedMap.put("InfraSpeEpithet", atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Rank")){
				atomisedMap.put("Rank",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("HybridFlag")){
				atomisedMap.put("HybridFlag",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesis")){
				atomisedMap.put("AuthorTeamParenthesis",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeam")){
				atomisedMap.put("AuthorTeam",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("CultivarGroupName")){
				atomisedMap.put("CultivarGroupName",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("CultivarName")){
				atomisedMap.put("CultivarName",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("TradeDesignationNames")){
				atomisedMap.put("Trade",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBacterial(NodeList atomised, Abcd206DataHolder dataHolder){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Subgenus")){
				atomisedMap.put("SubGenus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubgenusAuthorAndYear")){
				atomisedMap.put("SubgenusAuthorAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet")){
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet")){
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("ParentheticalAuthorTeamAndYear")){
				atomisedMap.put("ParentheticalAuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamAndYear")){
				atomisedMap.put("AuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("NameApprobation")){
				atomisedMap.put("NameApprobation",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private void getIDs(Element root, Abcd206DataHolder dataHolder){
		NodeList group;
		try {
			group = root.getElementsByTagName("SourceInstitutionID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.institutionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.institutionCode= "";
		}
		try {
			group = root.getElementsByTagName("SourceID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.collectionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.collectionCode = "";
		}
		try {
			group = root.getElementsByTagName("UnitID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.unitID = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.unitID = "";
		}
	}

	private void getRecordBasis(Element root, Abcd206DataHolder dataHolder){
		NodeList group;
		try {
			group = root.getElementsByTagName("RecordBasis");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.recordBasis = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.recordBasis = "";
		}
	}

	private void getMultimedia(Element root, Abcd206DataHolder dataHolder){
		NodeList group, multimedias, multimedia;
		try {
			group = root.getElementsByTagName("MultiMediaObjects");
			for(int i=0;i<group.getLength();i++){
				multimedias = group.item(i).getChildNodes();
				for (int j=0;j<multimedias.getLength();j++){
					if (multimedias.item(j).getNodeName().equals("MultiMediaObject")){	
						multimedia = multimedias.item(j).getChildNodes();
						for (int k=0;k<multimedia.getLength();k++){
							if(multimedia.item(k).getNodeName().equals("FileURI")){
								dataHolder.multimediaObjects.add(multimedia.item(k).getTextContent());
								path = multimedia.item(k).getNodeName();
								getHierarchie(multimedia.item(k));
								dataHolder.knownABCDelements.add(path);
								path="";	
							}
						}
					}
				}
			}
		} catch (NullPointerException e) {
			logger.info(e);
		}
	}

	private void getNumbers(Element root, Abcd206DataHolder dataHolder){
		NodeList group;
		try {
			group = root.getElementsByTagName("AccessionNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";	
			dataHolder.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.accessionNumber = "";
		}
		try {
			group = root.getElementsByTagName("CollectorsFieldNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.fieldNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.fieldNumber = "";
		}

		try {
			group = root.getElementsByTagName("CollectorsNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.collectorsNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.collectorsNumber = "";
		}

		try {
			group = root.getElementsByTagName("AccessionNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.accessionNumber = "";
		}
	}

	private void getGeolocation(Element root, Abcd206DataHolder dataHolder){
		NodeList group, childs;
		try {
			group = root.getElementsByTagName("LocalityText");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.locality = group.item(0).getTextContent();
			if (group.item(0).hasAttributes())
				if (group.item(0).getAttributes().getNamedItem("lang") != null)
					dataHolder.languageIso = group.item(0).getAttributes().getNamedItem("lang").getTextContent();
		} catch (NullPointerException e) {
			dataHolder.locality = "";
		}
		try {
			group = root.getElementsByTagName("LongitudeDecimal");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.longitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			dataHolder.longitude=0.0;
		}
		try {
			group = root.getElementsByTagName("LatitudeDecimal");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.latitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			dataHolder.latitude=0.0;
		}
		try {
			group = root.getElementsByTagName("Country");
			childs = group.item(0).getChildNodes();
			for (int i=0;i<childs.getLength(); i++){
				if(childs.item(i).getNodeName() == "Name"){
					path=childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					dataHolder.knownABCDelements.add(path);
					path="";
					dataHolder.country = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			dataHolder.country = "";
		}
		try {
			group = root.getElementsByTagName("Country");
			childs = group.item(0).getChildNodes();
			for (int i=0;i<childs.getLength(); i++){
				if(childs.item(i).getNodeName() == "ISO3166Code"){
					path=childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					dataHolder.knownABCDelements.add(path);
					path="";
					dataHolder.isocountry = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			dataHolder.isocountry = "";
		}
		try {
			group = root.getElementsByTagName("Altitude");
			for (int i=0;i<group.getLength();i++){
				childs = group.item(i).getChildNodes();
				for (int j=0;j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("MeasurementOrFactText")){
						path=childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						dataHolder.knownABCDelements.add(path);
						path="";
						dataHolder.altitude = Integer.valueOf(childs.item(j).getTextContent());
					}
				}
			}
		} catch (NullPointerException e) {
			dataHolder.altitude = -9999;
		}

		try {
			group = root.getElementsByTagName("Depth");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path="";
			dataHolder.depth = Integer.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			dataHolder.depth = -9999;
		}

		try{
			group = root.getElementsByTagName("NamedArea");
			dataHolder.namedAreaList = new ArrayList<String>();
			for (int i=0;i<group.getLength();i++){
				childs = group.item(i).getChildNodes();
				for (int j=0; j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("AreaName")){
						path = childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						dataHolder.knownABCDelements.add(path);
						path="";
						dataHolder.namedAreaList.add(childs.item(j).getTextContent());
					}
				}
			}
		}catch(NullPointerException e){
			dataHolder.namedAreaList = new ArrayList<String>();
		}
	}

	private void getGatheringPeople(Element root, Abcd206DataHolder dataHolder){
		NodeList group, childs, person;
		try {
			group = root.getElementsByTagName("GatheringAgent");
			dataHolder.gatheringAgentList = new ArrayList<String>();
			for (int i=0; i< group.getLength(); i++){
				childs = group.item(i).getChildNodes();
				for (int j=0; j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("Person")){
						person = childs.item(j).getChildNodes();
						for (int k=0; k<person.getLength(); k++){
							if (person.item(k).getNodeName().equals("FullName")){
								path=person.item(k).getNodeName();
								getHierarchie(person.item(k));
								dataHolder.knownABCDelements.add(path);
								path="";
								dataHolder.gatheringAgentList.add(person.item(k).getTextContent());
							}
						}
					}

				}
			}
		} catch (NullPointerException e) {
			dataHolder.gatheringAgentList = new ArrayList<String>();
		}
	}

	private Institution getInstitution(String institutionCode, Abcd206ImportConfigurator config, Abcd206DataHolder dataHolder){
		Institution institution;
		List<Institution> institutions;
		try{
			logger.info(dataHolder.institutionCode);
			institutions = getAgentService().searchInstitutionByCode(dataHolder.institutionCode);
		}catch(Exception e){
			institutions=new ArrayList<Institution>();
		}
		if (institutions.size() ==0 || !config.getReUseExistingMetadata()){
			logger.info("Institution (agent) unknown or not allowed to reuse existing metadata");
			//create institution
			institution = Institution.NewInstance();
			institution.setCode(dataHolder.institutionCode);				
		}
		else{
			logger.info("Institution (agent) already in the db");
			institution = institutions.get(0);
		}
		return institution;
	}

	/*
	 * Look if the Collection does already exists
	 * @param collectionCode: a string
	 * @param institution: the current Institution
	 * @param app
	 * @return the Collection (existing or new)
	 */
	private Collection getCollection(String collectionCode, Institution institution, Abcd206ImportConfigurator config, Abcd206DataHolder dataHolder){
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try{
			collections = getCollectionService().searchByCode(dataHolder.collectionCode);
		}catch(Exception e){
			collections=new ArrayList<Collection>();
		}
		if (collections.size() ==0 || !config.getReUseExistingMetadata()){
			logger.info("Collection not found or do not reuse existing metadata  " + dataHolder.collectionCode);
			//create new collection
			collection.setCode(dataHolder.collectionCode);
			collection.setCodeStandard("GBIF");
			collection.setInstitute(institution);
		}
		else{
			boolean collectionFound=false;
			for (int i=0; i<collections.size(); i++){
				collection = collections.get(i);
				try {
					if (collection.getInstitute().getCode().equalsIgnoreCase(institution.getCode())){ 
						//found a collection with the same code and the same institution
						collectionFound=true;
					}
				} catch (NullPointerException e) {}
			}
			if (!collectionFound){ 
				collection.setCode(dataHolder.collectionCode);
				collection.setCodeStandard("GBIF");
				collection.setInstitute(institution);
			}

		}
		return collection;
	}

	/*
	 * 
	 * @param app
	 * @param derivedThing
	 * @param sec
	 */
	private void handleIdentifications(Abcd206ImportConfigurator config, DerivedUnitFacade facade, ReferenceBase sec, Abcd206DataHolder dataHolder){
		NonViralName<?> taxonName = null;
		String fullScientificNameString;
		Taxon taxon = null;
		DeterminationEvent determinationEvent = null;
		List<TaxonBase> names = null;

		String scientificName="";
		boolean preferredFlag=false;

		for (int i = 0; i < dataHolder.identificationList.size(); i++) {
			fullScientificNameString = dataHolder.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");
			if (fullScientificNameString.indexOf("_preferred_") != -1){
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1].split("_code_")[0];
				if (pTmp.equals("1") || pTmp.toLowerCase().indexOf("true") != -1){
					preferredFlag=true;
				} else { 
					preferredFlag=false;
				}
			}
			else{ 
				scientificName = fullScientificNameString;
			}
			logger.info(fullScientificNameString);
			if (fullScientificNameString.indexOf("_code_") != -1){	
				dataHolder.nomenclatureCode = fullScientificNameString.split("_code_")[1];
			}
			if (config.getDoAutomaticParsing() || dataHolder.atomisedIdentificationList == null || dataHolder.atomisedIdentificationList.size()==0){	
				taxonName = this.parseScientificName(scientificName, dataHolder);	
			} else {
				if (dataHolder.atomisedIdentificationList != null || dataHolder.atomisedIdentificationList.size()>0){
					taxonName = this.setTaxonNameByType(dataHolder.atomisedIdentificationList.get(i), scientificName, dataHolder);
				}
			}
			if(taxonName == null){
				taxonName = NonViralName.NewInstance(null);
				taxonName.setFullTitleCache(scientificName);
			}
			if (config.getDoReUseTaxon()){
				try{
					names = getTaxonService().searchTaxaByName(scientificName, sec);
					taxon = (Taxon)names.get(0);
				} catch(Exception e){
					taxon=null;
				}
			}
//			taxonName = NonViralName.NewInstance(null);
//			taxonName.setFullTitleCache(scientificName);

			if (!config.getDoReUseTaxon() || taxon == null){
				getNameService().save(taxonName);
				taxon = Taxon.NewInstance(taxonName, sec); //TODO sec set null
			}
			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
			
			for (String strReference : dataHolder.referenceList){
				
				ReferenceBase reference = ReferenceFactory.newGeneric();
				reference.setTitleCache(strReference, true);
				determinationEvent.addReference(reference);
			}
			facade.addDetermination(determinationEvent);
		}

	}

	private NonViralName<?> parseScientificName(String scientificName, Abcd206DataHolder dataHolder){
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?>taxonName = null;
		boolean problem=false;

		if (dataHolder.nomenclatureCode.toString().equals("Zoological")){
			taxonName = (ZoologicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN,null);
			if (taxonName.hasProblem()){
				problem=true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Botanical")){
			taxonName  = (BotanicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICBN,null);
			if (taxonName.hasProblem()){
				problem=true;;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Bacterial")){
			taxonName = (BacterialName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB, null);
			if (taxonName.hasProblem()){
				problem=true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Cultivar")){
			taxonName = (CultivarPlantName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP, null);
			if (taxonName.hasProblem()){
				problem=true;
			}
		}
//		if (this.nomenclatureCode.toString().equals("Viral")){
//		ViralName taxonName = (ViralName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICVCN(), null);
//		if (taxonName.hasProblem())
//		logger.info("pb ICVCN");
//		}
		//TODO: parsing of ViralNames?
		if(problem){
			taxonName = NonViralName.NewInstance(null);
			taxonName.setTitleCache(scientificName, true);
		}
		return taxonName;

	}

	private NonViralName<?> setTaxonNameByType(HashMap<String, String> atomisedMap,String fullName, Abcd206DataHolder dataHolder){
		if (dataHolder.nomenclatureCode.equals("Zoological")){
			NonViralName<ZoologicalName> taxonName  = ZoologicalName.NewInstance(null); 
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap,"Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,"SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,"SpeciesEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"SubspeciesEpithet"));
			Team team = null;
			if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"), true);
			}else{
				if (getFromMap(atomisedMap,"AuthorTeamAndYear") != null){
					team = Team.NewInstance();
					team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamAndYear"), true);
				}
			}
			if(team != null){
				taxonName.setBasionymAuthorTeam(team);
			}else{
				if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null){
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
				} else if (getFromMap(atomisedMap,"AuthorTeamAndYear") != null){
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamAndYear"));
				}
			}
			if(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear"), true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()){
				logger.info("pb ICZN");
			}else{
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Botanical")){
			NonViralName<BotanicalName> taxonName  = BotanicalName.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap,"Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,"FirstEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"InfraSpeEpithet"));
			try{taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap,"Rank")));
			}catch(Exception e){}
			Team team = null;
			if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"), true);
				if(team != null){
					taxonName.setBasionymAuthorTeam(team);
				}
			}
			if (getFromMap(atomisedMap,"AuthorTeam") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeam"), true);
				if(team != null){
					taxonName.setCombinationAuthorTeam(team);
				}
			}
			if (team == null)	{
				if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null){
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
				}else if (getFromMap(atomisedMap,"AuthorTeam") != null){
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeam"));
				}
			}
			if(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear"), true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()){
				logger.info("pb ICBN");
			}else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Bacterial")){
			NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap,"Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,"SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,"Species"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"SubspeciesEpithet"));
			if(getFromMap(atomisedMap,"AuthorTeamAndYear") != null){
				Team team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamAndYear"), true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if(getFromMap(atomisedMap,"ParentheticalAuthorTeamAndYear") != null){
				Team team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"ParentheticalAuthorTeamAndYear"), true);
				taxonName.setBasionymAuthorTeam(team);
			}
			if (taxonName.hasProblem()){
				logger.info("pb ICNB");
			}else{
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Cultivar")){
			CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

			if (taxonName.hasProblem()){
				logger.info("pb ICNCP");
			}else {
				return taxonName;
			}
		}
//		if (this.nomenclatureCode.equals("Viral")){
//		ViralName taxonName = ViralName.NewInstance(null);
//		taxonName.setFullTitleCache(fullName, true);
//		taxonName.setAcronym(getFromMap(atomisedMap,"Acronym"));
//		if (taxonName.hasProblem())
//		logger.info("pb ICVCN");
//		else return taxonName;
//		}
		//TODO ViralName
		NonViralName<?>taxonName = NonViralName.NewInstance(null);
		taxonName.setFullTitleCache(fullName, true);
		return taxonName;
	}

	private String getFromMap(HashMap<String, String> atomisedMap, String key){
		String value = null;
		if (atomisedMap.containsKey(key)){
			value = atomisedMap.get(key);
		}
		try{
			if (value != null && key.matches(".*Year.*")){
				value=value.trim();
				if (value.matches("[a-z A-Z ]*[0-9]{4}$")){
					String tmp=value.split("[0-9]{4}$")[0];
					int year = Integer.parseInt(value.split(tmp)[1]);
					if (year >= 1752){
						value=tmp;
					}else{
						value=null;
					}
				}else{
					value=null;
				}
			}
		}catch(Exception e){value=null;}

		return value;
	}

	private void compareABCDtoCDM(String urlFileName, ArrayList<String> knownElts, Abcd206DataHolder dataHolder){

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructeur = factory.newDocumentBuilder();
			URL url = new URL(urlFileName);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Document document = constructeur.parse(is);
			Element root = document.getDocumentElement();
			traverse(root, dataHolder);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> elts = dataHolder.allABCDelements.keySet();
		Iterator< String>it = elts.iterator();
		String elt;
		while (it.hasNext()){
			elt = it.next();
			if (knownElts.indexOf(elt) == -1){
				logger.info("Unsaved ABCD element: " + elt + " - " + dataHolder.allABCDelements.get(elt));
			}
		}
	}
	
	

	/**
	 * Traverses the tree for compareABCDtoCDM
	 * @param node
	 * @param dataHolder
	 */
	private void traverse(Node node, Abcd206DataHolder dataHolder){
		// Extract node info:
		String test = node.getTextContent();

		// Print and continue traversing.
		if(test != null && test != "#text" && node.getNodeName() != "#text" && test.split("\n").length==1 && test.length()>0){
			path=node.getNodeName();
			getHierarchie(node);
			dataHolder.allABCDelements.put(path,test);
			path="";
		}
		// Now traverse the rest of the tree in depth-first order.
		if (node.hasChildNodes()) {
			// Get the children in a list.
			NodeList nl = node.getChildNodes();
			// How many of them?
			int size = nl.getLength();
			for (int i=0; i<size; i++){
				// Recursively traverse each of the children.
				traverse (nl.item(i), dataHolder);
			}
		}
	}



	@Override
	protected boolean isIgnore(Abcd206ImportState state) {
		//return ! config.isDoNameFacts();
		return false;
	}



}
