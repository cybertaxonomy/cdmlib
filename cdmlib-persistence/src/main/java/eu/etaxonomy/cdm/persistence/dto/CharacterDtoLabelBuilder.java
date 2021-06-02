/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * @author k.luther
 * @since Apr 20, 2021
 */
public class CharacterDtoLabelBuilder  {

    public static final CharacterDtoLabelBuilder NewDefaultInstance(){
        return new CharacterDtoLabelBuilder();
    }

    private CharacterDtoLabelBuilder(){}

    public static String buildAbbrevLabel(CharacterDto character, Language lang){
        TermNodeDto propertyNode = character.getProperty();
        TermNodeDto structureNode = character.getStructure();
        TermNodeDto ratioToNode = character.getRatioTo();
        Representation structureRepresentation = structureNode.getTerm().getRepresentation(lang);
        if(structureRepresentation==null){
            structureRepresentation = structureNode.getTerm().getRepresentation(Language.DEFAULT());
        }
        Representation propertyRepresentation = propertyNode.getTerm().getRepresentation(lang);
        if(propertyRepresentation==null){
            propertyRepresentation = propertyNode.getTerm().getRepresentation(Language.DEFAULT());
        }

        String abbrevLabel = null;
        if(structureRepresentation!=null && propertyRepresentation!=null){
            if(structureRepresentation.getAbbreviatedLabel()!=null && propertyRepresentation.getAbbreviatedLabel()!=null){
                abbrevLabel = structureRepresentation.getAbbreviatedLabel()+" "+propertyRepresentation.getAbbreviatedLabel();
            }
        }

        if (character.getRatioTo() != null){
            Representation ratioToRepresentation = ratioToNode.getTerm().getRepresentation(lang);
            if(ratioToRepresentation==null){
                ratioToRepresentation = ratioToNode.getTerm().getRepresentation(Language.DEFAULT());
            }
            if(structureRepresentation!=null && propertyRepresentation!=null){

                if(structureRepresentation.getAbbreviatedLabel()!=null && propertyRepresentation.getAbbreviatedLabel()!=null && ratioToRepresentation.getAbbreviatedLabel() != null){
                    abbrevLabel = propertyRepresentation.getAbbreviatedLabel() + " ratio " +structureRepresentation.getAbbreviatedLabel()+ " to " + ratioToRepresentation.getAbbreviatedLabel();
                }
            }

        }

        return abbrevLabel;
    }
    public static String buildLabel(CharacterDto character, Language lang){
        TermNodeDto propertyNode = character.getProperty();
        TermNodeDto structureNode = character.getStructure();
        TermNodeDto ratioToNode = character.getRatioTo();
        Representation structureRepresentation = structureNode.getTerm().getRepresentation(lang);
        if(structureRepresentation==null){
            structureRepresentation = structureNode.getTerm().getRepresentation(Language.DEFAULT());
        }
        Representation propertyRepresentation = propertyNode.getTerm().getRepresentation(lang);
        if(propertyRepresentation==null){
            propertyRepresentation = propertyNode.getTerm().getRepresentation(Language.DEFAULT());
        }
        String label = null;

        if(structureRepresentation!=null && propertyRepresentation!=null){
            if(structureRepresentation.getLabel()!=null && propertyRepresentation.getLabel()!=null){
                label = structureRepresentation.getLabel()+" "+propertyRepresentation.getLabel();
            }

        }
        if(label!=null){
            //default label
            label = structureNode.getTerm().getRepresentation_L10n()+" "+propertyNode.getTerm().getRepresentation_L10n();
        }
        if (character.getRatioTo() != null){
            Representation ratioToRepresentation = ratioToNode.getTerm().getRepresentation(lang);
            if(ratioToRepresentation==null){
                ratioToRepresentation = ratioToNode.getTerm().getRepresentation(Language.DEFAULT());
            }
            if(structureRepresentation!=null && propertyRepresentation!=null){
                if(structureRepresentation.getLabel() != null && propertyRepresentation.getLabel() != null && ratioToRepresentation.getLabel() != null){
                    label = propertyRepresentation.getLabel() + " ratio " +structureRepresentation.getLabel()+" to " +ratioToRepresentation.getLabel() ;
                }

            }
            if(label==null){
                //default label
                label = propertyNode.getTerm().getRepresentation_L10n()+ " ratio " +structureNode.getTerm().getRepresentation_L10n()+ " to " + ratioToNode.getTerm().getRepresentation_L10n() ;
            }
        }

        return label;
    }

}
