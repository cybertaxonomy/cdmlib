/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

/**
 * @author a.mueller
 * @since 18.02.2021
 */
public class TripleResult<S extends Object, T extends Object, U extends Object>
        extends DoubleResult<S,T> {

    private U thirdResult;

    public TripleResult(S firstResult, T secondResult, U thirdResult) {
        super(firstResult, secondResult);
        this.thirdResult = thirdResult;
    }

    public U getThirdResult() {
        return thirdResult;
    }

    public void setThirdResult(U thirdResult) {
        this.thirdResult = thirdResult;
    }

}
