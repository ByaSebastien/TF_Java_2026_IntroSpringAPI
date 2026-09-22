package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SearchSpecification {
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

    static <T> List<Specification<T>> search(Map<String, String> params) {

        List<SearchParam<T>> searchParams = SearchParam.create(params);

        return searchParams.stream().map(
                SearchSpecification::search
        ).toList();
    }
}
