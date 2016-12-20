/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


 /**
 * Interface providing methods for nomenclatural references.
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:29
 */
public interface INomenclaturalReference  extends IReference{

	public final String MICRO_REFERENCE_TOKEN = "@@MicroReference";


	/**
     * Returns the citation string including the details (micro reference) information
     * from {@link TaxonNameBase taxon name}.
     * E.g. if the references title cache is <i>L., Sp. Pl. 3. 1757</i> the nomenclatural citation
     * may be something like <i>L., Sp. Pl. 3: 45. 1757</i>
     *
     * @param microReference the detail, e.g. a page number, a figure, ...
     * @return String
     */
    public String getNomenclaturalCitation(String microReference);

	/**
	 * Returns a string representation for the year of publication / creation
	 * of a reference.
	 */
	public String getYear();



	/**
	 * Returns the cached value for the abbreviated representation of this reference.
	 * The abbreviated representation will normally use the {@link #getAbbrevTitle()
	 * abbreviated title } instead of the {@link Reference#getTitle() full title}.
	 * The cache may be protected.
	 * @see #getAbbrevTitle()
	 * @see #getTitle()
	 * @see #isProtectedAbbrevTitleCache()
	 * @return the abbreviated representation of this reference
	 */
	//TODO discuss if we move all the abbreviated title methods to another interface, e.g. IReference
	public String getAbbrevTitleCache();

	/**
	 * Sets the {@link #getAbbrevTitleCache() abbreviated title cache}
	 *
	 * @param abbrevTitleCache
	 * @deprecated this method exists only for compliance with the java bean standard.
	 * It usually has little effect as it will not protect the cache.
	 * Use {@link #setAbbrevTitleCache(String, boolean)} instead to protect the cache.
	 */
	@Deprecated
    public void setAbbrevTitleCache(String abbrevTitleCache);

	/**
	 * Sets the {@link #getAbbrevTitleCache() abbreviated title cache}.
	 *
	 * @param abbrevTitleCache
	 * @param isProtected wether or not the cache should be protected. Usually one may
	 * want to set isProtected to <code>true</code>
	 */
	public void setAbbrevTitleCache(String abbrevTitleCache, boolean isProtected);


	/**
	 * If true the {@link #getAbbrevTitleCache() abbreviated title cache} is not computed
	 * on the fly but set by {@link #setAbbrevTitleCache(String)}.
	 * @see IIdentifiableEntity#isProtectedTitleCache()
	 * @return true if cache is protected
	 */
	public boolean isProtectedAbbrevTitleCache();

	/**
	 * Sets the protected flag for the {@link #getAbbrevTitleCache() abbreviated title cache}.
	 * @param protectedAbbrevTitleCache
	 */
	public void setProtectedAbbrevTitleCache(boolean protectedAbbrevTitleCache);

}
