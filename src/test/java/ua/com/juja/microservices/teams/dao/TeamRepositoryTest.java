package ua.com.juja.microservices.teams.dao;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.integration.BaseIntegrationTest;
import ua.com.juja.microservices.teams.entity.Team;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Ivan Shapovalov
 * @author Andrii Sidun
 */
@RunWith(SpringRunner.class)
public class TeamRepositoryTest extends BaseIntegrationTest {

    @Inject
    private TeamRepository teamRepository;

    @Test
    public void test_saveTeamExecutedCorrectly() {
        final String userInOneTeam = "user-in-one-team";
        final String userInSeveralTeams = "user-in-several-teams";
        final Team expected =
                new Team(new HashSet<>(Arrays.asList(userInOneTeam, "user1", "user2", userInSeveralTeams)));

        Team actual = teamRepository.saveTeam(expected);

        assertNotNull(actual);
        assertThat(actual.getMembers(), is(expected.getMembers()));
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json")
    public void test_getUserTeamsUserInOneTeamExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userInOneTeam = "user-in-one-team";
        final String userInSeveralTeams = "user-in-several-teams";
        final Team expected =
                new Team(new HashSet<>(Arrays.asList(userInOneTeam, "user1", "user2", userInSeveralTeams)));

        List<Team> actual = teamRepository.getUserActiveTeams(userInOneTeam, actualDate);

        assertEquals(actual.size(), 1);
        assertThat(actual.get(0).getMembers(), is(expected.getMembers()));
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void test_getUserTeamsIfUserInSeveralTeamsExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userInOneTeam = "user-in-one-team";
        final String userInSeveralTeams = "user-in-several-teams";
        final Team team1 =
                new Team(new LinkedHashSet<>(Arrays.asList(userInOneTeam, "user1", "user2", userInSeveralTeams)));
        final Team team2 =
                new Team(new LinkedHashSet<>(Arrays.asList(userInSeveralTeams, "user3", "user4", "user5")));
        final List<Team> expected = new ArrayList<>();
        expected.add(team1);
        expected.add(team2);

        List<Team> actual = teamRepository.getUserActiveTeams(userInSeveralTeams, actualDate);

        assertEquals(actual.size(), expected.size());
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getMembers(), is(expected.get(i).getMembers()));
        }
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void test_getUserTeamsIfUserNotInTeamExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userNotInTeam = "user-not-in-team";
        final List<Team> expected = new ArrayList<>();

        List<Team> actual = teamRepository.getUserActiveTeams(userNotInTeam, actualDate);

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void test_getUserTeamsIfUserInDeactivatedTeamsExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userInDeactivatedTeam = "user-in-deactivated-team";
        final List<Team> expected = new ArrayList<>();

        List<Team> actual = teamRepository.getUserActiveTeams(userInDeactivatedTeam, actualDate);

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void test_deactivateTeamExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userInOneTeam = "user-in-one-team";
        List<Team> teamsBefore = teamRepository.getUserActiveTeams(userInOneTeam, actualDate);
        assertEquals(1, teamsBefore.size());
        teamsBefore.get(0).setDeactivateDate(actualDate);

        Date newDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        teamRepository.saveTeam(teamsBefore.get(0));
        List<Team> teamsAfter = teamRepository.getUserActiveTeams(userInOneTeam, newDate);

        assertEquals(0, teamsAfter.size());
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json")
    public void test_checkUsersActiveTeamsSomeUserInSeveralTeamsExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userInOneTeam = "user-in-one-team";
        final String userInSeveralTeams = "user-in-several-teams";
        Set<String> members = new HashSet<>();
        members.add(userInOneTeam);
        members.add(userInSeveralTeams);
        final List<String> expected = new ArrayList<>();
        expected.add(userInOneTeam);
        expected.add(userInSeveralTeams);

        List<String> actual = teamRepository.checkUsersActiveTeams(members, actualDate);

        assertEquals(actual.size(), 2);
        assertThat(actual, is(expected));
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json")
    public void test_checkUsersActiveTeamsNoOneInSeveralTeamsReturnsEmptyList() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final String userNotOneTeam = "user-not-in-team";
        Set<String> members = new HashSet<>();
        members.add(userNotOneTeam);
        final List<String> expected = new ArrayList<>();

        List<String> actual = teamRepository.checkUsersActiveTeams(members, actualDate);

        assertEquals(actual.size(), 0);
        assertThat(actual, is(expected));
    }

    @Test
    @UsingDataSet(locations = "/datasets/getAllActiveTeamsDataSet.json")
    public void test_getAllActiveTeamsIfMongoTemplateReturnsNotNullTeamExecutedCorrectly() {
        Date actualDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        final Team team1 = new Team(new HashSet<>(Arrays.asList("user1", "user2", "user3", "user4")));
        final Team team2 = new Team(new HashSet<>(Arrays.asList("user5", "user6", "user7", "user8")));
        final List<Team> expected = Arrays.asList(team1, team2);

        List<Team> actual = teamRepository.getAllActiveTeams(actualDate);

        assertEquals(2, actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getMembers(), is(expected.get(i).getMembers()));
        }
    }
}