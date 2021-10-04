/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.Optional;

import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Initializes the {@link Taxon#getTaxonNodes() taxonNodes}, which is required for
 * @{link {@link Taxon#isMisapplicationOnly()},
 * see https://dev.e-taxonomy.eu/redmine/issues/9797
 * <p>
 * <b>NOTE:</b> <code>Taxon.name</code> and <code>Taxon.sec</code> are initialized by the {@link TaxonNodeAutoInitializer}
 * <p>
 *
 *
 * @author a.kohlbecker
 * @since 30.07.2010
 */
public class TaxonAutoInitializer extends AutoPropertyInitializer<Taxon> {

    @Override
    public void initialize(Taxon bean) {
       beanInitializer.initializeInstance(bean.getTaxonNodes());
    }

    @Override
    public Optional<String> hibernateFetchJoin(Class<?> clazz, String beanAlias){

        String result = "";
        result += String.format(" LEFT JOIN FETCH %s.taxonNodes tn ", beanAlias);
        return Optional.of(result);
    }

}