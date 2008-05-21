package eu.etaxonomy.cdm.remote.dto.assembler;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaInstance;
import eu.etaxonomy.cdm.remote.dto.MediaTO;
import eu.etaxonomy.cdm.remote.dto.MediaSTO;

public class MediaAssembler extends AssemblerBase<MediaSTO, MediaTO, Media> {

	@Override
	MediaSTO getSTO(Media cdmObj) {
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
	MediaTO getTO(Media cdmObj) {
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
