package demo.engine;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(ConcurrentTestRunner.class)
public class BuisnessOperationConcurrentGettingTest {

    private Set<BuisnessOperationExecutor> checkSingle;
    private static final int threadCountForTest = 100;

    @Before
    public void setUp() {
        checkSingle = new HashSet<>();
    }

    //test getInstance synchronization
    @Test
    @ThreadCount(threadCountForTest)
    public void createBuisnessOperationExecutorTest() throws Exception {
        checkSingle.add(BuisnessOperationExecutor.getInstance());
    }

    @After
    public void check() {
        assertEquals(checkSingle.size(), 1);
    }

}