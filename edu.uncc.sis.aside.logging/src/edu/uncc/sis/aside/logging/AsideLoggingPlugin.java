package edu.uncc.sis.aside.logging;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AsideLoggingPlugin extends Plugin {

	private static AsideLoggingPlugin plugin;
	
	public AsideLoggingPlugin(){
		super();
	}
	
	ArrayList<AsideLoggingManager> loggingManagers = new ArrayList<AsideLoggingManager>();
	
	public static AsideLoggingPlugin getDefault(){
		if(plugin == null)
			plugin = new AsideLoggingPlugin();
		return plugin;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		synchronized (this.loggingManagers) {
			Iterator<AsideLoggingManager> it = this.loggingManagers.iterator();
			while (it.hasNext()) {
				AsideLoggingManager logManager = (AsideLoggingManager) it.next();
				logManager.internalShutdown(); 
			}
			this.loggingManagers.clear(); 
		}
		super.stop(context);
	}

	/**
	 * Adds a log manager object to the list of active log managers
	 */	
	void addLogManager(AsideLoggingManager logManager) {
		synchronized (this.loggingManagers) {
			if (logManager != null)
				this.loggingManagers.add(logManager); 
		}
	}
	
	/**
	 * Removes a log manager object from the list of active log managers
	 */
	void removeLogManager(AsideLoggingManager logManager) {
		synchronized (this.loggingManagers) {
			if (logManager != null)
				this.loggingManagers.remove(logManager); 
		}
	}
	
}
