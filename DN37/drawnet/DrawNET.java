package drawnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

import drawnet.exception.DNException;
import drawnet.lib.ddl.Model;
import drawnet.lib.filters.XMLFilter;
import drawnet.lib.filters.XMLFormalismFilter;
import drawnet.lib.filters.XMLModelFilter;
import drawnet.modelgui.DNDocument;
import drawnet.modelgui.DNMainFrame;
import drawnet.modelgui.DNView;
import drawnet.modelgui.commands.CommandManager;
import drawnet.modelgui.dialogs.DrawNETFormalismSelect;
import drawnet.modelgui.dialogs.DrawNETProgressBar;
import drawnet.modelgui.dialogs.DrawNETSplashScreen;
import drawnet.modelgui.fsmstates.FSMManager;
import drawnet.modelgui.graphframe.DrawNETGraphFrame;
import drawnet.modelgui.menus.DrawNETWindowMenu;
import drawnet.utils.DialogUtils;
import drawnet.xml.XMLReadable;
import drawnet.xml.config.ConfigManager;
import drawnet.xml.config.DrawNETDeepConfig;
import drawnet.xml.mx.MXFile;

// Daniele:
import drawnet.modelgui.fsmstates.FSMState; 
import java.awt.Dimension;
//import java.awt.Frame;
//import java.awt.GraphicsDevice;
//import java.awt.GraphicsEnvironment;


public class DrawNET extends JFrame {

	private static final long serialVersionUID = 1L;

	private static DrawNET instance = null;
	
	private DNMainFrame mainFrame = null;
	
	private File mainConfigFile = null;
	
	private DrawNETSplashScreen splashScreen = null;
	
	private ConfigManager configManager = null;
	
	private CommandManager commandManager = null;
	
	private LinkedList<DNDocument> documents = new LinkedList<DNDocument>();
	
	private FSMManager fsmManager = null;
	
    public DrawNET() {
		System.setProperty( "sun.awt.noerasebackground", "true" );
		if( instance != null )
		{
			System.err.println( "Cannot create more than one application" );
			System.exit( 1 );
		}
		instance = this;
    }
    
    public static DrawNET getApplication() {
    	return instance;
    }
    
    public DNMainFrame getMainFrame() {
    	return mainFrame;
    }
    
    public ConfigManager getConfigManager() {
    	return configManager;
    }
    
    public CommandManager getCommandManager() {
    	return commandManager;
    }
    
	public DrawNETSplashScreen getSplashScreen() {
		return splashScreen;
	}
	
	public FSMManager getFSMManager() {
		return fsmManager;
	}
    
    public boolean setMainConfigFile(File configFile) throws FileNotFoundException {
    	if(configFile == null) {
    		throw new InvalidParameterException("Cannot pass a null config file.");
    	}
    	
    	if(!configFile.exists()) {
    		throw new FileNotFoundException("Configuration file not found.");
    	}
    	
    	mainConfigFile = configFile;
    	
    	return true;
    }
    
    public boolean startDrawNET() {
    	configManager = new ConfigManager();
    	
    	try {
    		if(!configManager.readConfiguration(mainConfigFile)) {
    			DialogUtils.showErrorDialog("Error",
    					"Error reading configuration file.\nApplication start aborted.");
    			System.exit(1);
    		}
    	} catch (DNException ex) {
    		ex.printStackTrace();
    		DialogUtils.showErrorDialog("Exception (" + ex.getClass().getSimpleName() + ")",
    				"Exception thrown parsing configuration file.\n" + ex.getLocalizedMessage());
    		System.exit(1);
    	}
    	
    	XMLReadable.setXMLPath(configManager.getFormalismPath());
    	XMLReadable.setDTDPath(configManager.getDtdPath());
    	
    	XMLFormalismFilter.setXMLPath(configManager.getFormalismPath().getAbsolutePath() + File.separator);
    	XMLFilter.setDTDPath(configManager.getDtdPath().getAbsolutePath() + File.separator);
    	
    	if(configManager.showSplashScreen()) {
    		splashScreen = new DrawNETSplashScreen();
    	}
    	
    	commandManager = new CommandManager();
    	
    	fsmManager = new FSMManager();
	
		try {
			// Si tenta di settare la skin impostata nel file di configurazione
			Skin skin = SkinLookAndFeel.loadThemePack(configManager.getSkinFile().toURI().toURL());
			SkinLookAndFeel.setSkin(skin);
			UIManager.setLookAndFeel(new SkinLookAndFeel());
		} catch (Exception ex) {
			ex.printStackTrace();
    		DialogUtils.showErrorDialog("Exception (" + ex.getClass().getSimpleName() + ")",
    				"Exception: unsupported look and feel: "
    				+ configManager.getSkinFile().getName() + "\nException thrown: " + ex.getLocalizedMessage());
    		try {
    			// In caso di errore si ripiega sul L&F standard del sistema
    			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    		} catch (Exception e) {
    			e.printStackTrace();
    			try {
    				// In caso di ulteriore errore si ripiega su quello default di Java
    				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    			} catch (Exception exc) {
    				exc.printStackTrace();
    				// Se anche il L&F di default non funziona, allora si esce
    	    		DialogUtils.showErrorDialog("Exception (" + exc.getClass().getSimpleName() + ")",
    	    				"Exception: unable to set application Look and Feel.\nAborting.");
    	    		System.exit(1);
    			}
    		}
		}
    	
		mainFrame = new DNMainFrame();
		
		if(splashScreen != null) {
			splashScreen.showSplashScreen();
		}
		
		//mainFrame.setTitle("DrawNET ]|[");
		mainFrame.setTitle("SecuriDN - DrawNET 3.7"); // Daniele
		
		try {
			if(!mainFrame.buildFrame()) {
				DialogUtils.showErrorDialog("Error",
						"Error building application main frame.\nAborting.");
				System.exit(1);
			}
		} catch (DNException ex) {
			ex.printStackTrace();
			return false;
		}

		// Daniele:		
		//mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setSize(new Dimension(920, 724));
		mainFrame.setLocationRelativeTo(null);

		mainFrame.setVisible(true);
		
		commandManager.start();

		// Daniele:
		FSMState destState = this.getApplication().getFSMManager().getState("BlackArrowInit");
		this.getApplication().getFSMManager().setCurrentState(destState);
		
		if(splashScreen != null) {
			splashScreen.hideSplashScreen();
		}

		// Daniele:
		//GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
      		//GraphicsDevice device = graphics.getDefaultScreenDevice();
		//device.setFullScreenWindow(mainFrame);
		
//		ProvaFrame pf = new ProvaFrame();
//		pf.setVisible(true);

		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
    }
    
    public void exitDrawNET() {
    	// TODO sistemare la procedura
	    if(!mainFrame.getDNDesktop().checkForExit()) {
	    	//System.exit(0);
	    	return;
	    }
	    
	    try {
		    if(this.closeAllDocuments()) {
		    	if(commandManager != null && commandManager.isStarted()) {
		    		commandManager.stop();
		    	}
		    	
		    	if(mainFrame != null) {
		    		mainFrame.dispose();
		    	}
		    	
		    	synchronized (this) {
		    		notifyAll();
				}
		    }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}
    
    public boolean closeDocument(DNDocument document) {
    	if(document == null) {
    		return false;
    	}
    	
    	if(!document.onCloseDocument()) {
    		return false;
    	}
    	
    	try {
    		if(saveIfChanged(document) && mainFrame.closeDocumentViews(document)) {
    			documents.remove(document);
    			return true;
    		}
    		return false;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		DialogUtils.showErrorDialog("Exception (" + ex.getClass().getSimpleName() + ")",
    				"Exception thrown closing document:\n" + ex.getLocalizedMessage());
    		return false;
		}
    }

    public boolean closeActiveDocument() {
    	DNDocument doc = this.getActiveDocument();
    	if(doc != null) {
    		return this.closeDocument(doc);
    	}
    	return false;
    }
    
    public boolean closeAllDocuments() {
    	while(documents.size() > 0) {
    		if( !this.closeDocument(documents.get(0)) ) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean closeView(DNView view) {
    	//TODO
    	return true;
    }
    
    public boolean activateDocument(DNDocument document) {
    	return mainFrame.activateDocument(document);
    }
    
    public boolean activateView(DNView view) {
    	return mainFrame.activateView(view);
    }
    
    public void addDocument(DNDocument document) {
    	this.documents.add(document);
    }
    
    public boolean newDocument() throws DNException {
    	//FIXME Risistemare il metodo, ripreso direttamente dal CMD_New
		DrawNETGraphFrame frame = null;
		if (DrawNETDeepConfig.AskFormalism && this.getMainFrame().getDNDesktop().getMayAskFormalism()) {
			int res = DrawNETFormalismSelect.Show();
			if(res != JOptionPane.OK_OPTION) {
				return false;
			}
			frame = new DrawNETGraphFrame(null, DrawNETFormalismSelect.getFormalismIndexFile());
		} else {
			frame = new DrawNETGraphFrame();
		}
		frame.setVisible(true); // necessary as of kestrel
		this.getMainFrame().getDNDesktop().add(frame);
		frame.addToMenu((DrawNETWindowMenu) this.getMainFrame().getDNDesktop().getMenuBar().getMenuItem("Window"));
		frame.SelectThisFrame();
		
    	return true;
    }
    
    public boolean openDocument() throws DNException {
    	//FIXME Risistemare il metodo, ripreso direttamente dal CMD_Open
		String newPath = DrawNETDeepConfig.ProjectsPath;
		String prjPath = System.getProperty("user.dir", "") + "/" + newPath
				+ (newPath.endsWith("/") ? "" : (newPath.equals("") ? "" : "/"));
		prjPath = "./models/"; // Daniele
		JFileChooser jfc = new JFileChooser(prjPath);
		
		int returnVal = jfc.showOpenDialog(DrawNET.getApplication().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = jfc.getSelectedFile();
			DrawNETProgressBar progressBar = DrawNET.getApplication().getMainFrame().getProgressBar();
			progressBar.showProgressBar();
			progressBar.setProgress("Loading model");

			if (chosenFile.getName().toLowerCase().endsWith(".mx")) {
				// Valid Only for XMLModelFilter
				String[] extraFiles = { "Model" };
				MXFile mxFile = new MXFile(chosenFile, extraFiles);
				progressBar.setProgress("Loading mx file: " + mxFile.getModelFile("Model"));

				XMLModelFilter xmmf = new XMLModelFilter();
				xmmf.setFilename(mxFile.getModelFile("Model"));
				Model model = xmmf.read();
				if(model == null) {
					DialogUtils.showErrorDialog("Error", "Model opening aborted.");
					progressBar.setProgress("Model loading aborted.");
					progressBar.hideProgressBar();
					return false;
				}

				progressBar.setProgress("Creating frame");

				DrawNETGraphFrame frame = new DrawNETGraphFrame(mxFile.getFDLname(), model, chosenFile); // Daniele
				
				progressBar.setProgress("Showing frame");

				frame.setVisible(true); // necessary as of kestrel
				this.getMainFrame().getDNDesktop().add(frame);
				frame.addToMenu((DrawNETWindowMenu) this.getMainFrame().getDNDesktop().getMenuBar().getMenuItem("Window"));
				try {
					frame.setSelected(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new DNException(ex.getLocalizedMessage());
				}
			}
			progressBar.setProgress("Model successfully loaded");

			progressBar.hideProgressBar();
		}
		
		return true;
    }

    protected boolean saveIfChanged(DNDocument document) {
    	//TODO
    	return true;
    }
    
    public boolean saveActiveDocument() {
    	DNDocument doc = this.getActiveDocument();
    	if(doc != null) {
    		return doc.save();
    	}
    	return false;
    }
    
    public boolean saveAsActiveDocument() {
    	DNDocument doc = this.getActiveDocument();
    	if(doc != null) {
    		return doc.saveAs();
    	}
    	return false;
    }
    
    public DNView getActiveView() {
    	//TODO
    	return null;
    }
    
    public DNDocument getActiveDocument() {
    	//TODO
    	return null;
    }
    
    public int getDocumentCount() {
    	return this.documents.size();
    }
    
    public List<DNDocument> getDocumentList() {
    	return this.documents;
    }
    
    public List<DNDocument> getDocumentsWithFile(File docFile) {
    	LinkedList<DNDocument> docList = new LinkedList<DNDocument>();
    	for(DNDocument doc : documents) {
    		if(doc.getFile() != null && doc.getFile().equals(docFile)) {
    			docList.add(doc);
    		}
    	}
    	return docList;
    }
    
    public void updateAllViews() {
    	this.updateAllViews(null);
    }
    
    public void updateAllViews(Object caller) {
    	for(DNDocument doc : documents) {
    		doc.updateAllViews(caller);
    	}
    }
    
}
