package com.github.api.client.service;

import com.github.api.client.client.GithubApiClient;
import com.github.api.client.model.Branch;
import com.github.api.client.model.Commit;
import com.github.api.client.model.Owner;
import com.github.api.client.model.Repository;
import com.github.api.client.model.dto.RepositoryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReposDataFetcherServiceTest {
    @Mock
    GithubApiClient githubApiClient;
    @InjectMocks
    ReposDataFetcherService service;

    @Test
    void test_getUserReposShouldReturnOnlyNonForkedRepositories() {
        Commit commit = new Commit("sha");
        Branch branch = new Branch("branchName", commit);

        Commit commit2 = new Commit("sha2");
        Branch branch2 = new Branch("branchName2", commit2);

        Owner owner = new Owner("userName");
        Repository repository = new Repository("repositoryName", owner, false);
        Repository repository2 = new Repository("repositoryName", owner, true);

        List<Branch> branches = List.of(branch, branch2);
        List<Repository> repositories = List.of(repository, repository2);

        Mockito.when(this.githubApiClient.getUserRepos(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString())
                ).thenReturn(repositories);
        Mockito.when(this.githubApiClient.getBranchesForUserRepo(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString())
        ).thenReturn(branches);
        List<RepositoryDTO> repositoryDTOS = this.service
                .getUserRepos("userName", "accessToken", "sort", "direction");

        assertEquals(1, repositoryDTOS.size());
        assertEquals(2, repositoryDTOS.get(0).branches().size());
        assertEquals(repository.name(), repositoryDTOS.get(0).repositoryName());
        assertEquals(repository.owner().login(), repositoryDTOS.get(0).ownerLogin());
        assertEquals(branch.name(), repositoryDTOS.get(0).branches().get(0).branchName());
        assertEquals(branch.commit().sha(), repositoryDTOS.get(0).branches().get(0).sha());
        assertEquals(branch2.name(), repositoryDTOS.get(0).branches().get(1).branchName());
        assertEquals(branch2.commit().sha(), repositoryDTOS.get(0).branches().get(1).sha());
    }
}
