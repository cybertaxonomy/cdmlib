/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;

/**
 * This class adds a Media to a DescriptionElementBase
 * @author a.mueller
 * @since 12.03.2010
 */
public class DbImportMediaMapper extends DbImportMultiAttributeMapperBase<DescriptionElementBase, DbImportStateBase<?,?>> {
	private static final Logger logger = Logger.getLogger(DbImportMediaMapper.class);

//********************************** FACTORY ***************************************

	public static DbImportMediaMapper NewInstance(String dbUriAttribute){
		return new DbImportMediaMapper(dbUriAttribute, null);
	}


	public static DbImportMediaMapper NewInstance(String dbFirstUriAttribute, String dbSecondUriAttribute){
		return new DbImportMediaMapper(dbFirstUriAttribute, dbSecondUriAttribute);
	}


//********************************** VARIABLES ***************************************
	private String dbFirstUriAttribute;
	private String dbSecondUriAttribute;


//********************************** METHODS ***************************************


	public DbImportMediaMapper(String dbFirstUriAttribute, String dbSecondUriAttribute) {
		super();
		this.dbFirstUriAttribute = dbFirstUriAttribute;
		this.dbSecondUriAttribute = dbSecondUriAttribute;
	}

	@Override
	public DescriptionElementBase invoke(ResultSet rs, DescriptionElementBase element) throws SQLException {
		String uri1String = getStringDbValue(rs, dbFirstUriAttribute);
		String uri2String = getStringDbValue(rs, dbSecondUriAttribute);
		Integer size = null;
		String mimeType = null;
		String suffix = null;

		URI uri1 = null;
		try {
			uri1 = new URI(uri1String);
		} catch (URISyntaxException e) {
			String warning = "URISyntaxException when trying to convert first uri string: " + uri1String;
			logger.error(warning);
		}
		URI uri2 = null;
		try {
			uri2 = new URI(uri2String);
		} catch (URISyntaxException e) {
			String warning = "URISyntaxException when trying to convert first uri string: " + uri1String;
			logger.error(warning);
		}
		Media media = Media.NewInstance(uri1, size, mimeType, suffix);
		if (media != null){
			MediaRepresentation secondRepresentation = MediaRepresentation.NewInstance(mimeType, suffix, uri2, size, null);
			media.addRepresentation(secondRepresentation);
		}else{
			media = Media.NewInstance(uri2, size, mimeType, suffix);
		}
		element.addMedia(media);
		return element;
	}


}
