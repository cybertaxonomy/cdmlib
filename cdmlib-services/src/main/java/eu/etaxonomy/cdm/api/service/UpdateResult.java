/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;

/**
 * This class represents the result of an update action.
 *
 * @author k.luther
 * @since 11.02.2015
 */
public class UpdateResult implements Serializable{

    private static final long serialVersionUID = -7040027587709706700L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(UpdateResult.class);

    private Status status = Status.OK;

    @SuppressWarnings("unchecked")
    private final Collection<Exception> exceptions = new CircularFifoBuffer(10);

    private Set<CdmBase> updatedObjects = new HashSet<>();

    private final Set<CdmEntityIdentifier> updatedCdmIds = new HashSet<>();

    private final Set<CdmBase> unchangedObjects = new HashSet<>();

    private CdmBase cdmEntity;

    private Map<Class<? extends CdmBase>, Set<UUID>> insertedUuids = new HashMap<>();
    private Map<Class<? extends CdmBase>, Set<UUID>> updatedUuids = new HashMap<>();

    public enum Status {
        OK(0),
        ABORT(1),
        ERROR(3),
        ;

        protected Integer severity;
        private Status(int severity){
            this.severity = severity;
        }

        public int compareSeverity(Status other){
            return this.severity.compareTo(other.severity);
        }
    }

    //***************************** GETTER /SETTER /ADDER *************************/

    /**
     * The resulting status of an update action.
     *
     * @see UpdateStatus
     * @return
     */
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * The highest exception that occurred during delete (if any).
     */
    public Collection<Exception> getExceptions() {
        return exceptions;
    }
    public void addException(Exception exception) {
        this.exceptions.add(exception);
    }
    public void addExceptions(Collection<Exception> exceptions) {
        this.exceptions.addAll(exceptions);
    }

    //inserted object UUIDs
    public Map<Class<? extends CdmBase>, Set<UUID>> getInsertedUuids(){
        return this.insertedUuids;
    }
    public void addInsertedUuid(CdmBase cdmBase) {
        Class<? extends CdmBase> clazz = CdmBase.deproxy(cdmBase).getClass();
        initClassRecord(insertedUuids, clazz);
        this.insertedUuids.get(clazz).add(cdmBase.getUuid());
    }
    public Set<UUID> getInsertedUuids(Class<? extends CdmBase> clazz){
        return byMapKey(this.insertedUuids, clazz);
    }

    //updated object UUIDs
    public Map<Class<? extends CdmBase>, Set<UUID>> getUpdatedUuids(){
        return this.updatedUuids;
    }
    public void addUpdatedUuid(CdmBase cdmBase) {
        Class<? extends CdmBase> clazz = CdmBase.deproxy(cdmBase).getClass();
        initClassRecord(updatedUuids, clazz);
        this.updatedUuids.get(clazz).add(cdmBase.getUuid());
    }
    public Set<UUID> getUpdatedUuids(Class<? extends CdmBase> clazz){
        return byMapKey(this.updatedUuids, clazz);
    }

    private void initClassRecord(Map<Class<? extends CdmBase>, Set<UUID>> map, Class<? extends CdmBase> clazz){
        if (map.get(clazz) == null){
            map.put(clazz, new HashSet<>());
        }
    }
    private Set<UUID> byMapKey(Map<Class<? extends CdmBase>, Set<UUID>> map, Class<? extends CdmBase> clazz){
        return map.get(clazz) == null ? new HashSet<>() : map.get(clazz);
    }

    //updated CDM id-s
    public Set<CdmEntityIdentifier> getUpdatedCdmIds() {
        return updatedCdmIds;
    }
    public void addUpdatedCdmId(CdmEntityIdentifier cdmId) {
        this.updatedCdmIds.add(cdmId);
    }
    public void addUpdatedCdmIds(Set<CdmEntityIdentifier> updatedCdmIds) {
        this.updatedCdmIds.addAll(updatedCdmIds);
    }
    public void addUpdatedCdmId(CdmBase updatedObject) {
        this.updatedCdmIds.add(CdmEntityIdentifier.NewInstance(updatedObject));
    }

    //updated objects
    public Set<CdmBase> getUpdatedObjects() {
        return updatedObjects;
    }
    public void addUpdatedObject(CdmBase relatedObject) {
            this.updatedObjects.add(relatedObject);
    }
    public void addUpdatedObjects(Set<? extends CdmBase> updatedObjects) {
        this.updatedObjects.addAll(updatedObjects);
    }


    //cdmEntity
    public void setCdmEntity(CdmBase cdmBase) {
        this.cdmEntity = cdmBase;
    }
    public CdmBase getCdmEntity(){
        return cdmEntity;
    }

    //unchanged objects
    public Set<CdmBase> getUnchangedObjects() {
        return unchangedObjects;
    }
    public void addUnchangedObjects(Set<? extends CdmBase> unchangedObjects) {
        this.unchangedObjects.addAll(unchangedObjects);
    }
    public void addUnChangedObject(CdmBase unchangedObject) {
        this.unchangedObjects.add(unchangedObject);
    }

    //****************** CONVENIENCE *********************************************/

    /**
     * Sets the status to {@link Status#ERROR} if not yet set to a more serious
     * status.
     */
    public void setError(){
        setMaxStatus(Status.ERROR);
    }

    /**
     * Sets the status to {@link Status#ABORT} if not yet set to a more serious
     * status.
     */
    public void setAbort(){
        setMaxStatus(Status.ABORT);
    }

    /**
     * Sets status to most severe status. If maxStatus is more severe then existing status
     * existing status is set to maxStatus. Otherwise nothing changes.
     * If minStatus is more severe then given status minStatus will be the new status.
     * @param maxStatus
     */
    public void setMaxStatus(Status maxStatus) {
        if (this.status.compareSeverity(maxStatus) < 0){
            this.status = maxStatus;
        }
    }

    public void includeResult(UpdateResult includedResult){
        includeResult(includedResult, false);
    }

    public void includeResult(UpdateResult includedResult, boolean excludeStatusAndException){

        if (!excludeStatusAndException){
            this.setMaxStatus(includedResult.getStatus());
            this.addExceptions(includedResult.getExceptions());
        }
        this.addUpdatedObjects(includedResult.getUpdatedObjects());
        this.addUpdatedCdmIds(includedResult.getUpdatedCdmIds());
        //also add cdm entity of included result to updated objects
        if(includedResult.getCdmEntity()!=null){
            this.getUpdatedObjects().add(includedResult.getCdmEntity());
        }
    }

    public boolean isOk(){
        return this.status == Status.OK;
    }

    public boolean isAbort(){
        return this.status == Status.ABORT;
    }

    public boolean isError(){
        return this.status == Status.ERROR;
    }

// *********************************** TO STRING ***************************************/
    @Override
    public String toString(){
        String separator = ", ";
        String exceptionString = "";
        for (Exception exception : exceptions) {
            exceptionString += exception.getLocalizedMessage()+separator;
        }
        if(exceptionString.endsWith(separator)){
            exceptionString = exceptionString.substring(0, exceptionString.length()-separator.length());
        }
        String updatedObjectString = toStringObjectsString(separator, updatedObjects);
        String unchangedObjectString = toStringObjectsString(separator, unchangedObjects);
        return "[UpdateResult]\n" +
            "Status: " + status.toString()+"\n" +
            "Exceptions: " + exceptionString+"\n" +
            "Updated objects: " + updatedObjectString+"\n" +
            "Updated objects IDs: " + toStringIdsString(separator, updatedCdmIds)+"\n" +
            "Unchanged objects: " + unchangedObjectString
            ;
    }

    private String toStringIdsString(String separator, Set<CdmEntityIdentifier> cdmIds) {
        String result = "";
        for (CdmEntityIdentifier id : cdmIds){
            result = CdmUtils.concat(separator, id.toString());
        }
        return result;
    }
    /**
     * Serializes the CdmBase collection
     */
    protected static String toStringObjectsString(String separator, Set<CdmBase> cdmBases) {
        String cdmBasesString = "";
        for (CdmBase cdmBase: cdmBases) {
            if(cdmBase instanceof IIdentifiableEntity){
                cdmBasesString += ((IIdentifiableEntity) cdmBase).getTitleCache()+separator;
            }
            else{
                cdmBasesString += cdmBase.toString()+separator;
            }
        }
        if(cdmBasesString.endsWith(separator)){
            cdmBasesString = cdmBasesString.substring(0, cdmBasesString.length()-separator.length());
        }
        return cdmBasesString;
    }
}
