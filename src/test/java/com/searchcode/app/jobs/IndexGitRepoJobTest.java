package com.searchcode.app.jobs;

import junit.framework.TestCase;

import java.util.Arrays;

public class IndexGitRepoJobTest extends TestCase {
    public void testGetBlameFilePath() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("./repo/something/test");
        assertEquals("repo/something/test", actual);
    }

    // No such thing as a windows path in the index, so should return empty
    public void testGetBlameFilePathWindows() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("\\repo\\something\\test");
        assertEquals("", actual);
    }

    public void testIndexSucess() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        assertFalse(gitRepoJob.checkIndexSucess("/tmp/"));
        gitRepoJob.createIndexSuccess("/tmp/");
        assertTrue(gitRepoJob.checkIndexSucess("/tmp/"));
        gitRepoJob.deleteIndexSuccess("/tmp/");
        assertFalse(gitRepoJob.checkIndexSucess("/tmp/"));
    }

    public void testCloneSucess() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        assertFalse(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
        gitRepoJob.createCloneUpdateSuccess("/tmp/");
        assertTrue(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
        gitRepoJob.deleteCloneUpdateSuccess("/tmp/");
        assertFalse(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
    }

    public void testDeleteNoFile() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        for(int i=0; i< 100; i++) {
            gitRepoJob.deleteIndexSuccess("/tmp/");
            gitRepoJob.deleteCloneUpdateSuccess("/tmp/");
        }
    }

    // TODO actually do something with this information
    public void testSomething() {
        String[] split = "myrepo/path/to/myfile.txt".split("/");
        String temp = String.join("/", Arrays.asList(split).subList(1, split.length));
        assertEquals("path/to/myfile.txt", temp);
    }
}
