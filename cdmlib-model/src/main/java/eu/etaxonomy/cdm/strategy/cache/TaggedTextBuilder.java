/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Jul 12, 2018
 */
public class TaggedTextBuilder {

    List<TaggedText> taggedText = new ArrayList<>();

    public void add(TagEnum type, String text){
        taggedText.add(new TaggedText(type, text));
    }

    public void addSeparator(String separator) {
        taggedText.add(TaggedText.NewSeparatorInstance(separator));
    }

    public void addWhitespace() {
        taggedText.add(TaggedText.NewWhitespaceInstance());
    }


    public void add(TagEnum type, String text, TypedEntityReference<?> entityReference){
        taggedText.add(new TaggedText(type, text, entityReference));
    }

    public void add(TagEnum type, String text, CdmBase entity){
        CdmBase deproxiedEntity = HibernateProxyHelper.deproxy(entity);
        taggedText.add(new TaggedText(type, text, new TypedEntityReference<>(deproxiedEntity.getClass(), deproxiedEntity.getUuid())));
    }

    public void  clear() {
        taggedText.clear();
    }

    public void addAll(TaggedTextBuilder ttb) {
        taggedText.addAll(ttb.taggedText);
    }

    public void addAll(List<TaggedText> tags) {
        taggedText.addAll(tags);
    }

    public List<TaggedText> getTaggedText() {
        return taggedText;
    }

    @Override
    public String toString(){
        return TaggedCacheHelper.createString(taggedText);
    }
}