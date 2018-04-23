/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

/**
 * @author a.kohlbecker
 \* @since Jan 31, 2014
 *
 */
public interface IPublishable {

    /**
     * Returns the boolean value indicating if this entity should be withheld (<code>publish=false</code>) or not
     * (<code>publish=true</code>) during any publication process to the general public.
     * This publish flag implementation is preliminary and may be replaced by a more general
     * implementation of READ rights in future.<BR>
     * The default value is <code>true</code>.
     */
    public boolean isPublish();


    public void setPublish(boolean isPublish);

}
