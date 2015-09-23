package serialization.marshall;

import ru.etu.astamir.common.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Artem Mon'ko
 */
public class PairMarshallTest extends JAXBTest<Pair<Integer, Integer>> {
	public PairMarshallTest() {
		super(Pair.class);
	}

	@Override
	protected Collection<Pair<Integer, Integer>> getTestingInstances() {
		return Collections.unmodifiableCollection(Arrays.asList(Pair.of(1, 2)));
	}
}
