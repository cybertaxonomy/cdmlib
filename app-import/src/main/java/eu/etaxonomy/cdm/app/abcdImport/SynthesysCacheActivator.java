/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.abcdImport;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;




/**
 * @author PK
 * @created 19.09.2008
 * @version 1.0
 */
public class SynthesysCacheActivator {
	private static final Logger logger = Logger.getLogger(SynthesysCacheActivator.class);

	protected String fullScientificNameString = null;
	protected String institutionCode = null;
	protected String collectionCode = null;
	protected String unitID = null;
	protected String recordBasis = null;
	protected String accessionNumber = null;
	protected String collectorsNumber = null;
	protected String fieldNumber = null;
	protected Double longitude = null;
	protected Double latitude = null;
	protected String locality = null;
	protected String country = null;
	protected String isocountry = null;
	protected ArrayList<String> gatheringAgentList = new ArrayList<String>();
	protected ArrayList<String> identificationList = new ArrayList<String>();

	static DbSchemaValidation hbm2dll = DbSchemaValidation.UPDATE;

	protected HSSFWorkbook hssfworkbook = null;



	private ArrayList<Hashtable<String, String>> parseXLS() {
		String filename = "/home/patricia/Desktop/CDMtabular9c04a474e2_23_09_08.xls";
//		String filename = "/home/patricia/Desktop/synthesys.xls";
		ArrayList<Hashtable<String, String>> units = new ArrayList<Hashtable<String,String>>();
		
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start from first few rows
			for(int i = 0; i < 10 || i < rows; i++) {
				row = sheet.getRow(i);
				if(row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if(tmp > cols) cols = tmp;
				}
			}

			
			Hashtable<String, String> headers = null;
			ArrayList<String> columns = new ArrayList<String>();
			row = sheet.getRow(0);
			for (int c =0; c<cols; c++){
				cell = row.getCell(c);
				columns.add(cell.toString());
			}
			for(int r = 1; r < rows; r++) {
				row = sheet.getRow(r);
				headers = new Hashtable<String, String>();
				if(row != null) {
					for(int c = 0; c < cols; c++) {
						cell = row.getCell((short)c);
						if(cell != null) {
							headers.put(columns.get(c),cell.toString());
						}
					}
				}
				units.add(headers);
			}
			System.out.println("units: "+units);

			


		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
		return units;
	}


	public void saveUnit(Hashtable<String,String> unit){
		String author = unit.get("author");
		author=author.replaceAll("None","");
		String taxonName = unit.get("taxonName");
		taxonName = taxonName.replaceAll("None", "");

		try {
			this.institutionCode = unit.get("institution").replaceAll("None", null);
		} catch (Exception e) {
		}

		try {this.collectionCode = unit.get("collection").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.unitID = unit.get("unitID").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.recordBasis = unit.get("recordBasis").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.accessionNumber = null;
		} catch (Exception e) {
		}
		try {this.locality = unit.get("locality").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.longitude = Double.valueOf(unit.get("longitude"));
		} catch (Exception e) {
		}
		try {this.latitude = Double.valueOf(unit.get("latitude"));
		} catch (Exception e) {
		}
		try {this.country = unit.get("country").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.isocountry = unit.get("isoCountry").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.fieldNumber = unit.get("field number").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.collectorsNumber = unit.get("collector number").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {String coll =unit.get("collector");
		coll=coll.replaceAll("None", null);
		this.gatheringAgentList.add(coll);
		} catch (Exception e) {
		}
		try {this.identificationList.add(taxonName+" "+author);
		} catch (Exception e) {System.out.println(e);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean invoke(){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;
		TransactionStatus tx = null;


		app = CdmApplicationController.NewInstance(CdmDestinations.localH2(), hbm2dll);
		
		tx = app.startTransaction();
		try {
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			Reference sec = refFactory.newDatabase();
			sec.setTitleCache("SYNTHESYS CACHE DATA", true);

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
			System.out.println(this.identificationList);
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

				
//				tx = app.startTransaction();
				app.getNameService().saveOrUpdate(taxonName);
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
				collections = app.getCollectionService().searchByCode(this.collectionCode);
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
				System.out.println("a trouvé la collection avec la meme institution? "+collectionFound);
				if (!collectionFound){ //need to add a new collection with the pre-configured institution
					collection.setCode(this.collectionCode);
					collection.setCodeStandard("GBIF");
					collection.setInstitute(institution);
				}

			}
			System.out.println("collection inserted");
			//link specimen & collection
			derivedThing.setCollection(collection);

			/**
			 * GATHERING EVENT
			 */
			//create gathering event
			GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
			//add locality
			Language language = Language.DEFAULT();
			LanguageString loc = LanguageString.NewInstance(this.locality,language);
			gatheringEvent.setLocality(loc);

			//create coordinates point
			Point coordinates = Point.NewInstance();
			//add coordinates
			coordinates.setLatitude(this.latitude);
			coordinates.setLongitude(this.longitude);
			gatheringEvent.setExactLocation(coordinates);

			NamedArea area = NamedArea.NewInstance();
			

			WaterbodyOrCountry country = null;
//			System.out.println("isocountry "+this.isocountry);
			if (this.isocountry != null)
				country = app.getOccurrenceService().getCountryByIso(this.isocountry);
			
//			System.out.println(country.getLabel());
//			Set<Continent> cont = country.getContinents();
//			
//			System.out.println(cont.size());
//			Iterator<Continent> iter = cont.iterator();
//			while (iter.hasNext())
//				System.out.println(iter.next().toString());
			
			if (country != null){
				area.addWaterbodyOrCountry(country);
				System.out.println("country not null!");
			}
//			else{
//				if (this.country != null){
//					List<WaterbodyOrCountry>countries = app.getOccurrenceService().getWaterbodyOrCountryByName(this.country);
//					if (countries.size() >0)
//						area.addWaterbodyOrCountry(countries.get(0));
//					else
//						System.out.println("NO COUNTRY");//TODO need to add a new country!
//				}
//			}
//			app.getTermService().saveTerm(area);
			gatheringEvent.addCollectingArea(area);

			//create collector
			AgentBase collector;
			ListIterator<String> collectors = this.gatheringAgentList.listIterator();
			//add the collectors
			String collName;
			while (collectors.hasNext()){
				collName = collectors.next();
				/*check if the collector does already exist*/
				try{
					Pager<AgentBase> col = app.getAgentService().findByTitle(null, collName, null, null, null, null, null, null);
					collector=col.getRecords().get(0);
					System.out.println("a trouve l'agent");
				}catch (Exception e) {
					collector = Person.NewInstance();
					collector.setTitleCache(collName, true);
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
			try {
//				tx = app.startTransaction();
				app.getTermService().saveOrUpdate(area);//save it sooner
				app.getOccurrenceService().saveOrUpdate(derivedThing);
//				app.commitTransaction(tx);
//				app.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("PATATE "+e);
			}


			logger.info("saved new specimen ...");



		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
//		
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
		SynthesysCacheActivator abcdAct = new SynthesysCacheActivator();
		ArrayList<Hashtable<String,String>> units = abcdAct.parseXLS();
		Hashtable<String,String> unit=null;
		for (int i=0; i<units.size();i++){
			unit = units.get(i);
			System.out.println(unit);
			abcdAct.saveUnit(unit);//and then invoke
			abcdAct.invoke();
			
		}
	}



}
