// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author andreas kohlbecker
 * @date Sep 4, 2012
 *
 */
public class TaxonBaseVoter extends CdmPermissionVoter {

    public static final Logger logger = Logger.getLogger(TaxonBaseVoter.class);

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return TaxonBase.class;
    }

//    public TaxonBaseVoter(){
////        implicitPermissionClasses.add(new TaxonNodeVoter()); //FIXME springify !!
//    }

}
