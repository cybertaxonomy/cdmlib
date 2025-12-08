/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

/**
 * Interface for all CdmBase classes that are CDM entities (annotated by a @Entity
 * annotation, not by a @MappedSuperclas annotation. These are all classes that are
 * allowed to be used e.g. in JPA select clauses.
 *
 * @author muellera
 * @since 20.06.2025
 */
public interface ICdmEntity extends ICdmBase {

}
