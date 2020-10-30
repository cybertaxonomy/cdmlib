/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import  eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.CharacterDto;
import eu.etaxonomy.cdm.persistence.dto.CharacterNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermTreeDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;


/**
 * @author k.luther
 * @since Oct 30, 2020
 */
public class TermNodeServiceImplTest  extends CdmTransactionalIntegrationTest{

    @SpringBeanByType
    private ITermNodeService termNodeService;

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private ITermService termService;


    @Test
    public void testSaveCharacterNode_supportedData() {
        DefinedTerm structure = DefinedTerm.NewInstance(TermType.Structure);
        TermTree<DefinedTerm> structureTree = TermTree.NewInstance(TermType.Structure);
        TermNode<DefinedTerm> nodeStructure = structureTree.getRoot().addChild(structure);

        DefinedTerm property = DefinedTerm.NewInstance(TermType.Property);
        TermTree<DefinedTerm> propertyTree = TermTree.NewInstance(TermType.Property);
        TermNode<DefinedTerm> nodeProperty = propertyTree.getRoot().addChild(property);
        termService.saveOrUpdate(property);
        termService.saveOrUpdate(structure);
        termTreeService.saveOrUpdate(structureTree);
        termTreeService.saveOrUpdate(propertyTree);

        TermTree<Feature> characterTree = TermTree.NewInstance(TermType.Feature);
        UUID characterTreeUuid = characterTree.getUuid();
        Character character = Character.NewInstance(nodeStructure, nodeProperty);
        character.setSupportsCategoricalData(false);

        characterTree.getRoot().addChild(character);
        termService.saveOrUpdate(character);
        termTreeService.saveOrUpdate(characterTree);
        termTreeService.getSession().flush();
        TermTreeDto dto = termTreeService.getTermTreeDtoByUuid(characterTreeUuid);
        List<TermNodeDto> children = dto.getRoot().getChildren();
        CharacterNodeDto nodeDto = (CharacterNodeDto) children.get(0);
        TermDto termDto = nodeDto.getTerm();
        if (termDto instanceof CharacterDto){
            CharacterDto characterDto = (CharacterDto) termDto;
            characterDto.setSupportsCategoricalData(true);
            List<CharacterNodeDto> dtos = new ArrayList<>();
            dtos.add(nodeDto);
            termNodeService.saveCharacterNodeDtoList(dtos);
            commitAndStartNewTransaction();
            characterTree = termTreeService.load(characterTreeUuid);
            List<TermNode<Feature>> childNodes = characterTree.getRoot().getChildNodes();
            TermNode<Feature> child = childNodes.get(0);

//            Assert.assertTrue(child.getTerm().isSupportsCategoricalData());

        }else{
            Assert.fail();
        }
    }

    @Test
    public void testSaveCharacterNode_representation() {
        DefinedTerm structure = DefinedTerm.NewInstance(TermType.Structure);
        TermTree<DefinedTerm> structureTree = TermTree.NewInstance(TermType.Structure);
        TermNode<DefinedTerm> nodeStructure = structureTree.getRoot().addChild(structure);

        DefinedTerm property = DefinedTerm.NewInstance(TermType.Property);
        TermTree<DefinedTerm> propertyTree = TermTree.NewInstance(TermType.Property);
        TermNode<DefinedTerm> nodeProperty = propertyTree.getRoot().addChild(property);
        termService.saveOrUpdate(property);
        termService.saveOrUpdate(structure);
        termTreeService.saveOrUpdate(structureTree);
        termTreeService.saveOrUpdate(propertyTree);

        TermTree<Feature> characterTree = TermTree.NewInstance(TermType.Feature);
        UUID characterTreeUuid = characterTree.getUuid();
        Character character = Character.NewInstance(nodeStructure, nodeProperty);
        character.setSupportsCategoricalData(false);

        characterTree.getRoot().addChild(character);
        termService.saveOrUpdate(character);
        termTreeService.saveOrUpdate(characterTree);
        termTreeService.getSession().flush();
        TermTreeDto dto = termTreeService.getTermTreeDtoByUuid(characterTreeUuid);
        List<TermNodeDto> children = dto.getRoot().getChildren();
        CharacterNodeDto nodeDto = (CharacterNodeDto) children.get(0);
        TermDto termDto = nodeDto.getTerm();
        if (termDto instanceof CharacterDto){
            CharacterDto characterDto = (CharacterDto) termDto;
            Representation rep = characterDto.getPreferredRepresentation(Language.DEFAULT());
            if (rep != null){
                rep.setText("Test");
            }else{
                rep = Representation.NewInstance("Test", "", "", Language.DEFAULT());
                characterDto.addRepresentation(rep);
            }
            List<CharacterNodeDto> dtos = new ArrayList<>();
            dtos.add(nodeDto);
            termNodeService.saveCharacterNodeDtoList(dtos);
            commitAndStartNewTransaction();
            characterTree = termTreeService.load(characterTreeUuid);
            List<TermNode<Feature>> childNodes = characterTree.getRoot().getChildNodes();
            TermNode<Feature> child = childNodes.get(0);

            Assert.assertTrue(child.getTerm().getPreferredRepresentation(Language.DEFAULT()).getText().equals("Test"));

        }else{
            Assert.fail();
        }




    }



    @Override
    public void createTestDataSet() throws FileNotFoundException {

    }

}
