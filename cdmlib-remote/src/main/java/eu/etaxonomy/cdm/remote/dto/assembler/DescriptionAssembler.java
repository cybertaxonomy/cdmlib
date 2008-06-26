/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.remote.dto.BaseSTO;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.DescriptionElementSTO;
import eu.etaxonomy.cdm.remote.dto.DescriptionTO;
import eu.etaxonomy.cdm.remote.dto.IdentifiedString;

/**
 * @author a.kohlbecker
 * @created 24.06.2008
 * @version 1.0
 */
@Component
public class DescriptionAssembler extends AssemblerBase<BaseSTO, DescriptionTO, DescriptionBase> {
	private static Logger logger = Logger.getLogger(DescriptionAssembler.class);
	
	@Autowired
	private MediaAssembler mediaAssembler;
	@Autowired
	private IDefinedTermDao languageDao;
	@Autowired
	private LocalisedTermAssembler localisedTermAssembler;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	BaseSTO getSTO(DescriptionBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public DescriptionTO getTO(DescriptionBase descriptionBase,
			Enumeration<Locale> locales) {
		DescriptionTO to = new DescriptionTO();
		to.setUuid(descriptionBase.getUuid().toString());
		to.setCreated(descriptionBase.getCreated());
		to.setCreatedBy(descriptionBase.getCreatedBy());
		//TODO to.setLabel(label);
		for(DescriptionElementBase descriptionElementBase :  descriptionBase.getElements()){
			to.addElement(getDescriptionElementSTO(descriptionElementBase, locales));
		}
		//TODO implement sources and scopes
		return to;
	}
	
	/**
	 * @param descriptionBaseSet
	 * @param locales
	 * @return
	 */
	public Set<DescriptionTO> getTOs(Set<? extends DescriptionBase> descriptionBaseSet,
			Enumeration<Locale> locales){
		HashSet<DescriptionTO> tos = new HashSet<DescriptionTO>(descriptionBaseSet.size());
		for (DescriptionBase descriptionBase : descriptionBaseSet) {
			tos.add(getTO(descriptionBase, locales));
		}
		return tos;
	}

	/**
	 * @param descriptionElementBase
	 * @param locales
	 * @return
	 */
	public DescriptionElementSTO getDescriptionElementSTO(
			DescriptionElementBase descriptionElementBase,
			Enumeration<Locale> locales) {
		
		List<Language> languages = languageDao.getLanguagesByLocale(locales);

		DescriptionElementSTO sto = new DescriptionElementSTO();
		sto.setUuid(descriptionElementBase.getUuid().toString());
		
		if(descriptionElementBase.getType() != null){
			Feature type = descriptionElementBase.getType();
			sto.setType(localisedTermAssembler.getSTO(type, locales));
		}
		// media
		for(Media media : descriptionElementBase.getMedia()){
			sto.addMedia(mediaAssembler.getSTO(media, locales));			
		}
		// TextData specific
		if(descriptionElementBase instanceof TextData){
			TextData textdata = (TextData)descriptionElementBase;
			//TODO extract method for finding text by preferred languages
			for (Language language : languages) {
				String text = textdata.getText(language);
				if(text != null){
					sto.setLanguage(language.getLabel());
					sto.setDescription(text);
					break;
				}
			}
			// HACK:
			Language language = textdata.getMultilanguageText().keySet().iterator().next();
			String text = textdata.getMultilanguageText().get(language).getText();
			sto.setLanguage(language.getLabel());
			sto.setDescription(text);
		}
		return sto;
	}

	
}
