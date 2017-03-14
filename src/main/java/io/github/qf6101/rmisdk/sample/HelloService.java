package io.github.qf6101.rmisdk.sample;

import io.github.qf6101.rmisdk.base.RMIBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * User: qfeng
 * Date: 15-11-12 下午7:13
 * Usage: 随机数服务内容
 */
public class HelloService extends RMIBaseService implements IHello {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);
    //声明远程对象的唯一uuid
    private static final long serialVersionUID = 7593088675813868548L;
    //随机数生成器
    private volatile Random rand = null;

    /**
     * 随机数服务对象，实现随机数服务接口
     *
     * @param listenPort      随机数服务的监听端口
     * @param servicePoolSize 线程池大小
     * @param maxDelay        请求等待最长时间（单位：秒）
     * @throws Exception
     */
    public HelloService(int listenPort, int servicePoolSize, int maxDelay) throws Exception {
        //初始化工作
        super(listenPort, servicePoolSize, maxDelay);
        //初始化随机数生成器
        rand = new Random(System.currentTimeMillis());
        LOGGER.info("Successfully create random number generator");
    }

    /**
     * 生成随机数
     *
     * @param requestID 请求ID
     * @return 随机数
     * @throws RemoteException
     */
    @Override
    public int randNumber(int requestID) throws RemoteException {
        //向线程池提交计算逻辑，并返回存根
        Future<Integer> future = servicePool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return randNumberImp();
            }
        });
        try {
            //通过存根，向线程池请求随机数，并返回
            int number = future.get(maxDelay, TimeUnit.SECONDS);
            LOGGER.info(this.fullServiceName + " Send " + requestID + " :" + number);
            return number;
        } catch (Exception ex) {
            //计算或等待超时，打印错误日志信息，并取消请求，返回-1
            LOGGER.error("Exceed maximum delay for requestID " + requestID, ex);
            future.cancel(true);
            return -1;
        }
    }

    /**
     * 生成随机数的计算逻辑
     *
     * @return 随机数
     */
    private int randNumberImp() {
        return rand.nextInt();
    }
}
