package edu.uncc.sis.aside.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import edu.uncc.sis.aside.AsidePlugin;



/** 
 *  Copied almost entirely from Findbugs project.
 *  a simple scheduling rule for mutually exclusivity, more-or-less copied from:
 *  http://help.eclipse.org/help30/topic/org.eclipse.platform.doc.isv/guide/runtime_jobs_rules.htm
 */
public class MutexSchedulingRule implements ISchedulingRule {

	// enable multicore
	private static final boolean MULTICORE = Runtime.getRuntime().availableProcessors() > 1;

	private static final int MAX_JOBS = Runtime.getRuntime().availableProcessors();

	private final IProject project;

	public MutexSchedulingRule(IProject project) {
		super();
		this.project = project;
	}

	public boolean isConflicting(ISchedulingRule rule) {
		if(rule instanceof MutexSchedulingRule) {
			if(project == null){
				// we don't know the project, so better to say we have conflict
				return true;
			}
			MutexSchedulingRule mRule = (MutexSchedulingRule) rule;
			if(MULTICORE) {
				return mRule.project.equals(project) || tooManyJobsThere();
			}
			return true;
		}
		return false;
	}

	private static boolean tooManyJobsThere() {
		Job[] fbJobs = Job.getJobManager().find(AsidePlugin.class);
		int runningCount = 0;
		for (Job job : fbJobs) {
			if(job.getState() == Job.RUNNING){
				runningCount ++;
			}
		}
		// TODO made this condition configurable
		return runningCount > MAX_JOBS;
	}

	public boolean contains(ISchedulingRule rule) {
		if(rule instanceof IProject && project != null){
			return project.equals(rule);
		}
		return isConflicting(rule);
		/* from the URL above: "If you do not need to create hierarchies of locks,
		   you can implement the contains method to simply call isConflicting." */
	}

	@Override
	public String toString() {
		return "MutexSchedulingRule, project: " + project;
	}

}
