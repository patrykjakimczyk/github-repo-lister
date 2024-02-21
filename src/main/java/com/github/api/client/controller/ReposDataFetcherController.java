package com.github.api.client.controller;

import com.github.api.client.model.response.GetUserRepositoriesResponse;
import com.github.api.client.service.ReposDataFetcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
@RestController
public class ReposDataFetcherController {
    private static final String GET_USER_REPOS_URL = "{user}/repos";
    private final ReposDataFetcherService githubApiClientService;

    @GetMapping(value = GET_USER_REPOS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetUserRepositoriesResponse> getUserRepos(
            @PathVariable(value = "user") String userName,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken,
            @RequestHeader(value = HttpHeaders.ACCEPT) String accept,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction
    ) throws HttpMediaTypeNotAcceptableException {
        if (accept.isEmpty()) {
            throw new HttpMediaTypeNotAcceptableException("");
        }

        log.info("Performing repositories data fetching for username: {}", userName);
        GetUserRepositoriesResponse response = new GetUserRepositoriesResponse(
                this.githubApiClientService.getUserRepos(userName, accessToken, sort, direction)
        );

        return ResponseEntity.ok().body(response);
    }
}
