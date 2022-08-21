package nl.theepicblock.smunnel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListUtil {
	public static <T> void setSize(ArrayList<T> list, int expectedSize, Consumer<T> destructor, Supplier<T> constructor) {
		if (list.size() < expectedSize) {
			list.ensureCapacity(expectedSize);
			while (list.size() != expectedSize) {
				list.add(constructor.get());
			}
		} else if (list.size() > expectedSize) {
			while (list.size() != expectedSize) {
				var e = list.remove(list.size() - 1);
				destructor.accept(e);
			}
		}
	}
}
