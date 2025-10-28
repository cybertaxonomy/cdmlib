/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @date 2022-03-05
 */
public class CollectorParser {

    private static final Logger logger = LogManager.getLogger();

    private static final String etAl = "\\set\\s+al\\.?";
    private static final String team = ".+\\s*(&.+|"+etAl+")";
    private static final Pattern teamRe = Pattern.compile(team);
    private static final String initialChars = "[A-Z"+UTF8.CAPITAL_A_ACUTE
            + UTF8.CAPITAL_E_ACUTE
            + UTF8.CAPITAL_I_ACUTE
            + UTF8.CAPITAL_O_ACUTE
            + UTF8.CAPITAL_U_ACUTE
            + "]";
    private static String initialWithSep = initialChars+"\\.(\\s|"+UTF8.ANY_DASH_RE()+")?";
    private static String initialsRe = "("+initialWithSep+"|(de(l|\\sla|\\slos)?|v[ao]n)\\s*){1,5}";

    private static CollectorParser singleton;
    public static final CollectorParser Instance() {
        if (singleton == null) {
            singleton = new CollectorParser();
        }
        return singleton;
    }

    public ParserResult<TeamOrPersonBase<?>> parse(String authorStr) {

        ParserResult<TeamOrPersonBase<?>> parserResult = new ParserResult<TeamOrPersonBase<?>>();

        TeamOrPersonBase<?> result;
        if (StringUtils.isBlank(authorStr)) {
            return parserResult;
        } else {
            authorStr = authorStr.trim();
        }

        Matcher matcher = teamRe.matcher(authorStr);
        if (matcher.matches()) {
            Team team = Team.NewInstance();
            result = team;
            String lastMember = matcher.group(1);
            List<Person> members = getMembers(authorStr.substring(0, authorStr.replace(lastMember, "").length()), parserResult);
            members.stream().forEach(m->team.addTeamMember(m));
            if (lastMember.matches(etAl) || lastMember.matches("\\s*&\\s*al\\.?")) {
                team.setHasMoreMembers(true);
            }else {
                lastMember = lastMember.substring(1).trim();
                members = getMembers(lastMember, parserResult);
                //TODO this should be only 1 Person so we may call single person directly
                members.stream().forEach(m->team.addTeamMember(m));
            }
        }else {
            List<Person> members = getMembers(authorStr, parserResult);
            if (members.size() == 1) {
                result = members.get(0);
            }else {
                Team team = Team.NewInstance();
                result = team;
                members.stream().forEach(m->team.addTeamMember(m));
            }
        }
        parserResult.setEntity(result);
        return parserResult;
    }

    private List<Person> getMembers(String membersStr, ParserResult<TeamOrPersonBase<?>> parserResult) {

        List<Person> result = new ArrayList<>();

        String[] split = membersStr.split(",");

        boolean isLast = false;
//        boolean lastWasFamily;
        for (int i = 0; i<split.length; i++) {
            Person person = Person.NewInstance();
            isLast = i >= split.length-1;
            String str = split[i].trim();

            Pattern initialsPattern = Pattern.compile("(" + initialsRe + ")(.*)$");
            Matcher matcher = initialsPattern.matcher(str);
            if (matcher.matches()) {
                //initials not separated by comma
                String initials = matcher.group(1).trim();
                person.setInitials(initials);
                String family = matcher.group(matcher.groupCount()); //str.replaceAll(initials + "$", "").trim();
                person.setFamilyName(family);
            }else {
                if (isLast) {
                    String msg = "Collector could not be parsed: "+ str;
                    parserResult.addWarning(msg);
                    logger.info(msg);
                    person.setTitleCache(str, true);
                }else {
                    String next = split[i+1].trim();
                    if (next.matches(initialsRe)) {
                        person.setFamilyName(str);
                        person.setInitials(next.trim());
                        i++;
                        while(i+1 < split.length && split[i+1].trim().matches(initialsRe)) {
                             next = split[i+1].trim();
                             person.setInitials(next.trim());
                             i++;
                        }
                    }else {
                        String msg = "Collector could not be parsed: "+ str;
                        parserResult.addWarning(msg);
                        logger.info(msg);
                        person.setTitleCache(str, true);
                    }
                }
            }
            result.add(person);
        }
        return result;
    }
}