/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * This interface represents articles in a {@link IJournal journal}. An article is an independent
 * piece of prose written by an {@link TeamOrPersonBase author (team)} which is published among
 * other articles within a particular issue of a journal.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "JournalArticle"
 * <li> "NewspaperArticle"
 * <li> "MagazineArticle"
 * </ul>
 * It also implements {@link IHasEditor} as in rare cases (e.g. Notulae) an "article" may have an editor
 * (and no author). See #10710
 */
public interface IArticle extends ISection, IVolumeReference, ISeriesPart, IHasEditor {

	/**
	 * Returns this articles journal.
	 * @return
	 */
	public IJournal getInJournal();

	/**
	 * Sets this articles journal
	 * @param journal
	 */
	public void setInJournal(IJournal journal);
}