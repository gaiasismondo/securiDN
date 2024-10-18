package drawnet.modelgui.fsmstates;

import java.awt.event.MouseEvent;

import drawnet.DrawNET;
import drawnet.modelgui.commands.Command;
import drawnet.modelgui.commands.element.CMD_DeleteSelectedElements;
import drawnet.modelgui.commands.element.CMD_DeleteSelectedElementsConnected;
import drawnet.modelgui.datasets.ElementLevelDataSet;
import drawnet.utils.InteractiveDrawingData;

import javax.swing.JOptionPane; // Daniele

public class FSM_Rubber extends FSMState {

	private Command CmdDeleteSelectedElements;
	boolean Mode;

	public FSM_Rubber(boolean M) {
	    super("Rubber" + (M ? "Connected" : ""));
	    
	    Mode = M;	    
	}
	
	public void init() {
		CmdDeleteSelectedElements = Mode ? CMD_DeleteSelectedElementsConnected.getInstance() :
			CMD_DeleteSelectedElements.getInstance();
//			DrawNET.getApplication().getCommandManager().getCommandByID("DeleteSelectedElements" + (Mode ? "Connected" : ""));
	}

	public FSMState mouseReleased(MouseEvent e) {
		InteractiveDrawingData IDD = DrawNET.getApplication().getMainFrame().getDNDesktop().getIDDs();
		IDD.x1 = e.getX();
		IDD.y1 = e.getY();
		DrawNET.getApplication().getMainFrame().getDNDesktop().adjustIDD(1);

		ElementLevelDataSet El = DrawNET.getApplication().getMainFrame().getDNDesktop().FindInAccessibleLayers(IDD.x1, IDD.y1);

//System.out.println("Delete Mode: " + (Mode ? "Connected" : "Normal"));
		if(El != null) {
		    if(!El.getGraphicElement().isSelected())
		    	DrawNET.getApplication().getMainFrame().getDNDesktop().getCurrentSubModelSelection().set(El);
		    // Daniele:
		    int choice = JOptionPane.showConfirmDialog(null, "Element " + El.getName()
					+ " will be deleted.\n" + "Are you sure?",
					"Delete Element?", JOptionPane.YES_NO_OPTION);
		    if (choice == JOptionPane.YES_OPTION)
		    	CmdDeleteSelectedElements.execute();
		}

		return this;
	}

}
