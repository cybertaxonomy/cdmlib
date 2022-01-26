/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import java.util.ArrayList;
import java.util.List;

/**
 * @author a.mueller
 * @since 21.10.2018
 *
 */
public class MatchResult {

    private static final String NULL = "null";
    private static final String CLAZZ = "clazz";
    private static final String TYPE = "type";

    public static MatchResult SUCCESS(){
        return new MatchResult();
    }
//    public static MatchResult NULL(String fieldName) {
//        MatchResult result = new MatchResult();
//        result.addNonMatching(fieldName, null);
//        return result;
//    }
//    public static MatchResult EQUAL(String fieldName) {
//        MatchResult result = new MatchResult();
//        result.addNonMatching(fieldName, MatchMode.EQUAL);;
//        return result;
//    }
    public static MatchResult NewInstance(String fieldName, MatchMode matchMode, Object value1, Object value2) {
        MatchResult result = new MatchResult();
        result.addNonMatching(fieldName, matchMode, value1, value2);
        return result;
    }
    public static MatchResult NewNoTypeInstance(Enum<?> type1, Enum<?> type2) {
        MatchResult result = new MatchResult();
        result.addNoTypeMatching(type1, type2);
        return result;
    }

    private class NonMatching{
        private NonMatching(String path, MatchMode matchMode, Object value1, Object value2) {
            this.path = path;
            this.matchMode = matchMode;
            this.value1 = value1;
            this.value2 = value2;
        }
        String path;
        MatchMode matchMode;
        Object value1;
        Object value2;
        @Override
        public String toString() {
            return "NonMatching [path=" + path + ", matchMode=" + matchMode + "]";
        }
    }

    private String currentPath = "";
    private List<NonMatching> nonMatchings = new ArrayList<>();
//    private List<String> nonMatchingPasses = new ArrayList<>();
//    private List<MatchMode> nonMatchingMatchModes = new ArrayList<>();


    public boolean isSuccessful(){
        return nonMatchings.isEmpty();
    }

    private void addNonMatching(String fieldName, MatchMode matchMode, Object value1, Object value2){
        nonMatchings.add(new NonMatching(fieldName, matchMode, value1, value2));
//        nonMatchingPasses.add(currentPath + "." + fieldName);
//        nonMatchingMatchModes.add(matchMode);
    }

    /**
     * @param fieldMatcher
     */
    public void addNonMatching(FieldMatcher fieldMatcher, Object value1, Object value2) {
        addNonMatching(fieldMatcher.getPropertyName(), fieldMatcher.getMatchMode(), value1, value2);
    }

    public void addNullMatching(Object value1, Object value2) {
        addNonMatching(NULL, null, value1, value2);
    }

    public void addNoClassMatching(Class<?> clazz1, Class<?> clazz2) {
        addNonMatching(CLAZZ, MatchMode.EQUAL_REQUIRED, clazz1, clazz2);
    }

    public void addNoTypeMatching(Enum<?> type1, Enum<?> type2) {
        addNonMatching(TYPE, MatchMode.EQUAL_REQUIRED, type1, type2);
    }

    /**
     * @param clazz
     */
    public void addClass(Class<? extends IMatchable> clazz) {
        currentPath += "[" +  clazz.getSimpleName()+"]";
    }

    /**
     * @return
     */
    public boolean isFailed() {
        return !isSuccessful();
    }

    /**
     * @param string
     */
    public void addPath(String path) {
        currentPath += "." + path;
    }

    /**
     *
     */
    public void removePath() {
        if (currentPath.equals("")){
            throw new IllegalStateException("Path can not be removed. Is already empty");
        }
        int index = currentPath.lastIndexOf(".");
        if (index == -1){
            currentPath = "";
        }else{
            currentPath = currentPath.substring(0, index);
        }
    }
    /**
     * @param fieldResult
     */
    public void addSubResult(MatchResult subResult) {
        for (int i = 0; i < subResult.nonMatchings.size(); i++ ){
            NonMatching subNonMatching = subResult.nonMatchings.get(i);
            subNonMatching.path = currentPath + (subNonMatching.path.startsWith(".")? "":".")+ subNonMatching.path;
            nonMatchings.add(subNonMatching);
//            nonMatchingPasses.add((currentPath + "" + subResult.nonMatchingPasses.get(i)));
//            nonMatchingMatchModes.add(subResult.nonMatchingMatchModes.get(i));
        }
    }

    public void addSubResult(String fieldName, MatchResult subResult) {
        addPath(fieldName);
        addSubResult(subResult);
        removePath();
    }

    @Override
    public String toString() {
        String result = isSuccessful()? "SUCCESS" : "FAIL [";
        for (int i = 0; i < nonMatchings.size(); i++ ){
            if (i > 0){
                result += "\n      ";
            }
            NonMatching nonMatch = nonMatchings.get(i);
            result += nonMatch.path;
            String matchMode = nonMatch.matchMode == null? "NULL" : nonMatch.matchMode.toString();
            result += "(" + matchMode + "): ";
            result += nonMatch.value1 == null ? "null": nonMatch.value1.toString();
            result += " <-> ";
            result += nonMatch.value2 == null ? "null": nonMatch.value2.toString();

        }
        if (isFailed()){
            result += "]";
        }
        return result;
    }

}
