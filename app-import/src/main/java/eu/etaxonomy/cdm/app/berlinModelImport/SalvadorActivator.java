/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.berlinModelImport;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class SalvadorActivator {
	private static final Logger logger = Logger.getLogger(SalvadorActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.El_Salvador();
//	static final Source berlinModelSource = BerlinModelDestinations.El_Salvador_Andreas();
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Salvador();
	static final UUID treeUuid = UUID.fromString("b010c84d-6049-45f4-9f13-c065101eaa26");
	static final UUID secUuid = UUID.fromString("d03ef02a-f226-4cb1-bdb4-f6c154f08a34");
	static final int sourceSecId = 7331;
	
	static final UUID featureTreeUuid = UUID.fromString("ae9615b8-bc60-4ed0-ad96-897f9226d568");
	static final Object[] featureKeyList = new Integer[]{302, 303, 306, 307, 309, 310, 311, 312, 350, 1500, 1800, 1900, 1950, 1980, 2000, 10299}; 
	static boolean isIgnore0AuthorTeam = true;  //special case for Salvador. 
	static boolean doExport = false;
	static boolean useTaxonomicTree = true;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;
	static final IImportConfigurator.EDITOR editor = IImportConfigurator.EDITOR.EDITOR_AS_EDITOR;

	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode  = NomenclaturalCode.ICBN;
	
	//ignore null
	static final boolean ignoreNull = true;

// ****************** ALL *****************************************
	
	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	static final boolean doTypes = false;  //Types do not exist in El_Salvador DB
	static final boolean doNameFacts = false;  //Name Facts do not exist in El_Salvador DB
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;
	static final boolean doOccurences = false; //Occurrences do not exist in El_Salvador DB
	static final boolean doCommonNames = false; //CommonNames do not exist in Salvador DB
	
	//etc.
	static final boolean doMarker = true;
	static final boolean doUser = true;
		

// ************************ NONE **************************************** //
	
//	//authors
//	static final boolean doAuthors = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.CONCEPT_REFERENCES;
//	//names
//	static final boolean doTaxonNames = false;
//	static final boolean doRelNames = false;
//	static final boolean doNameStatus = false;
//	static final boolean doTypes = false;
//	static final boolean doNameFacts = false;
//	
//	//taxa
//	static final boolean doTaxa = true;
//	static final boolean doRelTaxa = true;
//	static final boolean doFacts = false;
//	static final boolean doOccurences = false;
//	
//	//etc.
//	static final boolean doMarker = false;
//	static final boolean doUser = false;	
	
	
	public boolean doImport(ICdmDataSource destination){
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") ...");
		
		//make BerlinModel Source
		Source source = berlinModelSource;
		
		BerlinModelImportConfigurator config = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		config.setTaxonomicTreeUuid(treeUuid);
		config.setSecUuid(secUuid);
		config.setSourceSecId(sourceSecId);
		config.setNomenclaturalCode(nomenclaturalCode);
		config.setIgnoreNull(ignoreNull);
		
		config.setDoAuthors(doAuthors);
		config.setDoReferences(doReferences);
		config.setDoTaxonNames(doTaxonNames);
		config.setDoRelNames(doRelNames);
		config.setDoNameStatus(doNameStatus);
		config.setDoTypes(doTypes);
		config.setDoNameFacts(doNameFacts);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoFacts(doFacts);
		config.setDoOccurrence(doOccurences);
		config.setDoCommonNames(doCommonNames);
		
		config.setDoMarker(doMarker);
		config.setDoUser(doUser);
		
		config.setDbSchemaValidation(hbm2dll);

		config.setCheck(check);
		config.setEditor(editor);
		config.setIgnore0AuthorTeam(isIgnore0AuthorTeam);
		config.setUseTaxonomicTree(useTaxonomicTree);
		
		config.setNamerelationshipTypeMethod(getHandleNameRelationshipTypeMethod());
		config.setUserTransformationMethod(getTransformUsernameMethod());
		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		boolean result = bmImport.invoke(config);
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
		return result;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SalvadorActivator activator = new SalvadorActivator();
		ICdmDataSource destination = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;

		activator.doImport(destination);
		if (doExport == true){
			SalvadorExport export = new SalvadorExport();
			export.doExport(destination);
		}
	}

	
	private Method getHandleNameRelationshipTypeMethod(){
		String methodName = "handleNameRelationshipType";
		try {
			Method method = this.getClass().getDeclaredMethod(methodName, Integer.class, NonViralName.class, NonViralName.class);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			logger.error("Problem creating Method: " + methodName);
			return null;
		}
	}


	//used by BerlinModelImportConfigurator
	@SuppressWarnings("unused")
	private static boolean handleNameRelationshipType(Integer relQualifierFk, NonViralName nameTo, NonViralName nameFrom){
		if (relQualifierFk == 72){
			nameTo.getHomotypicalGroup().merge(nameFrom.getHomotypicalGroup());
			return true;
		}
		return false;
	}

	private Method getTransformUsernameMethod(){
		String methodName = "transformUsername";
		try {
			Method method = this.getClass().getDeclaredMethod(methodName, String.class);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			logger.error("Problem creating Method: " + methodName);
			return null;
		}
	}
	
	//used by BerlinModelImportConfigurator
	@SuppressWarnings("unused")
	private static String transformUsername(String nameToBeTransformed){
		if (nameToBeTransformed == null){
			return null;
		}else if ("W.G.Berendsohn".equals(nameToBeTransformed)){
			return "wgb";
		}else if(nameToBeTransformed.startsWith("fs") || nameToBeTransformed.equals("BGBM\\fs")){
			return "Frank Specht";
		}
		return nameToBeTransformed;
	}

}
