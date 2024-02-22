package com.github.api.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.api.client.PropertiesValues;
import com.github.api.client.exception.ExceptionMessage;
import com.github.api.client.model.dto.BranchDTO;
import com.github.api.client.model.dto.RepositoryDTO;
import com.github.api.client.model.response.GetUserRepositoriesResponse;
import com.github.api.client.service.ReposDataFetcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ExtendWith(MockitoExtension.class)
class ReposDataFetcherControllerTest {
    private final String url = "/api/";
    private final String userName = "userName";
    private final String accessToken = "accessToken";

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ReposDataFetcherService githubApiClientService;
    @MockBean
    PropertiesValues propertiesValues;

    private HttpHeaders headers;

    @BeforeEach
    void init() {
        propertiesValues.notAcceptableMessage = "Requested response's media type is not acceptable. Required type is 'application/json'";
        propertiesValues.missingHeader = "Request's 'Accept' or 'Authorization' header is missing";

        MediaType mediaType = new MediaType(MediaType.APPLICATION_JSON);
        headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.accessToken);
        headers.setAccept(List.of(mediaType));
    }

    @Test
    void test_getUserReposShouldReturnResponseWithListOfRepositories() throws Exception {
        final RepositoryDTO repositoryDTO = buildRepositoryDTO();
        final GetUserRepositoriesResponse expectedResponse = new GetUserRepositoriesResponse(List.of(repositoryDTO));
        final String expectedJson = new ObjectMapper().writeValueAsString(expectedResponse);

        Mockito.when(this.githubApiClientService.getUserRepos(
                    eq(this.userName),
                    eq(this.accessToken),
                    nullable(String.class),
                    nullable(String.class)
                )).thenReturn(List.of(repositoryDTO));

        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(this.headers)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void test_getUserReposShouldReturnListOfRepositoriesWithSortAndDirectionAndNullAccessToken() throws Exception {
        final String sort = "sort";
        final String direction = "direction";
        final RepositoryDTO repositoryDTO = buildRepositoryDTO();
        final GetUserRepositoriesResponse expectedResponse = new GetUserRepositoriesResponse(List.of(repositoryDTO));
        final String expectedJson = new ObjectMapper().writeValueAsString(expectedResponse);
        headers.remove(HttpHeaders.AUTHORIZATION);

        Mockito.when(this.githubApiClientService.getUserRepos(
                eq(this.userName),
                nullable(String.class),
                eq(sort),
                eq(direction)
        )).thenReturn(List.of(repositoryDTO));

        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(this.headers)
                        .param(sort, sort)
                        .param(direction, direction)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void test_getUserReposShouldReturnResponseWithEmptyList() throws Exception {
        final GetUserRepositoriesResponse expectedResponse = new GetUserRepositoriesResponse(Collections.emptyList());
        final String expectedJson = new ObjectMapper().writeValueAsString(expectedResponse);

        Mockito.when(this.githubApiClientService.getUserRepos(
                eq(this.userName),
                eq(this.accessToken),
                nullable(String.class),
                nullable(String.class)
        )).thenReturn(Collections.emptyList());

        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(this.headers)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void test_getUserReposShouldThrowMissingHeaderException() throws Exception {
        final ExceptionMessage exceptionMessage = new ExceptionMessage(HttpStatus.BAD_REQUEST.value(), this.propertiesValues.missingHeader);
        final String expectedJson = new ObjectMapper().writeValueAsString(exceptionMessage);
        headers.remove(HttpHeaders.ACCEPT);


        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(headers)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void test_getUserReposShouldThrowMediaTypeNotAcceptableOnNullAcceptHeaderException() throws Exception {
        final ExceptionMessage exceptionMessage = new ExceptionMessage(HttpStatus.NOT_ACCEPTABLE.value(), this.propertiesValues.notAcceptableMessage);
        final String expectedJson = new ObjectMapper().writeValueAsString(exceptionMessage);
        headers.setAccept(Collections.emptyList());


        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(headers)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isNotAcceptable()).andReturn();
    }

    @Test
    void test_getUserReposShouldThrowMediaTypeNotAcceptableException() throws Exception {
        final ExceptionMessage exceptionMessage = new ExceptionMessage(HttpStatus.NOT_ACCEPTABLE.value(), this.propertiesValues.notAcceptableMessage);
        final String expectedJson = new ObjectMapper().writeValueAsString(exceptionMessage);
        final MediaType mediaType = new MediaType(MediaType.APPLICATION_XML);
        headers.setAccept(List.of(mediaType));


        mockMvc.perform(get(this.url + ReposDataFetcherController.GET_USER_REPOS_URL, this.userName)
                        .headers(headers)
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isNotAcceptable()).andReturn();
    }

    private RepositoryDTO buildRepositoryDTO() {
        BranchDTO branchDTO = new BranchDTO("branchName", "sha");
        return new RepositoryDTO("repositoryName", this.userName, List.of(branchDTO));
    }
}
