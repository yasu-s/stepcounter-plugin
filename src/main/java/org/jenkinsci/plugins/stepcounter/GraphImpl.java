package org.jenkinsci.plugins.stepcounter;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jenkinsci.plugins.stepcounter.model.StepCounterResult;
import org.jenkinsci.plugins.stepcounter.util.Constants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

public class GraphImpl extends Graph {

    private StepCounterResultAction obj;
    private String relPath;
    private Map<Integer, Color> _colors = new HashMap<Integer, Color>();

    public GraphImpl(StepCounterResultAction obj, Calendar timestamp, int defaultW, int defaultH, String relPath) {
        super(timestamp, defaultW, defaultH);
        this.obj = obj;
        this.relPath = relPath;

        for (Field field : Color.class.getDeclaredFields()) {
            if (field.getClass().equals(Color.class)) {
                try {
                    Color color = (Color) field.get(null);
                    if (color != null && !_colors.containsKey(color.getRGB())) {
                        _colors.put(color.getRGB(), color);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected JFreeChart createGraph() {
        final CategoryDataset dataset = createDataSet(obj).build();
        final JFreeChart chart = ChartFactory.createLineChart(null, // chart
                // title
                null, // unused
                "Steps", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0,
        // 5.0));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRange(true);
        // rangeAxis.setLowerBound(0);

        // final LineAndShapeRenderer renderer = (LineAndShapeRenderer)
        // plot.getRenderer();
        // renderer.setBaseStroke(new BasicStroke(4.0f));
        // ColorPalette.apply(renderer);

        @SuppressWarnings("serial")
        StackedAreaRenderer ar = new StackedAreaRenderer2() {
            @Override
            public String generateURL(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                return relPath + label.build.getNumber() + "/"
                        + Constants.ACTION_URL + "/";
            }

            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                StepCounterResultAction a = label.build.getAction(StepCounterResultAction.class);
                Map<String, StepCounterResult> stepsMap = a.getStepsMap();
                Comparable<?> key = dataset.getRowKey(row);
                for (Entry<String, StepCounterResult> entry : stepsMap.entrySet()) {
                    if (entry.getKey().equals(key)) {
                        return "[" + entry.getKey() + "] ビルド " + label.build.getDisplayName() + " 合計:"
                                + entry.getValue().getTotalSum() + " 実行:" + entry.getValue().getRunsSum()
                                + " コメント:" + entry.getValue().getCommentsSum() + " 空行:"
                                + entry.getValue().getBlanksSum();
                    }
                }

                return "不明";
            }
        };
        plot.setRenderer(ar);

        for (int i = 0; i < ar.getColumnCount(); i++) {
            ar.setSeriesPaint(i, _colors.get(_colors.values().toArray()[i]));
        }

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }


    protected DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet(StepCounterResultAction obj) {
        DataSetBuilder<String, NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        for (StepCounterResultAction a = obj; a != null; a = a.getPreviousResult()) {
            Map<String, StepCounterResult> stepsMap = a.getStepsMap();
            NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(a.getBuild());
            for (Entry<String, StepCounterResult> entry : stepsMap.entrySet()) {
                dsb.add(entry.getValue().getTotalSum(), entry.getKey(), label);
            }
        }
        return dsb;
    }

}
