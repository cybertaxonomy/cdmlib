/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since 30.07.2010
 *
 */
public abstract class AutoPropertyInitializer<T extends CdmBase> {

    @Autowired
    protected IBeanInitializer beanInitializer;


    /**
     * Implement this method to initialize associated entities of the supplied beans.
     * <p>
     * For initializing collections of cdm entity bean it is recommended to
     * use {@link IBeanInitializer#initializeInstance(Object)}.<br>
     * <b>WARNING</b>: You must NOT use {@link IBeanInitializer#initialize(Object, java.util.List)}
     * or {@link IBeanInitializer#initializeAll(java.util.List, java.util.List)} otherwise you risk
     * to get StackOverflowExceptions.
     *
     * @param bean the cdm entity bean to process
     */
    public abstract void initialize(T bean);


	public abstract String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception;


}
