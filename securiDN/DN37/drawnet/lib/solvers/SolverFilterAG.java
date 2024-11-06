package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;
import drawnet.lib.ddl.propertyvalues.StringPropertyValue;
import drawnet.lib.ddl.propertyvalues.BooleanPropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.text.ElementIterator;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class SolverFilterAG extends SolverFilter{

	private final String DESCR = "AG ---> JSON";

	private ElementInstance ag = null;
	private ArrayList<String> edges = new ArrayList<String>();
	private ArrayList<String[]> nodes = new ArrayList<String[]>();


	/**
	 * Constructor.
	 */
	public SolverFilterAG(){
		super();
		setDescription( DESCR );
	}

	/**
	 * Constructor.
	 *
	 * @param listener the listener to assign to this solver
	 */
	public SolverFilterAG( SolverListener listener )
	{
		super( listener );
		setDescription( DESCR );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see solvers.SolverLauncher#execute()
	 */

	private void print(String messaggio)
	{
		System.out.print(messaggio);
	}


	private void mainVisit()
	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;

		enumeration = model_.getMainElement().subElementsEnum();
      		while (enumeration.hasMoreElements())
      		{
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
			
			print("\nmainVisit " + elementInstance.getId() + ": " + elementType.getId());
			
			if (elementType.getId().equals("AG"))
			{
				ag = elementInstance;
				print(" ---> e' un AG");
			}
		}
		print("\n");
	}
	

	//vengono eliminati dal modello gli archi entranti in nodi di tipo analytics
	//vengono eliminati dal modello i nodi di tipo analytics 
	//vengono eliminati dal modello i nodi di tipo G_Layer e G_iconDef
	//(vengono salvati in un array e poi vengono eliminati alla fine per preservare puntatori)
	private void removeAnalytics()
	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;
		ArrayList<String> analyticsNodes = new ArrayList<String>();

		enumeration = ag.subElementsEnum();

		while (enumeration.hasMoreElements()){

			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
		
			if(elementType.getId().equals("Arc")){
				String destination_node_string = elementInstance.getPropertyValue("to").toString();
				ElementInstance destination_node = ag.getSubElement(destination_node_string);
				if(destination_node.getElementType().getId().equals("Analytics")){
					ag.removeSubElement(elementInstance.getId());
				}		
			}

			if(elementType.getId().equals("Analytics")||elementType.getId().equals("G_Layer")||elementType.getId().equals("G_iconDef")){
				analyticsNodes.add(elementInstance.getId());
			}
		
		}

		for (String s : analyticsNodes) {
			ag.removeSubElement(s);
		}
	}

	
	//scorre il grado e inserisce tutti i nodi nell'ArrayList nodes e tutti gli archi nell'arrayList edges
	private void getEdgesAndNodes() {
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
		ElementType elementType;
	
		enumeration = ag.subElementsEnum();
	
		while (enumeration.hasMoreElements()) {
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
	
			if (elementType.getId().equals("Arc")) {
				edges.add(elementInstance.getId());
			} else if (elementType.getId().equals("Node") || elementType.getId().equals("NodeOR") || elementType.getId().equals("NodeAND")) {
				String[] tmp = new String[3];
				tmp[0] = elementInstance.getId();
				
				// Recupera l'IP se il nodo è di tipo 'Node', altrimenti lascia vuoto
				tmp[1] = elementType.getId().equals("Node") ? elementInstance.getPropertyValue("IP").toString() : "";
	
				String additionalAttribute = "None";

				// Controlla se il valore di initial_technique è True aggiunge Attacker come terzo elemento
				BooleanPropertyValue initialTechnique = (BooleanPropertyValue) elementInstance.getPropertyValue("initial");
				if (initialTechnique != null && initialTechnique.getValue()) {
					additionalAttribute = "Attacker";
				}

				// Controlla se il valore di final_technique è True aggiunge Target come terzo elemento
				BooleanPropertyValue finalTechnique = (BooleanPropertyValue) elementInstance.getPropertyValue("final");
				if (finalTechnique != null && finalTechnique.getValue()) {
					additionalAttribute = "Target";
				}

				tmp[2] = additionalAttribute;
				nodes.add(tmp);
			}
		}
	}
	

	//Vengono catalogati come "nodi con genitori" tutti i nodi che hanno almeno un arco entrante
	//Facendo la differenza tra l'insieme di tutti i nodi e dei "nodi con genitori" si trovano quelli senza genitori
	private ArrayList<String[]> getNodesWithoutParents(ArrayList<String> edges , ArrayList<String[]> nodes){
		
		ArrayList<String> nodesWithParents = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String destination_node = edge.getPropertyValue("to").toString();

			if(!nodesWithParents.contains(destination_node)){
				nodesWithParents.add(destination_node);
			}
		}

		ArrayList<String[]> nodesWithoutParents = new ArrayList<String[]>();

		for (String[] node: nodes){
			String nodeId = node[0];
			if(!nodesWithParents.contains(nodeId)){
				nodesWithoutParents.add(node);
			}
		}

		return nodesWithoutParents;
	}


	//restituisce true se il nodo passato in input è un nodo di tipo AND, false altrimenti
	private boolean isANDnode(String node){
		ElementInstance n = ag.getSubElement(node);
		if (n.getElementType().getId().equals("NodeAND")){
			return true;
		}
		return false;
	}


	//restituisce true se il nodo passato in input è un nodo di tipo OR, false altrimenti
	private boolean isORnode(String node){
		ElementInstance n = ag.getSubElement(node);
		if (n.getElementType().getId().equals("NodeOR")){
			return true;
		}
		return false;
	}


	//prende in input un nodo e una lista di archi e restituisce un array contenente gli archi uscenti da quel nodo
	private ArrayList<String> getOutgoingEdges(String node, ArrayList<String> edges){

		ArrayList<String> outgoingEdges = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String starting_node = edge.getPropertyValue("from").toString();

			if(starting_node.equals(node)){
				outgoingEdges.add(e);
			}
		}

		return outgoingEdges;
	}


	//prende in input un nodo e una lista di archi e restituisce un array contenente gli archi entranti in quel nodo
	private ArrayList<String> getIncomingEdges(String node, ArrayList<String> edges){

		ArrayList<String> incomingEdges = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String destination_node = edge.getPropertyValue("to").toString();

			if(destination_node.equals(node)){
				incomingEdges.add(e);
			}
		}

		return incomingEdges;
	}


	//Preso l'elenco degli archi uscenti da un nodo restituisce l'elenco dei figli
	private ArrayList<String> getChildrenNodes(ArrayList<String> outgoingEdges){

		ArrayList<String> childrenNodes = new ArrayList<String>();

			for(String e: outgoingEdges){
				ElementInstance edge = ag.getSubElement(e);
				childrenNodes.add(edge.getPropertyValue("to").toString());
			}

		return childrenNodes;
	}


	//Preso l'elenco degli archi entanti in un nodo restituisce l'elenco dei genitori
	private ArrayList<String> getParentsNodes(ArrayList<String> incomingEdges){

		ArrayList<String> parentsNodes = new ArrayList<String>();

			for(String e: incomingEdges){
				ElementInstance edge = ag.getSubElement(e);
				parentsNodes.add(edge.getPropertyValue("from").toString());
			}

		return parentsNodes;
	}


	//restotuisce l'ordine topologico dell'attack graph
	private ArrayList<String[]> getTopologicalOrder() {
		this.getEdgesAndNodes();

		@SuppressWarnings("unchecked")
		ArrayList<String> edges = (ArrayList<String>) this.edges.clone();

		@SuppressWarnings("unchecked")
		ArrayList<String[]> nodes = (ArrayList<String[]>) this.nodes.clone();
	
		String[] start= getNodesWithoutParents(edges, nodes).get(0);
		String startId = start[0];
		
		ArrayList<String> visited = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();
		ArrayList<String[]> topologicalOrder = new ArrayList<String[]>();

		stack.add(startId);

		while (!stack.isEmpty()) {
			String currentNodeId = stack.peek(); 

			if (!visited.contains(currentNodeId)) {
				visited.add(currentNodeId);

				ArrayList<String> childrenNodesIds = getChildrenNodes(getOutgoingEdges(currentNodeId, edges));

				for (String nodeId : childrenNodesIds) {
					if (!visited.contains(nodeId))  stack.add(nodeId);
				}

			} 
			else {
				stack.pop(); // Rimuove l'elemento dopo aver visitato tutti i figli
				if (!topologicalOrder.stream().anyMatch(node->node[0].equals(currentNodeId))) {
					for(String[] node: nodes){
						if(node[0].equals(currentNodeId)){
							topologicalOrder.add(0, node);
							break;

						}
					}
				}
			}
		}

		return topologicalOrder;
	}


	//Prende in input una lista di nodi e restituisce la stessa lista ma senza i nodi che sono operatori logici
	private ArrayList<String[]> removeLogicalNodes(ArrayList<String[]> topologicalOrder) {

		ArrayList<String[]> orderWithoutLogicalNodes = new ArrayList<String[]>();

		for(String[] node: topologicalOrder){
			if(!this.isANDnode(node[0]) && !this.isORnode(node[0])){
				orderWithoutLogicalNodes.add(node);
			}
		}

		return orderWithoutLogicalNodes;
	}


	//Prende in input un array di stringhe e restituisce un nuovo Array di stringhe 
	//nel quale ogni elemento è costituito solo dalla parte che segue _ 
	private ArrayList<String[]> removePrefix(ArrayList<String[]> topologicalOrder){

		ArrayList<String[]> orderWithoutPrefix = new ArrayList<String[]>();

		for(String[] node: topologicalOrder){
			String[] splittedId = node[0].split("_");
			orderWithoutPrefix.add(new String[]{splittedId[1],node[1],node[2]});
		}

		return orderWithoutPrefix;

	}
	
	
	private void writeJson(ArrayList<String[]> order, String filename) {
		ArrayList<Map<String, String>> attackSequence = new ArrayList<>();
	
		// Popola la lista con gli oggetti, ognuno contenente "attack_name", "IP" e "additional_attribute"
		for (String[] node : order) {
			Map<String, String> attackEntry = new HashMap<>();
			attackEntry.put("attack_name", node[0]); 
			attackEntry.put("IP", node[1]); 
			attackEntry.put("additional_attribute", node[2]); 
			attackSequence.add(attackEntry);
		}
	
		// Crea l'oggetto JSON principale con chiave "attack_sequence"
		JsonObject jsonObject = new JsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		jsonObject.add("attack_sequence", gson.toJsonTree(attackSequence));
	
		// Scrive l'oggetto JSON nel file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			writer.write(gson.toJson(jsonObject));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean execute()
	{

		String filename = "Attack_sequence.json";

		this.mainVisit();
		this.removeAnalytics();

		ArrayList<String[]> topologicalOrder = this.getTopologicalOrder();
		topologicalOrder = this.removeLogicalNodes(topologicalOrder);
		topologicalOrder = this.removePrefix(topologicalOrder);

	
		System.out.println("TOPOLOGICAL ORDER:");
		for(String[] node : topologicalOrder) {
			System.out.println(node[0] + "   IP:   " + node[1]);
		}

		print("\nAG ---> JSON eseguito\n");

		this.writeJson(topologicalOrder, filename);

		
		return true;
	}
}
