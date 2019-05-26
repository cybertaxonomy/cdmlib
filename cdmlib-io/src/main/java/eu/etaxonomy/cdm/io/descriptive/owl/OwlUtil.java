/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * @author pplitzner
 * @since Apr 15, 2019
 *
 */
public class OwlUtil {

    public static final String BASE_URI = "http://cybertaxonomy.eu/";

    /**
     * resource URIs
     */
    public static final String RESOURCE_URI = BASE_URI+"resource/";
    public static final String RESOURCE_NODE = RESOURCE_URI+"node/";
    public static final String RESOURCE_REPRESENTATION = RESOURCE_URI+"representation/";
    public static final String RESOURCE_FEATURE_TREE = RESOURCE_URI+"featureTree/";
    public static final String RESOURCE_TERM_VOCABULARY = RESOURCE_URI+"termVocabulary/";
    public static final String RESOURCE_TERM = RESOURCE_URI+"term/";

    /**
     * property URIs
     */
    public static final String PROPERTY_BASE_URI = BASE_URI+"property/";
    public static final String PROPERTY_UUID = PROPERTY_BASE_URI+"uuid";
    public static final String PROPERTY_URI = PROPERTY_BASE_URI+"uri";
    public static final String PROPERTY_HAS_ROOT_NODE = PROPERTY_BASE_URI + "hasRootNode";
    public static final String PROPERTY_HAS_SUBSTRUCTURE = PROPERTY_BASE_URI+"hasSubStructure";
    public static final String PROPERTY_HAS_REPRESENTATION = PROPERTY_BASE_URI+"hasRepresentation";
    public static final String PROPERTY_HAS_VOCABULARY = PROPERTY_BASE_URI+"hasVocabulary";
    public static final String PROPERTY_HAS_TERM = PROPERTY_BASE_URI+"hasTerm";

    public static final String PROPERTY_LABEL = PROPERTY_BASE_URI+"label";
    public static final String PROPERTY_LABEL_ABBREV = PROPERTY_BASE_URI+"label_abbrev";
    public static final String PROPERTY_DESCRIPTION = PROPERTY_BASE_URI+"description";
    public static final String PROPERTY_LANGUAGE = PROPERTY_BASE_URI+"language";
    public static final String PROPERTY_LANGUAGE_UUID = PROPERTY_BASE_URI+"language_uuid";

    public static final String PROPERTY_IS_A = PROPERTY_BASE_URI+"is_a";
    public static final String PROPERTY_TYPE = PROPERTY_BASE_URI+"type";

    public static final String PROPERTY_TERM_INCLUDES = PROPERTY_BASE_URI+"term_includes";
    public static final String PROPERTY_TERM_IS_GENERALIZATION_OF = PROPERTY_BASE_URI+"term_is_generalization_of";

    /**
     * types
     */
    public final static String NODE = "node";
    public final static String TREE = "tree";
    public final static String VOCABULARY = "vocabulary";
    public final static String TERM = "term";

    public static Property propHasSubStructure;
    public static Property propHasRepresentation;
    public static Property propHasRootNode;
    public static Property propUuid;
    public static Property propUri;
    public static Property propLabel;
    public static Property propLabelAbbrev;
    public static Property propLanguage;
    public static Property propLanguageUuid;
    public static Property propIsA;
    public static Property propType;
    public static Property propDescription;

    public static Property propHasVocabulary;
    public static Property propHasTerm;

    public static Property propTermIsGeneralizationOf;
    public static Property propTermIncludes;

    public static Model createModel(){
        Model model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlUtil.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRepresentation = model.createProperty(OwlUtil.PROPERTY_HAS_REPRESENTATION);
        propHasRootNode = model.createProperty(OwlUtil.PROPERTY_HAS_ROOT_NODE);
        propUuid = model.createProperty(OwlUtil.PROPERTY_UUID);
        propUri = model.createProperty(OwlUtil.PROPERTY_URI);
        propLabel = model.createProperty(OwlUtil.PROPERTY_LABEL);
        propLabelAbbrev = model.createProperty(OwlUtil.PROPERTY_LABEL_ABBREV);
        propLanguage = model.createProperty(OwlUtil.PROPERTY_LANGUAGE);
        propLanguageUuid = model.createProperty(OwlUtil.PROPERTY_LANGUAGE_UUID);
        propIsA = model.createProperty(OwlUtil.PROPERTY_IS_A);
        propType = model.createProperty(OwlUtil.PROPERTY_TYPE);
        propDescription = model.createProperty(OwlUtil.PROPERTY_DESCRIPTION);

        propHasVocabulary = model.createProperty(OwlUtil.PROPERTY_HAS_VOCABULARY);
        propHasTerm = model.createProperty(OwlUtil.PROPERTY_HAS_TERM);

        propTermIsGeneralizationOf = model.createProperty(OwlUtil.PROPERTY_TERM_IS_GENERALIZATION_OF);
        propTermIncludes = model.createProperty(OwlUtil.PROPERTY_TERM_INCLUDES);

        return model;
    }

}
