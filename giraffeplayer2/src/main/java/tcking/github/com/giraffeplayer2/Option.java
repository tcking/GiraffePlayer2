package tcking.github.com.giraffeplayer2;

import java.io.Serializable;

/**
 * Created by tcking on 2017
 */

public class Option implements Serializable,Cloneable {
    private int category;
    private String name;
    private Object value;

    private Option(int category, String name, Object value) {
        this.category = category;
        this.name = name;
        this.value = value;
    }

    public static Option create(int category, String name, String value){
        return new Option(category, name, value);
    }

    public static Option create(int category, String name, Long value){
        return new Option(category, name, value);
    }

    public int getCategory() {
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

        if (category != option.category) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        return value != null ? value.equals(option.value) : option.value == null;

    }

    @Override
    public int hashCode() {
        int result = category;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public Option clone() throws CloneNotSupportedException {
        return (Option) super.clone();
    }
}
