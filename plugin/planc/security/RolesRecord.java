package plugin.planc.security;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar sle_roles
 * 
 */
public class RolesRecord extends AbstractRecordDataInput {

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es para crear un nuevo registro
	 * @param uid - id de tipo de usuario a crear (constant.prperties)
	 */
	public RolesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("id", TUIUtils.getJTextField(rcd, "id"), true, newr);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 150dlu", // columns
				"p, 3dlu, p, p"); // rows
		// lay.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("id"), cc.xy(1, 1));
		build.add(getInputComponent("id"), cc.xyw(3, 1, 2));
		build.add(getLabelFor("name"), cc.xy(1, 3));
		build.add(getInputComponent("name"), cc.xyw(1, 4, 4));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
