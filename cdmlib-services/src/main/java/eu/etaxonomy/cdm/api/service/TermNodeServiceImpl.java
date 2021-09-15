/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dto.CharacterDto;
import eu.etaxonomy.cdm.persistence.dto.CharacterNodeDto;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @since Jul 22, 2019
 */
@Service
@Transactional(readOnly = false)
public class TermNodeServiceImpl
        extends VersionableServiceBase<TermNode, ITermNodeDao>
        implements ITermNodeService {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermNodeServiceImpl.class);

	@Override
    @Autowired
	protected void setDao(ITermNodeDao dao) {
		this.dao = dao;
	}

	@Autowired
    private ITermService termService;

	@Autowired
	private IVocabularyService vocabularyService;

	@Override
    public List<TermNode> list(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths){
	    return dao.list(termType, limit, start, orderHints, propertyPaths);
	}

	@Override
	@Transactional(readOnly = false)
	public DeleteResult deleteNode(UUID nodeUuid, TermNodeDeletionConfigurator config) {
	    DeleteResult result = new DeleteResult();
        TermNode<Feature> node = CdmBase.deproxy(dao.load(nodeUuid));
	    result = isDeletable(nodeUuid, config);
	    if (result.isOk()){
	        TermNode<Feature> parent = node.getParent();
            parent = CdmBase.deproxy(parent, TermNode.class);
	        List<TermNode> children = new ArrayList<>(node.getChildNodes());

	        if (config.getChildHandling().equals(ChildHandling.DELETE)){

	            for (TermNode child: children){
	                deleteNode(child.getUuid(), config);
	               // node.removeChild(child);
	            }
	            if (parent != null){
	                parent.removeChild(node);
	            }

	        } else{

	            if (parent != null){
	                parent.removeChild(node);
	                for (TermNode child: children){
	                    node.removeChild(child);
	                    parent.addChild(child);
	                }
	            }else{
	                 result.setAbort();
	                 result.addException(new ReferencedObjectUndeletableException("The root node can not be deleted without its child nodes"));
	                 return result;
	             }
	         }

	         dao.delete(node);
	         result.addDeletedObject(node);
	         if(parent!=null){
	             result.addUpdatedObject(parent);
	         }
	         if (config.isDeleteElement()){
	             DefinedTermBase term = node.getTerm();
                 termService.delete(term.getUuid());
                 result.addDeletedObject(term);
             }
	     }
	     return result;
	 }
	 private UpdateResult createChildNode(UUID parentNodeUUID, UUID nodeUuid, DefinedTermBase term, UUID vocabularyUuid){
	     UpdateResult result =  createChildNode(parentNodeUUID, term, vocabularyUuid);
	     result.getCdmEntity().setUuid(nodeUuid);
	     return result;
	 }


	 @Override
     public UpdateResult createChildNode(UUID parentNodeUuid, DefinedTermBase term, UUID vocabularyUuid){
	     TermVocabulary vocabulary = vocabularyService.load(vocabularyUuid);

	     vocabulary.addTerm(term);
	     vocabularyService.save(vocabulary);
	     return addChildNode(parentNodeUuid, term.getUuid());
	 }

     @Override
     public UpdateResult addChildNode(UUID nodeUUID, UUID termChildUuid){
         return addChildNode(nodeUUID, termChildUuid, 0);
     }

	 @Override
	 public UpdateResult addChildNode(UUID nodeUUID, UUID termChildUuid, int position){
	     UpdateResult result = new UpdateResult();

	     TermNode node = load(nodeUUID);
	     if (node == null){
	         result.setError();
             result.addException(new Exception("The parent node does not exist."));
             return result;
	     }
	     DefinedTermBase child = HibernateProxyHelper.deproxy(termService.load(termChildUuid), DefinedTermBase.class);

	     if(node != null && node.getGraph() != null && !node.getGraph().isAllowDuplicates() && node.getGraph().getDistinctTerms().contains(child)){
	         result.setError();
	         result.addException(new Exception("This term tree does not allow duplicate terms."));
	         return result;
	     }

	     TermNode childNode;
         if(position<0) {
             childNode = node.addChild(child);
         }
         else{
             childNode = node.addChild(child, position);
         }
         save(childNode);
         result.addUpdatedObject(node);
         result.setCdmEntity(childNode);
         return result;
     }

	 @Override
	 public DeleteResult isDeletable(UUID nodeUuid, TermNodeDeletionConfigurator config){
	     TermNode<Feature> node = load(nodeUuid);
	     DeleteResult result = new DeleteResult();
	     Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(node);
	     for (CdmBase ref:references){
	         if (ref instanceof TermNode){
	             break;
	         }
	         if (ref instanceof TermTree){
	             TermTree<Feature> refTree = HibernateProxyHelper.deproxy(ref, TermTree.class);
	             if (node.getGraph().equals((refTree))){
	                 break;
	             }
	         }
	         result.setAbort();
	         result.addException(new ReferencedObjectUndeletableException("The featureNode is referenced by " + ref.getUserFriendlyDescription() +" with id " +ref.getId()));

	     }
	     return result;
	 }

    @Override
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid, int position) {
        UpdateResult result = new UpdateResult();
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("parent");
        propertyPaths.add("parent.children");
        propertyPaths.add("children");
        TermNode test = load(movedNodeUuid, propertyPaths);
        TermNode movedNode = CdmBase.deproxy(load(movedNodeUuid, propertyPaths), TermNode.class);
        TermNode targetNode = CdmBase.deproxy(load(targetNodeUuid, propertyPaths));
        TermNode parent = CdmBase.deproxy(movedNode.getParent());
        parent.removeChild(movedNode);
        if(position < 0){
            targetNode.addChild(movedNode);
        }
        else{
            targetNode.addChild(movedNode, position);
        }
        result.addUpdatedObject(targetNode);
        if(parent!=null){
            result.addUpdatedObject(parent);
        }
        result.setCdmEntity(movedNode);
        return result;
    }

    @Override
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid) {
        return moveNode(movedNodeUuid, targetNodeUuid, -1);
    }

    @Override
    public UpdateResult saveTermNodeDtoList(List<TermNodeDto> dtos){
        UpdateResult result = new UpdateResult();
        MergeResult<TermNode> mergeResult;
        List<UUID> uuids = new ArrayList<>();
        dtos.stream().forEach(dto -> uuids.add(dto.getUuid()));
        List<TermNode> nodes = dao.list(uuids, null, 0, null, null);
        //check all attributes for changes and adapt
        for (TermNode node: nodes){
            for (TermNodeDto dto: dtos){

                if (dto.getUuid().equals(node.getUuid())){
    //                only node changes, everything else will be handled by the operations/service methods
                    if (!dto.getInapplicableIf().equals(node.getInapplicableIf())){
                        node.getInapplicableIf().clear();
                        node.getInapplicableIf().addAll(dto.getInapplicableIf());
                    }
                    if (!dto.getOnlyApplicableIf().equals(node.getOnlyApplicableIf())){
                        node.getOnlyApplicableIf().clear();
                        node.getOnlyApplicableIf().addAll(dto.getOnlyApplicableIf());
                    }
                }
                mergeResult = dao.merge(node, true);
                result.addUpdatedObject(mergeResult.getMergedEntity());
            }
        }
        return result;
    }

    @Override
    public UpdateResult saveCharacterNodeDtoList(List<CharacterNodeDto> dtos){
        MergeResult<TermNode> mergeResult;
        UpdateResult result = new UpdateResult();
        List<UUID> nodeUuids = new ArrayList<>();

        dtos.stream().forEach(dto -> nodeUuids.add(dto.getUuid()));
        List<TermNode> nodes = dao.list(nodeUuids, null, 0, null, null);
        //check all attributes for changes and adapt
        for (TermNode<Character> node: nodes){
            for (CharacterNodeDto dto: dtos){
    //            TermNodeDto dto = dtoIterator.next();
                if (dto.getUuid().equals(node.getUuid())){

                    if (!dto.getInapplicableIf().equals(node.getInapplicableIf())){
                        node.getInapplicableIf().clear();
                        node.getInapplicableIf().addAll(dto.getInapplicableIf());
                    }
                    if (!dto.getOnlyApplicableIf().equals(node.getOnlyApplicableIf())){
                        node.getOnlyApplicableIf().clear();
                        node.getOnlyApplicableIf().addAll(dto.getOnlyApplicableIf());
                    }

                    Character character = null;
                    CharacterDto characterDto = (CharacterDto) dto.getTerm();
                    character = HibernateProxyHelper.deproxy(node.getTerm(), Character.class);
                    if (characterDto.getRatioTo() != null){
                        TermNode ratioToStructure = this.load(characterDto.getRatioTo().getUuid());
                        character.setRatioToStructure(ratioToStructure);
                    }else{
                        character.setRatioToStructure(null);
                    }

                    //supportsXXX
                    //TODO add all other supportsXXX (6 are missing)
                    character.setSupportsCategoricalData(characterDto.isSupportsCategoricalData());
                    character.setSupportsQuantitativeData(characterDto.isSupportsQuantitativeData());

                    //availableForXXX
                    character.setAvailableForTaxon(characterDto.isAvailableForTaxon());
                    character.setAvailableForOccurrence(characterDto.isAvailableForOccurrence());
                    character.setAvailableForTaxonName(characterDto.isAvailableForTaxonName());

//                  representations
                    for (Representation rep: dto.getTerm().getRepresentations()){
                        Representation oldRep = character.getRepresentation(rep.getLanguage());
                        if (oldRep == null){
                            oldRep = new Representation();
                            oldRep.setLanguage(rep.getLanguage());
                            character.addRepresentation(oldRep);
                        }
                        oldRep.setLabel(rep.getLabel());
                        oldRep.setAbbreviatedLabel(rep.getAbbreviatedLabel());
                        oldRep.setText(rep.getText());
                        oldRep.setPlural(rep.getPlural());
                    }
                    Set<Representation> deleteRepresentations = new HashSet<>();
                    if (character.getRepresentations().size() > dto.getTerm().getRepresentations().size()){
                        for (Representation rep: character.getRepresentations()){
                            if(dto.getTerm().getRepresentation(rep.getLanguage()) == null){
                                deleteRepresentations.add(rep);
                            }
                        }
                    }

                    if (!deleteRepresentations.isEmpty()){
                        for (Representation rep: deleteRepresentations){
                            character.removeRepresentation(rep);
                        }
                    }

//                  structural modifier
                    if (characterDto.getStructureModifier() != null){
                        DefinedTerm structureModifier = (DefinedTerm) termService.load(characterDto.getStructureModifier().getUuid());
                        character.setStructureModifier(structureModifier);
                    }else{
                        character.setStructureModifier(null);
                    }
//                  recommended measurement units
                    character.getRecommendedMeasurementUnits().clear();
                    List<UUID> uuids = new ArrayList<>();
                    for (TermDto termDto: characterDto.getRecommendedMeasurementUnits()){
                        uuids.add(termDto.getUuid());
                    }
                    List<DefinedTermBase> terms;
                    if (!uuids.isEmpty()){
                        terms = termService.load(uuids, null);
                        Set<MeasurementUnit> measurementUnits = new HashSet<>();
                        for (DefinedTermBase term: terms){
                            if (term instanceof MeasurementUnit){
                                measurementUnits.add((MeasurementUnit)term);
                            }
                        }
                        character.getRecommendedMeasurementUnits().addAll(measurementUnits);
                    }
//                  statistical measures
                    character.getRecommendedStatisticalMeasures().clear();
                    uuids = new ArrayList<>();
                    for (TermDto termDto: characterDto.getRecommendedStatisticalMeasures()){
                        uuids.add(termDto.getUuid());
                    }
                    if (!uuids.isEmpty()){
                        terms = termService.load(uuids, null);
                        Set<StatisticalMeasure> statisticalMeasures = new HashSet<>();
                        for (DefinedTermBase<?> term: terms){
                            if (term instanceof StatisticalMeasure){
                                statisticalMeasures.add((StatisticalMeasure)term);
                            }
                        }
                        character.getRecommendedStatisticalMeasures().addAll(statisticalMeasures);
                    }

//                  recommended mod. vocabularies
                    character.getRecommendedModifierEnumeration().clear();
                    uuids = new ArrayList<>();
                    for (TermVocabularyDto termDto: characterDto.getRecommendedModifierEnumeration()){
                        uuids.add(termDto.getUuid());
                    }
                    List<TermVocabulary> termVocs;
                    if (!uuids.isEmpty()){
                        termVocs = vocabularyService.load(uuids, null);
                        for (TermVocabulary voc: termVocs){
                            character.addRecommendedModifierEnumeration(voc);
                        }
                    }

//                  supported state vocabularies
                    character.getSupportedCategoricalEnumerations().clear();
                    uuids = new ArrayList<>();
                    for (TermVocabularyDto termDto: characterDto.getSupportedCategoricalEnumerations()){
                        uuids.add(termDto.getUuid());
                    }
                    if (!uuids.isEmpty()){
                        termVocs = vocabularyService.load(uuids, null);

                        for (TermVocabulary voc: termVocs){
                            character.addSupportedCategoricalEnumeration(voc);
                        }
                    }
                    node.setTerm(character);
                    mergeResult = dao.merge(node, true);
                    result.addUpdatedObject(mergeResult.getMergedEntity());
                }
            }
        }
        return result;
    }

    @Override
    public UpdateResult saveNewCharacterNodeDtoMap(Map<Character, CharacterNodeDto> dtos, UUID vocabularyUuid){
        UpdateResult result = new UpdateResult();
        UpdateResult resultLocal = new UpdateResult();
        for (Entry<Character, CharacterNodeDto> dtoEntry: dtos.entrySet()){
            resultLocal = createChildNode(dtoEntry.getValue().getParentUuid(), dtoEntry.getKey(), vocabularyUuid);
            dtoEntry.getValue().setUuid(resultLocal.getCdmEntity().getUuid());
            result.includeResult(resultLocal);
        }
        List<CharacterNodeDto> dtoList = new ArrayList<>(dtos.values());
        result.includeResult(saveCharacterNodeDtoList(dtoList));
        return result;

    }



}
