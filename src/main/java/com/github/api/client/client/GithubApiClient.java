package com.github.api.client.client;

import com.github.api.client.PropertiesValues;
import com.github.api.client.exception.GithubUserNotFoundException;
import com.github.api.client.exception.WrongParamValueException;
import com.github.api.client.model.Branch;
import com.github.api.client.model.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public final class GithubApiClient {
    public static final String API_VERSION_HEADER_KEY = "X-GitHub-Api-Version";
    private static final String USER_AGENT_HEADER_KEY = "User-Agent";
    private static final String SORT_PARAM_KEY = "sort";
    private static final String DIRECTION_PARAM_KEY = "direction";
    private final RestTemplate restTemplate;
    private final PropertiesValues propertiesValues;

    public List<Repository> getUserRepos(String userName, String accessToken, String sort, String direction) {
        String userReposUrl =  this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserReposUrl, userName);
        ParameterizedTypeReference<List<Repository>> responseType = new ParameterizedTypeReference<>(){};
        userReposUrl = buildUrlWithParameters(userReposUrl, sort, direction).toUriString();

        return performRequest(userReposUrl, userName, accessToken, responseType);
    }

    public List<Branch> getBranchesForUserRepo(String userName, String repoName, String accessToken) {
        String repoBranchesUrl = this.propertiesValues.githubApiBaseUrl +
                String.format(this.propertiesValues.githubApiUserRepoBranchesUrl, userName, repoName);
        ParameterizedTypeReference<List<Branch>> responseType = new ParameterizedTypeReference<>(){};

        return performRequest(repoBranchesUrl, userName, accessToken, responseType);
    }

    private <T> List<T> performRequest(
            String url,
            String userName,
            String accessToken,
            ParameterizedTypeReference<List<T>> responseType
    ) {
        try {
            ResponseEntity<List<T>> apiResponse = this.restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    buildRequestEntity(url, userName, accessToken),
                    responseType
            );
            log.info("Fetching data from: {} succeded", url);

            return Objects.nonNull(apiResponse.getBody()) ? apiResponse.getBody() : Collections.emptyList();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                log.warn("User or repository not found during request: {}", url);
                throw new GithubUserNotFoundException(this.propertiesValues.userNotFoundMessage);
            }

            log.warn("Unexpected error occurred during request");
            throw exception;
        }
    }

    private RequestEntity<Void> buildRequestEntity(String url, String userName, String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (StringUtils.hasText(accessToken)) {
            httpHeaders.setBearerAuth(accessToken);
        }

        // Github API's documentation recommends to set this headers
        // More info here: https://docs.github.com/en/rest/using-the-rest-api/getting-started-with-the-rest-api
        MediaType mediaType = MediaType.valueOf(this.propertiesValues.githubApiAcceptHeader);
        httpHeaders.setAccept(List.of(mediaType));
        httpHeaders.set(API_VERSION_HEADER_KEY, this.propertiesValues.githubApiVersion);
        httpHeaders.set(USER_AGENT_HEADER_KEY, userName);

        return RequestEntity
                .get(url)
                .headers(httpHeaders)
                .build();
    }

    // This method adds optional parameters for sorting repositories
    // More info here: https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-a-user
    private UriComponents buildUrlWithParameters(String url, String sort, String direction) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (StringUtils.hasText(sort)) {
            if (!this.propertiesValues.allowedSorts.contains(sort)) {
                throw new WrongParamValueException(this.propertiesValues.wrongSortParamMessage);
            }

            builder.queryParam(SORT_PARAM_KEY, sort);
        }

        if (StringUtils.hasText(direction)) {
            if (!this.propertiesValues.allowedDirections.contains(direction)) {
                throw new WrongParamValueException(this.propertiesValues.wrongDirectionParamMessage);
            }

            builder.queryParam(DIRECTION_PARAM_KEY, direction);
        }

        return builder.build(true);
    }
}