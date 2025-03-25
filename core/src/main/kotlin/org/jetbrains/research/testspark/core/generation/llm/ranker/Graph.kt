package org.jetbrains.research.testspark.core.generation.llm.ranker

import com.kuzudb.Connection
import com.kuzudb.Database
import com.kuzudb.Value

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
        val statement =
            conn.prepare(
                "CREATE (:${node.type.label} {name: \$name, fqName: \$fqName, isUnitUnderTest: \$isUnitUnderTest, isStandardLibrary: \$isStandardLibrary})",
            )
        if (!statement.isSuccess) println(statement.errorMessage)
        val queryResult =
            conn.execute(
                statement,
                mapOf(
                    "name" to Value(node.name),
                    "fqName" to Value(node.fqName),
                    "isUnitUnderTest" to Value(node.isUnitUnderTest),
                    "isStandardLibrary" to Value(node.isStandardLibrary),
                ),
            )
        if (!queryResult.isSuccess) print(queryResult.errorMessage)
    }

    override fun addEdge(edge: GraphEdge) {
        val result =
            when (edge.type) {
                GraphEdgeType.INHERITANCE -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Class), (n2:Class)
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.CALLS -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Method), (n2:Method) 
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.HAS_METHOD -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Class), (n2:Method) 
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.HAS_TYPE_PARAMETER -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Method), (n2:Class) 
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.HAS_RETURN_TYPE -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Method), (n2:Class)
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.HAS_TYPE_PROPERTY -> {
                    val statement =
                        conn.prepare(
                            """
                       |MATCH (n1:Class), (n2:Class) 
                       |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                       |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.THROWS -> {
                    val statement =
                        conn.prepare(
                            """
                            |MATCH (n1:Method), (n2:Class)
                            |WHERE n1.fqName = ${'$'}from AND n2.fqName = ${'$'}to
                            |MERGE (n1)-[:${edge.type}]->(n2)
                            """.trimIndent(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
            }
        if (!result.isSuccess) print(result.errorMessage)
    }

    fun close() {
        conn.close()
        db.close()
    }
}
