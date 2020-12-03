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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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

    public EntityInitStrategy(String ... propertyPaths){
        if(propertyPaths != null){
            this.propertyPaths.addAll(Arrays.asList(propertyPaths));
        }
    }

    /**
     * Extends the property base bath by all property definitions in the
     * <code>extensions</code> and adds the resulting property path to the
     * EntityInitStrategy.
     * <p>
     * Potential duplicate property paths de-duplicated.
     *
     * @param basePath
     *            can be NUll or empty to just append the extensions to the init
     *            strategies.
     * @param extensions
     * @param basePathIsCollection
     */
    public EntityInitStrategy extend(String basePath, EntityInitStrategy extensions, boolean basePathIsCollection) {
        return extend(basePath, extensions.getPropertyPaths(), basePathIsCollection);
    }

    /**
     * Extends the property base bath by all property definitions in the
     * <code>extensions</code> and adds the resulting property path to the
     * EntityInitStrategy.
     * <p>
     * Potential duplicate property paths de-duplicated.
     *
     * @param basePath
     *            can be NUll or empty to just add the extensions to the init
     *            strategies.
     * @param extensions
     * @param basePathIsCollection
     */
    public EntityInitStrategy extend(String basePath, List<String> extensions, boolean basePathIsCollection){
        for(String appendix : extensions){
            if(basePathIsCollection && (appendix.startsWith("$") || appendix.startsWith("*"))){
                // need to suppress wildcards, see AdvancedBeanInitializer.initializeNodeWildcard()
                continue;
            }
            if(!StringUtils.isEmpty(basePath)){
                propertyPaths.add(basePath + DOT + appendix);
                propertyPaths.remove(basePath);
            } else {
                propertyPaths.add(appendix);
            }
        }
        propertyPaths = propertyPaths.stream().distinct().collect(Collectors.toList());
        return this;
    }


    /**
     * @return
     */
    public List<String> getPropertyPaths() {
        return propertyPaths;
    }

    @Override
    public EntityInitStrategy clone() {
        return new EntityInitStrategy(this.propertyPaths);

    }
}
