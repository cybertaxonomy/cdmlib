/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.*;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.common.Annotation;

import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;



/**
 * @author a.mueller
 *
 */
public class BerlinModelNameFactsIO {
	private static final Logger logger = Logger.getLogger(BerlinModelNameFactsIO.class);

	private static int modCount = 10;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for NameFacts not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap, MapWrapper<ReferenceBase> referenceMap){
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		Source source = bmiConfig.getSource();
		INameService nameService = cdmApp.getNameService();
		
		logger.info("start makeNameFacts ...");
		
		boolean delete = bmiConfig.isDeleteAll();

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
			int border = 20;
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
				//TaxonNameBase taxonNameBase = BotanicalName.NewInstance(null);
				
				if (taxonNameBase != null){
					//PROTOLOGUE
					if (category.equalsIgnoreCase(NAME_FACT_PROTOLOGUE)){
						ReferenceBase ref = (ReferenceBase)taxonNameBase.getNomenclaturalReference();
						//ReferenceBase ref = Book.NewInstance();
						
						if (ref != null){
						
							String mimeTypeTif = "image/tiff";
							String mimeTypeJpeg = "image/jpeg";
							String mimeTypePng = "image/png";
							String suffixTif = "tif";
							String suffixJpg = "jpg";
							String suffixPng = "png";
							Integer size = null;
							
							Media media = Media.NewInstance();
							//String urlPath = "http://wp5.e-taxonomy.eu/dataportal/cichorieae/media/";
							String urlPath = "file:\\\\Bgbm11\\Edit-WP6\\";
							//tiff
							String urlTif = urlPath + "protolog/tif/" + nameFact + "." + suffixTif;
							if (CdmUtils.urlExists(urlTif, true)){
								ImageFile tifImage = ImageFile.NewInstance(urlTif, size);
								MediaRepresentation tifRepresentation = MediaRepresentation.NewInstance(mimeTypeTif, suffixTif);
								tifRepresentation.addRepresentationPart(tifImage);
								media.addRepresentation(tifRepresentation);
							}
							//jpeg
							boolean fileExists = true;
							int jpgCount = 1;
							while (fileExists){
								String urlJpeg = urlPath + "protolog/jpeg/" + nameFact + "_p" + jpgCount++ + "." + suffixJpg;
								if (CdmUtils.urlExists(urlJpeg, true)){
									ImageFile jpgImage = ImageFile.NewInstance(urlJpeg, size);
									MediaRepresentation jpgRepresentation = MediaRepresentation.NewInstance(mimeTypeJpeg, suffixJpg);
									jpgRepresentation.addRepresentationPart(jpgImage);
									media.addRepresentation(jpgRepresentation);
								}else{
									fileExists = false;
								}
							}
							//png
							String urlPng = urlPath + "protolog/png/" + nameFact + "." + suffixPng;
							if (CdmUtils.urlExists(urlPng, true)){
								ImageFile pngImage = ImageFile.NewInstance(urlPng, size);
								MediaRepresentation pngRepresentation = MediaRepresentation.NewInstance(mimeTypePng, suffixPng);
								pngRepresentation.addRepresentationPart(pngImage);
								media.addRepresentation(pngRepresentation);
							}else{
								fileExists = true;
								int pngCount = 1;
								while (fileExists){
									urlPng = urlPath + "protolog/png/" + nameFact + "00" + pngCount++ + "." + suffixPng;
									if (CdmUtils.urlExists(urlPng, true)){
										ImageFile pngImage = ImageFile.NewInstance(urlPng, size);
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

	
}
