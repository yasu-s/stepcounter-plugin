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

public class StepGraph extends Graph {

    private StepCounterResultAction obj;

    public StepGraph(StepCounterResultAction obj, Calendar timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
        this.obj = obj;
    }

    protected JFreeChart createGraph() {
        final CategoryDataset dataset = createDataSet(obj).build();
        final JFreeChart chart = ChartFactory.createLineChart(null, // chart
                // title
                null, // unused
                Messages.StepTrend_Label(), // range axis label
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

        StackedAreaRenderer ar = new StackedAreaRenderer2();

        plot.setRenderer(ar);

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
                dsb.add(entry.getValue().getRunsSum(), Messages.StepTrend_RunLine(), label);
                dsb.add(entry.getValue().getCommentsSum(), Messages.StepTrend_CommentLine(), label);
                dsb.add(entry.getValue().getBlanksSum(), Messages.StepTrend_BlankLine(), label);
            }
        }
        return dsb;
    }

}
