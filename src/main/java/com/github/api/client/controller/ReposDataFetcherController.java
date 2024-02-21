package com.github.api.client.controller;

import com.github.api.client.model.response.GetUserRepositoriesResponse;
import com.github.api.client.service.ReposDataFetcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ReposDataFetcherController {
    private static final String GET_USER_REPOS_URL = "/repos/{user}";
    private final ReposDataFetcherService githubApiClientService;

    @GetMapping(GET_USER_REPOS_URL)
    public ResponseEntity<GetUserRepositoriesResponse> getUserRepos(
            @PathVariable(value = "user") String userName,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken
    ) {
        GetUserRepositoriesResponse response = new GetUserRepositoriesResponse(
                this.githubApiClientService.getUserRepos(userName, accessToken)
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}