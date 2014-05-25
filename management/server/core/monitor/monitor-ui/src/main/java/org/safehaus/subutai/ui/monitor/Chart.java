
package org.safehaus.subutai.ui.monitor;

import com.vaadin.ui.Window;
import org.safehaus.subutai.api.monitor.Metric;
import org.safehaus.subutai.ui.monitor.util.FileUtil;
import org.safehaus.subutai.ui.monitor.util.JavaScript;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

class Chart {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String CHART_TEMPLATE = FileUtil.getContent("js/chart.js");

    private final int maxSize;
    private final JavaScript javaScript;

    Chart(Window window, int maxSize) {
        this.maxSize = maxSize;
        javaScript = new JavaScript(window);
        loadScripts();
    }

    private void loadScripts() {
        javaScript.loadFile("js/jquery.min.js");
        javaScript.loadFile("js/jquery.flot.min.js");
        javaScript.loadFile("js/jquery.flot.time.min.js");
    }

    void load(String host, Metric metric, Map<Date, Double> values) {

        String data = toPoints(values);
        String label = String.format("%s for %s", metric.toString(), host);

        String chart = CHART_TEMPLATE
                .replace( "$label", label )
                .replace( "$yTitle", metric.getUnit() )
                .replace( "$data", data );

        javaScript.execute(chart);
    }

    private String toPoints(Map<Date, Double> values) {

        String str = "";
        int i = 0;

        for (Date date : values.keySet()) {
            if (!str.isEmpty()) {
                str += ", ";
            }

            str += String.format("[Date.parse('%s'), %s ]", DATE_FORMAT.format(date), values.get(date));
            i++;

            if (i > maxSize) {
                break;
            }
        }

        return String.format("[%s]", str);
    }
}
