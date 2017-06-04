/**
 *
 */
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * If we have a OneToMany relationship which holds data which is not reusable or only reusable
 * within a certain parent class we may not want to have an MN table for handling the relationship
 * but a direct link (foreign key) from the child table to the parent table.
 * This class removes MN tables and replaces them by the direct link (if possible)
 *
 * Tested for: MySQL, H2, PostGres
 * Untested: SQL Server (currently not testable due to #4957)
 *
 * Open issues:
 *  - validation that no duplicate values exist (which will throw an exception during invoke)
 *
 * @author a.mueller
 * @date 2015-05-21
 */
public class MnTableRemover extends AuditedSchemaUpdaterStepBase {

	private final String fkColumnName;  //new column in child table which points to the parent table
	private final String mnParentFkColumnName;  //column in MN table which points to the parent table
	private final String mnChildFkColumnName;   //column in MN table which points to the child table
	private final String parentTableName;
	private final String childTableName;

	protected List<ISchemaUpdaterStep> innerStepList = new ArrayList<ISchemaUpdaterStep>();


	public static MnTableRemover NewInstance(String stepName, String mnTableName,
            String fkColumnName,
            String mnParentFkColumnName,
            String mnChildFkColumnName,
            String parentTableName,
            String childTableName,
            boolean includeAudited){

		MnTableRemover result = new MnTableRemover(stepName,
		        mnTableName, fkColumnName, mnParentFkColumnName,
		        mnChildFkColumnName, parentTableName, childTableName, includeAudited);
		return result;
	}

//	public static MnTableRemover NewMnInstance(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, boolean includeAudTable, boolean hasSortIndex, boolean secondTableInKey){
//		MnTableRemover result = new MnTableRemover(stepName, firstTableName, firstTableAlias, secondTableName, secondTableAlias, new String[]{}, new String[]{}, null, null, includeAudTable, hasSortIndex, secondTableInKey, false, false, false);
//		return result;
//	}


	protected MnTableRemover(String stepName, String mnTableName,
	        String fkColumnName,
	        String mnParentFkColumnName,
	        String mnChildFkColumnName,
	        String parentTableName,
	        String childTableName,
	        boolean includeAudited) {
	    super(stepName, mnTableName, includeAudited);
		this.fkColumnName = fkColumnName;
		this.mnParentFkColumnName = mnParentFkColumnName;
		this.mnChildFkColumnName = mnChildFkColumnName;
		this.parentTableName = parentTableName;
		this.childTableName = childTableName;

		makeSteps();
	}


    private void makeSteps() {
        String mnTableName = tableName;

        //TODO validate
        //mn child column must be unique

        //Create new column
        boolean notNull = false; //??
        ISchemaUpdaterStep step = ColumnAdder.NewIntegerInstance(
                "Create foreign key column to parent table to replace MN table",
                childTableName,
                fkColumnName,
                includeAudTable,
                notNull,
                parentTableName);
        innerStepList.add(step);


        //copy data to new column
        String stepName = "Copy data to new column";
        String childTable = "@@" + childTableName + "@@ c";
        String childTableAud = "@@" + childTableName + "_AUD@@ c";
        String mnTable = "@@" + mnTableName + "@@";
        String mnTableAud = "@@" + mnTableName + "_AUD@@";
        String sql = ""
                + " UPDATE " + childTable
                + " SET " + fkColumnName + " = "
                +       " (SELECT " + mnParentFkColumnName
                        + " FROM " + mnTable + " MN "
                        + " WHERE MN." + mnChildFkColumnName + " = c.id) ";
        String sqlAudited = ""
                + " UPDATE " + childTableAud
                + " SET " + fkColumnName + " = "
                +       " (SELECT " + mnParentFkColumnName
                        + " FROM " + mnTableAud + " MN "
                        + " WHERE MN." + mnChildFkColumnName + " = c.id AND c.REV = MN.REV) ";
        SimpleSchemaUpdaterStep dataUpdateStep = SimpleSchemaUpdaterStep.NewExplicitAuditedInstance(stepName, sql, sqlAudited, 99);
        innerStepList.add(dataUpdateStep);

        //delete old table
        step = TableDroper.NewInstance("Drop MN table", mnTableName, includeAudTable, true);
        innerStepList.add(step);

        return;
    }

    @Override
    protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType)  {
        //we only do have inner steps here
        return true;
    }


    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<ISchemaUpdaterStep>
                    ( this.innerStepList);
        return result;
    }
}

