/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.kohlbecker
 * @since Nov 25, 2020
 */
public class RepresentableEntityReference<T extends CdmBase> extends TypedEntityReference<T> {


    private static final long serialVersionUID = 1L;

    private List<TaggedText> taggedText;

    public RepresentableEntityReference(Class<T> type, UUID uuid, List<TaggedText> taggedText) {
        super(type, uuid, TaggedCacheHelper.createString(taggedText));
        this.taggedText = taggedText;
    }

    public List<TaggedText> getTaggedText() {
        return taggedText;
    }

}
