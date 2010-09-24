/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.common.mediaMetaData.MediaMetaData;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
@Component
public class SpecimenExcelImport  extends SpecimenImportBase<SpecimenExcelImportConfigurator, SpecimenExcelImportState>  implements ICdmIO<SpecimenExcelImportState> {

	private static final Logger logger = Logger.getLogger(SpecimenExcelImport.class);

	protected String fullScientificNameString;
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
	protected ArrayList<String> namedAreaList;
	protected ArrayList<String> multimediaObjects;

	protected HSSFWorkbook hssfworkbook = null;


	public SpecimenExcelImport() {
		super();
	}


	/*
	 * Store the unit's properties into variables
	 * @param unit: the hashmap containing the splitted Excel line (Key=column name, value=value)
	 */
	private void setUnitPropertiesExcel(HashMap<String,String> unit){
		String author = unit.get("author");
		author=author.replaceAll("None","");
		String taxonName = unit.get("taxonName");
		taxonName = taxonName.replaceAll("None", "");

		try {
			this.institutionCode = unit.get("institution").replaceAll("None", null);
		} catch (Exception e) {this.institutionCode = "";}

		try {this.collectionCode = unit.get("collection").replaceAll("None", null);
		} catch (Exception e) {this.collectionCode = "";}

		try {this.unitID = unit.get("unitID").replaceAll("None", null);
		} catch (Exception e) {this.unitID = "";}

		try {this.recordBasis = unit.get("recordBasis").replaceAll("None", null);
		} catch (Exception e) {this.recordBasis = "";}

		try {this.accessionNumber = null;
		} catch (Exception e) {this.accessionNumber = "";}

		try {this.locality = unit.get("locality").replaceAll("None", null);
		} catch (Exception e) {this.locality = "";}

		try {this.longitude = Double.valueOf(unit.get("longitude"));
		} catch (Exception e) {this.longitude = 0.0;}

		try {this.latitude = Double.valueOf(unit.get("latitude"));
		} catch (Exception e) {this.latitude = 0.0;}

		try {this.country = unit.get("country").replaceAll("None", null);
		} catch (Exception e) {this.country = "";}

		try {this.isocountry = unit.get("isoCountry").replaceAll("None", null);
		} catch (Exception e) {this.isocountry = "";}

		try {this.fieldNumber = unit.get("field number").replaceAll("None", null);
		} catch (Exception e) {this.fieldNumber = "";}

		try {this.collectorsNumber = unit.get("collector number").replaceAll("None", null);
		} catch (Exception e) {this.collectorsNumber = "";}

		try {
			String url =unit.get("url");		
			url=url.replaceAll("None", null);
			this.multimediaObjects.add(url);
		} catch (Exception e) {this.multimediaObjects = new ArrayList<String>();}

		try {
			String coll =unit.get("collector");		
			coll=coll.replaceAll("None", null);
			this.gatheringAgentList.add(coll);
		} catch (Exception e) {this.gatheringAgentList = new ArrayList<String>();}

		try {this.identificationList.add(taxonName+" "+author);
		} catch (Exception e) {this.identificationList = new ArrayList<String>();}

	}

	private Institution getInstitution(String institutionCode, SpecimenExcelImportConfigurator config){
		Institution institution;
		List<Institution> institutions;
		try{
			institutions= getAgentService().searchInstitutionByCode(this.institutionCode);
		}catch(Exception e){
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
	private Collection getCollection(String collectionCode, Institution institution, SpecimenExcelImportConfigurator config){
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try{
			collections = getCollectionService().searchByCode(this.collectionCode);
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
	private void setTaxonNameBase(SpecimenExcelImportConfigurator config, DerivedUnitBase derivedThing, ReferenceBase sec){
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
				if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1)
					preferredFlag=true;
				else
					preferredFlag=false;
			}
			else scientificName = fullScientificNameString;

			if (fullScientificNameString.indexOf("_code_") != -1)	
				this.nomenclatureCode = fullScientificNameString.split("_code_")[1];

			if (config.getDoAutomaticParsing()){	
				taxonName = this.parseScientificName(scientificName);	
			} else {
				taxonName.setTitleCache(scientificName, true);
			}

			if (config.getDoReUseTaxon()){
				try{
					names = getTaxonService().searchTaxaByName(scientificName, sec);
					taxon = (Taxon)names.get(0);
				}
				catch(Exception e){taxon=null;}
			}
			if (!config.getDoReUseTaxon() || taxon == null){
				getNameService().save(taxonName);
				taxon = Taxon.NewInstance(taxonName, sec); //sec set null
			}

			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
//			no reference in the GBIF INDEX
//			for (int l=0;l<this.referenceList.size();l++){
//			ReferenceBase reference = new Generic();
//			reference.setTitleCache(this.referenceList.get(l));
//			determinationEvent.addReference(reference);
//			}
			derivedThing.addDetermination(determinationEvent);
		}

	}

	private NonViralName<?> parseScientificName(String scientificName){
		System.out.println("parseScientificName");
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?>taxonName = null;
		boolean problem=false;

		System.out.println("nomenclature: "+this.nomenclatureCode);

		if(this.nomenclatureCode == null){
			taxonName = NonViralName.NewInstance(null);
			taxonName.setTitleCache(scientificName, true);
			return taxonName;
		}

		if (this.nomenclatureCode.toString().equals("Zoological")){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN,null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Botanical")){
			taxonName  = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICBN,null);
			if (taxonName.hasProblem())
				problem=true;;}
		if (this.nomenclatureCode.toString().equals("Bacterial")){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB, null);
			if (taxonName.hasProblem())
				problem=true;
		}
		if (this.nomenclatureCode.toString().equals("Cultivar")){
			taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP, null);
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
			taxonName.setTitleCache(scientificName, true);
		}
		return taxonName;

	}


	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	public boolean start(SpecimenExcelImportConfigurator config){
		boolean result = true;
//		CdmApplicationController app2 = null;
		TransactionStatus tx = null;

//		app = config.getCdmAppController();
//		try {
//		app = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
//		} catch (DataSourceNotFoundException e1) {
//		e1.printStackTrace();
//		System.out.println("DataSourceNotFoundException "+e1);
//		} catch (TermNotFoundException e1) {
//		e1.printStackTrace();
//		System.out.println("TermNotFoundException " +e1);
//		}
		
		tx = startTransaction();
		try {
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

			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(getTermService(), this.locality, this.languageIso, this.longitude, 
					this.latitude, this.gatheringAgentList);
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(this.isocountry, this.country, getOccurrenceService());
			NamedArea areaCountry = unitsGatheringArea.getArea();
			unitsGatheringEvent.addArea(areaCountry);
			//Only for ABCD XML data
//			unitsGatheringArea = new UnitsGatheringArea(this.namedAreaList);
//			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
//			for (int i=0; i<nas.size();i++)
//				unitsGatheringEvent.addArea(nas.get(i));


			//create field/observation
			FieldObservation fieldObservation = FieldObservation.NewInstance();
			//add fieldNumber
			fieldObservation.setFieldNumber(this.fieldNumber);
			//join gatheringEvent to fieldObservation
			fieldObservation.setGatheringEvent(unitsGatheringEvent.getGatheringEvent());
			//add Multimedia URLs
			if(this.multimediaObjects.size()>0){
				MediaRepresentation representation;
				Media media;
				MediaMetaData mmd ;
				ImageMetaData imd ;
				URL url ;
				ImageFile imf;
				for (int i=0;i<this.multimediaObjects.size();i++){
					if(this.multimediaObjects.get(i) != null){
						//mmd = new MediaMetaData();
						imd = ImageMetaData.newInstance();
						url = new URL(this.multimediaObjects.get(i));
						//imd = MediaMetaData.readImageMetaData(url, imd);
						imd.readMetaData(url.toURI(), 0);
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

			getTermService().save(areaCountry);//save it sooner
			//ONLY FOR ABCD XML DATA
//			for (int i=0; i<nas.size();i++)
//				app.getTermService().saveTerm(nas.get(i));//save it sooner (foreach area)
			getTermService().saveLanguageData(unitsGatheringEvent.getLocality());//save it sooner
			getOccurrenceService().save(derivedThing);

			logger.info("saved new specimen ...");


		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		commitTransaction(tx);
		System.out.println("commit done");
		//app.close();
		return result;
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
	
//	public boolean doInvoke(SpecimenImportState state){
//		invoke(state.getConfig());
//		return false;
//	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(SpecimenExcelImportState state) {
		return false;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.specimen.SpecimenIoBase#doInvoke(eu.etaxonomy.cdm.io.specimen.abcd206.SpecimenImportState)
	 */
	@Override
	protected boolean doInvoke(SpecimenExcelImportState state) {
		System.out.println("INVOKE Specimen Import From Excel File (Synthesys Cache format");
		SpecimenExcelImport test = new SpecimenExcelImport();
		URI source = state.getConfig().getSource();
		ArrayList<HashMap<String,String>> unitsList = null;
		try{
			unitsList = ExcelUtils.parseXLS(source);
		} catch(FileNotFoundException e){
			String message = "File not found: " + source;
			warnProgress(state, message, e);
			logger.error(message);
		}
		System.out.println("unitsList"+unitsList);
		if (unitsList != null){
			HashMap<String,String> unit=null;
			for (int i=0; i<unitsList.size();i++){
				unit = unitsList.get(i);
				test.setUnitPropertiesExcel(unit);//and then invoke
				test.start(state.getConfig());
				state.getConfig().setDbSchemaValidation(DbSchemaValidation.UPDATE);
			}
		}

		return false;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(SpecimenExcelImportState state) {
		logger.warn("Validation not yet implemented for " + this.getClass().getSimpleName());
		return true;
	}


}
