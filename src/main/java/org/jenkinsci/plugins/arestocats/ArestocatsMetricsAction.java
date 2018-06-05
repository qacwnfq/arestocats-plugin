package org.jenkinsci.plugins.arestocats;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author fjadebeck
 */
public class ArestocatsMetricsAction implements Action {

    private final Run<?, ?> build;

    private int numBuilds;
    private String metrics;

    public ArestocatsMetricsAction(Run<?, ?> build, String metrics) {
        this.build = build;
        this.metrics = metrics;
    }

    public String getMetrics() {
        return this.metrics;
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
        return "aRESTocatsMetrics";
    }

    public Run<?, ?> getRun() {
        return build;
    }

}
