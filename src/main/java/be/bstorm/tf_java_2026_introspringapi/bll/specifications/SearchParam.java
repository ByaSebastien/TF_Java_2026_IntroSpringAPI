package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SearchParam<T> {

    @Getter @Setter
    private String field;

    @Getter @Setter
    private SearchOperator op;

    @Getter @Setter
    private Object value;

    private static <T> SearchParam<T> create(
            Map.Entry<String, String> entry
    ){
        String field;
        SearchOperator op;
        String value;

        String[] parts = entry.getKey().split("_");

        if( parts.length == 1 ){
            field = parts[0];
            op = SearchOperator.EQ;
        }
        else if( parts.length == 2 ){
            op = SearchOperator.valueOf(parts[0].toUpperCase());
            field = parts[1];
        }
        else {
            throw new IllegalArgumentException("Invalid search parameter: " + entry.getKey());
        }

        value = entry.getValue();

        return new SearchParam<T>(field, op, value);
    }

    public static <T> List<SearchParam<T>> create(
            Map<String, String> params
    ){
        return params.entrySet().stream()
                .filter(e -> !e.getKey().equals("page") && !e.getKey().equals("size") && !e.getKey().equals("sort"))
                .map(SearchParam::<T>create)
                .toList();
    }
}
