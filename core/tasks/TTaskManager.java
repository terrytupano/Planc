/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package core.tasks;

import gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.swing.*;
import javax.xml.bind.*;

import org.mvel2.*;

import action.*;

import com.alee.laf.progressbar.*;
import com.alee.utils.swing.*;

import core.*;
import core.datasource.*;

/**
 * Main class for managin all background task and task in general.
 * 
 * @author terry
 * 
 */
public class TTaskManager {

	private static ScheduledExecutorService scheduledExecutorService;
	private static ThreadPoolExecutor executorService;
	private static UIListUpdater listUpdater;
	private static WebProgressBar progressBar;
	private static int poolSize = 3;
	private static Hashtable<String, Future> hashtable;

	/**
	 * Initialize {@link TTaskManager}. crating {@link ExecutorService} submit internal services and other object needed
	 * to normal running.
	 */
	public static void init() {
		scheduledExecutorService = Executors.newScheduledThreadPool(poolSize);
		executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
		hashtable = new Hashtable<String, Future>();
		progressBar = new WebProgressBar(0, poolSize);
		progressBar.setStringPainted(true);
		// Values updater
		ComponentUpdater.install(progressBar, "", 1000, new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateTaskBar();
			}
		});

		// check inactivity every ten second
		scheduleAtFixedRate(new CheckInactivity(), 10, 10, TimeUnit.SECONDS);
		// check request frame maximized everty 1/4 seg
		scheduleAtFixedRate(new RequestMaximized(), 5000, 250, TimeUnit.MILLISECONDS);
		// refresh active UIListPanel every 5 second
		listUpdater = new UIListUpdater();
		scheduleAtFixedRate(listUpdater, 10, 1, TimeUnit.SECONDS);
	}

	/**
	 * return the progress bar used by this class to update the status of active task, queque task, etc.
	 * 
	 * @return progress bar
	 */
	public static WebProgressBar getProgressBar() {
		return progressBar;
	}

	private static void updateTaskBar() {
		int ac = executorService.getActiveCount();
		int qz = executorService.getQueue().size();
		progressBar.setEnabled(ac > 0);
		progressBar.setValue(ac);
		progressBar.setString("Activas: " + ac + " en espera: " + qz);
		float f = Math.abs((float) ((ac * .3 / poolSize) - .3)); // from green to red
		Color c = new Color(Color.HSBtoRGB(f, .85f, .85f));
		progressBar.setProgressTopColor(c);
	}

	/**
	 * return a {@link UIListUpdater} used to refresh actives instances of {@link UIListPanel}
	 * 
	 * @return updater
	 */
	public static UIListUpdater getListUpdater() {
		return listUpdater;
	}

	public static Future submitCallable(TCallable tca, TaskListener tl, boolean ab) {
		Future f = executorService.submit(tca);
		hashtable.put(tca.getClass().getSimpleName(), f);
		tca.setFuture(f, ab);
		if (tl != null) {
			SignalTaskListener stl = new SignalTaskListener(tl, f);
			Future myf = scheduleAtFixedRate(stl, 1, 1, TimeUnit.SECONDS);
			stl.setMyFuture(myf);
		}
		return f;
	}

	public static Future submitRunnable(TRunnable run, TaskListener tl) {
		Future f = executorService.submit(run);
		hashtable.put(run.getClass().getSimpleName(), f);
		if (tl != null) {
			SignalTaskListener stl = new SignalTaskListener(tl, f);
			Future myf = scheduleAtFixedRate(stl, 1, 1, TimeUnit.SECONDS);
			stl.setMyFuture(myf);
		}
		return f;
	}

	public static void executeTask(Runnable ra) {
		executorService.execute(ra);
	}

	public static ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
}

/**
 * Class that notify a {@link TaskListener} when {@link Future} asocitated task finish its work. when
 * {@link Future#isDone()} = <code>true</code>, {@link TaskListener#taskDone(Future)} is called and this runnable ends.
 * 
 * @author terry
 * 
 */
class SignalTaskListener implements Runnable {

	private TaskListener listener;
	private Future future;
	private Future myFuture;

	public SignalTaskListener(TaskListener tl, Future f) {
		this.listener = tl;
		this.future = f;
	}

	@Override
	public void run() {
		if (future.isDone()) {
			listener.taskDone(future);
			myFuture.cancel(false);
		}
	}

	/**
	 * set the future (of this runnable instance). this object is used to cancel this runnable instance
	 * 
	 * @param myf - future
	 */
	public void setMyFuture(Future myf) {
		this.myFuture = myf;
	}
}

/**
 * read the comunication file seraching the {@link TPreferences#REQUEST_MAXIMIZE} messege send by another instance of
 * this aplication trying to running
 * 
 * @author terry
 * 
 */
class RequestMaximized implements Runnable {

	@Override
	public void run() {
		// 180203: during open/save native file dialog comm file is unreacheble !?!?! O.o Why ?? i don't fucking know
		// !!!
		String rm = TPreferences.readMessage(TPreferences.REQUEST_MAXIMIZE);
		if (rm != null && rm.equals("true")) {
			PlanC.frame.setState(JFrame.NORMAL);
			TPreferences.sendMessage(TPreferences.REQUEST_MAXIMIZE, "false");
		}
	}
}

/**
 * check the inactivity time. If this time es reach, display signin dialgog
 * 
 * @author terry
 */
class CheckInactivity implements Runnable, MouseMotionListener {
	private static long lastMouseMove;
	private static int signOut;

	CheckInactivity() {
		signOut = SystemVariables.getintVar("signout") * 60 * 1000;
		lastMouseMove = System.currentTimeMillis();
		PlanC.frame.addMouseMotionListener(this);

	}
	@Override
	public void run() {
		if (((System.currentTimeMillis() - lastMouseMove) > signOut) && (Session.getUser() != null)) {
			SignOut.signOut();
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		lastMouseMove = System.currentTimeMillis();
	}
}

class ExecuteTaskAndExit implements Runnable {

	String code;
	String taskN;
	public ExecuteTaskAndExit(String tn) {
		this.taskN = tn;
		PlanC.logger.log(Level.INFO, "MVEL task " + taskN + " created.");
		DBAccess dba = ConnectionManager.getAccessTo("t_report_definition");
		Record r = dba.exist("t_rdname = '" + tn + "'");
		String cod64 = (String) r.getFieldValue("t_rdcode");
		this.code = new String(DatatypeConverter.parseBase64Binary(cod64));
	}
	@Override
	public void run() {
		try {
			Serializable s = MVEL.compileExpression(code);
			MVEL.executeExpression(s, new HashMap());
			PlanC.logger.log(Level.INFO, "MVEL task " + taskN + " Ended.");
			Exit.shutdown();
		} catch (Exception e) {
			PlanC.logger.logp(Level.SEVERE, null, null, e.getMessage(), e);
			Exit.shutdown();
		}
	}
}
