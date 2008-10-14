package eu.etaxonomy.cdm.io.unitsPortal;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

public class TestABCD  extends SpecimenIoBase  implements ICdmIO {


	private static final Logger logger = Logger.getLogger(TestABCD.class);

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
	protected String languageIso = null;
	protected String country;
	protected String isocountry;
	protected int depth;
	protected int altitude;
	protected ArrayList<String> gatheringAgentList;
	protected ArrayList<String> identificationList;
	protected ArrayList<String> namedAreaList;

	protected HSSFWorkbook hssfworkbook = null;


	public TestABCD() {
		super();
	}

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

	private static ArrayList<Hashtable<String, String>> parseXLS(String fileName) {
		ArrayList<Hashtable<String, String>> units = new ArrayList<Hashtable<String,String>>();

		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fileName));
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

		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
		return units;
	}

	private void setUnitPropertiesXML(Element racine){
		try{
			NodeList group,childs,identifications,results,taxonsIdentified,person,scnames;

			String tmpName = null;
			try {
				group = racine.getChildNodes();
				for (int i=0; i< group.getLength(); i++){
					if (group.item(i).getNodeName() == "Identifications"){
						group = group.item(i).getChildNodes();
						break;
					}
				}
//				group = racine.getElementsByTagName("Identifications");
				this.identificationList = new ArrayList<String>();
				for (int j=0; j< group.getLength(); j++){
					if(group.item(j).getNodeName() == "Identification"){
						identifications = group.item(j).getChildNodes();
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
							else if(identifications.item(m).getNodeName() == "PreferredFlag"){
								this.identificationList.add(tmpName+"_preferred_"+identifications.item(m).getTextContent());
							}
							else{
								if (tmpName != null)
									this.identificationList.add(tmpName+"_preferred_"+"0");
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
		} catch (Exception e) {
			logger.info("Error occured while parsing XML file"+e);
		}
	}

	private void setUnitPropertiesExcel(Hashtable<String,String> unit){
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

	private Institution getInstitution(String institutionCode, CdmApplicationController app){
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
		return institution;
	}

	private Collection getCollection(String collectionCode, Institution institution, CdmApplicationController app){
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
		return collection;
	}

	private void setTaxonNameBase(CdmApplicationController app, DerivedUnitBase derivedThing, ReferenceBase sec){
		TaxonNameBase taxonName;
		String fullScientificNameString;
		Taxon taxon = null;
		DeterminationEvent determinationEvent = null;
		List<TaxonNameBase> names = null;
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		String scientificName="";
		boolean preferredFlag=false;
		//TODO Botanical Name from ABCD? Zoological? etc etc
		for (int i = 0; i < this.identificationList.size(); i++) {
			fullScientificNameString = this.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");
			if (fullScientificNameString.indexOf("_preferred_") != -1){
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1];
				if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1)
					preferredFlag=true;
				else
					preferredFlag=false;
			}
			else scientificName = fullScientificNameString;

//			taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICZN(),null);
//			if (taxonName.hasProblem()){
//			System.out.println("pb ICZN");
//			taxonName  = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICBN(),null);
//			if (taxonName.hasProblem()){
//			System.out.println("pb ICBN");
//			taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNB(), null);
//			if (taxonName.hasProblem()){
//			System.out.println("pb ICNB");
//			taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNCP(), null);
//			if (taxonName.hasProblem()){
//			System.out.println("pb ICNCP");
//			}
//			}
//			}				
//			}
			taxonName = nvnpi.parseFullName(scientificName);
			if (true){
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

			app.getNameService().saveTaxonName(taxonName);
			taxon = Taxon.NewInstance(taxonName, sec); //TODO use real reference for sec

			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
			derivedThing.addDetermination(determinationEvent);
		}

	}

	public boolean start(IImportConfigurator config){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;
		TransactionStatus tx = null;

		try {
			app = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
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
			if (derivedThing == null) 
				derivedThing = Observation.NewInstance();

			this.setTaxonNameBase(app, derivedThing, sec);


			//set catalogue number (unitID)
			derivedThing.setCatalogNumber(this.unitID);
			derivedThing.setAccessionNumber(this.accessionNumber);
			derivedThing.setCollectorsNumber(this.collectorsNumber);


			/**
			 * INSTITUTION & COLLECTION
			 */
			//manage institution
			Institution institution = this.getInstitution(this.institutionCode,app);
			//manage collection
			Collection collection = this.getCollection(this.collectionCode, institution, app); 
			//link specimen & collection
			derivedThing.setCollection(collection);

			/**
			 * GATHERING EVENT
			 */

			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(app, this.locality, this.languageIso, this.longitude, 
					this.latitude, this.gatheringAgentList);
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(this.isocountry, this.country,app);
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


	public boolean invoke(IImportConfigurator config){
		System.out.println("INVOKE Specimen Import");
		TestABCD test = new TestABCD();
		String sourceName = config.getSourceNameString();
		String extension = sourceName.substring(sourceName.length()-3, sourceName.length());
		System.out.println("extension: "+extension);
		if (extension.indexOf("xml") != -1)
		{
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
		}
		else{
			ArrayList<Hashtable<String,String>> unitsList = parseXLS(sourceName);
			if (unitsList != null){
				Hashtable<String,String> unit=null;
				for (int i=0; i<unitsList.size();i++){
					unit = unitsList.get(i);
					test.setUnitPropertiesExcel(unit);//and then invoke
					test.start(config);
					config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
				}
			}

		}
		return false;

	}

	public boolean check(IImportConfigurator config) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean invoke(IImportConfigurator config,
			CdmApplicationController app, Map stores) {
		invoke(config,stores);
		return false;
	}

	public boolean invoke(IImportConfigurator config, Map stores) {
		invoke(config);
		return false;
	}




}
