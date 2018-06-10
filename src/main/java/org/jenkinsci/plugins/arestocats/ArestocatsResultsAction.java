package org.jenkinsci.plugins.arestocats;

import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author fjadebeck
 */
public class ArestocatsResultsAction implements RunAction2, SimpleBuildStep.LastBuildAction {

    private Run<?, ?> build;

    private String results;
    private String summary;
    private List<ArestocatsProjectResultsAction> projectActions;

    public ArestocatsResultsAction(Run<?, ?> build, String results, String summary) {
        this.build = build;
        this.results = results;
        this.summary = summary;

        List<ArestocatsProjectResultsAction> projectActions = new ArrayList<>();
        projectActions.add(new ArestocatsProjectResultsAction(build.getParent()));
        this.projectActions = projectActions;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return this.projectActions;
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

    @Override
    public void onAttached(Run<?, ?> r) {
       this.build = r;
    }
    @Override
    public void onLoad(Run<?, ?> r) {
        this.build = r;
    }


}
