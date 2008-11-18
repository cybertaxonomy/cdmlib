package eu.etaxonomy.cdm.io.abcd206;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.MediaMetaData;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ViralName;
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
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

public class AbcdIO  extends SpecimenIoBase  implements ICdmIO {


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


	public AbcdIO() {
		super();
		knownABCDelements.add("Identifications");
		knownABCDelements.add("Identification");
		knownABCDelements.add("Result");
		knownABCDelements.add("TaxonIdentified");
		knownABCDelements.add("ScientificName");
		knownABCDelements.add("FullScientificNameString");
		knownABCDelements.add("NameAtomised");
		knownABCDelements.add("SourceInstitutionID");
		knownABCDelements.add("SourceID");
		knownABCDelements.add("UnitID");
		knownABCDelements.add("RecordBasis");
		knownABCDelements.add("AccessionNumber");
		knownABCDelements.add("LocalityText");
		knownABCDelements.add("LongitudeDecimal");
		knownABCDelements.add("Country");
		knownABCDelements.add("ISO3166Code");
		knownABCDelements.add("CollectorsFieldNumber");
		knownABCDelements.add("CollectorsNumber");
		knownABCDelements.add("AccessionNumber");
		knownABCDelements.add("Altitude_MeasurementOrFactText");
		knownABCDelements.add("Depth");
		knownABCDelements.add("NamedArea_AreaName");
		knownABCDelements.add("GatheringAgent_Person_FullName");
	}

	/*
	 * Return the list of root nodes for an ABCD 2.06 XML file
	 * @param fileName: the file's location
	 * @return the list of root nodes ("Unit")
	 */
	private static NodeList getUnitsNodeList(String fileName){
		NodeList unitList = null;
		try {
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
			File xml = new File(fileName);
			Document document = constructeur.parse(xml);
			Element racine = document.getDocumentElement();
			unitList = racine.getElementsByTagName("Unit");
		}catch(Exception e){
			System.out.println(e);
		}
		return unitList;
	}


	public void afficherInfos(Node noeud, int niv) {
		short type = noeud.getNodeType();
		String nom = noeud.getNodeName();
		String valeur = noeud.getNodeValue();

		indenter(niv, type == Node.TEXT_NODE);
		if(!knownABCDelements.contains(nom)){
			System.out.print(nom + " (" + type + ") = '");
			if(valeur != null && !valeur.matches("^\\s+$")){
				System.out.print(valeur);
				System.out.println("'");
			}
		}
		if ((type == Node.DOCUMENT_NODE 
				|| type == Node.ELEMENT_NODE)
				&& noeud.hasChildNodes()) {
			NodeList liste = noeud.getChildNodes();
			for(int i = 0; i < liste.getLength(); i++)
				afficherInfos(liste.item(i), niv + 1);
		}
	}
	public void indenter(int n, boolean texte){
		String tab = "\t";
		for(int i = 0; i < n; i++){
			System.out.print(tab);
		}
		if(texte){
			System.out.print(" - ");
		}
		else
			System.out.print(" + ");
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

//			try{afficherInfos(racine, 0);}catch (Exception e) {System.out.println(e);}
			group = racine.getChildNodes();
//			logger.info("ABCD ELEMENT not stored: "+group.item(i).getNodeName().toString()+" - value: "+group.item(i).getTextContent());
			for (int i=0; i< group.getLength(); i++){
				if (group.item(i).getNodeName() == "Identifications"){
					group = group.item(i).getChildNodes();
					break;
				}
			}
			this.identificationList = new ArrayList<String>();
			this.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
			this.referenceList = new ArrayList<String>();
			this.multimediaObjects = new ArrayList<String>();

			this.getScientificNames(group);

//			System.out.println("this.identificationList "+this.identificationList.toString());
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


	private void getScientificNames(NodeList group){
		NodeList identifications,results;
		String tmpName = null;
		for (int j=0; j< group.getLength(); j++){
			if(group.item(j).getNodeName() == "Identification"){
				this.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName() == "Result"){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++)
							if (results.item(k).getNodeName() == "TaxonIdentified")
								tmpName=this.getScientificName(results.item(k));
					}
					else if(identifications.item(m).getNodeName() == "PreferredFlag")
						this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent()+"_code_"+this.nomenclatureCode);

					else if (identifications.item(m).getNodeName() == "References")
						this.getReferences(identifications.item(m));
					else
						if (tmpName != null)
							this.identificationList.add(tmpName+"_preferred_"+"0"+"_code_"+this.nomenclatureCode);
				}
			}
		}
	}

	private void getReferences(Node result){
		NodeList results,reference;
		results = result.getChildNodes();
		for(int k=0; k<results.getLength();k++){
			if (results.item(k).getNodeName() == "Reference"){
				reference = results.item(k).getChildNodes();
				for(int l=0;l<reference.getLength();l++){
					if (reference.item(l).getNodeName()=="TitleCitation")
						referenceList.add(reference.item(l).getTextContent());
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
			if (taxonsIdentified.item(l).getNodeName() == "ScientificName"){
				scnames = taxonsIdentified.item(l).getChildNodes();
				for (int n=0;n<scnames.getLength();n++){
					if (scnames.item(n).getNodeName() == "FullScientificNameString")
						tmpName = scnames.item(n).getTextContent();
					if (scnames.item(n).getNodeName() == "NameAtomised"){
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
		if (code == "Botanical")
			return this.getAtomisedBotanical(atomised);
		if (code == "Bacterial")
			return this.getAtomisedBacterial(atomised);
		if (code == "NameViral")
			return this.getAtomisedViral(atomised);
		if (code == "NameZoological")
			return this.getAtomisedZoological(atomised);
		return new HashMap<String,String>();
	}

	private HashMap<String,String> getAtomisedZoological(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName()=="GenusOrMonomial")
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="Subgenus")
				atomisedMap.put("Subgenus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="SpeciesEpithet")
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="SubspeciesEpithet")
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="AuthorTeamOriginalAndYear")
				atomisedMap.put("AuthorTeamOriginalAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="AuthorTeamParenthesisAndYear")
				atomisedMap.put("AuthorTeamParenthesisAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="CombinationAuthorTeamAndYear")
				atomisedMap.put("CombinationAuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="Breed")
				atomisedMap.put("Breed",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="NamedIndividual")
				atomisedMap.put("NamedIndividual",atomised.item(i).getTextContent());
		}
		return atomisedMap;

	}

	private HashMap<String,String> getAtomisedViral(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName()=="GenusOrMonomial")
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="ViralSpeciesDesignation")
				atomisedMap.put("ViralSpeciesDesignation", atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="Acronym")
				atomisedMap.put("Acronym",atomised.item(i).getTextContent());
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBotanical(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName()=="GenusOrMonomial")
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="FirstEpithet")
				atomisedMap.put("FirstEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="InfraspecificEpithet")
				atomisedMap.put("InfraSpeEpithet", atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="Rank")
				atomisedMap.put("Rank",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="HybridFlag")
				atomisedMap.put("HybridFlag",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="AuthorTeamParenthesis")
				atomisedMap.put("AuthorTeamParenthesis",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="AuthorTeam")
				atomisedMap.put("AuthorTeam",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="CultivarGroupName")
				atomisedMap.put("CultivarGroupName",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="CultivarName")
				atomisedMap.put("CultivarName",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="TradeDesignationNames")
				atomisedMap.put("Trade",atomised.item(i).getTextContent());
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBacterial(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName()=="GenusOrMonomial")
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="Subgenus")
				atomisedMap.put("SubGenus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="SubgenusAuthorAndYear")
				atomisedMap.put("SubgenusAuthorAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="SpeciesEpithet")
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="SubspeciesEpithet")
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="ParentheticalAuthorTeamAndYear")
				atomisedMap.put("ParentheticalAuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="AuthorTeamAndYear")
				atomisedMap.put("AuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName()=="NameApprobation")
				atomisedMap.put("NameApprobation",atomised.item(i).getTextContent());
		}
		return atomisedMap;
	}

	private void getIDs(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("SourceInstitutionID");
			this.institutionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.institutionCode= "";
		}
		try {
			group = racine.getElementsByTagName("SourceID");
			this.collectionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.collectionCode = "";
		}
		try {
			group = racine.getElementsByTagName("UnitID");
			this.unitID = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.unitID = "";
		}
	}

	private void getRecordBasis(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("RecordBasis");
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
					if (multimedias.item(j).getNodeName() == "MultiMediaObject"){	
						multimedia = multimedias.item(j).getChildNodes();
						for (int k=0;k<multimedia.getLength();k++){
							if(multimedia.item(k).getNodeName() == "FileURI")
								this.multimediaObjects.add(multimedia.item(k).getTextContent());
						}
					}
				}
			}
		} catch (NullPointerException e) {
			System.out.println(e);
		}
	}

	private void getNumbers(Element racine){
		NodeList group;
		try {
			group = racine.getElementsByTagName("AccessionNumber");
			this.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.accessionNumber = "";
		}
		try {
			group = racine.getElementsByTagName("CollectorsFieldNumber");
			this.fieldNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.fieldNumber = "";
		}

		try {
			group = racine.getElementsByTagName("CollectorsNumber");
			this.collectorsNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.collectorsNumber = "";
		}

		try {
			group = racine.getElementsByTagName("AccessionNumber");
			this.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.accessionNumber = "";
		}
	}

	private void getGeolocation(Element racine){
		NodeList group, childs;
		try {
			group = racine.getElementsByTagName("LocalityText");
			this.locality = group.item(0).getTextContent();
			if (group.item(0).hasAttributes())
				if (group.item(0).getAttributes().getNamedItem("lang") != null)
					this.languageIso = group.item(0).getAttributes().getNamedItem("lang").getTextContent();
		} catch (NullPointerException e) {
			this.locality = "";
		}
		try {
			group = racine.getElementsByTagName("LongitudeDecimal");
			this.longitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			this.longitude=0.0;
		}
		try {
			group = racine.getElementsByTagName("LatitudeDecimal");
			this.latitude = Double.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			this.latitude=0.0;
		}
		try {
			group = racine.getElementsByTagName("Country");
			this.country = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.country = "";
		}
		try {
			group = racine.getElementsByTagName("ISO3166Code");
			this.isocountry = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			this.isocountry = "";
		}
		try {
			group = racine.getElementsByTagName("Altitude");
			for (int i=0;i<group.getLength();i++){
				childs = group.item(i).getChildNodes();
				for (int j=0;j<childs.getLength();j++){
					if (childs.item(j).getNodeName() == "MeasurementOrFactText")
						this.altitude = Integer.valueOf(childs.item(j).getTextContent());
				}
			}
		} catch (NullPointerException e) {
			this.altitude = -9999;
		}

		try {
			group = racine.getElementsByTagName("Depth");
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
					if (childs.item(j).getNodeName() == "AreaName")
						this.namedAreaList.add(childs.item(j).getTextContent());
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
					if (childs.item(j).getNodeName() == "Person"){
						person = childs.item(j).getChildNodes();
						for (int k=0; k<person.getLength(); k++)
							if (person.item(k).getNodeName() == "FullName")
								this.gatheringAgentList.add(person.item(k).getTextContent());
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
			System.out.println(this.institutionCode);
			institutions= config.getCdmAppController().getAgentService().searchInstitutionByCode(this.institutionCode);
		}catch(Exception e){
			System.out.println("BLI "+e);
			institutions=new ArrayList<Institution>();
		}
		if (institutions.size() ==0 || !config.getReUseExistingMetadata()){
			System.out.println("Institution (agent) unknown or not allowed to reuse existing metadata");
			//create institution
			institution = Institution.NewInstance();
			institution.setCode(this.institutionCode);				
		}
		else{
			System.out.println("Institution (agent) already in the db");
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
			collections = config.getCdmAppController().getOccurrenceService().searchCollectionByCode(this.collectionCode);
		}catch(Exception e){
			collections=new ArrayList<Collection>();
		}
		if (collections.size() ==0 || !config.getReUseExistingMetadata()){
			System.out.println("Collection not found or do not reuse existing metadata  "+this.collectionCode);
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
		TaxonNameBase<?,?> taxonName = null;
		String fullScientificNameString;
		Taxon taxon = null;
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
				if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1)
					preferredFlag=true;
				else
					preferredFlag=false;
			}
			else scientificName = fullScientificNameString;

			if (fullScientificNameString.indexOf("_code_") != -1)	
				this.nomenclatureCode = fullScientificNameString.split("_code_")[1];

			if (config.getDoAutomaticParsing())	
				taxonName = this.parseScientificName(scientificName);	
			else {
				taxonName = this.setTaxonNameType(this.atomisedIdentificationList.get(i));
				taxonName.setTitleCache(scientificName);
			}

			if (config.getDoReUseTaxon()){
				try{
					names = config.getCdmAppController().getTaxonService().searchTaxaByName(scientificName, sec);
					taxon = (Taxon)names.get(0);}
				catch(Exception e){taxon=null;}
			}
			if (!config.getDoReUseTaxon() || taxon == null){
				config.getCdmAppController().getNameService().saveTaxonName(taxonName);
				taxon = Taxon.NewInstance(taxonName, sec); //sec set null
			}

			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
			for (int l=0;l<this.referenceList.size();l++){
				ReferenceBase reference = new Generic();
				reference.setTitleCache(this.referenceList.get(l));
				determinationEvent.addReference(reference);
			}
			derivedThing.addDetermination(determinationEvent);
		}

	}

	private TaxonNameBase<?,?> parseScientificName(String scientificName){
		System.out.println("scientificName");
		TaxonNameBase<?,?> taxonName = null;
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();

		System.out.println("nomenclature: "+this.nomenclatureCode);
		if (this.nomenclatureCode == "Zoological"){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN(),null);
			if (taxonName.hasProblem())
				System.out.println("pb ICZN");}
		if (this.nomenclatureCode == "Botanical"){
			taxonName  = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICBN(),null);
			if (taxonName.hasProblem())
				System.out.println("pb ICBN");}
		if (this.nomenclatureCode == "Bacterial"){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB(), null);
			if (taxonName.hasProblem())
				System.out.println("pb ICNB");
		}
		if (this.nomenclatureCode == "Cultivar"){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP(), null);
			if (taxonName.hasProblem())
				System.out.println("pb ICNCP");
		}
		if (this.nomenclatureCode == "Viral"){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICVCN(), null);
			if (taxonName.hasProblem())
				System.out.println("pb ICVCN");
		}
		try{taxonName.hasProblem();}
		catch (Exception e) {
			taxonName = nvnpi.parseFullName(scientificName);
		}
		if (taxonName.hasProblem())
			taxonName = nvnpi.parseFullName(scientificName);
		return taxonName;
	}

	@SuppressWarnings("unchecked")
	private TaxonNameBase<?,?> setTaxonNameType(HashMap<String, String> atomisedMap){
		System.out.println("nomenclature: "+this.nomenclatureCode);
		if (this.nomenclatureCode == "Zoological"){
			NonViralName<ZoologicalName> taxonName  = ZoologicalName.NewInstance(null); 
			taxonName.setGenusOrUninomial(atomisedMap.get("Genus"));
			taxonName.setInfraGenericEpithet(atomisedMap.get("SubGenus"));
			taxonName.setSpecificEpithet(atomisedMap.get("SpeciesEpithet"));
			taxonName.setInfraSpecificEpithet(atomisedMap.get("SubspeciesEpithet"));
			taxonName.setAuthorshipCache(""); //AuthorTeamAndYear? AuthorTeamParenthesis? CombinationAuthorAndYear,?
//			taxonName.setCombinationAuthorTeam(INomenclaturalAuthor);//AuthorTeamAndYear? AuthorTeamParenthesis? CombinationAuthorAndYear,?
			if (taxonName.hasProblem())
				System.out.println("pb ICZN");
			else return taxonName;
		}
		if (this.nomenclatureCode == "Botanical"){
			NonViralName<BotanicalName> taxonName  = BotanicalName.NewInstance(null);
			taxonName.setGenusOrUninomial(atomisedMap.get("Genus"));
			taxonName.setInfraGenericEpithet(atomisedMap.get("FirstEpithet"));
			taxonName.setInfraSpecificEpithet(atomisedMap.get("InfraSpeEpithet"));
			try{taxonName.setRank(Rank.getRankByName(atomisedMap.get("Rank")));
			}catch(Exception e){}
			taxonName.setAuthorshipCache(atomisedMap.get(""));//AuthorTeam? AuthorTeamParenthesis?
			
			if (taxonName.hasProblem())
				System.out.println("pb ICBN");
			else return taxonName;
		}
		if (this.nomenclatureCode == "Bacterial"){
			NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
			taxonName.setGenusOrUninomial(atomisedMap.get("Genus"));
			taxonName.setInfraGenericEpithet(atomisedMap.get("SubGenus"));
//			taxonName.setSpecificEpithet(specificEpithet);//Species??
//			taxonName.setInfraSpecificEpithet(infraSpecificEpithet)//subspeciesepithet?
//			taxonName.setAuthorshipCache(authorshipCache);//parenthetical...
//			taxonName.setCombinationAuthorTeam(combinationAuthorTeam)//authorteamandyear?
			if (taxonName.hasProblem())
				System.out.println("pb ICNB");
			else return taxonName;
		}
		if (this.nomenclatureCode == "Cultivar"){
			NonViralName<CultivarPlantName> taxonName = CultivarPlantName.NewInstance(null);
			
			if (taxonName.hasProblem())
				System.out.println("pb ICNCP");
			else return taxonName;
		}
		if (this.nomenclatureCode == "Viral"){
			ViralName taxonName = ViralName.NewInstance(null);
//			taxonName.//setGenus?;
//			taxonName.//setViralSpeciesDesignation
			taxonName.setAcronym(atomisedMap.get("Acronym"));
			if (taxonName.hasProblem())
				System.out.println("pb ICVCN");
			else return taxonName;
		}
		NonViralName<?>taxonName = NonViralName.NewInstance(null);
		return taxonName;
	}
	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	public boolean start(SpecimenImportConfigurator config){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;
		TransactionStatus tx = null;

		app = config.getCdmAppController();
//		try {
//		app = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
//		} catch (DataSourceNotFoundException e1) {
//		e1.printStackTrace();
//		System.out.println("DataSourceNotFoundException "+e1);
//		} catch (TermNotFoundException e1) {
//		e1.printStackTrace();
//		System.out.println("TermNotFoundException " +e1);
//		}

		tx = app.startTransaction();
		try {
//			ReferenceBase sec = Database.NewInstance();
//			sec.setTitleCache("XML DATA");
			ReferenceBase sec = config.getSourceReference();

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

			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(config, this.locality, this.languageIso, this.longitude, 
					this.latitude, this.gatheringAgentList);
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(this.isocountry, this.country, config);
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
				MediaRepresentationPart part;
				MediaRepresentation representation;
				Media media;
				MediaMetaData mmd ;
				ImageMetaData imd ;
				URL url ;
				for (int i=0;i<this.multimediaObjects.size();i++){
					mmd = new MediaMetaData();
					imd = new ImageMetaData();
					url = new URL(this.multimediaObjects.get(i));
					imd = mmd.readImageMetaData(url, imd);
					//TODO update the Multimedia Object without size :)
					representation = MediaRepresentation.NewInstance();
					representation.addRepresentationPart(ImageFile.NewInstance(this.multimediaObjects.get(i), null, imd));
					media = Media.NewInstance();
					media.addRepresentation(representation);
					fieldObservation.addMedia(media);
				}
			}
//			//link fieldObservation and specimen
			DerivationEvent derivationEvent = DerivationEvent.NewInstance();
			derivationEvent.addOriginal(fieldObservation);
			derivedThing.addDerivationEvent(derivationEvent);

			/**
			 * SAVE AND STORE DATA
			 */			

			app.getTermService().saveTerm(areaCountry);//save it sooner
			for (int i=0; i<nas.size();i++)
				app.getTermService().saveTerm(nas.get(i));//save it sooner (foreach area)
			app.getTermService().saveLanguageData(unitsGatheringEvent.getLocality());//save it sooner
			app.getOccurrenceService().saveSpecimenOrObservationBase(derivedThing);

			logger.info("saved new specimen ...");


		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		app.commitTransaction(tx);
		System.out.println("commit done");
		app.close();
		return result;
	}


	public boolean invoke(SpecimenImportConfigurator config){
		System.out.println("INVOKE Specimen Import from ABCD2.06 XML File");
		AbcdIO test = new AbcdIO();
		String sourceName = config.getSourceNameString();
		NodeList unitsList = getUnitsNodeList(sourceName);
		if (unitsList != null)
		{
			System.out.println("nb units to insert: "+unitsList.getLength());
			for (int i=0;i<unitsList.getLength();i++){
				test.setUnitPropertiesXML((Element)unitsList.item(i));
				test.start(config);
				config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
			}
		}

		return false;

	}


	public boolean invoke(IImportConfigurator config, Map stores) {
		System.out.println("invoke de ABCDio");
		invoke((SpecimenImportConfigurator)config);
		return false;
	}




}
