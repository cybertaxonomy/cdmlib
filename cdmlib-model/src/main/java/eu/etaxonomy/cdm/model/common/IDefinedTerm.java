/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;


import eu.etaxonomy.cdm.model.media.Media;

public interface IDefinedTerm<T extends IDefinedTerm> extends ILoadableTerm<T> {

	@Transient
	public UUID getUuid();
	
	public T getByUuid(UUID uuid);

	/**
	 * Returns the defined term this term is a kind of.
	 * Therefore the returned term is a generalization of <code>this</code> term
	 */
	public T getKindOf();

//	public void setKindOf(T kindOf);

	/**
	 * Returns all defined terms this term is a generalization for.
	 * Therefore the returned terms are kind of <code>this</code> term
	 */
	public Set<T> getGeneralizationOf();

//	public void setGeneralizationOf(Set<T> generalizationOf);
//
//	public void addGeneralizationOf(T generalization);
//
//	public void removeGeneralization(T generalization);

	/**
	 * Returns the defined term this term is a part of.
	 * Therefore the returned term includes <code>this</code> term
	 */
	public T getPartOf();

//	public void setPartOf(T partOf);

	/**
	 * Returns all defined terms this term includes.
	 * Therefore the returned terms are part of <code>this</code> term
	 */
	public Set<T> getIncludes();

//	public void setIncludes(Set<T> includes);
//
//	public void addIncludes(T includes);
//
//	public void removeIncludes(T includes);

	public Set<Media> getMedia();

//	public void setMedia(Set<Media> media);
//
//	public void addMedia(Media media);
//
//	public void removeMedia(Media media);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#getVocabulary()
	 */
//	public TermVocabulary<T> getVocabulary();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IDefTerm#setVocabulary(eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
//	public void setVocabulary(TermVocabulary<T> newVocabulary);

}