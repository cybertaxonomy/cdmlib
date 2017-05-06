/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

/**
 * TaxonName interface for fungi.
 *
 * @author a.mueller
 * @date 05.05.2017
 */
public interface IFungusName extends IBotanicalName {
      /**
      * Returns the boolean value of the flag indicating whether the specimen
      * type of <i>this</i> fungi name is asexual (true) or not
      * (false). The Article 59 of the ICBN
      * permits mycologists to give asexually reproducing fungi (anamorphs)
      * separate names from their sexual states (teleomorphs).
      *
      * @return  the boolean value of the isAnamorphic flag
      */
     public boolean isAnamorphic();

     /**
      * @see  #isAnamorphic()
      */
     public void setAnamorphic(boolean anamorphic);
}
