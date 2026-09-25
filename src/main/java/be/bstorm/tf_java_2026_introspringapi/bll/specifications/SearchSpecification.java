package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Convertit des paramètres URL en Specifications JPA dynamiques.
 * Permet les recherches complexes sans requêtes SQL écrites manuellement.
 * Utilisé par le service pour filter les Games via Repository.
 */
public interface SearchSpecification {
    /**
     * Crée une Specification JPA depuis un SearchParam.
     * @param searchParam critère avec field, opérateur, valeur
     * @return Specification applicable via JpaSpecificationExecutor
     */
    private static <T> Specification<T> search(SearchParam<T> searchParam){
        return (root, query, cb) -> switch (searchParam.getOp()) {
            case EQ -> cb.equal(cb.lower(root.get(searchParam.getField())),searchParam.getValue().toString().toLowerCase());
            case NE -> cb.notEqual(cb.lower(root.get(searchParam.getField())),searchParam.getValue().toString().toLowerCase());
            case GT -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.gt(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            case GTE -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.ge(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            case LT -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.lt(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            case LTE -> {
                try {
                    Number number = new BigDecimal(searchParam.getValue().toString());
                    yield cb.le(root.get(searchParam.getField()), number);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Value must be a number");
                }
            }
            case START -> cb.like(cb.lower(root.get(searchParam.getField())),searchParam.getValue().toString().toLowerCase() + "%");
            case END -> cb.like(cb.lower(root.get(searchParam.getField())),"%" + searchParam.getValue().toString().toLowerCase());
            case CONTAINS -> cb.like(cb.lower(root.get(searchParam.getField())),"%" + searchParam.getValue().toString().toLowerCase() + "%");
            case IN -> root.get(searchParam.getField()).in(((String)searchParam.getValue()).split("/,"));
            case NIN -> cb.not(root.get(searchParam.getField())).in(((String)searchParam.getValue()).split("/,"));
        };
    }

    /**
     * Convertit tous les paramètres de requête en liste de Specifications.
     * @param params paramètres URL bruts
     * @return List de Specifications combinables avec and/or
     */
    static <T> List<Specification<T>> search(Map<String, String> params) {

        List<SearchParam<T>> searchParams = SearchParam.create(params);

        return searchParams.stream().map(
                SearchSpecification::search
        ).toList();
    }
}
