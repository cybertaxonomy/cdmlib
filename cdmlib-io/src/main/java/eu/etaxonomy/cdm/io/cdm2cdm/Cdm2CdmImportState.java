/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Import state base class for migrating data from one CDM instance to another.
 *
 * @author a.mueller
 * @since 17.08.2019
 */
public class Cdm2CdmImportState
        extends ImportStateBase<Cdm2CdmImportConfigurator,Cdm2CdmImportBase>{

    private ICdmRepository sourceRepository;

    private Map<UUID, CdmBase> permanentCache = new HashMap<>();

//************************ CONSTRUCTOR **********************************/

    protected Cdm2CdmImportState(Cdm2CdmImportConfigurator config) {
        super(config);
    }

// ************************* GETTER / SETTER ****************************/

    public ICdmRepository getSourceRepository() {
        return sourceRepository;
    }
    public void setSourceRepository(ICdmRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public CdmBase getPermanent(UUID uuid) {
        return permanentCache.get(uuid);
    }
    public void putPermanent(UUID uuid, CdmBase cdmBase) {
        permanentCache.put(uuid, cdmBase);
    }
}
