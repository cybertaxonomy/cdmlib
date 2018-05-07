/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.description;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author a.mueller
 * @since 07.05.2018
 *
 */
public class DescriptiveDataSetDefaultCacheStrategy
            extends IdentifiableEntityDefaultCacheStrategy<DescriptiveDataSet> {

    private static final long serialVersionUID = 3307740888390159983L;

    final static UUID uuid = UUID.fromString("4018e36d-6c6e-4f9e-950c-66e928b51a22");

    @Override
    public String getTitleCache(DescriptiveDataSet descriptiveDataSet) {
        Representation preferredRepresentation = descriptiveDataSet.getPreferredRepresentation(Language.DEFAULT());

        if (preferredRepresentation != null){
            if (isNotBlank(preferredRepresentation.getLabel())){
                return preferredRepresentation.getLabel();
            }
        }
        return super.getTitleCache(descriptiveDataSet);
    }

    @Override
    protected UUID getUuid() {
        return uuid;
    }
}
