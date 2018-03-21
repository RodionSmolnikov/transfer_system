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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(ConcurrentTestRunner.class)
public class BuisnessOperationConcurrentTest {

    private volatile AtomicLong counter;
    private Set<BuisnessOperationExecutor> checkSingle;
    private DBManager manager;
    private BuisnessOperationExecutor executor;
    private Map<String, Object> testParams;
    ConcurrentNavigableMap<Long, Map<String,Object>> requestQueue;
    private List<Long> count;

    private static final int threadCountForTest = 100;


    //check singleton synchro
    @Before
    public void createSet() {
        checkSingle = new HashSet<>();
    }

    @Test
    @ThreadCount(threadCountForTest)
    public void createBuisnessOperationExecutorTest() throws Exception {
        checkSingle.add(BuisnessOperationExecutor.getInstance());
    }

    @After
    public void checkSingle() {
        assertEquals(checkSingle.size(), 1);
    }

    //check chengeRequest Synchro - number in queue are one by one
    @Before
    public void setUpForConcurrentQueueChangeRequestTest() throws Exception {
        manager = mock(DBManager.class);
        executor = new BuisnessOperationExecutor(manager, requestQueue);
        requestQueue = new ConcurrentSkipListMap<>();
        testParams = new HashMap<>();
        testParams.put("first_name", "test_name");
        testParams.put("balance", 100.0);
        count = new ArrayList<>();
    }

    @Test
    @ThreadCount(threadCountForTest)
    public void concurrentQueueChangeRequestTest() throws Exception {
        count.add(executor.queueChangeRequest(BuisnessOperationExecutor.Operations.CREATE_ACCOUNT, testParams));
    }

    @After
    public void checkConcurrentQueueChangeRequestTest() throws SQLException {
        for (long i=0; i < threadCountForTest; i++){
            assertTrue(count.contains(i));
        }
    }


    //check chengeRequest Synchro - number in queue are one by one
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

    @Test
    @ThreadCount(threadCountForTest)
    public void concurrentExecuteRequestsTest() throws Exception {
        executor.getResultByQueueNumber(counter.getAndIncrement());
    }

    @After
    public void checkQueueConcurrentExecuteRequestsTest() throws SQLException {
        assertTrue(requestQueue.isEmpty());
    }

}