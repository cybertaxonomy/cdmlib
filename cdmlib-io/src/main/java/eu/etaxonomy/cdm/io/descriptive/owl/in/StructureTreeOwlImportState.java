/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class StructureTreeOwlImportState extends ImportStateBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImport> {

    static Property propHasSubStructure;
    static Property propHasRepresentation;
    static Property propHasRootNode;
    static Property propUuid;
    static Property propUri;
    static Property propLabel;
    static Property propLabelAbbrev;
    static Property propLanguage;
    static Property propLanguageUuid;
    static Property propIsA;
    static Property propType;
    static Property propDescription;

    private Model model;

    protected StructureTreeOwlImportState(StructureTreeOwlImportConfigurator config) {
        super(config);


        model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRepresentation = model.createProperty(OwlConstants.PROPERTY_HAS_REPRESENTATION);
        propHasRootNode = model.createProperty(OwlConstants.PROPERTY_HAS_ROOT_NODE);
        propUuid = model.createProperty(OwlConstants.PROPERTY_UUID);
        propUri = model.createProperty(OwlConstants.PROPERTY_URI);
        propLabel = model.createProperty(OwlConstants.PROPERTY_LABEL);
        propLabelAbbrev = model.createProperty(OwlConstants.PROPERTY_LABEL_ABBREV);
        propLanguage = model.createProperty(OwlConstants.PROPERTY_LANGUAGE);
        propLanguageUuid = model.createProperty(OwlConstants.PROPERTY_LANGUAGE_UUID);
        propIsA = model.createProperty(OwlConstants.PROPERTY_IS_A);
        propType = model.createProperty(OwlConstants.PROPERTY_TYPE);
        propDescription = model.createProperty(OwlConstants.PROPERTY_DESCRIPTION);

    }

    public Model getModel() {
        return model;
    }

}
