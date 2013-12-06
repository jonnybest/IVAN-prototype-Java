package de.uka.ipd.resi.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Manages all ToolTips for a shell (dialogs etc.).
 * 
 * @author Torben Brumm
 */
public class ToolTipHandler {
	
	/**
	 * The name of the key to the data in a control the tool tip shall display.
	 */
	public static final String TOOLTIP_DATA_KEY = "TOOLTIP";
	
	/**
	 * Widget that currently uses the ToolTip.
	 */
	private Widget currentWidget;
	
	/**
	 * Shell the tip is presented on.
	 */
	private final Shell shell;
	
	/**
	 * Text that is displayed.
	 */
	private final Label text;
	
	/**
	 * Constructor.
	 * 
	 * @param parent Shell the TollTip will hover over.
	 */
	public ToolTipHandler(final Shell parent) {
		final Display display = parent.getDisplay();
		
		this.shell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
		this.shell.setLayout(new GridLayout(1, false));
		this.shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		this.text = new Label(this.shell, SWT.NONE | SWT.WRAP);
		this.text.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		this.text.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		final GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		layoutData.widthHint = 350;
		this.text.setLayoutData(layoutData);
	}
	
	/**
	 * Activates the tool tip for the given control. The control needs to have a "TOOLTIP" data set with the text to
	 * display.
	 * 
	 * @control Control to activate the tool tip for.
	 */
	public void activateToolTip(final Control control) {
		control.addMouseListener(new MouseAdapter() {
			
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDown(final MouseEvent e) {
				if (ToolTipHandler.this.shell.isVisible()) {
					ToolTipHandler.this.shell.setVisible(false);
				}
			}
		});
		control.addMouseTrackListener(new MouseTrackAdapter() {
			
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseExit(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseExit(final MouseEvent e) {
				if (ToolTipHandler.this.shell.isVisible()) {
					ToolTipHandler.this.shell.setVisible(false);
				}
				ToolTipHandler.this.currentWidget = null;
			}
			
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseHover(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseHover(final MouseEvent event) {
				final Point pt = new Point(event.x, event.y);
				
				// only need initializing and repositioning on a new widget
				if (event.widget == ToolTipHandler.this.currentWidget) {
					return;
				}
				
				// initialize tip with data
				ToolTipHandler.this.currentWidget = event.widget;
				final Point tipPosition = control.toDisplay(pt);
				final String text = (String) event.widget.getData(ToolTipHandler.TOOLTIP_DATA_KEY);
				ToolTipHandler.this.text.setText(text != null ? text : "");
				ToolTipHandler.this.shell.pack();
				
				// display at right position
				final Rectangle displayBounds = ToolTipHandler.this.shell.getDisplay().getBounds();
				final Rectangle shellBounds = ToolTipHandler.this.shell.getBounds();
				shellBounds.x = Math.max(Math.min(tipPosition.x, displayBounds.width - shellBounds.width), 0);
				shellBounds.y = Math.max(Math.min(tipPosition.y + 16, displayBounds.height - shellBounds.height), 0);
				ToolTipHandler.this.shell.setBounds(shellBounds);
				ToolTipHandler.this.shell.setVisible(true);
			}
		});
		
	}
	
}
