/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.mail.MethodNotSupportedException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelFactsImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.model.reference.Reference;
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

	public static final String NAMESPACE = "Fact";
	
	public static final String SEQUENCE_PREFIX = "ORDER: ";
	
	private int modCount = 10000;
	private static final String pluralString = "facts";
	private static final String dbTableName = "Fact";

	//FIXME don't use as class variable
	private Map<Integer, Feature> featureMap;
	
	public BerlinModelFactsImport(){
		super();
	}


	private TermVocabulary<Feature> getFeatureVocabulary(){
		try {
			//TODO work around until service method works
			TermVocabulary<Feature> featureVocabulary =  BerlinModelTransformer.factCategory2Feature(1).getVocabulary();
			//TermVocabulary<Feature> vocabulary = getTermService().getVocabulary(vocabularyUuid);
			return featureVocabulary;
		} catch (UnknownCdmTypeException e) {
			logger.error("Feature vocabulary not available. New vocabulary created");
			return TermVocabulary.NewInstance("User Defined Feature Vocabulary", "User Defined Feature Vocabulary", null, null); 
		}
	}
	
	private Map<Integer, Feature>  invokeFactCategories(BerlinModelImportState state){
		
		Map<Integer, Feature>  result = state.getConfig().getFeatureMap();
		Source source = state.getConfig().getSource();
		
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
					UUID featureUuid = null;
					if (factCategoryId == 14 && factCategory.startsWith("Maps")){
						//E+M maps
						featureUuid = BerlinModelTransformer.uuidFeatureMaps;  
					}else{
						logger.warn("New Feature (FactCategoryId: " + factCategoryId + ")");
					}
					feature = getFeature(state, featureUuid, factCategory, factCategory, null, featureVocabulary);
								feature = Feature.NewInstance(factCategory, factCategory, null);
//					featureVocabulary.addTerm(feature);
//					feature.setSupportsTextData(true);
					//TODO
//					MaxFactNumber	int	Checked
//					ExtensionTableName	varchar(100)	Checked
//					Description	nvarchar(1000)	Checked
//					locExtensionFormName	nvarchar(80)	Checked
//					RankRestrictionFk	int	Checked
				}
								
				result.put(factCategoryId, feature);
			}
//			Collection<Feature> col = result.values();
//			getTermService().saveOrUpdate((Collection)col);
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#doInvoke(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	@Override
	protected void doInvoke(BerlinModelImportState state) {
		featureMap = invokeFactCategories(state);
		super.doInvoke(state);
		return;
	}
		
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = super.getIdQuery(state);
		if (StringUtils.isNotBlank(state.getConfig().getFactFilter())){
			result += " WHERE " + state.getConfig().getFactFilter();
		}else{
			result = super.getIdQuery(state);
		}
		result += getOrderBy(state.getConfig());
		return result;
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery = 
					" SELECT Fact.*, PTaxon.RIdentifier as taxonId, RefDetail.Details " + 
					" FROM Fact " +
                      	" INNER JOIN PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk " +
                      	" LEFT OUTER JOIN RefDetail ON Fact.FactRefDetailFk = RefDetail.RefDetailId AND Fact.FactRefFk = RefDetail.RefFk " +
              	" WHERE (FactId IN (" + ID_LIST_TOKEN + "))";
			    strQuery += getOrderBy(config);
				
		return strQuery;
	}


	private String getOrderBy(BerlinModelImportConfigurator config) {
		String result;
		try{
			if (config.getSource().checkColumnExists("Fact", "Sequence")){
				result = " ORDER By Fact.Sequence, Fact.FactId";
			}else{
				result = " ORDER By Fact.FactId";
			}
		} catch (MethodNotSupportedException e) {
			logger.info("checkColumnExists not supported");
			result = " ORDER By Fact.FactId";
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		Map<String, Reference> biblioRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);

		ResultSet rs = partitioner.getResultSet();
		
		Reference<?> sourceRef = state.getTransactionalSourceReference();
			
		try{
			int i = 0;
			//for each fact
			while (rs.next()){
				try{
					if ((i++ % modCount) == 0){ logger.info("Facts handled: " + (i-1));}
					
					int factId = rs.getInt("factId");
					Object taxonIdObj = rs.getObject("taxonId");
					long taxonId = rs.getLong("taxonId");
					Object factRefFkObj = rs.getObject("factRefFk");
					Object categoryFkObj = rs.getObject("factCategoryFk");
					Integer categoryFk = rs.getInt("factCategoryFk");
					String details = rs.getString("Details");
					String fact = CdmUtils.Nz(rs.getString("Fact"));
					String notes = CdmUtils.Nz(rs.getString("notes"));
					Boolean doubtfulFlag = rs.getBoolean("DoubtfulFlag");
					
					TaxonBase<?> taxonBase = getTaxon(taxonMap, taxonIdObj, taxonId);
					Feature feature = getFeature(featureMap, categoryFkObj, categoryFk) ;
					
					if (taxonBase == null){
						logger.warn("Taxon for Fact " + factId + " does not exist in store");
						success = false;
					}else{
						TaxonDescription taxonDescription;
						if ( (taxonDescription = getMyTaxonDescripion(taxonBase, state, categoryFk, taxonIdObj, taxonId, factId, fact, sourceRef)) == null){
							success = false;
							continue;
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
						
						if(newTextData == true)	{ 
							textData = TextData.NewInstance(); 
						}
						
						//for diptera database
						if (categoryFk == 99 && notes.contains("<OriginalName>")){
//							notes = notes.replaceAll("<OriginalName>", "");
//							notes = notes.replaceAll("</OriginalName>", "");
							fact = notes + ": " +  fact ;
						}
						//for E+M maps
						if (categoryFk == 14 && state.getConfig().isRemoveHttpMapsAnchor() && fact.contains("<a href")){
							//example <a href="http://euromed.luomus.fi/euromed_map.php?taxon=280629&size=medium">distribution</a>
							fact = fact.replace("<a href\"", "").replace("\">distribution</a>", "");
						}
						
						//TODO textData.putText(fact, bmiConfig.getFactLanguage());  //doesn't work because  bmiConfig.getFactLanguage() is not not a persistent Language Object
						//throws  in thread "main" org.springframework.dao.InvalidDataAccessApiUsageException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: eu.etaxonomy.cdm.model.common.Language
						if (! taxonDescription.isImageGallery()){
							textData.putText(Language.DEFAULT(), fact);
							textData.setFeature(feature);
						}
						
						//reference
						Reference citation = null;
						String factRefFk = String.valueOf(factRefFkObj);
						if (factRefFkObj != null){
							citation = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, factRefFk);	
							}
						if (citation == null && (factRefFkObj != null)){
								logger.warn("Citation not found in referenceMap: " + factRefFk);
							success = false;
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
						String strPublishFlag = "publishFlag";
						boolean publishFlagExists = state.getConfig().getSource().checkColumnExists(dbTableName, strPublishFlag);
						if (publishFlagExists){
							Boolean publishFlag = rs.getBoolean(strPublishFlag);
							textData.addMarker(Marker.NewInstance(MarkerType.PUBLISH(), publishFlag));
						}
						
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
	//						//;
	//					}else if (categoryFkObj == FACT_OBSERVATION){
	//						//;
	//					}else if (categoryFkObj == FACT_DISTRIBUTION_EM){
	//						//
	//					}else {
	//						//TODO
	//						//logger.warn("FactCategory " + categoryFk + " not yet implemented");
	//					}
						
						//notes
						doCreatedUpdatedNotes(state, textData, rs);
						
						//TODO
						//Designation References -> unclear how to map to CDM
						//factId -> OriginalSource for descriptionElements not yet implemented
						
						//sequence -> textData is not an identifiable entity therefore extensions are not possible
						//fact category better
						
						taxaToSave.add(taxonBase);
					}
				} catch (Exception re){
					logger.error("An exception occurred during the facts import");
					re.printStackTrace();
					success = false;
				}
				//put
			}
			logger.info("Facts handled: " + (i-1));
			logger.info("Taxa to save: " + taxaToSave.size());
			getTaxonService().save(taxaToSave);	
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
		return success;
	}

	private TaxonDescription getMyTaxonDescripion(TaxonBase taxonBase, BerlinModelImportState state, Integer categoryFk, Object taxonIdObj, long taxonId, int factId, String fact, Reference<?> sourceRef) {
		Taxon taxon = null;
		if ( taxonBase instanceof Taxon ) {
			taxon = (Taxon) taxonBase;
		}else{
			logger.warn("TaxonBase " + (taxonIdObj==null?"(null)":taxonIdObj) + " for Fact " + factId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
			return null;
		}
		
		TaxonDescription taxonDescription = null;
		Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
		
		boolean isImage = false;
		Media media = null;
		//for diptera images
		if (categoryFk == 51){  //TODO check also FactCategory string
			isImage = true;
			media = Media.NewInstance();
			taxonDescription = makeImage(state, fact, media, descriptionSet, taxon);
			
			
			
			if (taxonDescription == null){
				return null;
			}
			
			TextData textData = null;
			for (DescriptionElementBase el:  taxonDescription.getElements()){
				if (el.isInstanceOf(TextData.class)){
					textData = CdmBase.deproxy(el, TextData.class);
				}
			}
			if (textData == null){
				textData = TextData.NewInstance(Feature.IMAGE());
				taxonDescription.addElement(textData);
			}
			textData.addMedia(media);
		}
		//all others (no image) -> getDescription
		else{ 
			for (TaxonDescription desc: descriptionSet){
				if (! desc.isImageGallery()){
					taxonDescription = desc;
				}
			}
			if (taxonDescription == null){
				taxonDescription = TaxonDescription.NewInstance();
				taxonDescription.setTitleCache(sourceRef == null ? null : sourceRef.getTitleCache(), true);
				taxon.addDescription(taxonDescription);
			}
		}
		return taxonDescription;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
			
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			Set<String> refDetailIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
				handleForeignKey(rs, referenceIdSet, "FactRefFk");
				handleForeignKey(rs, referenceIdSet, "PTDesignationRefFk");
				handleForeignKey(rs, refDetailIdSet, "FactRefDetailFk");
				handleForeignKey(rs, refDetailIdSet, "PTDesignationRefDetailFk");
		}
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);


			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> biblioReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);
			
			//nom refDetail map
			nameSpace = BerlinModelRefDetailImport.NOM_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> nomRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomRefDetailMap);
			
			//biblio refDetail map
			nameSpace = BerlinModelRefDetailImport.BIBLIO_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> biblioRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioRefDetailMap);
	
		} catch (SQLException e) {
			throw new RuntimeException(e);
	}
		return result;
	}
	
	
	/**
	 * @param state 
	 * @param media 
	 * @param media 
	 * @param descriptionSet 
	 * 
	 */
	private TaxonDescription makeImage(BerlinModelImportState state, String fact, Media media, Set<TaxonDescription> descriptionSet, Taxon taxon) {
		TaxonDescription taxonDescription = null;
		Reference sourceRef = state.getTransactionalSourceReference();
		Integer size = null; 
		ImageInfo imageInfo = null;
		URI uri;
		try {
			uri = new URI(fact.trim());
		} catch (URISyntaxException e) {
			logger.warn("URISyntaxException. Image could not be imported: " + fact);
			return null;
		}
		try {
			imageInfo = ImageInfo.NewInstance(uri, 0);
		} catch (IOException e) {
			logger.error("IOError reading image metadata." , e);
		} catch (HttpException e) {
			logger.error("HttpException reading image metadata." , e);
		}
		MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance(imageInfo.getMimeType(), null);
		media.addRepresentation(mediaRepresentation);
		ImageFile image = ImageFile.NewInstance(uri, size, imageInfo);
		mediaRepresentation.addRepresentationPart(image);
		
		taxonDescription = taxon.getOrCreateImageGallery(sourceRef == null ? null :sourceRef.getTitleCache());
		
		return taxonDescription;
	}

	private TaxonBase getTaxon(Map<String, TaxonBase> taxonMap, Object taxonIdObj, Long taxonId){
		if (taxonIdObj != null){
			return taxonMap.get(String.valueOf(taxonId));
		}else{
			return null;
		}
		
	}
	
	private Feature getFeature(Map<Integer, Feature>  featureMap, Object categoryFkObj, Integer categoryFk){
		if (categoryFkObj != null){
			return featureMap.get(categoryFk); 
		}else{
			return null;
		}
		
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelFactsImportValidator();
		return validator.validate(state);
	}
				
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
			}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
		}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoFacts();
	}



}
