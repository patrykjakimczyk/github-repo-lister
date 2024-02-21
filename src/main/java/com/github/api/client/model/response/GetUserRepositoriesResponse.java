package com.github.api.client.model.response;

import com.github.api.client.model.dto.RepositoryDTO;

import java.util.List;

public record GetUserRepositoriesResponse(List<RepositoryDTO> repositories) {}