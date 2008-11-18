package eu.etaxonomy.cdm.io.abcd206;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
			if(group.item(j).getNodeName().equals("Identification")){
				this.nomenclatureCode ="";
				identifications = group.item(j).getChildNodes();
				for (int m=0; m<identifications.getLength();m++){
					if(identifications.item(m).getNodeName().equals("Result")){
						results = identifications.item(m).getChildNodes();
						for(int k=0; k<results.getLength();k++)
							if (results.item(k).getNodeName().equals("TaxonIdentified"))
								tmpName=this.getScientificName(results.item(k));
					}
					else if(identifications.item(m).getNodeName().equals("PreferredFlag"))
						this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent()+"_code_"+this.nomenclatureCode);

					else if (identifications.item(m).getNodeName().equals("References"))
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
			if (results.item(k).getNodeName().equals("Reference")){
				reference = results.item(k).getChildNodes();
				for(int l=0;l<reference.getLength();l++){
					if (reference.item(l).getNodeName().equals("TitleCitation"))
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
			if (taxonsIdentified.item(l).getNodeName().equals("ScientificName")){
				scnames = taxonsIdentified.item(l).getChildNodes();
				for (int n=0;n<scnames.getLength();n++){
					if (scnames.item(n).getNodeName().equals("FullScientificNameString"))
						tmpName = scnames.item(n).getTextContent();
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
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial"))
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("Subgenus"))
				atomisedMap.put("Subgenus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet"))
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet"))
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("AuthorTeamOriginalAndYear"))
				atomisedMap.put("AuthorTeamOriginalAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesisAndYear"))
				atomisedMap.put("AuthorTeamParenthesisAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("CombinationAuthorTeamAndYear"))
				atomisedMap.put("CombinationAuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("Breed"))
				atomisedMap.put("Breed",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("NamedIndividual"))
				atomisedMap.put("NamedIndividual",atomised.item(i).getTextContent());
		}
		return atomisedMap;

	}

	private HashMap<String,String> getAtomisedViral(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial"))
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("ViralSpeciesDesignation"))
				atomisedMap.put("ViralSpeciesDesignation", atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("Acronym"))
				atomisedMap.put("Acronym",atomised.item(i).getTextContent());
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBotanical(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial"))
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("FirstEpithet"))
				atomisedMap.put("FirstEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("InfraspecificEpithet"))
				atomisedMap.put("InfraSpeEpithet", atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("Rank"))
				atomisedMap.put("Rank",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("HybridFlag"))
				atomisedMap.put("HybridFlag",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("AuthorTeamParenthesis"))
				atomisedMap.put("AuthorTeamParenthesis",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("AuthorTeam"))
				atomisedMap.put("AuthorTeam",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("CultivarGroupName"))
				atomisedMap.put("CultivarGroupName",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("CultivarName"))
				atomisedMap.put("CultivarName",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("TradeDesignationNames"))
				atomisedMap.put("Trade",atomised.item(i).getTextContent());
		}
		return atomisedMap;
	}

	private HashMap<String,String> getAtomisedBacterial(NodeList atomised){
		HashMap<String,String> atomisedMap = new HashMap<String,String>();
		for (int i=0;i<atomised.getLength();i++){
			if(atomised.item(i).getNodeName().equals("GenusOrMonomial"))
				atomisedMap.put("Genus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("Subgenus"))
				atomisedMap.put("SubGenus",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("SubgenusAuthorAndYear"))
				atomisedMap.put("SubgenusAuthorAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("SpeciesEpithet"))
				atomisedMap.put("SpeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("SubspeciesEpithet"))
				atomisedMap.put("SubspeciesEpithet",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("ParentheticalAuthorTeamAndYear"))
				atomisedMap.put("ParentheticalAuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("AuthorTeamAndYear"))
				atomisedMap.put("AuthorTeamAndYear",atomised.item(i).getTextContent());
			if(atomised.item(i).getNodeName().equals("NameApprobation"))
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
					if (multimedias.item(j).getNodeName().equals("MultiMediaObject")){	
						multimedia = multimedias.item(j).getChildNodes();
						for (int k=0;k<multimedia.getLength();k++){
							if(multimedia.item(k).getNodeName().equals("FileURI"))
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
					if (childs.item(j).getNodeName().equals("MeasurementOrFactText"))
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
					if (childs.item(j).getNodeName().equals("AreaName"))
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
					if (childs.item(j).getNodeName().equals("Person")){
						person = childs.item(j).getChildNodes();
						for (int k=0; k<person.getLength(); k++)
							if (person.item(k).getNodeName().equals("FullName"))
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
		NonViralName<?> taxonName = null;
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
				if (pTmp.equals("1") || pTmp.toLowerCase().indexOf("true") != -1)
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
				taxonName = this.setTaxonNameByType(this.atomisedIdentificationList.get(i), scientificName);
			}
			if(taxonName == null){
				taxonName = NonViralName.NewInstance(null);
				taxonName.setFullTitleCache(scientificName);
			}
			if (config.getDoReUseTaxon()){
				try{
					names = config.getCdmAppController().getTaxonService().searchTaxaByName(scientificName, sec);
					taxon = (Taxon)names.get(0);
				}
				catch(Exception e){taxon=null;}
			}
//			taxonName = NonViralName.NewInstance(null);
//			taxonName.setFullTitleCache(scientificName);

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

	private NonViralName<?> parseScientificName(String scientificName){
		System.out.println("parseScientificName");
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?>taxonName = null;
		boolean problem=false;

		System.out.println("nomenclature: "+this.nomenclatureCode);
		if (this.nomenclatureCode.toString().equals("Zoological")){
			taxonName = (ZoologicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN(),null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Botanical")){
			taxonName  = (BotanicalName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICBN(),null);
			if (taxonName.hasProblem())
				problem=true;;}
		if (this.nomenclatureCode.toString().equals("Bacterial")){
			taxonName = (BacterialName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB(), null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Cultivar")){
			taxonName = (CultivarPlantName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP(), null);
			if (taxonName.hasProblem())
				problem=true;;
		}
//		if (this.nomenclatureCode.toString().equals("Viral")){
//		ViralName taxonName = (ViralName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICVCN(), null);
//		if (taxonName.hasProblem())
//		System.out.println("pb ICVCN");
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
		System.out.println("nomenclature: "+this.nomenclatureCode);
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
				System.out.println("pb ICZN");
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
				System.out.println("pb ICBN");
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
				System.out.println("pb ICNB");
			else return taxonName;
		}
		if (this.nomenclatureCode.equals("Cultivar")){
			NonViralName<CultivarPlantName> taxonName = CultivarPlantName.NewInstance(null);

			if (taxonName.hasProblem())
				System.out.println("pb ICNCP");
			else return taxonName;
		}
//		if (this.nomenclatureCode.equals("Viral")){
//		ViralName taxonName = ViralName.NewInstance(null);
//		taxonName.setFullTitleCache(fullName, true);
//		taxonName.setAcronym(getFromMap(atomisedMap,"Acronym"));
//		if (taxonName.hasProblem())
//		System.out.println("pb ICVCN");
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
		if (value != null && key.matches(".*Year.*")){
			value=value.trim();
			if (value.matches("[a-z A-Z ]*[0-9]{4}$")){
				String tmp=value.split("[0-9]{4}$")[0];
				int year = Integer.parseInt(value.split(tmp)[1]);
				if (year >= 1752)
					value=tmp;
			}
		}
		return value;
	}
	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	public boolean start(SpecimenImportConfigurator config){
		boolean result = true;
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
				MediaRepresentation representation;
				Media media;
				MediaMetaData mmd ;
				ImageMetaData imd ;
				URL url ;
				ImageFile imf;
				for (int i=0;i<this.multimediaObjects.size();i++){
					if(this.multimediaObjects.get(i) != null){
						mmd = new MediaMetaData();
						imd = new ImageMetaData();
						url = new URL(this.multimediaObjects.get(i));
						imd = mmd.readImageMetaData(url, imd);
						if (imd != null){
							System.out.println("image not null");
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
