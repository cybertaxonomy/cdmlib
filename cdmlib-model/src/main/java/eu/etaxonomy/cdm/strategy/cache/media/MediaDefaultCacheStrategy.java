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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class MediaDefaultCacheStrategy
        extends StrategyBase
        implements IMediaCacheStrategy {

    private static final long serialVersionUID = 7246846028810250751L;

    protected static final  Logger logger = Logger.getLogger(MediaDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("0517ae48-597d-4d6b-9f18-8752d689720d");


	public static MediaDefaultCacheStrategy NewInstance() {
		return new MediaDefaultCacheStrategy();
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(Media media) {
		media = CdmBase.deproxy(media);
		return getTitleCacheByLanguage(media, Language.DEFAULT());
	}

	public String getTitleCacheByLanguage(Media media, Language lang) {
		String result;
		List<Language> languages = Arrays.asList(new Language[]{lang});
		LanguageString languageString = MultilanguageTextHelper.getPreferredLanguageString(media.getAllTitles(), languages);
		result = (languageString != null ? languageString.getText() : "");

		//get first image uri
		if (isBlank(result)){
			for (MediaRepresentation mediaRepresentation : media.getRepresentations()){
				for (MediaRepresentationPart part : mediaRepresentation.getParts()){
					result = (part == null || part.getUri() == null) ? null : part.getUri().toString();
					if (isBlank(result)){
						continue;
					}
					@SuppressWarnings("null")
                    int lastSlashPos = result.lastIndexOf("/");
					if (lastSlashPos != -1 && lastSlashPos + 1 < result.length()){
						result = result.substring(lastSlashPos + 1);
					}
					break;
				}
				if (! isBlank(result)){
					break;
				}
			}
			if (isBlank(result)){
				result = "- empty media - <" + media.getUuid() + ">";
			}
		}
		return result;
	}



}
