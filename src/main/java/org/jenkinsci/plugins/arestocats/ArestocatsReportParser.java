package org.jenkinsci.plugins.arestocats;

import hudson.model.Run;

public interface ArestocatsReportParser {

    String parseMetricsFromBuilds(Run<?, ?> build, int numberOfBuilds);

    String parseResultsFromBuilds(Run<?, ?> build, int numberOfBuilds);
}
