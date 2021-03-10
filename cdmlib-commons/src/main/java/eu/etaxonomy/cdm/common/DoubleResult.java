/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.apache.log4j.Logger;

/**
 * An instance of this class represents an method result that contains 2 variables. The variables may be typified.
 *
 * @author a.mueller
 * @since 30.10.2008
 */
public class DoubleResult<S extends Object, T extends Object> {
	private static final Logger logger = Logger.getLogger(DoubleResult.class);

	private S firstResult = null;
	private T secondResult = null;

	public DoubleResult() {
		if (logger.isDebugEnabled()){logger.debug("Constructor");}
	}


	public DoubleResult(S firstResult, T secondResult) {
		this.firstResult = firstResult;
		this.secondResult = secondResult;
	}

	public S getFirstResult() {
		return firstResult;
	}
	public void setFirstResult(S firstResult) {
		this.firstResult = firstResult;
	}

	public T getSecondResult() {
		return secondResult;
	}
	public void setSecondResult(T secondResult) {
		this.secondResult = secondResult;
	}
}