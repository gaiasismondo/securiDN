package drawnet.lib.solvers;

import java.util.Enumeration;
import java.util.ArrayList;
import java.io.File;

import javax.swing.JOptionPane;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.FixedArrayPropertyValue;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.lib.ddl.propertyvalues.BooleanPropertyValue;
import drawnet.lib.ddl.propertyvalues.StructPropertyValue;
import drawnet.lib.ddl.propertyvalues.StringPropertyValue;
import drawnet.lib.ddl.propertytypes.StructPropertyType;
import drawnet.lib.ddl.propertytypes.FixedArrayPropertyType;
import drawnet.lib.ddl.propertytypes.PropertyType;
import drawnet.lib.filters.XMLModelFilter;

import drawnet.DrawNET;
import drawnet.exception.DNException;
import drawnet.modelgui.graphframe.DrawNETGraphFrame;
import drawnet.modelgui.menus.DrawNETWindowMenu;
import drawnet.xml.mx.MXFile;
import drawnet.lib.ddl.Model;

public class SolverFilterSecuriDN extends SolverFilter 
{

  private ElementInstance arch, ag, dbn, attack=null, goal=null;
  private ArrayList<String> nodesIn; // nodesOut;
  private ArrayList<String> visitedNodes;
  private ArrayList<String> visitedAssets;
  private ArrayList<String> reachableAssets;

  private final String DESCR = "Arch ---> AG ---> DBN";

  private int maxOrder = -1;
  
  /**
   * Constructor.
   */
  public SolverFilterSecuriDN() 
  {
    	super();
    	setDescription( DESCR );
  }

  /**
   * Constructor.
   * 
   * @param listener the listener to assign to this solver
   */
  public SolverFilterSecuriDN( SolverListener listener ) 
  {
    	super( listener );
    	setDescription( DESCR );
  }

  /*
   * (non-Javadoc)
   * 
   * @see solvers.SolverLauncher#execute()
   */

  private boolean mainVisit()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
  	ElementType elementType;
	int archCount = 0;
  	int agCount = 0;
 	int dbnCount = 0;

	// CERCA ARCH, AG GENERALE, DBN, NEL MODELLO PRINCIPALE.
	enumeration = model_.getMainElement().subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		
		//System.out.println("mainVisit " + elementInstance.getId() + ": " + elementType.getId());
		
		if (elementType.getId().equals("Arch"))
		{
			arch = elementInstance;
			archCount++;
		}
		if (elementType.getId().equals("AG"))
		{
			ag = elementInstance;
			agCount++;
			//ag = new ElementInstance(ag.getId(), ag.getElementType(), true);
			
		}		
		if (elementType.getId().equals("DBN"))
		{
			dbn = elementInstance;
			dbnCount++;
		}
	}

	if (archCount != 1 || agCount != 1 || dbnCount != 1)
	{
		JOptionPane.showMessageDialog(null, "The main model must contain one Arch, one AG, one DBN.");
		return false;
	}

	// CLEAN AG GENERALE.
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null || elementType.getParent("_root.AG.AGArc") != null)
			ag.removeSubElement(elementInstance.getId());
	}

	// CLEAN DBN.
	enumeration = dbn.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.DBN.DBNNode") != null || elementType.getParent("_root.DBN.DBNArc") != null)
			dbn.removeSubElement(elementInstance.getId());
	}

	return true;
  }

  private boolean archVisit()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, from, to, localAttack=null, localGoal=null;
  	ElementType elementType, fromType, toType;
	ElementRefPropertyValue fromRef, toRef;
	int attackCount=0, goalCount=0, assetCount=0, attackNode=0, goalNode=0;
	boolean esito=true, check;

	// CONTA IL NUMERO DI ATTACKER, GOAL, ASSET PRESENTI.
	enumeration = arch.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();	
		//System.out.println("elemento " + elementInstance.getId() + ":" + elementType.getId());
		if (elementType.getId().equals("Attacker"))
			attackCount++;
		if (elementType.getId().equals("Goal"))
			goalCount++;
		if (elementType.getParent("_root.Arch.AssetNode") != null)
			assetCount++;
	}

	if (assetCount == 0) 
	{
		JOptionPane.showMessageDialog(null, "Arch must contain at least one asset.");
		esito=false;
	}
	if (attackCount != 1 || goalCount != 1) 
	{
		JOptionPane.showMessageDialog(null, "Arch must contain one attacker and one goal.");
		esito=false;
	}
	/*if (goalCount != 1) 
	{
		JOptionPane.showMessageDialog(null, "Arch must contain one goal.");
		esito=false;
	}*/	

	if (! esito)
		return esito;

	// CONTROLLA GLI ASSET COLLEGATI A ATTACKER E GOAL.
	// CONTROLLA LE TECNICHE INDICATE IN ATTACKER E GOAL.
	enumeration = arch.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		if (elementType.getId().equals("AttackArc"))
		{
			//System.out.println("---> Trovato AttackArc " + elementInstance.getId());
			fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
			toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
			from = arch.getSubElementByPath(fromRef.toString());
			to = arch.getSubElementByPath(toRef.toString());
			fromType = from.getElementType();
			toType = to.getElementType();

			if (fromType.getId().equals("Attacker"))
			{
				attackNode++;
				//attack = ag.getSubElement(to.getId() + "_" + from.getPropertyValue("step").toString());
				localAttack = to.getSubElement(from.getPropertyValue("step").toString());
			}
			if (fromType.getId().equals("Goal"))
			{
				goalNode++;
				//goal = ag.getSubElement(to.getId() + "End");
				//goal = ag.getSubElement(to.getId() + "_" + from.getPropertyValue("step").toString());
				localGoal = to.getSubElement(from.getPropertyValue("step").toString());
			}
		}
	}

	if (attackNode != 1) 
	{
		//System.out.println("null attacker");
		JOptionPane.showMessageDialog(null, "The attacker must be connected to one asset.");
		esito=false;
	}
	if (goalNode != 1)
	{
		//System.out.println("null goal");
		JOptionPane.showMessageDialog(null, "The goal must be connected to one asset.");
		esito=false;
	}

	if (! esito)
		return esito;

	if (localAttack == null) 
	{
		//System.out.println("null attacker");
		JOptionPane.showMessageDialog(null, "The initial technique does not exist.");
		esito=false;
	}
	if (localGoal == null)
	{
		//System.out.println("null goal");
		JOptionPane.showMessageDialog(null, "The final technique does not exist.");
		esito=false;
	}

	if (! esito)
		return esito;
		
	System.out.println("\nPATH: from " + localAttack.getId() + " to " + localGoal.getId());

	// CONTROLLA L'AG LOCALE DI OGNI ASSET.
	enumeration = arch.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		if (elementType.getParent("_root.Arch.AssetNode") != null)
		{
			check = checkAgNodes(elementInstance);
			if (! check)
				esito = false;
		}
	}	

	if (! esito)
		return esito;

	// COPIA OGNI AG LOCALE NELL'AG GENERALE.
	enumeration = arch.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();	
		//System.out.println("archVisit " + elementInstance.getId() + ": " + elementType.getId());
		if (elementType.getParent("_root.Arch.AssetNode") != null)
			this.agBuild(elementInstance);
        }

	//System.out.println(localAttack.getContainer().getId() + "_" + localAttack.getId());
	//System.out.println(localGoal.getContainer().getId() + "_" + localGoal.getId());
	attack = ag.getSubElement(localAttack.getContainer().getId() + "_" + localAttack.getId());
	goal = ag.getSubElement(localGoal.getContainer().getId() + "_" + localGoal.getId());
	attack.setPropertyValue("initial", new BooleanPropertyValue(true));
	goal.setPropertyValue("final", new BooleanPropertyValue(true));
	System.out.println("\nPATH: from " + attack.getId() + " to " + goal.getId());
	
	/*
	// UNISCE DUE AG SE ESISTE UN ARCO TRA GLI ASSET CORRISPONDENTI NELL'ARCH.

	enumeration = arch.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		if (elementType.getParent("_root.Arch.AssetArc") != null)
			this.agJoin(elementInstance);
	}
	*/

	// CERCA ATTACKER E GOAL NELL'ARCHITETTURA.
	// MEMORIZZA LA TECNICA INIZIALE E LA TECNICA FINALE.
	
	return esito;
  }

  private boolean checkAgNodes(ElementInstance asset)
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
	ElementType elementType;
	FixedArrayPropertyValue toConnections, fromConnections;
	boolean esito=true;

	enumeration = asset.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();	

		// CONTROLLA SE UN NODO HA PIU' ARCHI ENTRANTI.
		// CONTROLLA SE UN NODO SHARED HA L'ARCO ENTRANTE.
		if (elementType.getId().equals("Node") || elementType.getId().equals("NodeShared"))
		{
			// archi entranti
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");		
	
			if (toConnections != null && toConnections.size() > 1)
			{
				esito=false;
				JOptionPane.showMessageDialog(null, "node " + elementInstance.getId() + " in asset " + asset.getId() + " has multiple incoming arcs. Use AND/OR node.");
			}
				
			if (elementType.getId().equals("NodeShared") && (toConnections == null || toConnections.size() == 0))	
			{
				esito=false;
				JOptionPane.showMessageDialog(null, "Shared node " + elementInstance.getId() + " in asset " + asset.getId() + " must have one incoming arc.");
			}	
		}

		// CONTROLLA CHE I NODI LOGICI ABBIANO ALMENO DUE INPUT E ALMENO UN OUTPUT.
		if (elementType.getId().equals("NodeAND") || elementType.getId().equals("NodeOR"))
		{
			// archi entranti
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");			
			if (toConnections == null || toConnections.size() < 2)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Boolean node " + elementInstance.getId() + " in asset " + asset.getId() + " must have at least two incoming arcs.");
			}

			// archi uscenti
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections == null || fromConnections.size() < 1)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Boolean node " + elementInstance.getId() + " in asset " + asset.getId() + " must have at least one outcoming arc."); 
			}
		}

		// CONTROLLA CHE L'ANALITICA ABBIA ALMENO UN ARCO ENTRANTE.
		if (elementType.getId().equals("Analytics"))
		{
			// archi entranti
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");						
			if (toConnections == null || toConnections.size() < 1)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Analytics " + elementInstance.getId() + " in asset " + asset.getId() + " must have at least one incoming arc.");
			}

			/*
			// archi uscenti
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections != null && fromConnections.size() > 0)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Analytics " + elementInstance.getId() + " in asset " + asset.getId() + " must have no outcoming arcs."); 
			}
			*/
		}

		// CONTROLLA CHE UNA DIFESA ABBIA ALMENO UN ARCO
		if (elementType.getId().equals("NodeDefense"))
		{
			/*
			// archi entranti
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");			
			if (toConnections != null && toConnections.size() > 0)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Defense " + elementInstance.getId() + " in asset " + asset.getId() + " must have no incoming arcs.");
			}
			*/
			// archi uscenti
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections == null || fromConnections.size() < 1)
			{
				esito = false;
				JOptionPane.showMessageDialog(null, "Defense " + elementInstance.getId() + " in asset " + asset.getId() + " must have at least one outcoming arc."); 
			}
		}
	}

	return esito;
  }

  private void agBuild(ElementInstance asset)
  {
	// COPIA L'AG RELATIVO ALL'ASSET, NELL'AG GENERALE.
	double txAsset, tyAsset;

	txAsset = FloatPropertyValue.getFloatProperty(asset, "G_tx");
	tyAsset = FloatPropertyValue.getFloatProperty(asset, "G_ty");
	txAsset = txAsset*1.5;
	tyAsset = tyAsset*1.5;
	//tx = Double.parseDouble(asset.getPropertyValue("tx").toString());
	//System.out.println("asset: " + asset.getId() + " " + txAsset + " " + tyAsset);

	agBuildNode(asset, txAsset, tyAsset);
	agBuildArc(asset, txAsset, tyAsset);
  }

  private void agBuildNode(ElementInstance asset, double txAsset, double tyAsset)
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, clone;
  	ElementType elementType;
	FixedArrayPropertyValue fromConnections, toConnections;
	String fap;
	int i;
	double tx, ty, prob;

	// COPIA I NODI DELL'AG LOCALE DELL'ASSET, NELL'AG GENERALE.

	enumeration = asset.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		
		if (elementType.getParent("_root.AG.AGNode") != null)
		{
			clone = (ElementInstance)elementInstance.clone();
			//if (!elementType.getId().equals("NodeShared"))
			clone.setId(asset.getId() + "_" + clone.getId());

			if (elementType.getId().equals("Node"))
			{
				prob = FloatPropertyValue.getFloatProperty(elementInstance, "prob");
				FloatPropertyValue.setFloatProperty(clone, "prob", prob);
			}

			tx = FloatPropertyValue.getFloatProperty(clone, "G_tx");
			ty = FloatPropertyValue.getFloatProperty(clone, "G_ty");
			tx = tx + txAsset;
			ty = ty + tyAsset;
			//clone.setPropertyValue("G_tx", new FloatPropertyValue(tx));
			//clone.setPropertyValue("G_ty", new FloatPropertyValue(ty));
			FloatPropertyValue.setFloatProperty(clone, "G_tx", tx);
			FloatPropertyValue.setFloatProperty(clone, "G_ty", ty);

			fromConnections = (FixedArrayPropertyValue)clone.getPropertyValue("G_fromConnections");
			if (fromConnections != null)
			{
				for (i=0; i<fromConnections.size(); i++)
				{					
					fap = fromConnections.getPropertyValueAt(i).toString();
					//System.out.println("fap: " + fap);
					fap = ag.getElementInstanceAbsolutePath() + "." + asset.getId() + fap;
					fromConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				clone.setPropertyValue("G_fromConnections", fromConnections);
			}

			toConnections = (FixedArrayPropertyValue)clone.getPropertyValue("G_toConnections");
			if (toConnections != null)
			{
				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					//System.out.println("fap: " + fap);
					fap = ag.getElementInstanceAbsolutePath() + "." + asset.getId() + fap;
					toConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				clone.setPropertyValue("G_toConnections", toConnections);
			}			

			if (!ag.addSubElement(clone))
				System.out.println(clone.getId() + " NON AGGIUNTO");
		}	
        }
  }

  private void agBuildArc(ElementInstance asset, double txAsset, double tyAsset)
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, clone;
  	ElementType elementType;
	FixedArrayPropertyValue segments, segmentsNew;
	StructPropertyValue xy, xyNew;
	StructPropertyType structType;
	FixedArrayPropertyType segmentType;
	String from, to;
	int i;
	double tx, ty, x1, x2, y1, y2, ctrlx1, ctrlx2, ctrly1, ctrly2;

	// COPIA GLI ARCHI DELL'AG LOCALE DELL'ASSET, NELL'AG GENERALE.

	enumeration = asset.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		
		if (elementType.getParent("_root.AG.AGArc") != null)
		{
			//System.out.println("sto clonando l'arco " + elementInstance.getId());
			clone = (ElementInstance)elementInstance.clone();
			clone.setId(asset.getId() + clone.getId());

			from = clone.getPropertyValue("from").toString();
			to = clone.getPropertyValue("to").toString();
			from = ag.getElementInstanceAbsolutePath() + "." + asset.getId() + "_" + from;
			to = ag.getElementInstanceAbsolutePath() + "." + asset.getId() + "_" + to;
			//System.out.println("from: " + from);
			//System.out.println("to: "+ to);
			clone.setPropertyValue("from", new ElementRefPropertyValue(from));
			clone.setPropertyValue("to", new ElementRefPropertyValue(to));

			tx = FloatPropertyValue.getFloatProperty(clone, "G_tx");
			ty = FloatPropertyValue.getFloatProperty(clone, "G_ty");
			tx = tx + txAsset;
			ty = ty + tyAsset;
			//System.out.println(clone.getId() + " " + tx + " " + ty);
			//clone.setPropertyValue("G_tx", new FloatPropertyValue(tx));
			//clone.setPropertyValue("G_ty", new FloatPropertyValue(ty));
			FloatPropertyValue.setFloatProperty(clone, "G_tx", tx);
			FloatPropertyValue.setFloatProperty(clone, "G_ty", ty);

			//System.out.println("arc: " + elementInstance.getId() + " " + from + " " + to);
			segments = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_segmentsP1");
			if (segments == null)
				continue;
			xy = (StructPropertyValue)segments.getPropertyValueAt(0);

			x1 = Double.parseDouble(xy.getPropertyValueAt("x1").toString());
			//System.out.print("x1: " + x1);
			x1 = x1 + txAsset;
			//System.out.println("\tx1: " + x1);

			x2 = Double.parseDouble(xy.getPropertyValueAt("x2").toString());
			//System.out.print("x2: " + x2);
			x2 = x2 + txAsset;
			//System.out.println("\tx2: " + x2);
			
			y1 = Double.parseDouble(xy.getPropertyValueAt("y1").toString());
			//System.out.print("y1: " + y1);
			y1 = y1 + tyAsset;
			//System.out.println("\ty1: " + y1);

			y2 = Double.parseDouble(xy.getPropertyValueAt("y2").toString());
			//System.out.print("y2: " + y2);
			y2 = y2 + tyAsset;
			//System.out.println("\ty2: " + y2);
			
			ctrlx1 = Double.parseDouble(xy.getPropertyValueAt("ctrlx1").toString());
			ctrlx1 = ctrlx1 + txAsset;
			
			ctrlx2 = Double.parseDouble(xy.getPropertyValueAt("ctrlx2").toString());
			ctrlx2 = ctrlx2 + txAsset;

			ctrly1 = Double.parseDouble(xy.getPropertyValueAt("ctrly1").toString());
			ctrly1 = ctrly1 + tyAsset;

			ctrly2 = Double.parseDouble(xy.getPropertyValueAt("ctrly2").toString());
			ctrly2 = ctrly2 + tyAsset;

			segmentType = (FixedArrayPropertyType)elementInstance.getElementType().getProperty("G_segmentsP1", true);
    	  		structType = (StructPropertyType)(segmentType.getSubPropertyType());
    	  		xyNew = new StructPropertyValue(structType);
			if (!xyNew.setPropertyValue("x1", new FloatPropertyValue(x1)))
				System.out.println("x1 non impostata");
			xyNew.setPropertyValue("x2", new FloatPropertyValue(x2));
			xyNew.setPropertyValue("y1", new FloatPropertyValue(y1));
			xyNew.setPropertyValue("y2", new FloatPropertyValue(y2));
			xyNew.setPropertyValue("ctrlx1", new FloatPropertyValue(ctrlx1));
			xyNew.setPropertyValue("ctrlx2", new FloatPropertyValue(ctrlx2));
			xyNew.setPropertyValue("ctrly1", new FloatPropertyValue(ctrly1));
			xyNew.setPropertyValue("ctrly2", new FloatPropertyValue(ctrly2));

			segmentsNew = new FixedArrayPropertyValue(segmentType, 1);
			if (!segmentsNew.setValue(0, xyNew))
				System.out.println("segments non aggiornati");
			clone.setPropertyValue("G_segmentsP1", segmentsNew);

			if (!ag.addSubElement(clone))
				System.out.println(clone.getId() + " NON AGGIUNTO");
		}	
        }	
  }

  private void setAgLayer()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
  	ElementType elementType;
	ElementRefPropertyValue layer;

	// IMPOSTA LA PROPRIETA' G_inLayer PER OGNI NODO E OGNI ARCO NELL'AG GENERALE.

	layer = new ElementRefPropertyValue(ag.getSubElement("G_Layer1"));
	//System.out.println("Layer: " + layer);
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null || elementType.getParent("_root.AG.AGArc") != null)
			if (!elementInstance.setPropertyValue("G_inLayer", layer))
       				System.out.println("layer NON impostato in " + elementInstance.getId());
	}
  }

  private void ipComputers()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, from, to, arc;
  	ElementType elementType;
	FixedArrayPropertyValue toConnections, fromConnections;
	String ip, fap;
	int i;

	// DETERMINA IP DI COMPUTER, IED, NETWORK

	enumeration = arch.subElementsEnum();
	while (enumeration.hasMoreElements())
	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();	
		if (elementType.getId().equals("Computer") || elementType.getId().equals("IED") || elementType.getId().equals("Network"))
		{
			ip = elementInstance.getPropertyValue("IP").toString();
			if (ip.length() > 0)
			{
				System.out.println(elementInstance.getId() + " IP: " + ip);
				this.ipCopy(elementInstance, ip);
			}

			// RICERCA APPLICAZIONI E VM COLLEGATE AL COMPUTER/IED

			if (elementType.getId().equals("Computer") || elementType.getId().equals("IED"))
			{
				// archi entranti
				toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
				if (toConnections != null && toConnections.size() > 0)
					for (i=0; i<toConnections.size(); i++)
					{	
						fap = toConnections.getPropertyValueAt(i).toString();
						arc = arch.getSubElement(fap);
						if (arc.getElementType().getId().equals("Execute"))
						{
							from = arch.getSubElement(arc.getPropertyValue("from").toString());
							if (from.getElementType().getId().equals("Application"))
							{
								this.ipCopy(from, ip);
								this.ipApplication(from, ip);
							}
							if (from.getElementType().getId().equals("ExecutionEnvironment"))
							{
								this.ipCopy(from, ip);
								ipExecutionEnvironment(from, ip);
							}
						}	
					}
				// archi uscenti
				fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
				if (fromConnections != null && fromConnections.size() > 0)
					for (i=0; i<fromConnections.size(); i++)
					{
						fap = fromConnections.getPropertyValueAt(i).toString();
						arc = arch.getSubElement(fap);
						if (arc.getElementType().getId().equals("Execute"))
						{
							to = arch.getSubElement(arc.getPropertyValue("to").toString());
							if (to.getElementType().getId().equals("Application"))
							{
								this.ipCopy(to, ip);
								this.ipApplication(to, ip);
							}
							if (to.getElementType().getId().equals("ExecutionEnvironment"))
							{
								this.ipCopy(to, ip);
								ipExecutionEnvironment(to, ip);
							}
						}					
					}
			}
		}
	}
  }

  private void ipExecutionEnvironment(ElementInstance vm, String ip)
  {
	FixedArrayPropertyValue toConnections, fromConnections;
	ElementInstance from, to, arc;
	String fap;
	int i;

	// RICERCA APPLICAZIONI COLLEGATE A VM

	// archi entranti
	toConnections = (FixedArrayPropertyValue)vm.getPropertyValue("G_toConnections");
	if (toConnections != null && toConnections.size() > 0)
		for (i=0; i<toConnections.size(); i++)
		{	
			fap = toConnections.getPropertyValueAt(i).toString();
			arc = arch.getSubElement(fap);
			if (arc.getElementType().getId().equals("Execute"))
			{
				from = arch.getSubElement(arc.getPropertyValue("from").toString());
				if (from.getElementType().getId().equals("Application"))
				{
					this.ipCopy(from, ip);
					this.ipApplication(from, ip);
				}
			}	
			
		}

	// archi uscenti
	fromConnections = (FixedArrayPropertyValue)vm.getPropertyValue("G_fromConnections");
	if (fromConnections != null && fromConnections.size() > 0)
		for (i=0; i<fromConnections.size(); i++)
		{
			fap = fromConnections.getPropertyValueAt(i).toString();
			arc = arch.getSubElement(fap);
			if (arc.getElementType().getId().equals("Execute"))
			{
				to = arch.getSubElement(arc.getPropertyValue("to").toString());
				if (to.getElementType().getId().equals("Application"))
				{
					this.ipCopy(to, ip);
					this.ipApplication(to, ip);
				}
			}					
		}
  }

  private void ipApplication(ElementInstance app, String ip)
  {
	FixedArrayPropertyValue toConnections, fromConnections;
	ElementInstance from, to, arc;
	String fap;
	int i;

	// RICERCA CHANNEL COLLEGATI A SERVER APPLICATION

	// archi entranti
	toConnections = (FixedArrayPropertyValue)app.getPropertyValue("G_toConnections");
	if (toConnections != null && toConnections.size() > 0)
		for (i=0; i<toConnections.size(); i++)
		{	
			fap = toConnections.getPropertyValueAt(i).toString();
			arc = arch.getSubElement(fap);
			if (arc.getElementType().getId().equals("CommunicateServer"))
			{
				from = arch.getSubElement(arc.getPropertyValue("from").toString());
				if (from.getElementType().getId().equals("Channel"))
					this.ipCopy(from, ip);
			}	
			
		}

	// archi uscenti
	fromConnections = (FixedArrayPropertyValue)app.getPropertyValue("G_fromConnections");
	if (fromConnections != null && fromConnections.size() > 0)
		for (i=0; i<fromConnections.size(); i++)
		{
			fap = fromConnections.getPropertyValueAt(i).toString();
			arc = arch.getSubElement(fap);
			if (arc.getElementType().getId().equals("CommunicateServer"))
			{
				to = arch.getSubElement(arc.getPropertyValue("to").toString());
				if (to.getElementType().getId().equals("Channel"))
					this.ipCopy(to, ip);
			}					
		}
  }

  private void ipCopy(ElementInstance asset, String ip)
  {
	// COPIA IP NELL'AG GENERALE PER LE TECNICHE DELL'ASSET 

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
  	ElementType elementType;
	
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		
		if (elementType.getId().equals("Node") && elementInstance.getId().contains(asset.getId() + "_"))
		{
			elementInstance.setPropertyValue("IP", new StringPropertyValue(ip));	
			System.out.println(elementInstance.getId() + " IP: " + ip);
		}
	}
  }

  private void agJoin3()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, orNode, orArc, arc, to;
  	ElementType elementType;
	FixedArrayPropertyValue toConnections, fromConnections, newConnections, segmentValues;
	FixedArrayPropertyType connectionType, segmentType;
	StructPropertyValue fapValue0;
	StructPropertyType fapType;
	ElementRefPropertyValue layer;
	boolean merged;
	double tx, ty, x1, y1, x2, y2;
	int order, i;
	String fap;

        // UNISCE DUE AG SE ESISTE UN CAMMINO TRA GLI ASSET CORRISPONDENTI NELL'ARCH.
	
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		//System.out.print("\tControllo l'elemento " + elementInstance.getId());
		//System.out.println(": " + elementType.getId());
		if (elementType.getId().equals("NodeShared"))
		{
			//merged = 
			this.agMerge(elementInstance);
			//if (merged)
			enumeration = ag.subElementsEnum();
		}
	}

	// AGGIUNGE OR SE UN NODO HA PIU' DI UN ARCO ENTRANTE.

	layer = new ElementRefPropertyValue(ag.getSubElement("G_Layer1"));

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
		//System.out.print("\tControllo l'elemento " + elementInstance.getId());
		//System.out.println(": " + elementType.getId());
		if (elementType.getId().equals("Node"))
		{
			// archi entranti
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");		
	
			if (toConnections != null && toConnections.size() > 1)
			{
				// creo OR
				orNode = new ElementInstance(elementInstance.getId() + "OR", ag.getElementType().getSubElement("NodeOR"), true);
				tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
				ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");
				FloatPropertyValue.setFloatProperty(orNode, "G_tx", tx);
				FloatPropertyValue.setFloatProperty(orNode, "G_ty", ty);
				FloatPropertyValue.setFloatProperty(elementInstance, "G_tx", tx+50);
				FloatPropertyValue.setFloatProperty(elementInstance, "G_ty", ty+50);

				tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_sx");
				ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_sy");
				FloatPropertyValue.setFloatProperty(orNode, "G_sx", tx);
				FloatPropertyValue.setFloatProperty(orNode, "G_sy", ty);

				tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_px");
				ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_py");
				FloatPropertyValue.setFloatProperty(orNode, "G_px", tx);
				FloatPropertyValue.setFloatProperty(orNode, "G_py", ty);

				tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_r");
				FloatPropertyValue.setFloatProperty(orNode, "G_r", tx);
			
				order = IntegerPropertyValue.getIntegerProperty(elementInstance, "G_order");
				IntegerPropertyValue.setIntegerProperty(orNode, "G_order", order);
			
				if (!orNode.setPropertyValue("G_inLayer", layer))
       					System.out.println("layer NON impostato in " + orNode.getId());

				if (!ag.addSubElement(orNode))
					System.out.println(orNode.getId() + " NON AGGIUNTO");
			
				//orNode = ag.getSubElement(orNode.getId());

				// archi entranti di elementInstance diventano archi entranti di orNode
				connectionType = (FixedArrayPropertyType)elementInstance.getElementType().getProperty("G_toConnections", true);
				newConnections = new FixedArrayPropertyValue(connectionType, toConnections.size());
				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					newConnections.setValue(i, new ElementRefPropertyValue(fap));
					ag.getSubElementByPath(fap).setPropertyValue("to", new ElementRefPropertyValue(orNode.getElementInstanceAbsolutePath()));
				}
				//if (
				orNode.setPropertyValue("G_toConnections", newConnections);
				//System.out.println(orNode.getId() + " G_toConnections OK");

				// arco da orNode a elementInstance
				orArc = new ElementInstance(elementInstance.getId() + "ORarc", ag.getElementType().getSubElement("Arc"), true);
				orArc.setPropertyValue("from", new ElementRefPropertyValue(orNode.getElementInstanceAbsolutePath()));
				orArc.setPropertyValue("to", new ElementRefPropertyValue(elementInstance.getElementInstanceAbsolutePath()));

				tx = FloatPropertyValue.getFloatProperty(orNode, "G_tx");
				ty = FloatPropertyValue.getFloatProperty(orNode, "G_ty");
				FloatPropertyValue.setFloatProperty(orArc, "G_tx", tx);
				FloatPropertyValue.setFloatProperty(orArc, "G_ty", ty);

				tx = FloatPropertyValue.getFloatProperty(orNode, "G_sx");
				ty = FloatPropertyValue.getFloatProperty(orNode, "G_sy");
				FloatPropertyValue.setFloatProperty(orArc, "G_sx", tx);
				FloatPropertyValue.setFloatProperty(orArc, "G_sy", ty);

				tx = FloatPropertyValue.getFloatProperty(orNode, "G_px");
				ty = FloatPropertyValue.getFloatProperty(orNode, "G_py");
				FloatPropertyValue.setFloatProperty(orArc, "G_px", tx);
				FloatPropertyValue.setFloatProperty(orArc, "G_py", ty);

				tx = FloatPropertyValue.getFloatProperty(orNode, "G_r");
				FloatPropertyValue.setFloatProperty(orArc, "G_r", tx);
			
				order = IntegerPropertyValue.getIntegerProperty(orNode, "order");
				IntegerPropertyValue.setIntegerProperty(orArc, "G_order", order+1);

				segmentType = (FixedArrayPropertyType)orArc.getElementType().getProperty("G_segmentsP1", true);
    	  			segmentValues = new FixedArrayPropertyValue(segmentType, 1); 
    	  			fapType = (StructPropertyType)(segmentType.getSubPropertyType());

				x1 = FloatPropertyValue.getFloatProperty(orNode, "G_tx");
				y1 = FloatPropertyValue.getFloatProperty(orNode, "G_ty");
				x2 = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
				y2 = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");

				// segmento 0
				fapValue0 = new StructPropertyValue(fapType);

				fapValue0.setPropertyValue("x1", new FloatPropertyValue(x1));
    	  			fapValue0.setPropertyValue("ctrlx1", new FloatPropertyValue(x1));
    	  			fapValue0.setPropertyValue("y1", new FloatPropertyValue(y1));
    	  			fapValue0.setPropertyValue("ctrly1", new FloatPropertyValue(y1));

    	  			fapValue0.setPropertyValue("x2", new FloatPropertyValue(x2));
    	  			fapValue0.setPropertyValue("ctrlx2", new FloatPropertyValue(x2));
    	  			fapValue0.setPropertyValue("y2", new FloatPropertyValue(y2));
    	  			fapValue0.setPropertyValue("ctrly2", new FloatPropertyValue(y2));

				segmentValues.setValue(0, fapValue0);

				orArc.setPropertyValue("G_segmentsP1", segmentValues);

				if (!orArc.setPropertyValue("G_inLayer", layer))
       					System.out.println("layer NON impostato in " + orArc.getId());

				if (!ag.addSubElement(orArc))
					System.out.println(orArc.getId() + " NON AGGIUNTO");

				//orArc = ag.getSubElement(orArc.getId());

				// aggiungo orArc agli archi entranti di elementInstance
				newConnections = new FixedArrayPropertyValue(connectionType, 1);
				newConnections.setValue(0, new ElementRefPropertyValue(orArc.getElementInstanceAbsolutePath()));
				elementInstance.setPropertyValue("G_toConnections", newConnections);

				// aggiungo orArc agli archi uscenti di orNode
				connectionType = (FixedArrayPropertyType)elementInstance.getElementType().getProperty("G_fromConnections", true); 
				newConnections = new FixedArrayPropertyValue(connectionType, 1);
				newConnections.setValue(0, new ElementRefPropertyValue(orArc.getElementInstanceAbsolutePath()));
				//if (! 
				orNode.setPropertyValue("G_fromConnections", newConnections);
				//System.out.println(orNode.getId() + " G_fromConnections FALLITO");

				// aggiorno la posizione degli archi uscenti da ElementInstance
				tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
				ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");
				fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
				for (i=0; fromConnections != null && i<fromConnections.size(); i++)
				{
					fap = fromConnections.getPropertyValueAt(i).toString();
					arc = ag.getSubElementByPath(fap);
					to = ag.getSubElementByPath(arc.getPropertyValue("to").toString());
					x1 = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
					y1 = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");
					x2 = FloatPropertyValue.getFloatProperty(to, "G_tx");
					y2 = FloatPropertyValue.getFloatProperty(to, "G_ty");					

					segmentValues = new FixedArrayPropertyValue(segmentType, 1); 
    	  			
					// segmento 0
					fapValue0 = new StructPropertyValue(fapType);

					fapValue0.setPropertyValue("x1", new FloatPropertyValue(x1));
    	  				fapValue0.setPropertyValue("ctrlx1", new FloatPropertyValue(x1));
    	  				fapValue0.setPropertyValue("y1", new FloatPropertyValue(y1));
    	  				fapValue0.setPropertyValue("ctrly1", new FloatPropertyValue(y1));

	    	  			fapValue0.setPropertyValue("x2", new FloatPropertyValue(x2));
    		  			fapValue0.setPropertyValue("ctrlx2", new FloatPropertyValue(x2));
    		  			fapValue0.setPropertyValue("y2", new FloatPropertyValue(y2));
    		  			fapValue0.setPropertyValue("ctrly2", new FloatPropertyValue(y2));

					segmentValues.setValue(0, fapValue0);

					arc.setPropertyValue("G_segmentsP1", segmentValues);
				}		
			}
		}
	}
  }

  private void agMerge(ElementInstance nodeShared)
  {
	// DATO UN NODO CONDIVISO, RICERCA E UNISCE I NODI CORRISPONDENTI NELL'AG GENERALE
	int i; // j;
	boolean merged = false;
	ElementInstance sourceAsset, sourceNode, destNode, arc;
	FixedArrayPropertyValue toConnections;

	String nodeSharedName[] = nodeShared.getId().split("_");	
	String port = nodeShared.getPropertyValue("asset").toString();

	//System.out.println("path di " + pathType.length + " step");

	System.out.println("\nConsidero NodeShared " + nodeShared.getId());

	// CERCA GLI ASSET RAGGIUNGIBILI NELL'ARCH IN BASE AL TIPO DI PORTA
	sourceAsset = arch.getSubElement(nodeSharedName[0]); 	
	System.out.println("Cerco gli asset raggiungibili da " + sourceAsset.getId() + " con porta " + port);
	visitedAssets.clear();
	reachableAssets.clear();
	archVisit4(sourceAsset, port);


	// CONTROLLA CHE IL NODO CONDIVISO ABBIA UN ARCO ENTRANTE E INDIVIDUA IL NODO FROM
	toConnections = (FixedArrayPropertyValue)nodeShared.getPropertyValue("G_toConnections");
	/*
	if (toConnections == null || toConnections.size() == 0)
	{
		//System.out.println("NodeShared " + nodeShared.getId() + " non connesso");
		JOptionPane.showMessageDialog(null, "Shared node " + nodeSharedName[1] + " isolated in asset " + nodeSharedName[0]);

		// return false;
	}
	else
	{
		for (j=0; j < toConnections.size(); j++)
		{
	*/
	if (toConnections != null && toConnections.size() > 0)	
	{
		arc = ag.getSubElementByPath(toConnections.getPropertyValueAt(0).toString());
		sourceNode = ag.getSubElementByPath(arc.getPropertyValue("from").toString());
	
		// UNISCE I NODI NELL'AG GENERALE
		for (i=0; i < reachableAssets.size(); i++)
		{
			destNode = ag.getSubElement(reachableAssets.get(i) + "_" + nodeSharedName[1]);
			if (destNode != null && destNode.getElementType().getId().equals("Node"))
			{
				System.out.println(reachableAssets.get(i) + " contiene " + nodeSharedName[1]);
				arcClone(arc, sourceNode, destNode, i);
				merged = true;
			}
		}
		// RIMUOVE IL NODO CONDIVISO E IL RELATIVO ARCO
		System.out.println("rimuovo l'arco " + arc.getId());
		deleteArc(arc);
		
	}
	System.out.println("rimuovo NodeShared " + nodeShared.getId());
	ag.removeSubElement(nodeShared.getId());

	if (! merged)
		JOptionPane.showMessageDialog(null, "Shared node " + nodeSharedName[1] + " in asset " + nodeSharedName[0] + " has not been connected.");

	//return merged;
  }

  private void archVisit4(ElementInstance asset, String port)
  {
	// VISITA RICORSIVA DELL'ARCH SEGUENDO IL TIPO DI PATH.
	// GLI ASSET RAGGIUNTI SONO MEMORIZZATI IN reachableAssets.

	ElementInstance arc;
	FixedArrayPropertyValue fromConnections, toConnections;
	int i;
	String fap, toRef, fromRef;

	if (visitedAssets.contains(asset.getId()))
		return;

	if (asset.getElementType().getId().equals("Attacker") || asset.getElementType().getId().equals("Goal"))
		return;
	
	System.out.println("Visito " + asset.getId());
	visitedAssets.add(asset.getId());
	reachableAssets.add(asset.getId());
	
	fromConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_fromConnections");
	toConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_toConnections");

	for (i=0; fromConnections!=null && i<fromConnections.size(); i++)
	{
		fap = fromConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		arc = arch.getSubElementByPath(fap);
		toRef = arc.getPropertyValue("to").toString();
		//System.out.println("nodo puntato: " + toRef);
		this.archVisit4(arch.getSubElementByPath(toRef), port);
	}
	for (i=0; toConnections!=null && i<toConnections.size(); i++)
	{
		fap = toConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		arc = arch.getSubElementByPath(fap);
		fromRef = arc.getPropertyValue("from").toString();
		//System.out.println("nodo puntato: " + toRef);
		this.archVisit4(arch.getSubElementByPath(fromRef), port);
	}
  }
  

  /*
  private void archVisit3(ElementInstance asset, String pathType[], int step)
  {
	// VISITA RICORSIVA DELL'ARCH SEGUENDO IL TIPO DI PATH.
	// GLI ASSET RAGGIUNTI SONO MEMORIZZATI IN reachableAssets.

	ElementInstance arc;
	FixedArrayPropertyValue fromConnections, toConnections;
	int i;
	String fap, toRef, fromRef;

	if (visitedAssets.contains(asset.getId()))
		return;

	if (asset.getElementType().getId().equals("Attacker") || asset.getElementType().getId().equals("Goal"))
		return;
	
	System.out.println("Visito " + asset.getId());
	visitedAssets.add(asset.getId());
	
	if (step == pathType.length)
	{
		System.out.println("\tTrovato " + asset.getId());
		reachableAssets.add(asset.getId());
		return;
	}
	
	fromConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_fromConnections");
	toConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_toConnections");

	for (i=0; fromConnections!=null && i<fromConnections.size(); i++)
	{
		fap = fromConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		arc = arch.getSubElementByPath(fap);
		toRef = arc.getPropertyValue("to").toString();
		//System.out.println("nodo puntato: " + toRef);
		if (arc.getId().toLowerCase().contains(pathType[step]))
			this.archVisit3(arch.getSubElementByPath(toRef), pathType, step+1);
	}
	for (i=0; toConnections!=null && i<toConnections.size(); i++)
	{
		fap = toConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		arc = arch.getSubElementByPath(fap);
		fromRef = arc.getPropertyValue("from").toString();
		//System.out.println("nodo puntato: " + toRef);
		if (arc.getId().toLowerCase().contains(pathType[step]))
			this.archVisit3(arch.getSubElementByPath(fromRef), pathType, step+1);
	}
  } 
  */
  
  private void arcClone(ElementInstance arc, ElementInstance sourceNode, ElementInstance destNode, int indice)
  {
	// CREA UN ARCO CHE VA DA UN NODO (sourceNode) AL NODO CORRISPONDENTE AD UN NODO CONDIVISO (destNode)
	int i;
	double x1, y1, x2, y2;
	String cloneId;
	ElementInstance clone;
	FixedArrayPropertyValue segments, segmentsNew;
	FixedArrayPropertyType segmentType;
	StructPropertyType structType;
	StructPropertyValue xy, xyNew;
	FixedArrayPropertyValue connections, newConnections;
	FixedArrayPropertyType connectionType;

	System.out.println("clono l'arco " + arc.getId());

	clone = (ElementInstance)arc.clone();
	cloneId = arc.getId() + "clone" + String.valueOf(indice);
	clone.setId(cloneId);
	
	//clone.setPropertyValue("from", new ElementRefPropertyValue(sourceNode));
	clone.setPropertyValue("to", new ElementRefPropertyValue(destNode));

	segments = (FixedArrayPropertyValue)arc.getPropertyValue("G_segmentsP1");
	xy = (StructPropertyValue)segments.getPropertyValueAt(0);

	x1 = Double.parseDouble(xy.getPropertyValueAt("x1").toString());
	y1 = Double.parseDouble(xy.getPropertyValueAt("y1").toString());
	x2 = FloatPropertyValue.getFloatProperty(destNode, "G_tx");
	y2 = FloatPropertyValue.getFloatProperty(destNode, "G_ty");
	//System.out.println(destNode.getId() + ": " + x2 + " " + y2); 

	segmentType = (FixedArrayPropertyType)arc.getElementType().getProperty("G_segmentsP1", true);
    	structType = (StructPropertyType)(segmentType.getSubPropertyType());
    	xyNew = new StructPropertyValue(structType);
	segmentsNew = new FixedArrayPropertyValue(segmentType, 1);

	if (!xyNew.setPropertyValue("x1", new FloatPropertyValue(x1)))
		System.out.println("x1 non impostata");
	xyNew.setPropertyValue("x2", new FloatPropertyValue(x2));
	xyNew.setPropertyValue("y1", new FloatPropertyValue(y1));
	xyNew.setPropertyValue("y2", new FloatPropertyValue(y2));
	xyNew.setPropertyValue("ctrlx1", new FloatPropertyValue(x1));
	xyNew.setPropertyValue("ctrlx2", new FloatPropertyValue(x2));
	xyNew.setPropertyValue("ctrly1", new FloatPropertyValue(y1));
	xyNew.setPropertyValue("ctrly2", new FloatPropertyValue(y2));
	
	if (!segmentsNew.setValue(0, xyNew))
		System.out.println("segments non aggiornati");
	if (!clone.setPropertyValue("G_segmentsP1", segmentsNew))
		System.out.println("segments non assegnati");

	if (!ag.addSubElement(clone))
		System.out.println(clone.getId() + " NON AGGIUNTO");

	// AGGIUNGE L'ARCO AGLI ARCHI USCENTI DA sourceNode.
	connections = (FixedArrayPropertyValue)sourceNode.getPropertyValue("G_fromConnections");
	if (connections != null && connections.size() > 0)
	{
		connectionType = (FixedArrayPropertyType)sourceNode.getElementType().getProperty("G_fromConnections", true);	
		newConnections = new FixedArrayPropertyValue(connectionType, connections.size()+1);
		for (i=0; i<connections.size(); i++)
			newConnections.setValue(i, connections.getPropertyValueAt(i));
		newConnections.setValue(i, new ElementRefPropertyValue(clone.getElementInstanceAbsolutePath()));
		sourceNode.setPropertyValue("G_fromConnections", newConnections);
	}

	// AGGIUNGE L'ARCO AGLI ARCHI ENTRANTI IN destNode.
	connections = (FixedArrayPropertyValue)destNode.getPropertyValue("G_toConnections");
	connectionType = (FixedArrayPropertyType)destNode.getElementType().getProperty("G_toConnections", true);
	if (connections != null)	
		newConnections = new FixedArrayPropertyValue(connectionType, connections.size()+1);		
	else
		newConnections = new FixedArrayPropertyValue(connectionType, 1);
	i=0;
	if (connections != null && connections.size() > 0)
		for (i=0; i<connections.size(); i++)
			newConnections.setValue(i, connections.getPropertyValueAt(i));
	newConnections.setValue(i, new ElementRefPropertyValue(clone.getElementInstanceAbsolutePath()));
	destNode.setPropertyValue("G_toConnections", newConnections);
  }

  private int agVisit(ElementInstance node)
  {
	// VISITA RICORSIVAMENTE L'AG GENERALE.
	// MEMORIZZA IN NodesIn I NODI CHE PORTANO ALLA TECNICA FINALE.

	FixedArrayPropertyValue fromConnections;
	ElementInstance arc, to;
	int i, score;
	String fap, toRef;

	/*enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();
	
		System.out.println("agVisit " + elementInstance.getId() + ": " + elementType.getId());
 	}*/

	if (visitedNodes.contains(node.getId()))
		return 0;
	else
		visitedNodes.add(node.getId());
	
	System.out.println("Visito " + node.getId());

	if (node.getId().equals(goal.getId()))
	{
		nodesIn.add(node.getId());
		//System.out.println(node.getId() + " nel path.");
		return 1;
	}
	
	fromConnections = (FixedArrayPropertyValue)node.getPropertyValue("G_fromConnections");
	if (fromConnections == null || fromConnections.size() == 0)
	{
		//nodesOut.add(node.getId());
		//System.out.println(node.getId() + " fuori path.");
		return 0;
	}

	score=0;
	for (i=0; i<fromConnections.size(); i++)
	{
		fap = fromConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		toRef = ag.getSubElementByPath(fap).getPropertyValue("to").toString();
		//System.out.println("nodo puntato: " + toRef);
		//System.out.println("Passo a " + ag.getSubElementByPath(toRef).getId());
		score += this.agVisit(ag.getSubElementByPath(toRef));
	}
	if (score > 0)
	{
		nodesIn.add(node.getId());
		//System.out.println(node.getId() + " nel path.");
	}
	/*else
	{
		nodesOut.add(node.getId());
		//System.out.println(node.getId() + " fuori path.");
	}*/

	return score;
  }

  private void agVisitDefense()
  {
	// AGGIUNGE A NodesIn LE DIFESE E LE ANALITICHE COLLEGATE A NODI PRESENTI IN NodesIn 
	// PERCHE' NON DEVONO ESSERE CANCELLATE DURANTE LA POTATURA DEL GRAFO.

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, arc, from, to;
  	ElementType elementType;
	FixedArrayPropertyValue fromConnections, toConnections;
	
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getId().equals("NodeDefense"))
		{
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			arc = ag.getSubElementByPath(fromConnections.getPropertyValueAt(0).toString());
			to = ag.getSubElementByPath(arc.getPropertyValue("to").toString());
			if (nodesIn.contains(to.getId()))
				nodesIn.add(elementInstance.getId());
		}

		if (elementType.getId().equals("Analytics"))
		{
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
			arc = ag.getSubElementByPath(toConnections.getPropertyValueAt(0).toString());
			from = ag.getSubElementByPath(arc.getPropertyValue("from").toString());
			if (nodesIn.contains(from.getId()))
				nodesIn.add(elementInstance.getId());
		}		
	}
  }

  private void deleteNodes()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, arc;
  	ElementType elementType;
	FixedArrayPropertyValue fromConnections, toConnections;
	int i;
	String fap;

	// CANCELLA I NODI E I RELATIVI ARCHI CHE NON SONO SUL PATH TRA TECNICA INIZIALE E FINALE.

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null && !nodesIn.contains(elementInstance.getId()))
		{
			System.out.println("cancello il nodo " + elementInstance.getId());
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections != null)
				for (i=0; i<fromConnections.size(); i++)
				{
					fap = fromConnections.getPropertyValueAt(i).toString();
					arc = ag.getSubElementByPath(fap);
					this.deleteArc(arc);
					//ag.removeSubElement(arc.getId());
					System.out.println("ho rimosso l'arco " + arc.getId());
				}

			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
			if (toConnections != null)
				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					arc = ag.getSubElementByPath(fap);
					this.deleteArc(arc);
					//ag.removeSubElement(arc.getId());
					System.out.println("ho rimosso l'arco " + arc.getId());
				}

			ag.removeSubElement(elementInstance.getId());
			enumeration = ag.subElementsEnum();
		}		
	}
  }

  private void deleteArc(ElementInstance arc)
  {
	// RIMUOVE L'ARCO DALLE CONNESSIONI DEI NODI FROM E TO, NELL'AG GENERALE.
	// RIMUOVE L'ARCO DALL'AG GENERALE.

	ElementRefPropertyValue fromRef, toRef;
	ElementInstance from, to;
	FixedArrayPropertyValue connections, newConnections;
	FixedArrayPropertyType connectionType;
	int i, j;

	//System.out.println("arc: " + arc.getId());

	fromRef = (ElementRefPropertyValue)arc.getPropertyValue("from");
	toRef = (ElementRefPropertyValue)arc.getPropertyValue("to");
	from = ag.getSubElementByPath(fromRef.toString());
	to = ag.getSubElementByPath(toRef.toString());

	//System.out.println("delete " + from.getId() + " " + to.getId());
	
	connections = (FixedArrayPropertyValue)from.getPropertyValue("G_fromConnections");
	if (connections != null && connections.size() > 0)
	{
		connectionType = (FixedArrayPropertyType)from.getElementType().getProperty("G_fromConnections", true);	
		newConnections = new FixedArrayPropertyValue(connectionType, connections.size()-1);
		j=0;
		for (i=0; i<connections.size(); i++)
			if (!connections.getPropertyValueAt(i).toString().equals(arc.getElementInstanceAbsolutePath()))
			{
				newConnections.setValue(j, connections.getPropertyValueAt(i));
				j++;
			}
		from.setPropertyValue("G_fromConnections", newConnections);
	}

	connections = (FixedArrayPropertyValue)to.getPropertyValue("G_toConnections");
	if (connections != null && connections.size() > 0)
	{
		connectionType = (FixedArrayPropertyType)to.getElementType().getProperty("G_toConnections", true);
		newConnections = new FixedArrayPropertyValue(connectionType, connections.size()-1);
		j=0;
		for (i=0; i<connections.size(); i++)
			if (!connections.getPropertyValueAt(i).toString().equals(arc.getElementInstanceAbsolutePath()))
			{
				newConnections.setValue(j, connections.getPropertyValueAt(i));
				j++;
			}
		to.setPropertyValue("G_toConnections", newConnections);
	}

	ag.removeSubElement(arc.getId());
  }

  private void dbnNodeBuild()
  {
	// CREA I NODI DELLA DBN SEGUENDO I NODI DELL'AG GENERALE

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, node;
  	ElementType elementType;
	ElementRefPropertyValue layer;
	FixedArrayPropertyValue fromConnections, toConnections, newConnections;
	FixedArrayPropertyType connectionType;
	double tx, ty;
	int order, i;
	String fap, prob;

	layer = new ElementRefPropertyValue(dbn.getSubElement("G_Layer1"));

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null)
		{
			if (elementType.getId().equals("NodeOR"))
				node = new ElementInstance(elementInstance.getId(), dbn.getElementType().getSubElement("NodeOR"), true);
			else
				if (elementType.getId().equals("NodeAND"))
					node = new ElementInstance(elementInstance.getId(), dbn.getElementType().getSubElement("NodeAND"), true);
				else // tutti gli altri tipi di nodo (tecnica, difesa, analitica)
					node = new ElementInstance(elementInstance.getId(), dbn.getElementType().getSubElement("Node"), true);
					
			if (elementType.getId().equals("Node")) // nodo-tecnica
			{
				//prob = FloatPropertyValue.getFloatProperty(elementInstance, "prob");
				prob = elementInstance.getPropertyValue("prob").toString();
				StringPropertyValue.setStringProperty(node, "CPT", prob);
			}
			
			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");
			FloatPropertyValue.setFloatProperty(node, "G_tx", tx);
			FloatPropertyValue.setFloatProperty(node, "G_ty", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_sx");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_sy");
			FloatPropertyValue.setFloatProperty(node, "G_sx", tx);
			FloatPropertyValue.setFloatProperty(node, "G_sy", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_px");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_py");
			FloatPropertyValue.setFloatProperty(node, "G_px", tx);
			FloatPropertyValue.setFloatProperty(node, "G_py", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_r");
			FloatPropertyValue.setFloatProperty(node, "G_r", tx);
			
			order = IntegerPropertyValue.getIntegerProperty(elementInstance, "G_order");
			IntegerPropertyValue.setIntegerProperty(node, "G_order", order);
			//System.out.println("\norder: " + order);
			if (order > maxOrder)
				maxOrder = order;

			// ARCHI USCENTI
			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections != null)
			{
				connectionType = (FixedArrayPropertyType)elementInstance.getElementType().getProperty("G_fromConnections", true);
				newConnections = new FixedArrayPropertyValue(connectionType, fromConnections.size());

				for (i=0; i<fromConnections.size(); i++)
				{
					fap = fromConnections.getPropertyValueAt(i).toString();
					//System.out.println("\nfap: " + fap);
					//fap = fap.replace(".AG.", ".DBN.");
					fap = fap.replace(ag.getId(), dbn.getId());
					//System.out.println("fap: " + fap);
					newConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				node.setPropertyValue("G_fromConnections", newConnections);
			}

			// ARCHI ENTRANTI
			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
			if (toConnections != null)
			{
				connectionType = (FixedArrayPropertyType)elementInstance.getElementType().getProperty("G_toConnections", true);
				newConnections = new FixedArrayPropertyValue(connectionType, toConnections.size());

				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					//System.out.println("\nfap: " + fap);
					//fap = fap.replace(".AG.", ".DBN.");
					fap = fap.replace(ag.getId(), dbn.getId());
					//System.out.println("fap: " + fap);
					newConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				node.setPropertyValue("G_toConnections", newConnections);
			}

			if (!node.setPropertyValue("G_inLayer", layer))
       				System.out.println("layer NON impostato in " + node.getId());
			//else
				//System.out.println("layer impostato in " + node.getId());

			if (!dbn.addSubElement(node))
				System.out.println(node.getId() + " NON AGGIUNTO");
			//else
				//System.out.println(node.getId() + " AGGIUNTO");
		}
	}
  }

  private void dbnArcBuild()
  {
	// CREA GLI ARCHI DELLA DBN SEGUENDO GLI ARCHI DELL'AG GENERALE

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, arc;
  	ElementType elementType;
	ElementRefPropertyValue layer, fromRef, toRef;
	FixedArrayPropertyValue segments;
	double tx, ty;
	int order;
	String from, to;

	layer = new ElementRefPropertyValue(dbn.getSubElement("G_Layer1"));

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGArc") != null)
		{
			arc = new ElementInstance(elementInstance.getId(), dbn.getElementType().getSubElement("Arc"), true);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_tx");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_ty");
			FloatPropertyValue.setFloatProperty(arc, "G_tx", tx);
			FloatPropertyValue.setFloatProperty(arc, "G_ty", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_sx");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_sy");
			FloatPropertyValue.setFloatProperty(arc, "G_sx", tx);
			FloatPropertyValue.setFloatProperty(arc, "G_sy", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_px");
			ty = FloatPropertyValue.getFloatProperty(elementInstance, "G_py");
			FloatPropertyValue.setFloatProperty(arc, "G_px", tx);
			FloatPropertyValue.setFloatProperty(arc, "G_py", ty);

			tx = FloatPropertyValue.getFloatProperty(elementInstance, "G_r");
			FloatPropertyValue.setFloatProperty(arc, "G_r", tx);
			
			order = IntegerPropertyValue.getIntegerProperty(elementInstance, "G_order");
			IntegerPropertyValue.setIntegerProperty(arc, "G_order", order);
			//System.out.println("\norder: " + order);
			if (order > maxOrder)
				maxOrder = order;
			
			//System.out.println("\narc: " + elementInstance.getId());
			fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
			from = fromRef.getElementReferenceName();
			//System.out.println("from: " + from);
			//from = from.replace(".AG.", ".DBN.");
			from = from.replace(ag.getId(), dbn.getId());
			//System.out.println("from: " + from);
			arc.setPropertyValue("from", new ElementRefPropertyValue(from));

			toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
			to = toRef.getElementReferenceName();
			//System.out.println("to: " + to);
			//to = to.replace(".AG.", ".DBN.");
			to = to.replace(ag.getId(), dbn.getId());
			//System.out.println("to: " + to);
			arc.setPropertyValue("to", new ElementRefPropertyValue(to));

			segments = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_segmentsP1");
			arc.setPropertyValue("G_segmentsP1", segments);

			if (!arc.setPropertyValue("G_inLayer", layer))
       				System.out.println("layer NON impostato in " + arc.getId());
			//else
				//System.out.println("layer impostato in " + node.getId());

			if (!dbn.addSubElement(arc))
				System.out.println(arc.getId() + " NON AGGIUNTO");
			//else
				//System.out.println(node.getId() + " AGGIUNTO");
		}
	}
  }

  private void dbnTemporalArcBuild()
  {
	// AGGIUNGE I NODI TEMPORALI NELLA DBN, PER I NODI RELATIVI ALLE TECNICHE.

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, node;
  	ElementType elementType;
	ElementRefPropertyValue layer;
	FixedArrayPropertyValue fromConnections, toConnections, newConnections;
	FixedArrayPropertyType connectionType;

	double tx, ty;
	final double offset = 48.0;
	final double size = 12.0;
	int i, order, contaTemp;
	String fap;

	ElementInstance arc;
	FixedArrayPropertyValue segments;
	//String arcId, nodePath, arcPath;
	FixedArrayPropertyType segmentType;
	FixedArrayPropertyValue segmentValues;
	StructPropertyType fapType;
	StructPropertyValue fapValue0, fapValue1, fapValue2, fapValue3;

	layer = new ElementRefPropertyValue(dbn.getSubElement("G_Layer1"));
	contaTemp=1;

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null)
		{
			if (elementType.getId().equals("Node")) // tecnica
			{
				node = dbn.getSubElement(elementInstance.getId());
				//if (node != null)
				//System.out.println("trovato " + node.getId());
				/*else
				{
					System.out.println("non trovato " + elementInstance.getId());
					return;
				}*/
	
				arc = new ElementInstance("TemporalArc" + contaTemp++, dbn.getElementType().getSubElement("TemporalArc"), true);

				tx = FloatPropertyValue.getFloatProperty(node, "G_tx");
				ty = FloatPropertyValue.getFloatProperty(node, "G_ty");
				FloatPropertyValue.setFloatProperty(arc, "G_tx", tx);
				FloatPropertyValue.setFloatProperty(arc, "G_ty", ty);

				tx = FloatPropertyValue.getFloatProperty(node, "G_sx");
				ty = FloatPropertyValue.getFloatProperty(node, "G_sy");
				FloatPropertyValue.setFloatProperty(arc, "G_sx", tx);
				FloatPropertyValue.setFloatProperty(arc, "G_sy", ty);

				tx = FloatPropertyValue.getFloatProperty(node, "G_px");
				ty = FloatPropertyValue.getFloatProperty(node, "G_py");
				FloatPropertyValue.setFloatProperty(arc, "G_px", tx);
				FloatPropertyValue.setFloatProperty(arc, "G_py", ty);

				tx = FloatPropertyValue.getFloatProperty(node, "G_r");
				FloatPropertyValue.setFloatProperty(arc, "G_r", tx);
			
				IntegerPropertyValue.setIntegerProperty(arc, "G_order", ++maxOrder);
				//System.out.println("\norder: " + order);
		
				//System.out.println("from/to: " + node.getElementInstanceAbsolutePath());
				arc.setPropertyValue("from", new ElementRefPropertyValue(node.getElementInstanceAbsolutePath()));
				arc.setPropertyValue("to", new ElementRefPropertyValue(node.getElementInstanceAbsolutePath()));

				segmentType = (FixedArrayPropertyType)arc.getElementType().getProperty("G_segmentsP1", true);
    	  			segmentValues = new FixedArrayPropertyValue(segmentType, 4); 
    	  			fapType = (StructPropertyType)(segmentType.getSubPropertyType());

				tx = FloatPropertyValue.getFloatProperty(node, "G_tx") - size;
				ty = FloatPropertyValue.getFloatProperty(node, "G_ty");

				// segmento 0
				fapValue0 = new StructPropertyValue(fapType);

				fapValue0.setPropertyValue("x1", new FloatPropertyValue(tx));
    	  			fapValue0.setPropertyValue("ctrlx1", new FloatPropertyValue(tx));
    	  			fapValue0.setPropertyValue("y1", new FloatPropertyValue(ty));
    	  			fapValue0.setPropertyValue("ctrly1", new FloatPropertyValue(ty));

    	  			fapValue0.setPropertyValue("x2", new FloatPropertyValue(tx-offset));
    	  			fapValue0.setPropertyValue("ctrlx2", new FloatPropertyValue(tx-offset));
    	  			fapValue0.setPropertyValue("y2", new FloatPropertyValue(ty));
    	  			fapValue0.setPropertyValue("ctrly2", new FloatPropertyValue(ty));

				segmentValues.setValue(0, fapValue0);

				// segmento 1
				fapValue1 = new StructPropertyValue(fapType);

				fapValue1.setPropertyValue("x1", new FloatPropertyValue(tx-offset));
    	  			fapValue1.setPropertyValue("ctrlx1", new FloatPropertyValue(tx-offset));
    	  			fapValue1.setPropertyValue("y1", new FloatPropertyValue(ty));
    	  			fapValue1.setPropertyValue("ctrly1", new FloatPropertyValue(ty));

    	  			fapValue1.setPropertyValue("x2", new FloatPropertyValue(tx-offset));
    	  			fapValue1.setPropertyValue("ctrlx2", new FloatPropertyValue(tx-offset));
    	  			fapValue1.setPropertyValue("y2", new FloatPropertyValue(ty+offset));
    	  			fapValue1.setPropertyValue("ctrly2", new FloatPropertyValue(ty+offset));

				segmentValues.setValue(1, fapValue1);

				// segmento 2
				fapValue2 = new StructPropertyValue(fapType);

				fapValue2.setPropertyValue("x1", new FloatPropertyValue(tx-offset));
    	  			fapValue2.setPropertyValue("ctrlx1", new FloatPropertyValue(tx-offset));
    	  			fapValue2.setPropertyValue("y1", new FloatPropertyValue(ty+offset));
    	  			fapValue2.setPropertyValue("ctrly1", new FloatPropertyValue(ty+offset));

    	  			fapValue2.setPropertyValue("x2", new FloatPropertyValue(tx));
    	  			fapValue2.setPropertyValue("ctrlx2", new FloatPropertyValue(tx));
    	  			fapValue2.setPropertyValue("y2", new FloatPropertyValue(ty+offset));
    	  			fapValue2.setPropertyValue("ctrly2", new FloatPropertyValue(ty+offset));

				segmentValues.setValue(2, fapValue2);

				// segmento 3
				fapValue3 = new StructPropertyValue(fapType);

				fapValue3.setPropertyValue("x1", new FloatPropertyValue(tx));
    	  			fapValue3.setPropertyValue("ctrlx1", new FloatPropertyValue(tx));
    	  			fapValue3.setPropertyValue("y1", new FloatPropertyValue(ty+offset));
    	  			fapValue3.setPropertyValue("ctrly1", new FloatPropertyValue(ty+offset));

    	  			fapValue3.setPropertyValue("x2", new FloatPropertyValue(tx+size));
    	  			fapValue3.setPropertyValue("ctrlx2", new FloatPropertyValue(tx+size));
    	  			fapValue3.setPropertyValue("y2", new FloatPropertyValue(ty+size));
    	  			fapValue3.setPropertyValue("ctrly2", new FloatPropertyValue(ty+size));

				segmentValues.setValue(3, fapValue3);

				arc.setPropertyValue("G_segmentsP1", segmentValues);

				if (!arc.setPropertyValue("G_inLayer", layer))
       					System.out.println("layer NON impostato in " + arc.getId());
				//else
					//System.out.println("layer impostato in " + node.getId());

				if (!dbn.addSubElement(arc))
					System.out.println(arc.getId() + " NON AGGIUNTO");
				//else
					//System.out.println(node.getId() + " AGGIUNTO");

				fromConnections = (FixedArrayPropertyValue)node.getPropertyValue("G_fromConnections");
				if (fromConnections != null)
				{
					connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_fromConnections", true);
					newConnections = new FixedArrayPropertyValue(connectionType, fromConnections.size()+1);

					for (i=0; i<fromConnections.size(); i++)
					{
						fap = fromConnections.getPropertyValueAt(i).toString();
						//System.out.println("fap: " + fap);
						newConnections.setValue(i, new ElementRefPropertyValue(fap));
					}
					newConnections.setValue(i, new ElementRefPropertyValue(arc.getElementInstanceAbsolutePath()));

					node.setPropertyValue("G_fromConnections", newConnections);
				}

				toConnections = (FixedArrayPropertyValue)node.getPropertyValue("G_toConnections");
				if (toConnections != null)
				{
					connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_toConnections", true);
					newConnections = new FixedArrayPropertyValue(connectionType, toConnections.size()+1);

					for (i=0; i<toConnections.size(); i++)
					{
						fap = toConnections.getPropertyValueAt(i).toString();
						//System.out.println("\nfap: " + fap);
						newConnections.setValue(i, new ElementRefPropertyValue(fap));
					}	
					newConnections.setValue(i, new ElementRefPropertyValue(arc.getElementInstanceAbsolutePath()));

					node.setPropertyValue("G_toConnections", newConnections);
				}

				if (fromConnections == null)
				{
					connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_fromConnections", true);
					newConnections = new FixedArrayPropertyValue(connectionType, 1);
					newConnections.setValue(0, new ElementRefPropertyValue(arc.getElementInstanceAbsolutePath()));
					node.setPropertyValue("G_fromConnections", newConnections);
				}

				if (toConnections == null)
				{
					connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_toConnections", true);
					newConnections = new FixedArrayPropertyValue(connectionType, 1);
					newConnections.setValue(0, new ElementRefPropertyValue(arc.getElementInstanceAbsolutePath()));
					node.setPropertyValue("G_toConnections", newConnections);
				}
			}
		}
	}
  }

  private void dbnParentBuild()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, node, arc;
  	ElementType elementType;
	FixedArrayPropertyValue toConnections;
	ElementRefPropertyValue fromRef;
	String fap, fromNode, parents;
	int i;

	enumeration = dbn.subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.DBN.DBNNode") != null)
		{
			//System.out.println("\nnode: " + elementInstance.getId());
			parents = "";

			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
			if (toConnections != null)
			{
				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					//System.out.println("\nfap: " + fap);
					arc = model_.getSubElementByPath(fap);
					if (arc.getElementType().getId().equals("Arc"))
					{
						//System.out.print("incoming arc: " + arc.getId());
						fromNode = arc.getPropertyValue("from").toString();
						//System.out.print(" from " + fromNode);
						node = dbn.getSubElement(fromNode);
						if (parents.length() == 0)
							parents = node.getId();
						else
							parents = parents + ";" + node.getId();
					}
				}

				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					//System.out.println("\nfap: " + fap);
					arc = model_.getSubElementByPath(fap);
					if (arc.getElementType().getId().equals("TemporalArc"))
					{
						//System.out.print("incoming arc: " + arc.getId());
						fromNode = arc.getPropertyValue("from").toString();
						//System.out.print(" from " + fromNode);
						node = dbn.getSubElement(fromNode);
						if (parents.length() == 0)
							parents = node.getId();
						else
							parents = parents + ";" + node.getId();
					}
				}

				elementInstance.setPropertyValue("parent", new StringPropertyValue(parents));
			}
		}
	}
  }

  private void save() 
  {
	// SALVA, CHIUDE E RIAPRE IL MODELLO

	DrawNETGraphFrame frame1, frame2;
	XMLModelFilter xml, xmmf; 
	String fileName, title;
	File chosenFile;
	MXFile mxFile;
	Model model;

	// SALVA
	//fileName="models/_files/RSE1/" + this.generateBaseFilename(model_) + ".mdl";
	fileName = "models/_files/temp/temp.mdl";

 	xml = new XMLModelFilter();
	xml.setFilename(fileName);
      	xml.setFilenameAssoc("mdl", fileName);
      	xml.write(model_);

      	System.out.println("modello salvato nel file "+ fileName);

	try
	{
		// CHIUDI
		frame1 = (DrawNETGraphFrame)DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedFrame();
		title = "models/" + frame1.getTitle();
		//frame1.setVisible(false);
		frame1.closeFrame();
		//DrawNET.getApplication().getMainFrame().getDNDesktop().remove(frame1);
		
		/*
		// APRI
		frame2 = new DrawNETGraphFrame("SecuriDN/SecuriDN.dfs", model_);
		frame2.setVisible(true); 
		DrawNET.getApplication().getMainFrame().getDNDesktop().add(frame2);
		frame2.addToMenu((DrawNETWindowMenu) DrawNET.getApplication().getMainFrame().getDNDesktop().getMenuBar().getMenuItem("Window"));
		//frame2.setSelected(true);
		*/

		// APRI DA FILE
		chosenFile = new File("models/temp.mx");
		String[] extraFiles = { "Model" };
		mxFile = new MXFile(chosenFile, extraFiles);
		xmmf = new XMLModelFilter();
		xmmf.setFilename(mxFile.getModelFile("Model"));
		model = xmmf.read();
		if (title.contains("#"))
			frame2 = new DrawNETGraphFrame("SecuriDN/SecuriDN.dfs", model);
		else
			frame2 = new DrawNETGraphFrame("SecuriDN/SecuriDN.dfs", model, new File(title));
		frame2.setVisible(true);
		frame2.getDataSet().setChanged();
		System.out.println("title: " + title);
		//frame2.setTitle(title); 
		DrawNET.getApplication().getMainFrame().getDNDesktop().add(frame2);
		frame2.addToMenu((DrawNETWindowMenu) DrawNET.getApplication().getMainFrame().getDNDesktop().getMenuBar().getMenuItem("Window"));
	}
	catch (DNException e)
	{
		System.out.println("DNExcetpion");
	}

  }

  public boolean execute() 
  {
	int i, choice;

	if (! this.mainVisit())
		return false;
	
	if (! this.archVisit())
		return false;

	this.setAgLayer();

	this.ipComputers();

	choice = JOptionPane.showConfirmDialog(null, "Do you want to merge shared nodes?", "general AG generated", JOptionPane.YES_NO_OPTION);
	if (choice == JOptionPane.YES_OPTION)
	{
  		visitedAssets = new ArrayList<String>();
		reachableAssets = new ArrayList<String>();
        	this.agJoin3(); // AGGIORNARE
			
		choice = JOptionPane.showConfirmDialog(null, "Do you want to reduce the general AG?", "shared nodes merged", JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{	
			nodesIn = new ArrayList<String>();
			visitedNodes = new ArrayList<String>();
			//nodesOut = new ArrayList<String>();
			this.agVisit(attack);
			this.agVisitDefense();

			for (i=nodesIn.size()-1; i>=0; i--)
				System.out.println("In path: " + nodesIn.get(i));
			//for (i=nodesOut.size()-1; i>=0; i--)
				//System.out.println("Out path: " + nodesOut.get(i));
			if (nodesIn.size()==0)
				JOptionPane.showMessageDialog(null, "No paths found from " + attack.getId() + " to " + goal.getId());
			else
			{
				this.deleteNodes(); 
				//System.out.println("nodi cancellati");

				choice = JOptionPane.showConfirmDialog(null, "Do you want to generate the DBN?", "general AG reduced", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION)
				{
					this.dbnNodeBuild(); 
					//System.out.println("DBN nodi creati");
					this.dbnArcBuild(); 
					//System.out.println("DBN archi creati");
					this.dbnTemporalArcBuild(); 
					//System.out.println("DBN archi temp. creati");
					this.dbnParentBuild();
				}
			}	
		}
	}

	this.save(); 
	//System.out.println("modello aggiornato");

	return true;
  }

}

 /*
  private void archVisit2(ElementInstance asset, String destAssetType)
  {
	// VISITA RICORSIVA DELL'ARCHITETTURA ALLA RICERCA DEGLI ASSET DI UN CERTO TIPO

	FixedArrayPropertyValue fromConnections, toConnections;
	int i;
	String fap, toRef, fromRef;

	if (visitedAssets.contains(asset.getId()))
		return;

	if (asset.getElementType().getId().equals("Attacker") || asset.getElementType().getId().equals("Goal"))
		return;
	
	System.out.println("Visito " + asset.getId());
	visitedAssets.add(asset.getId());
	
	if (asset.getElementType().getId().equals(destAssetType))
	{
		System.out.println("Trovato " + asset.getId() + ":" + destAssetType);
	}
	
	fromConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_fromConnections");
	toConnections = (FixedArrayPropertyValue)asset.getPropertyValue("G_toConnections");

	
	for (i=0; fromConnections!=null && i<fromConnections.size(); i++)
	{
		fap = fromConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		toRef = arch.getSubElementByPath(fap).getPropertyValue("to").toString();
		//System.out.println("nodo puntato: " + toRef);
		this.archVisit2(arch.getSubElementByPath(toRef), destAssetType);
	}
	for (i=0; toConnections!=null && i<toConnections.size(); i++)
	{
		fap = toConnections.getPropertyValueAt(i).toString();
		//System.out.println("arco uscente: " + fap);
		fromRef = arch.getSubElementByPath(fap).getPropertyValue("from").toString();
		//System.out.println("nodo puntato: " + toRef);
		this.archVisit2(arch.getSubElementByPath(fromRef), destAssetType);
	}
  }

  private void agJoin2(ElementInstance nodeShared)
  {
	// CERCA GLI ASSET RAGGIUNGIBILI DALL'ASSET RELATIVO AL nodeShared.

	String nodeSharedName[] = nodeShared.getId().split("_");
	ElementInstance sourceAsset;
	String destAssetType;

	System.out.println("\nConsidero NodeShared " + nodeShared.getId());
	
	sourceAsset = arch.getSubElement(nodeSharedName[0]);
	destAssetType = nodeShared.getPropertyValue("asset").toString();
	
	System.out.println("Cerco un " + destAssetType + " raggiungibile da " + sourceAsset.getId());
	visitedAssets.clear();
	archVisit2(sourceAsset, destAssetType);
  }
  */

  /*
  private void agJoin(ElementInstance archArc)
  {
	// COLLEGA GLI AG LOCALI DEGLI ASSET COLLEGATI DA archArc.

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, archFrom, archTo;
  	ElementType elementType, archFromType, archToType;
	ElementRefPropertyValue archFromRef, archToRef;
	
	archFromRef = (ElementRefPropertyValue)archArc.getPropertyValue("from");
	archToRef = (ElementRefPropertyValue)archArc.getPropertyValue("to");
	archFrom = arch.getSubElementByPath(archFromRef.toString());
	archTo = arch.getSubElementByPath(archToRef.toString());
	archFromType = archFrom.getElementType();
	archToType = archTo.getElementType();

	System.out.println("\nArchitecture: from " + archFrom.getId() + " (" + archFromType.getId() + ") to " + archTo.getId() + " (" + archToType.getId() + ")");

	// asset renaming: asset type ---> asset name nei SharedNodes
	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getId().equals("NodeShared") && elementInstance.getId().contains(archTo.getId()) && elementInstance.getPropertyValue("asset").toString().equals(archFromType.getId()))
			elementInstance.setPropertyValue("asset", new StringPropertyValue(archFrom.getId()));

		if (elementType.getId().equals("NodeShared") && elementInstance.getId().contains(archFrom.getId()) && elementInstance.getPropertyValue("asset").toString().equals(archToType.getId()))
			elementInstance.setPropertyValue("asset", new StringPropertyValue(archTo.getId()));
	}

	this.arcMatch(archFrom, archTo);
	this.arcMatch(archTo, archFrom);
  }
  */

  /*
  private void arcMatch(ElementInstance archFrom, ElementInstance archTo)
  {
	// DATO UN ARCO DA Node a SharedNode, INDIVIDUA IL SUO DUALE. 

	Enumeration<ElementInstance> enumeration, enumeration2;
	ElementInstance elementInstance, elementInstance2, agFrom, agTo, arc1, from1, to1, arc2, from2, to2;
	ElementType elementType, elementType2, agFromType, agToType;
	ElementRefPropertyValue agFromRef, agToRef;

	//System.out.println("arcMatch tra " + archFrom.getId() + " " + archTo.getId());

	enumeration = ag.subElementsEnum();
      	while (enumeration.hasMoreElements())
	{
		arc1 = null;
		arc2 = null;
		from1 = null;
		from2 = null;
		to1 = null;
		to2 = null;

		elementInstance = enumeration.nextElement();
		if (elementInstance != null)
		{
			elementType = elementInstance.getElementType();
		
			if (elementType.getParent("_root.AG.AGArc") != null)
			{
				agFromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
				agToRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
				agFrom = ag.getSubElementByPath(agFromRef.toString());
				agTo = ag.getSubElementByPath(agToRef.toString());
				agFromType = agFrom.getElementType();
				agToType = agTo.getElementType();

				System.out.println("controllo " + elementInstance.getId() + " " + agFrom.getId() + " " + agTo.getId() + " " + agFromType.getId() + " " + agToType.getId());

				if (agFromType.getParent("_root.AG.AGNode")!=null && agToType.getId().equals("NodeShared") && agFrom.getId().contains(archFrom.getId()) && agTo.getPropertyValue("asset").toString().contains(archTo.getId()))
				{
					System.out.println("trovato " + elementInstance.getId());

					arc1 = elementInstance;
					from1 = agFrom;
					to1 = agTo;

					enumeration2 = ag.subElementsEnum();
      					while (enumeration2.hasMoreElements())
					{
						elementInstance2 = enumeration2.nextElement();
						elementType2 = elementInstance2.getElementType();
		
						if (elementType2.getParent("_root.AG.AGArc") != null)
						{
							agFromRef = (ElementRefPropertyValue)elementInstance2.getPropertyValue("from");
							agToRef = (ElementRefPropertyValue)elementInstance2.getPropertyValue("to");
							agFrom = ag.getSubElementByPath(agFromRef.toString());
							agTo = ag.getSubElementByPath(agToRef.toString());
							agFromType = agFrom.getElementType();
							agToType = agTo.getElementType();

							if (agFromType.getId().equals("NodeShared") && agToType.getParent("_root.AG.AGNode")!=null && agFrom.getPropertyValue("asset").toString().contains(archFrom.getId()) && agTo.getId().contains(archTo.getId()))
							{
								arc2 = elementInstance2;
								from2 = agFrom;
								to2 = agTo;
							}
						}
					}
					//if (arc1 != null)
					System.out.println("AG: from " + from1.getId() + " (" + from1.getElementType().getId() + ") to " + to1.getId() + " (" + to1.getElementType().getId() + ")");
					//if (arc2 != null)
					System.out.println("AG: from " + from2.getId() + " (" + from2.getElementType().getId() + ") to " + to2.getId() + " (" + to2.getElementType().getId() + ")");
					this.arcMerge(arc1, from1, to1, arc2, from2, to2);
				}
			}
		}
	}
  }
  */

  /*
  private void arcMerge(ElementInstance arc1, ElementInstance from1, ElementInstance to1, ElementInstance arc2, ElementInstance from2, ElementInstance to2)
  {
	// SOVRAPPONE DUE ARCHI

	FixedArrayPropertyValue segments1, segments2, toConnections;	
	StructPropertyValue xy1, xy2;
	Double x2, y2, ctrlx2, ctrly2;	
	String fap;
	int i;
	boolean trovato;

	arc1.setPropertyValue("to", new ElementRefPropertyValue(to2.getElementInstanceAbsolutePath()));
	
	segments1 = (FixedArrayPropertyValue)arc1.getPropertyValue("G_segmentsP1");
	xy1 = (StructPropertyValue)segments1.getPropertyValueAt(0);
	segments2 = (FixedArrayPropertyValue)arc2.getPropertyValue("G_segmentsP1");
	xy2 = (StructPropertyValue)segments2.getPropertyValueAt(0);

	x2 = Double.parseDouble(xy2.getPropertyValueAt("x2").toString());
	y2 = Double.parseDouble(xy2.getPropertyValueAt("y2").toString());
	ctrlx2 = Double.parseDouble(xy2.getPropertyValueAt("ctrlx2").toString());
	ctrly2 = Double.parseDouble(xy2.getPropertyValueAt("ctrly2").toString());
	
	xy1.setPropertyValue("x2", new FloatPropertyValue(x2));
	xy1.setPropertyValue("y2", new FloatPropertyValue(y2));
	xy1.setPropertyValue("ctrlx2", new FloatPropertyValue(ctrlx2));
	xy1.setPropertyValue("ctrly2", new FloatPropertyValue(ctrly2));

	toConnections = (FixedArrayPropertyValue)to2.getPropertyValue("G_toConnections");
	trovato=false;
	for (i=0; i<toConnections.size() && !trovato; i++)
	{
		fap = toConnections.getPropertyValueAt(i).toString();
		//System.out.println("\nfap: " + fap);
		//System.out.println(arc2.getElementInstanceAbsolutePath());
		if (fap.equals(arc2.getElementInstanceAbsolutePath()))
		{
			trovato=true;
			toConnections.setValue(i, new ElementRefPropertyValue(arc1.getElementInstanceAbsolutePath()));
		}
	}
	//to2.setPropertyValue("G_toConnections", fromConnections);

	ag.removeSubElement(to1.getId());
	ag.removeSubElement(arc2.getId());
	ag.removeSubElement(from2.getId());
  }
  */

