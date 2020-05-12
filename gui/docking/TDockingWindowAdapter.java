package gui.docking;

import javax.swing.*;

import net.infonode.docking.*;
import core.*;

/**
 * procesador de eventos para vistas dentro de perspectiva
 * 
 */
public class TDockingWindowAdapter extends DockingWindowAdapter {

	@Override
	public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
		DockingContainer.updateViews(addedWindow, true);
	}

	@Override
	public void windowClosing(DockingWindow window) throws OperationAbortedException {
		String[] ms = TStringUtils.getBundleString("docking.close.tab").split(";");
		Object[] options = {TStringUtils.getBundleString("action.delete.confirm"),
				TStringUtils.getBundleString("action.delete.cancel")};
		int o = JOptionPane.showOptionDialog(PlanC.frame, ms[0] + window + ms[1],
				TStringUtils.getBundleString("docking.close.title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (o != JOptionPane.YES_OPTION) {
			throw new OperationAbortedException("Window close was aborted!");
		}
	}

	@Override
	public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
		DockingContainer.updateViews(removedWindow, false);
	}
}