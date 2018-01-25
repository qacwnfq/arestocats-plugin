package org.jenkinsci.plugins.arestocats;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.io.IOException;
import java.util.logging.Logger;



/**
 * @author fjadebeck
 */
public class ArestocatsAction implements Action{

   private final Run<?, ?> build;

   private int numBuilds;
   private String results = "{}";

   public ArestocatsAction(Run<?, ?> build, String results) {
      this.build = build;
      this.results = results;
   }

   public String getResults() {
      return this.results;
   }

   public int getNumBuilds() {
      return numBuilds;
   }

   public int getCurrentNumber() {
      return this.build.getNumber();
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
      return "aRESTocats";
   }

   public boolean hasPlots() throws IOException {
      //TODO implement
      return true;
   }

   public Run<?, ?> getRun() {
      return build;
   }

}
