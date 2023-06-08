/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;

/**
 * Interface for classes which have a rule considered field.
 *
 * @author a.mueller
 * @since 12.08.2019
 */
public interface IRuleConsidered {

    /**
     * Returns the nomenclatural code rule considered (that is the
     * article/note/recommendation in the nomenclatural code ruling
     * the  taxon name(s) of this nomenclatural status).
     * The considered rule gives the reason why the
     * {@link NomenclaturalStatusType nomenclatural status type} has been
     * assigned to the {@link TaxonName taxon name(s)}.
     *
     * @see #getCodeEdition()
     */
    public String getRuleConsidered();

    /**
     * @see  #getRuleConsidered()
     */
    public void setRuleConsidered(String ruleConsidered);

    /**
     * The {@link NomenclaturalCodeEdition code edition} for the {@link #getText() rule considered}.
     */
    public NomenclaturalCodeEdition getCodeEdition();

    public void setCodeEdition(NomenclaturalCodeEdition codeEdition);

    /**
     * Getter for code edition source to make it available in webservices.
     * This is necessary as enums are handled as text only in webservices
     * (see JSONObject._processValue( Object value, JsonConfig jsonConfig).
     * However, this could also be handled during webservices processing
     * in future so the method may be removed one day.
     */
    public IdentifiableSource getCodeEditionSource();

}