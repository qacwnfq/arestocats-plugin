package org.jenkinsci.plugins.arestocats;

import hudson.model.Action;
import hudson.model.Run;

import java.io.IOException;


/**
 * @author fjadebeck
 */
public class ArestocatsResultsAction implements Action {

    private final Run<?, ?> build;

    private int numBuilds;
    private String results;

    public ArestocatsResultsAction(Run<?, ?> build, String results) {
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
        return "aRESTocats - Results";
    }

    @Override
    public String getUrlName() {
        return "aRESTocatsResults";
    }

    public Run<?, ?> getRun() {
        return build;
    }

}
