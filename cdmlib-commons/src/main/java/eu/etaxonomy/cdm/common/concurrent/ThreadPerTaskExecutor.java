/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.concurrent;

import java.util.concurrent.Executor;

/**
 * @author a.mueller
 * @since 26.08.2019
 */
public class ThreadPerTaskExecutor implements Executor{
    @Override
    public void execute(Runnable r) { new Thread(r).start(); }
}
