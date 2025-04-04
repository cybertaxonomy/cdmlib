/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 24.04.2024
 */
public class TaggedTextWithLink extends TaggedText {

    private static final long serialVersionUID = 490748289094932840L;

    private String link;
    private String doi;

    public static <T extends CdmBase>  TaggedTextWithLink NewInstance(TagEnum type, String text,
            TypedEntityReference entity, DOI doi, URI link){
        return new TaggedTextWithLink(type, text, entity, doi, link);
    }

    private TaggedTextWithLink(TagEnum type, String text, TypedEntityReference<?> entity, DOI doi, URI link) {
        super(type, text, entity);
        this.doi = doi == null ? null : doi.asURI();
        this.link = link == null ? null : link.toString();
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public String getDoi() {
        return doi;
    }
    public void setDoi(String doi) {
        this.doi = doi;
    }
}