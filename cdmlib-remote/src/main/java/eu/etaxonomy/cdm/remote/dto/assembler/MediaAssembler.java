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
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.media.AudioFile;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MovieFile;
import eu.etaxonomy.cdm.remote.dto.MediaRepresentationSTO;
import eu.etaxonomy.cdm.remote.dto.MediaTO;
import eu.etaxonomy.cdm.remote.dto.MediaSTO;

@Component
public class MediaAssembler extends AssemblerBase<MediaSTO, MediaTO, Media> {

	@Override
	MediaSTO getSTO(Media media, Enumeration<Locale> locales) {
		MediaSTO mediaSTO = null;
		if (media !=null){
			mediaSTO = new MediaSTO();
			setVersionableEntity(media, mediaSTO);
			//TODO: pick right language!
			mediaSTO.setTitle(media.getTitle().toString());
			mediaSTO.setRepresentations(getMediaRepresentationSTOs(media.getRepresentations()));
		}
		return mediaSTO;
	}

	@Override
	MediaTO getTO(Media media, Enumeration<Locale> locales) {
		MediaTO mediaTO = null;
		if (media !=null){
			mediaTO = new MediaTO();
			setVersionableEntity(media, mediaTO);
			//TODO: pick right language!
			mediaTO.setTitle(media.getTitle().toString());
			//TODO: pick right language!
			mediaTO.setDescription(media.getDescription().toString());
			mediaTO.setRepresentations(getMediaRepresentationSTOs(media.getRepresentations()));
		}
		
		return mediaTO;
	}
	
	public Set<MediaRepresentationSTO> getMediaRepresentationSTOs(Set<MediaRepresentation> representations){
		Set<MediaRepresentationSTO> representationSTOs = new HashSet<MediaRepresentationSTO>(representations.size()); 
		
		for (MediaRepresentation mediaRepr : representations){
			MediaRepresentationSTO reprSTO = new MediaRepresentationSTO();
			reprSTO.setMimeType(mediaRepr.getMimeType());
			reprSTO.setSuffix(mediaRepr.getSuffix());
			reprSTO.setUuid(mediaRepr.getUuid().toString());
			Integer width = null;
			Integer height = null;
			Integer duration = null;
			for(MediaRepresentationPart part : mediaRepr.getParts()){

				if(part instanceof AudioFile){
					AudioFile audio = (AudioFile)part;
					duration = audio.getDuration();
				} else 	if(part instanceof MovieFile){
					MovieFile movie = (MovieFile)part;
					duration = movie.getDuration();
					height = movie.getHeight();
					width = movie.getWidth();
				} else if(part instanceof ImageFile){
					ImageFile image = (MovieFile)part;
					height = image.getHeight();
					width = image.getWidth();
				}
				reprSTO.addRepresenationPart(part.getUuid().toString(), part.getUri().toString(), height, width, duration);
			}
		}
		return representationSTOs;
	}
}
