package jp.gr.java_conf.falius.communication.server;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.falius.communication.helper.ClientHelper;
import jp.gr.java_conf.falius.communication.helper.OnceClient;
import jp.gr.java_conf.falius.communication.receiver.OnReceiveListener;
import jp.gr.java_conf.falius.communication.receiver.ReceiveData;
import jp.gr.java_conf.falius.communication.sender.SendData;
import jp.gr.java_conf.falius.communication.sender.SendQueue;
import jp.gr.java_conf.falius.communication.swapper.OnceSwapper;
import jp.gr.java_conf.falius.communication.swapper.Swapper;

public class NonBlockingServerTest {
    private static Logger log = LoggerFactory.getLogger(NonBlockingServerTest.class);
    private static final String HOST = "localhost";
    private static final int PORT = 9001;

    @Test
    public void testReceiveValueInSwapper() throws IOException, TimeoutException {
        String[] receiveData = {"1", "2", "3"};
        try (Server server = new NonBlockingServer(PORT, new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiver) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiver, is(not(nullValue())));
                        SendData sender = new SendQueue();
                        for (String data : receiveData) {
                            String rev = receiver.getString();
                            assertThat(rev, is(data));
                            sender.put(Integer.parseInt(rev));
                        }
                        assertThat(receiver.getString(), is(nullValue()));
                        return sender;
                    }
                };
            }
        })) {
            server.startOnNewThread();

            ClientHelper client = new OnceClient(HOST, PORT);
            ReceiveData ret =  client.send(receiveData);
            assertThat(ret, is(not(nullValue())));
            assertThat(ret.dataCount(), is(receiveData.length));
            for (String data : receiveData) {
                assertThat(ret.getInt(), is(Integer.parseInt(data)));
            }
            assertThat(ret.get(), is(nullValue()));
        }
    }

    @Test
    public void testAddOnReceiveListener() throws IOException, TimeoutException {
        String[] receiveData = {"data1", "data2", "data3"};
        try (Server server = new NonBlockingServer(PORT, new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiver) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiver, is(not(nullValue())));
                        assertThat(receiver.get(), is(nullValue()));
                        SendData sender = new SendQueue();
                        return sender;
                    }
                };
            }
        })) {
            server.addOnReceiveListener(new OnReceiveListener() {

                @Override
                public void onReceive(String fromAddress, int readByte, ReceiveData receiver) {
                        assertThat(receiver, is(not(nullValue())));
                        SendData sender = new SendQueue();
                        for (String data : receiveData) {
                            String rev = receiver.getString();
                            assertThat(rev, is(data));
                            sender.put(rev);
                        }
                        assertThat(receiver.getString(), is(nullValue()));
                }
            });
            server.startOnNewThread();

            ClientHelper client = new OnceClient(HOST, PORT);
            client.send(receiveData);
        }
    }

    @Test
    public void testCall() throws IOException, TimeoutException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = null;
        String[] receiveData = {"1", "2", "3"};
        try (Server server = new NonBlockingServer(PORT, new Swapper.SwapperFactory() {

            @Override
            public Swapper get() {
                return new OnceSwapper() {

                    @Override
                    public SendData swap(String remoteAddress, ReceiveData receiver) {
                        log.info("swap from {}", remoteAddress);
                        assertThat(receiver, is(not(nullValue())));
                        SendData sender = new SendQueue();
                        for (String data : receiveData) {
                            String rev = receiver.getString();
                            assertThat(rev, is(data));
                            sender.put(Integer.parseInt(rev));
                        }
                        assertThat(receiver.getString(), is(nullValue()));
                        return sender;
                    }
                };
            }
        })) {
            future = executor.submit(server);

            ClientHelper client = new OnceClient(HOST, PORT);
            client.send(receiveData);
        }
        assertThat(future, is(not(nullValue())));

        executor.shutdown();

        Object ret = future.get();
        assertThat(ret, is(nullValue()));
        assertThat(future.isDone(), is(true));
    }

}
