package io.github.qf6101.rmisdk.sample;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-15 下午10:46
 * Usage: 业务客户端示例
 */
public class ApplicationClient {
    //随机数客户端
    private HelloClient hello = null;

    /**
     * 应用程序构造函数，全局只需要一个随机数客户端
     *
     * @throws Exception
     */
    public ApplicationClient() throws Exception {
        //初始化随机数客户端
        hello = new HelloClient("localhost", 6001, "localhost", 6002);
    }

    /**
     * 生成长度为5的随机数列表
     */
    public List<Integer> createFiveNumbers() {
        List<Integer> numbers = new ArrayList<Integer>(5);
        while (numbers.size() < 5) {
            //获取随机数
            int number = hello.nextNumber(numbers.size());
            //如果返回值异常，则不加入到列表中
            if (number != -1) {
                numbers.add(number);
            }
        }
        //返回随机数列表
        return numbers;
    }
}
