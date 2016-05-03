// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.net.URI;
import java.sql.Types;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;

/**
 * Maps text data to a database string field. (Only handles one language)
 * @author a.mueller
 * @created 06.02.2012
 * @version 1.0
 */
public class DbImageMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImageMapper.class);

	public static DbImageMapper NewInstance(Language language, String dbAttributeString){
		return new DbImageMapper(language, dbAttributeString, null);
	}

	public static DbImageMapper NewInstance(Language language, String dbAttributeString, String defaultValue){
		return new DbImageMapper(language, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbImageMapper(Language language, String dbAttributeString, Object defaultValue) {
		super("multiLanguageText", dbAttributeString, defaultValue);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(TextData.class)){
			List<Media> media = CdmBase.deproxy(cdmBase, TextData.class).getMedia();

			for (Media image : media) {
				Set<MediaRepresentation> representations = image.getRepresentations();

				for (MediaRepresentation representation : representations) {
					List<MediaRepresentationPart> representationParts = representation.getParts();

					for (MediaRepresentationPart representationPart : representationParts) {
						URI mediaUri = representationPart.getUri();

						// Add image data
						String thumb = null;
						Integer taxonFk = null; //getState().getDbId(taxonBase.getName());

						if (taxonFk != null && mediaUri != null) {
//							doCount(count++, modCount, pluralString);
//							invokeImages(thumb, mediaUri, taxonFk, connection);
						}
					}
				}

			}
		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type TextData, but was " + cdmBase.getClass());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
