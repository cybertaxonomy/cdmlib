/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.abcdImport;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

import javax.xml.parsers.*; 
import org.w3c.dom.*; 
import java.io.*; 


/**
 * @author PK
 * @created 04.08.2008
 * @version 1.0
 */
public class ABCD206Activator {
	private static final Logger logger = Logger.getLogger(ABCD206Activator.class);

	protected String fullScientificNameString;
	protected String institutionCode;
	protected String collectionCode;
	protected String unitID;
	protected String recordBasis;
	protected String accessionNumber;
	protected String fieldNumber;
	protected Double longitude;
	protected Double latitude;
	protected String locality;
	protected String country;
	protected String isocountry;
	protected ArrayList<String> gatheringAgentList;
	protected ArrayList<String> identificationList;

	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;



	private void parseXML(){
		try {
			// création d'une fabrique de documents
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

			// création d'un constructeur de documents
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();

			// lecture du contenu d'un fichier XML avec DOM
			File xml = new File("/home/patricia/Desktop/abcd206_3.xml");
			Document document = constructeur.parse(xml);

			Element racine = document.getDocumentElement();

			NodeList group,childs,identifications,results,taxonsIdentified,person,scnames;

			try {
				group = racine.getElementsByTagName("Identifications");
				this.identificationList = new ArrayList<String>();
				for (int i=0; i< group.getLength(); i++){
					childs = group.item(i).getChildNodes();
					for (int j=0; j<childs.getLength();j++){
						if(childs.item(j).getNodeName() == "Identification"){
							identifications = childs.item(j).getChildNodes();
							for (int m=0; m<identifications.getLength();m++){
								if(identifications.item(m).getNodeName() == "Result"){
									results = identifications.item(m).getChildNodes();
									for(int k=0; k<results.getLength();k++){
										if (results.item(k).getNodeName() == "TaxonIdentified"){
											taxonsIdentified = results.item(k).getChildNodes();
											for (int l=0; l<taxonsIdentified.getLength(); l++){
												if (taxonsIdentified.item(l).getNodeName() == "ScientificName"){
													scnames = taxonsIdentified.item(l).getChildNodes();
													for (int n=0;n<scnames.getLength();n++){
														if (scnames.item(n).getNodeName() == "FullScientificNameString")
															this.identificationList.add(scnames.item(n).getTextContent());
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (NullPointerException e) {
				System.out.println(e);
			}
			System.out.println("this.identificationList "+this.identificationList.toString());
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
			try {
				group = racine.getElementsByTagName("RecordBasis");
				this.recordBasis = group.item(0).getTextContent();
			} catch (NullPointerException e) {
				this.recordBasis = "";
			}
			try {
				group = racine.getElementsByTagName("AccessionNumber");
				this.accessionNumber = group.item(0).getTextContent();
			} catch (NullPointerException e) {
				this.accessionNumber = "";
			}
			try {
				group = racine.getElementsByTagName("LocalityText");
				this.locality = group.item(0).getTextContent();
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
				group = racine.getElementsByTagName("CollectorsFieldNumber");
				this.fieldNumber = group.item(0).getTextContent();
			} catch (NullPointerException e) {
				this.fieldNumber = "";
			}

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
									gatheringAgentList.add(person.item(k).getTextContent());
						}

					}
				}
			} catch (NullPointerException e) {
				this.gatheringAgentList = new ArrayList<String>();
			}

			System.out.println("racine: "+racine.getNodeName());
			System.out.println(this.collectionCode);
			System.out.println(this.institutionCode);
			System.out.println(this.fieldNumber);


		} catch (Exception e) {
			logger.info("Error occured while parsing XML file"+e);
		}




	}

	public boolean invoke(){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;


		try {
			app = CdmApplicationController.NewInstance(CdmDestinations.cdm_test_patricia(), hbm2dll);
		} catch (DataSourceNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("DataSourceNotFoundException "+e1);
		} catch (TermNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("TermNotFoundException " +e1);
		}


		try {
			ReferenceBase sec = Database.NewInstance();
			sec.setTitleCache("XML DATA");

			//create specimen
			Specimen specimen = Specimen.NewInstance();

			/* 
			for (int i=0; i< this.identificationList.size(); i++){
				this.fullScientificNameString = this.identificationList.get(i);

				TaxonNameBase taxonName = (BotanicalName)NonViralNameParserImpl.NewInstance().parseFullName(this.fullScientificNameString);
				if (withCdm){
					List<TaxonNameBase> names = app.getNameService().getNamesByName(this.fullScientificNameString);
					if (names.size() == 0){
						System.out.println("Name not found: " + this.fullScientificNameString);
					}else{
						if (names.size() > 1){
							System.out.println("More then 1 name found: " + this.fullScientificNameString);
						}
						taxonName = names.get(0);
					}
				}
				logger.info("Create new specimen ...");


				//set catalogue number (unitID)
				specimen.setCatalogNumber(this.unitID);
				specimen.setStoredUnder(taxonName);
			}
			 */
			System.out.println(this.identificationList.toString());
			TaxonNameBase taxonName = NonViralNameParserImpl.NewInstance().parseFullName(this.fullScientificNameString);
			this.fullScientificNameString = this.identificationList.get(0);
			System.out.println("fullscientificname: "+this.fullScientificNameString);
			if (withCdm){
				List<TaxonNameBase> names = app.getNameService().getNamesByName(this.fullScientificNameString);
				if (names.size() == 0){
					System.out.println("Name not found: " + this.fullScientificNameString);
				}else{
					if (names.size() > 1){
						System.out.println("More then 1 name found: " + this.fullScientificNameString);
					}
					taxonName = names.get(0);
				}
			}
			logger.info("Create new specimen ...");


			//set catalogue number (unitID)
			specimen.setCatalogNumber(this.unitID);
			specimen.setStoredUnder(taxonName);



			//manage institution
			Institution institution;
			List<Institution> institutions;
			try{
				institutions= app.getAgentService().searchInstitutionByCode(this.institutionCode);
			}catch(Exception e){
				System.out.println("BLI "+e);
				institutions=new ArrayList<Institution>();
			}
			if (institutions.size() ==0){
				System.out.println("Institution (agent) unknown");
				//create institution
				institution = Institution.NewInstance();
				institution.setCode(this.institutionCode);				
			}
			else{
				System.out.println("Institution (agent) already in the db");
				institution = institutions.get(0);
			}

			//manage collection
			Collection collection = Collection.NewInstance();
			List<Collection> collections;
			try{
				collections = app.getCollectionService().searchCollectionByCode(this.collectionCode);
			}catch(Exception e){
				System.out.println("BLA"+e);
				collections=new ArrayList<Collection>();
			}
			if (collections.size() ==0){
				System.out.println("Collection not found "+this.collectionCode);
				//create new collection
				collection.setCode(this.collectionCode);
				collection.setCodeStandard("GBIF");
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
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
					}
				}
				System.out.println("a trouvé la collection avec la meme institution? "+collectionFound);
				if (!collectionFound){ //need to add a new collection with the pre-configured institution
					collection.setCode(this.collectionCode);
					collection.setCodeStandard("GBIF");
					collection.setInstitute(institution);
				}

			}

			//link specimen & collection
			specimen.setCollection(collection);

			//create gathering event
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			//add locality
			gatheringEvent.setLocality(this.locality);

			//create coordinates point
			Point coordinates = Point.NewInstance();
			//add coordinates
			coordinates.setLatitude(this.latitude);
			coordinates.setLongitude(this.longitude);
			gatheringEvent.setExactLocation(coordinates);

			NamedArea area = NamedArea.NewInstance();
			//TODO	COUNTRY
//			WaterbodyOrCountry country = WaterbodyOrCountry.NewInstance();

//			area.addWaterbodyOrCountry(waterbodyOrCountry)
//			gatheringEvent.setCollectingArea(area);

			//create collector
			Agent collector = Person.NewInstance();
			ListIterator<String> collectors = this.gatheringAgentList.listIterator();
			//add the collectors
			while (collectors.hasNext()){
				collector.setTitleCache(collectors.next());
				gatheringEvent.setCollector(collector);
			}

			//create field/observation
			FieldObservation fieldObservation = FieldObservation.NewInstance();
			//add fieldNumber
			fieldObservation.setFieldNumber(fieldNumber);
			//join gatheringEvent to fieldObservation
			fieldObservation.setGatheringEvent(gatheringEvent);

			//save the specimen data
			app.getOccurrenceService().saveSpecimenOrObservationBase(specimen);
			//save the fieldObs. data
			app.getOccurrenceService().saveSpecimenOrObservationBase(fieldObservation);
			//save the taxon
			Taxon taxon = Taxon.NewInstance(taxonName, sec);
			app.getTaxonService().saveTaxon(taxon);


			logger.info("saved new specimen ...");



		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		return result;
	}



	private DeterminationEvent getDetermination(Taxon taxon, String actor){
		logger.info("Create determination event");
		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
		determinationEvent.setTaxon(taxon);
		Person person = Person.NewTitledInstance(actor);
		determinationEvent.setActor(person);
		return determinationEvent;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("main method");
		ABCD206Activator abcdAct = new ABCD206Activator();
		abcdAct.parseXML();
		abcdAct.invoke();
	}



}
