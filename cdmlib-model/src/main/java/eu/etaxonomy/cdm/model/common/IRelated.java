package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

/**
 * PLEASE LOOK AT NameRelationship and TaxonRelationship
 * @author m.doering
 *
 * @param <T>
 */
public interface IRelated<T extends RelationshipBase> {
	/**
	 * @param relation
	 */
	public void addRelationship(T relation);
	
	/**
	 * 
	 */
	public UUID getUuid();
}
