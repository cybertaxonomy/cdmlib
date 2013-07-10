// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out.old;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportBase;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportConfigurator;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportState;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * The export class for Images.
 * Inserts into DataWarehouse database table <code>Image</code>.
 * @author e.-m.lee
 * @date 18.08.2010
 *
 */
@Component
public class PesiImageExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiImageExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = Reference.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Image";
	private static final String pluralString = "DescriptionElements";
	private static final String parentPluralString = "Taxa";
	
	public PesiImageExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(PesiExportState state) {
		boolean result = true;
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(PesiExportState state) {

		logger.error("*** Started Making " + pluralString + " ...");

		// Get the limit for objects to save within a single transaction.
//			int limit = state.getConfig().getLimitSave();
		int limit = 1000;

		// PESI: Clear the database table Image.
		doDelete(state);

		// PESI: Create the Images
		int count = 0;
		int taxonCount = 0;
		int pastCount = 0;
		TransactionStatus txStatus = null;
		List<TaxonBase> list = null;

		Connection connection = state.getConfig().getDestination().getConnection();
		// Start transaction
		txStatus = startTransaction(true);
		logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		while ((list = getTaxonService().list(null, limit, taxonCount, null, null)).size() > 0) {

			taxonCount += list.size();
			logger.error("Fetched " + list.size() + " " + parentPluralString + ".");
			
			logger.error("Check for Images...");
			for (TaxonBase taxonBase : list) {
				
				if (taxonBase.isInstanceOf(Taxon.class)) {
					
					Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);

					// Determine the TaxonDescriptions
					Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions();

					// Determine the DescriptionElements (Citations) for the current Taxon
					for (TaxonDescription taxonDescription : taxonDescriptions) {
						
						// Check whether this TaxonDescription contains images
						if (taxonDescription.isImageGallery()) {
							
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								if (descriptionElement.isInstanceOf(TextData.class)) {
									List<Media> media = descriptionElement.getMedia();
									
									for (Media image : media) {
										Set<MediaRepresentation> representations = image.getRepresentations();
										
										for (MediaRepresentation representation : representations) {
											List<MediaRepresentationPart> representationParts = representation.getParts();
											
											for (MediaRepresentationPart representationPart : representationParts) {
												URI mediaUri = representationPart.getUri();
												
												// Add image data
												String thumb = null;
												Integer taxonFk = state.getDbId(taxonBase.getName());
												
												if (taxonFk != null && mediaUri != null) {
													doCount(count++, modCount, pluralString);
													invokeImages(thumb, mediaUri, taxonFk, connection);
												}
											}
										}
										
									}
								}
							}
						
						}
						
					}
				}
				
			}
			logger.error("Exported " + (count - pastCount) + " " + pluralString + ".");

			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");
			logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		}
		if (list.size() == 0) {
			logger.error("No " + pluralString + " left to fetch.");
		}
		// Commit transaction
		commitTransaction(txStatus);
		logger.error("Committed transaction.");

		logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(true));
		
		return;
	}

	/**
	 * Inserts image data into the Image datawarehouse table.
	 * @param thumb
	 * @param url
	 * @param taxonFk
	 * @param connection
	 */
	private void invokeImages(String thumb, URI url, Integer taxonFk, Connection connection) {
		String imagesSql = "INSERT INTO Image (taxonFk, img_thumb, img_url) VALUES" +
				" (?, ?, ?)";
		try {
			PreparedStatement imagesStmt = connection.prepareStatement(imagesSql);
			
			if (taxonFk != null) {
				imagesStmt.setInt(1, taxonFk);
			} else {
				imagesStmt.setObject(1, null);
			}

			if (thumb != null) {
				imagesStmt.setString(2, thumb);
			} else {
				imagesStmt.setObject(2, null);
			}
			
			if (url != null) {
				imagesStmt.setString(3, url.toString());
			} else {
				imagesStmt.setObject(3, null);
			}
			
			imagesStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Image could not be created. TaxonFk: " + taxonFk + ", Thumb: " + thumb + ", URL: " + url);
			e.printStackTrace();
		}

	}

	/**
	 * Deletes all entries of database tables related to <code>AdditionalTaxonSource</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear AdditionalTaxonSource
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! ( state.getConfig().isDoImages());
	}

}
