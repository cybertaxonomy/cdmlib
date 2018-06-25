/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.ITermRepresentation_L10n;
import eu.etaxonomy.cdm.remote.l10n.TermRepresentation_L10n;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 *
 */
public class TermBaseBeanProcessor extends AbstractCdmBeanProcessor<TermBase> {

    public static final Logger logger = Logger.getLogger(TermBaseBeanProcessor.class);

    private static final List<String> IGNORE_LIST = Arrays.asList(new String[] {
            "representations",
            "inversRepresentations",
            "terms"
            });

    private boolean replaceRepresentations = false;

    public boolean isReplaceRepresentations() {
        return replaceRepresentations;
    }

    public void setReplaceRepresentations(boolean replace) {
        this.replaceRepresentations = replace;
    }

    @Override
    public List<String> getIgnorePropNames() {
        return IGNORE_LIST;
    }

    @Override
    public JSONObject processBeanSecondStep(TermBase term, JSONObject json,	JsonConfig jsonConfig) {

        // handle OrderedTermVocabulary
        if(OrderedTermVocabulary.class.isAssignableFrom(term.getClass())){
            OrderedTermVocabulary<?> otv = (OrderedTermVocabulary<?>)term;
            if(Hibernate.isInitialized(otv.getTerms())){
                json.element("terms", otv.getOrderedTerms(), jsonConfig);
            }
        } else if(TermVocabulary.class.isAssignableFrom(term.getClass())) {
            TermVocabulary<?> tv = (TermVocabulary<?>)term;
            if(Hibernate.isInitialized(tv.getTerms())){
                json.element("terms", tv.getTerms(), jsonConfig);
            }
        }

        ITermRepresentation_L10n representation_L10n = new TermRepresentation_L10n(term, false);
        handleL10nRepresentation(json, representation_L10n, false);
        if(!replaceRepresentations){
            json.element("representations", term.getRepresentations(), jsonConfig);
        }

        // add additional representation for RelationShipBase
        if(RelationshipTermBase.class.isAssignableFrom(term.getClass())){
            RelationshipTermBase<?> relTerm = (RelationshipTermBase<?>)term;
            ITermRepresentation_L10n inverseRepresentation_L10n = new TermRepresentation_L10n(relTerm, true);
            handleL10nRepresentation(json, inverseRepresentation_L10n, true);
            if(!replaceRepresentations){
                json.element("inverseRepresentations", relTerm.getInverseRepresentations(), jsonConfig);
            }
        }
        return json;
    }

    /**
     * @param json
     * @param representation_L10n
     */
    private void handleL10nRepresentation(JSONObject json, ITermRepresentation_L10n representation_L10n, boolean isInverse) {
        String baseLabel = isInverse? "inverseRepresentation_L10n" : "representation_L10n";
        if (representation_L10n.getLabel() != null) {
            json.element(baseLabel,representation_L10n.getLabel());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            json.element(baseLabel + "_abbreviatedLabel", representation_L10n.getAbbreviatedLabel());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            json.element(baseLabel + "_languageIso", representation_L10n.getLanguageIso());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            json.element(baseLabel + "_languageUuid", representation_L10n.getLanguageUuid());
        }
    }

}
