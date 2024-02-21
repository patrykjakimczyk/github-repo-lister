package com.github.api.client.model.dto;

import java.util.List;

public record RepositoryDTO(String repositoryName, String ownerLogin, List<BranchDTO> branches) {}