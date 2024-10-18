
package drawnet.modelgui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import drawnet.DrawNET;
import drawnet.lib.solvers.Solver;
import drawnet.lib.solvers.SolverEvent;
import drawnet.lib.solvers.SolverListener;
import drawnet.modelgui.panels.DrawNETPanels;


public class DrawNETSolveWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	static DrawNETSolveWindow DNc = null;
	
	@Deprecated
	DrawNETPanels desktop;
	final JEditorPane Texts;
	
	DrawNETSolveWindow(JFrame Frm) {
	    super(Frm,"Solving...",false);
	    desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();
	    
	    Container pane = getContentPane();
	    pane.setLayout(new BorderLayout());

	    JButton ok = new JButton("OK");
	    pane.add(ok, BorderLayout.SOUTH);
	    
	    Texts = new JEditorPane();
	    Texts.setEditable(false);
	    Texts.setContentType("text/text");

	    JScrollPane textScrollPane = new JScrollPane(Texts);
        
	    	    	    
	    pane.add(textScrollPane, BorderLayout.CENTER);
	    
	    ok.addActionListener(new ActionListener()
		{ public void actionPerformed(ActionEvent evt)
			{ setVisible(false); }
		});

	    setSize(500, 360);
	}
	
	public static void Show() {
	    if(DNc == null) {
	    	DNc = new DrawNETSolveWindow(null);
	    }
	    Solver S = DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedFrameDataSet().getCurrentFrameTop().getSelectedSolver();
	    DNc.Texts.setText("Looking for solvers for formalism: " + DrawNET.getApplication().getMainFrame().getDNDesktop().getCurrentFormalismName() +
	    		      "\nUsing: " +  S.getDescription() + "\n\n");
	    //DNc.setVisible(true); // Daniele

	    S.setListener(new SolverListener() {
	      // event handler for this solver
	      public void notifyEvent(SolverEvent e) {
		if (e.getEventId() == SolverEvent.SE_SOLVER_STDOUT) {
		    DNc.Texts.setText(DNc.Texts.getText() + e.getEventText() + "\n");
		}
		else if (e.getEventId() == SolverEvent.SE_SOLVER_STDERR) {
		    DNc.Texts.setText(DNc.Texts.getText() + "!! " + e.getEventText() + " !!\n");
		}
		else if (e.getEventId() == SolverEvent.SE_SOLVER_ERROR) {
		    DNc.Texts.setText(DNc.Texts.getText() + "\n Error: " + e.getEventText() + "\n");
		}
	      }
	    });	    
	    S.solve(DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedFrameDataSet().getModel(), true);
	}
}
