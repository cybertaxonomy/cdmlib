/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author a.kohlbecker
 * @since Nov 6, 2018
 *
 */
public class EntityInitStrategy {

    private static final String DOT = ".";

    List<String> propertyPaths = new ArrayList<>();

    public EntityInitStrategy(){

    }


    public EntityInitStrategy(List<String> propertyPaths){
        if(propertyPaths != null){
            this.propertyPaths.addAll(propertyPaths);
        }
    }

    public void extend(String basePath, List<String> extensions, boolean basePathIsCollection){
        for(String appendix : extensions){
            if(basePathIsCollection && (appendix.startsWith("$") || appendix.startsWith("*"))){
                // need to suppress wildcards, see AdvancedBeanInitializer.initializeNodeWildcard()
                continue;
            }
            propertyPaths.add(basePath + DOT + appendix);
            propertyPaths.remove(basePath);
        }
    }


    /**
     * @return
     */
    public List<String> getPropertyPaths() {
        return propertyPaths;
    }
}
