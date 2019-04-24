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

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.etaxonomy.cdm.io.common.CdmImportBase;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
@Component("owlImport")
public class OwlImport extends CdmImportBase<OwlImportConfigurator, OwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    @Override
    protected boolean doCheck(OwlImportState state) {
        return false;
    }

    @Override
    public void doInvoke(OwlImportState state) {
        URI source = state.getConfig().getSource();

        Model model = ModelFactory.createDefaultModel();
        model.read(source.toString());
        model.write(System.out);
    }

    @Override
    protected boolean isIgnore(OwlImportState state) {
        return false;
    }

}
