package drawnet.modelgui.graphframe;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


import drawnet.DrawNET;
import drawnet.exception.DNException;
import drawnet.lib.ddl.Model;
import drawnet.modelgui.capabilities.ICutCopyPaste;
import drawnet.modelgui.capabilities.IPrintable;
import drawnet.modelgui.capabilities.ISVGExportable;
import drawnet.modelgui.capabilities.IUndoRedo;
import drawnet.modelgui.commands.frame.CMD_Close;
import drawnet.modelgui.datasets.FrameLevelDataSet;
import drawnet.modelgui.datasets.SubModelLevelDataSet;
import drawnet.modelgui.datasets.ElementLevelDataSet;
import drawnet.modelgui.menus.DrawNETWindowMenu;
import drawnet.modelgui.panels.DrawNETPanels;
import drawnet.utils.ElementSelection;
import drawnet.utils.ICheckCloseAndBottom;

import java.io.File; // Daniele

public class DrawNETGraphFrame extends JInternalFrame implements
		ICheckCloseAndBottom, IPrintable, ICutCopyPaste, IUndoRedo, ISVGExportable {

	private static final long serialVersionUID = 1L;

	@Deprecated
	private DrawNETPanels desktop;

	private static int openFrameCount = 0;
	private static int currentFrameCount = 0; // Daniele
	private static final int xOffset = 30, yOffset = 30;
	private JCheckBoxMenuItem frameMenuItem;
	private DrawNETWindowMenu windowMenu;
	private GraphFrameBottom windowBottom;
	private GraphFrameTop windowTop;

	// DrawNETGraphFrame Fram;
	private FrameLevelDataSet dataSet;

	private JScrollPane scrollDrawArea;

	private JPanel contentPane;

	public GraphFrameBottom getFrameBottom() {
		return windowBottom;
	}

	public void SelectThisFrame() {
		windowMenu.select(frameMenuItem);
		desktop.selectFrame(this, dataSet);
		// desktop.updateFrameLevelDataSet(DataSet);
	}

	public FrameLevelDataSet getDataSet() {
		return dataSet;
	}

	public void addToMenu(DrawNETWindowMenu menu) {
		frameMenuItem = new JCheckBoxMenuItem(getTitle());
		frameMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveToFront();
				SelectThisFrame();
			}
		});
		windowMenu = menu;
		menu.add(frameMenuItem);
		menu.select(frameMenuItem);
	}

	public DrawNETGraphFrame() throws DNException {
		this("Model #" + (openFrameCount + 1), new FrameLevelDataSet(), true); // Daniele
	}

	public DrawNETGraphFrame(String F, Model mo) throws DNException {
		this("Model #" + (openFrameCount + 1), new FrameLevelDataSet(F, mo,null), true); // Daniele
	}

	public DrawNETGraphFrame(String F, Model mo, File file) throws DNException {
		this(file.getName(), new FrameLevelDataSet(F, mo, file), false); // Daniele
	}

	public DrawNETGraphFrame(String windowName, String FilePath)
			throws DNException {
		this(
				windowName == null ? "Model #" + (openFrameCount + 1) : windowName, 
				new FrameLevelDataSet(FilePath), 
				windowName == null ? true : false // Daniele
		);
	}

	public DrawNETGraphFrame(String WindowName, FrameLevelDataSet Fld, boolean increment) {
		super(WindowName, true, // resizable
				true, // closable
				true, // maximizable
				true);// iconifiable

		if (increment) // Daniele
			openFrameCount++;
		currentFrameCount++; // Daniele

		// Fram = this;
		desktop = DrawNET.getApplication().getMainFrame().getDNDesktop();
		dataSet = Fld;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				CMD_Close.getInstance().execute();
			}

			public void internalFrameActivated(InternalFrameEvent e) {
				SelectThisFrame();
			}

			public void internalFrameDeactivated(InternalFrameEvent e) {
				windowMenu.select(null);
			}
		});

		// ...Create the GUI and put it in the window...

		// ...Then set the window size or call pack...
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		scrollDrawArea = dataSet.getCurrentDrawArea();

		contentPane.add(scrollDrawArea, BorderLayout.CENTER);

		windowBottom = new GraphFrameBottom();
		contentPane.add(windowBottom, BorderLayout.SOUTH);
		desktop.addFrame(this);

		windowTop = dataSet.getCurrentFrameTop();
		contentPane.add(windowTop, BorderLayout.NORTH);
		setContentPane(contentPane);
		setFrameIcon(new ImageIcon("icons/SubModel.gif"));
		setSize(600, 450);

		// Set the window's location.
		setLocation(xOffset * (currentFrameCount - 1) + 58, yOffset * (currentFrameCount - 1)); // Daniele
	}

	public void updateFrame(GraphFrameTop newTop, JScrollPane newCenter) {
		contentPane.remove(windowTop);
		contentPane.add(newTop, BorderLayout.NORTH);
		contentPane.remove(scrollDrawArea);
		contentPane.add(newCenter, BorderLayout.CENTER);
		contentPane.revalidate();
		contentPane.repaint();
		windowTop = newTop;
		scrollDrawArea = newCenter;
	}

	public void updateFrameTop(GraphFrameTop newTop) {
		contentPane.remove(windowTop);
		contentPane.add(newTop, BorderLayout.NORTH);
		contentPane.revalidate();
		contentPane.repaint();
		windowTop = newTop;
	}

	public void repaintDrawArea() {
		scrollDrawArea.repaint();
		desktop.getSelectedSubModelDataSet().getNavigatorPanel().repaintDrawArea();
	}

	public void closeFrame() {
		desktop.removeFrame(this);
		windowMenu.remove(frameMenuItem);
		dispose();
		currentFrameCount--; // Daniele
	}

	public boolean checkAndClose() {
		if (!dataSet.isChanged()) {
			closeFrame();
		} else {
			switch (JOptionPane.showConfirmDialog(null, "Model " + getTitle()
					+ " has not been saved\n" + "Save it before closing?",
					"Close window?", JOptionPane.YES_NO_CANCEL_OPTION)) {
			case 0:
				dataSet.save();
				if (!dataSet.isChanged())
					closeFrame();
				break;
			case 1:
				closeFrame();
				break;
			case 2:
				return false;
			}
		}
		return true;
	}

	public void setTitle(String title) {
		super.setTitle(title);
		frameMenuItem.setText(title);
		repaint();
	}

	public boolean hasBottom() {
		return true;
	}

	public void save() {
		if (dataSet != null) {
			dataSet.save();
		}
	}

	public void saveAs(boolean b) {
		if (dataSet != null) {
			dataSet.saveAs(b);
		}
	}

	public boolean canCut() {
		// TODO Auto-generated method stub
		return false;
	}

	public void cut() { //TODO If you want to implement the Cut
		System.out.println("Model Cut");
	}

	public boolean canCopy() {
		// TODO Auto-generated method stub
		return false;
	}

	public void copy() { //TODO If you want to implement the Copy
		System.out.println("Model Copy");
	}

	public boolean canCopyRef() {
		// TODO Auto-generated method stub
	    ElementSelection Sel = desktop.getCurrentSubModelSelection();

		return (Sel.size() == 1);
	}

	public void copyRef() {
	    ElementSelection Sel = desktop.getCurrentSubModelSelection();
	    Vector<ElementLevelDataSet> Els = Sel.getSelection();
		ElementLevelDataSet D = Els.get(0);
		
		dataSet.setCopiedReference(D.getElementInstance());
//System.out.println("Copy Reference: " + D.getElementInstance());
	}

	public boolean canPaste() {
		// TODO Auto-generated method stub
		return false;
	}

	public void paste() { //TODO If you want to implement the Paste
		System.out.println("Model Paste");
	}

	public boolean canUndo() {
		try {
			return desktop.getSelectedSubModelDataSet().getHistoryPanel().canUndo();
		} catch(Exception ex) {
			return false;
		}
	}
	
	public void undo() {
		SubModelLevelDataSet smlDataSet = desktop.getSelectedSubModelDataSet();
		if (smlDataSet != null) {
			smlDataSet.getHistoryPanel().doUndo();
			ElementSelection selection = desktop.getCurrentSubModelSelection();
			if (selection != null) {
				if (!selection.isEmpty()) {
					selection.clear();
					desktop.repaintDrawArea();
				}
			}
		}
	}

	public String getUndoCommandName() {
		try {
			return desktop.getSelectedSubModelDataSet().getHistoryPanel().getUndoCommandName();
		} catch(Exception ex) {
			return null;
		}
	}
	
	public boolean canRedo() {
		try {
			return desktop.getSelectedSubModelDataSet().getHistoryPanel().canRedo();
		} catch(Exception ex) {
			return false;
		}
	}
	
	public void redo() {
		SubModelLevelDataSet smlDataSet = desktop.getSelectedSubModelDataSet();
		if (smlDataSet != null) {
			smlDataSet.getHistoryPanel().doRedo();
			ElementSelection selection = desktop.getCurrentSubModelSelection();
			if (selection != null) {
				if (!selection.isEmpty()) {
					selection.clear();
					desktop.repaintDrawArea();
				}
			}
		}
	}

	public String getRedoCommandName() {
		try {
			return desktop.getSelectedSubModelDataSet().getHistoryPanel().getRedoCommandName();
		} catch(Exception ex) {
			return null;
		}
	}


	public boolean canPrint() {
		return true;
	}
	
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		//TODO: questo metodo e' solo un abbozzo; non riscala l'immagine.
		// Completarlo insieme alla realizzazione di un frame per l'anteprima
		// di stampa.
		try {
			if (pageIndex >= 1) {
				return Printable.NO_SUCH_PAGE;
			}
			Graphics2D g2 = (Graphics2D) graphics;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	
			scrollDrawArea.getViewport().paint(g2);
			return Printable.PAGE_EXISTS;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new PrinterException(ex.getMessage());
		}
	}

	@Override
	public boolean canExportSVG() {
		return true;
	}
	
	@Override
	public void printSVG(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		scrollDrawArea.getViewport().print(g2);
	}
	
}
