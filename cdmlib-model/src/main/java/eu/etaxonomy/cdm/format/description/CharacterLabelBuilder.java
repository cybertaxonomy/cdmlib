/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * @author k.luther
 * @since Apr 20, 2021
 */
public class CharacterLabelBuilder {

    public static final CharacterLabelBuilder NewDefaultInstance(){
        return new CharacterLabelBuilder();
    }

    private CharacterLabelBuilder(){}

    public static String buildLabel(Character character, Language lang){
        TermNode<DefinedTerm> propertyNode = character.getProperty();
        TermNode<DefinedTerm> structureNode = character.getStructure();
        TermNode<DefinedTerm> ratioToNode = character.getRatioToStructure();
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
            label = structureNode.getTerm().getLabel()+" "+propertyNode.getTerm().getLabel();
        }
        if (character.getRatioToStructure() != null){
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
                label = propertyNode.getTerm().getLabel()+ " ratio " +structureNode.getTerm().getLabel()+ " to " + ratioToNode.getTerm().getLabel() ;
            }
        }

        return label;
    }


    public static String buildAbbrevLabel(Character character, Language lang){
        TermNode<DefinedTerm> propertyNode = character.getProperty();
        TermNode<DefinedTerm> structureNode = character.getStructure();
        TermNode<DefinedTerm> ratioToNode = character.getRatioToStructure();
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

        if (character.getRatioToStructure() != null){
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







}
