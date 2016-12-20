/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.remote.l10n.TermRepresentation_L10n;

/**
 * @author a.kohlbecker
 * @date 24.03.2009
 *
 * @param <T>
 * @param <SERVICE>
 */
public abstract class AbstractListController<T extends CdmBase, SERVICE extends IService<T>> extends AbstractController<T, SERVICE> {

    /**
     * @param pager
     */
    protected static void localizeTerms(Pager<TermDto> pager) {
       for(TermDto termDto:  pager.getRecords()){
           termDto.localize(new TermRepresentation_L10n());
       }

    }


}
