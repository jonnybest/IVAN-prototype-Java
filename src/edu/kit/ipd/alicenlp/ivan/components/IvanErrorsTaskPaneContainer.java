/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.components;

import org.jdesktop.swingx.JXTaskPaneContainer;

/** This is a special JXTaskPaneContainer, which can display errors and warnings that occur in IVAN.
 * It provides an cues to the UI where to render errors (line numbers or character offsets),
 * actions for fixing and ignoring errors, and a serializeable state for easy instrumentation.
 * 
 * JXTaskPaneContainer provides an elegant view to display a list of tasks ordered by groups (org.jdesktop.swingx.JXTaskPanes). 
 * @author Jonny
 *
 */
public class IvanErrorsTaskPaneContainer extends JXTaskPaneContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public IvanErrorsTaskPaneContainer() {
		// TODO Auto-generated constructor stub
	}

}
