/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.term;

import java.util.Set;

import eu.etaxonomy.cdm.model.media.Media;

public interface IDefinedTerm<T extends IDefinedTerm>
        extends ISimpleTerm<T>, ILoadableTerm<T> {

    /**
     * Returns the defined term this term is a part of.
     * Therefore the returned term includes <code>this</code> term
     * Discuss: move to {@link ISimpleTerm}?
     * @see #getIncludes()
     */
    //Discuss: move to ISimpleTerm
    public T getPartOf();


    /**
     * Returns all defined terms this term includes.
     * Therefore the returned terms are part of <code>this</code> term
     * Discuss: move to {@link ISimpleTerm}?
     *
     * FIXME getIncludes():  Hibernate returns this as a collection of CGLibProxy$$DefinedTermBase objects
     * which can't be cast to instances of T - can we explicitly initialize these terms using
     * Hibernate.initialize() or ( ( PersistentCollection ) proxy ).forceInitialization(),
     * does this imply a distinct load, and find methods in the dao?
     *
     * @see #getPartOf()
     */
    public Set<T> getIncludes();

    /**
     * Returns the media attached to this {@link IDefinedTerm term}
     * @return
     */
    public Set<Media> getMedia();

    /**
     * Returns the label of this term used (and unique) in the term's vocabulary.
     * @see #setIdInVocabulary(String)
     */
    public String getIdInVocabulary();

    /**
     * Sets the label of this term used (and unique) in the term's vocabulary.
     * @see #getIdInVocabulary()
     * @param idInVocabulary
     */
    public void setIdInVocabulary(String idInVocabulary);

}
