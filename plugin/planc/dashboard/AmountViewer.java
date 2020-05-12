package plugin.planc.dashboard;

import gui.*;
import gui.docking.*;
import gui.jtreetable.*;
import gui.tree.*;

import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.table.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;
import core.reporting.*;
import core.tasks.*;

/**
 * tree table for view sle_planc_amount
 * 
 */
public class AmountViewer extends UIComponentPanel
		implements
			PropertyChangeListener,
			DockingComponent,
			TaskListener,
			Exportable {

	private JScrollPane scrollPane;
	private TDefaultTreeModel underModel;
	private AmountViewTreeTableModel treeTableModel;
	private Future<Hashtable<String, ServiceRequest>> future;
	private JTreeTable treeTable;
	private String lastView;
	private Hashtable<String, ServiceRequest> services;
	private ExportToFileAction exportToFileAction;
	private static AmountViewer amountViewer;
	private static String companyId = "", scenaryId = "";

	public AmountViewer() {
		super(null, false);
		this.scrollPane = new JScrollPane();
		this.services = null;
		underModel = new TDefaultTreeModel("av_path", "av_name", null);
		scrollPane.setBorder(null);
		setToolBar(new WorkforceView(this), new AccountView(this), new BUView(this));
		exportToFileAction = new ExportToFileAction(this,
				"av_src_file;av_path;av_formula_eval;av_formula_expr;av_pattern;av_srcrecord");
		addToolBarAction(exportToFileAction);
		RefreshAction ra = new RefreshAction(this) {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				recalc();
			}
		};
		ra.setIcon("RefreshAction");
		ra.setToolTip("ttaction.RefreshAction");
		addToolBarAction(ra);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		amountViewer = this;
		add(scrollPane);
	}

	public static AmountViewer getInstance() {
		return amountViewer;
	}

	@Override
	public void init() {
		setMessage("sle.ui.msg23");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object newv = evt.getNewValue();
		String prp = evt.getPropertyName();
		// System.out.println(prp + "=" + evt.getNewValue());

		// action performed for change view
		if (newv != null && newv instanceof RedirectAction) {
			changeView(newv.getClass().getSimpleName());
		}

		// path selected recalc on firs time or when change scenario
		if (prp.equals(TConstants.PATH_SELECTED)) {
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				String cia = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				String sc = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
				if (!scenaryId.equals(sc) || !companyId.equals(cia)) {
					companyId = cia;
					scenaryId = sc;
					recalc();
				}
			} else {
				setMessage("sle.ui.msg23");
			}

		}
	}

	public void recalc() {
		// cancel active task
		if (future != null) {
			future.cancel(true);
		}
		AmountViewerTask worker = new AmountViewerTask(companyId, scenaryId, getWaitComponent());
		future = TTaskManager.submitCallable(worker, this, false);
		setMessage(null);
		performTransition(getWaitComponent());

		/*
		 * try { services = command.doInBackground(); changeView(lastView);
		 * 
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
	}

	/**
	 * change the view according to selected action
	 * 
	 * @param vn - class name of view action
	 */
	private void changeView(String vn) {
		if (vn == null) {
			vn = WorkforceView.class.getSimpleName();
		}
		// setVisible(false);
		lastView = vn;
		ServiceRequest sr = services.get(vn);
		underModel.setServiceRequest(sr);
		treeTableModel = new AmountViewTreeTableModel(underModel);
		treeTable = new JTreeTable(treeTableModel);
		scrollPane.setViewportView(treeTable);

		// preferred column with
		TableColumnModel tcm = treeTable.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(400);
		for (int i = 1; i < tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setPreferredWidth(100);
		}
		// auto expand company and scenary nodes
		treeTable.getTree().expandRow(0);
		treeTable.getTree().expandRow(1);
		// treeTable.getTree().expandRow(2);
		// setVisible(true);

		performTransition(getComponentPanel());
	}

	@Override
	public void taskDone(Future f) {
		try {
			if (!future.isCancelled()) {
				services = future.get();
				changeView(lastView);
				exportToFileAction.addParameter("viewName", lastView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ServiceRequest getServiceRequest() {
		return services.get(lastView);
	}
}
