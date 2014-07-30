// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import java.util.regex.Pattern;

import org.springframework.core.type.filter.RegexPatternTypeFilter;

/**
 * a RegexPatternTypeFilter with the pattern {@code eu\.etaxonomy\.cdm\.remote\.vaadin\..*}
 * @author a.kohlbecker
 * @date Jul 28, 2014
 *
 */
public class VaadinPackageFilter extends RegexPatternTypeFilter {

    /**
     * @param pattern
     */
    public VaadinPackageFilter() {
        super(Pattern.compile("eu\\.etaxonomy\\.cdm\\.remote\\.vaadin\\..*"));
    }

}
