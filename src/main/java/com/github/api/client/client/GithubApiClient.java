package com.github.api.client.client;

import com.github.api.client.exception.GithubUserNotFoundException;
import com.github.api.client.model.Branch;
import com.github.api.client.model.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GithubApiClient {
    private static final String API_VERSION_HEADER_KEY = "X-GitHub-Api-Version";
    private static final String USER_AGENT_HEADER_KEY = "User-Agent";
    private final RestTemplate restTemplate;
    @Value("${github.api.url.base}")
    private String githubApiBaseUrl;
    @Value("${github.api.url.user-repos}")
    private String githubApiUserReposUrl;
    @Value("${github.api.url.user-repo-branches}")
    private String githubApiUserRepoBranchesUrl;
    @Value("${github.api.header.accept}")
    private String githubApiAcceptHeader;
    @Value("${github.api.version}")
    private String githubApiVersion;

    public List<Repository> getUserRepos(String userName, String accessToken) {
        String userReposUrl = githubApiBaseUrl + String.format(githubApiUserReposUrl, userName);

        try {
            ResponseEntity<List<Repository>> apiResponse = this.restTemplate.exchange(
                    userReposUrl,
                    HttpMethod.GET,
                    buildRequestEntity(userReposUrl, userName, accessToken),
                    new ParameterizedTypeReference<>() {}
            );

            return apiResponse.getBody();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new GithubUserNotFoundException("User with given name was not found");
            }

            throw exception;
        }
    }

    public List<Branch> getBranchesForUserRepo(String userName, String repoName, String accessToken) {
        String repoBranchesUrl = githubApiBaseUrl + String.format(githubApiUserRepoBranchesUrl, userName, repoName+"eee");

        try {
            ResponseEntity<List<Branch>> apiResponse = this.restTemplate.exchange(
                    repoBranchesUrl,
                    HttpMethod.GET,
                    buildRequestEntity(repoBranchesUrl, userName, accessToken),
                    new ParameterizedTypeReference<>() {}
            );

            return apiResponse.getBody();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new GithubUserNotFoundException("User with given name was not found");
            }

            throw exception;
        }
    }

    private RequestEntity<Void> buildRequestEntity(String url, String userName, String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (!StringUtils.hasText(accessToken)) {
            httpHeaders.setBearerAuth(accessToken);
        }

        // Github API's documentation recommends to set this headers
        // More info here: https://docs.github.com/en/rest/using-the-rest-api/getting-started-with-the-rest-api?apiVersion=2022-11-28
        MediaType mediaType = MediaType.valueOf(githubApiAcceptHeader);
        httpHeaders.setAccept(List.of(mediaType));
        httpHeaders.set(API_VERSION_HEADER_KEY, githubApiVersion);
        httpHeaders.set(USER_AGENT_HEADER_KEY, userName);

        return RequestEntity
                .get(url)
                .headers(httpHeaders)
                .build();
    }
}
