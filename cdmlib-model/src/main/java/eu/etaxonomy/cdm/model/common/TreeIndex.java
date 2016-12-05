/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * A class to handle tree indexes as used in {@link TaxonNode}, {@link FeatureNode}
 * etc.<BR>
 * Might be come a hibernate user type in future.
 *
 * @author a.mueller
 * @date 02.12.2016
 *
 */
public class TreeIndex {

    public static TreeIndex NewInstance(String treeIndex){
        return new TreeIndex(treeIndex);
    }


    /**
     * @param stringList
     * @return
     */
    public static List<TreeIndex> NewListInstance(List<String> stringList) {
        List<TreeIndex> result = new ArrayList<>();
        for (String string: stringList){
            result.add(new TreeIndex(string));
        }
        return result;
    }

    private static String regEx = "#[a-z](\\d+#)+";
    private static Pattern pattern = Pattern.compile(regEx);

    private static TreeIndexComparator comparator = new TreeIndexComparator();

    private String treeIndex;

    private TreeIndex(String treeIndex){
        if (! pattern.matcher(treeIndex).matches()){
            throw new IllegalArgumentException("Given string is not a valid tree index");
        }
        this.treeIndex = treeIndex;
    }

// ************** METHODS ***************************************/

    /**
     * @param taxonTreeIndex
     * @return
     */
    public boolean hasChild(TreeIndex childCandidateTreeIndex) {
        return childCandidateTreeIndex.treeIndex.startsWith(treeIndex);
    }


    /**
     * Returns a new TreeIndex instance which represents the parent of this tree index.
     * Returns null if this tree index already represents the root node of the tree.
     * @return
     */
    public TreeIndex parent(){
        int index = treeIndex.substring(0, treeIndex.length()-1).lastIndexOf(ITreeNode.separator);
        try {
            TreeIndex result = index < 0 ? null : NewInstance(treeIndex.substring(0, index+1));
            return result;
        } catch (Exception e) {
            //it is not a valid treeindex anymore
            return null;
        }
    }

// ********************** STATIC METHODS  *****************************/

    /**
     * Creates a list for the given tree indexes and sorts them in ascending
     * order.
     * @param treeIndexSet
     * @return
     */
    public static List<TreeIndex> sort(Collection<TreeIndex> treeIndexSet) {
        List<TreeIndex> result = new ArrayList<>(treeIndexSet);
        Collections.sort(result, comparator);
        return result;
    }


    /**
     * Creates a list for the given tree indexes and sorts them in descending
     * order.
     * @param treeIndexSet
     * @return
     */
    public static List<TreeIndex> sortDesc(Collection<TreeIndex> treeIndexSet) {
        List<TreeIndex> result = sort(treeIndexSet);
        Collections.reverse(result);
        return result;
    }

    public static Map<TreeIndex, TreeIndex> group(Collection<TreeIndex> groupingIndexes, Collection<TreeIndex> toBeGroupedIndexes){

        //for larger groupingIndexes we could optimize this by sorting both collections
        //prior to loop. This way we do traverse both lists once
        Map<TreeIndex, TreeIndex> result = new HashMap<>();
        List<TreeIndex> descSortedGroupingIndexes = sortDesc(groupingIndexes);

        for (TreeIndex toBeGrouped : toBeGroupedIndexes) {
            boolean groupFound = false;
            for (TreeIndex groupingIndex : descSortedGroupingIndexes){
                if (groupingIndex.hasChild(toBeGrouped)){
                    result.put(toBeGrouped, groupingIndex);
                    groupFound = true;
                    break;
                }
            }
            if (!groupFound){
                result.put(toBeGrouped, null);
            }
        }
        return result;
    }


// **************************** EQUALS *****************************/

    @Override
    public int hashCode() {
        return treeIndex.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeIndex){
            return treeIndex.equals(((TreeIndex)obj).treeIndex);
        }else{
            return false;
        }
    }

// *************************** toString() ***********************



    @Override
    public String toString(){
        return treeIndex;
    }

    /**
     * Null save toString method.
     * @param treeIndex
     * @return
     */
    public static String toString(TreeIndex treeIndex) {
        return treeIndex == null? null: treeIndex.toString();
    }


    /**
     * @param treeIndexes
     * @return
     */
    public static List<String> toString(Collection<TreeIndex> treeIndexes) {
        List<String> result = new ArrayList<>();
        for (TreeIndex treeIndex : treeIndexes){
            result.add(treeIndex.toString());
        }
        return result;
    }

}
