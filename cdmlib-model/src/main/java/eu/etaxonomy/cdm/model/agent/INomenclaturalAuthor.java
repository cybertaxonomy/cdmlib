/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

/**
* Interface providing methods for nomenclatural authorship.
*
* @author a.mueller
* @since 17-APR-2008
*/
public interface INomenclaturalAuthor {

	/**
	 * @see {@link TeamOrPersonBase#getNomenclaturalTitleCache()}
	 *
	 */
    public String getNomenclaturalTitleCache();


	/**
	 * @see {@link #getNomenclaturalTitleCache()}
	 * @deprecated to be replaced by {@link #getNomenclaturalTitleCache()}
	 */
    @Deprecated
	public void setNomenclaturalTitle(String nomenclaturalTitle);

    /**
     * fixes the missing setter method that corresponds to {@link #getNomenclaturalTitleCache()} see #9729
     *
     * @see {@link TeamOrPersonBase#getNomenclaturalTitleCache()}
     */
    public void setNomenclaturalTitleCache(String nomenclaturalTitle);

}
