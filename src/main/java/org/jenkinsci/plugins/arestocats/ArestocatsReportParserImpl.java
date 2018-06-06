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
        LOGGER.log(Level.SEVERE, "---");
        LOGGER.log(Level.SEVERE, results.toString());
        LOGGER.log(Level.SEVERE, "---");
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

}
