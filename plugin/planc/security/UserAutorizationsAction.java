package plugin.planc.security;

import java.awt.event.*;

import plugin.planc.*;
import action.*;
import core.*;

/**
 * presenta un icono asociado a un nombre de archivo pasado como argumento
 * 
 */
public class UserAutorizationsAction extends TAbstractAction {

	private UserAutorizationsTree sleAutorizations;

	/** nueva instancia
	 * 
	 * @param nam - id de nomre de accion 
	 * @param icoval - nombre archivo db para mostrar icono
	 */
	public UserAutorizationsAction(String nam, String icoval, UserAutorizationsTree tg) {
		super(nam, icoval, TAbstractAction.TABLE_SCOPE, "tt" + nam);
		this.sleAutorizations = tg;
		putValue(SLEPlanC.PLANC_ID, 50030L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		sleAutorizations.putClientProperty(TConstants.ACTION_PERFORMED, null);
		sleAutorizations.putClientProperty(TConstants.ACTION_PERFORMED, this);
	}
}
