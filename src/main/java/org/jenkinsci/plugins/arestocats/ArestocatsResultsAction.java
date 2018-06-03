package org.jenkinsci.plugins.arestocats;

import hudson.model.Action;
import hudson.model.Run;

import java.io.IOException;


/**
 * @author fjadebeck
 */
public class ArestocatsResultsAction implements Action {

    private final Run<?, ?> build;

    private String results;
    private String summary;

    public ArestocatsResultsAction(Run<?, ?> build, String results, String summary) {
        this.build = build;
        this.results = results;
        this.summary = summary;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getResults() {
        return this.results;
    }

    public int getCurrentBuildNumber() {
        return build.getNumber();
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
