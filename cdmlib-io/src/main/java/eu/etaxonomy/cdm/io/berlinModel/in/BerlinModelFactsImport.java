/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelFactsImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelFactsImport.class);

	public static final String SEQUENCE_PREFIX = "ORDER: ";
	
	private int modCount = 10000;
	
	public BerlinModelFactsImport(){
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		logger.warn("Checking for Facts not yet fully implemented");
		result &= checkDesignationRefsExist(bmiConfig);
		return result;
	}

	private TermVocabulary<Feature> getFeatureVocabulary(){
		try {
			//TODO work around until service method works
			TermVocabulary<Feature> featureVocabulary =  BerlinModelTransformer.factCategory2Feature(1).getVocabulary();
			//TermVocabulary<Feature> vocabulary = getTermService().getVocabulary(vocabularyUuid);
			return featureVocabulary;
		} catch (UnknownCdmTypeException e) {
			logger.error("Feature vocabulary not available. New vocabulary created");
			return new TermVocabulary<Feature>() ;
		}
	}
	
	private MapWrapper<Feature> invokeFactCategories(BerlinModelImportConfigurator bmiConfig){
		
		MapWrapper<Feature> result = bmiConfig.getFeatureMap();
		Source source = bmiConfig.getSource();
		
		try {
			//get data from database
			String strQuery = 
					" SELECT FactCategory.* " + 
					" FROM FactCategory "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			
			TermVocabulary<Feature> featureVocabulary = getFeatureVocabulary();
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("FactCategories handled: " + (i-1));}
				
				int factCategoryId = rs.getInt("factCategoryId");
				String factCategory = rs.getString("factCategory");
				
					
				Feature feature;
				try {
					feature = BerlinModelTransformer.factCategory2Feature(factCategoryId);
				} catch (UnknownCdmTypeException e) {
					logger.warn("New Feature (FactCategoryId: " + factCategoryId + ")");
					feature = Feature.NewInstance(factCategory, factCategory, null);
					feature.setVocabulary(featureVocabulary);
					feature.setSupportsTextData(true);
					//TODO
//					MaxFactNumber	int	Checked
//					ExtensionTableName	varchar(100)	Checked
//					Description	nvarchar(1000)	Checked
//					locExtensionFormName	nvarchar(80)	Checked
//					RankRestrictionFk	int	Checked
				}
								
			//	featureMap.put(factCategoryId, feature);
				result.put(factCategoryId, feature);
	
			}
			Collection<Feature> col = result.getAllValues();
			getTermService().saveTermsAll(col);
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return null;
		}

	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state) {
		boolean result = true;
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.NOMREF_STORE);
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start makeFacts ...");
		
		MapWrapper<Feature> featureMap = invokeFactCategories(config);
		
		try {
			//get data from database
			String strQuery = 
					" SELECT Fact.*, PTaxon.RIdentifier as taxonId, RefDetail.Details " + 
					" FROM Fact " +
                      	" INNER JOIN PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk " +
                      	" LEFT OUTER JOIN RefDetail ON Fact.FactRefDetailFk = RefDetail.RefDetailId AND Fact.FactRefFk = RefDetail.RefFk " +
                      	" WHERE (1=1)" + 
                        " ORDER By Sequence";
			ResultSet rs = source.getResultSet(strQuery) ;
			ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
			
			int i = 0;
			//for each fact
			while (rs.next()){
				try{
					if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
					
					int factId = rs.getInt("factId");
					Object taxonIdObj = rs.getObject("taxonId");
					int taxonId = rs.getInt("taxonId");
					Object factRefFkObj = rs.getObject("factRefFk");
					int factRefFk = rs.getInt("factRefFk");
					Object categoryFkObj = rs.getObject("factCategoryFk");
					Integer categoryFk = rs.getInt("factCategoryFk");
					String details = rs.getString("Details");
					String fact = CdmUtils.Nz(rs.getString("Fact"));
					String notes = CdmUtils.Nz(rs.getString("notes"));
					Boolean doubtfulFlag = rs.getBoolean("DoubtfulFlag");
					Boolean publishFlag = rs.getBoolean("publishFlag");
					
					TaxonBase taxonBase = getTaxon(taxonMap, taxonIdObj, taxonId);
					Feature feature = getFeature(featureMap, categoryFkObj, categoryFk) ;
					
					if (taxonBase == null){
						logger.warn("Taxon for Fact " + factId + " does not exist in store");
						result = false;
					}else{
						Taxon taxon;
						if ( taxonBase instanceof Taxon ) {
							taxon = (Taxon) taxonBase;
						}else{
							logger.warn("TaxonBase " + (taxonIdObj==null?"(null)":taxonIdObj) + " for Fact " + factId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
							result = false;
							continue;
						}
						
						TaxonDescription taxonDescription = null;
						Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
						
						boolean isImage = false;
						Media media = null;
						//for diptera images
						if (categoryFk == 51){  //TODO check also FactCategory string
							isImage = true;
							String uri = fact;
							Integer size = null; 
							ImageMetaData imageMetaData = new ImageMetaData();
							URL url;
							try {
								url = new URL(fact.trim());
							} catch (MalformedURLException e) {
								logger.warn("Malformed URL. Image could not be imported: " + CdmUtils.Nz(uri));
								continue;
							}
							imageMetaData.readFrom(url);
							media = Media.NewInstance();
							MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
							media.addRepresentation(mediaRepresentation);
							ImageFile image = ImageFile.NewInstance(uri, size, imageMetaData);
							mediaRepresentation.addRepresentationPart(image);
							for (TaxonDescription desc: descriptionSet){
								if (desc.isImageGallery()){
									taxonDescription = desc;
								}
							}
							if (taxonDescription == null){
								taxonDescription = TaxonDescription.NewInstance();
								taxonDescription.setTitleCache(sourceRef == null ? "Image Galery":sourceRef.getTitleCache()+"-Image Galery");
								taxon.addDescription(taxonDescription);
								taxonDescription.setImageGallery(true);
							}
						}
						//all others (no image)
						else{ 
							for (TaxonDescription desc: descriptionSet){
								if (! desc.isImageGallery()){
									taxonDescription = desc;
								}
							}
							if (taxonDescription == null){
								taxonDescription = TaxonDescription.NewInstance();
								taxonDescription.setTitleCache(sourceRef == null ? null:sourceRef.getTitleCache());
								taxon.addDescription(taxonDescription);
							}
						}
					
						//textData
						TextData textData = null;
						boolean newTextData = true;

						// For Cichorieae DB: If fact category is 31 (Systematics) and there is already a Systematics TextData 
						// description element append the fact text to the existing TextData
						if(categoryFk == 31) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								String featureString = descriptionElement.getFeature().getRepresentation(Language.DEFAULT()).getLabel();
								if (descriptionElement instanceof TextData && featureString.equals("Systematics")) { // TODO: test
									textData = (TextData)descriptionElement;
									String factTextStr = textData.getText(Language.DEFAULT());
									// FIXME: Removing newlines doesn't work
									if (factTextStr.contains("\\r\\n")) {
										factTextStr = factTextStr.replaceAll("\\r\\n","");
									}
									StringBuilder factText = new StringBuilder(factTextStr);
									factText.append(fact);
									fact = factText.toString();
									newTextData = false;
									break;
								}
							}
						}
						
						if(newTextData == true)	{ textData = TextData.NewInstance(); }

						
						
						//for diptera database
						if (categoryFk == 99 && notes.contains("<OriginalName>")){
							notes = notes.replaceAll("<OriginalName>", "");
							notes = notes.replaceAll("</OriginalName>", "");
							fact = notes + ": " +  fact ;
						}
						//TODO textData.putText(fact, bmiConfig.getFactLanguage());  //doesn't work because  bmiConfig.getFactLanguage() is not not a persistent Language Object
						//throws  in thread "main" org.springframework.dao.InvalidDataAccessApiUsageException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language
						if (isImage){
							textData.addMedia(media);
							textData.setType(Feature.IMAGE());
						}else{
							textData.putText(fact, Language.DEFAULT());
							textData.setType(feature);
						}
						
						//
						ReferenceBase citation;
						if (factRefFkObj != null){
							citation = referenceMap.get(factRefFk);	
							if (citation == null){
								citation = nomRefMap.get(factRefFk);
							}
							if (citation == null && (factRefFk != 0)){
								logger.warn("Citation not found in referenceMap: " + factRefFk);
								result = false;
							}
						}else{
							citation = null;
						}

						if (citation != null || CdmUtils.isNotEmpty(details)){
							DescriptionElementSource originalSource = DescriptionElementSource.NewInstance();
							originalSource.setCitation(citation);
							originalSource.setCitationMicroReference(details);
							textData.addSource(originalSource);
						}
						taxonDescription.addElement(textData);
						//doubtfulFlag
						if (doubtfulFlag){
							textData.addMarker(Marker.NewInstance(MarkerType.IS_DOUBTFUL(), true));
						}
						//publisheFlag
						textData.addMarker(Marker.NewInstance(MarkerType.PUBLISH(), publishFlag));
						//Sequence
						Integer sequence = rs.getInt("Sequence");
						if (sequence != null && sequence != 999){
							String strSequence = String.valueOf(sequence);
							strSequence = SEQUENCE_PREFIX + strSequence;
							//TODO make it an Extension when possible
							//Extension datesExtension = Extension.NewInstance(textData, strSequence, ExtensionType.ORDER());
							Annotation annotation = Annotation.NewInstance(strSequence, Language.DEFAULT());
							textData.addAnnotation(annotation);
						}
						
						//						if (categoryFkObj == FACT_DESCRIPTION){
//							//;
//						}else if (categoryFkObj == FACT_OBSERVATION){
//							//;
//						}else if (categoryFkObj == FACT_DISTRIBUTION_EM){
//							//
//						}else {
//							//TODO
//							//logger.warn("FactCategory " + categoryFk + " not yet implemented");
//						}
						
						//notes
						doCreatedUpdatedNotes(state, textData, rs, "Fact");
						
						//TODO
						//Designation References -> unclear how to map to CDM
						//factId -> OriginalSource for descriptionElements not yet implemented
						//sequence -> textData is not an identifiable entity therefore extensions are not possible
						//fact category better
						
						taxonStore.add(taxon);
					}
				} catch (Exception re){
					logger.error("An exception occurred during the facts import");
					result = false;
				}
				//put
			}
			logger.info("Facts handled: " + (i-1));
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().saveTaxonAll(taxonStore);	
			
			logger.info("end makeFacts ..." + getSuccessString(result));
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private TaxonBase getTaxon(MapWrapper<TaxonBase> taxonMap, Object taxonIdObj, Integer taxonId){
		if (taxonIdObj != null){
			return taxonMap.get(taxonId);
		}else{
			return null;
		}
		
	}
	
	private Feature getFeature(MapWrapper<Feature> featureMap, Object categoryFkObj, Integer categoryFk){
		if (categoryFkObj != null){
			return featureMap.get(categoryFk); 
		}else{
			return null;
		}
		
	}
	
	private boolean checkDesignationRefsExist(BerlinModelImportConfigurator config){
		try {
			boolean result = true;
			Source source = config.getSource();
			String strQueryArticlesWithoutJournal = "SELECT Count(*) as n " +
					" FROM Fact " +
					" WHERE (NOT (PTDesignationRefFk IS NULL) ) OR " +
                      " (NOT (PTDesignationRefDetailFk IS NULL) )";
			ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+count+" Facts with not empty designation references. Designation references are not imported.");
				
				System.out.println("========================================================");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoFacts();
	}

}
