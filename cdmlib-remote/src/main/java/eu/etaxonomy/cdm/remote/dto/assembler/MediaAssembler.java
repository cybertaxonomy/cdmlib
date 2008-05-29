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
import java.util.Locale;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaInstance;
import eu.etaxonomy.cdm.remote.dto.MediaTO;
import eu.etaxonomy.cdm.remote.dto.MediaSTO;

public class MediaAssembler extends AssemblerBase<MediaSTO, MediaTO, Media> {

	@Override
	MediaSTO getSTO(Media cdmObj, Enumeration<Locale> locales) {
		MediaSTO m = null;
		if (cdmObj !=null){
			m = new MediaSTO();
			setVersionableEntity(cdmObj, m);
			//TODO: pick right language!
			m.setTitle(cdmObj.getTitle().toString());
			for (MediaInstance mi : cdmObj.getInstances()){
				Integer width = null;
				Integer heigth = null;
				m.addInstance(mi.getUri(), mi.getMimeType(), heigth, width);
			}
		}
		return m;
	}

	@Override
	MediaTO getTO(Media cdmObj, Enumeration<Locale> locales) {
		MediaTO m = null;
		if (cdmObj !=null){
			m = new MediaTO();
			setVersionableEntity(cdmObj, m);
			//TODO: pick right language!
			m.setTitle(cdmObj.getTitle().toString());
			//TODO: pick right language!
			m.setDescription(cdmObj.getDescription().toString());
			for (MediaInstance mi : cdmObj.getInstances()){
				Integer width = null;
				Integer heigth = null;
				m.addInstance(mi.getUri(), mi.getMimeType(), heigth, width);
			}
		}
		return m;
	}
}
