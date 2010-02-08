/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import eu.etaxonomy.cdm.common.mediaMetaData.MediaMetaData;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.IGeneric;
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
public class AbcdIO extends SpecimenIoBase implements ICdmIO<SpecimenImportState> {
	private static final Logger logger = Logger.getLogger(AbcdIO.class);

	protected String fullScientificNameString;
	protected String atomisedStr;
	protected String nomenclatureCode;
	protected String institutionCode;
	protected String collectionCode;
	protected String unitID;
	protected String recordBasis;
	protected String accessionNumber;
	protected String collectorsNumber;
	protected String fieldNumber;
	protected Double longitude;
	protected Double latitude;
	protected String locality;
	protected String languageIso = null;
	protected String country;
	protected String isocountry;
	protected int depth;
	protected int altitude;
	protected ArrayList<String> gatheringAgentList;
	protected ArrayList<String> identificationList;
	protected ArrayList<HashMap<String, String>> atomisedIdentificationList;
	protected ArrayList<String> namedAreaList;
	protected ArrayList<String> referenceList;
	protected ArrayList<String> multimediaObjects;


	protected ArrayList<String> knownABCDelements = new ArrayList<String>();
	protected HashMap<String,String> allABCDelements = new HashMap<String,String>();


	public AbcdIO() {
		super();
	}

	
	@Override
	protected boolean doCheck(SpecimenImportState state) {
		logger.warn("Checking not yet implemented for AbcdIO.class");
		return true;
	}

	/*
	 * Return the list of root nodes for an ABCD 2.06 XML file
	 * @param fileName: the file's location
	 * @return the list of root nodes ("Unit")
	 */
	private static NodeList getUnitsNodeList(String urlFileName){
		NodeList unitList = null;
		try {
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
			URL url = new URL(urlFileName);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Document document = constructeur.parse(is);
			Element racine = document.getDocumentElement();
			unitList = racine.getElementsByTagName("Unit");

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
	private void setUnitPropertiesXML(Element racine){
		try{
			NodeList group;

//			try{afficherInfos(racine, 0);}catch (Exception e) {logger.info(e);}
			group = racine.getChildNodes();
//			logger.info("ABCD ELEMENT not stored: "+group.item(i).getNodeName().toString()+" - value: "+group.item(i).getTextContent());
			for (int i=0; i< group.getLength(); i++){
				if (group.item(i).getNodeName().equals("Identifications")){
					group = group.item(i).getChildNodes();
					break;
				}
			}
			this.identificationList = new ArrayList<String>();
			this.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
			this.referenceList = new ArrayList<String>();
			this.multimediaObjects = new ArrayList<String>();

			this.getScientificNames(group);

//			logger.info("this.identificationList "+this.identificationList.toString());
			this.getIDs(racine);
			this.getRecordBasis(racine);
			this.getMultimedia(racine);
			this.getNumbers(racine);
			this.getGeolocation(racine);
			this.getGatheringPeople(racine);

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

	private void getScientificNames(NodeList group){
		NodeList identifications,results;
		String tmpName = null;
		for (int j=0; j< group.getLength(); j++){
			if(group.item(j).getNodeName().equals("Identification")){
				this.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName().equals("Result")){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++)
							if (results.item(k).getNodeName().equals("TaxonIdentified")){
								tmpName=this.getScientificName(results.item(k));
							}
					}
					else if(identifications.item(m).getNodeName().equals("PreferredFlag")){
						if (this.nomenclatureCode != null && this.nomenclatureCode !="")
							this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent()+"_code_"+this.nomenclatureCode);
						else
							this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent());
						path=identifications.item(m).getNodeName();
						getHierarchie(identifications.item(m));
						knownABCDelements.add(path);
						path="";
					}

					else if (identifications.item(m).getNodeName().equals("References"))
						this.getReferences(identifications.item(m));
				}
			}
		}
		boolean hasPref=false;
		for (int j=0; j< group.getLength(); j++){
			if(group.item(j).getNodeName().equals("Identification")){
				this.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName().equals("Result")){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++)
							if (results.item(k).getNodeName().equals("TaxonIdentified")){
								tmpName=this.getScientificName(results.item(k));
							}
					}
					if(identifications.item(m).getNodeName().equals("PreferredFlag")){
						hasPref=true;
					}
				}
				if ( !hasPref && tmpName != null)
					if (this.nomenclatureCode != null && this.nomenclatureCode !="")
						this.identificationList.add(tmpName+"_preferred_"+"0"+"_code_"+this.nomenclatureCode);
					else
						this.identificationList.add(tmpName+"_preferred_"+"0");
			}
		}

	}



	private void getReferences(Node result){
		NodeList results,reference;
		results = result.getChildNodes();
		for(int k=0; k<results.getLength();k++){
			if (results.item(k).getNodeName().equals("Reference")){
				reference = results.item(k).getChildNodes();
				for(int l=0;l<reference.getLength();l++){
					if (reference.item(l).getNodeName().equals("TitleCitation")){
						path = reference.item(l).getNodeName();
						referenceList.add(reference.item(l).getTextContent());
						getHierarchie(reference.item(l));
						knownABCDelements.add(path);
						path="";
					}
				}
			}
		}
	}

	private String getScientificName(Node result){
		NodeList taxonsIdentified, scnames, atomised;
		String tmpName = "";
		atomisedStr="";
		taxonsIdentified = result.getChildNodes();
		for (int l=0; l<taxonsIdentified.getLength(); l++){
			if (taxonsIdentified.item(l).getNodeName().equals("ScientificName")){
				scnames = taxonsIdentified.item(l).getChildNodes();
				for (int n=0;n<scnames.getLength();n++){
					if (scnames.item(n).getNodeName().equals("FullScientificNameString")){
						path=scnames.item(n).getNodeName();
						tmpName = scnames.item(n).getTextContent();
						getHierarchie(scnames.item(n));
						knownABCDelements.add(path);
						path="";
					}
					if (scnames.item(n).getNodeName().equals("NameAtomised")){
						try {
							if (scnames.item(n).hasChildNodes()){
								this.nomenclatureCode = scnames.item(n).getChildNodes().item(1).getNodeName();
							}
						} catch (Exception e) {
							this.nomenclatureCode ="";
						}
						atomised = scnames.item(n).getChildNodes().item(1).getChildNodes();
						this.atomisedIdentificationList.add(this.getAtomisedNames(nomenclatureCode,atomised));
					}
				}
			}
		}
		return tmpName;
	}

	private HashMap<String,String> getAtomisedNames(String code, NodeList atomised){
		if (code.equals("Botanical"))
			return this.getAtomisedBotanical(atomised);
		if (code.equals("Bacterial"))
			return this.getAtomisedBacterial(atomised);
		if (code.equals("NameViral"))
			return this.getAtomisedViral(atomised);
		if (code.equals("NameZoological"))
			return this.getAtomisedZoological(atomised);
		return new HashMap<String,String>();
	}

	private HashMap<String,String> getAtomisedZoological(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";	
			}
			if(atomised.item(i).getNodeName().equals("Subgenus")){
				atomisedMap.put("Subgenus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet")){
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet")){
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamOriginalAndYear")){
				atomisedMap.put("AuthorTeamOriginalAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";}
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesisAndYear")){
				atomisedMap.put("AuthorTeamParenthesisAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";}
			if(atomised.item(i).getNodeName().equals("CombinationAuthorTeamAndYear")){
				atomisedMap.put("CombinationAuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";}
			if(atomised.item(i).getNodeName().equals("Breed")){
				atomisedMap.put("Breed",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";}
			if(atomised.item(i).getNodeName().equals("NamedIndividual")){
				atomisedMap.put("NamedIndividual",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;

	}

	private HashMap<String,String> getAtomisedViral(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("ViralSpeciesDesignation")){
				atomisedMap.put("ViralSpeciesDesignation", atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Acronym")){
				atomisedMap.put("Acronym",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBotanical(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("FirstEpithet")){
				atomisedMap.put("FirstEpithet",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("InfraspecificEpithet")){
				atomisedMap.put("InfraSpeEpithet", atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Rank")){
				atomisedMap.put("Rank",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("HybridFlag")){
				atomisedMap.put("HybridFlag",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesis")){
				atomisedMap.put("AuthorTeamParenthesis",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeam")){
				atomisedMap.put("AuthorTeam",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("CultivarGroupName")){
				atomisedMap.put("CultivarGroupName",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("CultivarName")){
				atomisedMap.put("CultivarName",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("TradeDesignationNames")){
				atomisedMap.put("Trade",atomised.item(i).getTextContent()); 
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBacterial(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial")){
				atomisedMap.put("Genus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("Subgenus")){
				atomisedMap.put("SubGenus",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubgenusAuthorAndYear")){
				atomisedMap.put("SubgenusAuthorAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet")){
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet")){
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("ParentheticalAuthorTeamAndYear")){
				atomisedMap.put("ParentheticalAuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("AuthorTeamAndYear")){
				atomisedMap.put("AuthorTeamAndYear",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
			if(atomised.item(i).getNodeName().equals("NameApprobation")){
				atomisedMap.put("NameApprobation",atomised.item(i).getTextContent());  
				path=atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				knownABCDelements.add(path);
				path="";
			}
		}
		return atomisedMap;
	}

	private void getIDs(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("SourceInstitutionID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.institutionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.institutionCode= "";
		}
		try {
			group = racine.getElementsByTagName("SourceID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.collectionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.collectionCode = "";
		}
		try {
			group = racine.getElementsByTagName("UnitID");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.unitID = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.unitID = "";
		}
	}

	private void getRecordBasis(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("RecordBasis");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.recordBasis = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.recordBasis = "";
		}
	}

	private void getMultimedia(Element racine){
		NodeList group, multimedias, multimedia;
		try {
			group = racine.getElementsByTagName("MultiMediaObjects");
			for(int i=0;i<group.getLength();i++){
				multimedias = group.item(i).getChildNodes();
				for (int j=0;j<multimedias.getLength();j++){
					if (multimedias.item(j).getNodeName().equals("MultiMediaObject")){	
						multimedia = multimedias.item(j).getChildNodes();
						for (int k=0;k<multimedia.getLength();k++){
							if(multimedia.item(k).getNodeName().equals("FileURI")){
								this.multimediaObjects.add(multimedia.item(k).getTextContent());
								path = multimedia.item(k).getNodeName();
								getHierarchie(multimedia.item(k));
								knownABCDelements.add(path);
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

	private void getNumbers(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("AccessionNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";	
			this.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.accessionNumber = "";
		}
		try {
			group = racine.getElementsByTagName("CollectorsFieldNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.fieldNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.fieldNumber = "";
		}

		try {
			group = racine.getElementsByTagName("CollectorsNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.collectorsNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.collectorsNumber = "";
		}

		try {
			group = racine.getElementsByTagName("AccessionNumber");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.accessionNumber = "";
		}
	}

	private void getGeolocation(Element racine){
		NodeList group, childs;
		try {
			group = racine.getElementsByTagName("LocalityText");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.locality = group.item(0).getTextContent();
			if (group.item(0).hasAttributes())
				if (group.item(0).getAttributes().getNamedItem("lang") != null)
					this.languageIso = group.item(0).getAttributes().getNamedItem("lang").getTextContent();
		} catch (NullPointerException e) {
			this.locality = "";
		}
		try {
			group = racine.getElementsByTagName("LongitudeDecimal");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.longitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			this.longitude=0.0;
		}
		try {
			group = racine.getElementsByTagName("LatitudeDecimal");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.latitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			this.latitude=0.0;
		}
		try {
			group = racine.getElementsByTagName("Country");
			childs = group.item(0).getChildNodes();
			for (int i=0;i<childs.getLength(); i++){
				if(childs.item(i).getNodeName() == "Name"){
					path=childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					knownABCDelements.add(path);
					path="";
					this.country = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			this.country = "";
		}
		try {
			group = racine.getElementsByTagName("Country");
			childs = group.item(0).getChildNodes();
			for (int i=0;i<childs.getLength(); i++){
				if(childs.item(i).getNodeName() == "ISO3166Code"){
					path=childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					knownABCDelements.add(path);
					path="";
					this.isocountry = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			this.isocountry = "";
		}
		try {
			group = racine.getElementsByTagName("Altitude");
			for (int i=0;i<group.getLength();i++){
				childs = group.item(i).getChildNodes();
				for (int j=0;j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("MeasurementOrFactText")){
						path=childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						knownABCDelements.add(path);
						path="";
						this.altitude = Integer.valueOf(childs.item(j).getTextContent());
					}
				}
			}
		} catch (NullPointerException e) {
			this.altitude = -9999;
		}

		try {
			group = racine.getElementsByTagName("Depth");
			path=group.item(0).getNodeName();
			getHierarchie(group.item(0));
			knownABCDelements.add(path);
			path="";
			this.depth = Integer.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			this.depth = -9999;
		}

		try{
			group = racine.getElementsByTagName("NamedArea");
			this.namedAreaList = new ArrayList<String>();
			for (int i=0;i<group.getLength();i++){
				childs = group.item(i).getChildNodes();
				for (int j=0; j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("AreaName")){
						path = childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						knownABCDelements.add(path);
						path="";
						this.namedAreaList.add(childs.item(j).getTextContent());
					}
				}
			}
		}catch(NullPointerException e){
			this.namedAreaList = new ArrayList<String>();
		}
	}

	private void getGatheringPeople(Element racine){
		NodeList group, childs, person;
		try {
			group = racine.getElementsByTagName("GatheringAgent");
			this.gatheringAgentList = new ArrayList<String>();
			for (int i=0; i< group.getLength(); i++){
				childs = group.item(i).getChildNodes();
				for (int j=0; j<childs.getLength();j++){
					if (childs.item(j).getNodeName().equals("Person")){
						person = childs.item(j).getChildNodes();
						for (int k=0; k<person.getLength(); k++)
							if (person.item(k).getNodeName().equals("FullName")){
								path=person.item(k).getNodeName();
								getHierarchie(person.item(k));
								knownABCDelements.add(path);
								path="";
								this.gatheringAgentList.add(person.item(k).getTextContent());
							}
					}

				}
			}
		} catch (NullPointerException e) {
			this.gatheringAgentList = new ArrayList<String>();
		}
	}

	private Institution getInstitution(String institutionCode, SpecimenImportConfigurator config){
		Institution institution;
		List<Institution> institutions;
		try{
			logger.info(this.institutionCode);
			institutions = getAgentService().searchInstitutionByCode(this.institutionCode);
		}catch(Exception e){
			institutions=new ArrayList<Institution>();
		}
		if (institutions.size() ==0 || !config.getReUseExistingMetadata()){
			logger.info("Institution (agent) unknown or not allowed to reuse existing metadata");
			//create institution
			institution = Institution.NewInstance();
			institution.setCode(this.institutionCode);				
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
	private Collection getCollection(String collectionCode, Institution institution, SpecimenImportConfigurator config){
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try{
			collections = getCollectionService().searchByCode(this.collectionCode);
		}catch(Exception e){
			collections=new ArrayList<Collection>();
		}
		if (collections.size() ==0 || !config.getReUseExistingMetadata()){
			logger.info("Collection not found or do not reuse existing metadata  "+this.collectionCode);
			//create new collection
			collection.setCode(this.collectionCode);
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
				collection.setCode(this.collectionCode);
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
	private void setTaxonNameBase(SpecimenImportConfigurator config, DerivedUnitBase derivedThing, ReferenceBase sec){
		NonViralName<?> taxonName = null;
		String fullScientificNameString;
		Taxon taxon = null;
		ReferenceFactory refFatory = ReferenceFactory.newInstance();
		DeterminationEvent determinationEvent = null;
		List<TaxonBase> names = null;

		String scientificName="";
		boolean preferredFlag=false;

		for (int i = 0; i < this.identificationList.size(); i++) {
			fullScientificNameString = this.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");
			if (fullScientificNameString.indexOf("_preferred_") != -1){
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1].split("_code_")[0];
				if (pTmp.equals("1") || pTmp.toLowerCase().indexOf("true") != -1)
					preferredFlag=true;
				else
					preferredFlag=false;
			}
			else scientificName = fullScientificNameString;

			logger.info(fullScientificNameString);
			if (fullScientificNameString.indexOf("_code_") != -1)	
				this.nomenclatureCode = fullScientificNameString.split("_code_")[1];

			if (config.getDoAutomaticParsing() || this.atomisedIdentificationList == null || this.atomisedIdentificationList.size()==0)	
				taxonName = this.parseScientificName(scientificName);	
			else {
				if (this.atomisedIdentificationList != null || this.atomisedIdentificationList.size()>0)
					taxonName = this.setTaxonNameByType(this.atomisedIdentificationList.get(i), scientificName);
			}
			if(taxonName == null){
				taxonName = NonViralName.NewInstance(null);
				taxonName.setFullTitleCache(scientificName);
			}
			if (config.getDoReUseTaxon()){
				try{
					names = getTaxonService().searchTaxaByName(scientificName, sec);
					taxon = (Taxon)names.get(0);
				}
				catch(Exception e){taxon=null;}
			}
//			taxonName = NonViralName.NewInstance(null);
//			taxonName.setFullTitleCache(scientificName);

			if (!config.getDoReUseTaxon() || taxon == null){
				getNameService().save(taxonName);
				taxon = Taxon.NewInstance(taxonName, sec); //sec set null
			}
			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
			
			for (int l=0;l<this.referenceList.size();l++){
				
				ReferenceBase reference = refFatory.newGeneric();
				reference.setTitleCache(this.referenceList.get(l));
				determinationEvent.addReference(reference);
			}
			derivedThing.addDetermination(determinationEvent);
		}

	}

	private NonViralName<?> parseScientificName(String scientificName){
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?>taxonName = null;
		boolean problem=false;

		if (this.nomenclatureCode.toString().equals("Zoological")){
			taxonName = (ZoologicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN,null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Botanical")){
			taxonName  = (BotanicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICBN,null);
			if (taxonName.hasProblem())
				problem=true;;}
		if (this.nomenclatureCode.toString().equals("Bacterial")){
			taxonName = (BacterialName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB, null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Cultivar")){
			taxonName = (CultivarPlantName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP, null);
			if (taxonName.hasProblem())
				problem=true;;
		}
//		if (this.nomenclatureCode.toString().equals("Viral")){
//		ViralName taxonName = (ViralName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICVCN(), null);
//		if (taxonName.hasProblem())
//		logger.info("pb ICVCN");
//		}
		//TODO: parsing of ViralNames?
		if(problem){
			taxonName = NonViralName.NewInstance(null);
			taxonName.setTitleCache(scientificName);
		}
		return taxonName;

	}

	@SuppressWarnings("unchecked")
	private NonViralName<?> setTaxonNameByType(HashMap<String, String> atomisedMap,String fullName){
		if (this.nomenclatureCode.equals("Zoological")){
			NonViralName<ZoologicalName> taxonName  = ZoologicalName.NewInstance(null); 
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap,"Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,"SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,"SpeciesEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"SubspeciesEpithet"));
			Team team = null;
			if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
			}else{
				if (getFromMap(atomisedMap,"AuthorTeamAndYear") != null){
					team = Team.NewInstance();
					team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamAndYear"));
				}
			}
			if(team != null)
				taxonName.setBasionymAuthorTeam(team);
			else{
				if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null)
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
				else if (getFromMap(atomisedMap,"AuthorTeamAndYear") != null)
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamAndYear"));
			}
			if(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear"));
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem())
				logger.info("pb ICZN");
			else return taxonName;
		}
		if (this.nomenclatureCode.equals("Botanical")){
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
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
				if(team != null)
					taxonName.setBasionymAuthorTeam(team);
			}
			if (getFromMap(atomisedMap,"AuthorTeam") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeam"));
				if(team != null)
					taxonName.setCombinationAuthorTeam(team);
			}
			if (team == null)	{
				if(getFromMap(atomisedMap,"AuthorTeamParenthesis") != null)
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeamParenthesis"));
				else if (getFromMap(atomisedMap,"AuthorTeam") != null)
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,"AuthorTeam"));
			}
			if(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear") != null){
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"CombinationAuthorTeamAndYear"));
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem())
				logger.info("pb ICBN");
			else return taxonName;
		}
		if (this.nomenclatureCode.equals("Bacterial")){
			NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap,"Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,"SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,"Species"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"SubspeciesEpithet"));
			if(getFromMap(atomisedMap,"AuthorTeamAndYear") != null){
				Team team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"AuthorTeamAndYear"));
				taxonName.setCombinationAuthorTeam(team);
			}
			if(getFromMap(atomisedMap,"ParentheticalAuthorTeamAndYear") != null){
				Team team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap,"ParentheticalAuthorTeamAndYear"));
				taxonName.setBasionymAuthorTeam(team);
			}
			if (taxonName.hasProblem())
				logger.info("pb ICNB");
			else return taxonName;
		}
		if (this.nomenclatureCode.equals("Cultivar")){
			CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

			if (taxonName.hasProblem())
				logger.info("pb ICNCP");
			else return taxonName;
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
		if (atomisedMap.containsKey(key))
			value = atomisedMap.get(key);
		try{
			if (value != null && key.matches(".*Year.*")){
				value=value.trim();
				if (value.matches("[a-z A-Z ]*[0-9]{4}$")){
					String tmp=value.split("[0-9]{4}$")[0];
					int year = Integer.parseInt(value.split(tmp)[1]);
					if (year >= 1752)
						value=tmp;
					else
						value=null;
				}
				else
					value=null;
			}
		}catch(Exception e){value=null;}

		return value;
	}
	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	public boolean start(SpecimenImportConfigurator config){
		boolean result = true;

		TransactionStatus tx = null;

//		try {
//		app = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
//		} catch (DataSourceNotFoundException e1) {
//		e1.printStackTrace();
//		logger.info("DataSourceNotFoundException "+e1);
//		} catch (TermNotFoundException e1) {
//		e1.printStackTrace();
//		logger.info("TermNotFoundException " +e1);
//		}

		tx = startTransaction();
		try {
//			ReferenceBase sec = Database.NewInstance();
//			sec.setTitleCache("XML DATA");
			ReferenceBase sec = config.getTaxonReference();

			/**
			 * SPECIMEN OR OBSERVATION OR LIVING
			 */
			DerivedUnitBase derivedThing = null;
			//create specimen
			boolean rbFound=false;
			if (this.recordBasis != null){
				if (this.recordBasis.toLowerCase().startsWith("s")) {//specimen
					derivedThing = Specimen.NewInstance();
					rbFound = true;
				}
				else if (this.recordBasis.toLowerCase().startsWith("o")) {//observation
					derivedThing = Observation.NewInstance();	
					rbFound = true;
				}
				else if (this.recordBasis.toLowerCase().startsWith("l")) {//living -> fossil, herbarium sheet....???
					derivedThing = LivingBeing.NewInstance();
					rbFound = true;
				}
				if (! rbFound){
					logger.info("The basis of record does not seem to be known: "+this.recordBasis);
					derivedThing = DerivedUnit.NewInstance();
				}
			}
			else{
				logger.info("The basis of record is null");
				derivedThing = DerivedUnit.NewInstance();
			}
//			if(derivedThing == null)derivedThing=Observation.NewInstance();

			this.setTaxonNameBase(config, derivedThing, sec);


			//set catalogue number (unitID)
			derivedThing.setCatalogNumber(this.unitID);
			derivedThing.setAccessionNumber(this.accessionNumber);
			derivedThing.setCollectorsNumber(this.collectorsNumber);


			/**
			 * INSTITUTION & COLLECTION
			 */
			//manage institution
			Institution institution = this.getInstitution(this.institutionCode,config);
			//manage collection
			Collection collection = this.getCollection(this.collectionCode, institution, config); 
			//link specimen & collection
			derivedThing.setCollection(collection);

			/**
			 * GATHERING EVENT
			 */

			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(this, this.locality, this.languageIso, this.longitude, 
					this.latitude, this.gatheringAgentList);
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(this.isocountry, this.country, this);
			NamedArea areaCountry = unitsGatheringArea.getArea();
			unitsGatheringEvent.addArea(areaCountry);
			unitsGatheringArea = new UnitsGatheringArea(this.namedAreaList);
			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
			for (int i=0; i<nas.size();i++)
				unitsGatheringEvent.addArea(nas.get(i));


			//create field/observation
			FieldObservation fieldObservation = FieldObservation.NewInstance();
			//add fieldNumber
			fieldObservation.setFieldNumber(this.fieldNumber);
			//join gatheringEvent to fieldObservation
			fieldObservation.setGatheringEvent(unitsGatheringEvent.getGatheringEvent());
//			//add Multimedia URLs
			if(this.multimediaObjects.size()>0){
				MediaRepresentation representation;
				Media media;
				ImageMetaData imd ;
				URL url ;
				ImageFile imf;
				for (int i=0;i<this.multimediaObjects.size();i++){
					if(this.multimediaObjects.get(i) != null){
						imd = ImageMetaData.newInstance();
						url = new URL(this.multimediaObjects.get(i));
						imd.readMetaData(url.toURI(), 0);
						if (imd != null){
							representation = MediaRepresentation.NewInstance();
							imf = ImageFile.NewInstance(this.multimediaObjects.get(i), null, imd);
							representation.addRepresentationPart(imf);
							media = Media.NewInstance();
							media.addRepresentation(representation);
							fieldObservation.addMedia(media);
						}
					}
				}
			}
//			//link fieldObservation and specimen
			DerivationEvent derivationEvent = DerivationEvent.NewInstance();
			derivationEvent.addOriginal(fieldObservation);
			derivedThing.addDerivationEvent(derivationEvent);

			/**
			 * SAVE AND STORE DATA
			 */			
			getTermService().save(areaCountry);//save it sooner
			for (int i=0; i<nas.size();i++)
				getTermService().save(nas.get(i));//save it sooner (foreach area)
			getTermService().saveLanguageData(unitsGatheringEvent.getLocality());//save it sooner
			getOccurrenceService().save(derivedThing);
			logger.info("saved ABCD specimen ...");


		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		commitTransaction(tx);

		return result;
	}

	private void compareABCDtoCDM(String urlFileName, ArrayList<String> knownElts){

		try {
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
			URL url = new URL(urlFileName);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Document document = constructeur.parse(is);
			Element racine = document.getDocumentElement();
			traverse(racine);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String> elts = allABCDelements.keySet();
		Iterator< String>it = elts.iterator();
		String elt;
		while (it.hasNext()){
			elt = it.next();
			if (knownElts.indexOf(elt) == -1)
				logger.info("Unsaved ABCD element: "+elt+" - "+allABCDelements.get(elt));
		}
	}
	
	

	private void traverse(Node node){
		// Extract node info:
		String test = node.getTextContent();

		// Print and continue traversing.
		if(test != null && test != "#text" && node.getNodeName() != "#text" && test.split("\n").length==1 && test.length()>0)
		{
			path=node.getNodeName();
			getHierarchie(node);
			allABCDelements.put(path,test);
			path="";
		}
		// Now traverse the rest of the tree in depth-first order.
		if (node.hasChildNodes()) {
			// Get the children in a list.
			NodeList nl = node.getChildNodes();
			// How many of them?
			int size = nl.getLength();
			for (int i=0; i<size; i++)
				// Recursively traverse each of the children.
				traverse (nl.item(i));
		}
	}

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	@Override
//	protected boolean doInvoke(IImportConfigurator config, 
//			Map<String, MapWrapper<? extends CdmBase>> stores){ 
//		SpecimenImportState state = ((SpecimenImportConfigurator)config).getState();
//		state.setConfig((SpecimenImportConfigurator)config);
//		return doInvoke(state);
//	}
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.abcd206.SpecimenIoBase#doInvoke(eu.etaxonomy.cdm.io.abcd206.SpecimenImportState)
	 */
	@Override
	public boolean doInvoke(SpecimenImportState state){
		logger.info("INVOKE Specimen Import from ABCD2.06 XML File");
		boolean result = true;
		SpecimenImportConfigurator config = state.getConfig();
		//AbcdIO test = new AbcdIO();
		String sourceName = config.getSource();
		NodeList unitsList = getUnitsNodeList(sourceName);
		if (unitsList != null)
		{
			logger.info("nb units to insert: "+unitsList.getLength());
			for (int i=0;i<unitsList.getLength();i++){
				this.setUnitPropertiesXML((Element)unitsList.item(i));
				result &= this.start(config);
				config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
				//compare the ABCD elements added in to the CDM and the unhandled ABCD elements
				compareABCDtoCDM(sourceName,this.knownABCDelements);
				//reset the ABCD elements added in CDM
				//knownABCDelements = new ArrayList<String>();
				allABCDelements = new HashMap<String,String>();
			}
		}

		return result;

	}


//	public boolean invoke(IImportConfigurator config, Map stores) {
//		logger.info("invoke AbcdIO");
//		return invoke((SpecimenImportConfigurator)config);
//	}


	@Override
	protected boolean isIgnore(SpecimenImportState state) {
		//return ! config.isDoNameFacts();
		return false;
	}



}
