/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_ALSO_PUBLISHED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_BIBLIOGRAPHY;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_PROTOLOGUE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelNameFactsImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelNameFactsImport  extends BerlinModelImportBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelNameFactsImport.class);

	public static final String NAMESPACE = "NameFact";

	/**
	 * write info message after modCount iterations
	 */
	private int modCount = 500;
	private static final String pluralString = "name facts";
	private static final String dbTableName = "NameFact";

	
	public BerlinModelNameFactsImport(){
		super(dbTableName, pluralString);
	}
	
	
	

	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		if (StringUtils.isNotEmpty(state.getConfig().getNameIdTable())){
			String result = super.getIdQuery(state);
			result += " WHERE ptNameFk IN (SELECT NameId FROM " + state.getConfig().getNameIdTable() + ")";
			return result;
		}else{
			return super.getIdQuery(state);
		}
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT NameFact.*, Name.NameID as nameId, NameFactCategory.NameFactCategory " + 
			" FROM NameFact INNER JOIN " +
              	" Name ON NameFact.PTNameFk = Name.NameId  INNER JOIN "+
              	" NameFactCategory ON NameFactCategory.NameFactCategoryID = NameFact.NameFactCategoryFK " + 
            " WHERE (NameFactId IN ("+ ID_LIST_TOKEN+") )";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonNameBase> nameToSave = new HashSet<TaxonNameBase>();
		Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		Map<String, Reference> biblioRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);

		ResultSet rs = partitioner.getResultSet();
		
		Reference<?> sourceRef = state.getTransactionalSourceReference();
		try {
			int i = 0;
			//for each reference
			while (rs.next() && (config.getMaximumNumberOfNameFacts() == 0 || i < config.getMaximumNumberOfNameFacts())){
				
				if ((i++ % modCount) == 0  && i!= 1 ){ logger.info("NameFacts handled: " + (i-1));}
				
				int nameFactId = rs.getInt("nameFactId");
				int nameId = rs.getInt("nameId");
				Object nameFactRefFkObj = rs.getObject("nameFactRefFk");
				String nameFactRefDetail = rs.getString("nameFactRefDetail");
				
				String category = CdmUtils.Nz(rs.getString("NameFactCategory"));
				String nameFact = CdmUtils.Nz(rs.getString("nameFact"));
				
				TaxonNameBase taxonNameBase = nameMap.get(String.valueOf(nameId));
				String nameFactRefFk = String.valueOf(nameFactRefFkObj);
				Reference citation = getReferenceOnlyFromMaps(biblioRefMap, 
						nomRefMap, nameFactRefFk);
				
				if (taxonNameBase != null){
					//PROTOLOGUE
					if (category.equalsIgnoreCase(NAME_FACT_PROTOLOGUE)){
						//Reference ref = (Reference)taxonNameBase.getNomenclaturalReference();
						//ref = Book.NewInstance();
						try{
							Media media = getMedia(nameFact, config.getMediaUrl(), config.getMediaPath());
							if (media.getRepresentations().size() > 0){
								TaxonNameDescription description = TaxonNameDescription.NewInstance();
								TextData protolog = TextData.NewInstance(Feature.PROTOLOGUE());
								protolog.addMedia(media);
								protolog.addSource(String.valueOf(nameFactId), NAMESPACE, null, null, null, null);
								description.addElement(protolog);
								taxonNameBase.addDescription(description);
								if (citation != null){
									description.addSource(null, null, citation, null);
									protolog.addSource(null, null, citation, nameFactRefDetail, null, null);
								}
							}//end NAME_FACT_PROTOLOGUE
						}catch(NullPointerException e){
							logger.warn("MediaUrl and/or MediaPath not set. Could not get protologue.");
							success = false;
						}						
					}else if (category.equalsIgnoreCase(NAME_FACT_ALSO_PUBLISHED_IN)){
						if (StringUtils.isNotBlank(nameFact)){
							TaxonNameDescription description = TaxonNameDescription.NewInstance();
							TextData additionalPublication = TextData.NewInstance(Feature.ADDITIONAL_PUBLICATION());
							//TODO language
							Language language = Language.DEFAULT();
							additionalPublication.putText(language, nameFact);
							additionalPublication.addSource(String.valueOf(nameFactId), NAMESPACE, citation, 
									nameFactRefDetail, null, null);
							description.addElement(additionalPublication);
							taxonNameBase.addDescription(description);
						}
					}else if (category.equalsIgnoreCase(NAME_FACT_BIBLIOGRAPHY)){
						if (StringUtils.isNotBlank(nameFact)){
							TaxonNameDescription description = TaxonNameDescription.NewInstance();
							TextData bibliography = TextData.NewInstance(Feature.CITATION());
							//TODO language
							Language language = Language.DEFAULT();
							bibliography.putText(language, nameFact);
							bibliography.addSource(String.valueOf(nameFactId), NAMESPACE, citation, 
									nameFactRefDetail, null, null);
							description.addElement(bibliography);
							taxonNameBase.addDescription(description);
						}
					}else {
						//TODO
						logger.warn("NameFactCategory '" + category + "' not yet implemented");
						success = false;
					}
					
					//TODO
//					DoubtfulFlag    bit        Checked
//					PublishFlag      bit        Checked
//					Created_When  datetime           Checked
//					Updated_When datetime           Checked
//					Created_Who    nvarchar(255)    Checked
//					Updated_Who  nvarchar(255)    Checked
//					Notes      nvarchar(1000)           Checked
					
					nameToSave.add(taxonNameBase);
				}else{
					//TODO
					logger.warn("TaxonName for NameFact " + nameFactId + " does not exist in store");
					success = false;
				}
				//put
			}
			if (config.getMaximumNumberOfNameFacts() != 0 && i >= config.getMaximumNumberOfNameFacts() - 1){ 
				logger.warn("ONLY " + config.getMaximumNumberOfNameFacts() + " NAMEFACTS imported !!!" )
			;};
			logger.info("Names to save: " + nameToSave.size());
			getNameService().save(nameToSave);	
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, nameIdSet, "PTnameFk");
				handleForeignKey(rs, referenceIdSet, "nameFactRefFk");
	}
	
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, Person> objectMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

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


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	
	//FIXME gibt es da keine allgemeine Methode in common?
	public Media getMedia(String nameFact, URL mediaUrl, File mediaPath){
		if (mediaUrl == null){
			logger.warn("Media Url should not be null");
			return null;
		}
		String mimeTypeTif = "image/tiff";
		String mimeTypeJpg = "image/jpeg";
		String mimeTypePng = "image/png";
		String mimeTypePdf = "application/pdf"; 
		String suffixTif = "tif";
		String suffixJpg = "jpg";
		String suffixPng = "png";
		String suffixPdf = "pdf"; 
		
		String sep = File.separator;
		Integer size = null;
		
		logger.debug("Getting media for NameFact: " + nameFact);
		
		Media media = Media.NewInstance();
		
		String mediaUrlString = mediaUrl.toString();

		//tiff
		String urlStringTif = mediaUrlString + "tif/" + nameFact + "." + suffixTif;
		File file = new File(mediaPath, "tif" + sep + nameFact + "." + suffixTif);
		MediaRepresentation representationTif = MediaRepresentation.NewInstance(mimeTypeTif, suffixTif);
		if (file.exists()){
			representationTif.addRepresentationPart(makeImage(urlStringTif, size, file));
		}
		if(representationTif.getParts().size() > 0){
			media.addRepresentation(representationTif);
		}
		// end tif
		// jpg
		boolean fileExists = true;
		int jpgCount = 0;
		MediaRepresentation representationJpg = MediaRepresentation.NewInstance(mimeTypeJpg, suffixJpg);
		while(fileExists){
			String urlStringJpeg = mediaUrlString + "cmd_jpg/" + nameFact + "_page_000" + jpgCount + "." + suffixJpg;		
			file = new File(mediaPath, "cmd_jpg" + sep + nameFact + "_page_000" + jpgCount + "." + suffixJpg);
			jpgCount++;
			if (file.exists()){ 
				representationJpg.addRepresentationPart(makeImage(urlStringJpeg, size, file));
			}else{
				fileExists = false;
			}
		}
		if(representationJpg.getParts().size() > 0){
			media.addRepresentation(representationJpg);
		}
		// end jpg
		//png
		String urlStringPng = mediaUrlString + "png/" + nameFact + "." + suffixPng;
		file = new File(mediaPath, "png" + sep + nameFact + "." + suffixPng);
		MediaRepresentation representationPng = MediaRepresentation.NewInstance(mimeTypePng, suffixPng);
		if (file.exists()){ 
			representationPng.addRepresentationPart(makeImage(urlStringPng, size, file));
		}else{
			fileExists = true;
			int pngCount = 0;
			while (fileExists){
				pngCount++;
				urlStringPng = mediaUrlString + "png/" + nameFact + "00" + pngCount + "." + suffixPng;
				file = new File(mediaPath, "png" + sep + nameFact + "00" + pngCount + "." + suffixPng);
				
				if (file.exists()){ 
					representationPng.addRepresentationPart(makeImage(urlStringPng, size, file));
				}else{
					fileExists = false;
				}
			}
		} 
		if(representationPng.getParts().size() > 0){
			media.addRepresentation(representationPng);
		}
		//end png
        //pdf 
        String urlStringPdf = mediaUrlString + "pdf/" + nameFact + "." + suffixPdf; 
        URI uriPdf;
		try {
			uriPdf = new URI(urlStringPdf);
			file = new File(mediaPath, "pdf" + sep + nameFact + "." + suffixPdf); 
	        MediaRepresentation representationPdf = MediaRepresentation.NewInstance(mimeTypePdf, suffixPdf); 
	        if (file.exists()){  
	                representationPdf.addRepresentationPart(MediaRepresentationPart.NewInstance(uriPdf, size)); 
	        }else{ 
	                fileExists = true; 
	                int pdfCount = 0; 
	                while (fileExists){ 
	                        pdfCount++; 
	                        urlStringPdf = mediaUrlString + "pdf/" + nameFact + "00" + pdfCount + "." + suffixPdf; 
	                        file = new File(mediaPath, "pdf/" + sep + nameFact + "00" + pdfCount + "." + suffixPdf); 
	                         
	                        if (file.exists()){  
	                                representationPdf.addRepresentationPart(MediaRepresentationPart.NewInstance(uriPdf, size)); 
	                        }else{ 
	                                fileExists = false; 
	                        } 
	                } 
	        }
			if(representationPdf.getParts().size() > 0){
	        	media.addRepresentation(representationPdf);
	        }
		} catch (URISyntaxException e) {
			e.printStackTrace();
			logger.error("URISyntaxException" + urlStringPdf);
		}
        //end pdf 
		
		if(logger.isDebugEnabled()){
			for (MediaRepresentation rep : media.getRepresentations()){
				for (MediaRepresentationPart part : rep.getParts()){
					logger.debug("in representation: " + part.getUri());
				}
			}
		}
		
		return media;
	}

	
	private ImageFile makeImage(String imageUri, Integer size, File file){
		ImageInfo imageMetaData = null;
		URI uri;
		try {
			uri = new URI(imageUri);
			try {
				imageMetaData = ImageInfo.NewInstance(uri, 0);
			} catch (IOException e) {
				logger.error("IOError reading image metadata." , e);
			} catch (HttpException e) {
				logger.error("HttpException reading image metadata." , e);
			}
			ImageFile image = ImageFile.NewInstance(uri, size, imageMetaData);
			return image;
		} catch (URISyntaxException e1) {
			logger.warn("URISyntaxException: " + imageUri);
			return null;
		}
		
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelNameFactsImportValidator();
		return validator.validate(state);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoNameFacts();
	}
	

	
	//for testing only
	public static void main(String[] args) {
		
		BerlinModelNameFactsImport nf = new BerlinModelNameFactsImport();
		
		URL url;
		try {
			url = new URL("http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/");
			File path = new File("/Volumes/protolog/protolog/");
			if(path.exists()){
				String fact = "Acanthocephalus_amplexifolius";
				// make getMedia public for this to work
				Media media = nf.getMedia(fact, url, path);
				logger.info(media);
				for (MediaRepresentation rep : media.getRepresentations()){
					logger.info(rep.getMimeType());
					for (MediaRepresentationPart part : rep.getParts()){
						logger.info(part.getUri());
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
	}
}
