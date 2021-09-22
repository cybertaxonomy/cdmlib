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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    private Map<UUID, CdmBase> sessionCache = new HashMap<>();
    private Map<Class,Set<UUID>> existingObjects = new HashMap<>();
    private Set<CdmBase> toSave = new HashSet<>();

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

    //session cache
    public CdmBase getFromSessionCache(UUID uuid) {
        return sessionCache.get(uuid);
    }
    public void putToSessionCache(CdmBase cdmBase) {
        this.sessionCache.put(cdmBase.getUuid(), cdmBase);
    }
    public void clearSessionCache(){
        this.sessionCache.clear();
    }

    //existing objects
    public Set<UUID> getExistingObjects(Class clazz) {
        return existingObjects.get(clazz);
    }
    public void putExistingObjects(Class clazz, Set<UUID> uuids) {
        this.existingObjects.put(clazz, uuids);
    }

    //to save
    public Set<CdmBase> getToSave() {
        return toSave;
    }
    public void addToSave(CdmBase toSave) {
        this.toSave.add(toSave);
    }
    public void removeToSave(CdmBase toSave) {
        this.toSave.add(toSave);
    }
    public void clearToSave() {
        this.toSave.clear();
    }
}
