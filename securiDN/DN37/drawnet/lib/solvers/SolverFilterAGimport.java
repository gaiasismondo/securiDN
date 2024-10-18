package drawnet.lib.solvers;

import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Comparator;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;

import java.awt.Dimension;

import org.xml.sax.SAXParseException;

import drawnet.lib.filters.XMLModelFilter;

import drawnet.lib.ddl.Model;
import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;

import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.FixedArrayPropertyValue;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.lib.ddl.propertyvalues.StructPropertyValue;

import drawnet.lib.ddl.propertytypes.FixedArrayPropertyType;
import drawnet.lib.ddl.propertytypes.StructPropertyType;

import drawnet.xml.mx.MXFile;

import drawnet.DrawNET;

import drawnet.exception.DNException;

import drawnet.modelgui.graphframe.DrawNETGraphFrame;
import drawnet.modelgui.menus.DrawNETWindowMenu;

public class SolverFilterAGimport extends SolverFilter 
{

  private final String DESCR = "AG import";

  private Model model;
  private ElementInstance arch;
  private ArrayList<String> assetList;
  private ArrayList<Model> modelList;
 
  /**
   * Constructor.
   */
  public SolverFilterAGimport() 
  {
    	super();
    	setDescription( DESCR );
  }

  /**
   * Constructor.
   * 
   * @param listener the listener to assign to this solver
   */
  public SolverFilterAGimport( SolverListener listener ) 
  {
    	super( listener );
    	setDescription( DESCR );
  }

  /*
   * (non-Javadoc)
   * 
   * @see solvers.SolverLauncher#execute()
   */

  private void print(String msg)
  {
	System.out.println(msg);
  }

  private int mainVisit()
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
  	ElementType elementType;
	int archCount = 0;

	// CERCA ARCH NEL MODELLO PRINCIPALE.
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
	}

	if (archCount == 1)
	{
		// CREA LA LISTA DI ASSET PRESENTI IN arch

		assetList = new ArrayList<String>();
		modelList = new ArrayList<Model>();
	
		enumeration = arch.subElementsEnum();
      		while (enumeration.hasMoreElements())
      		{
			elementInstance = enumeration.nextElement(); 
			elementType = elementInstance.getElementType();
	
			if (elementType.getParent("_root.Arch.AssetNode") != null)
			{
				assetList.add(elementInstance.getId());
				modelList.add(null);
			}
		}

		assetList.sort(Comparator.naturalOrder());
  	}
	else
		JOptionPane.showMessageDialog(null, "The model must contain one Architecture", "error", JOptionPane.ERROR_MESSAGE, null);

	return archCount;
  }

  private boolean selectAsset()
  {
	// GUI PER ASSOCIARE I FILE AGLI ASSET

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
  	ElementType elementType;
	int i, scelta, returnVal, esito;
	boolean trovato = false;
	
	String[] scelte = {"choose AG file", "remove AG file", "import all", "cancel"};
	String prjPath = "./models/";
	String chosenAsset, fileName;
	File chosenFile;

	JTable assetTable;
	JScrollPane assetPanel;
	JFileChooser jfc; 

	assetTable = new JTable(assetList.size(), 2)
	{	
		// non editabile
		public boolean editCellAt(int row, int column, java.util.EventObject e) 
		{
			return false;
		}
	};

	assetTable.getColumnModel().getColumn(0).setHeaderValue("asset");
	assetTable.getColumnModel().getColumn(0).setPreferredWidth(200);
	assetTable.getColumnModel().getColumn(1).setHeaderValue("file");
	assetTable.getColumnModel().getColumn(1).setPreferredWidth(200);
	
	for (i=0; i < assetList.size(); i++)
	{
		assetTable.setValueAt(assetList.get(i), i, 0);
		assetTable.setValueAt("", i, 1);
	}

	assetTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	assetTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

	assetPanel = new JScrollPane(assetTable);
	assetPanel.setPreferredSize(new Dimension(405,17*assetList.size()));

	jfc = new JFileChooser(prjPath);

	for (i=0; i < modelList.size(); i++)
		modelList.set(i, null);

	// SELEZIONA I FILE DA IMPORTARE
	
	do
	{
		scelta = JOptionPane.showOptionDialog(null, assetPanel, "Select an asset and choose the AG file to import.", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, scelte, "choose AG file");

		// AGGIUNGI FILE
		if (scelta == 0 && assetTable.getSelectedRow() >= 0)
		{
			returnVal = jfc.showOpenDialog(DrawNET.getApplication().getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				// CARICA E CONTROLLA
				chosenFile = jfc.getSelectedFile();
				esito = AGload(chosenFile);
				if (esito == 0) // OK
				{
					assetTable.setValueAt(chosenFile.getName(), assetTable.getSelectedRow(), 1);
					modelList.set(assetTable.getSelectedRow(), model);
				}
				else
				{
					chosenAsset = (String)assetTable.getValueAt(assetTable.getSelectedRow(), 0);
					this.mostraErrore(chosenFile.getName(), chosenAsset, esito);
				}
			}
		}

		// RIMUOVI FILE
		if (scelta == 1 && assetTable.getSelectedRow() >= 0)
		{
			assetTable.setValueAt("", assetTable.getSelectedRow(), 1);
			modelList.set(assetTable.getSelectedRow(), null);
		}

		if ( (scelta == 0 || scelta ==1) && assetTable.getSelectedRow() == -1)
			this.mostraErrore(null, null, 4);
	}
	while (scelta == 0 || scelta == 1);

	// IMPORTA

	if (scelta == 2)
	{
		for (i=0; i < assetList.size(); i++)
		{
			chosenAsset = assetList.get(i);
			model = modelList.get(i);
			//chosenAsset = (String)assetTable.getValueAt(i, 0);
			//chosenFile = (String)assetTable.getValueAt(i, 1);
			
			if (model != null)
			{
				//chosenFile = fileList.get(i);
				//AGload(chosenFile); 
				this.AGimport(chosenAsset);
				trovato = true;

				fileName = (String)assetTable.getValueAt(i, 1);
				JOptionPane.showMessageDialog(null, fileName + " imported in " + chosenAsset, "success", JOptionPane.INFORMATION_MESSAGE, null);
			}
		}
		
		if (! trovato)
			this.mostraErrore(null, null, 5);

	}

	return trovato;
  }

  private void mostraErrore(String fileName, String assetName, int errore)
  {
	String testo = "";

	if (errore == 1)
		testo = fileName + ", associated with " + assetName + ", does not exist.";

	if (errore == 2)
		testo = fileName + ", associated with " + assetName + ", is not a .mx file.";

	if (errore == 3)
		testo = fileName + ", associated with " + assetName + ", does not contain an AG model.";

	if (errore == 4)
		testo = "No asset selected.";

	if (errore == 5)
		testo = "No AG imported.";

	JOptionPane.showMessageDialog(null, testo, "error", JOptionPane.ERROR_MESSAGE, null);
  }

  private int AGload(File file)
  {
	MXFile mxFile;
	XMLModelFilter xmmf;
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance;
	String[] extraFiles = { "Model" };

	// CARICA MODELLO DA FILE

	if (! file.exists())
		return 1; // FILE NON ESISTE

	if (! file.getName().contains(".mx"))
		return 2; // NON E' UN MODELLO

	mxFile = new MXFile(file, extraFiles);
	xmmf = new XMLModelFilter();
	xmmf.setFilename(mxFile.getModelFile("Model"));
	model = xmmf.read();

	if (! model.getFormalism().getMainId().equals("AG"))
		return 3; // FORMALISMO DIVERSO DA AG	

	/*
	enumeration = model.getMainElement().subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		//elementType = elementInstance.getElementType();
		print(elementInstance.getId() + ":" + elementInstance.getElementType().getId());
	}
	*/

	return 0; // OK
  }

  private void AGimport(String assetName)
  {
	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, asset;
	ElementType elementType;

	asset = arch.getSubElement(assetName);

	// CLEAN asset's AG.
	enumeration = asset.subElementsEnum();
	while (enumeration.hasMoreElements())
	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null || elementType.getParent("_root.AG.AGArc") != null)
			asset.removeSubElement(elementInstance.getId());
	}

	// COPIA AG IN ASSET

	this.AGnodeBuild(asset);
	this.AGarcBuild(asset);
  }

  private void AGnodeBuild(ElementInstance asset)
  {
	// COPIA I NODI NELL'AG DELL'ASSET

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, node;
  	ElementType elementType;
	ElementRefPropertyValue layer;
	FixedArrayPropertyValue fromConnections, toConnections, newConnections;
	FixedArrayPropertyType connectionType;
	double tx, ty, prob;
	int order, i;
	String fap;

	layer = new ElementRefPropertyValue(asset.getSubElement("G_Layer1"));

	enumeration = model.getMainElement().subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGNode") != null)
		{
			//print("\n" + elementInstance.getId() + ":" + elementType.getId());

			node = new ElementInstance(elementInstance.getId(), asset.getElementType().getSubElement(elementType.getId()), true);

			if (elementType.getId().equals("Node"))
			{
				prob = FloatPropertyValue.getFloatProperty(elementInstance, "prob");
				FloatPropertyValue.setFloatProperty(node, "prob", prob);
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
			//if (order > maxOrder)
				//maxOrder = order;

			fromConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_fromConnections");
			if (fromConnections != null)
			{
				connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_fromConnections", true);
				newConnections = new FixedArrayPropertyValue(connectionType, fromConnections.size());

				for (i=0; i<fromConnections.size(); i++)
				{
					fap = fromConnections.getPropertyValueAt(i).toString();
					//System.out.println("fap: " + fap);
					//fap = fap.replace(model.getMainElement().getElementInstanceAbsolutePath(), asset.getElementInstanceAbsolutePath());
					fap = asset.getElementInstanceAbsolutePath() + "." + fap;
					//System.out.println("fap: " + fap);
					newConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				node.setPropertyValue("G_fromConnections", newConnections);
			}

			toConnections = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_toConnections");
			if (toConnections != null)
			{
				connectionType = (FixedArrayPropertyType)node.getElementType().getProperty("G_toConnections", true);
				newConnections = new FixedArrayPropertyValue(connectionType, toConnections.size());

				for (i=0; i<toConnections.size(); i++)
				{
					fap = toConnections.getPropertyValueAt(i).toString();
					//System.out.println("fap: " + fap);
					//fap = fap.replace(model.getMainElement().getElementInstanceAbsolutePath(), asset.getElementInstanceAbsolutePath());
					fap = asset.getElementInstanceAbsolutePath() + "." + fap;
					//System.out.println("fap: " + fap);
					newConnections.setValue(i, new ElementRefPropertyValue(fap));
				}
				node.setPropertyValue("G_toConnections", newConnections);
			}

			if (! node.setPropertyValue("G_inLayer", layer))
       				System.out.println("layer NON impostato in " + node.getId());
			//else
				//System.out.println("layer impostato in " + node.getId());

			if (! asset.addSubElement(node))
				System.out.println(node.getId() + " NON AGGIUNTO");
			//else
				//System.out.println(node.getId() + " AGGIUNTO");
		}
	}
  }

  private void AGarcBuild(ElementInstance asset)
  {
	// COPIA GLI ARCHI NELL'AG DELL'ASSET

	Enumeration<ElementInstance> enumeration;
	ElementInstance elementInstance, arc;
  	ElementType elementType;
	ElementRefPropertyValue layer, fromRef, toRef;
	FixedArrayPropertyValue segments, segmentsNew;
	StructPropertyValue xy, xyNew;
	StructPropertyType structType;
	FixedArrayPropertyType segmentType;
	double tx, ty, x1, x2, y1, y2, ctrlx1, ctrlx2, ctrly1, ctrly2;
	int order;
	String from, to;
	
	layer = new ElementRefPropertyValue(asset.getSubElement("G_Layer1"));

	enumeration = model.getMainElement().subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
		elementInstance = enumeration.nextElement();
		elementType = elementInstance.getElementType();

		if (elementType.getParent("_root.AG.AGArc") != null)
		{
			//print("\n" + elementInstance.getId() + ":" + elementType.getId());

			arc = new ElementInstance(elementInstance.getId(), asset.getElementType().getSubElement("Arc"), true);

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
			//if (order > maxOrder)
				//maxOrder = order;
			
			fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
			from = fromRef.getElementReferenceName();
			//System.out.println("from: " + from);
			from = from.replace(model.getMainElement().getElementInstanceAbsolutePath(), asset.getElementInstanceAbsolutePath());
			//System.out.println("from: " + from);
			arc.setPropertyValue("from", new ElementRefPropertyValue(from));

			toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
			to = toRef.getElementReferenceName();
			//System.out.println("to: " + to);
			to = to.replace(model.getMainElement().getElementInstanceAbsolutePath(), asset.getElementInstanceAbsolutePath());
			//System.out.println("to: " + to);
			arc.setPropertyValue("to", new ElementRefPropertyValue(to));

			segments = (FixedArrayPropertyValue)elementInstance.getPropertyValue("G_segmentsP1");
			if (segments != null)
			{
				xy = (StructPropertyValue)segments.getPropertyValueAt(0);

				x1 = Double.parseDouble(xy.getPropertyValueAt("x1").toString());
				//System.out.print("x1: " + x1);
				x2 = Double.parseDouble(xy.getPropertyValueAt("x2").toString());
				//System.out.print("x2: " + x2);
				y1 = Double.parseDouble(xy.getPropertyValueAt("y1").toString());
				//System.out.print("y1: " + y1);
				y2 = Double.parseDouble(xy.getPropertyValueAt("y2").toString());
				//System.out.print("y2: " + y2);
			
				ctrlx1 = Double.parseDouble(xy.getPropertyValueAt("ctrlx1").toString());
				ctrlx2 = Double.parseDouble(xy.getPropertyValueAt("ctrlx2").toString());
				ctrly1 = Double.parseDouble(xy.getPropertyValueAt("ctrly1").toString());
				ctrly2 = Double.parseDouble(xy.getPropertyValueAt("ctrly2").toString());

				segmentType = (FixedArrayPropertyType)arc.getElementType().getProperty("G_segmentsP1", true);
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
				arc.setPropertyValue("G_segmentsP1", segmentsNew);
			}

			if (! arc.setPropertyValue("G_inLayer", layer))
       				System.out.println("layer NON impostato in " + arc.getId());
			//else
				//System.out.println("layer impostato in " + node.getId());

			if (! asset.addSubElement(arc))
				System.out.println(arc.getId() + " NON AGGIUNTO");
			//else
				//System.out.println(node.getId() + " AGGIUNTO");
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
		frame1.closeFrame();
		
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
	//print("AGimport");
	if (this.mainVisit() == 1)
		if (this.selectAsset())
			this.save();
		
	return true;
  }

}


