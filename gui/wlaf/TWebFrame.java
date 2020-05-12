/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package gui.wlaf;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.jdesktop.core.animation.timing.*;
import org.jdesktop.swing.animation.timing.sources.*;

import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.fade.*;
import com.alee.laf.*;
import com.alee.laf.rootpane.*;

import core.*;

public class TWebFrame extends WebFrame {

	private static Animator frameAnimator;
	private ComponentTransition transitionPanel;
	private JComponent waitComponent, contentPanel;

	public TWebFrame() {
		super();
		transitionPanel = new ComponentTransition();
		transitionPanel.setTransitionEffect(new FadeTransitionEffect());
		waitComponent = TUIUtils.getWaitJLabel();
		transitionPanel.setContent(waitComponent);
		setContentPane(transitionPanel);

		setTitle(TStringUtils.getBundleString("about.app.id"));
		Vector v = new Vector();
		v.add(TResourceUtils.getIcon("appicon", 16).getImage());
		v.add(TResourceUtils.getIcon("appicon", 32).getImage());
		setIconImages(v);
		setShowResizeCorner(true);
//		WebLookAndFeel.setDecorateFrames(false);

		SwingTimerTimingSource ts = new SwingTimerTimingSource();
		AnimatorBuilder.setDefaultTimingSource(ts);
		frameAnimator = new AnimatorBuilder().setDuration(250, TimeUnit.MILLISECONDS).build();
		ts.init();
	}

	public void performTransition(Rectangle r1, Rectangle r2) {
		setContentPane(waitComponent);
		TimingTarget tt = PropertySetter.getTarget(this, "Bounds", r1, r2);
		TimingTargetAdapter tta = new TimingTargetAdapter() {
			@Override
			public void end(Animator source) {
				setContentPane(transitionPanel);
				transitionPanel.performTransition(contentPanel);
			}
		};
		frameAnimator.addTarget(tt);
		frameAnimator.addTarget(tta);
		frameAnimator.start();
	}

	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on factor f
	 * 
	 * @param f - size factor
	 * @return bounds
	 */
	public Rectangle getSizeBy(double f) {
		return getSizeBy(f, f);
	}

	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on factors wf and hf
	 * 
	 * @param wf - width factor
	 * @param hf - height factor
	 * @return center rectangle
	 */
	public Rectangle getSizeBy(double wf, double hf) {
		Rectangle gcr = getGraphicsConfiguration().getBounds();
		Rectangle rr = new Rectangle(0, 0, (int) (gcr.width * wf), (int) (gcr.height * hf));
		center(gcr, rr);
		return rr;
	}

	/**
	 * center the r2 rectangle based on r1 coordenates
	 * 
	 * @param r1 - base rectangle
	 * @param r2 - rectangle to center
	 */
	private void center(Rectangle r1, Rectangle r2) {
		r2.x = (r1.width - r2.width) / 2;
		r2.y = (r1.height - r2.height) / 2;
	}

	public void performTransition1(Rectangle r1, Rectangle r2) {
		TimingTarget tt = PropertySetter.getTarget(this, "Bounds", r1, r2);
		frameAnimator.clearTargets();

		TimingTargetAdapter tta = new TimingTargetAdapter() {
			@Override
			public void end(Animator source) {
				PlanC.executeFinal();
			}
		};
		frameAnimator.addTarget(tta);
		frameAnimator.addTarget(tt);
		frameAnimator.start();
	}

	public void performTransition(Rectangle r1) {
		Rectangle r2 = zoomIn(r1, getPreferredSize());
		performTransition(r1, r2);
	}

	public void setContent(JComponent c) {
		this.contentPanel = c;
		waitComponent.setPreferredSize(contentPanel.getPreferredSize());
		transitionPanel.setContent(contentPanel);
	}

	/**
	 * Calc new rectangle resulting that is centered based on src rectangle
	 * 
	 * @param src - orginial rectangle
	 * @param dim - new size of orginal rectangle
	 * 
	 * @return center rectangle
	 */
	public static Rectangle zoomIn(Rectangle src, Dimension dim) {
		Rectangle dest = new Rectangle(src);
		dest.setSize(dim);
		int hx = (dim.width - src.width) / 2;
		int hy = (dim.height - src.height) / 2;
		dest.x -= hx;
		dest.y -= hy;
		return dest;
	}

	/**
	 * @return the transitionPanel
	 */
	public ComponentTransition getTransitionPanel() {
		return transitionPanel;
	}

}
