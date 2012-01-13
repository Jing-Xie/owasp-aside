package edu.uncc.sis.aside.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ValidationRulesResetJob extends Job {

	public ValidationRulesResetJob(String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		return Status.OK_STATUS;
	}
	
	public void scheduleNonInteractive(){
		setSystem(true);
		setPriority(Job.SHORT);
		schedule();
	}
}
