package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;

public interface IAuditEventService {
	
	 /**
     * Returns a paged sublist of AuditEvent instances stored in the database.
	 * A maximum of 'limit' objects are returned, starting at object with index 'start'.
	 * 
     * @param limit the maximum number of entities returned (can be null to return all entities)
     * @param start
     * @param sort Whether the list is sorted going forward in time (AuditEventSort.FORWARDS) 
     * or backwards (AuditEventSort.BACKWARDS)
     * @return a Pager containing AuditEvent instances
     */  
	public Pager<AuditEvent> list(Integer limit, Integer start,AuditEventSort sort);
	
	/**
	 * Find the AuditEvent with an identifier equal to the parameter
	 * 
	 * @param id
	 * @return an AuditEvent, or null if there is no AuditEvent with that identifier
	 */
    public AuditEvent findById(Integer Id);
    
    /**
     * Find the AuditEvent with a uuid (surrogate key) equal to the uuid supplied
     * 
     * @param uuid
     * @return an AuditEvent, or null if there is no AuditEvent with a uuid which matches
     */
    public AuditEvent findByUuid(UUID uuid);
    
    /**
     * Checks whether an AuditEvent with a matching uuid exists in the database
     * 
     * @param uuid
     * @return true if an AuditEvent with a matching uuid exists in the database, false otherwise
     */
    public boolean exists(UUID uuid);
    
    /**
     * Returns the AuditEvent immediately proceeding the audit event passed as an argument
     * 
     * @param auditEvent
     * @return the AuditEvent immediately proceeding, or null if the AuditEvent passed is 
     * the most recent event
     */
    public AuditEvent getNextAuditEvent(AuditEvent auditEvent);
    
    /**
     * Returns the AuditEvent immediately preceding the audit event passed as an argument
     * 
     * @param auditEvent
     * @return the AuditEvent immediately preceding, or null if the AuditEvent passed is 
     * the first event in the database
     */
    public AuditEvent getPreviousAuditEvent(AuditEvent auditEvent);
}
