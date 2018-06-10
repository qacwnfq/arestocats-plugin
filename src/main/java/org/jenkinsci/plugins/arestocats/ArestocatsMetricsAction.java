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
public class ArestocatsMetricsAction implements RunAction2, SimpleBuildStep.LastBuildAction {

    private Run<?, ?> build;

    private String metrics;
    private List<ArestocatsProjectMetricsAction> projectActions;


    public ArestocatsMetricsAction(Run<?, ?> build, String metrics) {
        this.build = build;
        this.metrics = metrics;

        List<ArestocatsProjectMetricsAction> projectActions = new ArrayList<>();
        projectActions.add(new ArestocatsProjectMetricsAction(build.getParent()));
        this.projectActions = projectActions;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return this.projectActions;
    }


    public String getMetrics() {
        return this.metrics;
    }

    public int getCurrentBuildNumber() {
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

    @Override
    public void onAttached(Run<?, ?> run) {
        this.build = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.build = run;
    }

}
