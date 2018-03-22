package demo.engine;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import demo.datasource.DBManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(ConcurrentTestRunner.class)
public class BuisnessOperationConcurrentQueuingTest {

    private DBManager manager;
    private BuisnessOperationExecutor executor;
    private Map<String, Object> testParams;
    private ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;
    private List<Long> count;

    private static final int threadCountForTest = 100;

    @Before
    public void setUp() throws Exception {
        manager = mock(DBManager.class);
        requestQueue = new ConcurrentSkipListMap<>();
        executor = new BuisnessOperationExecutor(manager, requestQueue);
        testParams = new HashMap<>();
        testParams.put("first_name", "test_name");
        testParams.put("balance", 100.0);
        count = Collections.synchronizedList(new ArrayList<>());
    }

    //check add queue synchronization
    @Test
    @ThreadCount(threadCountForTest)
    public void concurrentQueueChangeRequestTest() throws Exception {
        count.add(executor.queueChangeRequest(BuisnessOperationExecutor.Operations.CREATE_ACCOUNT, testParams));
    }

    @After
    public void check() throws SQLException {
        for (long i=0; i < threadCountForTest; i++){
            assertTrue(count.contains(i));
        }
    }
}