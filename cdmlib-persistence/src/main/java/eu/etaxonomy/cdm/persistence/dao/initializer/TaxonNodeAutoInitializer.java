// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.kohlbecker
 * @date 30.07.2010
 *
 */
public class TaxonNodeAutoInitializer extends AutoPropertyInitializer<TaxonNode> {

    @Override
    public void initialize(TaxonNode bean) {
       beanInitializer.initializeInstance(bean.getTaxon().getName());
       beanInitializer.initializeInstance(bean.getTaxon().getSec());
    }

    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{

        String result = "";
        result += String.format(" LEFT JOIN FETCH %s.taxon taxon LEFT JOIN FETCH taxon.name LEFT JOIN FETCH taxon.sec", beanAlias);
        return result;
    }

}
