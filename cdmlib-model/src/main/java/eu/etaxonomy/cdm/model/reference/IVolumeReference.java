/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


 /**
 * Interface for all references that support set/getVolume().
 *
 * @author a.mueller
 * @since 24-Nov-2008 21:06:29
 */
public interface IVolumeReference extends IReference, IWithAuthorAndDate, IWithDoi {

	/**
	 * Returns the volume of a reference.
	 */
	public String getVolume();

	/**
	 * Sets the volume of the reference.
	 */
	public void setVolume(String volume);

}
