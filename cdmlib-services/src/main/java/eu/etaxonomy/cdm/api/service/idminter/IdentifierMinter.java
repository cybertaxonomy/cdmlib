/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

/**
 * @author a.kohlbecker
 * @since Dec 12, 2017
 *
 */
public interface IdentifierMinter<T> {

    public class Identifier<T>{
        T localId;

        String identifier;
        /**
         * @return the localId
         */
        public T getLocalId() {
            return localId;
        }
        /**
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }


    }

    public void setMinLocalId(T min);

    public void setMaxLocalId(T max);

    public Identifier<T> mint() throws OutOfIdentifiersException;
}
