package org.jenkinsci.plugins.arestocats;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;


/**
 * @author fjadebeck
 */
public class ArestocatsProjectMetricsAction implements Action {

    private Job<?, ?> project;

    public ArestocatsProjectMetricsAction(final Job<?, ?> project) {
        this.project = project;
    }

    public String getMetrics() {
        RunList<? extends Run> builds = project.getBuilds();
        Run<?, ?> build = builds.iterator().next();
        final Class<ArestocatsMetricsAction> type = ArestocatsMetricsAction.class;
        return build.getAction(type).getMetrics();
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
        return "aRESTocatsLatestBuildMetrics";
    }

    public int getCurrentNumber() {
        return project.getBuilds().size();
    }

    public String getProjectName() {
        return this.project.getName();
    }

    public Job<?, ?> getProject() {
        return this.project;
    }


}
