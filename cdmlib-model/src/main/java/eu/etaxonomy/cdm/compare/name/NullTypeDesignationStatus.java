/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.name;

import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * NOTE: This class was moved here from TypeDesignationWorkingSet to allow to be used
 * in model compare classes.
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class NullTypeDesignationStatus extends TypeDesignationStatusBase<NullTypeDesignationStatus>{

    private static final long serialVersionUID = -2521279149219093956L;

    private static final NullTypeDesignationStatus singleton = new NullTypeDesignationStatus();

    public static final NullTypeDesignationStatus SINGLETON(){
        return singleton;
    }

    private NullTypeDesignationStatus(){super(TermType.NameTypeDesignationStatus);}  //just take any type

    @Override
    public void resetTerms() {}

    @Override
    protected void setDefaultTerms(TermVocabulary<NullTypeDesignationStatus> termVocabulary) {}

    @Override
    public boolean hasDesignationSource() {
        return false;
    }
}
