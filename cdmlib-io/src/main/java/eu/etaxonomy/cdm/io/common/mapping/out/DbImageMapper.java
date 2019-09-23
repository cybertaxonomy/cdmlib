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
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;

/**
 * TODO under construction (maybe needs to be a multi-attribute mapper as it maps to taxon column
 * and to (multiple) uri columns.
 * @author a.mueller
 * @since 06.02.2012
 */
public class DbImageMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>
            implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImageMapper.class);

	public static DbImageMapper NewInstance(String dbAttributeString){
		return new DbImageMapper(dbAttributeString, null);
	}

	protected DbImageMapper(String dbAttributeString, Object defaultValue) {
		super("multiLanguageText", dbAttributeString, defaultValue);
	}

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

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}