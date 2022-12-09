/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;

/**
 * Interface representing all classes that can be used as {@link State states}.
 *
 * @author a.mueller
 * @date 08.12.2022
 * @see https://dev.e-taxonomy.eu/redmine/issues/10196
 */
public interface IAsState extends IIdentifiableEntity {

    /**
     * @see TermBase#getLabel()
     */
    public String getLabel();

    /**
     * @see TermBase#getPreferredRepresentation(List)
     */
    public Representation getPreferredRepresentation(List<Language> languages);

    /**
     * @see TermBase#getRepresentations()
     */
    public Set<Representation> getRepresentations();

    /**
     * @see TermBase#getPreferredRepresentation(Language)
     */
    public Representation getPreferredRepresentation(Language language);

}