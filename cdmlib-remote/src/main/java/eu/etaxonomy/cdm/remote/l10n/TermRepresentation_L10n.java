// $Id$
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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.remote.json.processor.bean.TermBaseBeanProcessor;

/**
 * @author l.morris & a.kohlbecker
 * @date Feb 22, 2013
 *
 */
public class TermRepresentation_L10n {
	
	public static final Logger logger = Logger.getLogger(TermRepresentation_L10n.class);

	String label = null;
	String abbreviatedLabel = null;
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the abbreviatedLabel
	 */
	public String getAbbreviatedLabel() {
		return abbreviatedLabel;
	}

	/**
	 * @param abbreviatedLabel the abbreviatedLabel to set
	 */
	public void setAbbreviatedLabel(String abbreviatedLabel) {
		this.abbreviatedLabel = abbreviatedLabel;
	}

	public TermRepresentation_L10n(TermBase term) {
		
		List<Language> languages = LocaleContext.getLanguages();
		
		if(Hibernate.isInitialized(term.getRepresentations())){
            Representation representation = term.getPreferredRepresentation(languages);
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
        } else {
            logger.debug("representations of term not initialized  " + term.getUuid().toString());
        }
		
	}

}
