package eu.etaxonomy.cdm.model.common;

import java.util.Set;

 
public interface ISourceable {

	public abstract Set<OriginalSource> getSources();

	public abstract void addSource(OriginalSource source);

	public abstract void removeSource(OriginalSource source);

}