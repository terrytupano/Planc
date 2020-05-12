package plugin.planc.dashboard;

import java.awt.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

import plugin.planc.*;
import core.datasource.*;


public class AreaChartDemo1 extends JPanel {
	
	private String chartType;
	
	public static String AREA_CHART = "AreaChart";
	public static String RING_CHART = "RingChart";
	public static String C1_CHART = "C1Chart";
	public static String C2_CHART = "C2Chart";

	public AreaChartDemo1(String ct) {
		super(new BorderLayout());
		setBackground(Color.WHITE);
		setBorder(new LineBorder(Color.GRAY));
		setValues(null);
		this.chartType = ct;
	}

	public void setValues(Record[] rcdlst) {
		setVisible(false);
		removeAll();
		//rcdlst = (rcdlst.length == 0) ? null : rcdlst;
		if (rcdlst != null ) {
			JLabel gt = new JLabel("", JLabel.CENTER);
			gt.setFont(gt.getFont().deriveFont(15f).deriveFont(Font.BOLD));

			JComponent chart = null;
			if (chartType == AREA_CHART) {
				gt.setText("Area comparation");
				gt.setForeground(Color.GREEN);
				JFreeChart localJFreeChart = createAreaChart(rcdlst);
				chart = new ChartPanel(localJFreeChart);
			}
			if (chartType == RING_CHART) {
				gt.setText("Total impact");
				gt.setForeground(Color.ORANGE);
				JFreeChart localJFreeChart = createRingChart(rcdlst);
				chart = new ChartPanel(localJFreeChart);
			}

			add(gt, BorderLayout.NORTH);
			add(chart, BorderLayout.CENTER);
			
		} else {
			JLabel jl = new JLabel("Carlos mejias !! poner algo aqui !!! ", JLabel.CENTER);
			add(jl, BorderLayout.CENTER);
		}
		setVisible(true);
	}
	
	private JFreeChart createRingChart(Record[] rcdlst) {
		  DefaultPieDataset dpds = new DefaultPieDataset();
		  // total
		  double tot = 0.0;
			for (Record rcd : rcdlst) {
				tot += (Double) rcd.getFieldValue(13);
			}
			for (Record rcd : rcdlst) {
				String na = (String) rcd.getFieldValue(0);
					Double v = (Double) rcd.getFieldValue(13);
					dpds.setValue(na, tot / v);
			}

		  RingPlot localRingPlot = new RingPlot(dpds);
		  localRingPlot.setCenterTextMode(CenterTextMode.VALUE);
		  localRingPlot.setCenterTextFont(new Font("SansSerif", 1, 24));
		  localRingPlot.setCenterTextColor(Color.GRAY);
		  localRingPlot.setBackgroundPaint(Color.WHITE);

		  localRingPlot.setCenterTextFormatter(new DecimalFormat("0.0%"));
		  
		  JFreeChart localJFreeChart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, localRingPlot, false);
		  //		  localJFreeChart.setBackgroundPaint(new GradientPaint(new Point(0, 0), new Color(20, 20, 20), new Point(400, 200), Color.DARK_GRAY));
		  //		  TextTitle localTextTitle = localJFreeChart.getTitle();
		  //		  localTextTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
		  //localTextTitle.setPaint(new Color(240, 240, 240));
		  //localTextTitle.setFont(new Font("Arial", 1, 26));
		  //localRingPlot.setBackgroundPaint(null);
		  localRingPlot.setOutlineVisible(false);
		  //localRingPlot.setLabelGenerator(null);
//		  localRingPlot.setSectionPaint("A", Color.ORANGE);
//		  localRingPlot.setSectionPaint("B", new Color(100, 100, 100));
//		  localRingPlot.setSectionPaint("D", Color.RED);
		  localRingPlot.setSectionDepth(0.05D);
		  localRingPlot.setSectionOutlinesVisible(false);
		  localRingPlot.setShadowPaint(null);
		  return localJFreeChart;

	}

	private JFreeChart createAreaChart(Record[] rcdlst) {
		// dataset
		DefaultCategoryDataset dcds = new DefaultCategoryDataset();
		for (Record rcd : rcdlst) {
			String na = (String) rcd.getFieldValue(0);
			for (int c = 1; c < 13; c++) {
				String ms = SLEPlanC.getSlotString(rcd.getFieldName(c));
				Double v = (Double) rcd.getFieldValue(c);
				dcds.addValue(v, na, ms);
			}
		}

		// chart
		JFreeChart localJFreeChart = ChartFactory.createAreaChart(null, null, null, dcds);
		CategoryPlot localCategoryPlot = (CategoryPlot) localJFreeChart.getPlot();
		localCategoryPlot.setForegroundAlpha(0.5F);
		localCategoryPlot.setBackgroundPaint(Color.WHITE);
//		localJFreeChart.setBackgroundPaint(Color.WHITE);
		localCategoryPlot.setOutlineVisible(false);

		/*
		 * TextTitle localTextTitle = new TextTitle(
		 * "An area chart demonstration.  We use this subtitle as an example of what happens when you get a really long title or subtitle."
		 * ); localTextTitle.setPosition(RectangleEdge.TOP); localTextTitle.setPadding(new
		 * RectangleInsets(UnitType.RELATIVE, 0.05D, 0.05D, 0.05D, 0.05D));
		 * localTextTitle.setVerticalAlignment(VerticalAlignment.BOTTOM); localJFreeChart.addSubtitle(localTextTitle);
		 * CategoryPlot localCategoryPlot = (CategoryPlot) localJFreeChart.getPlot();
		 * localCategoryPlot.setForegroundAlpha(0.5F); localCategoryPlot.setDomainGridlinesVisible(true); AreaRenderer
		 * localAreaRenderer = (AreaRenderer) localCategoryPlot.getRenderer();
		 * localAreaRenderer.setEndType(AreaRendererEndType.LEVEL); CategoryAxis localCategoryAxis =
		 * localCategoryPlot.getDomainAxis(); localCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		 * localCategoryAxis.setLowerMargin(0.0D); localCategoryAxis.setUpperMargin(0.0D);
		 * localCategoryAxis.addCategoryLabelToolTip("Type 1", "The first type.");
		 * localCategoryAxis.addCategoryLabelToolTip("Type 2", "The second type.");
		 * localCategoryAxis.addCategoryLabelToolTip("Type 3", "The third type."); NumberAxis localNumberAxis =
		 * (NumberAxis) localCategoryPlot.getRangeAxis();
		 * localNumberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		 * localNumberAxis.setLabelAngle(0.0D);
		 */
		// ChartUtilities.applyCurrentTheme(localJFreeChart);
		return localJFreeChart;
	}

	/**
	 * 
	 * @param rcdlst
	private void createBubbleChart(Record[] rcdlst) {
		DefaultXYZDataset dataset = new DefaultXYZDataset();
	    double[] arrayOfDouble1 = { 2.1D, 2.3D, 2.3D, 2.2D, 2.2D, 1.8D, 1.8D, 1.9D, 2.3D, 3.8D }; // x name
	    double[] arrayOfDouble2 = { 14.1D, 11.1D, 10.0D, 8.8D, 8.7D, 8.4D, 5.4D, 4.1D, 4.1D, 25.0D }; // y total
	    double[] arrayOfDouble3 = { 2.4D, 2.7D, 2.7D, 2.2D, 2.2D, 2.2D, 2.1D, 2.2D, 1.6D, 4.0D }; // z 
	    double[][] arrayOfDouble = { arrayOfDouble1, arrayOfDouble2, arrayOfDouble3 };
	    dataset.addSeries("Series 1", arrayOfDouble);
		    
	    JFreeChart localJFreeChart = ChartFactory.createBubbleChart(null, null, null, dataset, PlotOrientation.HORIZONTAL, true, true, false);
	    XYPlot localXYPlot = (XYPlot)localJFreeChart.getPlot();
	    localXYPlot.setForegroundAlpha(0.65F);
	    localXYPlot.setDomainPannable(true);
	    localXYPlot.setRangePannable(true);
	    XYItemRenderer localXYItemRenderer = localXYPlot.getRenderer();
	    localXYItemRenderer.setSeriesPaint(0, Color.blue);
	    NumberAxis localNumberAxis1 = (NumberAxis)localXYPlot.getDomainAxis();
	    localNumberAxis1.setLowerMargin(0.15D);
	    localNumberAxis1.setUpperMargin(0.15D);
	    NumberAxis localNumberAxis2 = (NumberAxis)localXYPlot.getRangeAxis();
	    localNumberAxis2.setLowerMargin(0.15D);
	    localNumberAxis2.setUpperMargin(0.15D);
	    return localJFreeChart;	}
	 */
}