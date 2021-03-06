package cz.payola.web.client.views.graph.visual.graph

import cz.payola.web.client.views.algebra._
import positioning.LocationDescriptor
import s2js.adapters.html.elements.CanvasRenderingContext2D
import cz.payola.common.visual.Color
import collection.mutable.ListBuffer
import cz.payola.common.rdf._
import cz.payola.common.entities.settings._
import cz.payola.web.client.models.PrefixApplier
import scala.Some

class VertexViewGroup(position: Point2D, prefixApplier: Option[PrefixApplier])
    extends VertexViewElement(position, prefixApplier) {

    private var containedElements = ListBuffer[VertexViewElement]()

    private var groupName: Option[String] = None

    def vertexViews = containedElements

    def getAllVertices: List[Vertex] = {
        val vertices = ListBuffer[Vertex]()
        vertexViews.foreach {
            _ match {
                case group: VertexViewGroup =>
                    vertices ++= group.getAllVertices
                case view: VertexView =>
                    vertices += view.vertexModel
            }
        }
        vertices.toList
    }

    def getFirstContainedVertex(): Vertex = {
        vertexViews(0) match {
            case group: VertexViewGroup =>
                group.getFirstContainedVertex()
            case view: VertexView =>
                view.vertexModel
        }
    }

    def setName(newName: String) {
        if (newName == null || newName == "") {
            information = None
        } else {
            groupName = Some(newName)
            if(information.isEmpty) {
                information = Some(InformationView.constructBySingle("", None))
            }
            information.get.labels = List(newName)
        }
    }

    def getName = if (groupName.isDefined) groupName.get else ""

    def edges: ListBuffer[EdgeView] = {
        val allEdges = ListBuffer[EdgeView]()
        containedElements.foreach(allEdges ++= _.edges)
        allEdges
    }

    /**
     * Splits the input list by origin of the edgeViews
     * and sets the splitted lists to the corresponding vertexViews.
     * @param newEdges
     */
    def edges_=(newEdges: ListBuffer[EdgeView]) {
        containedElements.foreach(_.edges = ListBuffer[EdgeView]()) //empty the lists
        newEdges.foreach { newEdge =>
            newEdge.originView match {
                case vv: VertexView =>
                    val originVertex = containedElements.find(_.represents(vv.vertexModel))
                    if (originVertex.isDefined) {
                        originVertex.get.edges += newEdge
                    }
                case vg: VertexViewGroup =>
                    edgesEqSup(vg, newEdge)
            }
        }
    }

    def edgesEqSup(vg: VertexViewGroup, newEdge: EdgeView) { //edges_= support function - s2js does not compile this correctly
        val originGroup = containedElements.find {element => vg.contains(element)}
        if (originGroup.isDefined) {
            originGroup.get.edges += newEdge
            redirectEdges(originGroup.get, Some(this), originGroup.get.edges.toList, List[VertexViewGroup]())
        }
    }

    def addLiteralVertex(typeOfAttribute: Edge, valueOfAttribute: Seq[Vertex], identNeighborVertex: IdentifiedVertex) {
        val identNeighbourVertexView =
            containedElements.find {
                vertexView => vertexView.represents(identNeighborVertex)
            }
        identNeighbourVertexView.map(_.addLiteralVertex(typeOfAttribute, valueOfAttribute, identNeighborVertex))
    }

    def addVertex(newVertex: VertexViewElement, allEdges: List[EdgeView]) {
        redirectEdges(newVertex, Some(this), allEdges, List[VertexViewGroup]())
        containedElements += newVertex
    }

    def addVertices(newVertices: List[VertexViewElement], allEdges: List[EdgeView]) {
        newVertices.foreach{ vertex => addVertex(vertex, allEdges) }
    }

    def removeVertex(oldVertex: VertexViewElement, allEdges: List[EdgeView], allGroups: List[VertexViewGroup]) {
        containedElements -= oldVertex

        //if the removed vertex is a part of another group do not redirect the edges to the original vertexView
        redirectEdges(oldVertex, None, allEdges, allGroups)
    }

    def removeAll(allEdges: List[EdgeView], allGroups: List[VertexViewGroup]): List[VertexViewElement] = {
        var removedVertices = List[VertexViewElement]()
        while(!containedElements.isEmpty) {
            val currentVertex = containedElements.head
            removedVertices ++= List(currentVertex)
            containedElements -= currentVertex
            redirectEdges(currentVertex, None, allEdges, allGroups)
        }
        removedVertices
    }

    private def redirectEdges(toRedirect: VertexViewElement, redirectTo: Option[VertexViewElement],
        allEdges: List[EdgeView], allGroups: List[VertexViewGroup]) {

        allEdges.foreach{ edge =>
            if (toRedirect.contains(edge.originView)) {
                val childGroupToRedirectTo: Option[VertexViewElement] = if (redirectTo.isEmpty) {
                    allGroups.find { group => group.contains(edge.originView)}
                } else { redirectTo }
                edge.redirectOrigin(childGroupToRedirectTo)

            }

            if(toRedirect.contains(edge.destinationView)) {
                val childGroupToRedirectTo: Option[VertexViewElement] = if (redirectTo.isEmpty) {
                    allGroups.find { group => group.contains(edge.destinationView)}
                } else { redirectTo }
                edge.redirectDestination(childGroupToRedirectTo)
            }
        }
    }

    def getCurrentAge = getMinAge

    def getMaxAge: Int = {
        containedElements.map(_.getCurrentAge).foldLeft(0)((i,j) => i.max(j))
    }

    def getMinAge: Int = {
        containedElements.map(_.getCurrentAge).foldLeft(0)((i,j) => i.min(j))
    }

    /**
     * Sets count of parent graph update cycles to 0 for all vertices in this group.
     */
    def resetCurrentAge() {
        containedElements.foreach(_.resetCurrentAge())
    }

    /**
     * Increases count of parent graph update graph cycles by 1 for all vertices in this group.
     */
    def increaseCurrentAge() {
        containedElements.foreach(_.increaseCurrentAge())
    }

    /**
     * Sets count of parent graph update cycles to all contained vertices.
     */
    def setCurrentAge(newAge: Int) {
        containedElements.foreach(_.setCurrentAge(newAge))
    }

    /**
     * Determines if the point is (geometrically) inside of this vertexView (rectangle represented byt this vertexView).
     * Should be used in vertexView selection process.
     * @param point to be decided if is inside or not
     * @return true if this.position - radius <= point <= this.position + radius
     */
    def isPointInside(point: Point2D): Boolean = {

        val radiusVector = Vector2D.One * radius
        isPointInRect(point, position + (-radiusVector),
            position + radiusVector)
    }

    def draw(context: CanvasRenderingContext2D, positionCorrection: Vector2D) {

        drawQuick(context, positionCorrection)

        if(glyphSpan.isDefined) {

            if(0 < position.y - radius && position.y + radius < context.canvas.clientHeight &&
                0 < position.x - radius && position.x + radius < context.canvas.clientWidth) {

                glyphSpan.get.show("inline")

                val halfSize = math.max(glyphSpan.get.htmlElement.getBoundingClientRect.height,
                    glyphSpan.get.htmlElement.getBoundingClientRect.width) / 2

                val left = position.x + positionCorrection.x - halfSize

                val top = position.y + positionCorrection.y - halfSize

                glyphSpan.get.setAttribute("style",
                    "left: "+left.toString+"px; top: "+top.toString+
                        "px; font-size: "+(radius+30)+"px;")
            } else {
                glyphSpan.get.hide()
            }
        }

        val informationPositionCorrection =
            if(glyphSpan.isDefined) { Vector2D(0, radius + borderSize) } else { Vector2D.Zero }
        val informationPosition =
            (LocationDescriptor.getVertexInformationPosition(position) + positionCorrection).toVector +
                informationPositionCorrection

        if (information.isDefined) {
            information.get.draw(context, informationPosition)
        }
    }

    def drawQuick(context: CanvasRenderingContext2D, positionCorrection: Vector2D) {
        val correctedPosition = this.position + positionCorrection

        drawSquare(context, correctedPosition, radius * 2, borderSize, borderColor);
        if (isSelected) {
            fillCurrentSpace(context, new Color(color.red, color.green, color.blue))
        } else {
            fillCurrentSpace(context, color)
        }
    }

    def setConfiguration(newCustomization: Option[DefinedCustomization]) {
        val name = getName
        setVisualConfiguration(newCustomization, name, name, () => List[(String, Seq[String])]())
    }

    override def toString: String = {
        if(getName == "")
            "Group"
        else
            getName
    }

    def contains(vertexElement: VertexViewElement): Boolean = {
        vertexElement match {
            case thatGroup: VertexViewGroup =>
                vertexViews.exists{
                    _ match {
                        case thisInnerGroup: VertexViewGroup =>
                            thisInnerGroup.isEqual(thatGroup) || thisInnerGroup.contains(thatGroup)
                    }
                }
            case thatVertex: VertexView =>
                vertexViews.exists{ thisElement => thisElement.represents(thatVertex.vertexModel) }
        }
    }

    /**
     * Compares all of the contained vertices to another vertexView. Returns true if one of the contained
     * vertices is equal to the vertexView
     * @param that
     * @return
     */
    override def isEqual(that: Any): Boolean = {
        if (that == null) {
            false
        }
        that match {
            case thatVG: VertexViewGroup =>
                thatVG.containedElements.length == this.containedElements.length && !thatVG.containedElements.map{
                    _ match {
                        case thatInnerVV: VertexView =>
                            this.containedElements.exists(_.represents(thatInnerVV.vertexModel))
                        case thatInnerGroup: VertexViewGroup =>
                            this.containedElements.map{_.isEqual(thatInnerGroup)}.contains(true)

                    }
                }.contains(false)
            case _ => false
        }
    }

    def represents(vertex: Vertex): Boolean = {
        vertexViews.exists(_.represents(vertex))
    }
}
