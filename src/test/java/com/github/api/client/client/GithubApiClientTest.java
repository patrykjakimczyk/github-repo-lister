package com.github.api.client.client;

import com.github.api.client.PropertiesValues;
import com.github.api.client.exception.GithubUserNotFoundException;
import com.github.api.client.exception.WrongParamValueException;
import com.github.api.client.model.Branch;
import com.github.api.client.model.Commit;
import com.github.api.client.model.Owner;
import com.github.api.client.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class GithubApiClientTest {
    private final String userName = "userName";
    private final String repositoryName = "repositoryName";
    private final String accessToken = "accessToken";
    private final Owner owner = new Owner(this.userName);
    private final Repository repository = new Repository(this.repositoryName, owner, false);

    @Mock
    RestTemplate restTemplate;
    @Mock
    PropertiesValues propertiesValues;
    @InjectMocks
    GithubApiClient client;

    @Captor
    ArgumentCaptor<RequestEntity<Void>> requestEntityArgumentCaptor;


    @BeforeEach
    void init() {
        propertiesValues.githubApiBaseUrl = "https://api.github.com/";
        propertiesValues.githubApiUserReposUrl = "users/%s/repos";
        propertiesValues.githubApiUserRepoBranchesUrl = "repos/%s/%s/branches";
        propertiesValues.githubApiVersion = "2022-11-28";
        propertiesValues.userNotFoundMessage = "Wrong param value for direction. Allowed values are {asc, desc}";
        propertiesValues.githubApiAcceptHeader = "application/vnd.github+json";
        propertiesValues.allowedSorts= List.of("created", "updated", "pushed", "full_name");
        propertiesValues.allowedDirections= List.of("asc", "desc");
    }

    @Test
    void test_getUserReposShouldReturnListOfRepositories() {
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName);

        Mockito.when(this.restTemplate.exchange(
                        eq(buildUriComponents(userReposUrl).toUriString()),
                        eq(HttpMethod.GET),
                        this.requestEntityArgumentCaptor.capture(),
                        Mockito.any(ParameterizedTypeReference.class)
                )).thenReturn((ResponseEntity.ok().body(List.of(this.repository))));
        List<Repository> repositories = this.client.getUserRepos(this.userName, this.accessToken, null, null);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertEquals(1, repositories.size());
        assertEquals(this.repository.name(), repositories.get(0).name());
        assertEquals(this.repository.owner().login(), repositories.get(0).owner().login());
        assertFalse(this.repository.fork());
        assertEquals("Bearer " + this.accessToken, requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getUserReposShouldReturnListOfRepositoriesForNullAccessToken() {
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(userReposUrl).toUriString()),
                eq(HttpMethod.GET),
                this.requestEntityArgumentCaptor.capture(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn((ResponseEntity.ok().body(List.of(this.repository))));
        List<Repository> repositories = this.client.getUserRepos(this.userName, null, null, null);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertEquals(1, repositories.size());
        assertEquals(this.repository.name(), repositories.get(0).name());
        assertEquals(this.repository.owner().login(), repositories.get(0).owner().login());
        assertFalse(this.repository.fork());
        assertNull(requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getUserReposShouldReturnListOfRepositoriesWithSortAndDirection() {
        final String sort = "created";
        final String direction = "asc";
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName) +
                "?sort=" + sort + "&direction=" + direction;

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(userReposUrl).toUriString()),
                eq(HttpMethod.GET),
                this.requestEntityArgumentCaptor.capture(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn((ResponseEntity.ok().body(List.of(this.repository))));
        List<Repository> repositories = this.client
                .getUserRepos(this.userName, this.accessToken, sort, direction);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertEquals(1, repositories.size());
        assertEquals(this.repository.name(), repositories.get(0).name());
        assertEquals(this.repository.owner().login(), repositories.get(0).owner().login());
        assertFalse(this.repository.fork());
        assertEquals("Bearer " + this.accessToken, requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getUserReposShouldEmptyList() {
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(userReposUrl).toUriString()),
                eq(HttpMethod.GET),
                this.requestEntityArgumentCaptor.capture(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn((ResponseEntity.ok().body(null)));
        List<Repository> repositories = this.client
                .getUserRepos(this.userName, this.accessToken, null, null);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertNotNull(repositories);
        assertEquals(0, repositories.size());
        assertEquals("Bearer " + this.accessToken, requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getUserReposShouldThrowUserNotFoundException() {
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName);
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(userReposUrl).toUriString()),
                eq(HttpMethod.GET),
                Mockito.any(RequestEntity.class),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenThrow(httpClientErrorException);

        Exception exception = assertThrows(GithubUserNotFoundException.class, () -> this.client
                .getUserRepos(this.userName, this.accessToken, null, null));
        assertEquals(this.propertiesValues.userNotFoundMessage, exception.getMessage());
    }

    @Test
    void test_getUserReposShouldThrowUnexpectedException() {
        final String userReposUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, this.userName);
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(userReposUrl).toUriString()),
                eq(HttpMethod.GET),
                Mockito.any(RequestEntity.class),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenThrow(httpClientErrorException);

        assertThrows(HttpClientErrorException.class, () -> this.client
                .getUserRepos(this.userName, this.accessToken, null, null));
    }

    @Test
    void test_getUserReposShouldThrowExceptionForInvalidSortValue() {
        final String invalidSort = "invalid_sort";

        Exception exception = assertThrows(WrongParamValueException.class, () -> this.client
                .getUserRepos(this.userName, this.accessToken, invalidSort, null));
        assertEquals(this.propertiesValues.wrongSortParamMessage, exception.getMessage());
    }

    @Test
    void test_getUserReposShouldThrowExceptionForInvalidDirectionValue() {
        final String sort = "full_name";
        final String invalidDirection = "invalidDirection";

        Exception exception = assertThrows(WrongParamValueException.class, () -> this.client
                .getUserRepos(this.userName, this.accessToken, sort, invalidDirection));
        assertEquals(this.propertiesValues.wrongDirectionParamMessage, exception.getMessage());
    }

    @Test
    void test_getRepoBranchesShouldReturnListOfBranches() {
        final Commit commit = new Commit("sha");
        final Branch branch = new Branch("name", commit);
        final String expectedBranchesUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserRepoBranchesUrl, this.userName, this.repositoryName);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(expectedBranchesUrl).toUriString()),
                eq(HttpMethod.GET),
                this.requestEntityArgumentCaptor.capture(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn((ResponseEntity.ok().body(List.of(branch))));
        List<Branch> branches = this.client.getBranchesForUserRepo(this.userName, this.repositoryName, this.accessToken);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertEquals(1, branches.size());
        assertEquals("Bearer " + this.accessToken, requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getRepoBranchesShouldReturnEmptyList() {
        final String expectedBranchesUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserRepoBranchesUrl, this.userName, this.repositoryName);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(expectedBranchesUrl).toUriString()),
                eq(HttpMethod.GET),
                this.requestEntityArgumentCaptor.capture(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn((ResponseEntity.ok().body(null)));
        List<Branch> branches = this.client.getBranchesForUserRepo(this.userName, this.repositoryName, this.accessToken);
        RequestEntity<Void> requestEntity = this.requestEntityArgumentCaptor.getValue();

        assertTrue(branches.isEmpty());
        assertEquals("Bearer " + this.accessToken, requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertEquals(this.propertiesValues.githubApiAcceptHeader, requestEntity.getHeaders().get(HttpHeaders.ACCEPT).get(0));
        assertEquals(this.userName, requestEntity.getHeaders().get(HttpHeaders.USER_AGENT).get(0));
        assertEquals(this.propertiesValues.githubApiVersion, requestEntity.getHeaders().get(GithubApiClient.API_VERSION_HEADER_KEY).get(0));
    }

    @Test
    void test_getRepoBranchesShouldThrowUserNotFoundException() {
        final String expectedBranchesUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserRepoBranchesUrl, this.userName, this.repositoryName);
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(expectedBranchesUrl).toUriString()),
                eq(HttpMethod.GET),
                Mockito.any(RequestEntity.class),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenThrow(httpClientErrorException);

        Exception exception = assertThrows(GithubUserNotFoundException.class, () ->
                this.client.getBranchesForUserRepo(this.userName, this.repositoryName, this.accessToken));
        assertEquals(this.propertiesValues.userNotFoundMessage, exception.getMessage());
    }

    @Test
    void test_getRepoBranchesShouldThrowUnexpectedException() {
        final String expectedBranchesUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserRepoBranchesUrl, this.userName, this.repositoryName);
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(this.restTemplate.exchange(
                eq(buildUriComponents(expectedBranchesUrl).toUriString()),
                eq(HttpMethod.GET),
                Mockito.any(RequestEntity.class),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenThrow(httpClientErrorException);

        assertThrows(HttpClientErrorException.class, () ->
                this.client.getBranchesForUserRepo(this.userName, this.repositoryName, this.accessToken));
    }

    private UriComponents buildUriComponents(String expectedUrl) {
        return UriComponentsBuilder.fromHttpUrl(expectedUrl).build(true);
    }
}
