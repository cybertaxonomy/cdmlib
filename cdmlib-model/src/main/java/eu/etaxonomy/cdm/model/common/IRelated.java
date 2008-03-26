package eu.etaxonomy.cdm.model.common;

/**
 * PLEASE LOOK AT NameRelationship and TaxonRelationship
 * @author m.doering
 *
 * @param <T>
 */
public interface IRelated<T extends RelationshipBase> {
	public void addRelationship(T relation);
}
