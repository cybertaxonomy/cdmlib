/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;


/**
 * This interface represents electronic publications the support of which are Cds 
 * (Compact Discs) or Dvds (Digital Versatile Discs). This class applies for Cds
 * or Dvds as a whole but not for parts of it.
 * CdDvd implements INomenclaturalReference as this seems to be allowed by the ICZN
 * (see http://www.iczn.org/electronic_publication.html)
 */
public interface ICdDvd extends IPublicationBase{

	
}
