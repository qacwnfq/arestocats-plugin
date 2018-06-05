package org.jenkinsci.plugins.arestocats;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

import java.util.List;


/**
 * @author fjadebeck
 */
public class ArestocatsProjectResultsAction implements Action {

    private AbstractProject<?, ?> project;

    public ArestocatsProjectResultsAction(final AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getResults() {
        List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
        AbstractBuild<?, ?> build = builds.get(0);
        final Class<ArestocatsResultsAction> type = ArestocatsResultsAction.class;
        return build.getAction(type).getResults();
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

    public AbstractProject<?, ?> getProject() {
        return this.project;
    }


}
