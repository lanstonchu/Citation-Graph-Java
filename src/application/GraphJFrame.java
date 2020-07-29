package application;


import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * The GraphJFrame class provides canvas on which a citation graph can be drawn  
 */
public class GraphJFrame extends JFrame {

	final static int defaultSizeX = 1000;
	final static int defaultSizeY = 800;
	// final static int defaultLocationX = 300;
	// final static int defaultLocationY = 50;
	int numCreatedGraph;

	public GraphJFrame () {
		numCreatedGraph = 0;
	}

	public void drawJFrame (ArrayList<Integer> fromVertices, ArrayList<Integer> toVertices, 
			ArrayList<String> vertexLabels, ArrayList<Integer> validId, boolean showSingletonFlag) {
		drawJFrame(defaultSizeX, defaultSizeY, fromVertices, toVertices, vertexLabels, validId, showSingletonFlag); // call another constructor
		return;
	}

    /**
    * To draw citation graph 
    *
    * @param  sizeX   The desired width of graph window
    * @param  sizeY   The desired height of graph window
    * @param  fromVertices   The from-side-vertices of the edges of the graph (for papers's citational relationship)
    * @param  toVertices   The to-side-vertices of the edges of the graph (for papers's citational relationship)
    * @param  vertexLabels   The text of vertices labels (for selected papers)
    * @param  validId   The list of ID of the selected papers
    * @param  showSingletonFlag   Whether we want to show the papers without any citational relationship
    *  
    */
	public void drawJFrame (int sizeX, int sizeY, ArrayList<Integer> fromVertices, ArrayList<Integer> toVertices, 
			ArrayList<String> vertexLabels, ArrayList<Integer> validId, boolean showSingletonFlag) {

		if (fromVertices.size()!=toVertices.size()) {
			throw new RuntimeException("Error: the numbers of from-vertices and to-vertices don't match.");				
		}

		if (fromVertices.size()!=0) {
			if (Collections.max(fromVertices) > vertexLabels.size() || Collections.max(toVertices) > vertexLabels.size()) {
				throw new RuntimeException("Error: the ID in from/to vertices shouldn't exceed the size of vertexLabels");
			}	
		}		

		if (validId.size()!= 0) {
			if (Collections.max(validId) > vertexLabels.size()) {
				throw new RuntimeException("Error: the maxID shouldn't exceed the size of vertexLabels");
			}	
		}

		this.getContentPane().removeAll();
		this.repaint();

		final mxGraph graph = new mxGraph();
		// graph.setAutoSizeCells(true); // auto resize?

		ArrayList<Integer> vUnionFromTo = union(fromVertices,toVertices);

		if (!validId.containsAll(vUnionFromTo)) {
			throw new RuntimeException("Error: some ID in the from/to vertices are not valid ID");
		}

		Object defaultParent = graph.getDefaultParent();

		// Graph contents
		graph.getModel().beginUpdate();
		try {

			ArrayList<Integer> verticeSet = new ArrayList<Integer>();
			if (showSingletonFlag == true) { // show singleton
				verticeSet = validId;
			}else if (showSingletonFlag == false) { // hide singleton
				verticeSet = vUnionFromTo;
			}else {
				throw new RuntimeException("shouldn't reach this line");
			}


			// Add all vertices
			for (int vertex : verticeSet) {
				String vertexID = "V" + Integer.toString(vertex); // don't remove "V" as per https://stackoverflow.com/questions/32663261/mxgraph-get-vertex-by-id
				String vertexLabel = vertexLabels.get(vertex - 1); // vertix ID starting from 1, while index of array starting from 0
				Object vAdded = graph.insertVertex(defaultParent, vertexID, vertexLabel, 100, 100, 1, 1); 
				graph.updateCellSize(vAdded, true); // automatically resize the box
			}

			// Add all edges

			int fromVertex, toVertex;
			String fromVertexID, toVertexID; 
			for (int i = 0; i < fromVertices.size(); i++) {
				fromVertex = fromVertices.get(i);
				toVertex = toVertices.get(i);

				fromVertexID = "V" + Integer.toString(fromVertex);
				toVertexID = "V" + Integer.toString(toVertex);

				Object vFrom = ((mxGraphModel)(graph.getModel())).getCell(fromVertexID);  // add new vertex if not yet added			
				Object vTo = ((mxGraphModel)(graph.getModel())).getCell(toVertexID); // try to search whether the vertex is already added

				graph.insertEdge(defaultParent, null, "", vFrom, vTo);

			}

		} finally {
			graph.getModel().endUpdate();
		}

		graph.setAllowDanglingEdges(false); // not allow edge change such that it disconnected the connected vertices
		graph.setCellsDisconnectable(false); // not allow disconnect edge from a vertix to connect to another vertix
		graph.setDropEnabled(false); // not allow a cell to another edge to interrupt the connection



		// tidy up graph position
		new mxHierarchicalLayout(graph).execute(defaultParent);

		// Add Graph to Frame
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		this.getContentPane().add(graphComponent, BorderLayout.CENTER);
		// graphComponent.setEnabled(false); // not allow the graph to be edited

		this.numCreatedGraph+=1;
		
		if (this.numCreatedGraph == 1) { // only resize for the first graph creation
			// Set frame size and location
			this.setSize(sizeX, sizeY);
			// this.setLocation(locationX, locationY);			
		}


		// this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // don't call this; otherwise program will be automatically close after closing JGraphX windows
		this.setVisible(true);

		return;
	}

    /**
    * Union (set operation) of 2 sets
    */
	static public <T> ArrayList<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<T>(set);
	}

}