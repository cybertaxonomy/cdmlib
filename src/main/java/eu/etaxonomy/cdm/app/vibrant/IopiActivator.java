/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.vibrant;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.berlinModelImport.BerlinModelSources;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * TODO add the following to a wiki page:
 * HINT: If you are about to import into a mysql data base running under windows and if you wish to dump and restore the resulting data bas under another operation systen 
 * you must set the mysql system variable lower_case_table_names = 0 in order to create data base with table compatible names.
 * 
 * 
 * @author a.mueller
 *
 */
public class IopiActivator {
	private static final Logger logger = Logger.getLogger(IopiActivator.class);

	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	static final Source berlinModelSource = BerlinModelSources.iopi();
	
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
	static final ICdmDataSource cdmDestination = cdm_test_local_iopi();
	
	static final boolean useSingleClassification = true;
	static final Integer sourceSecId = 1892; //7000000; 
	static final UUID classificationUuid = null; //UUID.fromString("aa3fbaeb-f5dc-4e75-8d60-c8f93beb7ba6");
	
	static final UUID sourceRefUuid = UUID.fromString("df68c748-3c64-4b96-9a47-db51fb9d387e");
	
	// set to zero for unlimited nameFacts
	static final int maximumNumberOfNameFacts = 0;
	
	static final int partitionSize = 1500;
	
	//check - import
	static final CHECK check = CHECK.CHECK_AND_IMPORT;

	//editor - import
	static final EDITOR editor = EDITOR.EDITOR_AS_EDITOR;
	
	//NomeclaturalCode
	static final NomenclaturalCode nomenclaturalCode = NomenclaturalCode.ICBN;

	//ignore null
	static final boolean ignoreNull = true;
	
	static boolean useClassification = true;
	
	
	//filter
	static String taxonTable = "v_cdm_exp_taxaAll";
	static String classificationQuery = " SELECT DISTINCT t.PTRefFk, r.RefCache FROM PTaxon t INNER JOIN Reference r ON t.PTRefFk = r.RefId WHERE t.PTRefFk = " + sourceSecId; 
	static String relPTaxonIdQuery = " SELECT r.RelPTaxonId " + 
					" FROM RelPTaxon AS r INNER JOIN v_cdm_exp_taxaDirect AS a ON r.PTNameFk2 = a.PTNameFk AND r.PTRefFk2 = a.PTRefFk ";
	static String nameIdTable = " v_cdm_exp_namesAll ";
	static String referenceIdTable = " v_cdm_exp_refAll ";
	static String factFilter = " factId IN ( SELECT factId FROM v_cdm_exp_factsAll )";
	static String authorTeamFilter = null; // " authorTeamId IN (SELECT authorTeamId FROM v_cdm_exp_authorTeamsAll) ";
	static String authorFilter = null;  // " authorId IN (SELECT authorId FROM v_cdm_exp_authorsAll) "; 
	//not used
	static String occurrenceFilter = " occurrenceId IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )";
	static String occurrenceSourceFilter = " occurrenceFk IN ( SELECT occurrenceId FROM v_cdm_exp_occurrenceAll )"; 
	static String commonNameFilter = " commonNameId IN ( SELECT commonNameId FROM v_cdm_exp_commonNamesAll )";

	
	
// **************** ALL *********************	

	//authors
	static final boolean doAuthors = true;
	//references
	static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
	//names
	static final boolean doTaxonNames = true;
	static final boolean doRelNames = true;
	static final boolean doNameStatus = true;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doRelTaxa = true;
	static final boolean doFacts = true;

	//etc.

	
// **************** SELECTED *********************

//	static final boolean doUser = true;
//	//authors
//	static final boolean doAuthors = false;
//	//references
//	static final DO_REFERENCES doReferences =  DO_REFERENCES.NONE;
//	//names
//	static final boolean doTaxonNames = false;
//	static final boolean doRelNames = false;
//	static final boolean doNameStatus = false;
//	static final boolean doTypes = false;
//	static final boolean doNameFacts = false;
//	
//	//taxa 
//	static final boolean doTaxa = false;
//	static final boolean doRelTaxa = false;
//	static final boolean doFacts = false;
//	
//	//etc.
//	static final boolean doMarker = false;

	
//******** ALWAYS IGNORE *****************************
	
	static final boolean doUser = false;
	static final boolean doOccurences = false;
	static final boolean doCommonNames = false;
	static final boolean doTypes = false;  
	static final boolean doNameFacts = false;
	static final boolean doMarker = false;

	
	public void importIopi (Source source, ICdmDataSource destination, DbSchemaValidation hbm2dll){
		System.out.println("Start import from BerlinModel("+ berlinModelSource.getDatabase() + ") to " + cdmDestination.getDatabase() + " ...");
		//make BerlinModel Source
				
		BerlinModelImportConfigurator config = BerlinModelImportConfigurator.NewInstance(source,  destination);
		
		config.setClassificationUuid(classificationUuid);
		config.setSourceSecId(sourceSecId);
		
		config.setNomenclaturalCode(nomenclaturalCode);

		try {
			Method makeUrlMethod = this.getClass().getDeclaredMethod("makeUrlForTaxon", TaxonBase.class, ResultSet.class);
			config.setMakeUrlForTaxon(makeUrlMethod);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		config.setIgnoreNull(ignoreNull);
		config.setDoAuthors(doAuthors);
		config.setDoReferences(doReferences);
		config.setDoTaxonNames(doTaxonNames);
		config.setDoRelNames(doRelNames);
		config.setDoNameStatus(doNameStatus);
		config.setDoTypes(doTypes);
		config.setDoNameFacts(doNameFacts);
		config.setUseClassification(useClassification);
		config.setSourceRefUuid(sourceRefUuid);
		
		config.setDoTaxa(doTaxa);
		config.setDoRelTaxa(doRelTaxa);
		config.setDoFacts(doFacts);
		config.setDoOccurrence(doOccurences);
		config.setDoCommonNames(doCommonNames);
		
		config.setDoMarker(doMarker);
		config.setDoUser(doUser);
		config.setEditor(editor);
		config.setDbSchemaValidation(hbm2dll);
		
		// maximum number of name facts to import
		config.setMaximumNumberOfNameFacts(maximumNumberOfNameFacts);

		config.setUseSingleClassification(useSingleClassification);
		
		config.setCheck(check);
		config.setEditor(editor);
		config.setRecordsPerTransaction(partitionSize);
		
//		filter
		config.setTaxonTable(taxonTable);
		config.setClassificationQuery(classificationQuery);
		config.setRelTaxaIdQuery(relPTaxonIdQuery);
		config.setNameIdTable(nameIdTable);
		config.setReferenceIdTable(referenceIdTable);
		config.setAuthorTeamFilter(authorTeamFilter);
		config.setAuthorFilter(authorFilter);
		config.setFactFilter(factFilter);
		config.setCommonNameFilter(commonNameFilter);
		config.setOccurrenceFilter(occurrenceFilter);
		config.setOccurrenceSourceFilter(occurrenceSourceFilter);
		config.setUseSingleClassification(useSingleClassification);
	

		
		// invoke import
		CdmDefaultImport<BerlinModelImportConfigurator> bmImport = new CdmDefaultImport<BerlinModelImportConfigurator>();
		bmImport.invoke(config);
		
		System.out.println("End import from BerlinModel ("+ source.getDatabase() + ")...");
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IopiActivator importActivator = new IopiActivator();
		Source source = berlinModelSource;
		ICdmDataSource cdmRepository = CdmDestinations.chooseDestination(args) != null ? CdmDestinations.chooseDestination(args) : cdmDestination;
		
		importActivator.importIopi(source, cdmRepository, hbm2dll);

	}
	
	public static ICdmDataSource cdm_test_local_iopi(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "iopi"; 
		String cdmUserName = "root";
		return CdmDestinations.makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	//for FAUNA Europaea (http://rbg-web2.rbge.org.uk/FE/fe.html)
	private static final String URLbase = "http://rbg-web2.rbge.org.uk/cgi-bin/nph-readbtree.pl/feout?FAMILY_XREF=%s&GENUS_XREF=%s&SPECIES_XREF=%s&TAXON_NAME_XREF=%s&RANK=%s";
	public static Method makeUrlForTaxon(TaxonBase<?> taxon, ResultSet rs){
		Method result = null;
		ExtensionType urlExtensionType = ExtensionType.URL();
		
		String family = "";
		String genus = "";
		String species = "";
		String taxonName = "";
		String rankStr = "";
			
		NonViralName<?>  name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		Rank rank = name.getRank();
		rankStr = transformFeRanks(rank);
			
		
		if (rank.equals(Rank.FAMILY())){
			family = name.getGenusOrUninomial();
			taxonName = name.getGenusOrUninomial();
		}else if (rank.isHigher(Rank.GENUS())){
			taxonName = name.getGenusOrUninomial();
		}else if (rank.isGenus()){
			genus = name.getGenusOrUninomial();
			rankStr = "genus";
		}else if (rank.isInfraGeneric()){
			genus = name.getGenusOrUninomial();
			taxonName = name.getInfraGenericEpithet();
		}else if (rank.isSpecies()){
			genus = name.getGenusOrUninomial();
			species = name.getSpecificEpithet();
			rankStr = "species";
		}else if (rank.isInfraSpecific()){
			genus = name.getGenusOrUninomial();
			species = name.getSpecificEpithet();
			taxonName = name.getInfraSpecificEpithet();
		}
		
		
		String url = String.format(URLbase ,family, genus, species, taxonName, rankStr);
		taxon.addExtension(url, urlExtensionType);
		
		return result;
	}
	
	private static String transformFeRanks(Rank rank){
		if (rank.equals(Rank.SPECIESAGGREGATE())){ return "agg.";
		}else if (rank.equals(Rank.CLASS())){ return "Class";
		}else if (rank.equals(Rank.DIVISION())){ return "Division";
		}else if (rank.equals(Rank.FAMILY())){ return "family";
		}else if (rank.equals(Rank.FORM())){ return "forma";
		}else if (rank.equals(Rank.GENUS())){ return "genus";
		}else if (rank.equals(Rank.GREX())){ return "grex";
		}else if (rank.equals(Rank.SPECIESGROUP())){ return "group";
		}else if (rank.equals(Rank.ORDER())){ return "Order";
//		}else if (rank.equals(Rank.PROL())){ return "proles";
//--		}else if (rank.equals(Rank.RACE())){ return "race";
		}else if (rank.equals(Rank.SECTION_BOTANY())){ return "Sect.";
		}else if (rank.equals(Rank.SERIES())){ return "Ser.";
		}else if (rank.equals(Rank.SPECIES())){ return "species";
		}else if (rank.equals(Rank.SUBCLASS())){ return "Subclass";
		}else if (rank.equals(Rank.SUBDIVISION())){ return "Subdivision";
		}else if (rank.equals(Rank.SUBFORM())){ return "Subf.";
		}else if (rank.equals(Rank.SUBGENUS())){ return "Subgen.";
		}else if (rank.equals(Rank.SUBORDER())){ return "Suborder";
		}else if (rank.equals(Rank.SUBSECTION_BOTANY())){ return "Subsect.";
		}else if (rank.equals(Rank.SUBSERIES())){ return "Subser.";
		}else if (rank.equals(Rank.SUBSPECIES())){ return "subsp";
		}else if (rank.equals(Rank.SUBVARIETY())){ return "subvar.";
		}else if (rank.equals(Rank.SUPERORDER())){ return "Superorder";
		}else if (rank.equals(Rank.TRIBE())){ return "Tribe";
		}else if (rank.equals(Rank.VARIETY())){ return "var.";
		}else{
			logger.debug("Rank not found: " + rank.getTitleCache());
			return "";
		}	
	}
	

}
