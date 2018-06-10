package org.jenkinsci.plugins.arestocats;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;



/**
 * @author fjadebeck
 */
public class ArestocatsProjectResultsAction implements Action {

    private Job<?, ?> project;

    public ArestocatsProjectResultsAction(final Job<?, ?> project) {
        this.project = project;
    }

    public String getResults() {
        RunList<? extends Run> builds = project.getBuilds();
        Run<?, ?> build = builds.iterator().next();
        final Class<ArestocatsResultsAction> type = ArestocatsResultsAction.class;
        return build.getAction(type).getResults();
    }

    public String getSummary() {
        RunList<? extends Run> builds = project.getBuilds();
        Run<?, ?> build = builds.iterator().next();
        final Class<ArestocatsResultsAction> type = ArestocatsResultsAction.class;
        return build.getAction(type).getSummary();
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
        return "aRESTocatsLatestBuildResults";
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
