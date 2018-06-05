package org.jenkinsci.plugins.arestocats;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.Symbol;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jenkins.tasks.SimpleBuildStep;

import org.json.*;

/**
 * @author fjadebeck
 */
public class ArestocatsPublisher extends Recorder implements SimpleBuildStep {

    private static transient final Logger LOGGER = Logger.getLogger(ArestocatsPublisher.class.getName());
    private final ArestocatsChartHandler arestocatsChartHandler = new ArestocatsChartHandler();
    private final ArestocatsDataRecorder arestocatsDataRecorder = new ArestocatsDataRecorder();
    /**
     * {@link FileSet} "includes" string, like "foo/bar/*.xml"
     */
    private String metricsDatafilesPattern = "reports/metrics/*.json";
    /**
     * {@link FileSet} "includes" string, like "foo/bar/*.xml"
     */
    private String resultsDatafilesPattern = "reports/csv/*.csv";
    private int numberOfBuilds;
    private int currentNumber = 0;

    @DataBoundConstructor
    public ArestocatsPublisher(String metricsDatafilesPattern, String resultsDatafilesPattern, Integer numBuilds) {
        this.metricsDatafilesPattern = metricsDatafilesPattern.trim();
        this.resultsDatafilesPattern = resultsDatafilesPattern.trim();
        int _numBuilds;
        try {
            _numBuilds = numBuilds;
        } catch (NullPointerException e) {
            _numBuilds = 15;
        }
        this.numberOfBuilds = _numBuilds;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        // TODO add action that shows latest summary plot on the project page!!
        ArestocatsProjectMetricsAction arestocatsProjectMetricsAction = new ArestocatsProjectMetricsAction(project);
        ArestocatsProjectResultsAction arestocatsProjectResultsAction = new ArestocatsProjectResultsAction(project);
        return Arrays.asList(arestocatsProjectResultsAction, arestocatsProjectMetricsAction);
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        this.currentNumber = build.getNumber();
        Integer returnStatusOfRecording = arestocatsDataRecorder.recordResultsAndMetrics(build, workspace, this.resultsDatafilesPattern, this.metricsDatafilesPattern);
        Integer returnStatusOfChartHandling = arestocatsChartHandler.registerChartsForResultsAndMetrics(build, this.numberOfBuilds);
        if (Math.min(returnStatusOfRecording, returnStatusOfChartHandling) == new Integer(1)) {
            build.setResult(Result.SUCCESS);
        } else if (Math.min(returnStatusOfRecording, returnStatusOfChartHandling) == new Integer(0)) {
            build.setResult(Result.UNSTABLE);
        }
        build.setResult(Result.FAILURE);
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    private static final long serialVersionUID = 1L;

    @Extension
    @Symbol("arestocats")
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return "aRESTocats";
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         *
         * @param project Project.
         * @param value   File mask to validate.
         * @return the validation result.
         * @throws IOException if an error occurs.
         */
        public FormValidation doCheckTestResults(@AncestorInPath AbstractProject project,
                                                 @QueryParameter String value) throws IOException {
            if (project == null) {
                return FormValidation.ok();
            }
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }
}
