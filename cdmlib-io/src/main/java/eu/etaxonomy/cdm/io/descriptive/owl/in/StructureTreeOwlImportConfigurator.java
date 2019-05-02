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

import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class StructureTreeOwlImportConfigurator extends ImportConfiguratorBase<StructureTreeOwlImportState, URI> {

    private static final long serialVersionUID = -7981427548996602252L;

    private TermVocabulary vocabulary;

    public static StructureTreeOwlImportConfigurator NewInstance(URI source){
        return new StructureTreeOwlImportConfigurator(source);
    }

    protected StructureTreeOwlImportConfigurator(URI source) {
        super(null);
        this.setSource(source);
        Reference reference = ReferenceFactory.newGeneric();
        reference.setTitle("StructureTree import from "+source);
        this.setSourceReference(reference);
    }

    @Override
    public StructureTreeOwlImportState getNewState() {
        return new StructureTreeOwlImportState(this);
    }

    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                StructureTreeOwlImport.class
        };
    }

    @Override
    public Reference getSourceReference() {
        return sourceReference;
    }

    public TermVocabulary getVocabulary(TermType termType, String vocLabel) {
        if(vocabulary==null){
            vocabulary = TermVocabulary.NewInstance(termType);
            vocabulary.setLabel(vocLabel);
        }
        return vocabulary;
    }

}
