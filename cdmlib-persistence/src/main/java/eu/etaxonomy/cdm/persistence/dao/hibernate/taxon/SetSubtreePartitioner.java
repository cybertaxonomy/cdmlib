/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @since 03.05.2018
 *
 */
public class SetSubtreePartitioner {

    public <T extends TaxonBase<?>>  void execute(String queryStr, IProgressMonitor monitor){
        Set<T> result = new HashSet<>();
//        Query query = getSession().createQuery(String queryStr, IProgressMonitor monitor);

    }
}
