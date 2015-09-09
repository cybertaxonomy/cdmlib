// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;

/**
 * @author pplitzner
 * @date Oct 2, 2015
 *
 */
public class Pair<A, B> implements Serializable{

    private static final long serialVersionUID = -3055055268438072322L;

    private A a;
    private B b;

    public Pair(A a, B b) {
        super();
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }
    public void setA(A a) {
        this.a = a;
    }
    public B getB() {
        return b;
    }
    public void setB(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair other = (Pair) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        return true;
    }



}
