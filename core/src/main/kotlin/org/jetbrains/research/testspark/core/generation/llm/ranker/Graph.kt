package org.jetbrains.research.testspark.core.generation.llm.ranker

import com.kuzudb.Connection
import com.kuzudb.Database
import com.kuzudb.KuzuList
import com.kuzudb.Value
import com.kuzudb.ValueNodeUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs

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
    FROM,
}

data class GraphNode(
    val name: String,
    val fqName: String,
    val type: GraphNodeType,
    val isUnitUnderTest: Boolean = false,
    val isStandardLibrary: Boolean = false,
    val score: Double = 0.0,
    val properties: Map<String, Any> = emptyMap(),
    // use in query mode
    val children: List<GraphNode> = emptyList(),
)

data class GraphEdge(
    val from: String,
    val to: String,
    val type: GraphEdgeType,
    val properties: Map<String, Any> = emptyMap(),
)

abstract class Graph {
    private val log = KotlinLogging.logger { this::class.java }

    abstract fun addNode(node: GraphNode)

    abstract fun addEdge(edge: GraphEdge)

    fun graphEdgeTypeWeight(edgeType: GraphEdgeType): Double =
        when (edgeType) {
            GraphEdgeType.INHERITANCE -> 0.7
            GraphEdgeType.CALLS -> 0.2
            GraphEdgeType.HAS_METHOD -> 0.5
            GraphEdgeType.HAS_TYPE_PARAMETER -> 1.0
            GraphEdgeType.HAS_RETURN_TYPE -> 1.0
            GraphEdgeType.HAS_TYPE_PROPERTY -> 1.0
            GraphEdgeType.THROWS -> 0.7
            GraphEdgeType.FROM -> 1.0
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
            if (node.fqName == source) {
                scores[node] = 0.5
            } else {
                scores[node] = 0.5 / (totalNodes - 1)
            }
        }

        val outWeightedDegreeMap =
            mutableMapOf<GraphNode, Double>().apply {
                nodes.forEach { node ->
                    this[node] =
                        edges.filter { edge -> edge.from == node.fqName }.sumOf { graphEdgeTypeWeight(it.type) }
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
                if (abs(newScore - scores[node]!!) > tolerance) {
                    hasConverged = false
                }
            }
            val normalizationFactor = 1.0 / newScores.values.sum()
            // Update scores with new values
            scores.putAll(newScores.mapValues { (_, value) -> value * normalizationFactor })
            iteration++
        } while (!hasConverged && iteration < maxIterations)
        log.info { "PageRank iteration: $iteration" }
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

    abstract fun getAllNodes(): List<GraphNode>

    abstract fun getAllEdges(): List<GraphEdge>

    abstract fun saveScores(scores: Map<String, Double>)

    abstract fun getInterestingNodes(threshold: Double? = null): List<GraphNode>
}

class KuzuGraph(
    dbPath: String = ":memory:",
) : Graph() {
    private val log = KotlinLogging.logger { this::class.java }
    private val db = Database(dbPath)
    private val conn = Connection(db)

    init {
        // Create Node tables
        conn.query(
            "CREATE NODE TABLE Class(name STRING, fqName STRING, isUnitUnderTest BOOLEAN DEFAULT false, isStandardLibrary BOOLEAN DEFAULT false, score DOUBLE DEFAULT 0.0, PRIMARY KEY(fqName))",
        )
        conn.query(
            "CREATE NODE TABLE Method(name STRING, fqName STRING, isUnitUnderTest BOOLEAN DEFAULT false, isStandardLibrary BOOLEAN DEFAULT false, score DOUBLE DEFAULT 0.0, PRIMARY KEY(fqName))",
        )
        // Create Edge tables
        conn.query("CREATE REL TABLE INHERITANCE(FROM Class TO Class)")
        conn.query("CREATE REL TABLE CALLS(FROM Method TO Method)")
        conn.query("CREATE REL TABLE HAS_METHOD(FROM Class TO Method)")
        conn.query("CREATE REL TABLE HAS_TYPE_PARAMETER(FROM Method TO Class, FROM Class TO Class)")
        conn.query("CREATE REL TABLE HAS_RETURN_TYPE(FROM Method TO Class)")
        conn.query("CREATE REL TABLE HAS_TYPE_PROPERTY(FROM Class TO Class)")
        conn.query("CREATE REL TABLE THROWS(FROM Method TO Class)")
        conn.query("CREATE REL TABLE FROM(FROM Method TO Class)")
    }

    override fun addNode(node: GraphNode) {
        val statement =
            conn.prepare(
                "CREATE (:${node.type.label} {name: \$name, fqName: \$fqName, isUnitUnderTest: \$isUnitUnderTest, isStandardLibrary: \$isStandardLibrary})",
            )
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
        if (!queryResult.isSuccess) log.warn { "Error inserting node: " + queryResult.errorMessage }
    }

    override fun addEdge(edge: GraphEdge) {
        val queryResult =
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
                            """.trimMargin(),
                        )
                    conn.execute(
                        statement,
                        mapOf("from" to Value(edge.from), "to" to Value(edge.to)),
                    )
                }
                GraphEdgeType.FROM -> {
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
            }
        if (!queryResult.isSuccess) {
            log.warn { "Error inserting edge: " + queryResult.errorMessage }
        }
    }

    override fun getAllNodes(): List<GraphNode> {
        val nodes = mutableListOf<GraphNode>()
        val queryResult =
            conn.query(
                "MATCH (n) RETURN LABEL(n) as label, n.name, n.fqName, n.isUnitUnderTest, n.isStandardLibrary, n.score",
            )
        while (queryResult.hasNext()) {
            val result = queryResult.next
            nodes.add(
                GraphNode(
                    type = GraphNodeType.valueOf(result.getValue(0).getValue<String>().uppercase()),
                    name = result.getValue(1).getValue<String>(),
                    fqName = result.getValue(2).getValue<String>(),
                    isUnitUnderTest = result.getValue(3).getValue<Boolean>(),
                    isStandardLibrary = result.getValue(4).getValue<Boolean>(),
                    score = result.getValue(5).getValue<Double>(),
                ),
            )
        }
        return nodes
    }

    override fun getAllEdges(): List<GraphEdge> {
        val edges = mutableListOf<GraphEdge>()
        val queryResult = conn.query("MATCH (n)-[r]->(m) RETURN LABEL(r), n.fqName, m.fqName")
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

    override fun saveScores(scores: Map<String, Double>) {
        val statement = conn.prepare("MATCH (n {fqName: \$fqName}) SET n.score = \$score")
        for ((fqName, score) in scores) {
            val queryResult =
                conn.execute(
                    statement,
                    mapOf(
                        "fqName" to Value(fqName),
                        "score" to Value(score),
                    ),
                )
            if (!queryResult.isSuccess) {
                log.warn { "Error saving score: " + queryResult.errorMessage }
            }
        }
    }

    override fun getInterestingNodes(threshold: Double?): List<GraphNode> {
        val nodes = mutableListOf<GraphNode>()
        val queryResult =
            conn.query(
                """MATCH (n)
                   | WITH COUNT(n.fqName) as totalNodes
                   | MATCH p=(c:Class)-[r:HAS_METHOD]->(m:Method)
                   | WHERE c.score > (1/totalNodes) AND m.score > (1/totalNodes)
                   | WITH c, collect(m) as classMethodsList
                   | UNWIND classMethodsList as cm
                   | WITH c, cm
                   | ORDER BY c.score DESC, cm.score DESC
                   | LIMIT 20
                   | RETURN c, collect(cm)
                """.trimMargin(),
            )
        if (!queryResult.isSuccess) {
            log.warn { "Error getting interesting nodes: " + queryResult.errorMessage }
            return emptyList()
        }
        while (queryResult.hasNext()) {
            val result = queryResult.next
            val classNode = kuzuNodeValueToGraphNode(result.getValue(0))
            val methodsList = KuzuList(result.getValue(1))
            val methodNodes = mutableListOf<GraphNode>()
            for (i in 1..methodsList.listSize) {
                methodsList.getListElement(i)?.let {
                    methodNodes.add(kuzuNodeValueToGraphNode(it))
                }
            }
            nodes.add(
                GraphNode(
                    type = GraphNodeType.CLASS,
                    name = classNode.name,
                    fqName = classNode.fqName,
                    isUnitUnderTest = classNode.isUnitUnderTest,
                    isStandardLibrary = classNode.isStandardLibrary,
                    score = classNode.score,
                    children = methodNodes.sortedByDescending { it.score },
                ),
            )
        }
        return nodes
    }

    fun kuzuNodeValueToGraphNode(value: Value): GraphNode =
        GraphNode(
            type = GraphNodeType.valueOf(ValueNodeUtil.getLabelName(value).uppercase()),
            name = ValueNodeUtil.getPropertyValueAt(value, 0).getValue<String>(),
            fqName = ValueNodeUtil.getPropertyValueAt(value, 1).getValue<String>(),
            isUnitUnderTest = ValueNodeUtil.getPropertyValueAt(value, 2).getValue<Boolean>(),
            isStandardLibrary = ValueNodeUtil.getPropertyValueAt(value, 3).getValue<Boolean>(),
            score = ValueNodeUtil.getPropertyValueAt(value, 4).getValue<Double>(),
        )

    fun close() {
        conn.close()
        db.close()
    }
}
