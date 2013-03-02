package org.jenkinsci.plugins.stepcounter;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jenkinsci.plugins.stepcounter.util.Constants;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class StepCounterProjectAction implements Action {

    private AbstractProject<?, ?> project;

    public StepCounterProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getDisplayName() {
        return Messages.StepCounterAction_Title();
    }

    public String getIconFileName() {
        return Constants.ACTION_ICON;
    }

    public String getUrlName() {
        return Constants.ACTION_URL;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            response.sendRedirect2(String.format("../%d/%s", build.getNumber(), Constants.ACTION_URL));
        }
    }

    public void doTrend(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
//        StepCounterResultAction a = getPreviousResult();
//        if (a != null)
//            a.createGraph(req, rsp);
//        else
            rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(StepCounterResultAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

}
