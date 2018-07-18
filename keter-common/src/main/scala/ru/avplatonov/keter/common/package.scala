package ru.avplatonov.keter

package object common {
    case class Graph(
        id: Option[String],
        name: Option[String],
        description: String,
        nodes: Seq[Node],
        parameters: Seq[Parameter]
    )

    case class Node(
        graphId: GraphId,
        name: String,
        taskId: TaskId,
        inputs: Map[String, Connector],
        outputs: Map[String, Connector]
    )

    case class Connector(nodeName: String, name: String)

    case class Task(
        id: Option[TaskId] = None,
        graphId: Option[GraphId] = None,
        name: String,
        description: String = "",
        tags: Set[String] = Set.empty,
        script: Script,
        hardware: Option[HardwareSpec] = None,
        files: Seq[FileLink] = Seq.empty
    )

    case class TaskId(value: String)

    case class GraphId(
        graphId: Option[String] = None,
        localId: Int
    )

    case class Script(
        text: String,
        parameters: Seq[Parameter]
    )

    case class Parameter(
        name: String,
        inOutType: InOutType,
        valueType: ParameterType
    )

    case class HardwareSpec(
        hdd: Int,
        ram: Int,
        cores: Int
    )

    case class FileLink(value: String)
}
