// $Id: TaxonBeanProcessor.java 5561 2009-04-07 12:25:33Z a.kohlbecker $
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

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.remote.l10n.TermRepresentation_L10n;

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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
     */
    @Override
    public List<String> getIgnorePropNames() {
        return IGNORE_LIST;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
     */
    @Override
    public JSONObject processBeanSecondStep(TermBase term, JSONObject json,	JsonConfig jsonConfig) {

        // handle OrderedTermVocabulary
        if(OrderedTermVocabulary.class.isAssignableFrom(term.getClass())){
            OrderedTermVocabulary otv = (OrderedTermVocabulary)term;
            if(Hibernate.isInitialized(otv.getTerms())){
                json.element("terms", otv.getOrderedTerms(), jsonConfig);
            }
        } else if(TermVocabulary.class.isAssignableFrom(term.getClass())) {
            TermVocabulary tv = (TermVocabulary)term;
            if(Hibernate.isInitialized(tv.getTerms())){
                json.element("terms", tv.getTerms(), jsonConfig);
            }
        }

        TermRepresentation_L10n representation_L10n = new TermRepresentation_L10n(term, false);
        if (representation_L10n.getLabel() != null) {
            json.element("representation_L10n",representation_L10n.getLabel());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            json.element("representation_L10n_abbreviatedLabel", representation_L10n.getAbbreviatedLabel());
        }
        if(!replaceRepresentations){
            json.element("representations", term.getRepresentations(), jsonConfig);
        }

        // add additional representation for RelationShipBase
        if(RelationshipTermBase.class.isAssignableFrom(term.getClass())){
            RelationshipTermBase<?> relTerm = (RelationshipTermBase<?>)term;
            TermRepresentation_L10n inverseRepresentation_L10n = new TermRepresentation_L10n(relTerm, true);
            if (inverseRepresentation_L10n.getLabel() != null) {
                json.element("inverseRepresentation_L10n", inverseRepresentation_L10n.getLabel());
            }
            if (inverseRepresentation_L10n.getAbbreviatedLabel() != null) {
                json.element("inverseRepresentation_L10n_abbreviatedLabel",  inverseRepresentation_L10n.getAbbreviatedLabel());
            }
            if(!replaceRepresentations){
                json.element("inverseRepresentations", relTerm.getRepresentations(), jsonConfig);
            }
        }
        return json;
    }

}
