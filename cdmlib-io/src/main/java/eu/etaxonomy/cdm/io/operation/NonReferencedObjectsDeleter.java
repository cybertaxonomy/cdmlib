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
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author k.luther
 * @since 2015
 */
@Component
public class NonReferencedObjectsDeleter extends CdmImportBase<NonReferencedObjectsDeleterConfigurator, DefaultImportState<NonReferencedObjectsDeleterConfigurator>> {

    private static final long serialVersionUID = -3514276133181062270L;

    @Override
	protected void doInvoke(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {

        doReferences(state);
        doTeams(state);
        doPersons(state);
	}

    private void doPersons(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        doAgents(state, Person.class, "Peson");
    }

    private void doTeams(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        doAgents(state, Team.class, "Team");
    }

    private void doAgents(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state, Class<? extends TeamOrPersonBase<?>> clazz, String label) {

        List<? extends TeamOrPersonBase<?>> authors =getAgentService().list(clazz, null, null, getOrderHint(), null);

        int deleted = 0;
        System.out.println("There are " + authors.size() + " " + label + "s.");
        for (TeamOrPersonBase<?> author: authors){
            long refObjectsCount = getCommonService().getReferencingObjectsCount(author);
            if (refObjectsCount == 0) {
                if (!state.getConfig().isDoOnlyReport()){
                    DeleteResult result = getAgentService().delete(author);
                    if (!result.isOk()){
                        System.out.println(label + " " + author.getTitleCache() + " with id " + author.getId() + " could not be deleted.");
                        result = null;
                    }else{
                        deleted++;
                        System.out.println("Deleted: " + author.getTitleCache() + "; id = " + author.getId());
                    }
                }else{
                    System.out.println(label + " to delete: " + author.getTitleCache() + "; id = " + author.getId());
                }
            }
        }
        System.out.println(deleted + " authors are deleted.");
    }


    private void doReferences(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
        String label = "reference";
        if (state.getConfig().isDoReferences()){
            List<String> propertyPath = new ArrayList<>();
            propertyPath.add("sources.citation");
            propertyPath.add("createdBy");

            List<Reference> references =getReferenceService().list(Reference.class, null, null, getOrderHint(), propertyPath);

            int deleted = 0;
            System.out.println("There are " + references.size() + " " + label + "s");
            for (Reference ref: references){
                long refObjects = getCommonService().getReferencingObjectsCount(ref);
                if (refObjects == 0) {
                    if (isIgnore(state, ref)){
                        System.out.println("Ignore: " + ref.getId() + "\t" + ref.getType() + "\t" +ref.getTitleCache() + "\t" + ref.getCreated()+ "\t" +
                                (ref.getCreatedBy() == null? "" : ref.getCreatedBy().getUsername()) + "\t" +
                                ref.getUpdated() + "\t" +  getSources(ref));
                    }else{
                        if (!state.getConfig().isDoOnlyReport()){
                            DeleteResult result = getReferenceService().delete(ref);
                            if (!result.isOk()){
                                System.out.println(label + " " + ref.getTitle() + " with id " + ref.getId() + " could not be deleted.");
                                result = null;
                            }else{
                                deleted++;
                                //System.out.println("Deleted: " + ref.getTitleCache() + "; id = " + ref.getId());
                            }
                        }else{
                            System.out.println(label + " to delete: " + ref.getTitleCache() + "; id = " + ref.getId());
                        }
                    }
                }
            }
            System.out.println(deleted + " " + label + "s are deleted.");
        }
    }

    private boolean isIgnore(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state, Reference ref) {
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

    private List<OrderHint> getOrderHint() {
        List<OrderHint> orderHint = new ArrayList<>();
        orderHint.add(OrderHint.ORDER_BY_ID);
        return orderHint;
    }

    @Override
	protected boolean doCheck(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
		return true;
	}

	@Override
	protected boolean isIgnore(DefaultImportState<NonReferencedObjectsDeleterConfigurator> state) {
		return false;
	}
}
