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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;

import org.json.*;

/**
 * @author fjadebeck
 */
public class ArestocatsPublisher extends Recorder implements SimpleBuildStep {

      private static transient final Logger LOGGER = Logger.getLogger(ArestocatsPublisher.class.getName());
      private static final String arestocatsMetricsPath = "arestocatsMetrics";
      /** * {@link FileSet} "includes" string, like "foo/bar/*.xml" */
      private final String dataFilesPattern;
      private final int numBuilds;
      private int currentNumber = 0;
      private JSONArray arestocatsData;
      private String results = "{}";

      /**
       * If true, don't throw exception on missing test results or no files found.
       */
      // TODO probably remove, since it is not used
      private boolean allowEmptyResults;

      @DataBoundConstructor
      public ArestocatsPublisher(String dataFilesPattern, Integer numBuilds) {
            this.dataFilesPattern = dataFilesPattern.trim();
            int _numBuilds;
            try {
                  _numBuilds = numBuilds;
            } catch (NullPointerException e) {
                  _numBuilds = 15;
            }
            this.numBuilds = _numBuilds;
            this.arestocatsData = new JSONArray();
      }

      @Override
      public Action getProjectAction(AbstractProject<?, ?> project) {
            return new ArestocatsProjectAction(project);
      }

      @Override
      public void perform( Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener )
                  throws InterruptedException, IOException {
            this.currentNumber = build.getNumber();
            recordArestocatsData( build, workspace );
            this.results = createMetricsStringForGoogleCharts( build, workspace);
            build.addAction( new ArestocatsAction( build, this.results ) );
            build.setResult( Result.SUCCESS );
      }


      FilePath getCategoryFilePath( Run<?, ?> build, String category) throws IOException, InterruptedException {
                  File buildDir = new File( build.getRootDir().getPath() );
                  File arestocatsMetricsDir = new File( buildDir, arestocatsMetricsPath );
                  File buildFile = new File( arestocatsMetricsDir, category + ".json" );
                  return new FilePath( buildFile );
      }

      JSONObject appendPreviousMetricsToCurrent(JSONObject metrics, JSONObject previous) {
            try {
                  for( int i = 0; i < previous.getJSONArray( "metrics" ).length(); i++ ) {
                        for( int j = 0; j < metrics.getJSONArray( "metrics" ).length(); j++ ) {
                              if (previous.getJSONArray( "metrics" ).getJSONObject( i ).getString( "name" ).equals(
                                          metrics.getJSONArray( "metrics" ).getJSONObject( j ).getString( "name" ) ) ) {
                                    JSONArray values = previous.getJSONArray( "metrics" ).getJSONObject( i ).getJSONArray( "values" );
                                    values.put(metrics.getJSONArray( "metrics" ).getJSONObject( j ).getDouble( "value" ) );
                                    while( values.length() > this.numBuilds ) {
                                          values.remove(0);
                                    }
                                    metrics.getJSONArray( "metrics" ).getJSONObject( j ).put( "values", values );
                              }
                        }
                  }
            } catch (JSONException e) {
                  for( int j = 0; j < metrics.getJSONArray( "metrics" ).length(); j++ ) {
                        double value = metrics.getJSONArray( "metrics" ).getJSONObject( j ).getDouble( "value" );
                        JSONArray values = new JSONArray();
                        values.put(value);
                        metrics.getJSONArray( "metrics" ).getJSONObject(j).put( "values", values );
                  }
            }
            metrics.put("buildNumber", this.getCurrentNumber());
            return metrics;
      }

      private String createMetricsStringForGoogleCharts( Run<?, ?> build, FilePath workspace) {
            return this.arestocatsData.toString();
      }

      private void recordArestocatsData( Run<?, ?> build, FilePath workspace )
                  throws IOException, InterruptedException {
            FilePath[] fps = workspace.list( this.dataFilesPattern );
            for( FilePath fp : fps ) {
                  JSONObject currentMetrics = new JSONObject( fp.readToString() );
                  String category = currentMetrics.getString( "category" );
                  JSONObject previousMetrics = loadPreviousArestocatsData( build, category );
                  JSONObject updatedMetrics = appendPreviousMetricsToCurrent(currentMetrics, previousMetrics);
                  getCategoryFilePath( build, category ).write( updatedMetrics.toString(), "UTF-8" );
                  if( this.arestocatsData == null ) {
                        this.arestocatsData = new JSONArray();
                  }
                  this.arestocatsData.put( updatedMetrics );
            }
      }

      private JSONObject loadPreviousArestocatsData( Run<?, ?> build, String category )
                  throws IOException, InterruptedException {
            Run<?, ?> previousBuild = build.getPreviousSuccessfulBuild();
            if( previousBuild == null ) {
                  return new JSONObject("{}");
            }
            try {
                  File previousFile = new File(previousBuild.getRootDir().getPath());
                  previousFile = new File( previousFile, arestocatsMetricsPath );
                  previousFile = new File( previousFile, category + ".json" );
                  FilePath previousFilePath = new FilePath( previousFile );
                  return new JSONObject( previousFilePath.readToString() );
            } catch( Exception e ) {
                  e.printStackTrace();
                  return new JSONObject("{}");
            }
      }

      public BuildStepMonitor getRequiredMonitorService() {
            return BuildStepMonitor.NONE;
      }

      public String getDataFilesPattern() {
            return dataFilesPattern;
      }

      public int getNumBuilds() {
            return numBuilds;
      }

      public int getCurrentNumber() {
            return currentNumber;
      }

      /**
       *
       * @return the allowEmptyResults
       */
      public boolean isAllowEmptyResults() {
            return allowEmptyResults;
      }

      @DataBoundSetter
      public final void setAllowEmptyResults(boolean allowEmptyResults) {
            this.allowEmptyResults = allowEmptyResults;
      }

      private static final long serialVersionUID = 1L;

      @Extension
      public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
            public String getDisplayName() {
                  return "aRESTocats";
            }

            /** 
             * Performs on-the-fly validation on the file mask wildcard.
             * @param project Project.
             * @param value File mask to validate.
             *
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
