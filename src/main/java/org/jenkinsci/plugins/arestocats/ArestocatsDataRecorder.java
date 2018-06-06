package org.jenkinsci.plugins.arestocats;

import hudson.FilePath;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;

public class ArestocatsDataRecorder {

    /**
     * @param build
     * @param workspace
     * @param resultsDatafilesPattern
     * @param metricsDatafilesPattern
     * @return 1 if succesfull, 0 if unstable, -1 if error
     * @throws IOException
     * @throws InterruptedException
     */
    public Integer recordResultsAndMetrics(Run<?, ?> build, FilePath workspace, String resultsDatafilesPattern, String metricsDatafilesPattern)
            throws IOException, InterruptedException {
        Integer returnStatusOfRecordingMetrics = recordMetricsData(build, workspace, metricsDatafilesPattern);
        Integer returnStatusOfRecordingResults = recordResultsData(build, workspace, resultsDatafilesPattern);
        Integer returnStatus = Math.min(returnStatusOfRecordingMetrics, returnStatusOfRecordingResults);
        return returnStatus;
    }

    /**
     * @param build
     * @param workspace
     * @param metricsDatafilesPattern
     * @return 1 if succesfull, 0 if unstable, -1 if error
     * @throws IOException
     * @throws InterruptedException
     */
    public Integer recordMetricsData(Run<?, ?> build, FilePath workspace, String metricsDatafilesPattern)
            throws IOException, InterruptedException {
        // ensures the directory exists, even if it is empty
        new FilePath(new File(new File(build.getRootDir(), Paths.BASE), Paths.METRICS)).mkdirs();
        FilePath[] metricFilePaths = workspace.list(metricsDatafilesPattern);
        for (FilePath metricFilePath : metricFilePaths) {
            File targetFile = new File(new File(new File(build.getRootDir(), Paths.BASE), Paths.METRICS), metricFilePath.getName());
            FilePath targetPath = new FilePath(targetFile);
            metricFilePath.copyTo(targetPath);
        }
        return new Integer(1);
    }

    /**
     * @param build
     * @param workspace
     * @param resultsDatafilesPattern
     * @return 1 if succesfull, 0 if unstable, -1 if error
     * @throws IOException
     * @throws InterruptedException
     */
    public Integer recordResultsData(Run<?, ?> build, FilePath workspace, String resultsDatafilesPattern)
            throws IOException, InterruptedException {
        // ensures this directory exists, even if it is empty
        new FilePath(new File(new File(build.getRootDir(), Paths.BASE), Paths.RESULTS)).mkdirs();
        FilePath[] metricFilePaths = workspace.list(resultsDatafilesPattern);
        for (FilePath metricFilePath : metricFilePaths) {
            File targetFile = new File(new File(new File(build.getRootDir(), Paths.BASE), Paths.RESULTS), metricFilePath.getName());
            FilePath targetPath = new FilePath(targetFile);
            metricFilePath.copyTo(targetPath);
        }
        return new Integer(1);
    }
}
