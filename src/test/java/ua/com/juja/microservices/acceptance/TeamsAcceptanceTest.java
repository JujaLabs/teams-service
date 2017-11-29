package ua.com.juja.microservices.acceptance;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.Utils;
import ua.com.juja.microservices.teams.dao.feign.KeepersClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;

import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Ivan Shapovalov
 * @author Vladimir Zadorozhniy
 */
@RunWith(SpringRunner.class)
public class TeamsAcceptanceTest extends BaseAcceptanceTest {
    private final String teamsActivateTeamUrl = "/v1/teams";
    private final String teamsDeactivateTeamUrl = "/v1/teams";
    private final String teamsGetTeamUrl = "/v1/teams/users";
    private final String teamsGetAllTeamsUrl = "/v1/teams";

    private final String keepersGetDirectionsUrl = "/v1/keepers";
    private final String teamsDirection = "teams";

    @Inject
    private RestTemplate restTemplate;

    @MockBean
    private KeepersClient keepersClient;

    private MockRestServiceServer mockServer;

    @Before
    public void setup() {
        super.setup();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @UsingDataSet(locations = "/datasets/activateTeamIfUserNotInActiveTeam.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void activateTeamIfUserNotInActiveTeamExecutedCorrectly() throws IOException {
        String jsonContentRequest = Utils.convertToString(resource
                ("acceptance/request/requestActivateTeamIfUserNotInActiveTeamExecutedCorrecly.json"));
        when(keepersClient.getDirections("uuid-from"))
                .thenReturn(Collections.singletonList(teamsDirection));
        Response actualResponse = getRealResponse(teamsActivateTeamUrl, jsonContentRequest, HttpMethod.POST);
        String result = actualResponse.asString();
        String jsonContentExpectedResponse = Utils.convertToString(
                resource("acceptance/response/responseActivateTeamIfUserNotInActiveTeamExecutedCorrectly.json"));

        printConsoleReport(teamsActivateTeamUrl, jsonContentExpectedResponse, actualResponse.body());
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .when(Option.IGNORING_EXTRA_FIELDS)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/activateTeamIfUsersInAnotherActiveTeam.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void activateTeamIfUserInAnotherActiveTeamExecutedCorrectly() throws IOException {
        String jsonContentRequest = Utils
                .convertToString(resource("acceptance/request/requestActivateTeamIfUsersInActiveTeamThrowsExceptions.json"));
        String jsonContentControlResponse = Utils.convertToString(
                resource("acceptance/response/responseActivateTeamIfUserInActiveTeamThrowsException.json"));
        when(keepersClient.getDirections("uuid-from"))
                .thenReturn(Collections.singletonList(teamsDirection));
        Response actualResponse = getRealResponse(teamsActivateTeamUrl, jsonContentRequest, HttpMethod.POST);

        String result = actualResponse.asString();
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonContentControlResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void deactivateTeamIfUserInTeamExecutedCorrectly() throws IOException {
        String jsonContentRequest = Utils.convertToString(resource
                ("acceptance/request/requestDeactivateTeamIfUserInTeamExecutedCorrectly.json"));
        when(keepersClient.getDirections("uuid-from"))
                .thenReturn(Collections.singletonList(teamsDirection));
        Response actualResponse = getRealResponse(teamsDeactivateTeamUrl, jsonContentRequest, HttpMethod.PUT);

        String result = actualResponse.asString();
        String jsonContentExpectedResponse = String.format(Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserInTeamExecutedCorrectly.json")),
                "", "");
        printConsoleReport(teamsDeactivateTeamUrl, jsonContentExpectedResponse, actualResponse.body());

        assertThatJson(result).when(Option.IGNORING_ARRAY_ORDER)
                .when(Option.IGNORING_EXTRA_FIELDS)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void deactivateTeamIfUserNotInTeamThrowsException() throws IOException {
        String jsonContentRequest = Utils.convertToString(resource
                ("acceptance/request/requestDeactivateTeamIfUserNotInTeamThrowsException.json"));
        String jsonContentExpectedResponse = Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserNotInTeamThrowsExeption.json"));
        when(keepersClient.getDirections("uuid-from"))
                .thenReturn(Collections.singletonList(teamsDirection));
        Response actualResponse = getRealResponse(teamsDeactivateTeamUrl, jsonContentRequest, HttpMethod.PUT);

        printConsoleReport(teamsDeactivateTeamUrl, jsonContentExpectedResponse, actualResponse.body());
        String result = actualResponse.asString();
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void deactivateTeamIfUserInSeveralTeamsThrowsException() throws IOException {
        String jsonContentRequest = Utils.convertToString(resource
                ("acceptance/request/requestDeactivateTeamIfUserInSeveralTeamsException.json"));
        String jsonContentExpectedResponse = Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserInSeveralTeamsThrowsExceptions.json"));
        when(keepersClient.getDirections("uuid-from"))
                .thenReturn(Collections.singletonList(teamsDirection));
        Response actualResponse = getRealResponse(teamsDeactivateTeamUrl, jsonContentRequest, HttpMethod.PUT);

        printConsoleReport(teamsDeactivateTeamUrl, jsonContentExpectedResponse, actualResponse.body());
        String result = actualResponse.asString();
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void getTeamIfUserInTeamExecutedCorrectly() throws IOException {
        String uuid = "uuid-in-one-team";
        String url = teamsGetTeamUrl + "/" + uuid;
        String jsonContentExpectedResponse = String.format(Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserInTeamExecutedCorrectly.json")),
                "", "");

        Response actualResponse = getRealResponse(url, "", HttpMethod.GET);

        String result = actualResponse.asString();
        printConsoleReport(url, jsonContentExpectedResponse, actualResponse.body());

        assertThatJson(result).when(Option.IGNORING_ARRAY_ORDER)
                .when(Option.IGNORING_EXTRA_FIELDS)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void getTeamIfUserInSeveralTeamsThrowsException() throws IOException {
        String uuid = "uuid-in-several-teams";
        String jsonContentExpectedResponse = Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserInSeveralTeamsThrowsExceptions.json"));
        String url = teamsGetTeamUrl + "/" + uuid;

        Response actualResponse = getRealResponse(url, "", HttpMethod.GET);

        printConsoleReport(url, jsonContentExpectedResponse, actualResponse.body());
        String result = actualResponse.asString();
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAndDeactivateDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void getTeamIfUserNotInTeamThrowsException() throws IOException {
        String uuid = "uuid-not-in-team";
        String jsonContentExpectedResponse = Utils.convertToString(
                resource("acceptance/response/responseGetDeactivateTeamIfUserNotInTeamThrowsExeption.json"));
        String url = teamsGetTeamUrl + "/" + uuid;

        Response actualResponse = getRealResponse(url, "", HttpMethod.GET);

        printConsoleReport(url, jsonContentExpectedResponse, actualResponse.body());
        String result = actualResponse.asString();
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(jsonContentExpectedResponse);
    }

    @UsingDataSet(locations = "/datasets/getAllActiveTeamsDataSet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Test
    public void getAllTeamExecutedCorrectly() throws IOException {
        String url = teamsGetAllTeamsUrl;
        Response actualResponse = getRealResponse(url, "", HttpMethod.GET);

        String result = actualResponse.asString();
        String jsonContentExpectedResponse = String.format(Utils.convertToString(
                resource("acceptance/response/responseGetAllTeamsExecutedCorrectly.json")),
                "", "");
        printConsoleReport(url, jsonContentExpectedResponse, actualResponse.body());
        assertThatJson(result).when(Option.IGNORING_ARRAY_ORDER)
                .when(Option.IGNORING_EXTRA_FIELDS)
                .isEqualTo(jsonContentExpectedResponse);
    }
}
