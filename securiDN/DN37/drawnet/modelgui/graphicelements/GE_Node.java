package drawnet.modelgui.graphicelements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Vector;
import java.util.HashSet;

import drawnet.DrawNET;
import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Model;
import drawnet.lib.ddl.propertytypes.FixedArrayPropertyType;
import drawnet.lib.ddl.propertytypes.StructPropertyType;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;
import drawnet.lib.ddl.propertyvalues.StructPropertyValue;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.FixedArrayPropertyValue;
import drawnet.lib.simplednql.querydefs.DrawNETQuerySelectAndSort;
import drawnet.modelgui.datasets.ElementLevelDataSet;
import drawnet.modelgui.graphicelements.drawingelements.DE_Cap;
import drawnet.modelgui.graphicelements.drawingelements.DE_Fill;
import drawnet.modelgui.graphicelements.drawingelements.DE_Stroke;
import drawnet.modelgui.panels.elementpanel.DrawNETModelElementInstancePanel;
import drawnet.modelgui.panels.explorerpanel.DrawNETLayersPanel;
import drawnet.utils.GraphicElement;
import drawnet.utils.ManipulableGraphicElement;


//import GraphicElements.PropConditions.*;
/*
class GEandPropCondition {
    PropCondition Cond;
    GraphicElement GE;
    
    public GEandPropCondition(GraphicElement G, PropCondition C) {
    	GE = G;
    	Cond = C;
    }

    public GraphicElement getGraphicElement() {
    	return GE;
    }
    
    public PropCondition getPropCondition() {
    	return Cond;
    }
    
    public boolean Match() {
    	return Cond.Match();
    }
}
*/

public class GE_Node extends GraphicElement implements ManipulableGraphicElement {	
	private Rectangle2D.Double BBoxInit;
	private Shape BBox;
	private Paint Inside;

	protected String ElTypePath;

	protected static int nCNT;	// Null elements counter

	class layedEl {
		GraphicElement Ge;
		String Layer;
		int ManipSet;
		
		layedEl(GraphicElement e, String l, int ms) {
			Ge = e; Layer = l; ManipSet = ms;
		}
	}
	
	class manipInitPos {
		double x;
		double y;
		double sx;
		double sy;
		
		manipInitPos(double x, double y) {
			this.x = x;
			this.y = y;
			this.sx = x;
			this.sy = y;
		}
		manipInitPos(double x, double y, double sx, double sy) {
			this.x = x;
			this.y = y;
			this.sx = sx;
			this.sy = sy;
		}
	}
	
	manipInitPos[] mip;
	
	boolean ManipShown;

	protected Vector<layedEl> drawingComps;
	private Vector<InitPos> DrawingCompsInitPos;

	class InitPos {
	    double x, y, w, h, rot;
	    InitPos(double nx, double ny, double nw, double nh, double nrot) {
	    	x = nx; y = ny; w = nw; h = nh; rot = nrot;
	    }
	}

	public String getElementTypeID() {
	    return ElTypePath;
	}

	public void updateShape() {
		checkMip();
		
		if (BBoxInit != null) {
			BBox = (Shape) BBoxInit.clone();
		} else {
			BBox = new Rectangle2D.Double(x - 2.0, y - 2.0, 5.0, 5.0);
		}

		AffineTransform Trans = AffineTransform.getTranslateInstance(x, y);
		Trans.concatenate(AffineTransform.getRotateInstance(-rot * Math.PI / 180.0));
		Trans.concatenate(AffineTransform.getScaleInstance(w / 100.0, h / 100.0));
		Trans.concatenate(AffineTransform.getTranslateInstance(-pivotX, -pivotY));
		BBox = Trans.createTransformedShape(BBox);

		Enumeration<InitPos> LEnIp = DrawingCompsInitPos.elements();
		for (layedEl lGrEl : drawingComps ) {
			GraphicElement GrEl = lGrEl.Ge;
			InitPos IP = LEnIp.nextElement();
			Point2D P2d = new Point2D.Double(IP.x, IP.y);
			Point2D Rtp = Trans.transform(P2d, null);
			
			double dx = 0.0, dy = 0.0;
			if(mip != null) {
			  if(mip[lGrEl.ManipSet] != null) {
//System.out.println(mip + " " + lGrEl + " " + lGrEl.ManipSet + " " + mip[lGrEl.ManipSet]);
				dx = mip[lGrEl.ManipSet].x - mip[lGrEl.ManipSet].sx;
				dy = mip[lGrEl.ManipSet].y - mip[lGrEl.ManipSet].sy;
			  }
			}
						
			GrEl.setX(Rtp.getX() + dx);
			GrEl.setY(Rtp.getY() + dy);
			GrEl.setWidth(IP.w * w / 100.0);
			GrEl.setHeight(IP.h * h / 100.0);
			GrEl.setRot(IP.rot + rot);
		}
	}

	public GE_Node() {
	    super();
	    ElTypePath = "_void";
	    geType = NODE;
	    isManipulable = true;
	    ManipShown = false;
	    mip = null;
	}	

/*	public GE_Node(double X1, double Y1, String Ep) {
		super(Ep);
		x = X1;
		y = Y1;
		w = 100;
		h = 100;
		rot = 0.0;
		
		DrawingComps = new Vector<GraphicElement>();
		DrawingCompsInitPos = new Vector<InitPos>();
		
		Inside = Color.black;
		updateShape();		
	}
*/
	public GE_Node(String Ep) {
		super(Ep);
		x = 0.0;
		y = 0.0;
		w = 100;
		h = 100;
		rot = 0.0;
	        geType = NODE;
		
		drawingComps = new Vector<layedEl>();
		DrawingCompsInitPos = new Vector<InitPos>();
	    isManipulable = true;
		ManipShown = false;
		mip = null;
		
		Inside = Color.black;
		updateShape();		
	}

	void checkMip() {
		boolean buildIt = false;
		
		if(mip == null) {
			buildIt = true;
//System.out.println("Building mip (is null)");
/*		} else {
			for(int i = 1; i < mip.length; i++) {
				if(mip[i] == null) {
System.out.println("Building mip (mip[" + i +"] is null)");
					buildIt = true;
					break;
				}
			} */
		}
		
		if(buildIt) {
		  if(getDataSet() != null) {
		   if(getDataSet().getElementInstance() != null) {
			FixedArrayPropertyValue fp = (FixedArrayPropertyValue)getDataSet().getElementInstance().getPropertyValue("G_labels");

			if(fp != null) {
				mip = new manipInitPos[fp.size()];
				for(int i = 1; i < mip.length; i++) {
					StructPropertyValue spv = (StructPropertyValue)fp.getValue(i);
					mip[i] = new manipInitPos(((FloatPropertyValue)(spv.getPropertyValue("x"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("y"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("sx"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("sy"))).getValue());
				}
			}
/*			else {
System.out.println("Cannot build mip (fp null)");
			} */
		   }
		  }
		}
	}

	public void paint(Graphics2D g) {
/*		Paint oldPaint = g.getPaint();
		g.setPaint(Inside);

		if(!isSelected())
			g.draw(NShp);
		else {
			g.setPaint(Color.red);
			g.draw(NShp);
		}
		
		if(isLocked()) {
			g.setPaint(Color.black);
			g.fill(new Ellipse2D.Double(x-5,y-5,10,10));
		}

		g.setPaint(oldPaint);*/

	    
		HashSet<String> invisibleLayers = DrawNET.getApplication().getMainFrame().getDNDesktop().getCurrentLayerPanel().getInvisibleLayersHashSet();

		// Prepares the mip array from the element definition if not already cached

		checkMip();

		for(layedEl lgrElem : drawingComps) {
//System.out.println(lgrElem.Layer);
			GraphicElement grElem = lgrElem.Ge;
			if(!invisibleLayers.contains(lgrElem.Layer)) {
				grElem.setDataSet(dataSet);
	            grElem.paint(g);
            }
		}
		
		if(ManipShown) {
			double rx, ry;
		
			if(mip != null) {
				for(int i = 1; i < mip.length; i++) {
					rx = x + mip[i].x; ry = y + mip[i].y;
					g.draw(new Rectangle2D.Double(rx-3, ry-3, 7, 7));
				}
			}
		}
		
		if(isSelected()) {
			g.setPaint(Color.red);
			g.draw(new Rectangle2D.Double(x-10, y-10, 20, 20));
		}
//g.setPaint(Color.red);
//g.draw(BBox);
	}
	
	public boolean contains(double X, double Y)
	{
		return BBox.contains(X, Y);
	}

	public boolean intersects(double X, double Y, double W, double H)
	{
		return BBox.intersects(X, Y, W, H);
	}

	public Shape getOutlineShape()
	{
	   	return BBox;
	}

	public Object clone() throws CloneNotSupportedException {
		GE_Node C;
		try {
			C = (GE_Node)super.clone();
			C.updateShape();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			throw ex;
		}
		
		return C;
	}
	
	public void setSelected(boolean S) {
		for (layedEl lGrEl : drawingComps) {
			GraphicElement GrEl = lGrEl.Ge;
	        GrEl.setSelected(S);
	    }
	    super.setSelected(S);
	}
	
	public void setLocked(boolean S) {
		for (layedEl lGrEl : drawingComps) {
			GraphicElement GrEl = lGrEl.Ge;
	        GrEl.setLocked(S);
	    }
	    super.setLocked(S);
	}	
	
	public GraphicElement createFromElementInstance(ElementInstance EI, String Elp) {
		GE_Node out = new GE_Node(Elp);

		return createFromElementInstance(EI, Elp, out);
	}
	
	protected GraphicElement defaultGE(int geType) {
		switch(geType) {
		case GraphicElement.EDGE:
			return new GE_EdgeSpline(new DE_Stroke(Color.black, 1.0), DE_Cap.parse("none",100),
					  DE_Cap.parse("blackTri",100),
					  DE_Cap.parse("line",100),
					  DE_Cap.parse("line",100),
					  ElTypePath+"&");
		case GraphicElement.SUBMODEL:
			return new GE_Rectangle(-15, -15, 31, 31, new DE_Fill(Color.red), new DE_Stroke(Color.black, 1.0), ElTypePath+"&");
		default:
			return new GE_Rectangle(-7, -7, 7, 7, new DE_Fill(Color.gray), new DE_Stroke(Color.black, 2.0), ElTypePath+"&");
		}
	}

	protected GraphicElement createFromElementInstance(ElementInstance EI, String Elp, GE_Node out) {

		Vector<layedEl> De = new Vector<layedEl>();
		Vector<InitPos> DeIP = new Vector<InitPos>();

		Rectangle2D.Double cBBoxInit = new Rectangle2D.Double(-2.0, -2.0, 5.0, 5.0);
				
    		ElementInstance Gd = ElementRefPropertyValue.getElementRefProperty(EI, "G_iconRef");

		if(Gd != null) {
			mip = new manipInitPos[IntegerPropertyValue.getIntegerProperty(Gd, "N_labelSets")+1];
			Vector<ElementInstance> QRefEl = DrawNETQuerySelectAndSort.makeQuery(
								Gd, "G_order"
							).Execute();
	        for(Enumeration<ElementInstance> LEn = QRefEl.elements(); LEn.hasMoreElements();) {
	            ElementInstance DrEl = LEn.nextElement();
//System.out.println(ElementRefPropertyValue.getElementRefProperty(DrEl, "G_inLayer"));
	            GraphicElement BaseEl = DrawNET.getApplication().getMainFrame().getDNDesktop().getGraphicElementByID(
						            	DrEl.getElementType().getElementTypeAbsolutePath() );
			    if(BaseEl != null) {
	                GraphicElement GE = BaseEl.createFromElementInstance(DrEl, ElTypePath+"&");

	                int labGrp = IntegerPropertyValue.getIntegerProperty(DrEl, "G_labelSet");
	                if((labGrp < mip.length) && (labGrp > 0)) {
	                	if(mip[labGrp] == null) {
	                		mip[labGrp] = new manipInitPos(GE.getX(), GE.getY());
	                	}
	                }

	                DeIP.add(new InitPos(GE.getX(), GE.getY(), GE.getWidth(), GE.getHeight(), GE.getRot()));
	                ElementInstance LayerInst = ElementRefPropertyValue.getElementRefProperty(DrEl, "G_inLayer"); 
	                De.add(new layedEl(GE, (LayerInst != null) ? LayerInst.getId() : null, IntegerPropertyValue.getIntegerProperty(DrEl, "G_labelSet")));
					Rectangle2D GEbbx = GE.getOutlineShape().getBounds2D();
					GEbbx = GEbbx.createUnion(cBBoxInit.getBounds2D());
					cBBoxInit.x = GEbbx.getX();
					cBBoxInit.y = GEbbx.getY();
					cBBoxInit.width = GEbbx.getWidth();
					cBBoxInit.height = GEbbx.getHeight();
	            } else {
					System.out.println("ERROR: " + DrEl.getElementType().getElementTypeAbsolutePath() + " in " + ElTypePath + " does not have an associated GE. It cannot be visualized!");
		   	    }
			}
			
			if(mip.length > 1) {
			  FixedArrayPropertyValue oldp = (FixedArrayPropertyValue)EI.getPropertyValue("G_labels");
			  if(oldp == null) {
				FixedArrayPropertyType Fap = (FixedArrayPropertyType)EI.getElementType().getProperty("G_labels", true);
//System.out.println("Mip size: " + mip.length + ", G_labels PType: " + Fap);
				FixedArrayPropertyValue manipPos = new FixedArrayPropertyValue(Fap, mip.length);
//System.out.println("manipPos PV: " + manipPos);		
				StructPropertyType Spt = (StructPropertyType)(Fap.getSubPropertyType());
//System.out.println("manipPos Spt: " + Spt);		
	
				for(int i = 0; i < mip.length; i++) {
					if((i > 0) && (mip[i] != null)) {
//System.out.println(i + ": " + mip[i].x + ", " + mip[i].y);
				    
					    StructPropertyValue Sv = new StructPropertyValue(Spt);
					    Sv.setPropertyValue("x", new FloatPropertyValue(Spt.getProperty("x"), mip[i].x));
					    Sv.setPropertyValue("y", new FloatPropertyValue(Spt.getProperty("y"), mip[i].y));
					    Sv.setPropertyValue("sx", new FloatPropertyValue(Spt.getProperty("sx"), mip[i].sx));
					    Sv.setPropertyValue("sy", new FloatPropertyValue(Spt.getProperty("sy"), mip[i].sy));
					    manipPos.setValue(i, Sv);
					} else {
					    StructPropertyValue Sv = new StructPropertyValue(Spt);
					    Sv.setPropertyValue("x", new FloatPropertyValue(Spt.getProperty("x"), 0.0));
					    Sv.setPropertyValue("y", new FloatPropertyValue(Spt.getProperty("y"), 0.0));
					    Sv.setPropertyValue("sx", new FloatPropertyValue(Spt.getProperty("sx"), 0.0));
					    Sv.setPropertyValue("sy", new FloatPropertyValue(Spt.getProperty("sy"), 0.0));
					    manipPos.setValue(i, Sv);
					}
				}
				EI.setPropertyValue("G_labels", manipPos);
			  } else {
//System.out.println("Building MIP for " + EI.getId());
				out.mip = new manipInitPos[mip.length];
				for(int i = 0; i < mip.length; i++) {
					StructPropertyValue spv = (StructPropertyValue)oldp.getValue(i);
					out.mip[i] = new manipInitPos(((FloatPropertyValue)(spv.getPropertyValue("x"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("y"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("sx"))).getValue(),
								((FloatPropertyValue)(spv.getPropertyValue("sy"))).getValue());
//System.out.println(i + " " + mip[i].x + " " + mip[i].y + " " + mip[i].sx + " " + mip[i].sy);
				}

			  }
			}			
		} else {
		    	De.add(new layedEl(defaultGE(out.getType()), null, 0));
		    	DeIP.add(new InitPos(-7, -7, 15, 15, 0));
			cBBoxInit.x = -7;
			cBBoxInit.y = -7;
			cBBoxInit.width = 15;
			cBBoxInit.height = 15;
		}

		out.readG_TransformFromInstance(EI);

		out.ElTypePath = EI.getElementType().getElementTypeAbsolutePath();
		out.drawingComps = De;
		out.DrawingCompsInitPos = DeIP;
		out.BBoxInit = cBBoxInit;
		out.updateShape();
		
		return out;
	}

	public void setDataSet(ElementLevelDataSet DS) {
	    dataSet = DS;
	}

	public void updateElementInstance() {
	    DrawNETModelElementInstancePanel Ep;
	    super.updateElementInstance();

	    if(dataSet != null) {
	        ElementInstance nI = dataSet.getElementInstance();

	        Ep = (DrawNETModelElementInstancePanel)dataSet.getElementPanel();
//	        Ep.updatePropertyValuesFromPanels(nI);
	    }
/*		if(DataSet != null) {
		    ElementInstance nI = DataSet.getElementInstance();

		    StructPropertyType Spt = (StructPropertyType)nI.getElementType().getProperty("G_transform", true);
		    StructPropertyValue Spv = new StructPropertyValue(Spt);

		    setStructFloatFieldValue(Spv, Spt, "tx", x);
		    setStructFloatFieldValue(Spv, Spt, "ty", y);
		    setStructFloatFieldValue(Spv, Spt, "sx", w);
		    setStructFloatFieldValue(Spv, Spt, "sy", h);
		    setStructFloatFieldValue(Spv, Spt, "r", rot);

		    nI.setPropertyValue("G_transform", Spv);

		    Ep = (DrawNETModelElementInstancePanel)DataSet.getElementPanel();
		    Ep.updatePropertyValuesFromPanels(nI);
		}
*/	}

	public void updatePanels() {
	    super.updatePanels();

	    DrawNETModelElementInstancePanel Ep;
	    if(dataSet != null) {
		Ep = (DrawNETModelElementInstancePanel)dataSet.getElementPanel();
//		Ep.setInstanceName(DataSet.getName());
		Ep.updatePropertyValues(dataSet.getElementInstance());
	    }
	}

	void AddRefToArrayProp(ElementInstance Ei, String Attr) {
	    if(dataSet != null) {
	        ElementInstance nI = dataSet.getElementInstance();
		FixedArrayPropertyType Fap = (FixedArrayPropertyType)nI.getElementType().getProperty(Attr, true);
	    	FixedArrayPropertyValue fpV = (FixedArrayPropertyValue)nI.getPropertyValue(Attr);
	    	int newSize = (fpV != null) ? (fpV.size()+1) : 1;
		FixedArrayPropertyValue newFpV = new FixedArrayPropertyValue(Fap, newSize);
		for(int i = 0; i < newSize-1; i++) {
			newFpV.setValue(i, fpV.getPropertyValueAt(i));
		}
		newFpV.setValue(newSize-1, new ElementRefPropertyValue(Ei));
		nI.setPropertyValue(Attr, newFpV);
	    }
	}

	public void ConnectedToArc(ElementInstance Ei) {
	    AddRefToArrayProp(Ei, "G_toConnections");
	}
	
	public void ConnectedFromArc(ElementInstance Ei) {
	    AddRefToArrayProp(Ei, "G_fromConnections");
	}
	
	void ClearArrayProp(String Attr) {
	    if(dataSet != null) {
	        ElementInstance nI = dataSet.getElementInstance();
		FixedArrayPropertyType Fap = (FixedArrayPropertyType)nI.getElementType().getProperty(Attr, true);
	    	FixedArrayPropertyValue fpV = (FixedArrayPropertyValue)nI.getPropertyValue(Attr);
		FixedArrayPropertyValue newFpV = new FixedArrayPropertyValue(Fap, 0);
		nI.setPropertyValue(Attr, newFpV);
	    }
	}

	public void ClearConnectedFromAndTo() {
		ClearArrayProp("G_toConnections");
		ClearArrayProp("G_fromConnections");		
	}

	protected int RemoveRefFromArrayProp(ElementInstance Ei, String Attr) {
	    if(dataSet != null) {
	        ElementInstance nI = dataSet.getElementInstance();
		FixedArrayPropertyType Fap = (FixedArrayPropertyType)nI.getElementType().getProperty(Attr, true);
	    	FixedArrayPropertyValue fpV = (FixedArrayPropertyValue)nI.getPropertyValue(Attr);
	    	int newSize = ((fpV != null) && (fpV.size() > 0)) ? (fpV.size()-1) : 0;
		FixedArrayPropertyValue newFpV = new FixedArrayPropertyValue(Fap, newSize);

		int j = 0;		
		for(int i = 0; i <= newSize; i++) {
			if (fpV != null) // Daniele
			{
			ElementRefPropertyValue opV = (ElementRefPropertyValue)fpV.getPropertyValueAt(i);
			if (opV != null) // Daniele
			if(opV.getElementReference() != Ei) {
			    newFpV.setValue(j, fpV.getPropertyValueAt(i));
			    j++;
			} else {
			}
			} 
			else
				System.out.println("fpv null"); // Daniele
		}
		nI.setPropertyValue(Attr, newFpV);
		return newSize;
	    }
	    return 0;
	}
	
	public boolean DisconnectedToArc(ElementInstance Ei) {
	    RemoveRefFromArrayProp(Ei, "G_toConnections");
	    return false;
	}
	
	public boolean DisconnectedFromArc(ElementInstance Ei) {
	    RemoveRefFromArrayProp(Ei, "G_fromConnections");
	    return false;
	}	
	
	public void move(double Dx, double Dy) {
	    super.move(Dx, Dy);
	    if(dataSet != null) {
//	        if(desktop == null) desktop = DrawNET.getApplication().getDNFrame().getDNDesktop();

	        ElementInstance nI = dataSet.getElementInstance();
	    	FixedArrayPropertyValue fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_fromConnections");
	    	int cSize = (fpV != null) ? fpV.size() : 0;
	    	for(int i = 0; i < cSize; i++) {
	    		ElementInstance cEdge = ((ElementRefPropertyValue)fpV.getPropertyValueAt(i)).getElementReference();
	    		GE_Edge gEdge= (GE_Edge)DrawNET.getApplication().getMainFrame().getDNDesktop().getGraphicElementByInstanceName(cEdge.getElementInstanceAbsolutePath());
			gEdge.AdjustFromTo(-1, Dx, Dy);
	    	}
	    	fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_toConnections");
	    	cSize = (fpV != null) ? fpV.size() : 0;
	    	for(int i = 0; i < cSize; i++) {
	    		ElementInstance cEdge = ((ElementRefPropertyValue)fpV.getPropertyValueAt(i)).getElementReference();
	    		GE_Edge gEdge= (GE_Edge)DrawNET.getApplication().getMainFrame().getDNDesktop().getGraphicElementByInstanceName(cEdge.getElementInstanceAbsolutePath());
			if (gEdge != null) // Daniele
				gEdge.AdjustFromTo(1, Dx, Dy);
	    	}
	    }
	}


	public boolean beforeDelete(int deleteTool) {
//	    if(desktop == null) desktop = DrawNET.getApplication().getDNFrame().getDNDesktop();
		
	    if(dataSet != null) {
	    	
	        ElementInstance nI = dataSet.getElementInstance();
	    	
/* Create an instance of a null node if deleteTool is	    	 */
		ElementInstance nullI = null;
		GE_null nullGE = null;
		
	    	FixedArrayPropertyValue from_fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_fromConnections");
	    	int from_cSize = (from_fpV != null) ? from_fpV.size() : 0;
	    	FixedArrayPropertyValue to_fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_toConnections");
	    	int to_cSize = (to_fpV != null) ? to_fpV.size() : 0;
		
	    	if((deleteTool == GraphicElement.DT_CONNECTED_ERASER) && (from_cSize+to_cSize>0)) {
			Model M = DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedFrameModel();
			String CurrSM = DrawNET.getApplication().getMainFrame().getDNDesktop().getCurrentSubModelId();
			ElementType GE_null_ET = M.getFormalism().getSubElement("GE_null",true);
	
			nCNT = GraphicElement.getNextCNT(nCNT, M, CurrSM + ".null");
			String ElName = "null" + nCNT;
			String _ElPath = CurrSM + "." + ElName;
			M.addSubElement(ElName, GE_null_ET, CurrSM, true);
			nullI = M.getSubElementByPath(_ElPath);

			GraphicElement.setG_TransformProperty(nullI, x, y, 100, 100, 0, 0,0.0);
			GE_null nullGEmaker = new GE_null();
		        nullGE = (GE_null)nullGEmaker.createFromElementInstance(nullI, _ElPath);
		        ElementLevelDataSet nullELDS = DrawNET.getApplication().getMainFrame().getDNDesktop().makeElementLevelDataSet(_ElPath, nullGE, nullI);
		        DrawNET.getApplication().getMainFrame().getDNDesktop().addToCurrentLayer(nullELDS);
		        DrawNET.getApplication().getMainFrame().getDNDesktop().repaintDrawArea();

	    	}	
	    	
/* Return to normal */
	    	
	    	
	        DrawNETLayersPanel LayersPanel = DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedSubModelDataSet().getLayersPanel();
	        Vector<ElementInstance> elToDelete = new Vector<ElementInstance>();
	        Vector<ElementInstance> elFromDelete = new Vector<ElementInstance>();

	    	for(int i = 0; i < from_cSize; i++) {
	    	    ElementInstance cEdge = ((ElementRefPropertyValue)from_fpV.getPropertyValueAt(i)).getElementReference();
		    elFromDelete.add(cEdge);
		    if(deleteTool == GraphicElement.DT_CONNECTED_ERASER) {
		    	nullGE.ConnectedFromArc(cEdge);
		    }
	    	}
	    	for(int i = 0; i < from_cSize; i++) {
	    	    ElementInstance cEdge = elFromDelete.get(i);
	    	    GE_Edge gEdge= (GE_Edge)DrawNET.getApplication().getMainFrame().getDNDesktop().getGraphicElementByInstanceName(cEdge.getElementInstanceAbsolutePath());
		    if(deleteTool == GraphicElement.DT_STANDARD_ERASER) {
    	                gEdge.beforeDelete(deleteTool);
	    	        LayersPanel.removeElement(gEdge.getDataSet());
	    	    } else {
	    	    	ElementRefPropertyValue.setElementRefProperty(cEdge, "from", nullI);
	    	    }
	    	}

	    	for(int i = 0; i < to_cSize; i++) {
	    	    ElementInstance cEdge = ((ElementRefPropertyValue)to_fpV.getPropertyValueAt(i)).getElementReference();
		    elToDelete.add(cEdge);
		    if(deleteTool == GraphicElement.DT_CONNECTED_ERASER) {
		    	nullGE.ConnectedToArc(cEdge);
		    }
	    	}
	    	for(int i = 0; i < to_cSize; i++) {
	    	    ElementInstance cEdge = elToDelete.get(i);
	    	    GE_Edge gEdge= (GE_Edge)DrawNET.getApplication().getMainFrame().getDNDesktop().getGraphicElementByInstanceName(cEdge.getElementInstanceAbsolutePath());
		    if(deleteTool == GraphicElement.DT_STANDARD_ERASER) {
    	                gEdge.beforeDelete(deleteTool);
	    	        LayersPanel.removeElement(gEdge.getDataSet());
	    	    } else {
	    	    	ElementRefPropertyValue.setElementRefProperty(cEdge, "to", nullI);
	    	    }
	    	}
	    }
	    return true;
	}

	public void swapFromToAttr(GE_Node newE) {
//	    if(desktop == null) desktop = DrawNET.getApplication().getDNFrame().getDNDesktop();
	    if(dataSet != null) {	    	
	        ElementInstance nI = dataSet.getElementInstance();
		ElementInstance newE_I = newE.getDataSet().getElementInstance();
		
	    	FixedArrayPropertyValue from_fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_fromConnections");
	    	int from_cSize = (from_fpV != null) ? from_fpV.size() : 0;
	    	FixedArrayPropertyValue to_fpV = (FixedArrayPropertyValue)nI.getPropertyValue("G_toConnections");
	    	int to_cSize = (to_fpV != null) ? to_fpV.size() : 0;

	    	for(int i = 0; i < from_cSize; i++) {
	    	    ElementInstance cEdge = ((ElementRefPropertyValue)from_fpV.getPropertyValueAt(i)).getElementReference();
		    newE.ConnectedFromArc(cEdge);
		    ElementRefPropertyValue.setElementRefProperty(cEdge, "from", newE_I);
	    	}
	    	for(int i = 0; i < to_cSize; i++) {
	    	    ElementInstance cEdge = ((ElementRefPropertyValue)to_fpV.getPropertyValueAt(i)).getElementReference();
		    newE.ConnectedToArc(cEdge);
		    ElementRefPropertyValue.setElementRefProperty(cEdge, "to", newE_I);
	    	}
	    	ClearConnectedFromAndTo();
	    }
	}



	public void showManipulators(int Level) {
		ManipShown = true;
//		ManipLevel = Level;
	}

	public void hideManipulators() {
		ManipShown = false;
	}

	public int findManipulator(double xx, double yy, boolean Shift, int Level) {
		checkMip();

		AffineTransform Trans = AffineTransform.getTranslateInstance(x, y);
		Trans.concatenate(AffineTransform.getRotateInstance(-rot * Math.PI / 180.0));
		Trans.concatenate(AffineTransform.getScaleInstance(w / 100.0, h / 100.0));
		Trans.concatenate(AffineTransform.getTranslateInstance(-pivotX, -pivotY));

		if(mip != null) {
			for(int i = 1; i < mip.length; i++) {
				Point2D Rtp = Trans.transform(new Point2D.Double(mip[i].x, mip[i].y), null);

			    Shape M1 = new Rectangle2D.Double(Rtp.getX()-2,Rtp.getY()-2,5,5);
			    if(M1.contains(xx, yy)) return i;

//System.out.println("Is (" + xx + ", " + yy + ") near (" + Rtp.getX() + ", " + Rtp.getY() + ") ?");
			}
		}
//System.out.println("No manip found at (" + xx + ", " + yy + ")");
		return -1;
	}
	
	public Shape getManipulatedShape(int mn, double x, double y, boolean Shift) {
		return null;
	}
	
	public void manipulate(int mn, double mx, double my, boolean Shift) {
		AffineTransform Trans = AffineTransform.getTranslateInstance(x, y);
		Trans.concatenate(AffineTransform.getRotateInstance(-rot * Math.PI / 180.0));
		Trans.concatenate(AffineTransform.getScaleInstance(w / 100.0, h / 100.0));
		Trans.concatenate(AffineTransform.getTranslateInstance(-pivotX, -pivotY));
	    Point2D SSmP = new Point2D.Double();
	    try {
	        SSmP = Trans.inverseTransform(new Point2D.Double(mx, my), null);
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    double xx = SSmP.getX();
	    double yy = SSmP.getY();
	    
//System.out.println("Moving manip " + mn + " to (" + xx + ", " + yy + ")");

		mip[mn].x = xx;
		mip[mn].y = yy;

		if(getDataSet() != null) {
			FixedArrayPropertyType Fap = (FixedArrayPropertyType)getDataSet().getElementInstance().getElementType().getProperty("G_labels", true);
			StructPropertyType Spt = (StructPropertyType)(Fap.getSubPropertyType());
			FixedArrayPropertyValue fp = (FixedArrayPropertyValue)getDataSet().getElementInstance().getPropertyValue("G_labels");
			StructPropertyValue Sv = (StructPropertyValue)fp.getValue(mn);
		    Sv.setPropertyValue("x", new FloatPropertyValue(Spt.getProperty("x"), mip[mn].x));
		    Sv.setPropertyValue("y", new FloatPropertyValue(Spt.getProperty("y"), mip[mn].y));
		}

	    updateShape();
	    updateElementInstance();
	}
	
	public void addIntermediatePoint(double x, double y) {
	}
	
	public void deleteManipulator(int mn) {
	}
	
	
/*	Vector GEandPC;		// Array of Graphic Elements and the condtion required to visualize them
	int CurrentGE;
	GraphicElement DefaultGE;
	
	Vector FromEdges;
	Vector ToEdges;

    	DefaultMutableTreeNode SubElements;

	public GE_Node(int T, String NGL) {
		super(D, NGL);
		
		x = 0.0;
		y = 0.0;
		w = 20.0;
		h = 20.0;
		rot = 0.0;
		DefaultGE = new GE_NShape(0.0, 0.0, NGL);
		GEandPC = new Vector();

		CurrentGE = -1;
		Type = T;
		
		FromEdges = new Vector();
		ToEdges = new Vector();
		
		SubElements = null;
	}

	GraphicElement getCurrentGE() {
	    if(CurrentGE == -1)
	        return DefaultGE;
	    else
	        return ((GEandPropCondition)(GEandPC.get(CurrentGE))).getGraphicElement();
	}

	String getCurrentProp() {
	    if(CurrentGE == -1)
	        return "*";
	    else
	        return ((GEandPropCondition)(GEandPC.get(CurrentGE))).getPropCondition().getCondition();
	}

	public void paint(Graphics2D g) {
	    getCurrentGE().paint(g);
	}
	
	public boolean contains(double X, double Y)
	{
	    return getCurrentGE().contains(X, Y);
	}

	public boolean intersects(double X, double Y, double W, double H)
	{
	    return getCurrentGE().intersects(X, Y, W, H);
	}

	public Shape getOutlineShape()
	{
	    return getCurrentGE().getOutlineShape();
	}

	public Object clone() {
		GE_Node C = (GE_Node)super.clone();
		Enumeration e;
//		C.CompleteNameInModel = CompleteNameInModel;
		C.GEandPC = new Vector();
		for(e = GEandPC.elements(); e.hasMoreElements(); ) {
		    GEandPropCondition El = (GEandPropCondition)e.nextElement();
		    
		    C.add((GraphicElement)El.getGraphicElement().clone(),
		    	  PropCondition.makePropCondition(
		    	  	El.getPropCondition().getCondition(),
		    	        C,  desktop));
		}
		C.DefaultGE = (GraphicElement)DefaultGE.clone();
		C.CurrentGE = CurrentGE;
		
		C.FromEdges = new Vector();
		for(e = FromEdges.elements(); e.hasMoreElements(); ) {
		    C.FromEdges.add(e.nextElement());
		}
		C.ToEdges = new Vector();
		for(e = ToEdges.elements(); e.hasMoreElements(); ) {
		    C.ToEdges.add(e.nextElement());
		}
		
		C.updateShape();
		return C;
	}

**	public void updatePanels() {
	    super.updatePanels();

	    DrawNETGorM_ElementPanel Ep;
	    if(DataSet != null) {
		Ep = (DrawNETGorM_ElementPanel)DataSet.getElementPanel();
		Ep.setInstanceName(DataSet.getName());
	    }
	}
**	
	public void updateShape() {
	    CurrentGE = -1;
	    int i = 0;
	    boolean Matched = false;
	    Enumeration e;
	    
	    for(e = GEandPC.elements(); e.hasMoreElements(); ) {
	    	GEandPropCondition GEcP = (GEandPropCondition)e.nextElement();
	    	if(!Matched) {
	    	    if(GEcP.Match()) {
	    	    	Matched = true;
	    	    	CurrentGE = i;
	    	    }
	    	}
	    	GraphicElement G = GEcP.getGraphicElement();
	    	G.setXYWHR(x, y, w, h, rot);
	    	G.setSelected(isSelected());
	    	G.setLocked(isLocked());
	        G.updateShape();
	        i++;
	    }
	    DefaultGE.setXYWHR(x, y, w, h, rot);
	    DefaultGE.setSelected(isSelected());
	    DefaultGE.setLocked(isLocked());
	    DefaultGE.updateShape();
	    
	    for(e = FromEdges.elements(); e.hasMoreElements();) {
	    	String ELDSName = (String)e.nextElement();
		ElementLevelDataSet ELDS = desktop.getElementLevelDataSet(ELDSName);
//System.out.println("Updating edge (From): " + ELDSName + " " + ELDS);
		if(ELDS != null) {
	    	    GE_Edge Ged = (GE_Edge)ELDS.getGraphicElement();
	    	    Ged.setEdgeStart(x, y);
		}
	    }

	    for(e = ToEdges.elements(); e.hasMoreElements();) {
	    	String ELDSName = (String)e.nextElement();
		ElementLevelDataSet ELDS = desktop.getElementLevelDataSet(ELDSName);
//System.out.println("Updating edge (To):   " + ELDSName + " " + ELDS);
		if(ELDS != null) {
	    	    GE_Edge Ged = (GE_Edge)ELDS.getGraphicElement();
	    	    Ged.setEdgeEnd(x, y);
		}
	    }
	}
	
	public void add(GraphicElement G, PropCondition C) {
	    GEandPC.add(new GEandPropCondition(G, C));
	    
	    if(G.getWidth() > w) w = G.getWidth();
	    if(G.getHeight() > h) h = G.getHeight();
	}

	public void add(GraphicElement G, String C) {
	    add(G, PropCondition.makePropCondition(C, this, desktop));
	}
	
	public void addToEdges(String Ed)
	{
	    ToEdges.add(Ed);
	}

	public void addFromEdges(String Ed)
	{
	    FromEdges.add(Ed);
	}
	
	public void setSubElements(DefaultMutableTreeNode DMT) {
	    SubElements = DMT;
	}
	
	public DefaultMutableTreeNode getSubElements() {
	    return SubElements;
	}
	
	public void removeConnectedEdges(DrawNETLayersPanel LayersPanel)
	{
	    Enumeration e;
	    for(e = FromEdges.elements(); e.hasMoreElements();) {
	    	String ELDSName = (String)e.nextElement();
		ElementLevelDataSet ELDS = desktop.getElementLevelDataSet(ELDSName);
		if(ELDS != null) {
			LayersPanel.removeElement(ELDS);
		}
	    }

	    for(e = ToEdges.elements(); e.hasMoreElements();) {
	    	String ELDSName = (String)e.nextElement();
		ElementLevelDataSet ELDS = desktop.getElementLevelDataSet(ELDSName);
		if(ELDS != null) {
			LayersPanel.removeElement(ELDS);
		}
	    }
	}
**	public String toXML() {
	    return "<use x=\"" + x + "\" y=\"" + y +
	    	      "\" id=\"" + DataSet.getName() +
	    	      "\" xlink:href=\"#" + GrLink +
	    	      "." + getCurrentProp() +
	    	      "\" />";
	}
**
*/
}
