/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl;

/**
 * @author pplitzner
 * @since Apr 15, 2019
 *
 */
public interface OwlConstants {

    public static final String BASE_URI = "http://cybertaxonomy.eu/";

    /**
     * resource URIs
     */
    public static final String RESOURCE_URI = BASE_URI+"resource/";
    public static final String RESOURCE_NODE = RESOURCE_URI+"node/";
    public static final String RESOURCE_FEATURE_TREE = RESOURCE_URI+"featureTree/";

    /**
     * property URIs
     */
    public static final String PROPERTY_BASE_URI = BASE_URI+"property/";
    public static final String PROPERTY_HAS_ROOT_NODE = PROPERTY_BASE_URI + "hasRootNode";
    public static final String PROPERTY_LABEL = PROPERTY_BASE_URI+"label";
    public static final String PROPERTY_IS_A = PROPERTY_BASE_URI+"is_a";
    public static final String PROPERTY_UUID = PROPERTY_BASE_URI+"uuid";
    public static final String PROPERTY_HAS_SUBSTRUCTURE = PROPERTY_BASE_URI+"hasSubStructure";

    /**
     * types
     */
    public final static String NODE = "node";
    public final static String TREE = "tree";
}
