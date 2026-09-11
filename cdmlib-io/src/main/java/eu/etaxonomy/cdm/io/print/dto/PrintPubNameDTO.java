/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print.dto;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author muellera
 * @since 11.09.2026
 */
public class PrintPubNameDTO {

    public List<TaggedText> taggedNameList;
    public String scientificName;

    public List<String> links = new ArrayList<>();
    public List<String> wfoIds = new ArrayList<>();
    public List<String> ipniIds = new ArrayList<>();
}
