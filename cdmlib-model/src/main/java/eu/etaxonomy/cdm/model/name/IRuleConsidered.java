/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

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
    String getRuleConsidered();

    /**
     * @see  #getRuleConsidered()
     */
    void setRuleConsidered(String ruleConsidered);

    /**
     * The {@link NomenclaturalCodeEdition code edition} for the {@link #getText() rule considered}.
     */
    NomenclaturalCodeEdition getCodeEdition();

    void setCodeEdition(NomenclaturalCodeEdition codeEdition);

}