package eu.etaxonomy.cdm.persistence.query;

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

public class GroupByCount extends Grouping {

	public GroupByCount(String name, SortOrder order) {
		super("", name, null, order);
	}
	
	@Override
	public void addProjection(ProjectionList projectionList) {
		projectionList.add(Projections.rowCount(),name);
	}
	
	@Override
	public String getAssociatedObj() {
		return null;
	}

}
