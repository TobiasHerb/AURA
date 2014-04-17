package de.tuberlin.aura.core.measurement;

/**
 * @author Asterios Katsifodimos
 */
public enum MeasurementType {
    /* Rewriting related */
    REWRITING_TIME,
    REWRITING_TIME_FIRST_SOLUTION,
    REWRITING_TIME_TOTAL,

    /* Adaptive views related */
    VIEW_SELECTION_ALGORITHM_EXECUTION_TIME,
    VIEW_SELECTION_CANDIDATE_VIEWS_ENUMERATION_EXECUTION_TIME,
    VIEW_SELECTION_COST_ESTIMATION_TIME,
    VIEW_SELECTION_CANDIDATE_VIEW_SIZE_ESTIMATION_TIME,
    VIEW_SELECTION_BENEFIT_ESTIMATION_TIME,
    VIEW_SELECTION_LOCATE_DOCUMENTS_TO_ANSWER_QUERY,
    VIEW_SELECTION_NUMBER_OF_CANDIDATE_VIEWS,
    VIEW_SELECTION_ALGORITHM,
    VIEW_SELECTION_ESTIMATED_WORKLOAD_SIZE,
    VIEW_SELECTION_SPACE_BUDGET,
    VIEW_SELECTION_SPACE_OCCUPATION,
    VIEW_SELECTION_NUMBER_OF_SELECTED_VIEWS,
    VIEW_SELECTION_SELECTED_VIEW,

    /* Query execution */
    QUERY_EXECUTION_TIME,
    QUERY_EXECUTION_TIME_TO_FIRST_RESULT,
    QUERY_EXECUTION_RESULTS_NUMBER,

    /* View materialization */
    VIEW_MATERIALIZATION_TIME,
    VIEW_MATERIALIZATION_BYTES_TRANSFERRED,
    TOTAL_TUPLE_STORAGE_TIME,
    TOTAL_TUPLES_STORED,
    TOTAL_TUPLE_RETRIEVAL_TIME,
    TOTAL_TUPLES_RETRIEVED,

    /* Data extraction */
    EXTRACTION_TIME,

    /* Time to build an embedding graph */
    EMBEDDING_GRAPH_GENERATION_TIME,
    EMBEDDING_GRAPH_NUMBER_OF_EDGES,
    EMBEDDING_GRAPH_NUMBER_OF_PATTERNS_EMBEDDED_BY_SOMEONE,
    EMBEDDING_GRAPH_TOTAL_NUMBER_OF_PATTERNS,

    /* Rewriting Graph */
    REWRITING_GRAPH_GENERATION_TIME,
    REWRITING_GRAPH_TOPOLOGICAL_SORT_TIME,
    REWRITING_GRAPH_NUMBER_OF_REWRITINGS,
    REWRITING_GRAPH_NUMBER_OF_EDGES,
    REWRITING_GRAPH_MAX_NUMBER_OF_REWRITINGS_PER_VIEW,
    EMBEDDING_GRAPH_NUMBER_OF_PATTERN_CLUSTERS,
    EMBEDDING_GRAPH_AVG_PRED_PER_VIEW,
    EMBEDDING_GRAPH_AVG_PRED_PER_NODE,
    EMBEDDING_GRAPH_AVG_NODES_PER_VIEW,

    /* Graph Algorithms */
    CYCLE_ELIMINATION_TIME,
    CYCLE_ELIMINATION_NUMBER_OF_REMOVED_EDGES,

    /* Subgraph selection time */
    SUBGRAPH_SELECTION_TIME,
    REWRITING_GRAPH_NUMBER_OF_VIEWS_REWRITTEN_FROM_VIEWS,
    REWRITING_GRAPH_NUMBER_OF_VIEWS_REWRITTEN_FROM_SOURCE,
    REWRITING_GRAPH_NUMBER_OF_VIEWS_PER_REAL_REWRITING,
    REWRITING_GRAPH_AVERAGE_PATTERN_OUT_DEGREE,
    REWRITING_GRAPH_NUMBER_OF_TRADITIONALLY_REWRITTEN_PATTERNS,
    REWRITING_GRAPH_LATENCY,
    LOCAL_TIME_OFFSET,
    EMBEDDING_GRAPH_AVG_VAL_NODES_PER_VIEW,
    EMBEDDING_GRAPH_AVG_CONT_NODES_PER_VIEW,
    EMBEDDING_GRAPH_AVG_RETURNING_NODES_PER_VIEW,


    FIRST_TUPLE_ARRIVED,
    LAST_TUPLE_ARRIVED,
    LAST_TUPLE_LEFT,
    FIRST_TUPLE_LEFT,
    VIEW_INSTALL_TIME,
    REWRITING_GRAPH_NUMBER_OF_UNIQUE_PATTERNS,
    DOCUMENT_ARRIVAL_TIME
}
