package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Parent;
import drawnet.lib.ddl.propertyvalues.StringPropertyValue;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.FixedArrayPropertyValue;

import javax.swing.JOptionPane;

import java.util.*;
import java.util.function.Function;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import drawnet.DrawNET;
import drawnet.modelgui.graphframe.DrawNETGraphFrame;
import drawnet.modelgui.menus.DrawNETWindowMenu;

import javax.swing.JFileChooser;

public class SolverFilterDBN2 extends SolverFilter
{

	private final String DESCR = "DBN ---> Octave";
	private final String OUTPUT_FILE = "OctaveScript_";

	private ElementInstance dbn = null;

	/**
	 * Constructor.
	 */
	public SolverFilterDBN2(){
		super();
		setDescription( DESCR );
	}

	/**
	 * Constructor.
	 *
	 * @param listener the listener to assign to this solver
	 */
	public SolverFilterDBN2( SolverListener listener ){
		super( listener );
		setDescription( DESCR );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see solvers.SolverLauncher#execute()
	 */

	
	private void print(String messaggio){
		System.out.print(messaggio);
	}

	private void mainVisit()
  	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
  		ElementType elementType;

		dbn = null;

		// CERCA DBN NEL MODELLO PRINCIPALE.
		enumeration = model_.getMainElement().subElementsEnum();
      		while (enumeration.hasMoreElements())
      		{
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
			
			if (elementType.getId().equals("DBN"))
				dbn = elementInstance;
		}
	}

	/**
	 * Creates an Octave script from the model and writes it to a file.
     *
     * @throws IOException if an I/O error occurs
     */
	private void createScriptOctave(String libraryPath) throws IOException{
		StringBuilder scriptBuilder = new StringBuilder(this.initializeOctave(libraryPath));

		scriptBuilder.append(this.extractNode())
					 .append(this.extractIntraSlice())
					 .append(this.extractInterSlice())
					 .append(this.createBnetAttackProbability())
					 .append(this.extractSliceOne())
					 .append(this.extractSliceTwo());

		writeFile(scriptBuilder.toString());	
		print("\ncreateScriptOctave: " + "script creato\n");
	}

	private String initializeOctave(String libraryPath){
		return "#!/usr/bin/octave -qf\nclear;\n\noldpwd=pwd;\ncd " +libraryPath +";\naddpath(genpath(pwd));\ncd(oldpwd);\nwarning off;\n\npkg load io\n";
	}

	/**
     * Extracts the nodes from the model and returns them as a string.
     *
     * @return the nodes as a string
     */
	private String extractNode() {
        List<String> hStates = getHStates();
        List<String> obs = getObs();

        StringBuilder nodeBuilder = new StringBuilder();

        if (!hStates.isEmpty()) {
            nodeBuilder.append("h_states = {");
            for (String hState : hStates) {
                nodeBuilder.append("'" + hState + "', ");
            }
            nodeBuilder.setLength(nodeBuilder.length() - 2); // Rimuove l'ultima virgola e spazio
            nodeBuilder.append("};\n\n");
        }

        if (!obs.isEmpty()) {
            nodeBuilder.append("obs = {");
            for (String ob : obs) {
                nodeBuilder.append("'" + ob + "', ");
            }
            nodeBuilder.setLength(nodeBuilder.length() - 2); // Rimuove l'ultima virgola e spazio
            nodeBuilder.append("};\n\n");

            nodeBuilder.append("names=[h_states, obs];\n\nn=length(names);\n\n");
        } else {
            nodeBuilder.append("names=[h_states];\n\nn=length(names);\n\n");
        }

        return nodeBuilder.toString();
    }

	/**
     * Extracts intra-slice arcs from the model and returns them as a string.
     *
     * @return the intra-slice arcs as a string
     */
	private String extractIntraSlice(){
		StringBuilder intracBuilder = new StringBuilder("\nintrac = {\n");

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
		while (enumeration.hasMoreElements()) {
			ElementInstance elementInstance = enumeration.nextElement();
			ElementType elementType = elementInstance.getElementType();

			if(elementType.getId().equals("Arc")){
				ElementRefPropertyValue fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
				ElementRefPropertyValue toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
				if(fromRef == null || toRef == null)
					continue;
				ElementInstance fromInstance = dbn.getSubElementByPath(fromRef.toString());
				ElementInstance toInstance = dbn.getSubElementByPath(toRef.toString());
				String from = fromInstance.getId();
				String to = toInstance.getId();

				intracBuilder.append("\t'").append(from).append("','").append(to).append("';\n");
			}
		}
	    
	    intracBuilder.delete(intracBuilder.lastIndexOf(";"), intracBuilder.lastIndexOf(";") + 1).append("};");

	    return intracBuilder.toString() + "\n";
	}

    /**
     * Extracts inter-slice arcs from the model and returns them as a string.
     *
     * @return the inter-slice arcs as a string
     */
	private String extractInterSlice(){
		StringBuilder intercBuilder = new StringBuilder("\ninterc = {\n");

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
		while (enumeration.hasMoreElements()) {
			ElementInstance elementInstance = enumeration.nextElement();
			ElementType elementType = elementInstance.getElementType();

			if(elementType.getId().equals("TemporalArc")){
				ElementRefPropertyValue fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
				ElementRefPropertyValue toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
				if(fromRef == null || toRef == null)
					continue;
				ElementInstance fromInstance = dbn.getSubElementByPath(fromRef.toString());
				ElementInstance toInstance = dbn.getSubElementByPath(toRef.toString());
				String from = fromInstance.getId();
				String to = toInstance.getId();

				intercBuilder.append("\t'").append(from).append("','").append(to).append("';\n");
			}
		}
	    
	    intercBuilder.delete(intercBuilder.lastIndexOf(";"), intercBuilder.lastIndexOf(";") + 1).append("};");

	    return intercBuilder.toString() + "\n\n";
	}

	private String createBnetAttackProbability(){
		StringBuilder stringBuilder = new StringBuilder("[intra, names] = mk_adj_mat(intrac, names, 1);\ninter = mk_adj_mat(interc, names, 0);\n");
		StringBuilder sizeNodeBuilder = new StringBuilder("ns = [");
		StringBuilder attackBuilder = new StringBuilder("bnet = mk_dbn(intra, inter, ns, 'names', names);\n\n");

		List<String> hStates = getHStates();
        List<String> obs = getObs();

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
		while (enumeration.hasMoreElements()) {
			ElementInstance elementInstance = enumeration.nextElement();
			ElementType elementType = elementInstance.getElementType();

			// Assumo che siano nodi binari e quindi possano avere solo due possibili configurazioni
			if (elementType.getId().contains("Node")) {
				
				if(elementType.getId().equals("NodeAND") ||elementType.getId().equals("NodeOR") || elementType.getId().equals("NodeXOR")){
					sizeNodeBuilder.append("2 ");
				}else {
					IntegerPropertyValue size = (IntegerPropertyValue)elementInstance.getPropertyValue("size");	
					if (size != null)
						sizeNodeBuilder.append(size.getValue() + " ");
				}

				StringPropertyValue meanTimeToCompromise = (StringPropertyValue)elementInstance.getPropertyValue("CPT");

		        if (hStates.contains(elementInstance.getId()) && !(elementType.getId().equals("NodeAND") || elementType.getId().equals("NodeOR")) && (meanTimeToCompromise != null && meanTimeToCompromise.toString().length() >= 1)){
		            attackBuilder.append(elementInstance.getId()).append("=bnet.names('").append(elementInstance.getId()).append("');\n");
		            attackBuilder.append("TMeanTech(").append(elementInstance.getId()).append(")= ").append(meanTimeToCompromise).append(";\n\n");
		        }else //if (obs.contains(elementInstance.getId()) || elementType.getId().equals("NodeAND") || elementType.getId().equals("NodeOR"))
					attackBuilder.append(elementInstance.getId()).append("=bnet.names('").append(elementInstance.getId()).append("');\n\n");
			}
		}

		sizeNodeBuilder.deleteCharAt(sizeNodeBuilder.length()-1).append("];\n\n");
		attackBuilder.append("DeltaT=1/(sum(1./TMeanTech(TMeanTech>0)));\nPrAttDef = DeltaT./TMeanTech; \n\n");


		return stringBuilder.toString() + sizeNodeBuilder.toString() + attackBuilder.toString();
	}
	
	/**
     * Extracts slice one details from the model and returns them as a string.
     *
     * @return the slice one details as a string
     */
	private String extractSliceOne(){
		StringBuilder sliceOneBuilder = new StringBuilder("%%%%%%%%% ------- slice 1 --------\n\n");

		List<String> hStates = getHStates();
        List<String> obs = getObs();
		HashMap<String, List<String>> IntraArcs = this.getIntraSliceArc();
		HashMap<String, String> sliceOneParents = getSliceOneParents(IntraArcs);

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
	    while (enumeration.hasMoreElements()) {
	    	ElementInstance elementInstance = enumeration.nextElement();
	        ElementType elementType = elementInstance.getElementType();

	        if (elementType.getId().contains("Node")) { 
				// Otteniamo parent non da getPropertyValue("parent") in quanto ci interessano i parent solo del primo slice
	        	String parent = sliceOneParents.getOrDefault(elementInstance.getId(), "");
	        	ArrayList<String> parentList = new ArrayList<>(Arrays.asList(parent.toString().split(";")));
				int numberParent = parent.isEmpty() ? 0 : parent.toString().split(";").length;
	       		String parentString = "'" + String.join("','", parentList) + "'";

				// Usa una variabile finale per memorizzare l'ID dell'elemento
            	final String elementId = elementInstance.getId();
            
            	// Verifica se elementId non è presente in nessuna delle liste di IntraArcs
            	boolean found = IntraArcs.values().stream().anyMatch(targets -> targets.contains(elementId));

				if (!found) {
						// Nello slice 1 anche se non si hanno intra arcs vengono inizializzate le cpt
						sliceOneBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 1 \n%parent order:{}\n");
						sliceOneBuilder.append("cpt(:,:)=[1.0, 0.0];\nbnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");
				}else{
					if (elementType.getId().equals("Node")) {
						sliceOneBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 1 \n%parent order:{").append(parent).append("}\n");

						if (hStates.contains(elementInstance.getId())){

							for(int i = 0; i<= numberParent; i++){
								// i = 2 -> binaryString = 10 
								String binaryString = Integer.toBinaryString(i);
								int maxLength = Math.max(binaryString.length(), numberParent);
								// Riempie con 0 iniziali fino a raggiungere maxLength
								binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
								binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

								if(i == 0)
									sliceOneBuilder.append("cpt(").append(binaryString).append(",:)=[").append("1.0").append(", ").append("0.0").append("];\n");
								else
									sliceOneBuilder.append("cpt(").append(binaryString).append(",:)=[").append("0.0").append(", ").append("1.0").append("];\n");
							}
							sliceOneBuilder.append("cpt1=mk_named_CPT({").append(parentString).append(",'").append(elementInstance.getId()).append("'},names, bnet.dag, cpt);\n");
							sliceOneBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt1);\nclear cpt;clear cpt1;\n\n");
						}else if (obs.contains(elementInstance.getId())){
							// Assumo che nel caso ci sia un nodo non tecnico, esso abbia associato una cpt
							StringPropertyValue cpt = (StringPropertyValue)elementInstance.getPropertyValue("CPT");
							if(cpt == null)
								continue;
							// Utilizzo della coda (stack) in modo tale da non avere problemi nell'estrazione dei dati 
							Queue<String> cptQueue = new LinkedList<>(new ArrayList<>(Arrays.asList(cpt.toString().split(";"))));

							for(int i = 0; i<= numberParent; i++){
								// i = 2 -> binaryString = 10 
								String binaryString = Integer.toBinaryString(i);
								int maxLength = Math.max(binaryString.length(), numberParent);
								// Riempie con 0 iniziali fino a raggiungere maxLength
								binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
								binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

								sliceOneBuilder.append("cpt(").append(binaryString).append(",:)=[").append(cptQueue.poll()).append(", ").append(cptQueue.poll()).append("];\n");
							}
							sliceOneBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");	
						}
					}else {
						sliceOneBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 1 \n%parent order:{").append(parent).append("}\n");
						if (elementType.getId().equals("NodeAND")) {
							sliceOneBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}= boolean_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'named', 'all');\n\n");
						}else{ //if (elementType.getId().equals("NodeOR")) {
							sliceOneBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}= boolean_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'named', 'any');\n\n");
						}
					}
				}
	       	}
	    }

	    return sliceOneBuilder.append("\n").toString();
	}

    /**
     * Extracts slice two details from the model and returns them as a string.
     *
     * @return the slice two details as a string
     */
	private String extractSliceTwo(){
		StringBuilder sliceTwoBuilder = new StringBuilder("%%%%%%%%% ------- slice 2 --------\n\n");

		List<String> hStates = getHStates();
		HashMap<String, String> InterArcs = this.getInterSliceArc();

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
	    while (enumeration.hasMoreElements()) {
	    	ElementInstance elementInstance = enumeration.nextElement();
	        ElementType elementType = elementInstance.getElementType();

	        if (elementType.getId().contains("Node")) { 
				// Otteniamo parent da getPropertyValue("parent") in quanto ci interessano i parent di tutti gli slice
	        	StringPropertyValue parent = (StringPropertyValue)elementInstance.getPropertyValue("parent");
	        	if(parent == null)
	        		continue;
				ArrayList<String> parentList = new ArrayList<>(Arrays.asList(parent.toString().split(";")));
	       		int numberParent = parentList.size();

	       		StringPropertyValue meanTimeToCompromise = (StringPropertyValue)elementInstance.getPropertyValue("CPT");           

	       		if (hStates.contains(elementInstance.getId()) && !(elementType.getId().equals("NodeAND") || elementType.getId().equals("NodeOR")) && (meanTimeToCompromise != null && meanTimeToCompromise.toString().length() >= 1)){
					sliceTwoBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 2 \n%parent order:{").append(parent).append("}\n");
					sliceTwoBuilder.append("CurPrAtt = PrAttDef(").append(elementInstance.getId()).append(");\n");

	       			if(numberParent > 1){ // Bisogna differenziare il caso con più parenti in quanto si dovrà utilizzare la funzione mk_named_CPT_inter
						String parentString = "'" + String.join("','", parentList) + "'";
						int totalNumber = (int)Math.pow(2,numberParent);
						for(int i = 0; i< totalNumber; i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), numberParent);
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

	              			if(i == 0)
							  	sliceTwoBuilder.append("cpt(").append(binaryString).append(",:)=[").append("1.0").append(", ").append("0.0").append("];\n");
	              			else if(i == 1)
							  	sliceTwoBuilder.append("cpt(").append(binaryString).append(",:)=[").append("1-CurPrAtt").append(", ").append("CurPrAtt").append("];\n");
	              			else
							  	sliceTwoBuilder.append("cpt(").append(binaryString).append(",:)=[").append("0.0").append(", ").append("1.0").append("];\n");

		        		}

		        		sliceTwoBuilder.append("cpt1=mk_named_CPT_inter({").append("'" + elementInstance.getId()+ "'").append(", ").append(parentString).append("},names, bnet.dag, cpt, [2]);\n");
		        		sliceTwoBuilder.append("bnet.CPD{bnet.eclass2(bnet.names('").append(elementInstance.getId()).append("'))}=tabular_CPD(bnet,n+bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt1);\nclear cpt;clear cpt1;\n\n");
					}else { //numberParent == 1
						for(int i = 0; i<= numberParent; i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), 1);
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

	              			if(i == 0)
							  	sliceTwoBuilder.append("cpt(").append(binaryString).append(",:)=[").append("1-CurPrAtt").append(", ").append("CurPrAtt").append("];\n");
	              			else 
							  	sliceTwoBuilder.append("cpt(").append(binaryString).append(",:)=[").append("0.0").append(", ").append("1.0").append("];\n");
		        		}
		            	sliceTwoBuilder.append("bnet.CPD{bnet.eclass2(bnet.names('").append(elementInstance.getId()).append("'))}=tabular_CPD(bnet,n+bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");
					}
	       		}
	       	}
	    }

	    return sliceTwoBuilder.append("\n").toString();
	}

	/**
	 * Extract intra-slice arcs from the model.
	 *
	 * @return a HashMap representing intra-slice arcs with source node IDs as keys and destination node IDs as values.
	 */
	private HashMap<String, List<String>> getIntraSliceArc(){
		HashMap<String, List<String>> intra = new HashMap<String, List<String>>();

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
		while (enumeration.hasMoreElements()) {
			ElementInstance elementInstance = enumeration.nextElement();
			ElementType elementType = elementInstance.getElementType();

			if(elementType.getId().equals("Arc")){
				ElementRefPropertyValue fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
				ElementRefPropertyValue toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
				if(fromRef == null || toRef == null)
					continue;
				ElementInstance fromInstance = dbn.getSubElementByPath(fromRef.toString());
				ElementInstance toInstance = dbn.getSubElementByPath(toRef.toString());
				String from = fromInstance.getId();
				String to = toInstance.getId();

				intra.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
			}
		}

		return intra;
	}

	/**
	 * Extract inter-slice arcs from the model.
	 *
	 * @return a HashMap representing inter-slice arcs with source node IDs as keys and destination node IDs as values.
	 */
	private HashMap<String, String> getInterSliceArc(){
		HashMap<String, String> inter = new HashMap<String, String>();

		Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
		while (enumeration.hasMoreElements()) {
			ElementInstance elementInstance = enumeration.nextElement();
			ElementType elementType = elementInstance.getElementType();

			if(elementType.getId().equals("TemporalArc")){
				// Riferimento ai nodi di partenza e arrivo dell'arco temporale del secondo slice
				ElementRefPropertyValue fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
				ElementRefPropertyValue toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
				if(fromRef == null || toRef == null)
					continue;
				ElementInstance fromInstance = dbn.getSubElementByPath(fromRef.toString());
				ElementInstance toInstance = dbn.getSubElementByPath(toRef.toString());
				String from = fromInstance.getId();
				String to = toInstance.getId();
				
				inter.put(from,to);
			}
		}
		return inter;
	}

	/**
	 * Extract the parents for each node from the given intra-slice arcs.
	 *
	 * @param intraArcs a HashMap representing intra-slice arcs where the keys are source node IDs and the values are lists of destination node IDs.
	 * @return a HashMap where the keys are node IDs and the values are strings representing the parent node IDs separated by semicolons.
	 */
	private HashMap<String, String> getSliceOneParents(HashMap<String, List<String>> intraArcs) {
	    // Creiamo una HashMap dove ogni nodo ha la lista dei suoi genitori
	    HashMap<String, List<String>> parentMap = new HashMap<>();

	    // Iteriamo sugli archi intra-slice
	    for (Map.Entry<String, List<String>> entry : intraArcs.entrySet()) {
	        String parent = entry.getKey();
	        List<String> children = entry.getValue();

	        // Aggiungiamo il parent alla lista dei genitori per ogni child
	        for (String child : children) {
	            parentMap.computeIfAbsent(child, k -> new ArrayList<>()).add(parent);
	        }
	    }

	    // Convertiamo la lista di genitori in una stringa separata da ";"
	    HashMap<String, String> parentStringMap = new HashMap<>();
	    for (Map.Entry<String, List<String>> entry : parentMap.entrySet()) {
	        String child = entry.getKey();
	        List<String> parents = entry.getValue();
	        parentStringMap.put(child, String.join(";", parents));
	    }

	    return parentStringMap;
	}

	/**
     * Extracts the h_states nodes from the model.
     *
     * @return the list of h_states nodes
     */
    private List<String> getHStates() {
        List<String> hStates = new ArrayList<>();

        Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
        while (enumeration.hasMoreElements()) {
            ElementInstance elementInstance = enumeration.nextElement();
            ElementType elementType = elementInstance.getElementType();

            if (elementType.getId().contains("Node")) {
                StringPropertyValue observed = (StringPropertyValue) elementInstance.getPropertyValue("observed");

                if (observed.toString().equals("false")) {
                    hStates.add(elementInstance.getId());
                }
            }
        }
        return hStates;
    }

    /**
     * Extracts the obs nodes from the model.
     *
     * @return the list of obs nodes
     */
    private List<String> getObs() {
        List<String> obs = new ArrayList<>();

        Enumeration<ElementInstance> enumeration = dbn.subElementsEnum();
        while (enumeration.hasMoreElements()) {
            ElementInstance elementInstance = enumeration.nextElement();
            ElementType elementType = elementInstance.getElementType();

            if (elementType.getId().contains("Node")) {
                StringPropertyValue observed = (StringPropertyValue) elementInstance.getPropertyValue("observed");

                if (observed.toString().equals("true")) {
                    obs.add(elementInstance.getId());
                }
            }
        }
        return obs;
    }

	/**
	 * Executes the main process for the current model.
	 *
	 * @return true if the process completes successfully, false if an IOException occurs.
	 */	
	public boolean execute() {
	    try {
	        // Apri un JFileChooser configurato per la selezione della cartella contenete la libreria FullBNT
	        JFileChooser folderChooser = new JFileChooser();
	        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Permetti solo la selezione di cartelle

			folderChooser.setDialogTitle("Seleziona la cartella contenente la libreria FullBNT");

	        int returnVal = folderChooser.showOpenDialog(null);
	        
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            // Ottieni il percorso della cartella selezionata
	            File selectedFolder = folderChooser.getSelectedFile();
	            String libraryPath = selectedFolder.getAbsolutePath(); 

	            this.mainVisit();
	            if (dbn != null) {
	                this.createScriptOctave(libraryPath);
	                JOptionPane.showMessageDialog(null, "DBN saved in file " + OUTPUT_FILE + this.getFrameTitle());
	            }
	            
	            return true;
	        } else {
	            // Se l'utente annulla la selezione, il metodo ritorna false
	            return false;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}


    /**
     * Writes the given content to a file.
     *
     * @param content the content to write to the file
     * @throws IOException if an I/O error occurs
     */
	private void writeFile(String content) throws IOException{
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE+this.getFrameTitle()))){
			bw.write(content);
		}
	}
	
	private String getFrameTitle(){
	    DrawNETGraphFrame frame1 = (DrawNETGraphFrame) DrawNET.getApplication().getMainFrame().getDNDesktop().getSelectedFrame();
        String title = frame1.getTitle();

	   // Rimuove l'ultimo carattere
	    if (title != null && title.length() > 0) {
	        title = title.substring(0, title.length() - 1);
	    }

	    return title;
	}
}