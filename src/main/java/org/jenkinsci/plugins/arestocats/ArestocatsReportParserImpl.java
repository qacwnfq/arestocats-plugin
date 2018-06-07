package org.jenkinsci.plugins.arestocats;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArestocatsReportParserImpl implements ArestocatsReportParser {
    @Override
    public String parseMetricsFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        return parseMetricsFromBuildsJSONArray(build, numberOfBuilds).toString();
    }

    @Override
    public String parseResultsSummaryFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        JSONArray allResults = parseResultsFromBuildsJSONArray(build, numberOfBuilds);
        for (int i = 0; i < allResults.length(); ++i) {
            if (allResults.getJSONObject(i).has("summary")) {
                return allResults.getJSONObject(i).toString();
            }
        }
        return "[]";
    }

    @Override
    public String parseResultsFromBuilds(Run<?, ?> build, int numberOfBuilds) {
        return parseResultsFromBuildsJSONArray(build, numberOfBuilds).toString();
    }

    public JSONArray parseResultsFromBuildsJSONArray(Run<?, ?> build, int numberOfBuilds) {
        JSONArray results = new JSONArray();
        List<Run<?, ?>> buildsToParse = new ArrayList();
        Run<?, ?> previousBuild = build;
        for (int i = 0; i < numberOfBuilds; ++i) {
            if (previousBuild.getResult() != Result.FAILURE) {
                buildsToParse.add(previousBuild);
            }
            if (previousBuild.getPreviousNotFailedBuild() == null) {
                previousBuild = previousBuild.getPreviousBuild();
                if (previousBuild == null) {
                    break;
                }
            } else {
                previousBuild = previousBuild.getPreviousNotFailedBuild();
            }
        }
        for (int i = buildsToParse.size() - 1; i >= 0; i--)
            try {
                results.put(parseResultsFromBuild(buildsToParse.get(i)));
            } catch (InterruptedException e) {
            } catch (IOException e) {
            }
        return restructureResultsArray(results);
    }

    private JSONArray parseResultsFromBuild(Run<?, ?> build)
            throws InterruptedException, IOException {
        JSONArray results = new JSONArray();
        File previousResultsDir = new File(new File(build.getRootDir().getPath(), Paths.BASE), Paths.RESULTS);
        FilePath[] resultsFilePaths = new FilePath(previousResultsDir).list("*.csv");
        for (FilePath resultsFilePath : resultsFilePaths) {
            JSONObject result = parseResultsFromString(resultsFilePath.getBaseName(), resultsFilePath.readToString(), build.getNumber());
            results.put(result);
        }
        return results;
    }

    private JSONObject parseResultsFromString(String resultName, String csvResults, int buildNumber) {
        JSONObject resultObject = new JSONObject();
        String results = csvResults.split("[\\r\\n]+")[1];
        JSONArray outcomes = new JSONArray();
        outcomes.put("skipped").put("errors").put("failed").put("successes");
        JSONArray data = new JSONArray();
        data.put(new Integer(buildNumber).toString());
        // result files are structured like
        // failures, successes, skipped, errors\n${failure}, ${success}, ${skipped}, ${error}`;
        data.put(Integer.parseInt(results.split(",")[2].trim()));
        data.put(Integer.parseInt(results.split(",")[3].trim()));
        data.put(Integer.parseInt(results.split(",")[0].trim()));
        data.put(Integer.parseInt(results.split(",")[1].trim()));
        resultObject.put("outcomes", outcomes);
        resultObject.put("data", new JSONArray().put(data));
        JSONObject labeledResultObject = new JSONObject();
        labeledResultObject.put(resultName, resultObject);
        return labeledResultObject;
    }

    private JSONArray restructureResultsArray(JSONArray results) {
        JSONArray restructuredArray = new JSONArray();
        for (int i = 0; i < results.length(); ++i) {
            JSONArray resultArray = results.getJSONArray(i);
            for (int j = 0; j < resultArray.length(); ++j) {
                JSONObject result = resultArray.getJSONObject(j);
                assert (result.names().length() == 1);
                String key = result.names().getString(0);
                boolean resultIsPresent = false;
                for (int k = 0; k < restructuredArray.length(); ++k) {
                    if (restructuredArray.getJSONObject(k).has(key)) {
                        restructuredArray
                                .getJSONObject(k)
                                .getJSONObject(key)
                                .getJSONArray("data")
                                .put(
                                        result
                                                .getJSONObject(key)
                                                .getJSONArray("data")
                                                .getJSONArray(0)
                                );
                        resultIsPresent = true;
                        break;
                    }
                }
                if (!resultIsPresent) {
                    restructuredArray.put(result);
                }
            }
        }
        return restructuredArray;
    }

    public JSONArray parseMetricsFromBuildsJSONArray(Run<?, ?> build, int numberOfBuilds) {
        JSONArray metrics = new JSONArray();
        List<Run<?, ?>> buildsToParse = new ArrayList()
        Run<?, ?> previousBuild = build;
        for (int i = 0; i < numberOfBuilds; ++i) {
                if (previousBuild.getResult() != Result.FAILURE) {
                    buildsToParse.add(previousBuild);
                }
                if (previousBuild.getPreviousNotFailedBuild() == null) {
                    previousBuild = previousBuild.getPreviousBuild();
                    if (previousBuild == null) {
                        break;
                    }
                } else {
                    previousBuild = previousBuild.getPreviousNotFailedBuild();
                }
        }
        for (int i = buildsToParse.size() - 1; i >= 0; i--)
            try {
                metrics.put(parseMetricsFromBuild(buildsToParse.get(i)));
            } catch (InterruptedException e) {
            } catch (IOException e) {
            }
        return restructureMetricsArray(metrics);
    }

    private JSONArray parseMetricsFromBuild(Run<?, ?> build)
            throws InterruptedException, IOException {
        JSONArray metrics = new JSONArray();
        File previousMetricsDir = new File(new File(build.getRootDir().getPath(), Paths.BASE), Paths.METRICS);
        FilePath[] metricsFilePaths = new FilePath(previousMetricsDir).list("*.json");
        for (FilePath metricsFilePath : metricsFilePaths) {
            JSONObject metric = parseMetricsFromString(metricsFilePath.getBaseName(), metricsFilePath.readToString(), build.getNumber());
            metrics.put(metric);
        }
        return metrics;
    }

    private JSONObject parseMetricsFromString(String resultName, String jsonMetrics, int buildNumber) {
        JSONObject metricsObject = new JSONObject();
        JSONObject inputMetrics = new JSONObject(jsonMetrics);

        JSONArray names = new JSONArray();
        JSONArray data = new JSONArray();
        JSONArray colors = new JSONArray();
        String label = inputMetrics.getJSONArray("metrics").getJSONObject(0).getString("label");
        data.put(new Integer(buildNumber).toString());
        for (int i = 0; i < inputMetrics.getJSONArray("metrics").length(); ++i) {
            names.put(inputMetrics.getJSONArray("metrics").getJSONObject(i).getString("name"));
            colors.put(inputMetrics.getJSONArray("metrics").getJSONObject(i).getString("color"));
            data.put(inputMetrics.getJSONArray("metrics").getJSONObject(i).getNumber("value"));
        }
        metricsObject.put("names", names);
        metricsObject.put("data", new JSONArray().put(data));
        metricsObject.put("color", colors);
        metricsObject.put("label", label);
        JSONObject labeledResultObject = new JSONObject();
        labeledResultObject.put(inputMetrics.getString("category"), metricsObject);
        return labeledResultObject;
    }

    private JSONArray restructureMetricsArray(JSONArray metrics) {
        JSONArray restructuredArray = new JSONArray();
        for (int i = 0; i < metrics.length(); ++i) {
            JSONArray resultArray = metrics.getJSONArray(i);
            for (int j = 0; j < resultArray.length(); ++j) {
                JSONObject metric = resultArray.getJSONObject(j);
                assert (metric.names().length() == 1);
                String key = metric.names().getString(0);
                boolean resultIsPresent = false;
                for (int k = 0; k < restructuredArray.length(); ++k) {
                    if (restructuredArray.getJSONObject(k).has(key)) {
                        restructuredArray
                                .getJSONObject(k)
                                .getJSONObject(key)
                                .getJSONArray("data")
                                .put(
                                        metric
                                                .getJSONObject(key)
                                                .getJSONArray("data")
                                                .getJSONArray(0)
                                );
                        resultIsPresent = true;
                        break;
                    }
                }
                if (!resultIsPresent) {
                    restructuredArray.put(metric);
                }
            }
        }
        return restructuredArray;
    }

}
