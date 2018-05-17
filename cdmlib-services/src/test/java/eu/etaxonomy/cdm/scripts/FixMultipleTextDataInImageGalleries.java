/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * We decided by convention to have only ONE TextData element in an image gallery and that
 * all media objects should be attached to that element.
 *
 * Run this script to clean-up a database for which this convention was not satisfied.
 *
 * @author n.hoffmann
 * @since Jun 9, 2010
 * @version 1.0
 */
public class FixMultipleTextDataInImageGalleries {
	public static final Logger logger = Logger.getLogger(FixMultipleTextDataInImageGalleries.class);

	public static ICdmDataSource dataSource(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "<IP adress>";
		String cdmDB = "<database name>";
		String cdmUserName = "<user>";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	private static ICdmDataSource makeDestination(DatabaseTypeEnum dbType, String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName, pwd);
		ICdmDataSource destination;
		if(dbType.equals(DatabaseTypeEnum.MySQL)){
			destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
		} else if(dbType.equals(DatabaseTypeEnum.PostgreSQL)){
			destination = CdmDataSource.NewPostgreSQLInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
		} else {
			//TODO others
			throw new RuntimeException("Unsupported DatabaseType");
		}
		return destination;
	}

	/**
	 * @param args
	 * @throws TermNotFoundException
	 * @throws DataSourceNotFoundException
	 */
	public static void main(String[] args) throws DataSourceNotFoundException, TermNotFoundException {

		CdmApplicationController applicationController = CdmApplicationController.NewInstance(dataSource());

		ConversationHolder conversation = applicationController.NewConversation();
		conversation.startTransaction();

		IDescriptionService service = applicationController.getDescriptionService();

		// get all taxon descriptions
		List<TaxonDescription> result = service.list(TaxonDescription.class, null, null, null, null);

		int countTaxonDescriptions = 0;

		for (TaxonDescription description : result){
			// filter image galleries with more than one element
			if(description.isImageGallery() && description.getElements().size() > 1){
				countTaxonDescriptions++;

				logger.warn("Found image gallery with mulitple TextData: " + description.getElements().size());

				TextData newDescriptionElement = TextData.NewInstance(Feature.IMAGE());

				Set<DescriptionElementBase> elementsToRemove = new HashSet<DescriptionElementBase>();

				// consolidate media from all elements into a new element
				for(DescriptionElementBase element : description.getElements()){
					List<Media> medias = element.getMedia();

					for(Media media : medias){
						newDescriptionElement.addMedia(media);
						logger.warn("Consolidating media");
					}
					elementsToRemove.add(element);
				}

				// remove old elements
				for(DescriptionElementBase element : elementsToRemove){
					description.removeElement(element);
				}

				// add the new element
				description.addElement(newDescriptionElement);

			}
		}
		conversation.commit(false);

		logger.warn("Descriptions Processed: "  + countTaxonDescriptions);
	}
}
