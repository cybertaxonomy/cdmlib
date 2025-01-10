/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Result of name parsing.
 *
 * @author muellera
 * @since 10.01.2025
 */
public class NameParserResult {

    private TaxonName name;

    private Set<TaxonName> otherNames = new HashSet<>();

    private Set<TeamOrPersonBase> authors = new HashSet<>();

    private Set<Reference> references = new HashSet<>();

    private boolean nameParsable = true;

    private boolean referenceParsable = true;

    public NameParserResult(TaxonName name) {
        this.name = name;
    }

    public TaxonName getName() {
        return name;
    }

    //other names
    public Set<TaxonName> getOtherNames() {
        return Collections.unmodifiableSet(otherNames);
    }
    public void addOtherName(TaxonName otherName) {
        this.otherNames.add(otherName);
    }

    //authors
    public Set<TeamOrPersonBase> getAuthors() {
        return Collections.unmodifiableSet(authors);
    }
    public void addAuthor(TeamOrPersonBase author) {
        this.authors.add(author);
    }

    //references
    public Set<Reference> getReferences() {
        return Collections.unmodifiableSet(references);
    }
    public void addReference(Reference reference) {
        this.references.add(reference);
    }

    //all entities
    public Set<CdmBase> getAllEntities() {
        Set<CdmBase> allEntities = new HashSet<>(authors);
        allEntities.addAll(references);
        allEntities.addAll(otherNames);
        allEntities.add(name);
        return allEntities;
    }
}