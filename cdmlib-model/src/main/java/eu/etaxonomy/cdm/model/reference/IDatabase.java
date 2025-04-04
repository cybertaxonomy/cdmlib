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
 * This interface represents a database used as an information source. A database is
 * a structured collection of records or data.
 * <P>
 * This interface corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "ComputerProgram".
 */
public interface IDatabase extends IAuthoredPublicationBase, IDynamicReference, IHasEditor {

}
