package plugin.planc.security;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * display tree build by {@link UserAutorizationsTransaction}
 * 
 */
public class UserAutorizationsTree extends TAbstractTree implements CellEditorListener, DockingComponent {

	private ServiceRequest serviceRequest;
	private MenuActionFactory usrAutCalc, payrollImport;
	private Record usrRcd;

	public UserAutorizationsTree() {
		super(null, "node", "name", "sub_node");
		usrAutCalc = new MenuActionFactory("sle.aut.cactypes", "UserAutorizationsCalcTypes", this);
		usrAutCalc.setScope(TAbstractAction.TABLE_SCOPE);
		payrollImport = new MenuActionFactory("sle.aut.payroll_I", "UserAutorizationsPayrollImport", this);
		payrollImport.setScope(TAbstractAction.TABLE_SCOPE);
		this.serviceRequest = new ServiceRequest(UserAutorizationsTransaction.class.getName(), null, null);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);

		setToolBar(new UserAutorizationsAction("sle.aut.bu", "sle_user_bu", this), new UserAutorizationsAction(
				"sle.aut.co", "sle_user_company", this), new UserAutorizationsAction("sle.aut.sc",
				"sle_user_scenarios", this), new UserAutorizationsAction("sle.aut.payroll", "sle_user_payrolls", this));
		addToolBarAction(usrAutCalc, payrollImport);

		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(TConstants.TREE_ICON_FIELD, "src_file");
		putClientProperty(TConstants.TREE_BOOLEAN_FIELD, "autorized");
		putClientProperty(SLEPlanC.PLANC_ID, 50030L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa == usrAutCalc) {
			pane = new UserAutorizationsCalcTypes(usrRcd);
		}
		if (aa == payrollImport) {
			pane = new UserAutorizationsPayrollImport(usrRcd);
		}
		return pane;
	}

	public void init() {
		enableActions(TAbstractAction.TABLE_SCOPE, false);
		setMessage("sle.ui.msg16");
		setVisibleToolBar(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// parametros segun usuario seleccionado
		if ((src instanceof UserList) && prp.equals(TConstants.RECORD_SELECTED)) {
			usrRcd = (Record) evt.getNewValue();
			serviceRequest.setData((usrRcd == null) ? null : usrRcd.getFieldValue("id"));
			enableActions(TAbstractAction.TABLE_SCOPE, (usrRcd != null));
			// refresh and find not available yet (WARNING: addtoolbaraction change the order of this two !!)
			TAbstractAction[] tbb = getToolBarActions();
			tbb[tbb.length - 3].setEnabled(false);
			tbb[tbb.length - 4].setEnabled(false);
			setMessage(usrRcd == null ? "sle.ui.msg16" : "sle.ui.msg19");
		}

		// parametros segun vista seleccionada
		if (prp.equals(TConstants.ACTION_PERFORMED)) {
			UserAutorizationsAction sa = (UserAutorizationsAction) evt.getNewValue();
			if (sa != null) {
				// refresh and filter
				enableActions(TAbstractAction.TABLE_SCOPE, true);
				String fn = (String) sa.getValue(TAbstractAction.ICON_ID);
				serviceRequest.setTableName(fn);
				setServiceRequest(serviceRequest);
				markLeafNodes();
				getNodeEditor().addCellEditorListener(this);
			}
		}
		// at end to show toolbar anytime
		setVisibleToolBar(true);
	}
	@Override
	public void filterTree(String text) {
		super.filterTree(text);
		JTree jt = getJTree();
		for (int i = 0; i < jt.getRowCount(); i++) {
			jt.expandRow(i);
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		AbstractCellEditor edt = (AbstractCellEditor) e.getSource();
		Record r = (Record) ((TEntry) edt.getCellEditorValue()).getKey();
		boolean aval = (Boolean) r.getFieldValue("autorized");
		String tableN = (String) r.getFieldValue("src_file");
		Integer usrId = new Integer(r.getFieldValue("sub_node").toString());

		// model according tablename
		DBAccess dba = ConnectionManager.getAccessTo(tableN);
		Record mod = dba.getModel();

		// key fields
		mod.setFieldValue(0, usrId);
		String no = (String) r.getFieldValue("node");
		String[] kls = no.split("<terry>");
		mod.setFieldValue(1, kls[0]);
		if (kls.length > 1) {
			mod.setFieldValue(2, kls[1]);
		}

		ServiceConnection.sendTransaction(aval ? ServiceRequest.DB_ADD : ServiceRequest.DB_DELETE, tableN, mod);
	}

	@Override
	public void editingCanceled(ChangeEvent e) {

	}
}
