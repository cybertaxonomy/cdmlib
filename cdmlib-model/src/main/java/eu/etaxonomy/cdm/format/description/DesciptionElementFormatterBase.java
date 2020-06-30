/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.format.DefaultCdmFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * Formatter base class for {@link DescriptionElementBase} instances.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public abstract class DesciptionElementFormatterBase<T extends DescriptionElementBase>
        extends DefaultCdmFormatter{

    protected static final String MISSING_TERM_LABEL = "-no state-"; //TODO

    private Class<T> clazz;

    protected DesciptionElementFormatterBase(Object object, FormatKey[] formatKeys, Class<T> clazz) {
        super(object, formatKeys);
        this.clazz = clazz;
    }

    @Override
    public String format(Object object, FormatKey... formatKeys) {
        if (formatKeys == null){
            return format(object);
        }else{
            throw new UnsupportedOperationException("Formatting with FormatKey is not yet supported by " + this.getClass().getSimpleName());
        }
    }

    @Override
    public String format(Object object) {
        List<Language> preferredLanguages = new ArrayList<>();
        preferredLanguages.add(Language.DEFAULT());
        return format(object, preferredLanguages);
    }

    public String format(Object object, List<Language> preferredLanguages) {
        if (! (object instanceof ICdmBase)){
            throw new IllegalArgumentException("object is not of type ICdmBase");
        }
        ICdmBase cdmBase = (ICdmBase)object;
        if (!cdmBase.isInstanceOf(clazz)){
            throw new IllegalArgumentException("object is not of type " + clazz.getSimpleName());
        }
        T descEl = CdmBase.deproxy(object, clazz);
        if (preferredLanguages == null || preferredLanguages.isEmpty()){
            preferredLanguages = new ArrayList<>();
            preferredLanguages.add(Language.DEFAULT());
        }
        return doFormat(descEl, preferredLanguages);
    }

    /**
     * To be implemented by subclasses. The caller must guarantee that preferredLanguages
     * is neither <code>null</code> nor empty.
     */
    protected abstract String doFormat(T descEl, List<Language> preferredLanguages);


    protected String getLabel(DefinedTermBase<?> term, List<Language> preferredLanguages) {
        if (term == null){
            return MISSING_TERM_LABEL;
        }
        Representation representation = term.getPreferredRepresentation(preferredLanguages);
        if (representation == null){
            return term.toString();
        }
        if (isNotBlank(representation.getLabel())){
            return representation.getLabel();
        }else if (isNotBlank(representation.getAbbreviatedLabel())){
            return representation.getAbbreviatedLabel();
        }else if (isNotBlank(representation.getText())){
            return representation.getText();
        }else{
            return representation.toString();
        }
    }

}
