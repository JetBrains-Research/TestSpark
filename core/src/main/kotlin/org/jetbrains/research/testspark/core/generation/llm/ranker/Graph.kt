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

    fun graphEdgeTypeWeight(edgeType: GraphEdgeType): Double =
        when (edgeType) {
            GraphEdgeType.INHERITANCE -> 5.0
            GraphEdgeType.CALLS -> 5.0
            GraphEdgeType.HAS_METHOD -> 1.0
            GraphEdgeType.HAS_TYPE_PARAMETER -> 10.0
            GraphEdgeType.HAS_RETURN_TYPE -> 7.0
            GraphEdgeType.HAS_TYPE_PROPERTY -> 1.0
            GraphEdgeType.THROWS -> 1.0
        }

    fun calculatePageRank(
        source: String,
        dampingFactor: Double = 0.85,
        maxIterations: Int = 100,
        tolerance: Double = 1e-6, // Convergence tolerance
    ): Map<String, Double> {
        val nodes = getAllNodes()
        val edges = getAllEdges()

        val totalNodes = nodes.size
        if (totalNodes == 0) return emptyMap()

        val scores = mutableMapOf<GraphNode, Double>()

        nodes.forEach { node ->
            if (node.fqName === source) {
                scores[node] = 0.5
            }
            scores[node] = 0.5 / (totalNodes - 1)
        }

        val outWeightedDegreeMap =
            mutableMapOf<GraphNode, Double>().apply {
                nodes.forEach { node ->
                    this[node] = edges.filter { edge -> edge.from == node.fqName }.sumOf { graphEdgeTypeWeight(it.type) }
                }
            }

        val incomingEdgesMap =
            mutableMapOf<GraphNode, List<GraphEdge>>().apply {
                nodes.forEach { node ->
                    this[node] = edges.filter { edge -> edge.to == node.fqName }
                }
            }

        var hasConverged: Boolean
        var iteration = 0

        do {
            hasConverged = true
            val newScores = mutableMapOf<GraphNode, Double>()

            for (node in nodes) {
                val incomingEdges = incomingEdgesMap[node] ?: emptyList()
                val incomingScoreSum =
                    incomingEdges.sumOf { edge ->
                        val sourceNode = nodes.find { it.fqName == edge.from } ?: return@sumOf 0.0
                        scores[sourceNode]!! * (graphEdgeTypeWeight(edge.type) / outWeightedDegreeMap[sourceNode]!!)
                    }

                val newScore = (1 - dampingFactor) / totalNodes + dampingFactor * incomingScoreSum
                newScores[node] = newScore

                // Check for convergence (difference between old and new scores)
                if (Math.abs(newScore - scores[node]!!) > tolerance) {
                    hasConverged = false
                }
            }

            // Update scores with new values
            scores.putAll(newScores)
            iteration++
        } while (!hasConverged && iteration < maxIterations)
        println("PageRank iteration: $iteration")
        // Step 4: Convert result to fqName -> score map
        return scores.mapKeys { (node, _) -> node.fqName }
    }

    fun score(
        source: String,
        targets: List<String>,
    ): List<Pair<String, Double>> {
        // Simple score using hops
        val results = mutableListOf<Pair<String, Double>>()

        val pageRank = calculatePageRank(source)

        for (target in targets) {
            val score = pageRank[target] ?: 0.0
            results.add(Pair(target, score))
        }
        return results
    }

    fun getAllNodes(): List<GraphNode>

    fun getAllEdges(): List<GraphEdge>
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
        if (!queryResult.isSuccess) println(queryResult.errorMessage)
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
        if (!result.isSuccess) println(result.errorMessage)
    }

    override fun getAllNodes(): List<GraphNode> {
        val nodes = mutableListOf<GraphNode>()
        val queryResult = conn.query("MATCH (n) RETURN LABEL(n) as label, n.name, n.fqName, n.isUnitUnderTest, n.isStandardLibrary")
        while (queryResult.hasNext()) {
            val result = queryResult.next
            nodes.add(
                GraphNode(
                    type = GraphNodeType.valueOf(result.getValue(0).getValue<String>().uppercase()),
                    name = result.getValue(1).getValue<String>(),
                    fqName = result.getValue(2).getValue<String>(),
                    isUnitUnderTest = result.getValue(3).getValue<Boolean>(),
                    isStandardLibrary = result.getValue(4).getValue<Boolean>(),
                ),
            )
        }
        return nodes
    }

    override fun getAllEdges(): List<GraphEdge> {
        val edges = mutableListOf<GraphEdge>()
        val queryResult = conn.query("MATCH (n)-[r]-(m) RETURN LABEL(r), n.fqName, m.fqName")
        while (queryResult.hasNext()) {
            val result = queryResult.next
            edges.add(
                GraphEdge(
                    type = GraphEdgeType.valueOf(result.getValue(0).getValue<String>().uppercase()),
                    from = result.getValue(1).getValue<String>(),
                    to = result.getValue(2).getValue<String>(),
                ),
            )
        }
        return edges
    }

    fun close() {
        conn.close()
        db.close()
    }
}
