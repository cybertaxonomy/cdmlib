/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.Collection;
import java.util.List;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * @author a.kohlbecker
 * @since Jun 24, 2015
 *
 */
public interface ICondensedDistributionComposer {

    /**
     * @param filteredDistributions
     * @param hideMarkedAreas
     * @param langs
     * @return
     */
    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> langs);

}
