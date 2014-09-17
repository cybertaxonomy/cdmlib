/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.cyprus;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.api.application.CdmIoApplicationController;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 16.12.2010
 */
public class CyprusAltitudeActivator {
	private static final Logger logger = Logger.getLogger(CyprusAltitudeActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
//	static final URI source = cyprus_distribution();
	static final URI source = cyprus_altitude();

	
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql_test();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_dev();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_production();

	
	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("14d1e912-5ec2-4d10-878b-828788b70a87");
	
	//classification
	static final UUID classificationUuid = UUID.fromString("0c2b5d25-7b15-4401-8b51-dd4be0ee5cab");
	
	private static final String sourceReferenceTitle = "Cyprus Excel Altitude Import";
	
	
	//TODO move to Feature vocabulary
	private static final UUID uuidAltitudeFeature = UUID.fromString("1a28ed59-e15f-4001-b5c2-ea89f0012671");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	private void doImport(ICdmDataSource cdmDestination){
		
		
		ArrayList<HashMap<String, String>> excel;
		try {
			excel = ExcelUtils.parseXLS(source, "coreTax");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		CdmApplicationController app = CdmIoApplicationController.NewInstance(cdmDestination, hbm2dll);

		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
	
		TransactionStatus tx = app.startTransaction();
		
		UUID uuidMikle77 = UUID.fromString("9f5fa7ee-538b-4ae5-bd82-2a9503fea1d6");
		UUID uuidMikle85 = UUID.fromString("994403c4-c400-413d-9a1a-8531a40bfd8c");
		
		Reference<?> mikle77 = app.getReferenceService().find(uuidMikle77);
		Reference<?> mikle85 = app.getReferenceService().find(uuidMikle85);
		
		
		Feature altitudeFeature = (Feature) app.getTermService().find(uuidAltitudeFeature);
		if (altitudeFeature == null){
//			altitudeFeature = Feature.NewInstance("Altitude", "Altitude", "alt.");
//			altitudeFeature.setUuid(uuidAltitudeFeature);
//			featureVoc = app.getVocabularyService().find(UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8"));
//			featureVoc.addTerm(altitudeFeature);
			throw new RuntimeException("Could not find altitudinal range feature");
		}
		
		MeasurementUnit meter = (MeasurementUnit)app.getTermService().find(UUID.fromString("8bef5055-789c-41e5-bea2-8dc2ea8ecdf6"));
//		NamedArea cyprus = (NamedArea)app.getTermService().find(UUID.fromString("da4cce9a-439b-4cc4-8073-85dc75bae169"));
	
		int count =1;
		for (HashMap<String, String> row : excel){
			count++;
			UUID baseUuid = makeUuid(row, "uuid");
			UUID acceptedUuid = makeUuid(row, "acceptedNameUuid");
			UUID parentUuid = makeUuid(row, "parentUuid");
			
//			String altitude = row.get("Altitude-kumuliert");
			
			String altitudeMin = row.get("Min");
			String altitudeMax = row.get("Max");
			String acceptedName = row.get("AcceptedName");
			
			
			
			String source = row.get("Source");
			
			if (StringUtils.isBlank(altitudeMin)){
				continue;
			}
			
			boolean hasAltitude = false;
			Reference<?> sourceRef = getSource(source, mikle77, mikle85);
			Taxon taxon = getTaxon(app, baseUuid, acceptedUuid, parentUuid, acceptedName, count);
			if (taxon != null){
				TaxonDescription desc = getDescription(taxon, sourceRef);
				
				hasAltitude = makeAltitude(altitudeMin, altitudeMax, altitudeFeature, sourceRef, desc, meter, count);
//				hasAltitude = makeAltitudeOld(altitude, altitudeFeature, sourceRef, desc, meter, count);
				if (hasAltitude){
					if(desc.getTaxon() == null){
						taxon.addDescription(desc);
					}
					taxaToSave.add(taxon);
				}else{
					logger.warn("HasALtitude is false in " + count);
				}
			}else{
				logger.warn("Taxon not recognized in line " + count);
			}
		}

		app.getTaxonService().saveOrUpdate(taxaToSave);
		
//		tx.setRollbackOnly();
		app.commitTransaction(tx);
	}


	private Taxon getTaxon(CdmApplicationController app, UUID baseUuid, UUID acceptedUuid, UUID parentUuid, String acceptedName, int row) {
		TaxonBase<?> base = app.getTaxonService().find(baseUuid);
//		TaxonBase<?> parent = app.getTaxonService().find(parentUuid);
		
		//TODO
		Taxon result = null;
		if (base.isInstanceOf(Taxon.class)){
			Taxon t = CdmBase.deproxy(base, Taxon.class);
			if (t.getTaxonNodes().size() == 1 && t.getTaxonNodes().iterator().next().getClassification().getUuid().equals(classificationUuid)){
				result = t;
			}else{
				logger.warn("Base taxon (uuid) not in classification. Row: " + row +  ", Taxon: " + base.getTitleCache());
			}
		}
		if (result == null){
			TaxonBase<?> accepted = app.getTaxonService().find(acceptedUuid);
			Taxon t = CdmBase.deproxy(accepted, Taxon.class);;
			if (t.getTaxonNodes().size() == 1 && t.getTaxonNodes().iterator().next().getClassification().getUuid().equals(classificationUuid)){
				if (hasSynonym(t, base)){
					result = t;
				}else{
					logger.warn("Synonym relation has changed somehow. Row: " + row +  ", Taxon: " + base.getTitleCache());
				}
				
			}else{
				logger.warn("Accepted taxon not in classification. Row: " + row +  ", Taxon: " + base.getTitleCache());
			}
		}
		
		if (result != null){
			if (! result.getName().getTitleCache().equals(acceptedName)){
				logger.warn("AcceptedName and taxon name is not equal in " + row + ".\n" +
						" Accepted Name: " + acceptedName + ";\n" +
						" Taxon    Name: " + result.getName().getTitleCache());
			}
		}
		
		return result;
	}

	private boolean hasSynonym(Taxon t, TaxonBase<?> base) {
		if (base.isInstanceOf(Synonym.class)){
			for (SynonymRelationship rel : t.getSynonymRelations()){
				if (rel.getSynonym().equals(base)){
					return true;
				}
			}
		}
		return false;
	}

//	private static final Pattern altitudePattern = Pattern.compile("\\d{1,4}(-\\d{1,4})?");


	private boolean makeAltitude(String altitudeMin, String altitudeMax, Feature altitudeFeature, 
			Reference<?> sourceRef, TaxonDescription desc, MeasurementUnit meter, int row) {
	
		QuantitativeData data = QuantitativeData.NewInstance(altitudeFeature);
		
		//Meikle
		if (source != null){
			TaxonNameBase<?,?> nameUsedInSource = null;  //TODO
			data.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, sourceRef, null, nameUsedInSource, null);
		}
//		//Excel   //excel source not wanted by Ralf
//		TaxonNameBase<?,?> nameUsedInSource = null;  //TODO probably we don't want this
//		data.addSource(OriginalSourceType.Import, String.valueOf(row), "row", getSourceReference(), null, nameUsedInSource, null);
		
		data.setUnit(meter);
		
		Integer min = Integer.valueOf(altitudeMin);
		StatisticalMeasurementValue minValue = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MIN(), min);
		data.addStatisticalValue(minValue);

		Integer max = Integer.valueOf(altitudeMax);
		StatisticalMeasurementValue maxValue = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MAX(), max);
		data.addStatisticalValue(maxValue);
		
		desc.addElement(data);
		return true;
	}
	
//	private boolean makeAltitudeOld(String altitudeOrig, Feature feature, Reference<?> source, TaxonDescription desc, MeasurementUnit meter, int row) {
//		String altitude = altitudeOrig.trim().replace(" ", "");
//		Matcher matcher = altitudePattern.matcher(altitude);
//		
//		if (matcher.matches()){
//			QuantitativeData data = QuantitativeData.NewInstance(feature);
//			
//			//Meikle
//			if (source != null){
//				TaxonNameBase<?,?> nameUsedInSource = null;  //TODO
//				data.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, source, null, nameUsedInSource, null);
//			}
//			//Excel
//			TaxonNameBase<?,?> nameUsedInSource = null;  //TODO probably we don't want this
//			data.addSource(OriginalSourceType.Import, String.valueOf(row), "row", getSourceReference(), null, nameUsedInSource, null);
//			data.setUnit(meter);
//			
//			String[] split = altitude.split("-");
//
//			Integer min = Integer.valueOf(split[0]);
//			StatisticalMeasurementValue minValue = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MIN(), min);
//			data.addStatisticalValue(minValue);
//
//			if (split.length > 1){
//				Integer max = Integer.valueOf(split[1]);
//				StatisticalMeasurementValue maxValue = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MAX(), max);
//				data.addStatisticalValue(maxValue);
//			}
//			desc.addElement(data);
//			return true;
//		}else{
//			logger.warn("Altitude does not match in row " + row + ": "  + altitudeOrig);
//			return false;
//		}
//	}

	private TaxonDescription getDescription(Taxon taxon, Reference<?> sourceRef) {
		if (taxon != null){
			//TODO Mikle existiert derzeit nicht also Source
			
			TaxonDescription desc = TaxonDescription.NewInstance();
			desc.setTitleCache("Import from " + getSourceReference().getTitleCache(), true);
			desc.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, sourceRef,null);
			desc.addSource(OriginalSourceType.Import, null, null, getSourceReference(), null);
			
			return desc;
		}
		return null;
	}

	private Reference<?> getSource(String source, Reference<?> m77, Reference<?> m85) {
		if(StringUtils.isNotBlank(source)){
			if (source.equals("Meikle 1977")){
				return m77;
			}else if (source.equals("Meikle 1985")){
				return m85;
			}else{
				logger.warn("Source not recognized: " + source);
			}
		}
		return null;
	}

	/**
	 * @param row
	 * @return
	 */
	private UUID makeUuid(HashMap<String, String> row, String colName) {
		if (StringUtils.isBlank(row.get(colName))){
			return null;
		}else{
			return UUID.fromString(row.get(colName));
		}
	}

//	private void getRowValues(HashMap<String, String> row) {
//		// TODO Auto-generated method stub
//		HashMap<String, Object> = new HashM
//		row
//		
//		
//	}

	
	Reference<?> sourceReference;
	private Reference<?> getSourceReference() {
		if (sourceReference == null){
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache(sourceReferenceTitle, true);
			
		}
		return sourceReference;
			
	}

	
	//Cyprus
	public static URI cyprus_altitude() {
		URI sourceUrl;
		try {
			sourceUrl = new URI("file:/F:/data/cyprus/Cyprus-altitude-import-neu.xls");
//			sourceUrl = new URI("file:/F:/data/cyprus/Zypern-Altitude.xls");
			return sourceUrl;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CyprusAltitudeActivator me = new CyprusAltitudeActivator();
		me.doImport(cdmDestination);
		me.testMatcher();
	}

	private void testMatcher() {
//		makeAltitude("0-4400", null, null);
		
	}
	
}
