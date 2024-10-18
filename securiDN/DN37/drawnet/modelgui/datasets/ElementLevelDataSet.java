package drawnet.modelgui.datasets;

import java.awt.Graphics2D;

import drawnet.DrawNET;
import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Model;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.modelgui.graphicelements.GE_SubModel;
import drawnet.modelgui.panels.DrawNETPanels;
import drawnet.modelgui.panels.elementpanel.DrawNETElementPanel;
import drawnet.utils.GraphicElement;

public class ElementLevelDataSet implements Cloneable {

	private String Name;

	private ElementType EType;
	private GraphicElement GE;
	private DrawNETElementPanel ElementPanel;

	private ElementInstance Inst;
	private ElementLevelDataSet containerELDS;

	public DrawNETElementPanel getElementPanel() {
		return ElementPanel;
	}

	public void setElementPanel(DrawNETElementPanel P) {
		ElementPanel = P;
	}

	public Object clone() throws CloneNotSupportedException {
		ElementLevelDataSet clone = null;
		try {
			clone = new ElementLevelDataSet(new String(Name),
					(GraphicElement) GE.clone(), ElementPanel, containerELDS);
			// DS.GE.setDataSet(DS);
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			throw ex;
		}
		return clone;
	}

	public String getName() {
		return Name;
	}
		
	public ElementLevelDataSet(String N, GraphicElement GEl,
			DrawNETElementPanel Ep, ElementLevelDataSet cELDS) {
		Name = N;
		GE = GEl;
		ElementPanel = Ep;
		containerELDS = cELDS;

		if (GE != null) {
			GE.setDataSet(this);
		}
	}

	// public ElementLevelDataSet(String N, GraphicElement GEl,
	// DrawNETElementPanel Ep, DDL.ElementType Et)
	// {
	// EType = Et;
	// Name = N;
	// GE = GEl;
	// ElementPanel = Ep;
	// desktop = D;
	//		
	// GE.setDataSet(this);
	// }

	public void paint(Graphics2D g) {
		GE.paint(g);
	}

	public boolean contains(double X, double Y) {
		return GE.contains(X, Y);
	}

	public boolean intersects(double X, double Y, double W, double H) {
		return GE.intersects(X, Y, W, H);
	}

	public GraphicElement getGraphicElement() {
		return GE;
	}

	public void updateElementInfoPanel(double x, double y, double w, double h,
			double rot, double px, double py) {
		ElementPanel.updateInfoPanel(x, y, w, h, rot, px, py);
		ElementPanel.setInstanceName(Inst.getId());
	}

	public String getInstanceNameFromPanel() {
		return ElementPanel.getInstanceName();
	}

	public void renameElementInstance() {
		DrawNETPanels desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();
		Model model = desktop.getSelectedFrameModel();
		String NewName = getInstanceNameFromPanel();

		if (containerELDS != null) {
			ElementInstance CE = containerELDS.getElementInstance();
			String CurrSM = CE.getId();

			if (CE.getSubElement(NewName) != null) {
				System.out.println("ERROR: Duplicate instance name");
				ElementPanel.setInstanceName(Inst.getId());
			} else {
				CE.renameSubElement(Inst.getId(), NewName);
				Name = CurrSM + "." + NewName;
				if (GE.getType() == GraphicElement.SUBMODEL) {
					GE_SubModel GEs = (GE_SubModel) GE;
					desktop.getSelectedFrameDataSet().remove(GEs.getSMLDataSetTreeNode());
					GEs.renameSMLDS(getInstanceNameFromPanel());
					desktop.getSelectedFrameDataSet().add(
							((GE_SubModel) containerELDS.GE).getSMLDataSetTreeNode(), GEs.getSMLDataSetTreeNode());
					desktop.getSelectedFrameDataSet().selectNodeIfNull(GEs.getSMLDataSetTreeNode());
					desktop.updateGraphFrameTop();
				}
			}
		} else {
			desktop.getSelectedFrameDataSet().renameRootElement(NewName);
		}
	}
	
	public void updateElementFromPanels() {
		GE.updateFromPanels(ElementPanel);
	}

	// public void updateModelElementFromPanels() {
	// ((GE_ModelElement)GE).updateModelFromPanels(ElementPanel);
	// }

	public ElementType getElementType() {
		return EType;
	}

	public void setElementType(ElementType Et) {
		EType = Et;
	}

	// public void save(String Pfx, ModelFilesCollection Mfc) {
	// Mfc.MRLfile.println(Pfx + GE.toXML());
	// }

	public void setRefLayerElementInstance(ElementInstance Ref, int Order) {
		if (GE != null) {
			// Model M = desktop.getSelectedFrameModel();
			Model M = Ref.getModel();
			ElementInstance nI = M.getSubElementByPath(GE.getPath());
			if(nI != null) { // Daniele
				ElementRefPropertyValue.setElementRefProperty(nI, "G_inLayer", Ref);
				IntegerPropertyValue.setIntegerProperty(nI, "G_order", Order);
			}
		}
	}

	public ElementInstance getElementInstance() {
		return Inst;
	}

	public void setElementInstance(ElementInstance I) {
		Inst = I;
	}

	public void setContainerELDS(ElementLevelDataSet cELDS) {
		containerELDS = cELDS;
	}
	
}
