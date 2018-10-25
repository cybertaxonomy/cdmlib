/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * This class is for implementing subclass specific match strategies for
 * second level parameters. E.g. if you want to implement a match strategy
 * for an author of a reference you need to define the match strategy
 * for persons AND the one for teams.
 * By default the implementation creates temporary matchings additional
 * to the one for the base class (here {@link TeamOrPersonBase}.
 * But these temporary matchings always use the default matching.
 * If one wants to change the default matching for subclass specific
 * parameters (e.g. family name) this is not possible.
 *
 * This is an adhoc implementation that may be replaced by a more generic implementation in future.
 * Especially undecided is the question how to handle class dependend match
 * modes for the same property path in short cut methods like {@link #getMatching()}
 * or {@link #getMatchMode(String)}. Are these needed at all?
 *
 *
 *
 * @author a.mueller
 * @since 15.10.2018
 */
public class SubClassMatchStrategy<T extends IMatchable> extends StrategyBase
        implements IMatchStrategy {

    private static final long serialVersionUID = 6546363555888196628L;

    final static UUID uuid = UUID.fromString("24039e29-1149-4685-a056-1fcd46d34bba");

    private Map<Class<? extends T>, IMatchStrategy> strategies = new HashMap<>();
    private Class<T> baseClass;
    private Matching matching = new Matching();

    public static <T extends IMatchable> SubClassMatchStrategy<T> NewInstance(Class<T> baseClass){
        return new SubClassMatchStrategy<>(baseClass);
    }

    @SafeVarargs
    public static <T extends IMatchable, S extends T> SubClassMatchStrategy<T> NewInstance(
                        Class<T> baseClass, Class<? extends T> ... subClasses ) throws MatchException{

        SubClassMatchStrategy<T> result = new SubClassMatchStrategy<>(baseClass);
        for (Class<? extends T> subClass : subClasses){
            result.addDefaultStrategyForClass(subClass);
        }
        return result;
    }

    private SubClassMatchStrategy(Class<T> baseClass) {
        if (baseClass == null || !IMatchable.class.isAssignableFrom(baseClass)){
            throw new IllegalArgumentException("Base class must not be null and must be assignable from IMatchable.");
        }
        this.baseClass = baseClass;
        try {
            putStrategy(baseClass, new DefaultMatchStrategy(baseClass));
        } catch (MatchException e) {
            //should not happen during call with single cache strategy
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public <S extends T> void addDefaultStrategyForClass(Class<S> clazz) throws MatchException{
        putStrategy(clazz, DefaultMatchStrategy.NewInstance(clazz));
    }

    public void putStrategy(Class<? extends T> clazz, IMatchStrategy strategy) throws MatchException{
        strategies.put(clazz, strategy);
        updateMatching();
    }

    /**
     * @throws MatchException
     *
     */
    private void updateMatching() throws MatchException {
        for (IMatchStrategy strategy : strategies.values()){
            matching = new Matching();
            List<FieldMatcher> fieldMatchers = strategy.getMatching().getFieldMatchers(false);
            for (FieldMatcher fieldMatcher : fieldMatchers){
                //FIXME differs?
                String propName = fieldMatcher.getPropertyName();
                if (matching.getFieldMatcher(propName) != null
                        && !matching.getFieldMatcher(propName).getMatchMode().equals(fieldMatcher.getMatchMode())){
                    throw new MatchException("Different match modes for properties with same name in subclasses not yet allowed.");
                }
                matching.addFieldMatcher(fieldMatcher);
            }
            List<CacheMatcher> cacheMatchers = strategy.getMatching().getCacheMatchers();

            for (CacheMatcher cacheMatcher : cacheMatchers){
                //FIXME differs?
//                String propName = cacheMatcher.getPropertyName();
//                if (matching.get CacheMatcher(propName) != null
//                        && !matching.getFieldMatcher(propName).getMatchMode().equals(fieldMatcher.getMatchMode())){
//                    throw new MatchException("Different match modes for properties with same name in subclasses not yet allowed.");
//                }
                matching.addCacheMatcher(cacheMatcher);
            }
        }
    }

    @Override
    protected UUID getUuid() {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchMode getMatchMode(String propertyName) {
        //Copied from DefaultMatchStrategy
        //We could also implement by iterating over strategies
        //TODO is this needed at all?
        FieldMatcher fieldMatcher = matching.getFieldMatcher(propertyName);
        return fieldMatcher == null ? defaultMatchMode : fieldMatcher.getMatchMode();
    }

    /**
     * {@inheritDoc}
     * @deprecated deprecated in this class, use {@link #setMatchMode(Class, String, MatchMode)}
     * instead.
     */
    @Deprecated
    @Override
    public void setMatchMode(String propertyName, MatchMode matchMode) throws MatchException {
        this.setMatchMode(propertyName, matchMode, null);
    }

    public void setMatchMode(Class<? extends T> clazz, String propertyName,
            MatchMode matchMode) throws MatchException {
        this.setMatchMode(clazz, propertyName, matchMode, null);
    }


    /**
     * {@inheritDoc}
     * * @deprecated deprecated in this class, use {@link #setMatchMode(String, MatchMode, IMatchStrategy)}
     * instead.
     */
    @Deprecated
    @Override
    public void setMatchMode(String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy)
            throws MatchException {
        boolean exists = false;
        for (IMatchStrategy strategy : strategies.values()){
            if (strategy.getMatchFieldPropertyNames().contains(propertyName)){
                try {
                    strategy.setMatchMode(propertyName, matchMode, matchStrategy);
                } catch (Exception e) {
                    throw new MatchException("Unexpected exception occurred when trying to set match mode.", e);
                }
                exists = true;
            }
        }
        if (!exists){
            throw new MatchException("Field "+propertyName+" not available in any of the registered match strategies.");
        }
     }


    /**
     * {@inheritDoc}
     * @throws MatchException
     */
    public void setMatchMode(Class<? extends T> clazz, String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy) throws MatchException{
        getBestMatchingStrategy(clazz).setMatchMode(propertyName, matchMode, matchStrategy);
    }

    /**
     * @param clazz
     */
    private IMatchStrategy getBestMatchingStrategy(Class<? extends T> clazz) {
        IMatchStrategy result = strategies.get(clazz);
        //TODO use subclass hierarchie
        if (result == null){
            result = getBaseClassStrategy();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends IMatchable> boolean invoke(S matchFirst, S matchSecond) throws MatchException {
        matchFirst = CdmBase.deproxy(matchFirst); //just in case
        Class<? extends IMatchable> clazz = matchFirst.getClass();
        IMatchStrategy strategy = strategies.get(clazz);
        if (strategy == null){
            strategy = getBaseClassStrategy();
        }
        return strategy.invoke(matchFirst, matchSecond);
    }

    /**
     * @return
     */
    private IMatchStrategy getBaseClassStrategy() {
        return strategies.get(baseClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matching getMatching() {
        //preliminary not yet implemented
        throw new RuntimeException("getMatching not yet implemented in " + getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getMatchClass() {
        //TODO or always return Set<Class>?
        //TODO needed at all?
        return baseClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getMatchFieldPropertyNames() {
        //preliminary not implemented. Simply aggregate, or change signature to xxx(clazz), or .. ?
        throw new RuntimeException("getMatchFieldPropertyNames not yet implemented in " + getClass().getName());
    }

}
