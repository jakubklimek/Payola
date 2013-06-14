package cz.payola.web.client.views

import cz.payola.common.rdf._
import cz.payola.web.client.events._
import cz.payola.common.entities.settings.OntologyCustomization
import cz.payola.common.visual.Color

class VertexEventArgs[+A](target: A, val vertex: Vertex) extends EventArgs[A](target)

/**
 * A view that displays RDF graphs.
 */
abstract class GraphView
{
    /** The graph that is currently being visualized. */
    protected var currentGraph: Option[Graph] = None

    /** Count of vertices in the result of current query (equal or more than vertices in currentGraph) */
    protected var currentResultsCount: Option[Int] = None

    /** The ontology customization that is currently used during visualization. */
    protected var currentCustomization: Option[OntologyCustomization] = None

    /** Triggered when a vertex is selected. */
    val vertexSelected = new UnitEvent[this.type, VertexEventArgs[this.type]]

    /** Triggered when the vertex is selected as a start of browsing. */
    val vertexBrowsing = new UnitEvent[this.type, VertexEventArgs[this.type]]

    /** Triggered when the vertex is selected as a start of browsing in a particular data source. */
    val vertexBrowsingDataSource = new UnitEvent[this.type, VertexEventArgs[this.type]]

    /** Triggered when a vertex is selected to represent the main vertex in a visualization. */
    val vertexSetMain = new UnitEvent[this.type , VertexEventArgs[this.type]]

    def setMainVertex(vertex: Vertex) { }

    /**
     * Updates both the graph that should be visualized and the customization that should be used.
     * @param graph The graph to visualize.
     * @param customization The ontology customization that should be used during visualization.
     * @param resultsCount Count of vertices in the result of current query (by which was the graph fetched); resultsCount >= graph.get.vertices.length
     */
    def update(graph: Option[Graph], customization: Option[OntologyCustomization], resultsCount: Option[Int]) {
        updateOntologyCustomization(customization)
        updateGraph(graph, true, resultsCount)
    }

    /**
     * Updates the current graph of the view.
     * @param graph The graph to add to the current graph.
     * @param resultsCount Count of vertices in the result of current query (by which was the graph fetched); resultsCount >= graph.get.vertices.length
     */
    def updateGraph(graph: Option[Graph], contractLiterals: Boolean, resultsCount: Option[Int]) {
        currentGraph = graph
    }

    /**
     * Runs the visualization.
     */
    def drawGraph() {}

    /**
     * Updates the ontology customization that should be used during graph visualization and re-runs the visualization.
     * @param customization The ontology customization that should be used.
     */
    def updateOntologyCustomization(customization: Option[OntologyCustomization]) {
        currentCustomization = customization
    }

    def updateVertexColor(vertex: Vertex, color: Option[Color]) {

    }

    /**
     * Removes the current graph from the view memory and resets the visualization.
     */
    def clear() {
        updateGraph(None, true, None)
    }
}
