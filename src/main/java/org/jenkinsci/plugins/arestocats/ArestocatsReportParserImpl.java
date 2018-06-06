package org.jenkinsci.plugins.arestocats;

import hudson.FilePath;
import hudson.model.Run;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArestocatsReportParserImpl implements ArestocatsReportParser {
    private static transient final Logger LOGGER = Logger.getLogger(ArestocatsReportParserImpl.class.getName());

    @Override
    public String parseMetricsFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        StringBuilder metricsStringBuilder = new StringBuilder();
        Run<?, ?> previousBuild = build;
        for (int i = 0; i < numberOfBuilds; ++i) {
            try {
                metricsStringBuilder.append(parseMetricsFromBuild(previousBuild));
            } catch (InterruptedException e) {

            } catch (IOException e) {

            } finally {
                previousBuild = previousBuild.getPreviousNotFailedBuild();
                if (previousBuild == null) {
                    break;
                }
            }
        }
        return metricsStringBuilder.toString();
    }

    @Override
    public String parseResultsFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        JSONArray results = new JSONArray();
        Run<?, ?> previousBuild = build;
        for (int i = 0; i < numberOfBuilds; ++i) {
            try {
                results.put(parseResultsFromBuild(previousBuild));
            } catch (InterruptedException e) {

            } catch (IOException e) {

            } finally {
                previousBuild = previousBuild.getPreviousNotFailedBuild();
                if (previousBuild == null) {
                    break;
                }
            }
        }
        return restructureResultsArray(results).toString();
    }

    private JSONArray parseResultsFromBuild(Run<?, ?> build)
            throws InterruptedException, IOException {
        JSONArray results = new JSONArray();
        File previousMetricsDir = new File(new File(build.getRootDir().getPath(), Paths.BASE), Paths.RESULTS);
        FilePath[] resultsFilePaths = new FilePath(previousMetricsDir).list("*.csv");
        for (FilePath resultsFilePath : resultsFilePaths) {
            JSONObject result = parseResultsFromString(resultsFilePath.getBaseName(), resultsFilePath.readToString(), build.getNumber());
            results.put(result);
        }
        return results;
    }

    private JSONObject parseResultsFromString(String resultName, String csvResults, int buildNumber) {
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        // result files are structured like
        // failures, successes, skipped, errors\n${failure}, ${success}, ${skipped}, ${error}`;
        String results = csvResults.split("[\\r\\n]+")[1];

        JSONArray failures = new JSONArray();
        failures.put(buildNumber);
        failures.put(Integer.parseInt(results.split(",")[0].trim()));
        data.put("failures", failures);

        JSONArray successes = new JSONArray();
        successes.put(buildNumber);
        successes.put(Integer.parseInt(results.split(",")[1].trim()));
        data.put("successes", successes);

        JSONArray skipped = new JSONArray();
        skipped.put(buildNumber);
        skipped.put(Integer.parseInt(results.split(",")[2].trim()));
        data.put("skipped", skipped);

        JSONArray errors = new JSONArray();
        errors.put(buildNumber);
        errors.put(Integer.parseInt(results.split(",")[3].trim()));
        data.put("errors", errors);

        result.put(resultName, data);
        return result;
    }

    private JSONArray restructureResultsArray(JSONArray results) {
        JSONArray restructuredArray = new JSONArray();
        String[] outcomeKeys = {"failures", "successes", "skipped", "errors"};
        for (int i = 0; i < results.length(); ++i) {
            JSONArray resultArray = results.getJSONArray(i);
            for (int j = 0; j < resultArray.length(); ++j) {
                JSONObject result = resultArray.getJSONObject(j);
                assert (result.names().length() == 1);
                boolean resultIsPresent = false;
                for (int k = 0; k < restructuredArray.length(); ++k) {
                    String key = result.names().getString(0);
                    if (restructuredArray.getJSONObject(k).has(key)) {
                        for (String outcomeKey : outcomeKeys) {
                            LOGGER.log(Level.SEVERE, restructuredArray.getJSONObject(k).toString());
                            restructuredArray
                                    .getJSONObject(k)
                                    .getJSONObject(key)
                                    .getJSONArray(outcomeKey)
                                    .put(result.getJSONArray(result.names().getString(0)));
                        }
                        resultIsPresent = true;
                        break;
                    }
                }
                if (!resultIsPresent) {
                    assert (result.names().length() == 1);
                    String key = result.names().getString(0);
                    JSONObject restructured = new JSONObject();

                    JSONObject outcome = new JSONObject();
                    for (String outcomeKey : outcomeKeys) {
                        outcome.put(outcomeKey,
                                new JSONArray().put(result.getJSONObject(key).getJSONArray(outcomeKey)));
                    }
                    restructured.put(key, outcome);
                    restructuredArray.put(restructured);
                }
            }
        }
        return restructuredArray;
    }

    private JSONArray parseMetricsFromBuild(Run<?, ?> build)
            throws InterruptedException, IOException {
        JSONArray metricsArray = new JSONArray();
        File previousMetricsDir = new File(new File(build.getRootDir().getPath(), Paths.BASE), Paths.METRICS);
        FilePath[] metricFilePaths = new FilePath(previousMetricsDir).list("*.json");
        for (FilePath metricFilePath : metricFilePaths) {
//            double metricValue;
//            JSONArray metricDataPoint = new JSONArray();
//            metricDataPoint.put(build.getNumber());
//            metricDataPoint.put();
//            JSONObject metricMeausurement = new JSONObject();
//            metricMeausurement.put()
//            build.getNumber();
//            jsonArray.put()
        }
        return metricsArray;
    }

//    private JSONArray parseMetricDataPoint();
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