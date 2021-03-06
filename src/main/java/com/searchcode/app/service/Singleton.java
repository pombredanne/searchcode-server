/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.config.InjectorConfig;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.*;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.AbstractMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Lazy Singleton Implementation
 * Generally used to share anything that the Quartz jobs require and to create all the shared objects
 * No idea if it really needs to be lazy other than it saves us creating everything on start although it all
 * gets called pretty quickly anyway so thats probably a moot point
 */
public final class Singleton {

    private static AbstractMap<String, Integer> runningIndexRepoJobs = null; // Used to know which jobs are currently running
    private static ISpellingCorrector spellingCorrectorInstance = null;
    private static Queue<CodeIndexDocument> codeIndexQueue = null; // Documents ready to be indexed
    private static int codeIndexLinesCount = 0; // Used to store how many lines we have ready to index for throttling

    private static SearchcodeLib searchcodeLib = null;
    private static AbstractMap<String, String> dataCache = null;
    private static AbstractMap<String, ApiResult> apiCache = null;
    private static AbstractMap<String, RepoResult> repoCache = null;
    private static AbstractMap<String, Object> genericCache = null;
    private static LoggerWrapper loggerWrapper = null;
    private static Scheduler scheduler = null;
    private static Repo repo = null;

    private static boolean backgroundJobsEnabled = true; // Controls if all background queue jobs should run or not
    private static UniqueRepoQueue uniqueGitRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueSvnRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueDeleteRepoQueue = null; // Used to queue the next repository to be deleted

    public static synchronized void incrementCodeIndexLinesCount(int incrementBy) {
        codeIndexLinesCount = codeIndexLinesCount + incrementBy;
    }

    public static synchronized void decrementCodeIndexLinesCount(int decrementBy) {
        codeIndexLinesCount = codeIndexLinesCount - decrementBy;

        if (codeIndexLinesCount < 0) {
            codeIndexLinesCount = 0;
        }
    }

    public static synchronized void setCodeIndexLinesCount(int value) {
        codeIndexLinesCount = value;
    }

    public static synchronized int getCodeIndexLinesCount() {
        return codeIndexLinesCount;
    }

    public static synchronized UniqueRepoQueue getUniqueGitRepoQueue() {
        if (uniqueGitRepoQueue == null) {
            uniqueGitRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }
        return uniqueGitRepoQueue;
    }

    public static synchronized UniqueRepoQueue getUniqueSvnRepoQueue() {
        if (uniqueSvnRepoQueue == null) {
            uniqueSvnRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }

        return uniqueSvnRepoQueue;
    }

    public static synchronized UniqueRepoQueue getUniqueDeleteRepoQueue() {
        if (uniqueDeleteRepoQueue == null) {
            uniqueDeleteRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }

        return uniqueDeleteRepoQueue;
    }

    /**
     * Used as cheap attempt to not have all the jobs trying to process the same thing note this has a race condition
     * and should be resolved at some point
     * TODO investigate usage and resolve race conditions
     */
    public static synchronized AbstractMap<String, Integer> getRunningIndexRepoJobs() {
        if (runningIndexRepoJobs == null) {
            runningIndexRepoJobs = new ConcurrentHashMap<String, Integer>();
        }

        return runningIndexRepoJobs;
    }

    public static synchronized Repo getRepo() {
        if (repo == null) {
            Injector injector = Guice.createInjector(new InjectorConfig());
            repo = injector.getInstance(Repo.class);
        }

        return repo;
    }

    public static synchronized ISpellingCorrector getSpellingCorrector() {
        if (spellingCorrectorInstance == null) {
            spellingCorrectorInstance = new SearchcodeSpellingCorrector();
        }

        return spellingCorrectorInstance;
    }

    public static synchronized Queue<CodeIndexDocument> getCodeIndexQueue() {
        if (codeIndexQueue == null) {
            codeIndexQueue = new ConcurrentLinkedQueue<CodeIndexDocument>();
        }

        return codeIndexQueue;
    }

    public static synchronized AbstractMap<String, String> getDataCache() {
        if (dataCache == null) {
            dataCache = new ConcurrentHashMap<String, String>();
        }

        return dataCache;
    }

    public static synchronized AbstractMap<String, ApiResult> getApiCache() {
        if (apiCache == null) {
            apiCache = new ConcurrentHashMap<String, ApiResult>();
        }

        return apiCache;
    }

    public static synchronized AbstractMap<String, RepoResult> getRepoCache() {
        if (repoCache == null) {
            repoCache = new ConcurrentHashMap<String, RepoResult>();
        }

        return repoCache;
    }

    public static synchronized AbstractMap<String, Object> getGenericCache() {
        if (genericCache == null) {
            genericCache = new ConcurrentHashMap<String, Object>();
        }

        return genericCache;
    }

    public static synchronized Scheduler getScheduler() {

        if (scheduler == null) {
            try {
                SchedulerFactory sf = new StdSchedulerFactory();
                scheduler = sf.getScheduler();
            } catch (SchedulerException ex) {
                Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
            }
        }

        return scheduler;
    }

    public static synchronized LoggerWrapper getLogger() {
        if (loggerWrapper == null) {
            loggerWrapper = new LoggerWrapper();
        }

        return loggerWrapper;
    }

    public static synchronized SearchcodeLib getSearchCodeLib() {
        if (searchcodeLib == null) {
            searchcodeLib = new SearchcodeLib();
        }

        return searchcodeLib;
    }

    /**
     * Overwrites the internal searchcode lib with the new one which will refresh the data it needs. Mainly used to
     * change the minified settings.
     */
    public static synchronized SearchcodeLib getSearchcodeLib(Data data) {
        searchcodeLib = new SearchcodeLib(data);

        return searchcodeLib;
    }

    public static synchronized boolean getBackgroundJobsEnabled() {
        return backgroundJobsEnabled;
    }

    public static synchronized void setBackgroundJobsEnabled(boolean jobsEnabled) {
        backgroundJobsEnabled = jobsEnabled;
    }
}
