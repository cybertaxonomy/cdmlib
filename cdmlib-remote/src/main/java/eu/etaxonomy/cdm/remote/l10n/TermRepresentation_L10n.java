/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.l10n;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.persistence.dto.ITermRepresentation_L10n;

/**
 * @author l.morris & a.kohlbecker
 * @since Feb 22, 2013
 *
 */
public class TermRepresentation_L10n implements ITermRepresentation_L10n {

    public static final Logger logger = Logger.getLogger(TermRepresentation_L10n.class);

    String label = null;
    String abbreviatedLabel = null;

    public TermRepresentation_L10n() {
    }

    public TermRepresentation_L10n(TermBase term, boolean useInverseRepresentation) {
        localize(term, useInverseRepresentation);
    }

    @Override
    public void localize(TermBase term, boolean useInverseRepresentation) {

        List<Language> languages = LocaleContext.getLanguages();

        if(useInverseRepresentation){
            RelationshipTermBase<?> relationshipTerm = (RelationshipTermBase<?>)term;
            if(Hibernate.isInitialized(relationshipTerm.getInverseRepresentations())){
                Representation representation = relationshipTerm.getPreferredInverseRepresentation(languages);
                setRepresentations(representation);
            } else {
                logger.debug("inverse representations of term not initialized  " + term.getUuid().toString());
            }

        } else {
            if(Hibernate.isInitialized(term.getRepresentations())){
                Representation representation = term.getPreferredRepresentation(languages);
                setRepresentations(representation);
            } else {
                logger.debug("representations of term not initialized  " + term.getUuid().toString());
            }
        }
    }

    @Override
    public void localize(Set<Representation> representations) {
        DefinedTerm tmpTerm = DefinedTerm.NewInstance(TermType.Unknown, null, null, null);
        tmpTerm.getRepresentations().clear(); // removes the null representation added throught the constructor
        tmpTerm.getRepresentations().addAll(representations);
        List<Language> languages = LocaleContext.getLanguages();
        Representation representation = tmpTerm.getPreferredRepresentation(languages);
        setRepresentations(representation);
    }

    /**
     * @param representation
     */
    private void setRepresentations(Representation representation) {
        if(representation != null){
            if(representation.getLabel() != null && representation.getLabel().length() != 0){
                label = representation.getLabel();
            } else if (representation.getText() != null && representation.getText().length() !=0) {
                label = representation.getText();
            } else {
                label = representation.getAbbreviatedLabel();
            }

            abbreviatedLabel = representation.getAbbreviatedLabel();
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getAbbreviatedLabel() {
        return abbreviatedLabel;
    }

    /**
     * @param abbreviatedLabel the abbreviatedLabel to set
     */
    public void setAbbreviatedLabel(String abbreviatedLabel) {
        this.abbreviatedLabel = abbreviatedLabel;
    }



}
