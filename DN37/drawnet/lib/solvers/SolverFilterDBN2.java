package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.Parent;
import drawnet.lib.ddl.propertyvalues.StringPropertyValue;
import drawnet.lib.ddl.propertyvalues.IntegerPropertyValue;
import drawnet.lib.ddl.propertyvalues.ElementRefPropertyValue;
import drawnet.lib.ddl.propertyvalues.FixedArrayPropertyValue;

import javax.swing.JOptionPane;

import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.Queue;
import java.util.LinkedList;
import java.util.function.Function;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SolverFilterDBN2 extends SolverFilter
{

	private final String DESCR = "DBN ---> Octave";
	private final String OUTPUT_FILE = "OctaveScript_testDBN.m";

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

	/*
	private void print(String messaggio){
		System.out.print(messaggio);
	}
	*/

	/*
	private void mainVisit(){
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;
		StringPropertyValue cpt;
		IntegerPropertyValue size;

		enumeration = model_.getMainElement().subElementsEnum();
      	while (enumeration.hasMoreElements())
      	{
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
			
			print("\nmainVisit " + elementInstance.getId() + ": " + elementType.getId());
			
			if (elementType.getId().contains("Node"))
			{
				print(" ---> e' un nodo");

				cpt = (StringPropertyValue)elementInstance.getPropertyValue("CPT");
				if (cpt != null)
					print(" CPT " + cpt.toString());

				size = (IntegerPropertyValue)elementInstance.getPropertyValue("size");	
				if (size != null)
					print(" size " + size.toString());
			}
			if (elementType.getId().contains("Arc"))
				print(" ---> e' un arco");	
		}
	}
	*/

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

	private void createScriptOctave() throws IOException{
		StringBuilder scriptBuilder = new StringBuilder();
		scriptBuilder.append("#!/usr/bin/octave -qf\nclear;\n\noldpwd=pwd;\ncd /home/nicomariv/FullBNT-1.0.4Octave/FullBNT-1.0.4;\naddpath(genpath(pwd));\ncd(oldpwd);\nwarning off;\n\npkg load io\n");
		scriptBuilder.append(this.extractNode()).append("names=[node];\n\nn=length(names);\n\n").append(this.extractIntrSlice());
		scriptBuilder.append("[intra, names] = mk_adj_mat(intrac, names, 1);\ninter = mk_adj_mat(interc, names, 0);\nns = [");
		
		int sizeNode = this.sizeNode();
		for (int i = 0; i < sizeNode; i++) {
            scriptBuilder.append("2 ");
        }

        scriptBuilder.deleteCharAt(scriptBuilder.length()-1).append("];\n\nbnet = mk_dbn(intra, inter, ns, 'names', names);\n\n");
		scriptBuilder.append(this.extractNodeNames());
		scriptBuilder.append(this.extractSliceOne());
		scriptBuilder.append(this.extractSliceTwo());

		writeFile(scriptBuilder.toString());	
		//print("\ncreateScriptOctave: " + "script creato\n");
	}

	private String extractNode() {
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
		ElementType elementType;


		enumeration = dbn.subElementsEnum();
		StringBuilder nodeBuilder = new StringBuilder();

		nodeBuilder.append("\nnode = {\n\t");
		while (enumeration.hasMoreElements()) {
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();

			if (elementType.getId().contains("Node")) {
				nodeBuilder.append("'" + elementInstance.getId() + "',");
			}
		}
		nodeBuilder.deleteCharAt(nodeBuilder.length()-1).append("\n};\n\n");

		return nodeBuilder.toString();
	}

	private String extractIntrSlice() {
	    Enumeration<ElementInstance> enumeration;
	    ElementInstance elementInstance, fromInstance, toInstance;
	    ElementType elementType;
	    ElementRefPropertyValue fromRef, toRef;
	    String from, to;

	    enumeration = dbn.subElementsEnum();

	    StringBuilder risIntra = new StringBuilder("\nintrac = {\n");
	    StringBuilder risIntre = new StringBuilder("interc = {\n");

	    while (enumeration.hasMoreElements()) {
	        elementInstance = enumeration.nextElement();
	        elementType = elementInstance.getElementType();

	        if (elementType.getId().contains("Arc")) {
	            fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
	            toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
	            fromInstance = dbn.getSubElementByPath(fromRef.toString());
	            toInstance = dbn.getSubElementByPath(toRef.toString());
	            from = fromInstance.getId();
	            to = toInstance.getId();
	            if (from.equals(to)) {
	                risIntre.append("\t'").append(from).append("','").append(to).append("';\n");
	            } else {
	                risIntra.append("\t'").append(from).append("','").append(to).append("';\n");
	            }
	        }
	    }
	    
	    risIntra.append("};");
	    risIntre.append("};\n");

	    return risIntra.toString() + "\n\n" + risIntre.toString() + "\n";
	}

	private String extractNodeNames() {
	    Enumeration<ElementInstance> enumeration;
	    ElementInstance elementInstance;
	    ElementType elementType;

	    StringBuilder nodeDataBuilder = new StringBuilder();
	    enumeration = dbn.subElementsEnum();
	    while (enumeration.hasMoreElements()) {
	        elementInstance = enumeration.nextElement();
	        elementType = elementInstance.getElementType();

	        if (elementType.getId().contains("Node")) {
	        	nodeDataBuilder.append(elementInstance.getId()).append("=bnet.names('").append(elementInstance.getId()).append("');\n\n");
	        }
	    }

	    return nodeDataBuilder.toString();
	}

	private String extractSliceOne(){
		Enumeration<ElementInstance> enumeration;
	    ElementInstance elementInstance;
	    ElementType elementType;
	    StringPropertyValue parent, cpt;
	    ArrayList<String> parentArray, cptArray;
	    String parentString;
	    int totalNumber;

	    StringBuilder cpdBuilder = new StringBuilder();
	    cpdBuilder.append("%%%%%%%%% ------- slice 1 --------\n\n");

	    enumeration = dbn.subElementsEnum();
	    while (enumeration.hasMoreElements()) {
	    	elementInstance = enumeration.nextElement();
	        elementType = elementInstance.getElementType();

	       	if (elementType.getId().contains("Node")) { 
	       		parent = (StringPropertyValue)elementInstance.getPropertyValue("parent");
	       		parentArray = new ArrayList<>(Arrays.asList(parent.toString().split(";")));
	       		
	       		if (parentArray.size() == 0 || (parentArray.size() == 1 && elementInstance.getId().contains(parent.toString()))){
					cpdBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 1 \n%parent order:{}\n");
					cpdBuilder.append("cpt(:,:)=[1.0, 0.0];\nbnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");
				}else{
					cpdBuilder.append("%node ").append(elementInstance.getId()).append("(id=").append(elementInstance.getId()).append(") slice 1 \n%parent order:{").append(String.join(",", parentArray)).append("}\n");
					
					parentString = "'" + String.join("','", parentArray) + "'";
					if(elementType.getId().contains("NodeAND")) {
						totalNumber = (int)Math.pow(2,parentArray.size());
						for(int i = 0; i<totalNumber; i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), parentArray.size());
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

							if(i == 0){
								cpdBuilder.append("cpt(").append(binaryString).append(",:)=[1.0, 0.0];\n");
							}else{
								cpdBuilder.append("cpt(").append(binaryString).append(",:)=[0.0, 1.0];\n");
							}
		        		}
		        		cpdBuilder.append("cpt1=mk_named_CPT({").append(parentString).append(", '").append(elementInstance.getId()).append("'},names, bnet.dag, cpt);\n");
		        		cpdBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt1);\nclear cpt;clear cpt1;\n\n");
					}else{
						cpt = (StringPropertyValue)elementInstance.getPropertyValue("CPT");
						cptArray = new ArrayList<>(Arrays.asList(cpt.toString().split(";")));

						Queue<String> cptQueue = new LinkedList<>(cptArray);

						for(int i = 0; i<= parentArray.size(); i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), parentArray.size());
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

	              			cpdBuilder.append("cpt(").append(binaryString).append(",:)=[").append(cptQueue.poll()).append(", ").append(cptQueue.poll()).append("];\n");
		        		}
		        		cpdBuilder.append("bnet.CPD{bnet.names('").append(elementInstance.getId()).append("')}=tabular_CPD(bnet,bnet.names('").append(elementInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");
					}
				}
	       	}
	    }

	    return cpdBuilder.append("\n").toString();
	}

	private String extractSliceTwo(){
		Enumeration<ElementInstance> enumeration;
	    ElementInstance elementInstance, fromInstance, toInstance;
	    ElementRefPropertyValue fromRef, toRef;
	    ElementType elementType;
	    StringPropertyValue parent, cpt;
	    ArrayList<String> parentArray, cptArray;
	    String parentString;
	    int totalNumber;

	    StringBuilder cpdBuilder = new StringBuilder();
	    cpdBuilder.append("%%%%%%%%% ------- slice 2 --------\n\n");

	    enumeration = dbn.subElementsEnum();
	    while (enumeration.hasMoreElements()) {
	    	elementInstance = enumeration.nextElement();
	        elementType = elementInstance.getElementType();

	        if (elementType.getId().contains("Arc")) {
	            fromRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("from");
	            toRef = (ElementRefPropertyValue)elementInstance.getPropertyValue("to");
	            fromInstance = dbn.getSubElementByPath(fromRef.toString());
	            toInstance = dbn.getSubElementByPath(toRef.toString());
	            if(fromInstance.getId().equals(toInstance.getId())) {
	            	parent = (StringPropertyValue)fromInstance.getPropertyValue("parent");

	            	if(parent != null && !parent.toString().equals(fromInstance.getId())){
	            		parentArray = new ArrayList<>();
	            		parentArray.add(fromInstance.getId());

	            		String[] values = parent.toString().split(";");
						for (String value : values) {
						    parentArray.add(value);
						}
	            		cpdBuilder.append("%node ").append(fromInstance.getId()).append("(id=").append(fromInstance.getId()).append(") slice 2 \n%parent order:{").append(String.join(",",parentArray)).append("}\n");

	            		cpt = (StringPropertyValue)fromInstance.getPropertyValue("CPT");
						cptArray = new ArrayList<>(Arrays.asList(cpt.toString().split(";")));

						Queue<String> cptQueue = new LinkedList<>(cptArray);
						parentString = "'" + String.join("','", parentArray) + "'";
						totalNumber = (int)Math.pow(2,parentArray.size());
						for(int i = 0; i< totalNumber; i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), parentArray.size());
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

	              			Function<String, String> pollOrDefault = (s) -> s == null ? "0" : s;

	              			cpdBuilder.append("cpt(").append(binaryString).append(",:)=[").append(pollOrDefault.apply(cptQueue.poll())).append(", ").append(pollOrDefault.apply(cptQueue.poll())).append("];\n");
		        		}

		        		cpdBuilder.append("cpt1=mk_named_CPT_inter({").append(parentString).append(", '").append(fromInstance.getId()).append("'},names, bnet.dag, cpt, [2]);\n");
		        		cpdBuilder.append("bnet.CPD{bnet.eclass2(bnet.names('").append(fromInstance.getId()).append("'))}=tabular_CPD(bnet,n+bnet.names('").append(fromInstance.getId()).append("'),'CPT',cpt1);\nclear cpt;clear cpt1;\n\n");
	            	}else{
	            		cpdBuilder.append("%node ").append(fromInstance.getId()).append("(id=").append(fromInstance.getId()).append(") slice 2 \n%parent order:{").append(fromInstance.getId()).append("}\n");
	            		cpt = (StringPropertyValue)fromInstance.getPropertyValue("CPT");
						cptArray = new ArrayList<>(Arrays.asList(cpt.toString().split(";")));

						Queue<String> cptQueue = new LinkedList<>(cptArray);

						for(int i = 0; i<= 1; i++){
		        			String binaryString = Integer.toBinaryString(i);
	                    	int maxLength = Math.max(binaryString.length(), 1);
	                    	binaryString = String.format("%0" + maxLength + "d", Integer.parseInt(binaryString));
	              			binaryString = String.join(",", binaryString.split("")).replace('1','2').replace('0','1');

	              			cpdBuilder.append("cpt(").append(binaryString).append(",:)=[").append(cptQueue.poll()).append(", ").append(cptQueue.poll()).append("];\n");
		        		}
		            	cpdBuilder.append("bnet.CPD{bnet.eclass2(bnet.names('").append(fromInstance.getId()).append("'))}=tabular_CPD(bnet,n+bnet.names('").append(fromInstance.getId()).append("'),'CPT',cpt);\nclear cpt;\n\n");
	            	}	               	
	            }
	        }
	    }

	    return cpdBuilder.append("\n").toString();
	}

	private int sizeNode(){
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance, parentInstance;
		ElementType elementType;

		enumeration = dbn.subElementsEnum();
		int i = 0;

		while (enumeration.hasMoreElements()) {
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();

			if (elementType.getId().contains("Node")) {
				i++;
			}
		}

		return i;
	}

	public boolean execute(){
		try {
			this.mainVisit();
			if (dbn != null)
			{
				this.createScriptOctave();
				JOptionPane.showMessageDialog(null, "DBN saved in file " + OUTPUT_FILE);
			}
			//print("\nSecuriDN ---> Octave eseguito\n");
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void writeFile(String content) throws IOException{
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE))){
			bw.write(content);
		}
	}
	

}
