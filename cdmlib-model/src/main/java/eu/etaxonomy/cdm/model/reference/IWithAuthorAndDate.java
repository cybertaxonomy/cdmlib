/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * Interface representing all {@link Reference references} which have
 * a {@link Reference#getType() type} allowing an authorship and
 * a single publication date.
 * @author a.mueller
 * @since 20.11.2018
 *
 */
public interface IWithAuthorAndDate {

    /**
     * Returns the references author(s)
     */
    public TeamOrPersonBase getAuthorship();

    /**
     * Sets the references author(s)
     */
    public void setAuthorship(TeamOrPersonBase authorship);

    /**
     * Returns the date when the reference was published as a {@link TimePeriod}
     */
    public VerbatimTimePeriod getDatePublished();

    /**
     * Sets the date when the reference was published.
     * @see #getDatePublished()
     */
    public void setDatePublished(VerbatimTimePeriod datePublished);


    /**
     * Sets the date when the reference was published.
     * <BR>
     * Note: The time period will be internally converted to
     * a VerbatimTimePeriod so later changes to it will not
     * be reflected in the reference time period.
     * @return the new converted VerbatimTimePeriod
     * @param datePublished the not yet converted TimePeriod
     * @deprecated only for compatibility with older versions
     * but may create problems in certain contexts therefore
     * will be removed soon.
     */
    @Transient
    @Deprecated
    public VerbatimTimePeriod setDatePublished(TimePeriod datePublished);
}
