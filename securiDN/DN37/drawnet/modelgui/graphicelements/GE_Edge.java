package drawnet.modelgui.graphicelements;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.util.Enumeration;
import java.util.Vector;

import drawnet.DrawNET;
import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Model;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.modelgui.datasets.ElementLevelDataSet;
import drawnet.modelgui.fsmstates.FSMState;
import drawnet.modelgui.panels.DrawNETPanels;
import drawnet.modelgui.panels.explorerpanel.DrawNETLayersPanel;
import drawnet.utils.GraphicElement;

public class GE_Edge extends GE_Node {

	private static FSMState EdgeDrawFirstTangent;
	private Vector<GE_EdgeSpline> EdgeSplineComps;
	private GE_EdgeSpline EdgeSplineFirstComp;

	private Vector<CubicCurve2D.Double> Spln_P1;
	private Vector<CubicCurve2D.Double> Spln_P2;

	public GE_Edge() {
		super();
		geType = EDGE;
		if (EdgeDrawFirstTangent == null) {
			EdgeDrawFirstTangent = DrawNET.getApplication().getFSMManager()
					.getState("EdgeDrawFirstTangent");
		}
		isManipulable = true;
	}

	public GE_Edge(String Ep) {
		super(Ep);
		geType = EDGE;
		EdgeSplineComps = new Vector<GE_EdgeSpline>();
		EdgeSplineFirstComp = null;
		isManipulable = true;
	}

	public GraphicElement createFromElementInstance(ElementInstance EI, String Elp) {
		GE_Edge out = new GE_Edge(Elp);
		out = (GE_Edge) createFromElementInstance(EI, Elp, out);

		out.Spln_P1 = new Vector<CubicCurve2D.Double>();
		out.Spln_P2 = new Vector<CubicCurve2D.Double>();

		GE_Spline.getSegments(EI, out.Spln_P1, "G_segmentsP1");
		GE_Spline.getSegments(EI, out.Spln_P2, "G_segmentsP2");

		out.readG_TransformFromInstance(EI);

		for (Enumeration<GE_Node.layedEl> LEn = out.drawingComps.elements(); LEn
				.hasMoreElements();) {
			GraphicElement GrEl = (LEn.nextElement()).Ge;

			if (GrEl instanceof GE_EdgeSpline) {
				GE_EdgeSpline GEes = (GE_EdgeSpline) GrEl;
				GEes.setXYWHR(out.x, out.y, out.w, out.h, out.rot);
				GEes.setPivotX(out.pivotX);
				GEes.setPivotY(out.pivotY);
				out.EdgeSplineComps.add(GEes);
				if (out.EdgeSplineFirstComp == null) {
					out.EdgeSplineFirstComp = GEes;
				}
				GEes.setParts(out.Spln_P1, out.Spln_P2);
			}
		}
		return out;
	}

	// public FSMState getNextState() {
	// return EdgeDrawFirstTangent;
	// }

	public boolean contains(double X, double Y) {
		if (super.contains(X, Y)) {
			return true;
		}

		// for(Enumeration<GE_EdgeSpline> LEn = EdgeSplineComps.elements();
		// LEn.hasMoreElements();) {
		// GE_EdgeSpline GeEs = LEn.nextElement();
		// if(GeEs.contains(X, Y)) return true;
		// }

		return EdgeSplineFirstComp.contains(X, Y);
	}

	public boolean intersects(double X, double Y, double W, double H) {
		if (super.intersects(X, Y, W, H)) {
			return true;
		}

		// for(Enumeration<GE_EdgeSpline> LEn = EdgeSplineComps.elements();
		// LEn.hasMoreElements();) {
		// GE_EdgeSpline GeEs = LEn.nextElement();
		// if(GeEs.intersects(X, Y, W, H)) return true;
		// }
		return EdgeSplineFirstComp.intersects(X, Y, W, H);
	}

	public void showManipulators(int Level) {
		EdgeSplineFirstComp.showManipulators(Level);
		super.showManipulators(Level);
	}

	public void hideManipulators() {
		EdgeSplineFirstComp.hideManipulators();
		super.hideManipulators();
	}

	public int findManipulator(double x, double y, boolean Shift, int Level) {
		int fMp = super.findManipulator(x, y, Shift, Level);
		
		if(fMp == -1) {
			return EdgeSplineFirstComp.findManipulator(x, y, Shift, Level);
		} else {
			return fMp + EdgeSplineFirstComp.TotManip();
		}
	}

	public Shape getManipulatedShape(int mn, double x, double y, boolean Shift) {
		if(mn < EdgeSplineFirstComp.TotManip()) {
			return EdgeSplineFirstComp.getManipulatedShape(mn, x, y, Shift);
		} else 
			return null;
	}

	public void manipulate(int mn, double x, double y, boolean Shift) {
		if(mn < EdgeSplineFirstComp.TotManip()) {
			for (GE_EdgeSpline GeEs : EdgeSplineComps) {
				GeEs.Manipulate(mn, x, y, Shift);
			}
			this.finishGESplineProcedure();
		} else {
			super.manipulate(mn - EdgeSplineFirstComp.TotManip(), x, y, Shift);
		}
		// setPivotX(EdgeSplineFirstComp.getPivotX());
		// setPivotY(EdgeSplineFirstComp.getPivotY());
	}

	public void addIntermediatePoint(double x, double y) {
		for (GE_EdgeSpline GeEs : EdgeSplineComps) {
			GeEs.AddIntermediatePoint(x, y);
		}
		this.finishGESplineProcedure();
	}

	public void deleteManipulator(int mn) {
		for (GE_EdgeSpline GeEs : EdgeSplineComps) {
			GeEs.DeleteManipulator(mn);
		}
		this.finishGESplineProcedure();
	}

	public void BreakArc(double x, double y) {
		for (GE_EdgeSpline GeEs : EdgeSplineComps) {
			GeEs.BreakArc(x, y);
		}
		this.finishGESplineProcedure();
	}

	public void JoinArc(double x, double y) {
		for (GE_EdgeSpline GeEs : EdgeSplineComps) {
			GeEs.JoinArc(x, y);
		}
		this.finishGESplineProcedure();
	}

	public void AdjustFromTo(int FT, double dx, double dy) {
		for (GE_EdgeSpline GeEs : EdgeSplineComps) {
			GeEs.AdjustFromTo(FT, dx, dy);
		}
		this.finishGESplineProcedure();
	}
	
	private void finishGESplineProcedure() {
		Spln_P1 = EdgeSplineFirstComp.getP1SplineVec();
		Spln_P2 = EdgeSplineFirstComp.getP2SplineVec();

		if (dataSet != null) {
			ElementInstance nI = dataSet.getElementInstance();
			GE_Spline.setSegments(nI, Spln_P1, "G_segmentsP1");
			GE_Spline.setSegments(nI, Spln_P2, "G_segmentsP2");
		}
	}

	public boolean beforeDelete(int deleteTool) {
		DrawNETPanels desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();
		DrawNETLayersPanel LayersPanel = desktop.getSelectedSubModelDataSet().getLayersPanel();

		ElementInstance nI = dataSet.getElementInstance();
		ElementInstance eFrom = ElementRefPropertyValue.getElementRefProperty(nI, "from");
		ElementInstance eTo = ElementRefPropertyValue.getElementRefProperty(nI, "to");
		if (eFrom != null) {
			GE_Node nFrom = (GE_Node) desktop.getGraphicElementByInstanceName(
					eFrom.getElementInstanceAbsolutePath());
			if (nFrom != null) // Daniele
			if (nFrom.DisconnectedFromArc(nI)) {
				LayersPanel.removeElement(nFrom.getDataSet());
			}
		}
		if (eTo != null) {
			GE_Node nTo = (GE_Node) desktop.getGraphicElementByInstanceName(
							eTo.getElementInstanceAbsolutePath());
			if (nTo != null) // Daniele
			if (nTo.DisconnectedToArc(nI)) {
				LayersPanel.removeElement(nTo.getDataSet());
			}
		}
		return true;
	}

	private ElementInstance DetachArcEndCorrectProp(String Prop) {
		// DrawNETLayersPanel LayersPanel =
		// desktop.getSelectedSubModelDataSet().getLayersPanel();

		ElementInstance nI = dataSet.getElementInstance();
		ElementInstance eConn = ElementRefPropertyValue.getElementRefProperty(
				nI, Prop);
		if (eConn != null) {
			GE_Node nConn = (GE_Node) DrawNET.getApplication().getMainFrame()
					.getDNDesktop().getGraphicElementByInstanceName(
							eConn.getElementInstanceAbsolutePath());
			return ((Prop.equals("from") ? nConn.DisconnectedFromArc(nI)
					: nConn.DisconnectedToArc(nI)) ? eConn : null);
		}
		return null;
	}

	public void DetachArcEnd(int Manip, double nx, double ny) {
		ElementInstance Alone;
		ElementInstance nullI = null;
		GE_null nullGE = null;

		String Prop = (Manip == 0) ? "from" : "to";
		Alone = DetachArcEndCorrectProp(Prop);
		
		DrawNETPanels desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();

		if (Alone != null) {
			// Move Existing null node

			nullGE = (GE_null) desktop.getGraphicElementByInstanceName(
							Alone.getElementInstanceAbsolutePath());
			nullGE.setX(nx);
			nullGE.setY(ny);
			nullGE.updateShape();
		} else {
			// Add a new null node
			Model M = desktop.getSelectedFrameModel();
			String CurrSM = desktop.getCurrentSubModelId();
			ElementType GE_null_ET = M.getFormalism().getSubElement("GE_null",
					true);

			nCNT = GraphicElement.getNextCNT(nCNT, M, CurrSM + ".null");
			String ElName = "null" + nCNT;
			String _ElPath = CurrSM + "." + ElName;
			M.addSubElement(ElName, GE_null_ET, CurrSM, true);
			nullI = M.getSubElementByPath(_ElPath);

			GraphicElement.setG_TransformProperty(nullI, nx, ny, 100, 100, 0,
					0, 0.0);
			GE_null nullGEmaker = new GE_null();
			nullGE = (GE_null) nullGEmaker.createFromElementInstance(nullI,
					_ElPath);
			ElementLevelDataSet nullELDS = desktop.makeElementLevelDataSet(
							_ElPath, nullGE, nullI);
			desktop.addToCurrentLayer(nullELDS);

			ElementRefPropertyValue.setElementRefProperty(dataSet
					.getElementInstance(), Prop, nullI);
		}
		if (Manip == 0)
			nullGE.ConnectedFromArc(dataSet.getElementInstance());
		else
			nullGE.ConnectedToArc(dataSet.getElementInstance());
	}

	public GE_null AttachArcEnd(GE_Node nNod, int Manip, double nx, double ny) {
		String Prop = (Manip == 0) ? "from" : "to";
		ElementInstance Alone = DetachArcEndCorrectProp(Prop);

		// if(desktop == null) desktop =
		// DrawNET.getApplication().getDNFrame().getDNDesktop();
		ElementInstance eiNod = nNod.getDataSet().getElementInstance();

		ElementRefPropertyValue.setElementRefProperty(dataSet
				.getElementInstance(), Prop, eiNod);
		if (Manip == 0)
			nNod.ConnectedFromArc(dataSet.getElementInstance());
		else
			nNod.ConnectedToArc(dataSet.getElementInstance());

		return ((Alone != null) ? (GE_null) DrawNET.getApplication()
				.getMainFrame().getDNDesktop().getGraphicElementByInstanceName(
						Alone.getElementInstanceAbsolutePath()) : null);
	}
}
