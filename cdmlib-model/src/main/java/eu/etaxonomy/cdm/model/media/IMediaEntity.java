/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.util.Set;

/**
 * If a class is implementing this interface a set of <code>media</code> may be added
 * to an instance of this class.
 *
 * @author a.mueller
 * @created 07.07.2008
 */
public interface IMediaEntity extends IMediaDocumented{

	/**
	 * Gets all media belonging to this object
	 * @return
	 */
	@Override
    public Set<Media> getMedia();

	/**
	 * Adds a media to this object
	 * @param media
	 */
	public void addMedia(Media media);

	/**
	 * Removes a media from this object
	 * @param media
	 */
	public void removeMedia(Media media);

}
