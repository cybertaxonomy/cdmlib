package eu.etaxonomy.cdm.model.description;

import java.util.List;

public interface IPolytomousKeyPart {

	public PolytomousKey getKey();

	public void setKey(PolytomousKey key);

	public List<IPolytomousKeyPart> getChildren();

}