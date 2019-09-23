/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import java.net.URI;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.description.Character;

/**
 *
 * @author pplitzner
 * @since Aug 28, 2019
 *
 */
@Component("characterOwlImport")
public class CharacterOwlImport extends CdmImportBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CharacterOwlImport.class);


    @Override
    protected boolean doCheck(StructureTreeOwlImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }

    @Override
    public void doInvoke(StructureTreeOwlImportState state) {
        URI source = state.getConfig().getSource();
        state.getModel().read(source.toString());
        ResIterator iterator = state.getModel().listResourcesWithProperty(OwlUtil.propIsA, OwlUtil.CHARACTER);
        while(iterator.hasNext()){
            Resource characterResource = iterator.next();
            Character character = OwlImportUtil.findTerm(Character.class, characterResource, this, state.getModel(), state);
            character = (Character) getTermService().load(character.getUuid(), Arrays.asList(new String[] {
                    "structure",
                    "structure.term",
                    "property",
                    "property.term",
                    "structureModifier",
                    "propertyModifier",
                    "recommendedModifierEnumeration",
                    "recommendedStatisticalMeasures",
                    "recommendedMeasurementUnits",
                    "supportedCategoricalEnumerations",
                    }));
            OwlImportUtil.addCharacterProperties(character, characterResource, this, state.getModel(), state);
            getTermService().saveOrUpdate(character);
        }
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlImportState state) {
        return false;
    }

}
