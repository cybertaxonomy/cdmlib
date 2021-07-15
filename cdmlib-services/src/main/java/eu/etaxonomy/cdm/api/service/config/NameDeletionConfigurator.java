/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.INameService;

/**
 * This class is used to configure name deletion.
 *
 * @see INameService#delete(eu.etaxonomy.cdm.model.name.TaxonName)
 *
 * @author a.mueller
 * @since 19.09.2011
 */
public class NameDeletionConfigurator  extends DeleteConfiguratorBase {

    private static final long serialVersionUID = 76414245247942488L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NameDeletionConfigurator.class);

	private boolean removeAllNameRelationships = false;
	private boolean ignoreIsBasionymFor = false;
	private boolean ignoreIsReplacedSynonymFor = false;
	private boolean ignoreHasBasionym = true;
	private boolean ignoreHasReplacedSynonym = true;
	private boolean ignoreIsOriginalSpellingFor = false;
	private boolean removeAllNameTypeDesignations = true;

	/**
	 * If true, all name relationships will be deleted prior to deleting the name.
	 * Exceptions will not be thrown due to existing name relationships.
	 * @return the removeAllNameRelationships
	 */
	public boolean isRemoveAllNameRelationships() {
		return removeAllNameRelationships;
	}
	public void setRemoveAllNameRelationships(boolean removeAllNameRelationships) {
		this.removeAllNameRelationships = removeAllNameRelationships;
	}

	/**
	 * If true, all basionym relationships in which this name is the basionym/original name will be removed.
	 * Exceptions will not be thrown due to existing basionym relationships.
	 * This value is neglected if {@link #isRemoveAllNameRelationships()} is true.
	 * @see #setIgnoreIsBasionymFor(boolean)
	 * @return the ignoreIsBasionymFor
	 */
	public boolean isIgnoreIsBasionymFor() {
		return ignoreIsBasionymFor;
	}
	/**
	 * @see #isIgnoreIsBasionymFor()
	 */
	public void setIgnoreIsBasionymFor(boolean ignoreIsBasionymFor) {
		this.ignoreIsBasionymFor = ignoreIsBasionymFor;
	}

	/**
     * If true, all basionym relationships in which this name is the basionym/original name will be removed.
     * Exceptions will not be thrown due to existing basionym relationships.
     * This value is neglected if {@link #isRemoveAllNameRelationships()} is true.
     * @see #setIgnoreIsBasionymFor(boolean)
     */
    public boolean isIgnoreIsOriginalSpellingFor() {
        return ignoreIsOriginalSpellingFor;
    }
    /**
     * @see #isIgnoreIsBasionymFor()
     */
    public void setIgnoreIsOriginalSpellingFor(boolean ignoreIsOriginalSpellingFor) {
        this.ignoreIsOriginalSpellingFor = ignoreIsOriginalSpellingFor;
    }

	/**
	 * If true, all name relationships in which this name is a replaced synonym will be removed.
	 * Exceptions will not be thrown due to existing isReplacedSynonym relationships.
	 * This value is neglected if {@link #isRemoveAllNameRelationships()} is true.
	 * @see #setIgnoreIsReplacedSynonymFor(boolean)
	 * @return the ignoreIsReplacedSynonymFor
	 */
	public boolean isIgnoreIsReplacedSynonymFor() {
		return ignoreIsReplacedSynonymFor;
	}
	/**
	 * @see #isIgnoreIsReplacedSynonymFor()
	 */
	public void setIgnoreIsReplacedSynonymFor(boolean ignoreIsReplacedSynonymFor) {
		this.ignoreIsReplacedSynonymFor = ignoreIsReplacedSynonymFor;
	}

	/**
	 * If true, all basionym relationships in which this name has a basionym/original name will
	 * be removed.<BR>
	 * Exceptions will not be thrown due to existing hasBasionym relationships.<BR>
	 * This value is neglected if {@link #isRemoveAllNameRelationships()} is true.<BR>
	 * Default value is <code>true</code>.<BR>
	 * @see #setIgnoreHasBasionym(boolean)
	 * @return the ignoreHasBasionym
	 */
	public boolean isIgnoreHasBasionym() {
		return ignoreHasBasionym;
	}
	/**
	 * @see #isIgnoreHasBasionym()
	 */
	public void setIgnoreHasBasionym(boolean ignoreHasBasionym) {
		this.ignoreHasBasionym = ignoreHasBasionym;
	}

	/**
	 * If true, all replaced synonym relationships in which this name has a
	 * replaced synonym will be removed.<BR>
	 * Exceptions will not be thrown due to existing hasReplacedSynonym relationships.<BR>
	 * This value is neglected if {@link #isRemoveAllNameRelationships()} is true.<BR>
	 * Default value is <code>true</code>.<BR>
	 * @see #setIgnoreHasBasionym(boolean)
	 * @return the ignoreHasReplacedSynonym
	 */
	public boolean isIgnoreHasReplacedSynonym() {
		return ignoreHasReplacedSynonym;
	}
	/**
	 * @see #isIgnoreHasReplacedSynonym()
	 */
	public void setIgnoreHasReplacedSynonym(boolean ignoreHasReplacedSynonym) {
		this.ignoreHasReplacedSynonym = ignoreHasReplacedSynonym;
	}

	public boolean isRemoveAllNameTypeDesignations() {
		return removeAllNameTypeDesignations;
	}
}