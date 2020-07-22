/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.operation.config.DeleteNonReferencedReferencesConfigurator;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author k.luther
 * @since 2015
 */
@Component
public class DeleteNonReferencedReferencesUpdater extends CdmImportBase<DeleteNonReferencedReferencesConfigurator, DefaultImportState<DeleteNonReferencedReferencesConfigurator>> {

    private static final long serialVersionUID = -3514276133181062270L;

    @Override
	protected void doInvoke(DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {

        List<OrderHint> orderHint = new ArrayList<>();
        orderHint.add(OrderHint.ORDER_BY_ID);

		if (state.getConfig().isDoAuthors()){
			List<TeamOrPersonBase> authors =getAgentService().list(TeamOrPersonBase.class, null, null, orderHint, null);

			int deleted = 0;
			System.out.println("There are " + authors.size() + " authors");
			for (TeamOrPersonBase<?> author: authors){
				long refObjects = getCommonService().getReferencingObjectsCount(author);
				if (refObjects == 0) {
				    DeleteResult result = getAgentService().delete(author);
					deleted++;
					if (!result.isOk()){
						System.out.println("Author " + author.getTitleCache() + " with id " + author.getId() + " could not be deleted.");
						result = null;
					}else{
					    System.out.println("Deleted: " + author.getTitleCache() + "; id = " + author.getId());
					}
				}
			}
			System.out.println(deleted + " authors are deleted.");
		}

		List<String> propertyPath = new ArrayList<>();
		propertyPath.add("sources.citation");
		propertyPath.add("createdBy");
		if (state.getConfig().isDoReferences()){
			List<Reference> references =getReferenceService().list(Reference.class, null, null, orderHint, propertyPath);

			int deleted = 0;
			System.out.println("There are " + references.size() + " references");
			for (Reference ref: references){
				long refObjects = getCommonService().getReferencingObjectsCount(ref);
				if (refObjects == 0) {
				    if (isIgnore(state, ref)){
				        System.out.println("Ignore: " + ref.getId() + "\t" + ref.getType() + "\t" +ref.getTitleCache() + "\t" + ref.getCreated()+ "\t" +
				                (ref.getCreatedBy() == null? "" : ref.getCreatedBy().getUsername()) + "\t" +
				                ref.getUpdated() + "\t" +  getSources(ref));
				    }else{
				        DeleteResult result = getReferenceService().delete(ref);
				        deleted++;
				        if (!result.isOk()){
				            System.out.println("Reference " + ref.getTitle() + " with id " + ref.getId() + " could not be deleted.");
				            result = null;
				        }else{
//				            System.out.println("Deleted: " + ref.getTitleCache() + "; id = " + ref.getId());
				        }
				    }
				}
			}
			System.out.println(deleted + " references are deleted.");
		}
	}

    private boolean isIgnore(DefaultImportState<DeleteNonReferencedReferencesConfigurator> state, Reference ref) {
        if (state.getConfig().isKeepReferencesWithTitle() && isNotBlank(ref.getTitle())
                || state.getConfig().isKeepRisSources() && hasRISSource(ref)){
            return true;
        }else{
            return false;
        }
    }

    private String getSources(Reference ref) {
        String result = "";
        for (IdentifiableSource source : ref.getSources()){
            result += source.getType() + ": " + (source.getCitation() == null? "" : source.getCitation().getTitleCache()) + "\t";
        }
        return result;
    }

    private boolean hasRISSource(Reference ref) {
        for (IdentifiableSource source : ref.getSources()){
            Reference citation = source.getCitation();
            if (citation != null && citation.getTitleCache().startsWith("RIS Reference")){
                return true;
            }
        }
        return false;
    }

    @Override
	protected boolean doCheck(DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		return true;
	}

	@Override
	protected boolean isIgnore(DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		return false;
	}
}
