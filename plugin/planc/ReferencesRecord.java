package plugin.planc;

import gui.*;

import java.awt.event.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * panel de nuevo/edicion para archivo de referencias
 * 
 */
public class ReferencesRecord extends AbstractRecordDataInput implements ActionListener {
	
	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es nuevo registro
	 */
	public ReferencesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		addInputComponent("domain_name", TUIUtils.getJTextField(rcd, "domain_name"), true, newr);
		addInputComponent("item_value", TUIUtils.getJTextField(rcd, "item_value"), true, newr);
		addInputComponent("group_value", TUIUtils.getJTextField(rcd, "group_value"), false, true);
		addInputComponent("meaning", TUIUtils.getJTextArea(rcd, "meaning"), true, true);
		addInputComponent("mean_label", TUIUtils.getJTextArea(rcd, "mean_label"), false, true);
		addInputComponent("order_val", TUIUtils.getJFormattedTextField(rcd, "order_val"), false, true);
		
		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
				"p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("domain_name"), cc.xy(1, 1));
		build.add(getInputComponent("domain_name"), cc.xyw(3, 1, 5));
		build.add(getLabelFor("item_value"), cc.xy(1, 3));
		build.add(getInputComponent("item_value"), cc.xyw(1, 4, 7));
		build.add(getLabelFor("group_value"), cc.xy(1, 6));
		build.add(getInputComponent("group_value"), cc.xyw(1, 7, 7));
		build.add(getLabelFor("meaning"), cc.xy(1, 9));
		build.add(getInputComponent("meaning"), cc.xyw(1, 10, 7));
		build.add(getLabelFor("mean_label"), cc.xy(1, 12));
		build.add(getInputComponent("mean_label"), cc.xyw(1, 13, 7));
		build.add(getLabelFor("order_val"), cc.xy(1, 15));
		build.add(getInputComponent("order_val"), cc.xy(3, 15));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}