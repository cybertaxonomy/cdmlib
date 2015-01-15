/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultCrud;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * A {@link Runnable} performing Level-2 validation of a JPA entity
 *
 * @author ayco_holleman
 *
 */
public class Level2ValidationTask extends EntityValidationTaskBase {

    public Level2ValidationTask(CdmBase entity, IEntityValidationResultCrud dao) {
        super(entity, dao, Level2.class);
    }

    public Level2ValidationTask(CdmBase entity, CRUDEventType crudEventType, IEntityValidationResultCrud dao) {
        super(entity, crudEventType, dao, Level2.class);
    }

}
