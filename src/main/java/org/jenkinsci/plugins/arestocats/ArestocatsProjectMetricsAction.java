package org.jenkinsci.plugins.arestocats;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

import java.util.List;


/**
 * @author fjadebeck
 */
public class ArestocatsProjectMetricsAction implements Action {

    private AbstractProject<?, ?> project;

    public ArestocatsProjectMetricsAction(final AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getMetrics() {
        List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
        AbstractBuild<?, ?> build = builds.get(0);
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

    public AbstractProject<?, ?> getProject() {
        return this.project;
    }


}
