package tcking.github.com.giraffeplayer2;

import java.io.Serializable;

/**
 * Created by tcking on 2017
 */

public class Option implements Serializable {
    private String category;
    private String name;
    private Object value;

    private Option(String category, String name, Object value) {
        this.category = category;
        this.name = name;
        this.value = value;
    }

    public static Option create(String category, String name, String value){
        return new Option(category, name, value);
    }

    public static Option create(String category, String name, int value){
        return new Option(category, name, value);
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (!category.equals(option.category)) return false;
        if (!name.equals(option.name)) return false;
        return value.equals(option.value);

    }

    @Override
    public int hashCode() {
        int result = category.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
