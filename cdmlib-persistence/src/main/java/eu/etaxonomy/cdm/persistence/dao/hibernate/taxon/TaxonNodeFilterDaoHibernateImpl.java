/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.List;
import java.util.UUID;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;

/**
 * Preliminary implementation for testing filtered taxa
 * @author a.mueller
 *
 */
@Repository
public class TaxonNodeFilterDaoHibernateImpl extends CdmEntityDaoBase<TaxonNode> {

	public TaxonNodeFilterDaoHibernateImpl() {
		super(TaxonNode.class);
	}


	//maybe we will later want to have ordering included
	public List<UUID> listUuids(TaxonNodeFilter filter){
		String select = " SELECT m.uuid ";
		String from = "FROM TaxonNode m ";
		String nodeCondition = getNodeFilter(filter);


		String fullQuery = select + from + " WHERE " + nodeCondition;
		System.out.println(fullQuery);
		Query query = getSession().createQuery(fullQuery);
		List<UUID> list = castToUuidList(query.list());
		return list;
	}




	private String getNodeFilter(TaxonNodeFilter filter) {
		String result = "";
		List<LogicFilter<TaxonNode>> nodesFilter = filter.getTaxonNodesFilter();
		boolean isFirst = true;
		for (LogicFilter<TaxonNode> singleFilter : nodesFilter){
			String treeIndex = singleFilter.getTreeIndex();
			String op = isFirst ? "" : op2Hql(singleFilter.getOperator());
			result = String.format("(%s%s(m.treeIndex like '%s%%'))", result, op, treeIndex);
			System.out.println(result);
			isFirst = false;
		}
		return result;
	}


	private String op2Hql(Op op){
		return op == Op.NOT ? " AND NOT " : op.toString();
	}

//
//
//	public static void main(String[] args){
//		String r = String.format(" main.treeIndex like '%s%%'", "aa");
//		System.out.println(r);
//	}


	@SuppressWarnings("unchecked")
	private List<UUID> castToUuidList(List<?> queryList){
		return (List<UUID>) queryList;
	}

}
