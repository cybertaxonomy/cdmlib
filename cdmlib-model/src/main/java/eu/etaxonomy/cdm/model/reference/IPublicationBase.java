/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

/**
 * This base interface represents all different kind of published
 * {@link IReference references} which constitute a physical
 * or virtual unit. A reference is a published
 * reference if it can be consulted by the general public.
 */
public interface IPublicationBase extends IReference {

	/**
	 * Returns the publisher string for this reference
	 */
	public String getPublisher() ;

	/**
	 * Sets the publisher string for this reference
	 * @param publisher
	 */
	public void setPublisher(String publisher) ;

	/**
     * Returns the 2nd publisher string for this reference
     */
    public String getPublisher2() ;

    /**
     * Sets the 2nd publisher string for this reference
     */
    public void setPublisher2(String publisher2) ;

	/**
	 * Returns the string which represents the place where this
	 * reference was published
	 */
	public String getPlacePublished() ;

	/**
	 * Sets the string which represents the place where this
	 * reference was published
	 */
	public void setPlacePublished(String placePublished) ;


    /**
     * Returns the string which represents the 2nd place where this
     * reference was published
     */
    public String getPlacePublished2() ;

    /**
     * Sets the string which represents the 2nd place where this
     * reference was published
     */
    public void setPlacePublished2(String placePublished) ;

	/**
	 * Sets the publisher and the publication place
	 *
	 * @see #setPublisher(String)
	 * @see #setPlacePublished(String)
	 *
	 * @param publisher the publisher to set
	 * @param placePublished the place where the publication was published
	 */
	public void setPublisher(String publisher, String placePublished);

	/**
     * Sets the 2nd publisher and the 2nd publication place
     *
     * @see #setPublisher(String)
     * @see #setPlacePublished(String)
     *
     * @param publisher2 the publisher to set
     * @param placePublished2 the place where the publication was published
     */
    public void setPublisher2(String publisher2, String placePublished2);
}