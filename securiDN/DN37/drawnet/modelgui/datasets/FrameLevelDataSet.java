package drawnet.modelgui.datasets;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import drawnet.DrawNET;
import drawnet.exception.DNException;
import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Formalism;
import drawnet.lib.ddl.Model;
import drawnet.lib.filters.XMLFormalismFilter;
import drawnet.lib.filters.XMLModelFilter;
import drawnet.modelgui.graphframe.GraphFrameTop;
import drawnet.modelgui.graphicelements.GE_SubModel;
import drawnet.modelgui.panels.DrawNETPanels;
import drawnet.modelgui.panels.elementpanel.DrawNETModelElementInstancePanel;
import drawnet.modelgui.panels.explorerpanel.DrawNETStructurePanel;
import drawnet.modelgui.panels.objectpanel.DrawNETExportPanel;
import drawnet.modelgui.panels.objectpanel.DrawNETImportPanel;
import drawnet.modelgui.panels.objectpanel.DrawNETLibraryPanel;
import drawnet.utils.ElementPanelsList;
import drawnet.utils.SubModelNodeInfo;
import drawnet.xml.config.DrawNETDeepConfig;
import drawnet.xml.dfs.DrawNETFormalismSetup;
import drawnet.xml.mx.MXFile;

public class FrameLevelDataSet {
	private DrawNETStructurePanel StructurePanel;
	private DrawNETLibraryPanel LibraryPanel;
	private DrawNETImportPanel ImportPanel;
	private DrawNETExportPanel ExportPanel;

	@Deprecated
	private DrawNETPanels desktop;

	private DefaultMutableTreeNode Root;
	private DefaultMutableTreeNode SelectedNode;

	private Formalism _Formalism;
	private DrawNETFormalismSetup FSetup;

	private ElementType RootFormalism;
	private ElementType MainFormalism;
	private boolean Instantiable;
	private Model _Model;

	private String PrincipalFormalism;
	private String RootElementName;

	private String FDLname;
	private String CommonPrefix;

	private File mxFile;
	private ElementInstance copiedReference;
	
	@Deprecated
	private String FileName;
	private String PureName = "";
	private String PurePath = "";
	private boolean ChangedSinceLastSaving;
	private JFileChooser jfc;

	public DrawNETStructurePanel getStructurePanel() {
		return StructurePanel;
	}

	public void setStructurePanel(DrawNETStructurePanel SP) {
		StructurePanel = SP;
	}

	public DrawNETLibraryPanel getLibraryPanel() {
		return LibraryPanel;
	}

	public void setLibraryPanel(DrawNETLibraryPanel P) {
		LibraryPanel = P;
	}

	public DrawNETImportPanel getImportPanel() {
		return ImportPanel;
	}

	public void setImportPanel(DrawNETImportPanel P) {
		ImportPanel = P;
	}

	public DrawNETExportPanel getExportPanel() {
		return ExportPanel;
	}

	public void setExportPanel(DrawNETExportPanel P) {
		ExportPanel = P;
	}

	public void selectNode(DefaultMutableTreeNode SN) {
		SelectedNode = SN;
	}

	public void selectNodeIfNull(DefaultMutableTreeNode SN) {
		if (SelectedNode == null) {
			SelectedNode = SN;
		}
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return SelectedNode;
	}

	public DefaultMutableTreeNode getRootNode() {
		return Root;
	}

	public ElementInstance getRootInstance() {
		return _Model.getRootElement();
	}
	
	public void addModelElement(String ToElement, ElementType ElF, String Name)
	{
//C		RootInstance.addSubElement(ToElement, Name, ElF);
	}
	
	public void setModelElementProperty(String El, String Prop, String Val)
	{
/*C		DDL.ElementInstance e=getModelElementInstance(El);
		if(e != null) {
		    e.setPropertyValue(Prop, Val);
		    return;
		} else {
		    DDL.AggregateInstance f=getAggregateInstance(El);
		    if(f != null) {
			    f.setPropertyValue(Prop, Val);
			    return;
		    }
		}
*/
	}

	public void removeModelElement(String El)
	{
/*C		DDL.ElementInstance e=getModelElementInstance(El);

	    	if(El.lastIndexOf(".") >= 0)
	    	    El = El.substring(El.lastIndexOf(".") + 1);
		if(e != null) {
		    DDL.ElementInstance ce=e.getContainerElement();
		    if(ce != null) {
		       ce.removeSubElement(El);
		    }
		}
*/	}
	
	
	public ElementInstance getModelElementInstance(String El) {
		if (El == null)
			return _Model.getRootElement();
		return _Model.getSubElementByPath(El);
	}
	
	public String getRootXML() {
/*C	    if(DrawNETDeepConfig.SyntaxHighlighting)
		return RootInstance.toXMLinHTML(FDLname, RootElementName);
	    else
		return RootInstance.toXML(FDLname, RootElementName);
*/ return "";
	}
	
	protected boolean InitFormalism(String FName) {

	   if(FName != null)
	   	FDLname = FName;
	   else
		FDLname = desktop.getDefaultFormalismName();
	
	   if(FDLname.endsWith(".fdl")) {
	       XMLFormalismFilter xmlf = new XMLFormalismFilter();
	       xmlf.setFilename(FDLname);
	       _Formalism = xmlf.read();
	       FSetup = null;
	       if(_Formalism == null) {
	    	   System.out.println("Error parsing " + FDLname + " file.\n");
	    	   return false;
	       }
	   } else if(FDLname.endsWith(".dfs")) {
		   try {
		   	FSetup = new DrawNETFormalismSetup(FDLname);
		   	_Formalism = FSetup.getFormalism();
		   } catch (Exception ex) {
			   ex.printStackTrace();
			   return false;
		   }
	   } else {
	   	System.out.println("\n\n Error: " + FDLname + " is not a valid formalism definition file\n\n");
	   	FSetup = null;
	   	_Formalism = null;
	   	return false;
	   }
/*	   
System.out.println(_Formalism.getRootElement().subElements().length);
for(int ki=0;ki<_Formalism.getRootElement().subElements().length;ki++)System.out.println(_Formalism.getRootElement().subElements()[ki]);
System.out.println(_Formalism.getMainElement().subElements().length);
for(int ki=0;ki<_Formalism.getMainElement().subElements().length;ki++)System.out.println(_Formalism.getMainElement().subElements()[ki]);
*/
	   RootFormalism = _Formalism.getRootElement();
	   MainFormalism = _Formalism.getMainElement();
/*C		MXFile mxf = null;
		
		if(FName == null) {
		    FDLname = desktop.getDefaultFormalismName();
		    MDLname = null;
		} else {
		    mxf = new MXFile(FName);
		    FDLname = mxf.FDLname;
		    MDLname = mxf.MDLname;
		}
		RootFormalism = new DDL.ElementType("Root", true);
		MainFormalism = RootFormalism.getSubElementType(PrincipalFormalism);
*/

		PrincipalFormalism = MainFormalism.getId();

		if(!MainFormalism.isDerivedFrom("_root.GraphBased")) {
			System.out.println("WARNING: Main formalsim (" + PrincipalFormalism
					+ ") is not Graph Base!\n");
		}
		
		Instantiable = MainFormalism.isDerivedFrom("_root.Instantiable");
		return true;
	}

	public FrameLevelDataSet() throws DNException {
		this((String)null);
	}
	

	public FrameLevelDataSet(String formalismFileName) throws DNException {
		this(formalismFileName, null,null); // Daniele
	}
	
	public FrameLevelDataSet(String formalismFileName, Model _initMod, File file) throws DNException
	{
	   	String FormFileName;
	   	
	   	if (file != null) // Daniele
		{
	   		FileName = file.getName(); 
			//System.out.println("filename: " + FileName);
		}
		else
			FileName = null;

		SubModelLevelDataSet SMLDS;
		desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();

		if(!InitFormalism(formalismFileName))	// Loads formalism: FDL, RDL and FRL
			return;
				
		if(_initMod != null) {
		    RootElementName = _initMod.getMainElement().getId();
		} else {
		    RootElementName = "Root" + PrincipalFormalism;
		}

//		String CompleteElName = "SubModel.Root." + PrincipalFormalism;

			// Create ElementLevelDataSet in ELDS for the root element
//		ElementLevelDataSet ELDS = new ElementLevelDataSet(D, "", new GE_void(),
//	    			   new DrawNETG_ElementPanel(D, MainFormalism, FSetup));
//		D.getTELDSC().add(CompleteElName, ELDS);
			// Create the ElementLevelDataSet for the root element

//////////////////////// Per adesso non ci sono GE associati ad un sottomodello
//		GE_SubModel GEn = new GE_SubModel(CompleteElName);
//		GE_Node GEn = new GE_Node(D, GraphicElement.SUBMODEL, CompleteElName);
//		ElementLevelDataSet NewELDS = new ElementLevelDataSet(D,
//			RootElementName, GEn, ELDS.getElementPanel());

/////////////////////// Per adesso utilizza un pannello vuoto per il sottomodello
//		ElementLevelDataSet NewELDS = new ElementLevelDataSet(D,
//			RootElementName, GEn, new DrawNETG_ElementPanel(D, MainFormalism, FSetup));
///////////////////////// Qui sotto
//		ElementLevelDataSet NewELDS = new ElementLevelDataSet(D,
//			RootElementName, GEn, new DrawNETElementPanel(D));
			// Create the root element instance
		if(_initMod != null) {
		    _Model = _initMod;
		} else {
		    _Model = new Model(RootElementName, _Formalism);
		}
		
//C		RootInstance = new DDL.ElementInstance(RootElementName,
//C				MainFormalism, null);	
		
//		SMLDS = new SubModelLevelDataSet(D, new String[] {RootElementName},
//						 MainFormalism, FSetup, _Model);
//		SMLDS.setContainerElement(NewELDS);
		
//	    	Root = new DefaultMutableTreeNode(
//	    			new SubModelNodeInfo(SMLDS));

	        ElementPanelsList EPList = desktop.getEPList();
	        String RootFormalismName = _Model.getMainElement().getElementType().getElementTypeAbsolutePath();
	        if(!EPList.contains(RootFormalismName)) {
		     EPList.add(RootFormalismName,
    			   new DrawNETModelElementInstancePanel(_Model.getMainElement().getElementType(), FSetup)
    			   );
		}

		GE_SubModel GEn = (GE_SubModel)(new GE_SubModel()).createFromElementInstance(
					_Model.getMainElement(), RootElementName);
		ElementLevelDataSet ELDS = desktop.makeElementLevelDataSet(RootElementName, GEn,
									_Model.getMainElement(), null);
		GEn.updateFrameLevelDeps(this);

		Root = GEn.getSMLDataSetTreeNode();
		SelectedNode = Root;
		
//		GEn.setSpec(SMLDS, Root);
		StructurePanel = new DrawNETStructurePanel(Root);
		GEn.setDSElement(ELDS);
		
		
		if(Instantiable) {
				// Model class related panel are created only
				// if the formalism is instantiable
		    ImportPanel = new DrawNETImportPanel();
		    ExportPanel = new DrawNETExportPanel();
        	    LibraryPanel = new DrawNETLibraryPanel();
        	} else {
        	    ImportPanel = null;
        	    ExportPanel = null;
        	    LibraryPanel = null;
		}
		FormFileName = formalismFileName;
		if(FormFileName != null) {
		    int FdPos = FormFileName.lastIndexOf(".dfs");
		    if(FdPos < 0) FdPos = FormFileName.lastIndexOf(".fdl");
                    if(FdPos>=0)
                        PureName = FormFileName.substring(0,FdPos);
                    else
                        PureName = null;
                    FdPos = FormFileName.lastIndexOf(File.separator);
                    if(FdPos >= 0)
                        PurePath = FormFileName.substring(0,FdPos);
                    else
                        PurePath = null;
		} else {
		    PureName = null;
		    PurePath = null;
		}

		ChangedSinceLastSaving = false;
	    	String newPath = DrawNETDeepConfig.ProjectsPath;
	    	String PrjPath = System.getProperty("user.dir","") + "/" +
			  newPath + (newPath.endsWith("/")  ? "" :
				    (newPath.equals("") ? "" : "/"));
		PrjPath = "./models/"; // Daniele
	    	jfc = new JFileChooser(PrjPath);

		// Daniele:

		// FileName -> with extension .mx
		// PureName -> without extension .mx
		// PurePath -> Only the path of the file
		// CommonPrefix -> The name with which all the file belonging to this model should begin with
		
		if (FileName != null)
		{
			if(!FileName.endsWith(".mx")) {			// Clear or add the .mx extension
		            PureName = FileName;
		            FileName = FileName + ".mx";
			} 
			else
			    PureName = FileName.substring(0,FileName.lastIndexOf(".mx"));

			PurePath = file.getAbsolutePath();
                	PurePath = PurePath.substring(0,PurePath.lastIndexOf(File.separator)) + "/";
			//desktop.changeSelectedFrameTitle(FileName); // Daniele
			//System.out.println("filename: " + FileName);

			CommonPrefix = (DrawNETDeepConfig.ProjectsFileHolderDir.equals("") ?
				 "" :  DrawNETDeepConfig.ProjectsFileHolderDir + "/" ) +
		    		 PureName;

			File contentDir = new File(PurePath + CommonPrefix);
			contentDir.mkdirs();
			CommonPrefix = CommonPrefix + "/" + PureName;
		}
	}
	
	public GraphFrameTop getCurrentFrameTop()
	{
		return ((SubModelNodeInfo)SelectedNode.getUserObject())
				.getDataSet().getFrameTop();
	}

	public JScrollPane getCurrentDrawArea()
	{
		return ((SubModelNodeInfo)SelectedNode.getUserObject())
				.getDataSet().getScrollDrawArea();
	}

	public void add(DefaultMutableTreeNode M, DefaultMutableTreeNode N) {
		StructurePanel.add(M,N);
	}

	public void add(DefaultMutableTreeNode N) {
		StructurePanel.add(SelectedNode,N);
	}

	public void remove(DefaultMutableTreeNode N) {
		if(SelectedNode == N) SelectedNode = null;
		StructurePanel.remove(N);
	}
	public void setChanged() {
		ChangedSinceLastSaving = true;
	}

    public void save() {
    	saveAs(false);
    }
    
	public void saveAs(boolean ReqName) {
		boolean SaveIt = false;

		if((FileName == null) || ReqName) {
		    int returnVal = jfc.showSaveDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	File file = jfc.getSelectedFile();
		    	FileName = file.getName();
		    	SaveIt = true;
		    	// FileName -> with extension .mx
		    	// PureName -> without extension .mx
		    	// PurePath -> Only the path of the file
		    	// CommonPrefix -> The name with which all the file belonging to this model should begin with
		        if(!FileName.endsWith(".mx")) {			// Clear or add the .mx extension
		            PureName = FileName;
		            FileName = FileName + ".mx";
		        } else
		            PureName = FileName.substring(0,FileName.lastIndexOf(".mx"));

			PurePath = file.getAbsolutePath();
                        PurePath = PurePath.substring(0,PurePath.lastIndexOf(File.separator)) + "/";
		    	desktop.changeSelectedFrameTitle(FileName);

		        CommonPrefix = (DrawNETDeepConfig.ProjectsFileHolderDir.equals("") ?
		    		 "" :  DrawNETDeepConfig.ProjectsFileHolderDir + "/" ) +
		    		 PureName;

		        File contentDir = new File(PurePath + CommonPrefix);
		        contentDir.mkdirs();
		        CommonPrefix = CommonPrefix + "/" + PureName;
		    }
		} else SaveIt = true;
		
		if(SaveIt) {
		    ChangedSinceLastSaving = false;
		    MXFile mxf = new MXFile();
		    
		    mxf.setCommonPrefix(CommonPrefix);
		    mxf.setPath(PurePath);
		    mxf.setFDLname(FDLname);
		    mxf.addModelFile("Model", ".mdl");
		    
		    mxf.save(PurePath + FileName);
		    
		//// The following instructions works only with the XMLModelFilter
		    XMLModelFilter xmmf = new XMLModelFilter();
		    xmmf.setFilename(mxf.getModelFile("Model"));
/*		    if(FDLname.endsWith(".fdl"))
		    	xmmf.setFormalismFilename(FDLname);
		    else
		    	xmmf.setFormalismFilename(FDLname.substring(0,FDLname.lastIndexOf(".")) + ".fdl");
*/
		    xmmf.write(_Model);
		}
	}
		
	public boolean isChanged() {
		return ChangedSinceLastSaving;
	}
	
	public DrawNETFormalismSetup getSetup() {
		return FSetup;
	}
	
	public Model getModel() {
		return _Model;
	}
	public void renameRootElement(String NewName) {
//	    RootElementName = NewName;
//	    _Model.getMainElement().setId(NewName);
	    System.out.println("ERROR: Root element cannot be renamed!\n(YET - maybe it will be possible in the next version!)");
	}
	
	public ElementInstance getCopiedReference() {
		return copiedReference;
	}
	
	public void setCopiedReference(ElementInstance ref) {
		copiedReference = ref;
	}
	
}
