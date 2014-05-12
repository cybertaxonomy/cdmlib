/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.media.Rights;

public interface IIdentifiableEntity extends ISourceable<IdentifiableSource>, IAnnotatableEntity{

    public LSID getLsid();

    public void setLsid(LSID lsid);

    public String generateTitle();

    public String getTitleCache();

    /**
     * Sets the title cache without changing the <code>protectCache</code> flag.<BR><BR>
     * NOTE: Use with care. If this flag is <code>false</code> the <code>titleCache</code> may be 
     * recomputed with the next call of {@link #getTitleCache()}, which is automatically the case when
     * the object is persisted.
     * @see #setTitleCache(String, boolean)
     * @see #getTitleCache()
     * @param titleCache
     * @deprecated this method only exists to be in line with the Java Beans Specification (JSR 220 or JSR 273) .
     * As it will set the {@link #isProtectedTitleCache() protected} flag to false the title cache value
     * may be automatically recomputed later. There are only very rare use cases were a programmer may 
     * want to use this method directly.
     * Better use {@link #setTitleCache(String, boolean)} with second parameter <code>protectCache</code>
     * set to <code>true</code>.
     */
    @Deprecated
    public void setTitleCache(String titleCache);

    /**
     * Sets the title cache.<BR>
     * NOTE: In most cases the <code>protectCache</code> argument should be set to <code>true</code>.
     * See comments at {@link #setTitleCache(String)}
     * 
     * @param titleCache the new title cache
     * @param protectCache the protect flag, <b>should in most cases be set to <code>true</code></b>
     */
    public void setTitleCache(String titleCache, boolean protectCache);

    public boolean isProtectedTitleCache();

    public void setProtectedTitleCache(boolean protectedTitleCache);
    
    public Set<Rights> getRights();

    public void addRights(Rights right);

    public void removeRights(Rights right);

    public List<Credit> getCredits();

    public Credit getCredits(Integer index);

    public void addCredit(Credit credig);

    public void addCredit(Credit credit, int index);

    public void removeCredit(Credit credit);

    public void removeCredit(int index);

    public Set<Extension> getExtensions();

    public void addExtension(Extension extension);

    public void removeExtension(Extension extension);

    /**
     * Overrides {@link eu.etaxonomy.cdm.model.common.CdmBase#toString()}.
     * This returns an String that identifies the object well without beeing necessarily unique.
     * Specification: This method should never call other object' methods so it can be well used for debugging
     * without problems like lazy loading, unreal states etc.
     * Note: If overriding this method's javadoc always copy or link the above requirement.
     * If not overwritten by a subclass method returns the class, id and uuid as a string for any CDM object.
     * For example: Taxon#13<b5938a98-c1de-4dda-b040-d5cc5bfb3bc0>
     * @see java.lang.Object#toString()
     */
    public String toString();

    public byte[] getData();

}