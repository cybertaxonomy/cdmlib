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
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

import javax.xml.parsers.*; 

import org.springframework.transaction.TransactionStatus;
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
	protected String collectorsNumber;
	protected String fieldNumber;
	protected Double longitude;
	protected Double latitude;
	protected String locality;
	protected String country;
	protected String isocountry;
	protected int depth;
	protected int altitude;
	protected ArrayList<String> gatheringAgentList;
	protected ArrayList<String> identificationList;
	protected ArrayList<String> namedAreaList;


	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;



	private NodeList getUnitsNodeList(){
		NodeList unitList = null;
		try {
			// création d'une fabrique de documents
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

			// création d'un constructeur de documents
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();

			// lecture du contenu d'un fichier XML avec DOM
//			File xml = new File("/home/patricia/Desktop/multiABCD.xml");
			File xml = new File("/home/patricia/Desktop/abcd206_3.xml");
			Document document = constructeur.parse(xml);

			Element racine = document.getDocumentElement();
			unitList = racine.getElementsByTagName("Unit");

		}catch(Exception e){
			System.out.println(e);
		}
		return unitList;
	}

	private void setUnitProperties(Element racine){
		try{
			NodeList group,childs,identifications,results,taxonsIdentified,person,scnames;

			String tmpName = null;
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
															tmpName = scnames.item(n).getTextContent();
													}
												}
											}
										}
									}
								}
								if(identifications.item(m).getNodeName() == "PreferredFlag"){
									this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent());
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

			try {//ALTITUDE
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

			try {//PROFONDEUR
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
			System.out.println(this.namedAreaList);

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
			System.out.println(this.unitID);

		} catch (Exception e) {
			logger.info("Error occured while parsing XML file"+e);
		}

	}

	@SuppressWarnings("unchecked")
	public boolean invoke(){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;
		TransactionStatus tx = null;

		try {
			app = CdmApplicationController.NewInstance(CdmDestinations.cdm_test_patricia(), hbm2dll);
		} catch (DataSourceNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("DataSourceNotFoundException "+e1);
		} catch (TermNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("TermNotFoundException " +e1);
		}

		tx = app.startTransaction();
		try {
			ReferenceBase sec = Database.NewInstance();
			sec.setTitleCache("XML DATA");

			/**
			 * SPECIMEN OR OBSERVATION OR LIVING
			 */
			DerivedUnitBase derivedThing = null;
			//create specimen
			if (this.recordBasis != null){
				if (this.recordBasis.toLowerCase().startsWith("s")) {//specimen
					derivedThing = Specimen.NewInstance();				
				}
				else if (this.recordBasis.toLowerCase().startsWith("o")) {//observation
					derivedThing = Observation.NewInstance();				
				}
				else if (this.recordBasis.toLowerCase().startsWith("l")) {//living -> fossil, herbarium sheet....???
					derivedThing = LivingBeing.NewInstance();
				}
			}
			if (derivedThing == null) derivedThing = Observation.NewInstance();

			TaxonNameBase taxonName = null;
			Taxon taxon = null;
			DeterminationEvent determinationEvent = null;
			List<TaxonNameBase> names = null;
			NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
			String scientificName="";
			boolean preferredFlag=false;
			for (int i = 0; i < this.identificationList.size(); i++) {
				this.fullScientificNameString = this.identificationList.get(i);
				this.fullScientificNameString = this.fullScientificNameString.replaceAll(" et ", " & ");
				if (this.fullScientificNameString.indexOf("_preferred_") != -1){
					scientificName = this.fullScientificNameString.split("_preferred_")[0];
					String pTmp = this.fullScientificNameString.split("_preferred_")[1];
					if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1)
						preferredFlag=true;
					else
						preferredFlag=false;
				}
				else scientificName = this.fullScientificNameString;

//				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICZN(),null);
//				if (taxonName.hasProblem()){
//				System.out.println("pb ICZN");
//				taxonName  = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICBN(),null);
//				if (taxonName.hasProblem()){
//				System.out.println("pb ICBN");
//				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNB(), null);
//				if (taxonName.hasProblem()){
//				System.out.println("pb ICNB");
//				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNCP(), null);
//				if (taxonName.hasProblem()){
//				System.out.println("pb ICNCP");
//				}
//				}
//				}				
//				}
				taxonName = nvnpi.parseFullName(scientificName);
				if (withCdm){
					names = app.getNameService().getNamesByName(scientificName);
					if (names.size() == 0){
						System.out.println("Name not found: " + scientificName);
					}else{
						if (names.size() > 1){
							System.out.println("More then 1 name found: " + scientificName);
						}
						System.out.println("Name found");
						taxonName = names.get(0);
					}
				}

//				TransactionStatus tx = app.startTransaction();
				app.getNameService().saveTaxonName(taxonName);
				taxon = Taxon.NewInstance(taxonName, sec); //TODO use real reference for sec
//				app.commitTransaction(tx);


				determinationEvent = DeterminationEvent.NewInstance();
				determinationEvent.setTaxon(taxon);
				determinationEvent.setPreferredFlag(preferredFlag);
				derivedThing.addDetermination(determinationEvent);
			}


			//set catalogue number (unitID)
			derivedThing.setCatalogNumber(this.unitID);
			derivedThing.setAccessionNumber(this.accessionNumber);
			derivedThing.setCollectorsNumber(this.collectorsNumber);


			/**
			 * INSTITUTION & COLLECTION
			 */
			//manage institution
			Institution institution;
			List<Institution> institutions;
			try{
				System.out.println(this.institutionCode);
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
				collections = app.getOccurrenceService().searchCollectionByCode(this.collectionCode);
			}catch(Exception e){
				System.out.println("BLA"+e);
				collections=new ArrayList<Collection>();
			}
			if (collections.size() ==0){
				System.out.println("Collection not found "+this.collectionCode);
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
			//link specimen & collection
			derivedThing.setCollection(collection);

			/**
			 * GATHERING EVENT
			 */
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

			if (this.altitude != -9999)
				gatheringEvent.setAbsoluteElevation(this.altitude);
			if (this.depth != -9999)
				gatheringEvent.setAbsoluteElevation(this.depth);


			NamedArea area = NamedArea.NewInstance();
//			app.getTermService().saveTerm(area);

			WaterbodyOrCountry country = app.getOccurrenceService().getCountryByIso(this.isocountry);
			if (country != null){
				area.addWaterbodyOrCountry(country);
			}
			else{
				List<WaterbodyOrCountry>countries = app.getOccurrenceService().getWaterbodyOrCountryByName(this.country);
				if (countries.size() >0)
					area.addWaterbodyOrCountry(countries.get(0));
			}

			gatheringEvent.addCollectingArea(area);

			//create collector
			Agent collector;
			ListIterator<String> collectors = this.gatheringAgentList.listIterator();
			//add the collectors
			String collName;
			while (collectors.hasNext()){
				collName = collectors.next();
				/*check if the collector does already exist*/
				try{
					List<Agent> col = app.getAgentService().findAgentsByTitle(collName);
					collector=col.get(0);
					System.out.println("a trouve l'agent");
				}catch (Exception e) {
					collector = Person.NewInstance();
					collector.setTitleCache(collName);
				}
				gatheringEvent.setCollector(collector);
			}
			//create field/observation
			FieldObservation fieldObservation = FieldObservation.NewInstance();
			//add fieldNumber
			fieldObservation.setFieldNumber(this.fieldNumber);
			
			//join gatheringEvent to fieldObservation
			fieldObservation.setGatheringEvent(gatheringEvent);

//			//link fieldObservation and specimen
			DerivationEvent derivationEvent = DerivationEvent.NewInstance();
			derivationEvent.addOriginal(fieldObservation);
			derivedThing.addDerivationEvent(derivationEvent);
//			derivationEvent.addDerivative(derivedThing);

			/**
			 * SAVE AND STORE DATA
			 */			
			//save the specimen data
			//	app.getOccurrenceService().saveSpecimenOrObservationBase(fieldObservation);

			//TransactionStatus tx = app.startTransaction();
			app.getTermService().saveTerm(area);//save it sooner
			app.getOccurrenceService().saveSpecimenOrObservationBase(derivedThing);
			//app.commitTransaction(tx);

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
		NodeList unitsList = abcdAct.getUnitsNodeList();
		if (unitsList != null)
			for (int i=0;i<unitsList.getLength();i++){
				abcdAct.setUnitProperties((Element)unitsList.item(i));
				abcdAct.invoke();
				hbm2dll = DbSchemaValidation.UPDATE;
			}
	}



}
