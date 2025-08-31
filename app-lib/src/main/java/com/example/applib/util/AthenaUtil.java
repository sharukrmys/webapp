package com.example.applib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.athena.enabled", havingValue = "true", matchIfMissing = true)
public class AthenaUtil {

    private final AthenaClient athenaClient;

    @Value("${aws.enabled:true}")
    private boolean awsEnabled;

    @Value("${aws.athena.enabled:true}")
    private boolean athenaEnabled;

    @Value("${aws.athena.output-location}")
    private String outputLocation;

    @Value("${aws.athena.query-timeout-seconds:300}")
    private long queryTimeoutSeconds;

    @Value("${aws.athena.poll-interval-ms:1000}")
    private long pollIntervalMs;

    /**
     * Execute a query and wait for the results
     *
     * @param query The SQL query to execute
     * @param database The database name
     * @return List of rows as maps
     */
    public List<Map<String, String>> executeQuery(String query, String database) {
        if (!awsEnabled || !athenaEnabled) {
            log.warn("AWS or Athena is disabled. Query execution skipped.");
            return List.of();
        }

        try {
            String queryExecutionId = startQueryExecution(query, database);
            waitForQueryToComplete(queryExecutionId);
            return getQueryResults(queryExecutionId);
        } catch (Exception e) {
            log.error("Error executing Athena query", e);
            throw new RuntimeException("Failed to execute Athena query", e);
        }
    }

    /**
     * Start a query execution
     *
     * @param query The SQL query to execute
     * @param database The database name
     * @return The query execution ID
     */
    public String startQueryExecution(String query, String database) {
        if (!awsEnabled || !athenaEnabled) {
            log.warn("AWS or Athena is disabled. Query execution skipped.");
            return null;
        }

        try {
            QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                    .database(database)
                    .build();

            ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                    .outputLocation(outputLocation)
                    .build();

            StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                    .queryString(query)
                    .queryExecutionContext(queryExecutionContext)
                    .resultConfiguration(resultConfiguration)
                    .build();

            StartQueryExecutionResponse startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest);
            return startQueryExecutionResponse.queryExecutionId();
        } catch (AthenaException e) {
            log.error("Error starting Athena query execution", e);
            throw new RuntimeException("Failed to start Athena query execution", e);
        }
    }

    /**
     * Wait for a query to complete
     *
     * @param queryExecutionId The query execution ID
     * @throws InterruptedException If the thread is interrupted
     */
    public void waitForQueryToComplete(String queryExecutionId) throws InterruptedException {
        if (!awsEnabled || !athenaEnabled) {
            log.warn("AWS or Athena is disabled. Query wait skipped.");
            return;
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.SECONDS.toMillis(queryTimeoutSeconds);

        while (System.currentTimeMillis() < endTime) {
            GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryExecutionResponse getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            QueryExecutionState state = getQueryExecutionResponse.queryExecution().status().state();

            if (state == QueryExecutionState.SUCCEEDED) {
                return;
            } else if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                throw new RuntimeException("Query failed or was cancelled. State: " + state);
            }

            // Sleep and retry
            Thread.sleep(pollIntervalMs);
        }

        // If we got here, the query timed out
        athenaClient.stopQueryExecution(StopQueryExecutionRequest.builder().queryExecutionId(queryExecutionId).build());
        throw new RuntimeException("Query timed out after " + queryTimeoutSeconds + " seconds");
    }

    /**
     * Get the results of a query
     *
     * @param queryExecutionId The query execution ID
     * @return List of rows as maps
     */
    public List<Map<String, String>> getQueryResults(String queryExecutionId) {
        if (!awsEnabled || !athenaEnabled) {
            log.warn("AWS or Athena is disabled. Query results retrieval skipped.");
            return List.of();
        }

        try {
            GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);

            List<Map<String, String>> results = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            // Process the results
            for (GetQueryResultsResponse result : getQueryResultsResults) {
                List<Row> rows = result.resultSet().rows();

                // Extract column names from the first row if not already done
                if (columnNames.isEmpty() && !rows.isEmpty()) {
                    Row headerRow = rows.get(0);
                    for (Datum datum : headerRow.data()) {
                        columnNames.add(datum.varCharValue());
                    }
                    // Skip the header row for the rest of the processing
                    rows = rows.subList(1, rows.size());
                }

                // Process each data row
                for (Row row : rows) {
                    Map<String, String> rowMap = new HashMap<>();
                    List<Datum> data = row.data();

                    for (int i = 0; i < data.size(); i++) {
                        if (i < columnNames.size()) {
                            rowMap.put(columnNames.get(i), data.get(i).varCharValue());
                        }
                    }

                    results.add(rowMap);
                }
            }

            return results;
        } catch (AthenaException e) {
            log.error("Error getting Athena query results", e);
            throw new RuntimeException("Failed to get Athena query results", e);
        }
    }
}
