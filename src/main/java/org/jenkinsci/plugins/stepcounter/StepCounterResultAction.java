package org.jenkinsci.plugins.stepcounter;

import hudson.Util;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.util.ChartUtil;
import hudson.util.Graph;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.stepcounter.model.StepCounterResult;
import org.jenkinsci.plugins.stepcounter.util.Constants;
import org.jenkinsci.plugins.stepcounter.util.StepCounterUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class StepCounterResultAction implements Action {

    /**
     * カテゴリごとのステップ数集計結果。キーはカテゴリ名
     */
    private Map<String, StepCounterResult> _stepsMap = new HashMap<String, StepCounterResult>();

    private AbstractBuild<?, ?> owner;

    private long allRunsSum     = 0;
    private long allCommentsSum = 0;
    private long allBlanksSum   = 0;
    private long allTotalSum    = 0;
    private long allFileNum     = 0;

    public StepCounterResultAction(AbstractBuild<?, ?> build) {
        this.owner = build;
    }

    public String getDisplayName() {
        return Messages.StepCounterProjectAction_Title();
    }

    public String getIconFileName() {
        return Constants.ACTION_ICON;
    }

    public String getUrlName() {
        return Constants.ACTION_URL;
    }

    public AbstractBuild<?, ?> getBuild() {
        return this.owner;
    }

    public long getAllRunsSum() {
        return allRunsSum;
    }

    public long getAllCommentsSum() {
        return allCommentsSum;
    }

    public long getAllBlanksSum() {
        return allBlanksSum;
    }

    public long getAllTotalSum() {
        return allTotalSum;
    }

    public long getAllFileNum() {
        return allFileNum;
    }

    public String getCommentsSumPercent() {
        return StepCounterUtil.convertPercent(allCommentsSum, allTotalSum);
    }

    public String getBlanksSumPercent() {
        return StepCounterUtil.convertPercent(allBlanksSum, allTotalSum);
    }

    public String getRunsSumPercent() {
        return StepCounterUtil.convertPercent(allRunsSum, allTotalSum);
    }

    public Map<String, StepCounterResult> getStepsMap() {
        return this._stepsMap;
    }

    public StepCounterResult getPreviousStepsMap(String key) {
        StepCounterResultAction action = getPreviousResult();
        if (action == null) return null;
        return action.getStepsMap().get(key);
    }

    public void putStepsMap(String key, StepCounterResult result) {
        _stepsMap.put(key, result);

        allRunsSum     += result.getRunsSum();
        allCommentsSum += result.getCommentsSum();
        allBlanksSum   += result.getBlanksSum();
        allTotalSum    += result.getTotalSum();
        allFileNum     += result.getFileSteps().size();
    }

    private String getRelPath(StaplerRequest req) {
        String relPath = req.getParameter("rel");
        if (relPath == null)
            return "";
        return relPath;
    }

    public void doTrend(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        Graph graph = createDefaultGraph(req, rsp);
        graph.doPng(req, rsp);
    }

    public void doTrendMap(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        Graph graph = createDefaultGraph(req, rsp);
        graph.doMap(req, rsp);
    }

    public void doStepTrend(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        Graph graph = new StepGraph(this, getBuild().getTimestamp(), 800, 200);
        graph.doPng(req, rsp);
    }

    private Graph createDefaultGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        final String relPath = getRelPath(req);

        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return null;
        }

        AbstractBuild<?, ?> build = getBuild();
        Calendar t = build.getTimestamp();

        String w = Util.fixEmptyAndTrim(req.getParameter("width"));
        String h = Util.fixEmptyAndTrim(req.getParameter("height"));
        int width = (w != null) ? Integer.valueOf(w) : 500;
        int height = (h != null) ? Integer.valueOf(h) : 200;

        Graph graph = new CategoryGraph(this, t, width, height, relPath);
        return graph;
    }


    public StepCounterResultAction getPreviousResult() {
        AbstractBuild<?, ?> b = owner;
        while (true) {
            b = b.getPreviousBuild();
            if (b == null)
                return null;
            if (b.getResult() == Result.FAILURE)
                continue;
            StepCounterResultAction r = b.getAction(StepCounterResultAction.class);
            if (r != null)
                return r;
        }
    }

}
