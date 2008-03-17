package eu.etaxonomy.cdm.model.common;

public interface IRelated<T extends RelationshipBase> {
	public void addRelationship(T relation);
}
