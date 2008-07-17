/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_ALSO_PUBLISHED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_FACT_PROTOLOGUE;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;



/**
 * @author a.mueller
 *
 */
public class BerlinModelNameFactsIO  extends BerlinModelIOBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelNameFactsIO.class);

	private int modCount = 500000;

	public BerlinModelNameFactsIO(){
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
	protected boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, 
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		INameService nameService = cdmApp.getNameService();
		
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
			int border = 500000;
			//for each reference
			while (rs.next() && i < border){
				
				if ((i++ % modCount) == 0){ logger.info("NameFacts handled: " + (i-1));}
				
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
						ReferenceBase ref = (ReferenceBase)taxonNameBase.getNomenclaturalReference();
						//ref = Book.NewInstance();
						
						if (ref != null){
						
							String mimeTypeTif = "image/tiff";
							String mimeTypeJpeg = "image/jpeg";
							String mimeTypePng = "image/png";
							String suffixTif = "tif";
							String suffixJpg = "jpg";
							String suffixPng = "png";
							String sep = File.separator;
							Integer size = null;
							
							
							Media media = Media.NewInstance();
							ImageMetaData imageMetaData = new ImageMetaData();
							String urlPath = "http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/";
							String strFolderPath = sep + sep + "Bgbm11" + sep  + "Edit-WP6" + sep + "protolog" + sep;
							//tiff
							String urlTif = urlPath + "tif/" + nameFact + "." + suffixTif;
							String fileTif = strFolderPath + "tif" + sep + nameFact + "." + suffixTif;;
							//if (CdmUtils.urlExists(urlTif, true)){
							if (imageMetaData.readFrom(new File(strFolderPath))){
								ImageFile tifImage = ImageFile.NewInstance(urlTif, size, imageMetaData);
								MediaRepresentation tifRepresentation = MediaRepresentation.NewInstance(mimeTypeTif, suffixTif);
								tifRepresentation.addRepresentationPart(tifImage);
								media.addRepresentation(tifRepresentation);
							}
							//jpeg
							boolean fileExists = true;
							int jpgCount = 0;
							while (fileExists){
								jpgCount++;
								String urlJpeg = urlPath + "jpeg/" + nameFact + "_p" + jpgCount + "." + suffixJpg;
								String fileJpeg = strFolderPath + "jpeg" + sep + nameFact + "_p" + jpgCount + "." + suffixJpg;
								if (imageMetaData.readFrom(new File(fileJpeg))){
								//if (CdmUtils.urlExists(urlJpeg, true)){
									ImageFile jpgImage = ImageFile.NewInstance(urlJpeg, size, imageMetaData);
									MediaRepresentation jpgRepresentation = MediaRepresentation.NewInstance(mimeTypeJpeg, suffixJpg);
									jpgRepresentation.addRepresentationPart(jpgImage);
									media.addRepresentation(jpgRepresentation);
								}else{
									fileExists = false;
								}
							}
							//png
							String urlPng = urlPath + "png/" + nameFact + "." + suffixPng;
							String filePng = strFolderPath + "png" + sep + nameFact + "." + suffixPng;
							if (imageMetaData.readFrom(new File(filePng))){
							//if (CdmUtils.urlExists(urlPng, true)){
								ImageFile pngImage = ImageFile.NewInstance(urlPng, size, imageMetaData);
								MediaRepresentation pngRepresentation = MediaRepresentation.NewInstance(mimeTypePng, suffixPng);
								pngRepresentation.addRepresentationPart(pngImage);
								media.addRepresentation(pngRepresentation);
							}else{
								fileExists = true;
								int pngCount = 0;
								while (fileExists){
									pngCount++;
									urlPng = urlPath + "png/" + nameFact + "00" + pngCount + "." + suffixPng;
									filePng = strFolderPath + "png" + sep + nameFact + "00" + pngCount + "." + suffixPng;
									if (imageMetaData.readFrom(new File(filePng))){
									//if (CdmUtils.urlExists(urlPng, true)){
										ImageFile pngImage = ImageFile.NewInstance(urlPng, size, imageMetaData);
										MediaRepresentation pngRepresentation = MediaRepresentation.NewInstance(mimeTypeJpeg, suffixPng);
										pngRepresentation.addRepresentationPart(pngImage);
										media.addRepresentation(pngRepresentation);
									}else{
										fileExists = false;
									}
								}
							} //end png
							//all
							if (media.getRepresentations().size() > 0){
								ref.addMedia(media);
							}

						}//end NAME_FACT_PROTOLOGUE
					}else if (category.equalsIgnoreCase(NAME_FACT_ALSO_PUBLISHED_IN)){
						if (! nameFact.equals("")){
							String prefix = "Also published in: ";
							Annotation annotation = Annotation.NewDefaultLanguageInstance(nameFact);
							taxonNameBase.addAnnotation(annotation);
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
			logger.warn("ONLY " + border + " NAMEFACTS imported !!!" );
			logger.info("Names to save: " + taxonNameStore.size());
			nameService.saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeNameFacts ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	public static void main(String[] args) {
		String mimeTypeTif = "image/tiff";
		String mimeTypeJpeg = "image/jpeg";
		String mimeTypePng = "image/png";
		String suffixTif = "tif";
		String suffixJpg = "jpg";
		String suffixPng = "png";
		String sep = File.separator;
		Media media = Media.NewInstance();
		ImageMetaData imageMetaData = new ImageMetaData();
		Integer size = null;
		String nameFact = "Lactuca_adenophora";
	
		String urlPath = "http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/protolog/";
		String strFolderPath = sep + sep + "Bgbm11" + sep  + "Edit-WP6" + sep + "protolog" + sep;
		//tiff
		String urlTif = urlPath + "tif/" + nameFact + "." + suffixTif;
		String fileTif = strFolderPath + "tif" + sep + nameFact + "." + suffixTif;;
		//if (CdmUtils.urlExists(urlTif, true)){
		if (imageMetaData.readFrom(new File(fileTif))){
			
		}
		
		//jpeg
		boolean fileExists = true;
		int jpgCount = 0;
		
		while (fileExists){
			jpgCount++;
			String urlJpeg = urlPath + "jpeg/" + nameFact + "_p" + jpgCount + "." + suffixJpg;
			String fileJpeg = strFolderPath + "jpeg" + sep + nameFact + "_p" + jpgCount + "." + suffixJpg;
			
			if (imageMetaData.readFrom(new File(fileJpeg))){
			//if (CdmUtils.urlExists(urlJpeg, true)){
				ImageFile jpgImage = ImageFile.NewInstance(urlJpeg, size, imageMetaData);
				MediaRepresentation jpgRepresentation = MediaRepresentation.NewInstance(mimeTypeJpeg, suffixJpg);
				jpgRepresentation.addRepresentationPart(jpgImage);
				media.addRepresentation(jpgRepresentation);
			}else{
				fileExists = false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoNameFacts();
	}

}
