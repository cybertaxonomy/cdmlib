/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;

/**
 *
 * @author pplitzner
 * @since May 2, 2019
 *
 */
public class StructureTreeOwlExportState extends XmlExportState<StructureTreeOwlExportConfigurator>{

    static Property propHasSubStructure;
    static Property propHasRepresentation;
    static Property propHasVocabulary;
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

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(StructureTreeOwlExportState.class);

    public StructureTreeOwlExportState(StructureTreeOwlExportConfigurator config) {
        super(config);
        // create model properties
        model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRepresentation = model.createProperty(OwlConstants.PROPERTY_HAS_REPRESENTATION);
        propHasVocabulary = model.createProperty(OwlConstants.PROPERTY_HAS_VOCABULARY);
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
