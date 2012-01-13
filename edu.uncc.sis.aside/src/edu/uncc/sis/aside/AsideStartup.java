package edu.uncc.sis.aside;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Jing Xie (jxie2 at uncc dot edu) <a href="http://www.uncc.edu/">UNC
 *         Charlotte</a>
 * 
 */
public class AsideStartup implements IStartup {
	private static final Logger logger = AsidePlugin.getLogManager().getLogger(
			AsideStartup.class.getName());

	@Override
	public void earlyStartup() {
		logger.info("ASIDE is starting up along with the launch of Eclipse platform");
	}

}
