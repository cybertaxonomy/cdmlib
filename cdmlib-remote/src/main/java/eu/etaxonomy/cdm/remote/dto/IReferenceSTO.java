/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.Set;
import java.util.UUID;

public interface IReferenceSTO extends IBaseSTO{

	public abstract String getAuthorship();

	public abstract void setAuthorship(String authorship);

	public abstract String getFullCitation();

	public abstract void setFullCitation(String fullCitation);

	public abstract Set<MediaSTO> getMedia();

	public abstract void addMedia(MediaSTO media);

}