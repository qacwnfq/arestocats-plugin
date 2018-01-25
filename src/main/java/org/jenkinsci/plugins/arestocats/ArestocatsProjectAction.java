package org.jenkinsci.plugins.arestocats;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.util.List;

import java.io.IOException;
import java.util.logging.Logger;



/**
 * @author fjadebeck
 */
public class ArestocatsProjectAction implements Action{

   private AbstractProject<?, ?> project;

   public ArestocatsProjectAction(final AbstractProject<?, ?> project) {
      this.project = project;
   }

   public String getResults() {
    List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
    AbstractBuild<?, ?> build = builds.get(0);
    final Class<ArestocatsAction> type = ArestocatsAction.class;
    return build.getAction(type).getResults();
   }

   @Override
   public String getIconFileName() {
      return "graph.gif";
   }

   @Override
   public String getDisplayName() {
      return "aRESTocats - Metrics";
   }

   @Override
   public String getUrlName() {
      return "aRESTocatsLatestBuild";
   }

   public int getCurrentNumber() {
       return project.getBuilds().size();
   }

   public String getProjectName() {
       return this.project.getName();
   }

   public AbstractProject<?, ?> getProject() {
       return this.project;
   }


}
