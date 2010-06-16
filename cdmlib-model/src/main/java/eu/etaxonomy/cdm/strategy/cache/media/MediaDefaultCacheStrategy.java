/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.media;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.Transient;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class MediaDefaultCacheStrategy extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<Media> {

	final static UUID uuid = UUID.fromString("0517ae48-597d-4d6b-9f18-8752d689720d");
	
	@Override
	protected UUID getUuid() {
		return uuid;
	}
	
	
	public String getTitleCache(Media media) {
		media = (Media) HibernateProxyHelper.deproxy(media, Media.class);
		
		List<Language> languages = Arrays.asList(new Language[]{Language.DEFAULT()});
		LanguageString languageString = MultilanguageTextHelper.getPreferredLanguageString(media.getAllTitles(), languages);
		return languageString != null ? languageString.getText() : null;
	
	}
	
	public String getTitleCacheByLanguage(Media media, Language lang) {
		List<Language> languages = Arrays.asList(new Language[]{lang});
		LanguageString languageString = MultilanguageTextHelper.getPreferredLanguageString(media.getAllTitles(), languages);
		return languageString != null ? languageString.getText() : null;
	
	}


	public static MediaDefaultCacheStrategy NewInstance() {
		return new MediaDefaultCacheStrategy();
	}

	
}
