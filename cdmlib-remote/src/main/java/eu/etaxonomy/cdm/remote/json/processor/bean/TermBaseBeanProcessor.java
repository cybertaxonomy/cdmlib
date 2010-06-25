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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

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
		
		List<Language> languages = LocaleContext.getLanguages();
		
		Representation representation;
		if(Hibernate.isInitialized(term.getRepresentations())){
			representation = term.getPreferredRepresentation(languages);
			if(representation != null){
				if(representation.getText() != null && representation.getText().length() != 0){
					json.element("representation_L10n", representation.getText());
				} else if (representation.getLabel() != null && representation.getLabel().length() !=0) {
					json.element("representation_L10n", representation.getLabel());
				} 

				json.element("representation_L10n_abbreviated", representation.getAbbreviatedLabel());
				
			}
			if(!replaceRepresentations){
				json.element("representations", term.getRepresentations(), jsonConfig);
			}
		} else {
			logger.debug("representations of term not initialized  " + term.getUuid().toString());
		}
		
		// add additional representation for RelationShipBase
		if(RelationshipTermBase.class.isAssignableFrom(term.getClass())){
			RelationshipTermBase<?> relTerm = (RelationshipTermBase<?>)term;
			Representation inversRepresentation;
			if(Hibernate.isInitialized(relTerm.getInverseRepresentations())){
				inversRepresentation = relTerm.getPreferredInverseRepresentation(languages);
				if(inversRepresentation != null){
					if(inversRepresentation.getText() != null && inversRepresentation.getText().length() != 0){
						json.element("inverseRepresentation_L10n", inversRepresentation.getText());
					} else if (inversRepresentation.getLabel() != null && inversRepresentation.getLabel().length() !=0) {
						json.element("inverseRepresentation_L10n", inversRepresentation.getLabel());
					} else {
						json.element("inverseRepresentation_L10n", inversRepresentation.getAbbreviatedLabel());
					}
				}
				if(!replaceRepresentations){
					json.element("inverseRepresentations", relTerm.getRepresentations(), jsonConfig);
				}
			} else {
				logger.debug("inverseRepresentations of term not initialized  " + relTerm.getUuid().toString());
			}
		}
		return json;
	}
	
}
