package eu.etaxonomy.cdm.io.common;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
@Component

public class DeleteNonReferencedreferencesUpdater extends CdmImportBase<DeleteNonReferencedReferencesConfigurator, DefaultImportState<DeleteNonReferencedReferencesConfigurator>> {


	@Override
	protected void doInvoke(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {

		if (state.getConfig().isDoAuthors()){
			List<TeamOrPersonBase> authors =getAgentService().list(TeamOrPersonBase.class, null, null, null, null);
			DeleteResult result;
			int deleted = 0;
			System.out.println("There are " + authors.size() + " authors");
			for (TeamOrPersonBase author: authors){
				Set<CdmBase> refObjects = getCommonService().getReferencingObjects(author);
				if (refObjects.isEmpty()) {
					result = getAgentService().delete(author);
					deleted++;
					if (!result.isOk()){
						System.out.println("Author " + author.getTitleCache() + " with id " + author.getId() + " could not be deleted.");
						result = null;
					}
				}
			}
			System.out.println(deleted + " authors are deleted.");
		}
		if (state.getConfig().isDoReferences()){
			List<Reference> references =getReferenceService().list(Reference.class, null, null, null, null);
			DeleteResult result;
			int deleted = 0;
			System.out.println("There are " + references.size() + " references");
			for (Reference ref: references){
				Set<CdmBase> refObjects = getCommonService().getReferencingObjects(ref);
				if (refObjects.isEmpty()) {
					result = getReferenceService().delete(ref);
					deleted++;
					if (!result.isOk()){
						System.out.println("Reference " + ref.getTitle() + " with id " + ref.getId() + " could not be deleted.");
						result = null;
					}
				}
			}
			System.out.println(deleted + " references are deleted.");
		}
	}

	@Override
	protected boolean doCheck(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isIgnore(
			DefaultImportState<DeleteNonReferencedReferencesConfigurator> state) {
		// TODO Auto-generated method stub
		return false;
	}


}
