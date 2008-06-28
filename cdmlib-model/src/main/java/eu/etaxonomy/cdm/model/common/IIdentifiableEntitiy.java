package eu.etaxonomy.cdm.model.common;

import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.media.Rights;

import org.apache.log4j.Logger;

public interface IIdentifiableEntitiy<T extends IdentifiableEntity> {

	public abstract String getLsid();

	public abstract void setLsid(String lsid);

	public abstract String generateTitle();

	//@Index(name="titleCacheIndex")
	public abstract String getTitleCache();

	public abstract void setTitleCache(String titleCache);

	public abstract void setTitleCache(String titleCache, boolean protectCache);

	@ManyToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract Set<Rights> getRights();

	public abstract void addRights(Rights right);

	public abstract void removeRights(Rights right);

	@OneToMany
	//(mappedBy="extendedObj")
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract Set<Extension> getExtensions();

	public abstract void addExtension(Extension extension);

	public abstract void removeExtension(Extension extension);

	public abstract boolean isProtectedTitleCache();

	public abstract void setProtectedTitleCache(boolean protectedTitleCache);

	@OneToMany
	//(mappedBy="sourcedObj")		
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract Set<OriginalSource> getSources();

	public abstract void addSource(OriginalSource source);

	public abstract void removeSource(OriginalSource source);

	/**
	 * Overrides {@link eu.etaxonomy.cdm.model.common.CdmBase#toString()}.
	 * This returns an String that identifies the object well without beeing necessarily unique.
	 * Specification: This method should never call other object' methods so it can be well used for debugging 
	 * without problems like lazy loading, unreal states etc.
	 * Note: If overriding this method's javadoc always copy or link the above requirement. 
	 * If not overwritten by a subclass method returns the class, id and uuid as a string for any CDM object. 
	 * For example: Taxon#13<b5938a98-c1de-4dda-b040-d5cc5bfb3bc0>
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

}