/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 */
public class StructureTreeOwlImportState
        extends ImportStateBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImport> {

    private Model model;

    /**
     * Terms already handled in this import run. Avoids repeated {@code exists}/{@code find}
     * queries (and their Hibernate auto-flush + Bean Validation cost) for nested includes.
     */
    private final Map<UUID, DefinedTermBase<?>> termCache = new HashMap<>();

    protected StructureTreeOwlImportState(StructureTreeOwlImportConfigurator config) {
        super(config);
        model = OwlUtil.createModel();
    }

    public Model getModel() {
        return model;
    }

    public Map<UUID, DefinedTermBase<?>> getTermCache() {
        return termCache;
    }
}