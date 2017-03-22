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

    /**
     * Returns a title cache String created by the according cache strategy
     * with the given identifiable entity WITHOUT setting the titleCache
     * of <code>this</code> object.
     * This method is meant for internal use and usually not needed for
     * external use. Use {@link #getTitleCache()} instead.
     *
     * @see #getTitleCache()
     * @return the computed title cache string
     */
    public String generateTitle();

    /**
     * Returns the title cache. If the title cache does not
     * exist yet or if a refresh is required the title cache is
     * recomputed and stored in the entity.<BR>
     * It is currently up to the implementing classes
     * if the cache is recomputed each time or only if the entity
     * has changed.
     *
     * @see #generateTitle()
     * @return the titleCache
     */
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


    /**
     * Replaces all occurrences of oldObject in the credits list with newObject
     * @param newObject the replacement object
     * @param oldObject the object to be replaced
     * @return true, if an object was replaced, false otherwise
     */
    public boolean replaceCredit(Credit newObject, Credit oldObject);

    public Set<Extension> getExtensions();

    public void addExtension(Extension extension);

    public void addExtension(String value, ExtensionType extensionType);

    public void removeExtension(Extension extension);

    /**
     * Returns the list of {@link Identifier alternative identifiers}.
     * In case the order of these identifiers is important it should be
     * implemented such that the first item in the list is the most
     * important/most current identifier. <BR>
     * E.g. if a barcode identifier
     * is more important than the accession number for a certain
     * specimen, than the barcode identifier should be before the accession number.
     * <BR>Or if a sample designation is the most recent of all sample designations
     * than it should be the first in the list while all history designations come
     * later.
     * @return
     */
    public List<Identifier> getIdentifiers();

    /**
     * Create and add a new identifier.
     * @see #getIdentifiers()
     * @param identifier
     * @param identifierType
     * @return
     */
    public Identifier addIdentifier(String identifier, DefinedTerm identifierType);

    /**
     * @see #getIdentifiers()
     * @param identifier
     */
    public void addIdentifier(Identifier identifier);

    /**
     * Adds an identifier at the given position. For use of
     * <code>index</code> see {@link List#add(int, Object)} and {@link#getIdentifiers()}
     * @see #getIdentifiers()
     * @param index the list index
     * @param identifier the identifier
     */
    public void addIdentifier(int index, Identifier identifier);

    /**
     * Removes an identifier at the given position. For use of
     * <code>index</code> see {@link List#add(int, Object)} and {@link#getIdentifiers()}
     * @param index the list index
     */
    public void removeIdentifier(int index);


    /**
     * Removes an identifier
     * @see #getIdentifiers()
     * @param identifier
     */
    public void removeIdentifier(Identifier identifier);

    /**
     * Replaces all occurrences of oldObject in the identifiers list with newObject
     * @param newObject the replacement object
     * @param oldObject the object to be replaced
     * @return true, if an object was replaced, false otherwise
     */
    public boolean replaceIdentifier(Identifier newObject, Identifier oldObject);


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
    @Override
    public String toString();

    public byte[] getData();

	void removeSources();




}
