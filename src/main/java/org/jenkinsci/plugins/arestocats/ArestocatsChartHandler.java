package org.jenkinsci.plugins.arestocats;

import hudson.model.Run;

public class ArestocatsChartHandler {
    private final ArestocatsReportParser arestocatsReportParser = new ArestocatsReportParserImpl();

    public Integer registerChartsForResultsAndMetrics(Run<?, ?> build, int numberOfBuilds) {
        Integer returnCodeRegisterResultCharts = registerChartsForResults(build, numberOfBuilds);
        Integer returnCodeRegisterMetricCharts = registerChartsForMetrics(build, numberOfBuilds);
        return Math.min(returnCodeRegisterResultCharts, returnCodeRegisterMetricCharts);
    }

    public Integer registerChartsForResults(Run<?, ?> build, int numberOfBuilds) {
        String results = arestocatsReportParser.parseResultsFromBuilds(build, numberOfBuilds);
        String summary = arestocatsReportParser.parseResultsSummaryFromBuilds(build, numberOfBuilds);
        build.addAction(new ArestocatsResultsAction(build, results, summary));
        return Integer.valueOf(1);
    }

    public Integer registerChartsForMetrics(Run<?, ?> build, int numberOfBuilds) {
        String metrics = arestocatsReportParser.parseMetricsFromBuilds(build, numberOfBuilds);
        build.addAction(new ArestocatsMetricsAction(build, metrics));
        return Integer.valueOf(1);
    }
}
