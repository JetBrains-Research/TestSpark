package org.jetbrains.research.testspark.core.generation.llm.ranker

import com.kuzudb.Connection
import com.kuzudb.Database

enum class GraphNodeType(
    val label: String,
) {
    CLASS("Class"),
    METHOD("Method"),
}

enum class GraphEdgeType {
    INHERITANCE,
    CALLS,
    HAS_METHOD,
    HAS_TYPE_PARAMETER,
    HAS_RETURN_TYPE,
    HAS_TYPE_PROPERTY,
    THROWS,
}

data class GraphNode(
    val name: String,
    val fqName: String,
    val type: GraphNodeType,
    val isUnitUnderTest: Boolean = false,
    val isStandardLibrary: Boolean = false,
    val properties: Map<String, Any> = emptyMap(),
)

data class GraphEdge(
    val from: String,
    val to: String,
    val type: GraphEdgeType,
    val properties: Map<String, Any> = emptyMap(),
)

interface Graph {
    fun addNode(node: GraphNode)

    fun addEdge(edge: GraphEdge)
}

class KuzuGraph(
    dbPath: String = ":memory:",
) : Graph {
    private val db = Database(dbPath)
    private val conn = Connection(db)

    init {
        // Create Node tables
        conn.query(
            "CREATE NODE TABLE Class(name STRING, fqName STRING, isUnitUnderTest BOOLEAN DEFAULT false, isStandardLibrary BOOLEAN DEFAULT false, PRIMARY KEY(fqName))",
        )
        conn.query(
            "CREATE NODE TABLE Method(name STRING, fqName STRING, isUnitUnderTest BOOLEAN DEFAULT false, isStandardLibrary BOOLEAN DEFAULT false, PRIMARY KEY(fqName))",
        )
        // Create Edge tables
        conn.query("CREATE REL TABLE INHERITANCE(FROM Class TO Class)")
        conn.query("CREATE REL TABLE CALLS(FROM Method TO Method)")
        conn.query("CREATE REL TABLE HAS_METHOD(FROM Class TO Method)")
        conn.query("CREATE REL TABLE HAS_TYPE_PARAMETER(FROM Method TO Class, FROM Class TO Class)")
        conn.query("CREATE REL TABLE HAS_RETURN_TYPE(FROM Method TO Class)")
        conn.query("CREATE REL TABLE HAS_TYPE_PROPERTY(FROM Class TO Class)")
        conn.query("CREATE REL TABLE THROWS(FROM Method TO Class)")
    }

    override fun addNode(node: GraphNode) {
        conn.query(
            "CREATE (:${node.type.label} {name: \"${node.name}\", fqName: \"${node.fqName}\", isUnitUnderTest: ${node.isUnitUnderTest}, isStandardLibrary: ${node.isStandardLibrary}})",
        )
    }

    override fun addEdge(edge: GraphEdge) {
        when (edge.type) {
            GraphEdgeType.INHERITANCE ->
                conn.query(
                    """
                       |MATCH (n1:Class), (n2:Class)
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.CALLS ->
                conn.query(
                    """
                       |MATCH (n1:Method), (n2:Method) 
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}' 
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.HAS_METHOD ->
                conn.query(
                    """
                       |MATCH (n1:Class), (n2:Method) 
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.HAS_TYPE_PARAMETER ->
                conn.query(
                    """
                       |MATCH (n1:Method), (n2:Class) 
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.HAS_RETURN_TYPE ->
                conn.query(
                    """
                       |MATCH (n1:Method), (n2:Class)
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.HAS_TYPE_PROPERTY ->
                conn.query(
                    """
                       |MATCH (n1:Class), (n2:Class) 
                       |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                       |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimMargin(),
                )
            GraphEdgeType.THROWS ->
                conn.query(
                    """
                    |MATCH (n1:Method), (n2:Class)
                    |WHERE n1.fqName = '${edge.from}' AND n2.fqName = '${edge.to}'
                    |MERGE (n1)-[:${edge.type}]->(n2)
                    """.trimIndent(),
                )
        }
    }

    fun close() {
        conn.close()
        db.close()
    }
}
