/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroup.TypeDesignationSetType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * @author muellera
 * @since 23.04.2024
 */
public class TextualTypeDesignationGroupFormatter extends TypeDesignationGroupFormatterBase<TextualTypeDesignation> {

    public static final TextualTypeDesignationGroupFormatter INSTANCE() {
        return new TextualTypeDesignationGroupFormatter();
    }

    public void format(TaggedTextBuilder finalBuilder, TypeDesignationGroupContainer manager,
            Map<VersionableEntity,TypeDesignationGroup> orderedBaseEntity2TypesMap,
            int typeSetCount,
            TypeDesignationGroupFormatterConfiguration config,
            TextualTypeDesignation baseEntity, TypeDesignationSetType lastWsType) {

        TypeDesignationGroup typeDesignationGroup = orderedBaseEntity2TypesMap.get(baseEntity);

        TaggedTextBuilder localBuilder = new TaggedTextBuilder();

        if(typeSetCount > 0){
            localBuilder.add(TagEnum.separator, TYPE_SEPARATOR);
        }else if (config.isWithStartingTypeLabel()){
            //TODO this is not really exact as we may want to handle specimen types and
            //name types separately, but this is such a rare case (if at all) and
            //increases complexity so it is not yet implemented
            boolean isPlural = hasMultipleTypes(orderedBaseEntity2TypesMap);
            String label = (isPlural? "Type": "Type");  // for now we do not distinguish if multiple groups exist, this may change in future:  #9596#note-45
            localBuilder.add(TagEnum.label, label);
            localBuilder.add(TagEnum.postSeparator, ": ");
        }

        //TODO why is typeDesingationSet not a list
//        List<TypeDesignationStatusBase<?>> statusList = new ArrayList<>(typeDesignationSet.keySet());
//        statusList.sort(statusComparator);

        //text
        String text = entityLabel(baseEntity, config);
        if(baseEntity.isVerbatim()) {
            text = "\"" + text + "\"";
        }
        localBuilder.add(TagEnum.typeDesignation, text, baseEntity);

        //source
        if (config.isWithCitation()) {
            handleGeneralSource(baseEntity, localBuilder, config);
        }

        typeDesignationGroup.setRepresentation(localBuilder.toString());
        finalBuilder.addAll(localBuilder);
        return;
    }

    @Override
    protected String entityLabel(TextualTypeDesignation textualTypeDesignation,
            TypeDesignationGroupFormatterConfiguration config) {

        List<Language> languages = config != null? config.getLanguages() : new ArrayList<>(); //Note: can be null when used in constructor call to avoid lazy loading or for RegistrationWrapperDTO
        LanguageString lsString = MultilanguageTextHelper.getPreferredLanguageString(textualTypeDesignation.getText(),
                languages);
        return lsString != null ? lsString.getText(): null;
    }

    @Override
    protected void buildTaggedTextForTypeDesignationBase(TypeDesignationBase<?> typeDes,
            TaggedTextBuilder workingsetBuilder, TypeDesignationGroupFormatterConfiguration config) {
        //not needed here
    }
}
