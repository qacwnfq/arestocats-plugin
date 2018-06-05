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
        build.addAction(new ArestocatsResultsAction(build, results));
        return new Integer(1);
    }

    public Integer registerChartsForMetrics(Run<?, ?> build, int numberOfBuilds) {
        String metrics = arestocatsReportParser.parseMetricsFromBuilds(build, numberOfBuilds);
        build.addAction(new ArestocatsMetricsAction(build, metrics));
        return new Integer(1);
    }
}
