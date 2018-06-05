package org.jenkinsci.plugins.arestocats;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import jenkins.tasks.SimpleBuildStep;

import org.json.*;

/**
 * @author fjadebeck
 */
public class ArestocatsPublisher extends Recorder implements SimpleBuildStep {

    private static transient final Logger LOGGER = Logger.getLogger(ArestocatsPublisher.class.getName());
    /**
     * {@link FileSet} "includes" string, like "foo/bar/*.xml"
     */
    private final String metricDatafilesPattern;
    private final String resultsDatafilesPattern;
    private final int numBuilds;
    private int currentNumber = 0;
    transient private JSONArray arestocatsData;
    private String results = "{}";
    private String metrics = "{}";

    @DataBoundConstructor
    public ArestocatsPublisher(String metricDatafilesPattern, String resultsDatafilesPattern, Integer numBuilds) {
        this.metricDatafilesPattern = metricDatafilesPattern.trim();
        this.resultsDatafilesPattern = resultsDatafilesPattern.trim();
        int _numBuilds;
        try {
            _numBuilds = numBuilds;
        } catch (NullPointerException e) {
            _numBuilds = 15;
        }
        this.numBuilds = _numBuilds;
        this.arestocatsData = new JSONArray();
    }

//    @Override
//    public Action getProjectAction(AbstractProject<?, ?> project) {
//        return new ArestocatsProjectAction(project);
//    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        this.currentNumber = build.getNumber();
        recordArestocatsData(build, workspace);
//        this.resultsPath = createMetricsStringForGoogleCharts(build, workspace);
        build.addAction(new ArestocatsMetricsAction(build, this.metrics));
        build.addAction(new ArestocatsResultsAction(build, this.results));
        build.setResult(Result.SUCCESS);
    }


    JSONObject appendPreviousMetricsToCurrent(JSONObject metrics, JSONObject previous) {
        try {
            for (int i = 0; i < previous.getJSONArray("metricsPath").length(); i++) {
                for (int j = 0; j < metrics.getJSONArray("metricsPath").length(); j++) {
                    if (previous.getJSONArray("metricsPath").getJSONObject(i).getString("name").equals(
                            metrics.getJSONArray("metricsPath").getJSONObject(j).getString("name"))) {
                        JSONArray values = previous.getJSONArray("metricsPath").getJSONObject(i).getJSONArray("values");
                        values.put(metrics.getJSONArray("metricsPath").getJSONObject(j).getDouble("value"));
                        while (values.length() > this.numBuilds) {
                            values.remove(0);
                        }
                        metrics.getJSONArray("metricsPath").getJSONObject(j).put("values", values);
                    }
                }
            }
        } catch (JSONException e) {
            for (int j = 0; j < metrics.getJSONArray("metricsPath").length(); j++) {
                double value = metrics.getJSONArray("metricsPath").getJSONObject(j).getDouble("value");
                JSONArray values = new JSONArray();
                values.put(value);
                metrics.getJSONArray("metricsPath").getJSONObject(j).put("values", values);
            }
        }
        metrics.put("buildNumber", this.getCurrentNumber());
        return metrics;
    }

    private void recordArestocatsData(Run<?, ?> build, FilePath workspace)
            throws IOException, InterruptedException {
        recordMetricsData(build, workspace);
        recordResultsData(build, workspace);
    }

    private void recordMetricsData(Run<?, ?> build, FilePath workspace)
            throws IOException, InterruptedException {
        FilePath[] metricFilePaths = workspace.list(this.metricDatafilesPattern);
        for (FilePath metricFilePath : metricFilePaths) {
            File targetFile = new File(new File(Paths.BASE, Paths.METRICS), metricFilePath.getName());
            FilePath targetPath = new FilePath(targetFile);
            metricFilePath.copyTo(targetPath);
        }
    }

    private void recordResultsData(Run<?, ?> build, FilePath workspace)
            throws IOException, InterruptedException {
        FilePath[] metricFilePaths = workspace.list(this.resultsDatafilesPattern);
        for (FilePath metricFilePath : metricFilePaths) {
            File targetFile = new File(new File(Paths.BASE, Paths.RESULTS), metricFilePath.getName());
            FilePath targetPath = new FilePath(targetFile);
            metricFilePath.copyTo(targetPath);
        }
    }

//    private JSONObject loadPreviousArestocatsData(Run<?, ?> build, String category)
//            throws IOException, InterruptedException {
//        Run<?, ?> previousBuild = build.getPreviousSuccessfulBuild();
//        if (previousBuild == null) {
//            return new JSONObject("{}");
//        }
//        try {
//            File previousFile = new File(previousBuild.getRootDir().getPath());
//            previousFile = new File(previousFile, metricsPath);
//            previousFile = new File(previousFile, category + ".json");
//            FilePath previousFilePath = new FilePath(previousFile);
//            return new JSONObject(previousFilePath.readToString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JSONObject("{}");
//        }
//    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public String getMetricDatafilesPattern() {
        return metricDatafilesPattern;
    }

    public int getNumBuilds() {
        return numBuilds;
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
