package org.safehaus.kiskis.mgmt.ui.monitor.service;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.kiskis.mgmt.ui.monitor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Query {

    private final static Logger LOG = LoggerFactory.getLogger(Query.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String QUERY = FileUtil.getContent("elasticsearch/query.json");

    public static Map<Date, Double> execute(String host, String metric, Date startDate, Date endDate) {

        Map<Date, Double> data = Collections.emptyMap();

        try {
            data = doExecute(host, metric, startDate, endDate);
        } catch (Exception e) {
            LOG.error("Error while executing query: ", e);
        }

        return data;
    }

    private static Map<Date, Double> doExecute(String host, String metricName, Date startDate, Date endDate) throws Exception {

        String query = QUERY
                .replace("$host", host)
                .replace("$metricName", metricName)
                .replace("$startDate", dateToStr(startDate) )
                .replace("$endDate", dateToStr(endDate) );

        String response = HttpPost.execute(query);
        List<JsonNode> nodes = toNodes(response);

        LOG.info("nodes count: {}", nodes.size());

        // Reverse the list b/c the query returns the data in desc order (to get the latest values first).
        Collections.reverse(nodes);

        return toMap(nodes);
    }

    private static List<JsonNode> toNodes(String response) throws IOException {

        JsonNode json = OBJECT_MAPPER.readTree(response);
        JsonNode hits = json.get("hits").get("hits");

        ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();

        for (int i = 0; i < hits.size(); i++) {
            JsonNode node = hits.get(i).get("_source");
            nodes.add(node);

            LOG.info("node: {}", node);
        }

        return nodes;
    }

    private static Map<Date, Double> toMap(List<JsonNode> nodes) {

        Map<Date, Double> values = new LinkedHashMap<Date, Double>();

        for (JsonNode node : nodes) {
            Date date = strToDate( node.get("@timestamp").asText() );
            double value = node.get("val").asDouble();
            values.put(date, value);
        }

        return values;
    }

    private static Date strToDate(String dateStr) {

        String target = dateStr.replace("T", " ").replace("Z", "");
        Date date = null;

        try {
            date = DATE_FORMAT.parse(target);
        } catch (ParseException e) {
            LOG.error("Error while parsing time: ", e);
        }

        return date;
    }

    private static String dateToStr(Date date) {
        return DATE_FORMAT.format(date)
                .replace(" ", "T");
    }
}
