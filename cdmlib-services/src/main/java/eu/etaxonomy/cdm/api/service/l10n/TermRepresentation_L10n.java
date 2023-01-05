/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.l10n;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.ITermRepresentation_L10n;

/**
 * @author l.morris & a.kohlbecker
 * @since Feb 22, 2013
 */
public class TermRepresentation_L10n implements ITermRepresentation_L10n {

    private static final Logger logger = LogManager.getLogger();

    String label = null;
    String abbreviatedLabel = null;
    String text = null;
    String languageIso = null;
    String languageUuid = null;

    public TermRepresentation_L10n() {
    }

    public TermRepresentation_L10n(TermBase term, boolean useInverseRepresentation) {
        localize(term, useInverseRepresentation);
    }

    /**
     * Shortcut for {@link #from(TermBase, boolean)} with <code>useInverseRepresentation = false</code>.
     */
    public static TermRepresentation_L10n from(TermBase term) {
        return TermRepresentation_L10n.from(term, false);
    }

    public static TermRepresentation_L10n from(TermBase term, boolean useInverseRepresentation) {
        if(term == null) {
            return null;
        }
        return new TermRepresentation_L10n(term, useInverseRepresentation);
    }

    @Override
    public void localize(TermBase term, boolean useInverseRepresentation) {

        List<Language> languages = LocaleContext.getLanguages();

        if(useInverseRepresentation){
            RelationshipTermBase<?> relationshipTerm = (RelationshipTermBase<?>)term;
            if(Hibernate.isInitialized(relationshipTerm.getInverseRepresentations())){
                Representation representation = relationshipTerm.getPreferredInverseRepresentation(languages);
                setRepresentation(representation);
            } else {
                logger.debug("inverse representations of term not initialized  " + term.getUuid().toString());
            }

        } else {
            if(Hibernate.isInitialized(term.getRepresentations())){
                Representation representation = term.getPreferredRepresentation(languages);
                if (representation != null) {
                	setRepresentation(representation);
                }else {
                	label = term.getTitleCache();
                }
            } else {
                logger.debug("representations of term not initialized  " + term.getUuid().toString());
            }
        }
    }

    @Override
    public void localize(Set<Representation> representations) {
        DefinedTerm tmpTerm = DefinedTerm.NewInstance(TermType.Unknown, null, null, null);
        tmpTerm.getRepresentations().clear(); // removes the null representation added through the constructor
        tmpTerm.getRepresentations().addAll(representations);
        List<Language> languages = LocaleContext.getLanguages();
        Representation representation = tmpTerm.getPreferredRepresentation(languages);
        setRepresentation(representation);
    }

    private void setRepresentation(Representation representation) {
        if(representation != null){
            if(representation.getLabel() != null && representation.getLabel().length() != 0){
                label = representation.getLabel();
            } else if (representation.getText() != null && representation.getText().length() !=0) {
                label = representation.getText();
            } else {
                label = representation.getAbbreviatedLabel();
            }

            abbreviatedLabel = representation.getAbbreviatedLabel();

            text = representation.getText();

            Language lang = representation.getLanguage();
            if (lang != null){
                this.languageIso = lang.getIso639_2();
                if (this.languageIso == null){
                    this.languageIso = lang.getIso639_1();
                }
                this.languageUuid = lang.getUuid().toString();
            }
        }
    }

    @Override
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getAbbreviatedLabel() {
        return abbreviatedLabel;
    }
    public void setAbbreviatedLabel(String abbreviatedLabel) {
        this.abbreviatedLabel = abbreviatedLabel;
    }

    @Override
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getLanguageIso() {
        return languageIso;
    }
    public void setLanguageIso(String languageIso) {
        this.languageIso = languageIso;
    }

    @Override
    public String getLanguageUuid() {
        return languageUuid;
    }
    public void setLanguageUuid(String languageUuid) {
        this.languageUuid = languageUuid;
    }
}
