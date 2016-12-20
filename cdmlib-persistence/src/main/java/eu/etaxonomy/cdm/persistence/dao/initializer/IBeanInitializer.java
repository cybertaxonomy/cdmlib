/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.Collection;
import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.kohlbecker
 * @date 25.03.2009
 *
 */
public interface IBeanInitializer {

    /**
     * Wildcard for initializing all *ToOne relations of a <code>bean</code>.
     */
    public static final String LOAD_2ONE_WILDCARD = "$";

    /**
     * Wildcard for initializing all *ToOne and all *ToMany relations of a <code>bean</code>.
     */
    public static final String LOAD_2ONE_2MANY_WILDCARD = "*";

    /**
     * Initializes all *ToOne relations of the <code>bean</code>.
     *
     * @param bean
     */
    public void load(Object bean);

    /**
     * Initializes all *ToOne and all *ToMany relations of the <code>bean</code>.
     *
     * @param bean
     */
    public void loadFully(Object bean);

    /**
     * Allows more fine grained initialization not only of the root bean
     * identified by its <code>uuid</code> but also of specific paths of
     * the object graph. The sub graph to initialize may be defined in the
     * <code>propertyPaths</code> parameter as list of paths all starting at the
     * root bean.
     * <p>
     * You can use wildcards <code>*</code> {@link LOAD_2ONE_2MANY_WILDCARD}
     * and <code>$</code> {@link LOAD_2ONE_WILDCARD} for initializing
     * all *ToOne and all *ToMany relations of a bean.
     * NOTE: A wildcard subsequently terminates its property path.
     * <p>
     * <b>Example:</b> Assuming <code>cdmEntity</code> is a {@link Taxon}
     * instance the following <code>propertyPaths</code> can be used for
     * initializing bean properties of this instance. It is possible to
     * initialized nested properties of properties with unlimited depth.
     * <ul>
     * <li><code>name</code>: initializes {@link Taxon#getName()}</li>
     * <li><code>name.rank</code>: initializes {@link Taxon#getName()}.{@link TaxonNameBase#getRank() getRank()}</li>
     * <li><code>name.$</code>: initializes all *ToOne relations of the {@link Taxon#getName()}</li>
     * </ul>
     *
     * @param bean
     * @param propertyPaths
     *            a List of property names
     */
    public void initialize(Object bean,  List<String> propertyPaths);

    /**
     * Initializes the entities given in the bean list according to the given
     * <code>propertyPaths</code>.
     *
     * @param beanList
     * @param propertyPaths
     * @return
     */
    public <C extends Collection<?>> C initializeAll(C list,  List<String> propertyPaths);

    /**
     * Initialize the the proxy, unwrap the target object and return it.
     *
     * @param proxy
     *            the proxy to initialize may wrap a single bean or a collection
     * @return the unwrapped target object
     */
    public Object initializeInstance(Object proxy);


    /*TODO implement:
    public void loadBy(UUID uuid);
    public void loadFullyByUuid(UUID uuid);*/

}
