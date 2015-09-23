package model.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.legacy.EmptyElementLegacy;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.legacy.LegacyContact;

import java.util.Random;

/**
 * Тест сетки на массиве.
 */
public class LegacyVirtualGridTest {
    LegacyVirtualGrid grid;
    LegacyTopologyElement testElement = EmptyElementLegacy.create(null);
    int maxRowCount;

    @Before
    public void setUp() {
        grid = new LegacyVirtualGrid();
        maxRowCount = new Random().nextInt(10) + 1;
        grid.setMaxRowCount(maxRowCount);
    }

    @Test
    public void addElementToColumnTest() {
        int size = new Random().nextInt(100);
        for (int i = 0; i < size; i++) {
            grid.addElementToColumn(EmptyElementLegacy.create(null), new Random().nextInt(10));
        }
        
        Assert.assertTrue(grid.rowCount() <= maxRowCount);
        Assert.assertTrue(grid.columnCount() <= 10);
    }

    @Test
    public void addElementTest() {
        int size = new Random().nextInt(100);
        for (int i = 0; i < size; i++) {
            grid.addElement(new LegacyContact(null, new Point(), 0, null, null, null));
        }

        Assert.assertEquals(size, grid.getAllElements().size());
        Assert.assertTrue(grid.rowCount() <= maxRowCount);
    }
}
