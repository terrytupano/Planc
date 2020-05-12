package plugin.planc.config;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * tree view for join files SLE_CATEGORY and SLE_CATEGORY_VALUE
 * 
 */
public class CategoryTree extends TAbstractTree implements DockingComponent {

	private ServiceRequest serviceRequest;
	private NewRecord newCategory;
	private EditRecord editCategory, newCategoryValue, editCategoryValue;
	public CategoryTree() {
		super(null, "node", "name", "subnode");
		serviceRequest = new ServiceRequest(TreeViewFromParentChildrenTables.class.getName(), "SLE_CATEGORY", null);
		serviceRequest.setParameter(ServiceRequest.CHILDREN_TABLE_NAME, "SLE_CATEGORY_VALUE");
		serviceRequest.setParameter(ServiceRequest.PARENT_STRING, "name");
		serviceRequest.setParameter(ServiceRequest.CHILDREN_STRING, "code");
		serviceRequest.setParameter(ServiceRequest.PARENT_JOIN_BY, "id");
		serviceRequest.setParameter(ServiceRequest.CHILDREN_JOIN_BY, "category_id");
		serviceRequest.setParameter(ServiceRequest.NODE_FIELD, "id");
		serviceRequest.setParameter(ServiceRequest.SUB_NODE_FIELD, "id");

		// new category
		newCategory = new NewRecord(this);
		newCategory.setName("cattree.newcategory");
		newCategory.setToolTip("ttcattree.newcategory");
		newCategory.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("SLE_CATEGORY", "NewRecord", 16));

		// edit category
		editCategory = new EditRecord(this);
		editCategory.setName("cattree.editcategory");
		editCategory.setToolTip("ttcattree.editcategory");
		editCategory.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("SLE_CATEGORY", "EditRecord", 16));

		// new category value
		newCategoryValue = new EditRecord(this);
		newCategoryValue.setName("cattree.newcatvalue");
		newCategoryValue.setToolTip("ttcattree.newcatvalue");
		newCategoryValue.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("SLE_CATEGORY_VALUE", "NewRecord", 16));

		// edit category value
		editCategoryValue = new EditRecord(this);
		editCategoryValue.setName("cattree.editcatvalue");
		editCategoryValue.setToolTip("ttcattree.editcatvalue");
		editCategoryValue.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("SLE_CATEGORY_VALUE", "EditRecord", 16));

		setToolBar(newCategory, editCategory, newCategoryValue, editCategoryValue, new DeleteRecord(this));
		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(TConstants.TREE_ICON_FIELD, "icon");
//		showSeparator(true);
		putClientProperty(SLEPlanC.PLANC_ID, 70352L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		Record srcr = getRecord();
		if (aa == newCategory) {
			Record r = ConnectionManager.getAccessTo("sle_category").getModel();
			r.setFieldValue("id", System.currentTimeMillis());
			pane = new CategoryRecord(r, true);
		}
		if (aa == editCategory) {
			Record r = ConnectionManager.getAccessTo("sle_category").exist("id = " + srcr.getFieldValue("node"));
			pane = new CategoryRecord(r, false);
		}
		if (aa == newCategoryValue) {
			Record r = ConnectionManager.getAccessTo("SLE_CATEGORY_VALUE").getModel();
			r.setFieldValue("id", System.currentTimeMillis());
			r.setFieldValue("category_id", srcr.getFieldValue("node"));
			pane = new CategoryValueRecord(r, true);
		}
		if (aa == editCategoryValue) {
			Record r = ConnectionManager.getAccessTo("SLE_CATEGORY_VALUE").exist("id = " + srcr.getFieldValue("node"));
			pane = new CategoryValueRecord(r, false);
		}
		return pane;
	}

	/**
	 * override to dispach event to correct table
	 */
	@Override
	public boolean executeAction(TActionEvent event) {
		if (event.getRedirectAction() instanceof DefaultCancelAction) {
			return true;
		}
		Object src = event.getSource();
		boolean ok = false;
		AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
		if (src == newCategory) {
			ConnectionManager.getAccessTo("sle_category").add(ardi.getRecord());
			ok = true;
		}
		if (src == editCategory) {
			ConnectionManager.getAccessTo("sle_category").update(ardi.getRecord());
			ok = true;
		}
		if (src == newCategoryValue) {
			ConnectionManager.getAccessTo("SLE_CATEGORY_VALUE").add(ardi.getRecord());
			ok = true;
		}
		if (src == editCategoryValue) {
			ConnectionManager.getAccessTo("SLE_CATEGORY_VALUE").update(ardi.getRecord());
			ok = true;
		}
/*
		if (event.getSource() instanceof DeleteRecord) {
			Object[] options = {TStringUtils.getBundleString("action.delete.confirm"),
					TStringUtils.getBundleString("action.delete.cancel")};
			int o = JOptionPane.showOptionDialog(PlanC.frame, TStringUtils.getBundleString("action.delete.message"),
					TStringUtils.getBundleString("action.delete.title"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (o == JOptionPane.YES_OPTION) {
				Record rcd = getRecord();
				String wc = 
				if (rcd.getTableName().equals("SLE_CATEGORY")) {
					ConnectionManager.getAccessTo("sle_category").update(ardi.getRecord());
				}
				ConnectionManager.getAccessTo("sle_category").update(ardi.getRecord());
				ConnectionManager.getAccessTo("SLE_CATEGORY_VALUE").add(ardi.getRecord());
				ok = ConnectionManager.getAccessTo(rcd.getTableName()).delete(rcd);
			} else {
				ok = false;
			}
		}
*/
		if (!ok) {
			ok = super.executeAction(event); 
		}
		if (ok) {
			freshen();
		}
		return ok;
	}
	/**
	 * override to perform aditional verification: <br>
	 * - if node is a parent node: enable editcategory, newcategoryvalue <br>
	 * - if node is leaf node: enable editcatvalue
	 */
	@Override
	public void enableActions(int sco, boolean ena) {
		super.enableActions(sco, ena);
		Record r = getRecord();
		if (r != null) {
			// easy verification: if subnode ==0, record has leaf (or is able too)
			long sn = (Long) r.getFieldValue("subnode");
			editCategory.setEnabled(sn == 0);
			newCategoryValue.setEnabled(sn == 0);
			editCategoryValue.setEnabled(sn != 0);
		}
	}

	public void init() {
		setServiceRequest(serviceRequest);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		// Object prp = evt.getPropertyName();

	}
}
