/**
 * 
 */
package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.INomenclaturalAuthorCacheStrategy;

/**
 * @author AM
 *
 */
@Entity
public abstract class TeamOrPersonBase<T extends TeamOrPersonBase> extends Agent implements INomenclaturalAuthor {
	static Logger logger = Logger.getLogger(TeamOrPersonBase.class);

	protected String nomenclaturalTitle;
	protected INomenclaturalAuthorCacheStrategy<T> cacheStrategy;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.agent.INomenclaturalAgent#getNomenclaturalTitle()
	 */
	public String getNomenclaturalTitle() {
		return nomenclaturalTitle;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.agent.INomenclaturalAgent#setNomenclaturalTitle(java.lang.String)
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle) {
		this.nomenclaturalTitle = nomenclaturalTitle;
	}

}
