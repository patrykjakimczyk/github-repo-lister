package com.github.api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertiesValues {
    @Value("${github.api.url.base}")
    public String githubApiBaseUrl;
    @Value("${github.api.url.user-repos}")
    public String githubApiUserReposUrl;
    @Value("${github.api.url.user-repo-branches}")
    public String githubApiUserRepoBranchesUrl;
    @Value("${github.api.header.accept}")
    public String githubApiAcceptHeader;
    @Value("${github.api.version}")
    public String githubApiVersion;
    @Value("#{'${github.api.sorts}'.split(', ')}")
    public List<String> allowedSorts;
    @Value("#{'${github.api.directions}'.split(', ')}")
    public List<String> allowedDirections;

    @Value("${exception.message.wrong-param.sort}")
    public String wrongSortParamMessage;
    @Value("${exception.message.wrong-param.direction}")
    public String wrongDirectionParamMessage;
    @Value("${exception.message.user-not-found}")
    public String userNotFoundMessage;
    @Value("${exception.message.not-acceptable}")
    public String notAcceptableMessage;
    @Value("${exception.message.missing-header}")
    public String missingHeader;
    @Value("${exception.message.unexpected-error}")
    public String unexpectedErrorMessage;
}
