package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * list for SLE_PLANC_GEN_ACCOUNT but showing records from SLE_PLANC_ACCOUNT
 * 
 */
public class AccountGeneration extends UIListPanel implements DockingComponent {

	private ServiceRequest serviceRequest;
	private String scenaryId;
	private Long accountId;
	private DBAccess dbAccess;
	private EditRecord2 editAction;
	private DeleteRecord2 deleteAction;

	public AccountGeneration() {
		super(null);
		editAction = new EditRecord2(this);
		editAction.setToolTip("ttaccgentree.edit");
		editAction.setMessagePrefix("accgentree.edit.");
		editAction.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("Gear", "EditRecord", 16));
		editAction.setAllowWrite(true);
		deleteAction = new DeleteRecord2(this) {
			@Override
			public void actionPerformed2() {
				Record rcd = dbAccess.getModel();
				rcd.setFieldValue("scenario_id", scenaryId);
				rcd.setFieldValue("account_id", accountId);
				boolean ok = dbAccess.delete(rcd);
				if (ok) {
					freshen();
				}
			}
		};
		deleteAction.setToolTip("ttaccgentree.delete");
		deleteAction.setMessagePrefix("accgentree.");
		deleteAction.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("Gear", "DeleteRecord", 16));

		setToolBar(editAction, deleteAction);
		putClientProperty(TConstants.SHOW_COLUMNS, "id;name;type_id;with_increase");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(SLEPlanC.PLANC_ID, 70360L);
	}

	@Override
	protected void enableRecordScopeActions(boolean ena) {
		super.enableRecordScopeActions(ena);
		Record r = getRecord();
		boolean enadel = false;
		boolean enaedt = false;
		if (r != null) {
			// save the account for delete action
			accountId = (Long) r.getFieldValue("id");

			// but 180228: only allow new/edit for "personalizada y planificada" account types
			long at = (Long) r.getFieldValue("type_id");
			enaedt = at == 0 || at == 15;

			// enable detele if this account has associated generation parameter
			enadel = (Boolean) r.getFieldValue("hasGenAcc");
		}
		deleteAction.setEnabled(enadel);
		editAction.setEnabled(enaedt);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof EditRecord2) {
			Record r = getRecord();
			accountId = (Long) r.getFieldValue("id");
			Record rcd = dbAccess.exist("scenario_id = '" + scenaryId + "' AND account_id = " + accountId);
			boolean nr = rcd == null;
			if (nr) {
				rcd = dbAccess.getModel();
				rcd.setFieldValue("scenario_id", scenaryId);
				rcd.setFieldValue("account_id", accountId);
			}
			pane = new AccountGenerationRecord(rcd, nr);
		}
		return pane;
	}

	public void init() {
		serviceRequest = new ServiceRequest(AccountGenerationTransaction.class.getName(), "", scenaryId);
		dbAccess = ConnectionManager.getAccessTo("sle_planc_gen_account");
		setMessage("sle.ui.msg03");

		TEntry[] te = TStringUtils.getTEntryGroup("ag.increase_");
		setReferenceColumn("with_increase", te);
		te = SLEPlanC.getTEntryGroupFromDB("accountType");
		TEntry[] te1 = new TEntry[te.length + 1];
		System.arraycopy(te, 0, te1, 0, te.length);
		te1[te1.length - 1] = new TEntry("0; ");
		setReferenceColumn("type_id", te1);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		// Object newval = evt.getNewValue();

		// only respond to path selection
		if (prp.equals(TConstants.PATH_SELECTED)) {
			scenaryId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			}
			if (scenaryId != null) {
				serviceRequest.setData(scenaryId);
				setServiceRequest(serviceRequest);
			} else {
				setMessage("sle.ui.msg03");
			}
		}
	}
}
