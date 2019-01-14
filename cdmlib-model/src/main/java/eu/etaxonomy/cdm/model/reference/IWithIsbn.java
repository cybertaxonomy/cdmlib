/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

/**
 * Interface representing all {@link Reference references} which have
 * a {@link Reference#getType() type} allowing an ISBN.
 * @author a.mueller
 * @since 20.11.2018
 *
 */
public interface IWithIsbn {


    /**
     * Returns this books isbn (international standard book number)
     */
    public String getIsbn();

    /**
     * Sets this books isbn (international standard book number)
     * @param isbn
     */
    public void setIsbn(String isbn);
}
