package com.github.api.client.service;

import com.github.api.client.client.GithubApiClient;
import com.github.api.client.model.Branch;
import com.github.api.client.model.dto.BranchDTO;
import com.github.api.client.model.Repository;
import com.github.api.client.model.dto.RepositoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReposDataFetcherService {
    private final GithubApiClient githubApiClient;

    public List<RepositoryDTO> getUserRepos(String userName, String accessToken) {
        return this.githubApiClient.getUserRepos(userName, accessToken)
                .stream()
                .filter(repository -> !repository.fork())
                .map(repository -> {
                    List<Branch> branches = this.githubApiClient.getBranchesForUserRepo(
                            userName,
                            repository.name(),
                            accessToken
                    );
                    return mapToRepositoryDTO(repository, branches);
                })
                .toList();
    }

    private RepositoryDTO mapToRepositoryDTO(Repository repository, List<Branch> branches) {
        return new RepositoryDTO(
                repository.name(),
                repository.owner().login(),
                branches.stream()
                        .map(branch -> new BranchDTO(branch.name(), branch.commit().sha()))
                        .toList()
        );
    }
}
