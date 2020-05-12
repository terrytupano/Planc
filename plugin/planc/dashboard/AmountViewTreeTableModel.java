package plugin.planc.dashboard;

import gui.jtreetable.*;
import gui.tree.*;

import java.util.*;

import javax.swing.tree.*;

import core.*;
import core.datasource.*;

public class AmountViewTreeTableModel extends AbstractTreeTableModel {

	private TDefaultTreeModel underModel;

	public AmountViewTreeTableModel(TDefaultTreeModel um) {
		super(um.getRoot());
		this.underModel = um;
	}

	@Override
	public Object getChild(Object parent, int index) {
		return underModel.getChild(parent, index);
	}
	@Override
	public int getChildCount(Object parent) {
		return underModel.getChildCount(parent);
	}

	@Override
	public Class getColumnClass(int column) {
		if (column == 0) {
			return TreeTableModel.class;
		}
		return underModel.getModel().getFieldValue(column).getClass();
	}
	@Override
	public int getColumnCount() {
		// name, all month_slot and total only
		return underModel.getModel().getIndexOf("av_total") + 1;
	}

	@Override
	public String getColumnName(int column) {
		Record r = underModel.getModel();
		Hashtable<String, String> ht = (Hashtable<String, String>) underModel.getServiceResponse().getParameter(
				ServiceResponse.RECORD_FIELDS_DESPRIPTION);
		return ht.get(r.getFieldName(column));
	}

	@Override
	public Object getValueAt(Object node, int column) {
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
		Record r = (Record) ((TEntry) dmtn.getUserObject()).getKey();
		return r.getFieldValue(column);
	}
}
