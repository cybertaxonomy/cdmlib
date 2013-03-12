/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class UseImport {
	public static final Logger logger = Logger.getLogger(UseImport.class);

	public static ICdmDataSource dataSource() {
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "localhost";
		String cdmDB = "palm_use_cdm_db";
		String cdmUserName = "root";
		String cdmPWD = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, cdmPWD);
	}

	private static ICdmDataSource makeDestination(DatabaseTypeEnum dbType,
			String cdmServer, String cdmDB, int port, String cdmUserName,
			String pwd) {
		// establish connection
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName,
				pwd);
		ICdmDataSource destination;
		if (dbType.equals(DatabaseTypeEnum.MySQL)) {
			destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB,
					port, cdmUserName, pwd, null);
		} else if (dbType.equals(DatabaseTypeEnum.PostgreSQL)) {
			destination = CdmDataSource.NewPostgreSQLInstance(cdmServer, cdmDB,
					port, cdmUserName, pwd, null);
		} else {
			// TODO others
			throw new RuntimeException("Unsupported DatabaseType");
		}
		return destination;
	}

	public boolean importFromExcelSS(String xlsPath) {
		boolean success = true;
		
		CdmApplicationController applicationController = CdmApplicationController.NewInstance(dataSource());
		ConversationHolder conversation = applicationController.NewConversation();
		conversation.startTransaction();
		
		ITaxonService service = applicationController.getTaxonService();
		ITermService termService = applicationController.getTermService();
		IDescriptionService descService = applicationController.getDescriptionService();
		IReferenceService referenceService = applicationController.getReferenceService();
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(xlsPath);

		} catch (FileNotFoundException e) {
			success = false;
			System.out.println("File not found in the specified path.");
			e.printStackTrace();
		}

		POIFSFileSystem fileSystem = null;
		
		try {
			fileSystem = new POIFSFileSystem(inputStream);

			HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
			HSSFSheet sheet = workBook.getSheetAt(0);
			Iterator rows = sheet.rowIterator();
			// Iterator rows = sheet.rowIterator();
			ArrayList<ArrayList<String>> lstUpdates = new ArrayList<ArrayList<String>>();
			Set<Integer> lstTaxonIDs = new HashSet<Integer>();
			//Set<Integer> lstTaxonIDs;
		

			while (rows.hasNext()) {

				HSSFRow row = (HSSFRow) rows.next();
				System.out.println("Row No.: " + row.getRowNum());
				Iterator cells = row.cellIterator();
				ArrayList<String> lstTaxon = new ArrayList<String>();
				while (cells.hasNext()) {
					HSSFCell cell = (HSSFCell) cells.next();
				
					int intCellType = cell.getCellType();
					switch (intCellType) {
						case 0:
							int cellValue = (int) cell.getNumericCellValue(); 
							lstTaxon.add(Integer.toString(cellValue));
							break;
						case 1:
							lstTaxon.add(cell.getStringCellValue());
							break;
					}
				}
				lstUpdates.add(lstTaxon);
				lstTaxonIDs.add(Integer.parseInt(lstTaxon.get(0)));	
			}
			
			List<TaxonBase> taxa = service.findTaxaByID(lstTaxonIDs);
			for(TaxonBase idTaxa : taxa) {
				//System.out.println(idTaxa.getUuid().toString());
				System.out.println(idTaxa.getName());
			}
			
			
			MarkerType useMarkerType = (MarkerType) termService.find(UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039"));
			Marker useMarker = Marker.NewInstance(useMarkerType, true);
			for (ArrayList<String> lstUpdate : lstUpdates) {
				System.out.println("-----------------------------------------------: " + lstUpdate.get(1));
				String idTaxonToUpdate = lstUpdate.get(1);
				TaxonDescription newUseDescription = TaxonDescription.NewInstance();
				newUseDescription.addMarker(useMarker);
				newUseDescription.setTitleCache(lstUpdate.get(2));
				Reference citation = ReferenceFactory.newGeneric();
				Team authorTeam = Team.NewInstance();
				authorTeam.setTitleCache(lstUpdate.get(3));
				citation.setAuthorTeam(authorTeam);
				citation.setTitle(lstUpdate.get(4));
				
				//citation.
				TimePeriod year = TimePeriod.NewInstance(Integer.parseInt(lstUpdate.get(5)));
				citation.setDatePublished(year);
				citation.setTitleCache(lstUpdate.get(6));
				//citation.
				for(TaxonBase taxon : taxa) {
					String taxonUUID = taxon.getUuid().toString(); 
					//System.out.println(idTaxonToUpdate + "|" + taxonUUID);
					if(idTaxonToUpdate.equals(taxonUUID)) {
						logger.info("Processing Taxn " + taxon.getTitleCache() + " with UUID: " + taxon.getUuid());
						if(taxon.isInstanceOf(Synonym.class)) {
							Taxon bestCandidate = null;
							Synonym synonym = CdmBase.deproxy(taxon, Synonym.class);
							Set<Taxon> acceptetdCandidates = synonym.getAcceptedTaxa();
							if(!acceptetdCandidates.isEmpty()){
								bestCandidate = acceptetdCandidates.iterator().next();
								if(acceptetdCandidates.size() == 1){
									logger.info(acceptetdCandidates.size() + " Accepted taxa found for synonym " + taxon.getTitleCache() + ", using first one: " + bestCandidate.getTitleCache());
									Set<TaxonDescription> taxonDescriptions = bestCandidate.getDescriptions();
									if(!taxonDescriptions.isEmpty()) {
										TaxonDescription firstDescription = taxonDescriptions.iterator().next();
										//newUseSummary.addSource(null, null, citation, null);
										//firstDescription.addElement(newUseSummary);
									}
									else {
										logger.warn("No description container for: " + bestCandidate.getName());
										
									}
								} else {
									logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + "for synonym " + taxon.getTitleCache());
									//List<DescriptionElementBase> descriptionElements = descService.getDescriptionElementsForTaxon((Taxon) bestCandidate, null, null, null, null, null);
									Set<TaxonDescription> taxonDescriptions = bestCandidate.getDescriptions();
									if(!taxonDescriptions.isEmpty()) {
										TaxonDescription firstDescription = taxonDescriptions.iterator().next();
										//newUseSummary.addSource(null, null, citation, null);
										//firstDescription.addElement(newUseSummary);
									}
									else {
										logger.warn("No description container for: " + bestCandidate.getName());
										
									}
								}
							}
						}
						else {
							Taxon taxonAccepted = (Taxon) taxon;
							/*Set<TaxonDescription> taxonDescriptions = taxonAccepted.getDescriptions();
							if(!taxonDescriptions.isEmpty()) {
								TaxonDescription firstDescription = taxonDescriptions.iterator().next();
								//newUseSummary.addSource(null, null, citation, null);
								//firstDescription.addElement(newUseSummary);
							}
							else {
								logger.warn("No description container for: " + taxonAccepted.getName());
							}*/
							taxonAccepted.addDescription(newUseDescription);
							service.saveOrUpdate(taxonAccepted);
							
						}
					}
				}
				
			}
			conversation.commit(false);
			
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}

		return success;

	}
	
	private boolean loadUses() {
		boolean success = true;
		//String xslUseSummaryPathString = "C://workspace//Matched_UseSummary_referenceIdTaxEd_TaxonName.xls";
		//String xslUseSummaryPathString = "C://workspace//testUseSummaries.xls";
		
		
		String xslUseSummaryPathString = "//Users//alextheys//Projects//CDM_Trunk//Palm_Use_Data_Extension//CDMLib-apps//cdmlib-apps//UseImport//src//main//resources//Matched_UseSummary_referenceIdTaxEd_TaxonName.xls";
		
		//String xslUseRecordPathString = "C://workspace//UseRecordTerms_UseSummaryId.xls";
		//String xslUseRecordPathString = "C://workspace//testUseRecords.xls";
		//String xslUseRecordPathString = "C://workspace//test_useRecord.xls";
		String xslUseRecordPathString = "//Users//alextheys//Projects//CDM_Trunk//Palm_Use_Data_Extension//CDMLib-apps//cdmlib-apps//UseImport//src//main//resources//UseRecordTerms_UseSummaryId.xls";
		
		InputStream inputStream = null;
		
		
		CdmApplicationController applicationController = CdmApplicationController.NewInstance(dataSource());
		ConversationHolder conversation = applicationController.NewConversation();
		conversation.startTransaction();
		
		ITaxonService taxonService = applicationController.getTaxonService();
		ITermService termService = applicationController.getTermService();
		IDescriptionService descService = applicationController.getDescriptionService();
		IReferenceService referenceService = applicationController.getReferenceService();
			
		
		ArrayList<ArrayList<String>> lstUseSummaries = loadSpreadsheet(xslUseSummaryPathString);
		ArrayList<ArrayList<String>> lstUseRecords = loadSpreadsheet(xslUseRecordPathString);
		
		MarkerType useMarkerType = (MarkerType) termService.find(UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039"));
		Feature featureUseRecord = (Feature) termService.find(UUID.fromString("8125a59d-b4d5-4485-89ea-67306297b599"));
		Feature featureUseSummary = (Feature) termService.find(UUID.fromString("6acb0348-c070-4512-a37c-67bcac016279"));
		Pager<DefinedTermBase>  notAvailModPager = (Pager<DefinedTermBase> ) termService.findByTitle(Modifier.class, "N/A", null, null, null, null, null, null);
		Pager<DefinedTermBase>  notAvailStatePager = (Pager<DefinedTermBase> ) termService.findByTitle(State.class, "N/A", null, null, null, null, null, null);
		Modifier notAvailMod = (Modifier) notAvailModPager.getRecords().get(0);
		State notAvailState = (State) notAvailStatePager.getRecords().get(0);
		
		int i = 0;
		int j = 0;
		try {
			for (ArrayList<String> lstUseSummary : lstUseSummaries) {
				i++;
				String idTaxonToUpdate = lstUseSummary.get(3);
				TaxonBase taxon = taxonService.find(UUID.fromString(idTaxonToUpdate));
				if (taxon != null) {
					TaxonDescription newUseDescription = TaxonDescription.NewInstance();
					Marker useMarker = Marker.NewInstance(useMarkerType, true);
					newUseDescription.addMarker(useMarker);
					Reference useReference = null;
					Pager<Reference> reference = referenceService.findByTitle(Reference.class, lstUseSummary.get(5), null, null, null, null, null, null);
					if(reference.getCount() == 0) {
						System.out.println("Reference title: " + lstUseSummary.get(5) + " not found.");
					} else if(reference.getCount() > 0 ) {
						useReference = reference.getRecords().get(0);
					}
					IdentifiableSource source =IdentifiableSource.NewInstance(useReference, null);
					source.setOriginalNameString(taxon.getName().toString());
					newUseDescription.addSource(source);
					TextData useSummary = TextData.NewInstance(featureUseSummary);
					LanguageString languageString = LanguageString.NewInstance(lstUseSummary.get(1), Language.ENGLISH());
					useSummary.putText(languageString);
					descService.saveDescriptionElement(useSummary);
					newUseDescription.addElement(useSummary);
					for (ArrayList<String> lstUseRecord : lstUseRecords) {
						j++;
						//System.out.println("Processing UseSummary#: " + i + " ID:" + lstUseSummary.get(0) + "UseRecord: " + lstUseRecord.get(1));
						if(lstUseSummary.get(0).equals(lstUseRecord.get(0))) {
							CategoricalData useRecord = CategoricalData.NewInstance();
							useRecord.setFeature(featureUseRecord);
							String modifyingText = "";
							if(lstUseRecord.get(3) != null && lstUseRecord.get(3).length() > 0) {
								Pager<DefinedTermBase> useCategoryPager = termService.findByTitle(State.class, lstUseRecord.get(3), null, null, null, null, null, null);
								State useCategory = null;
								if(useCategoryPager.getCount() > 0) {
									useCategory = (State) useCategoryPager.getRecords().get(0);
								} else {
									useCategory = notAvailState;
								}
								StateData stateCatData = StateData.NewInstance(useCategory);
								stateCatData.setState(useCategory);
								stateCatData.putModifyingText(Language.ENGLISH(), "Use Category");
								modifyingText += useCategory.toString() + ";";
								useRecord.addState(stateCatData);
								
								 
								//useRecord.addState(stateData);
							} else {
								State useCategory = notAvailState;
								StateData stateCatData = StateData.NewInstance(useCategory);
								stateCatData.setState(useCategory);
								stateCatData.putModifyingText(Language.ENGLISH(), "Use Category");
								modifyingText += useCategory.toString() + ";";
								useRecord.addState(stateCatData);
								
							}
							
							if(lstUseRecord.get(4) != null && lstUseRecord.get(4).length() > 0) {
								Pager<DefinedTermBase> useSubCategoryPager = termService.findByTitle(State.class, lstUseRecord.get(4), null, null, null, null, null, null);
								State useSubCategory = null;
								if(useSubCategoryPager.getCount() > 0) {
									useSubCategory = (State) useSubCategoryPager.getRecords().get(0);
								
								} else {
									useSubCategory = notAvailState;
								}
								StateData stateSubCatData = StateData.NewInstance(useSubCategory);
								stateSubCatData.setState(useSubCategory);
								stateSubCatData.putModifyingText(Language.ENGLISH(), "Use SubCategory");
								modifyingText += useSubCategory.toString() + ";";
								useRecord.addState(stateSubCatData);
								
							}
							else {
								State useSubCategory = notAvailState;
								StateData stateSubCatData = StateData.NewInstance(useSubCategory);
								stateSubCatData.setState(useSubCategory);
								stateSubCatData.putModifyingText(Language.ENGLISH(), "Use SubCategory");
								modifyingText += useSubCategory.toString() + ";";
								useRecord.addState(stateSubCatData);
								
							}
							if(lstUseRecord.get(5) != null && lstUseRecord.get(5).length() > 0) {
								Pager<DefinedTermBase> countryPager = termService.findByTitle(Modifier.class, lstUseRecord.get(5), null, null, null, null, null, null);
								Modifier country = null;
								if(countryPager.getCount() > 0) {
									country = (Modifier) countryPager.getRecords().get(0);
								} else {
									country = notAvailMod;
								}
								modifyingText += country.toString() + ";";
								useRecord.addModifier(country);
							} else {
								Modifier country = notAvailMod;
								modifyingText += country.toString() + ";";
								useRecord.addModifier(country);
							}
							
							if(lstUseRecord.get(6) != null && lstUseRecord.get(6).length() > 0) {
								Pager<DefinedTermBase> plantPartPager = termService.findByTitle(Modifier.class, lstUseRecord.get(6), null, null, null, null, null, null);
								Modifier plantPart = null;
								if(plantPartPager.getCount() > 0) {
									plantPart = (Modifier) plantPartPager.getRecords().get(0);
								} else {
									plantPart = notAvailMod;
								}
								modifyingText += plantPart.toString() + ";";
								useRecord.addModifier(plantPart);
							}else {
								Modifier plantPart = notAvailMod;
								modifyingText += plantPart.toString() + ";";
								useRecord.addModifier(plantPart);
							}
							if(lstUseRecord.get(7) != null && lstUseRecord.get(7).length() > 0) {
								Pager<DefinedTermBase> humanGroupPager = termService.findByTitle(Modifier.class, lstUseRecord.get(7), null, null, null, null, null, null);
								Modifier humanGroup = null;
								if(humanGroupPager.getCount() > 0) {
									humanGroup = (Modifier) humanGroupPager.getRecords().get(0);
								} else {
									humanGroup = notAvailMod;
								}
								modifyingText += humanGroup.toString() + ";";
								useRecord.addModifier(humanGroup);
							} else {
								Modifier humanGroup = notAvailMod;
								modifyingText += humanGroup.toString() + ";";
								useRecord.addModifier(humanGroup);
							}
							if(lstUseRecord.get(8) != null && lstUseRecord.get(8).length() > 0) {
								Pager<DefinedTermBase> ethnicGroupPager = termService.findByTitle(Modifier.class, lstUseRecord.get(8), null, null, null, null, null, null);
								Modifier ethnicGroup = null;
								if(ethnicGroupPager.getCount() > 0) {
									ethnicGroup = (Modifier) ethnicGroupPager.getRecords().get(0);
									modifyingText += ethnicGroup.toString() + ";";
								} else {
									ethnicGroup = notAvailMod;
								}
								useRecord.addModifier(ethnicGroup);
							}
							else {
								Modifier ethnicGroup = notAvailMod;
								modifyingText += ethnicGroup.toString() + ";";
								useRecord.addModifier(ethnicGroup);
							}
							useRecord.putModifyingText(Language.ENGLISH(), modifyingText);
							descService.saveDescriptionElement(useRecord);
							newUseDescription.addElement(useRecord);
						}
					}
					
					
					
					if (taxon.isInstanceOf(Synonym.class)){
						Taxon bestCandidate = null;
						Synonym synonym = CdmBase.deproxy(taxon, Synonym.class);
						Set<Taxon> acceptetdCandidates = synonym.getAcceptedTaxa();
						if(!acceptetdCandidates.isEmpty()){
							bestCandidate = acceptetdCandidates.iterator().next();
							if(acceptetdCandidates.size() == 1){
								logger.info(acceptetdCandidates.size() + " Accepted taxa found for synonym " + taxon.getTitleCache() + ", using first one: " + bestCandidate.getTitleCache());
								bestCandidate.addDescription(newUseDescription);
								taxonService.saveOrUpdate(bestCandidate);
								conversation.commit();
							}
							else {
								logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + "for synonym " + taxon.getTitleCache());
								bestCandidate.addDescription(newUseDescription);
								taxonService.saveOrUpdate(bestCandidate);
								conversation.commit();
							}
						}
					} else {
						Taxon taxonAccepted = (Taxon) taxon;
						taxonAccepted.addDescription(newUseDescription);
						taxonService.saveOrUpdate(taxonAccepted);
						conversation.commit();
					}
				}
				else {
					System.out.println("Processing UseSummary#: " + i + " ID:" + lstUseSummary.get(0));
				}
			}
			
			conversation.close();
			applicationController.close();
		
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}
		return success;
		
	}
	
	//Completed and tested!
	private boolean loadTerms() {
		boolean success = true;
		
		//String xslPathString = "C://workspace//terms.xls";
		String xslPathString = "//Users//alextheys//Projects//CDM_Trunk//Palm_Use_Data_Extension//CDMLib-apps//cdmlib-apps//UseImport//src//main//resources//terms.xls";
		
		CdmApplicationController applicationController = CdmApplicationController.NewInstance(dataSource());
		ConversationHolder conversation = applicationController.NewConversation();
		conversation.startTransaction();
		
		ITaxonService service = applicationController.getTaxonService();
		ITermService termService = applicationController.getTermService();
		IVocabularyService vocabularyService = applicationController.getVocabularyService();
		IReferenceService referenceService = applicationController.getReferenceService();	
		
		TermVocabulary<State> stateVocabulary =  (TermVocabulary<State>) vocabularyService.find(UUID.fromString("67430d7c-fd43-4e9d-af5e-d0dca3f74931")); 
		TermVocabulary<Modifier> countryVocabulary = (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("116c51f1-e63a-46f7-a258-e1149a42868b"));  
		TermVocabulary<Modifier> plantPartVocabulary = (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("369914fe-d54b-4063-99ce-abc81d30ad35"));  
		TermVocabulary<Modifier> humanGroupVocabulary =  (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("ca46cea5-bdf7-438d-9cd8-e2793d2178dc"));
		
		IDescriptionService descService = applicationController.getDescriptionService();
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(xslPathString);

		} catch (FileNotFoundException e) {
			success = false;
			System.out.println("File not found in the specified path.");
			e.printStackTrace();
		}

		POIFSFileSystem fileSystem = null;
		
		try {
			fileSystem = new POIFSFileSystem(inputStream);

			HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
			HSSFSheet sheet = workBook.getSheetAt(0);
			Iterator rows = sheet.rowIterator();

			ArrayList<ArrayList<String>> lstUpdates = new ArrayList<ArrayList<String>>();
		
			while (rows.hasNext()) {

				HSSFRow row = (HSSFRow) rows.next();
				System.out.println("Row No.: " + row.getRowNum());
				Iterator cells = row.cellIterator();
				ArrayList<String> lstTerms = new ArrayList<String>();
				while (cells.hasNext()) {
					HSSFCell cell = (HSSFCell) cells.next();
				
					int intCellType = cell.getCellType();
					switch (intCellType) {
						case 0:
							int cellValue = (int) cell.getNumericCellValue(); 
							lstTerms.add(Integer.toString(cellValue));
							break;
						case 1:
							lstTerms.add(cell.getStringCellValue());
							break;
					}
				}
				lstUpdates.add(lstTerms);
				//lstTaxonIDs.add(Integer.parseInt(lstTaxon.get(0)));	
			}
			for (ArrayList<String> lstUpdate : lstUpdates) {
				int termType = Integer.parseInt(lstUpdate.get(0)); 
				switch (termType) {
				//Case 0 = UseCategory
				case 0:
					Pager<State> useCategoryPager = termService.findByRepresentationText(lstUpdate.get(1), State.class, null, null);
					State useCategory = null;
					State useSubCat = null;
					if (useCategoryPager.getCount()>0) {
						useCategory = useCategoryPager.getRecords().get(0);
					}
					if(useCategory == null) {
						useCategory = State.NewInstance(lstUpdate.get(1), lstUpdate.get(1), null);
					}
					//State useCategory = (State) termService.
					if(lstUpdate.size() > 2) {
						useSubCat = State.NewInstance(lstUpdate.get(2), lstUpdate.get(2), null);
						useCategory.addIncludes(useSubCat);
					}
					stateVocabulary.addTerm(useCategory);
					vocabularyService.saveOrUpdate(stateVocabulary);
					conversation.commit(true);
					break;
				
				//case 1: = HumanGroup
				case 1:
					Pager<Modifier> humanGroupPager = termService.findByRepresentationText(lstUpdate.get(1), Modifier.class, null, null);
					
					Modifier humanGroup = null;
					Modifier ethnicGroup = null;
					if(humanGroupPager.getCount()>0) {
						humanGroup = humanGroupPager.getRecords().get(0);
					}
					
					if(humanGroup == null) {
						humanGroup = Modifier.NewInstance(lstUpdate.get(1), lstUpdate.get(1), null);
					}
					
					if(lstUpdate.size() >2) {
						ethnicGroup = Modifier.NewInstance(lstUpdate.get(2), lstUpdate.get(2), null);
						humanGroup.addIncludes(ethnicGroup);
					}
					humanGroupVocabulary.addTerm(humanGroup);
					vocabularyService.saveOrUpdate(humanGroupVocabulary);
					conversation.commit(true);
					break;
				
				//case 2: = Country
				case 2:
					Pager<Modifier> countryPager = termService.findByRepresentationText(lstUpdate.get(1), Modifier.class, null, null);
					Modifier country = null;
					
					if(countryPager.getCount()>0) {
						country = countryPager.getRecords().get(0);
					}
					
					if(country == null) {
						country = Modifier.NewInstance(lstUpdate.get(1), lstUpdate.get(1), null);
						countryVocabulary.addTerm(country);
						vocabularyService.saveOrUpdate(countryVocabulary);
					}
					conversation.commit(true);
					break;
				
				//case 3: //plantPart
				case 3:
					Pager<Modifier> plantPartPager = termService.findByRepresentationText(lstUpdate.get(1), Modifier.class, null, null);
					Modifier plantPart = null;
					
					if(plantPartPager.getCount()>0) {
						plantPart = plantPartPager.getRecords().get(0);
					}
					
					if(plantPart == null) {
						plantPart = Modifier.NewInstance(lstUpdate.get(1), lstUpdate.get(1), null);
						plantPartVocabulary.addTerm(plantPart);
						vocabularyService.saveOrUpdate(plantPartVocabulary);
					}
					conversation.commit(true);
					break;
					
				}
			}
			conversation.close();	
			applicationController.close();
			
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
		
	}
	
	private ArrayList<ArrayList<String>> loadSpreadsheet(String xslPathString) {
		ArrayList<ArrayList<String>> lstUpdates = new ArrayList<ArrayList<String>>();
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(xslPathString);

		} catch (FileNotFoundException e) {
			System.out.println("File not found in the specified path.");
			e.printStackTrace();
		}
		
		POIFSFileSystem fileSystem = null;
		
		try {
			fileSystem = new POIFSFileSystem(inputStream);

			HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
			HSSFSheet sheet = workBook.getSheetAt(0);
			Iterator rows = sheet.rowIterator();
			// Iterator rows = sheet.rowIterator();
			//Set<Integer> lstTaxonIDs;
		

			while (rows.hasNext()) {

				HSSFRow row = (HSSFRow) rows.next();
				System.out.println("Row No.: " + row.getRowNum());
				Iterator cells = row.cellIterator();
				ArrayList<String> lstTerms = new ArrayList<String>();
				while (cells.hasNext()) {
					HSSFCell cell = (HSSFCell) cells.next();
				
					int intCellType = cell.getCellType();
					switch (intCellType) {
						case 0:
							int cellValue = (int) cell.getNumericCellValue(); 
							lstTerms.add(Integer.toString(cellValue));
							break;
						case 1:
							lstTerms.add(cell.getStringCellValue());
							break;
					}
				}
				lstUpdates.add(lstTerms);
				//lstTaxonIDs.add(Integer.parseInt(lstTaxon.get(0)));	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lstUpdates;
	}
	
	
	private boolean setupNecessaryItems() {
		boolean success = false;
		CdmApplicationController applicationController = CdmApplicationController.NewInstance(dataSource());
		ConversationHolder conversation = applicationController.NewConversation();
		
		
		ITaxonService service = applicationController.getTaxonService();
		ITermService termService = applicationController.getTermService();
		IVocabularyService vocabularyService = applicationController.getVocabularyService();
		IFeatureTreeService featureTreeService = applicationController.getFeatureTreeService();
		
		MarkerType existingMarkertype = (MarkerType)termService.find(UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039"));
		Feature featureUseRecord = (Feature) termService.find(UUID.fromString("8125a59d-b4d5-4485-89ea-67306297b599"));
		Feature featureUseSummary = (Feature) termService.find(UUID.fromString("6acb0348-c070-4512-a37c-67bcac016279"));
		TermVocabulary<State> stateVocabulary =  (TermVocabulary<State>) vocabularyService.find(UUID.fromString("67430d7c-fd43-4e9d-af5e-d0dca3f74931")); 
		TermVocabulary<Modifier> countryVocabulary = (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("116c51f1-e63a-46f7-a258-e1149a42868b"));  
		TermVocabulary<Modifier> plantPartVocabulary = (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("369914fe-d54b-4063-99ce-abc81d30ad35"));  
		TermVocabulary<Modifier> humanGroupVocabulary =  (TermVocabulary<Modifier>) vocabularyService.find(UUID.fromString("ca46cea5-bdf7-438d-9cd8-e2793d2178dc"));
		Pager<DefinedTermBase>  notAvailModPager = (Pager<DefinedTermBase> ) termService.findByTitle(Modifier.class, "N/A", null, null, null, null, null, null);
		Pager<DefinedTermBase>  notAvailStatePager = (Pager<DefinedTermBase> ) termService.findByTitle(State.class, "N/A", null, null, null, null, null, null);
		
		conversation.startTransaction();
		if (existingMarkertype == null) {
			existingMarkertype = MarkerType.NewInstance("use", "use", null);
			existingMarkertype.setUuid( UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039"));
			TermVocabulary<MarkerType> markerTypeVocabulary = (TermVocabulary<MarkerType>)vocabularyService.find((UUID.fromString("19dffff7-e142-429c-a420-5d28e4ebe305")));
			markerTypeVocabulary.addTerm(existingMarkertype);
			vocabularyService.saveOrUpdate(markerTypeVocabulary);
			conversation.commit(true);
		}
		if (stateVocabulary == null) {
			
			URI termSourceUri = null;
			try {
				termSourceUri = new URI("eu.etaxonomy.cdm.model.description.State");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			stateVocabulary = TermVocabulary.NewInstance("Use Category", "Use Category", null, termSourceUri);
			stateVocabulary.setUuid(UUID.fromString("67430d7c-fd43-4e9d-af5e-d0dca3f74931"));
			vocabularyService.saveOrUpdate(stateVocabulary);
			conversation.commit(true);
		}
		if (countryVocabulary == null) {
			URI termSourceUri = null;
			try {
				termSourceUri = new URI("eu.etaxonomy.cdm.model.description.Modifier");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			countryVocabulary = TermVocabulary.NewInstance("Country", "Country", null, termSourceUri);
			countryVocabulary.setUuid(UUID.fromString("116c51f1-e63a-46f7-a258-e1149a42868b"));
			
			vocabularyService.saveOrUpdate(countryVocabulary);
			conversation.commit(true);
		}
		if (plantPartVocabulary == null) {
			URI termSourceUri = null;
			try {
				termSourceUri = new URI("eu.etaxonomy.cdm.model.description.Modifier");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			plantPartVocabulary = TermVocabulary.NewInstance("Plant Part", "Plant Part", null, termSourceUri);
			plantPartVocabulary.setUuid(UUID.fromString("369914fe-d54b-4063-99ce-abc81d30ad35"));
			vocabularyService.saveOrUpdate(plantPartVocabulary);
			conversation.commit(true);
		}
		if (humanGroupVocabulary == null) {
			URI termSourceUri = null;
			try {
				termSourceUri = new URI("eu.etaxonomy.cdm.model.description.Modifier");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			humanGroupVocabulary = TermVocabulary.NewInstance("Human Group", "Human Group", null, termSourceUri);
			humanGroupVocabulary.setUuid(UUID.fromString("ca46cea5-bdf7-438d-9cd8-e2793d2178dc"));
			vocabularyService.saveOrUpdate(humanGroupVocabulary);
			conversation.commit(true);
		}
		if(featureUseRecord == null|| featureUseSummary == null) {
			TermVocabulary<Feature> featureVocabulary = (TermVocabulary<Feature>)vocabularyService.find((UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8")));
			FeatureTree palmWebFeatureTree = featureTreeService.find(UUID.fromString("72ccce05-7cc8-4dab-8e47-bf3f5fd848a0"));
			//List<FeatureTree> featureTrees = CdmStore.getService(IFeatureTreeService.class).list(FeatureTree.class, null, null, null, null);
			
			if (featureUseRecord == null ) {
				featureUseRecord = Feature.NewInstance("Use Record", "Use Record", null);
				featureUseRecord.setUuid(UUID.fromString("8125a59d-b4d5-4485-89ea-67306297b599"));
				featureUseRecord.isSupportsCategoricalData();
				featureUseRecord.setSupportsCategoricalData(true);
				featureVocabulary.addTerm(featureUseRecord);
				FeatureNode useRecFeatureNode = FeatureNode.NewInstance(featureUseRecord);
				palmWebFeatureTree.getRoot().addChild(useRecFeatureNode);
			}
			if (featureUseSummary == null) {
				featureUseSummary = Feature.NewInstance("Use", "Use", null);
				featureUseSummary.setUuid(UUID.fromString("6acb0348-c070-4512-a37c-67bcac016279"));
				featureUseSummary.isSupportsTextData();
				featureUseSummary.setSupportsTextData(true);
				//TermVocabulary<Feature> featureVocabulary = (TermVocabulary<Feature>)CdmStore.getService(IVocabularyService.class).find((UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8")));
				featureVocabulary.addTerm(featureUseSummary);
				FeatureNode useSumFeatureNode = FeatureNode.NewInstance(featureUseSummary);
				palmWebFeatureTree.getRoot().addChild(useSumFeatureNode);
			}
			
			vocabularyService.saveOrUpdate(featureVocabulary);
			featureTreeService.saveOrUpdate(palmWebFeatureTree);
			conversation.commit(true);
			
		}
		if(notAvailModPager.getCount() == 0) {
			Modifier notAvailMod = Modifier.NewInstance("N/A", "N/A", null);
			termService.saveOrUpdate(notAvailMod);
			conversation.commit(true);
		}
		
		if(notAvailStatePager.getCount() == 0) {
			State notAvailState = State.NewInstance("N/A", "N/A", null);
			termService.saveOrUpdate(notAvailState);
			conversation.commit(true);
		}
		/*if(featureUseRecord == null) {
			featureUseRecord = Feature.NewInstance("Use Record", "Use Record", null);
			featureUseRecord.setUuid(UUID.fromString("8125a59d-b4d5-4485-89ea-67306297b599"));
			featureUseRecord.isSupportsCategoricalData();
			featureUseRecord.setSupportsCategoricalData(true);
			//TermVocabulary<Feature> featureVocabulary = (TermVocabulary<Feature>)vocabularyService.find((UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8")));
			featureVocabulary.addTerm(featureUseRecord);
			FeatureTree palmWebFeatureTree = featureTreeService.find(UUID.fromString("72ccce05-7cc8-4dab-8e47-bf3f5fd848a0"));
			FeatureNode useRecFeatureNode = FeatureNode.NewInstance(featureUseRecord);
			palmWebFeatureTree.getRoot().addChild(useRecFeatureNode);
			vocabularyService.saveOrUpdate(featureVocabulary);
			featureTreeService.saveOrUpdate(palmWebFeatureTree);
			conversation.commit(true);
			
		}
		if(featureUseSummary == null) {
			featureUseSummary = Feature.NewInstance("Use Summary", "Use Summary", null);
			featureUseSummary.setUuid(UUID.fromString("6acb0348-c070-4512-a37c-67bcac016279"));
			featureUseSummary.isSupportsTextData();
			featureUseSummary.setSupportsTextData(true);
			//TermVocabulary<Feature> featureVocabulary = (TermVocabulary<Feature>)vocabularyService.find((UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8")));
			featureVocabulary.addTerm(featureUseSummary);
			FeatureTree palmWebFeatureTree = featureTreeService.find(UUID.fromString("72ccce05-7cc8-4dab-8e47-bf3f5fd848a0"));
			FeatureNode useRecFeatureNode = FeatureNode.NewInstance(featureUseSummary);
			palmWebFeatureTree.getRoot().addChild(useRecFeatureNode);
			vocabularyService.saveOrUpdate(featureVocabulary);
			featureTreeService.saveOrUpdate(palmWebFeatureTree);
			conversation.commit(true);
		}*/
		
		conversation.close();
		applicationController.close();
		
		return success;
	}

	public static void main(String[] args) {
		UseImport uiImport = new UseImport();
		// String xlsPath = ".//toload.xlsx";
		//String xlsPath = "C://workspace//CDM Trunk//UseImport//src//main//java//eu//etaxonomy//cdm//toLoad2.xls";
		
		uiImport.setupNecessaryItems();
		uiImport.loadTerms();
		uiImport.loadUses();
		//String xlsPath = "C://workspace//toLoad3.xls";
		//uiImport.importFromExcelSS(xlsPath);

	}
}
