package plugin.planc.config;

import gui.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import plugin.planc.*;

import action.*;

/**
 * SLE_CURRENCY_EXCHANGE list
 * 
 */
public class CurrencyExchange extends UIListPanel {

	private ServiceRequest request;

	public CurrencyExchange() {
		super(null);
		
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CURRENCY_EXCHANGE", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "source_id;target_id;since;rate");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;CurrencyExchange");
		putClientProperty(SLEPlanC.PLANC_ID, 70330L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setServiceRequest(request);
		TEntry[] telist = SLEPlanC.getTEntryGroupFrom("SLE_CURRENCY", "id", "name", null);
		setReferenceColumn("source_id", telist);
		setReferenceColumn("target_id", telist);
		
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new CurrencyExchangeRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new CurrencyExchangeRecord(getRecord(), false);
		}
		return pane;
	}
}
