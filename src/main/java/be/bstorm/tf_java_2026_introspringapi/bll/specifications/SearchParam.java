package be.bstorm.tf_java_2026_introspringapi.bll.specifications;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Représente un critère de recherche single.
 * Parse les paramètres URL pour extraire le champ, l'opérateur et la valeur.
 * Format: "field" -> EQ, "op_field" -> opérateur ET champ.
 * Exemple: GET /game?name=dmc&gt_price=5000
 */
@AllArgsConstructor
public class SearchParam<T> {

    @Getter @Setter
    private String field;

    @Getter @Setter
    private SearchOperator op;

    @Getter @Setter
    private Object value;

    /**
     * Parse un seul paramètre URL.
     * @param entry clé-valeur du paramètre
     * @return SearchParam extrait
     */
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

    /**
     * Parse tous les paramètres de requête en SearchParam.
     * Ignore page, size, sort (paramètres de pagination).
     * @param params Map complète des query parameters
     * @return List de SearchParam extraits
     */
    public static <T> List<SearchParam<T>> create(
            Map<String, String> params
    ){
        return params.entrySet().stream()
                .filter(e -> !e.getKey().equals("page") && !e.getKey().equals("size") && !e.getKey().equals("sort"))
                .map(SearchParam::<T>create)
                .toList();
    }
}
