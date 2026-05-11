/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub.order;

import java.util.Comparator;

import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO;

public interface IPrintPubFactOrderStrategy {

    Comparator<PrintPubFactDTO> comparator();
}