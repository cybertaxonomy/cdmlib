/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_PARTIAL_SYN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_PRO_PARTE_SYN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_SYNONYM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelCommonNamesImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * FIXME TO BE IMPLEMENTED (Common names)
 * 
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelCommonNamesImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelCommonNamesImport.class);

	public static final UUID REFERENCE_LANGUAGE_ISO639_2_UUID = UUID.fromString("40c4f8dd-3d9c-44a4-b77a-76e137a89a5f");
	public static final UUID REFERENCE_LANGUAGE_STRING_UUID = UUID.fromString("2a1b678f-c27d-48c1-b43e-98fd0d426305");
	public static final UUID STATUS_ANNOTATION_UUID = UUID.fromString("e3f7b80a-1286-458d-812c-5e818f731968");
	
	public static final String NAMESPACE = "common name";
	
	
	private static int modCount = 10000;
	private static final String pluralString = "common names";
	private static final String dbTableName = "emCommonName";


	public BerlinModelCommonNamesImport(){
		super();
	}
	
	

	@Override
	protected String getIdQuery() {
		String result = " SELECT CommonNameId FROM emCommonName ";
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String recordQuery = "";
		recordQuery = 
			" SELECT emCommonName.CommonNameId, emCommonName.CommonName, PTaxon.RIdentifier AS taxonId, emCommonName.RefFk AS refId, emCommonName.Status, emCommonName.RegionFks, emCommonName.MisNameRefFk, emCommonName.NameInSourceFk , " + 
        		" regionLanguage.Language AS regionLanguage, languageCommonName.Language, languageCommonName.LanguageOriginal, languageCommonName.ISO639_2, " + 
        		" emLanguageRegion.Region, emLanguageReference.RefFk as languageRefRefFk, emLanguageReference.ReferenceShort, " + 
        		" emLanguageReference.ReferenceLong, emLanguageReference.LanguageFk, languageReferenceLanguage.Language AS refLanguage, " +
        		" languageReferenceLanguage.ISO639_2 AS refLanguageIso639_2, regionLanguage.ISO639_2 AS regionLanguageIso " +
        	" FROM emLanguage as regionLanguage RIGHT OUTER JOIN " + 
        		" emLanguageRegion ON regionLanguage.LanguageId = emLanguageRegion.LanguageFk RIGHT OUTER JOIN " +
        		" emLanguage AS languageReferenceLanguage RIGHT OUTER JOIN " +
        		" emLanguageReference ON languageReferenceLanguage.LanguageId = emLanguageReference.LanguageFk RIGHT OUTER JOIN " +
        		" emCommonName INNER JOIN " +
        		" PTaxon ON emCommonName.PTNameFk = PTaxon.PTNameFk AND emCommonName.PTRefFk = PTaxon.PTRefFk ON " + 
        		" emLanguageReference.ReferenceId = emCommonName.LanguageRefFk LEFT OUTER JOIN " +
        		" emLanguage AS languageCommonName ON emCommonName.LanguageFk = languageCommonName.LanguageId ON " + 
        		" emLanguageRegion.RegionId = emCommonName.RegionFks ";
		return recordQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		Map<String, Taxon> taxonMap = (Map<String, Taxon>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		Map<String, TaxonNameBase> taxonNameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		
		Map<String, ReferenceBase> biblioRefMap = (Map<String, ReferenceBase>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, ReferenceBase> nomRefMap = (Map<String, ReferenceBase>) partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);
		
		logger.warn("Regions not yet implemented for Common Names");
		logger.warn("MisappliedNameRefFk  not yet implemented for Common Names");
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){

				//create TaxonName element
				Object commonNameId = rs.getObject("CommonNameId");
				int taxonId = rs.getInt("RIdentifier");
				Object refId = rs.getObject("refId");
				String commonNameString = rs.getString("CommonName");
				String iso639_2 = rs.getString("ISO639_2");
				String languageString = rs.getString("Language");
				String originalLanguageString = rs.getString("LanguageOriginal");
				Object misNameRefFk = rs.getObject("MisNameRefFk");
				Object languageRefRefFk = rs.getObject("languageRefRefFk");
				String refLanguage = rs.getString("refLanguage");
				String refLanguageIso639_2 = rs.getString("refLanguageIso639_2");
				String status = rs.getString("Status");
				Object nameInSourceFk = rs.getObject("NameInSourceFk");
				
				//TODO
				//String region = rs.getString("Region");
				//String regionFk  = rs.getString("RegionFks");
				
				
				if (CdmUtils.isEmpty(commonNameString)){
					String message = "CommonName is empty or null. Do not import record for taxon " + taxonId;
					logger.warn(message);
					continue;
				}
				
				Taxon taxon;
				TaxonBase taxonBase = null;
				taxonBase  = taxonMap.get(String.valueOf(taxonId));
				if (! taxonBase.isInstanceOf(Taxon.class)){
					logger.warn("taxon (" + taxonId + ") is not accepted. Can't import common name " +  commonNameId);
					continue;
				}else{
					taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				}
				
				//TODO test performance, implement in state
				Language language = getTermService().getLanguageByIso(iso639_2);
				addOriginalLanguage(language, originalLanguageString);
				if (language == null && CdmUtils.isNotEmpty(iso639_2)){
					logger.warn("Language for iso code '" + iso639_2 + "' and common name " + commonNameString + " was not found");
				}
				CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameString, language);
				TaxonDescription description = getDescription(taxon);
				description.addElement(commonTaxonName);
				
				String strRefId = String.valueOf(refId);
				String languageRefFk = String.valueOf(languageRefRefFk);
				if (! CdmUtils.nullSafeEqual(strRefId, languageRefRefFk)){
					logger.warn("CommonName.RefFk (" + CdmUtils.Nz(strRefId) + ") and LanguageReference.RefFk " + CdmUtils.Nz(languageRefFk) + " are not equal. I will import only languageRefFk");
				}
						
				ReferenceBase reference = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(languageRefRefFk));
				String microCitation = null;
				String originalNameString = null;
				TaxonNameBase nameUsedInSource = taxonNameMap.get(String.valueOf(nameInSourceFk));
				DescriptionElementSource source = DescriptionElementSource.NewInstance(reference, microCitation, nameUsedInSource, originalNameString);
				commonTaxonName.addSource(source);
				
				
				if (CdmUtils.isNotEmpty(refLanguage)){
					ExtensionType refLanguageExtensionType = getExtensionType( state, REFERENCE_LANGUAGE_STRING_UUID, "reference language","The language of the reference","ref. lang.");
					Extension.NewInstance(reference, refLanguage, refLanguageExtensionType);
				}
				
				if (CdmUtils.isNotEmpty(refLanguageIso639_2)){
					ExtensionType refLanguageIsoExtensionType = getExtensionType( state, REFERENCE_LANGUAGE_ISO639_2_UUID, "reference language iso 639-2","The iso 639-2 code of the references language","ref. lang. 639-2");
					Extension.NewInstance(reference, refLanguageIso639_2, refLanguageIsoExtensionType);
				}
				
				if (CdmUtils.isNotEmpty(status)){
					AnnotationType statusAnnotationType = getAnnotationType( state, STATUS_ANNOTATION_UUID, "status","The status of this object","status");
					Annotation annotation = Annotation.NewInstance(status, statusAnnotationType, Language.DEFAULT());
					commonTaxonName.addAnnotation(annotation);
				}
				
				//Notes
				doIdCreatedUpdatedNotes(state, commonTaxonName, rs, String.valueOf(commonNameId), NAMESPACE);
				
				partitioner.startDoSave();
				taxaToSave.add(taxon);

			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
			
		//	logger.info( i + " names handled");
		getTaxonService().save(taxaToSave);
		return success;

	}

	/**
	 * @param language
	 * @param originalLanguageString
	 */
	private void addOriginalLanguage(Language language,	String originalLanguageString) {
		if (CdmUtils.isEmpty(originalLanguageString)){
			return;
		}else if (language == null){
			logger.warn("Language could not be defined, but originalLanguageString exists: " + originalLanguageString);
		}else {
			Representation representation = language.getRepresentation(language);
			if (representation == null){
				language.addRepresentation(Representation.NewInstance(originalLanguageString, originalLanguageString, originalLanguageString, language));
				getTermService().saveOrUpdate(language);
			}
		}
		
	}

	/**
	 * @param taxon
	 * @return
	 */
	private TaxonDescription getDescription(Taxon taxon) {
		TaxonDescription result = null;
		for (TaxonDescription taxonDescription : taxon.getDescriptions()){
			if (! taxonDescription.isImageGallery()){
				result = taxonDescription;
			}
		}
		if (result == null){
			result = TaxonDescription.NewInstance(taxon);
		}
		return result;
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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
				handleForeignKey(rs, referenceIdSet, "refId");
				handleForeignKey(rs, referenceIdSet, "languageRefRefFk");
				handleForeignKey(rs, referenceIdSet, "NameInSourceFk");
				handleForeignKey(rs, referenceIdSet, "MisNameRefFk");
			}
			
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);

			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = Taxon.class;
			idSet = taxonIdSet;
			Map<String, Taxon> taxonMap = (Map<String, Taxon>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> nomReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> biblioReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
		


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelCommonNamesImportValidator();
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
		return ! state.getConfig().isDoCommonNames();
	}

}
