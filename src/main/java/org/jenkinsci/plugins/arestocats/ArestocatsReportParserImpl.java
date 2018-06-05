package org.jenkinsci.plugins.arestocats;

import hudson.model.Run;

public class ArestocatsReportParserImpl implements ArestocatsReportParser {
    @Override
    public String parseMetricsFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        return "";
    }

    @Override
    public String parseResultsFromBuilds(Run<?, ?> build, int numberOfBuilds) {

        return "";
    }
}



//    JSONObject appendPreviousMetricsToCurrent(JSONObject metrics, JSONObject previous) {
//        try {
//            for (int i = 0; i < previous.getJSONArray("metricsPath").length(); i++) {
//                for (int j = 0; j < metrics.getJSONArray("metricsPath").length(); j++) {
//                    if (previous.getJSONArray("metricsPath").getJSONObject(i).getString("name").equals(
//                            metrics.getJSONArray("metricsPath").getJSONObject(j).getString("name"))) {
//                        JSONArray values = previous.getJSONArray("metricsPath").getJSONObject(i).getJSONArray("values");
//                        values.put(metrics.getJSONArray("metricsPath").getJSONObject(j).getDouble("value"));
//                        while (values.length() > this.numberOfBuilds) {
//                            values.remove(0);
//                        }
//                        metrics.getJSONArray("metricsPath").getJSONObject(j).put("values", values);
//                    }
//                }
//            }
//        } catch (JSONException e) {
//            for (int j = 0; j < metrics.getJSONArray("metricsPath").length(); j++) {
//                double value = metrics.getJSONArray("metricsPath").getJSONObject(j).getDouble("value");
//                JSONArray values = new JSONArray();
//                values.put(value);
//                metrics.getJSONArray("metricsPath").getJSONObject(j).put("values", values);
//            }
//        }
//        metrics.put("buildNumber", this.getCurrentNumber());
//        return metrics;
//    }
//
//

//    private JSONObject loadPreviousArestocatsData(Run<?, ?> build, String category)
//            throws IOException, InterruptedException {
//        Run<?, ?> previousBuild = build.getPreviousSuccessfulBuild();
//        if (previousBuild == null) {
//            return new JSONObject("{}");
//        }
//        try {
//            File previousFile = new File(previousBuild.getRootDir().getPath());
//            previousFile = new File(previousFile, metricsPath);
//            previousFile = new File(previousFile, category + ".json");
//            FilePath previousFilePath = new FilePath(previousFile);
//            return new JSONObject(previousFilePath.readToString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JSONObject("{}");
//        }
//    }