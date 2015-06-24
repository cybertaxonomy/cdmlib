// $Id$
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
import java.util.Map;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * @author a.kohlbecker
 * @date Jun 24, 2015
 *
 */
public class EuroPlusMedCondensedDistributionComposer implements ICondensedDistributionComposer {

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<PresenceAbsenceTerm, String> createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> langs) {
        // TODO Auto-generated method stub
        return null;
    }

}
