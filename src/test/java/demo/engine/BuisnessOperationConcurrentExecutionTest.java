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
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(ConcurrentTestRunner.class)
public class BuisnessOperationConcurrentExecutionTest {

    private volatile AtomicLong counter;
    private DBManager manager;
    private BuisnessOperationExecutor executor;
    private Map<String, Object> testParams;
    private ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;
    private List<Long> count;

    private static final int threadCountForTest = 100;

    @Before
    public void setUp() throws Exception {
        manager = mock(DBManager.class);
        requestQueue = new ConcurrentSkipListMap<> ();
        executor = new BuisnessOperationExecutor(manager, requestQueue);
        testParams = new HashMap<>();
        when(manager.addAccount(any())).thenReturn("Success");
        count = new ArrayList<>();
        for (int i = 0; i < threadCountForTest; i++)
            executor.queueChangeRequest(BuisnessOperationExecutor.Operations.CREATE_ACCOUNT, testParams);

        counter = new AtomicLong();
        counter.set(0);
    }

    //check execution synchronization
    @Test
    @ThreadCount(threadCountForTest)
    public void concurrentExecuteRequestsTest() throws Exception {
        executor.getResultByQueueNumber(counter.getAndIncrement());
    }

    @After
    public void check() throws SQLException {
        assertTrue(requestQueue.isEmpty());
    }

}