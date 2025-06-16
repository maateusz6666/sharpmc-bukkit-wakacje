package sharpmc.pl.utils.builders;

import java.util.ArrayList;
import java.util.List;

public class ListBuilder<T> {
    private final List<T> list = new ArrayList<>();

    public ListBuilder<T> put(T t) {
        this.list.add(t);
        return this;
    }

    public List<T> build() {
        return this.list;
    }
}
