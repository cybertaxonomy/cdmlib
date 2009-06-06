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
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_PROTOLOGUE;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelNameFactsImport  extends BerlinModelImportBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelNameFactsImport.class);

	/**
	 * write info message after modCount iterations
	 */
	private int modCount = 50;

	
	public BerlinModelNameFactsImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for NameFacts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState<BerlinModelImportConfigurator> state){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start makeNameFacts ...");

		try {
			//get data from database
			String strQuery = 
					" SELECT NameFact.*, Name.NameID as nameId, NameFactCategory.NameFactCategory " + 
					" FROM NameFact INNER JOIN " +
                      	" Name ON NameFact.PTNameFk = Name.NameId  INNER JOIN "+
                      	" NameFactCategory ON NameFactCategory.NameFactCategoryID = NameFact.NameFactCategoryFK " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next() && (config.getMaximumNumberOfNameFacts() == 0 || i < config.getMaximumNumberOfNameFacts())){
				
				if ((i++ % modCount) == 0  && i!= 1 ){ logger.info("NameFacts handled: " + (i-1));}
				
				int nameFactId = rs.getInt("nameFactId");
				int nameId = rs.getInt("nameId");
				int nameFactRefFk = rs.getInt("nameFactRefFk");
				int categoryFk = rs.getInt("nameFactCategoryFk");
				String category = CdmUtils.Nz(rs.getString("NameFactCategory"));
				String nameFact = CdmUtils.Nz(rs.getString("nameFact"));
				
				TaxonNameBase taxonNameBase = taxonNameMap.get(nameId);
				//taxonNameBase = BotanicalName.NewInstance(null);
				
				if (taxonNameBase != null){
					//PROTOLOGUE
					if (category.equalsIgnoreCase(NAME_FACT_PROTOLOGUE)){
						//ReferenceBase ref = (ReferenceBase)taxonNameBase.getNomenclaturalReference();
						//ref = Book.NewInstance();
						try{
							Media media = getMedia(nameFact, config.getMediaUrl(), config.getMediaPath());
							if (media.getRepresentations().size() > 0){
								TaxonNameDescription description = TaxonNameDescription.NewInstance();
								TextData protolog = TextData.NewInstance(Feature.PROTOLOG());
								protolog.addMedia(media);
								description.addElement(protolog);
								taxonNameBase.addDescription(description);
							}//end NAME_FACT_PROTOLOGUE
						}catch(NullPointerException e){
							logger.warn("MediaUrl and/or MediaPath not set. Could not get protologue.");
						}						
					}else if (category.equalsIgnoreCase(NAME_FACT_ALSO_PUBLISHED_IN)){
						if (! nameFact.equals("")){
							
							
							TaxonNameDescription description = TaxonNameDescription.NewInstance();
							TextData additionalPublication = TextData.NewInstance(Feature.ADDITIONAL_PUBLICATION());
							//TODO language
							Language language = Language.DEFAULT();
							additionalPublication.putText(nameFact, language);
							description.addElement(additionalPublication);
							taxonNameBase.addDescription(description);
						}
					}else {
						//TODO
						logger.warn("NameFactCategory '" + category + "' not yet implemented");
					}
					
					//TODO
//					NameFactRefFk            int        Checked
//					DoubtfulFlag    bit        Checked
//					PublishFlag      bit        Checked
//					Created_When  datetime           Checked
//					Updated_When datetime           Checked
//					Created_Who    nvarchar(255)    Checked
//					Updated_Who  nvarchar(255)    Checked
//					Notes      nvarchar(1000)           Checked
//					NameFactRefDetail       nvarchar(80)      Checked
					
					taxonNameStore.add(taxonNameBase);
				}else{
					//TODO
					logger.warn("TaxonName for NameFact " + nameFactId + " does not exist in store");
				}
				//put
			}
			if (config.getMaximumNumberOfNameFacts() != 0 && i >= config.getMaximumNumberOfNameFacts() - 1){ 
				logger.warn("ONLY " + config.getMaximumNumberOfNameFacts() + " NAMEFACTS imported !!!" )
			;};
			logger.info("Names to save: " + taxonNameStore.size());
			getNameService().saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeNameFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoNameFacts();
	}
	
	
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
		media.addRepresentation(representationTif);
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
		media.addRepresentation(representationJpg);
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
		media.addRepresentation(representationPng);
		//end png
        //pdf 
        String urlStringPdf = mediaUrlString + "pdf/" + nameFact + "." + suffixPdf; 
        file = new File(mediaPath, "pdf" + sep + nameFact + "." + suffixPdf); 
        MediaRepresentation representationPdf = MediaRepresentation.NewInstance(mimeTypePdf, suffixPdf); 
        if (file.exists()){  
                representationPdf.addRepresentationPart(MediaRepresentationPart.NewInstance(urlStringPdf, size)); 
        }else{ 
                fileExists = true; 
                int pdfCount = 0; 
                while (fileExists){ 
                        pdfCount++; 
                        urlStringPdf = mediaUrlString + "pdf/" + nameFact + "00" + pdfCount + "." + suffixPdf; 
                        file = new File(mediaPath, "pdf/" + sep + nameFact + "00" + pdfCount + "." + suffixPdf); 
                         
                        if (file.exists()){  
                                representationPdf.addRepresentationPart(MediaRepresentationPart.NewInstance(urlStringPdf, size)); 
                        }else{ 
                                fileExists = false; 
                        } 
                } 
        }  
        media.addRepresentation(representationPdf); 
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
		ImageMetaData imageMetaData = new ImageMetaData();
		imageMetaData.readFrom(file);
		ImageFile image = ImageFile.NewInstance(imageUri, size, imageMetaData);
		return image;
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
