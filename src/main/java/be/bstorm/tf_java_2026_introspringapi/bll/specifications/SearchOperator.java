package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

/**
 * Énumération des opérateurs de comparaison supportés par SearchSpecification.
 * Permet des recherches dynamiques: EQ=equals, GT=greater-than, CONTAINS=like %%, etc.
 * Format URL: GET /game?name=dmc&gt_releaseYear=2000
 */
public enum SearchOperator {
    EQ,         // égal
    NE,         // non égal
    GT,         // plus grand que (>)
    GTE,        // plus grand ou égal (>=)
    LT,         // plus petit que (<)
    LTE,        // plus petit ou égal (<=)
    START,      // commence par
    END,        // se termine par
    CONTAINS,   // contient
    IN,         // dans une liste
    NIN         // non dans une liste
}
