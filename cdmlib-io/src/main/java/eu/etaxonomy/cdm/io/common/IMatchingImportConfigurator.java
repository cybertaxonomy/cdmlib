/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

/**
 * @author a.kohlbecker
 * @since 15.09.2010
 *
 */
public interface IMatchingImportConfigurator {

	public void setReuseExistingTaxaWhenPossible(boolean doMatchTaxa);

	public boolean isReuseExistingTaxaWhenPossible();



}
