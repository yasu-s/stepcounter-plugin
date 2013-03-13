package org.jenkinsci.plugins.stepcounter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.stepcounter.util.StepCounterUtil;

public class StepCounterResult implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	Map<String, FileStep> _steps = new HashMap<String, FileStep>();

	List<String> errorMessages = new ArrayList<String>();

	long blankSum = 0;
	long runSum = 0;
	long commentSum = 0;
	long totalSum = 0;

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void addErrorMessage(String errorMessage) {
		this.errorMessages.add(errorMessage);
	}

	public Map<String, FileStep> getFileSteps() {
		return _steps;
	}

	public FileStep getFileStep(String key) {
	    return _steps.get(key);
	}

	public void setFileSteps(Map<String, FileStep> steps) {
		this._steps = steps;
		totalSum = 0;
		commentSum = 0;
		blankSum = 0;
		runSum = 0;
		for (FileStep fileStep : steps.values()) {
			totalSum += fileStep.getTotal();
			commentSum += fileStep.getComments();
			blankSum += fileStep.getBlanks();
			runSum += fileStep.getRuns();
		}
	}

	public void addFileStep(FileStep fileStep) {
		_steps.put(fileStep.getFilePath(), fileStep);
		totalSum += fileStep.getTotal();
		commentSum += fileStep.getComments();
		blankSum += fileStep.getBlanks();
		runSum += fileStep.getRuns();
	}

	public long getTotalSum() {
		return totalSum;
	}

	public long getCommentsSum() {
		return commentSum;
	}

	public long getBlanksSum() {
		return blankSum;
	}

	public long getRunsSum() {
		return runSum;
	}

	public String getCommentsSumPercent() {
	    return StepCounterUtil.convertPercent(commentSum, totalSum);
	}

	public String getBlanksSumPercent() {
        return StepCounterUtil.convertPercent(blankSum, totalSum);
    }

	public String getRunsSumPercent() {
        return StepCounterUtil.convertPercent(runSum, totalSum);
    }
}
