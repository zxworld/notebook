#### 下面这个程序，是写的一个项目启动就运行，接收流水信息的真实案例

* 特点：推送的流水信息数据量大，并且是一直推送，需要解析入库等操作
* 设计思路：考虑到入库时间远远<入库时间，所以使用多线程入库，在接收时放置一个队列，大于队列最大数，阻塞线程；单独启一个线程池处理队列里面存放的数据

```
    package com.ylink.ylpay.ttdordermp.client;

    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.net.URI;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Properties;
    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.LinkedBlockingQueue;
    import java.util.concurrent.RejectedExecutionHandler;
    import java.util.concurrent.ThreadPoolExecutor;
    import java.util.concurrent.TimeUnit;
    import java.util.concurrent.locks.Lock;
    import java.util.concurrent.locks.ReentrantLock;

    import net.sf.json.JSONObject;

    import org.eclipse.jetty.websocket.WebSocket;
    import org.eclipse.jetty.websocket.WebSocketClient;
    import org.eclipse.jetty.websocket.WebSocketClientFactory;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.context.ApplicationListener;
    import org.springframework.context.event.ContextRefreshedEvent;
    import org.springframework.core.io.DefaultResourceLoader;
    import org.springframework.core.io.ResourceLoader;
    import org.springframework.web.context.ContextLoader;
    import org.springframework.web.context.WebApplicationContext;

    import com.ylink.ylpay.client.Client;
    import com.ylink.ylpay.model.TTDOrdermp;
    import com.ylink.ylpay.ttdordermp.service.ApplyMoneyService;
    import com.ylink.ylpay.ttdordermp.service.TTDOrderService;

    public class ClientAction implements ApplicationListener<ContextRefreshedEvent>{
	protected static Logger logger = LoggerFactory.getLogger( ClientAction.class);
	
	private static String FILE_PATH = "classpath:merchant.properties";
	private static ResourceLoader resourceLoader = new DefaultResourceLoader();
	private static Properties properties = new Properties();
	private static int capacity = 500; // 默认容量为500
	private static int coreThreadNumber = 10; // 默认线程10
	public static int threadQueueSize = 2000;
	public static long sleep = 300; // 休眠半毫秒
	private static BlockingQueue<String> queue = null;
	private static ThreadPoolExecutor executor = null;
	
	static{
		try {
			properties.load(resourceLoader.getResource(FILE_PATH).getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		 String queueSize = properties.getProperty("queueSize");
		 String threadNumber = properties.getProperty("threadNumber");
		 String threadQueueSizeStr = properties.getProperty("threadQueueSize");
		 String sleepStr = properties.getProperty("sleep");
		if(queueSize!=null
				&& !"".equals(queueSize)){
			try{
				capacity = Integer.parseInt(queueSize);
			}catch (NumberFormatException e) {
				System.out.println("转换queueSize错误，"+e.getMessage());
				capacity = 500;
			}
		}
		if(threadNumber!=null
				&& !"".equals(threadNumber)){
			try{
				coreThreadNumber = Integer.parseInt(threadNumber);
			}catch (NumberFormatException e) {
				System.out.println("转换threadNumber错误，"+e.getMessage());
				coreThreadNumber = 10;
			}
		}
		if(sleepStr!=null
				&& !"".equals(sleepStr)){
			try{
				sleep = Long.parseLong(sleepStr);
			}catch (NumberFormatException e) {
				System.out.println("转换sleepStr错误，"+e.getMessage());
				sleep = 300;
			}
		}
		if(threadQueueSizeStr!=null
				&& !"".equals(threadQueueSizeStr)){
			try{
				threadQueueSize = Integer.parseInt(threadQueueSizeStr);
			}catch (NumberFormatException e) {
				System.out.println("转换sleepStr错误，"+e.getMessage());
				threadQueueSize = 300;
			}
		}
		System.out.println("初始化queue["+capacity+"]和ThreadPool["+coreThreadNumber+"]["+coreThreadNumber*2+"]和sleep["+sleep+"]和threadPoolSize["+threadQueueSize+"]");
		queue = new LinkedBlockingQueue<String>(capacity);
		executor = new ThreadPoolExecutor(coreThreadNumber, coreThreadNumber*2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(threadQueueSize), new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					logger.info("进入线程池拒绝策略，线程队列大小[{}]，时间:[{}],线程[{}]", new Object[]{executor.getQueue().size(), new Date(), Thread.currentThread().getName()});
					executor.getQueue().put(r);
					logger.info("进入线程池拒绝策略，添加新任务后，线程队列大小[{}]，时间:[{}],线程[{}]", new Object[]{executor.getQueue().size(), new Date(), Thread.currentThread().getName()});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//将银联智慧推送的消息写入一个单独的文件
	public static void writeUpsmartLog(String str) {
        try {
        	String file_path = "/home/br_monap/apache-tomcat-6.0.37/logs/upsmart.log";
    //        	String file_path = "F:/upsmart.log";
        	File log_file = new File(file_path);
        	if(!log_file.exists()){
        		log_file.createNewFile();
        	}
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	FileOutputStream out = new FileOutputStream(log_file, true); //追加用true        
        	StringBuffer sb = new StringBuffer();
        	sb.append(sdf.format(new Date()) + "-");
        	sb.append(str + "\n");
        	out.write(sb.toString().getBytes("utf-8"));
        	out.close();
        } catch(IOException ex) {
           ex.printStackTrace();
        }
    }
	
	private void initResourcesForWS() {
		String url = Client.DESTINATION;
    //		url = "ws://127.0.0.1:8080/websocket/websocket/test";
		System.out.println("访问URI：" + url);

		try {
			WebSocketClientFactory wscf = new WebSocketClientFactory(); 
			wscf.start();
			WebSocketClient webSocketClient = wscf.newWebSocketClient();
			webSocketClient.setProtocol(Client.PROTOCOL);
			webSocketClient.setMaxIdleTime(Integer.MAX_VALUE);
			webSocketClient.open(new URI(url),
					new WebSocket.OnTextMessage() {
						/**
						 * 处理智惠推送消息的接口
						 */
						public void onMessage(String message) {
							logger.info("接收到智惠推送消息:" + message);
							//added by xuyang
							if(message == null
									|| message.isEmpty()){
								logger.info("message is empty...return");
								return;
							}
							writeUpsmartLog("接收到智惠推送消息:" + message);
							JSONObject jb = JSONObject.fromObject(message);
							 String data = (String)jb.get("data");
							 String sign = (String)jb.get("uuid");
							 String[] datas = retMesg(data);
							 if(datas!=null){
								 for(String strData:datas){
									 try {
										queue.put(strData+"|"+sign); // 数据入队列
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								 }
							 }
						}

						public void onOpen(Connection connection) {
							logger.info("与智惠建立链接");
							//added by xuyang
							writeUpsmartLog("与智惠建立链接");
							try {
								connection.sendMessage("Hello upsmart, I'm " + Client.ACCOUNT);
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}

						public void onClose(int closeCode, String message) {
							logger.info("与智惠的链接断开：" + message);
							//added by xuyang
							writeUpsmartLog("与智惠的链接断开:" + message);
							initResourcesForWS();
						}
					});
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String[] retMesg(String mesg){
		String[] mchnts = mesg.split("\\|");
		if(mchnts.length>=1){
			String[] strs = mchnts[0].split(",");
			if(strs[0].length()>20){
				return mchnts;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println("--------------开始-------------------");
		ClientAction client = new ClientAction();
		client.initResourcesForWS();
		System.out.println("-------------结束-------------------");
		
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			//root application context 没有parent，他就是老大.
			logger.info("excecute onApplicationEven...");
			initResourcesForWS();
			new Thread(new HandleThread(queue, executor)).start();
	      }
		
	}
    }
    class HandleThread implements Runnable{
	
	private BlockingQueue<String> queue;
	
	private ThreadPoolExecutor executor;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final Lock lock = new ReentrantLock(); // 总线程锁。因为每个任务都是一个新的Thread，为确保共享数据的准确性，在这里定义锁
	
	HandleThread(BlockingQueue<String> queue, ThreadPoolExecutor executor) {
		this.queue = queue;
		this.executor = executor;
	}

	@Override
	public void run() {
		while(true){ //while true 会一直占用CPU时间，造成CUP比例一直都是100%以上。。
			try {
				Thread.sleep(ClientAction.sleep); // 这个地方休眠一段时间，给CPU切换时间
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			String data = queue.poll();
			if(data!=null
					&& !"".equals(data)){
				if(executor.getQueue().size()>=ClientAction.threadQueueSize){
					this.logger.info("线程池任务队列已满,大小[{}],线程[{}]", new Object[]{executor.getQueue().size(), Thread.currentThread().getName()});
				}
				executor.execute(new HandleDataService(lock, data));
				this.logger.info("添加新任务,线程[{}]", Thread.currentThread().getName());
			}
		}
	}
    }
    class HandleDataService implements Runnable{
	
	private WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();

	private String data;
	
	private Lock lock;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private TTDOrderService tTDOrderService = (TTDOrderService) wac.getBean("tTDOrderService");
	
	private ApplyMoneyService applyMoneyService = (ApplyMoneyService) wac.getBean("applyMoneyService");
	
	HandleDataService(Lock lock, String data){
		this.lock = lock;
		this.data = data;
	}
	
	@Override
	public void run() {
		String[] strDatas = data.split("\\|");
		String data = strDatas[0];
		String sign = strDatas[1];
		
		TTDOrdermp dermp = tTDOrderService.addTTDOrdermp(data, sign);
		
		if(dermp == null){
			return;
		}
		try{
			lock.lock(); // 加锁
			applyMoneyService.createOrUpdateApplyMoney(dermp);
		}catch (Exception e) {
			logger.info("操作汇总表异常", e);
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
    }
```

<font color="#4F94CD">遇到的问题：</font>

1. 一开始用ThreadPoolExecutor的时候，给了一个无界队列存放，处理速度是快了，但是内存占用率很大。。并且while(true) 不给CPU切换时间，很快就OOM了。。
2. 给ThreadPoolExecutor添加了容量，内存是降下来了，但是还是在运行一段时间后，报了OOM了。。加容量的时候给线程池订制了拒绝策略，threadPool put Runnable，这个意思就是如果threadPool中的任务队列满了，将线程阻塞， 直到threadPool任务队列可用。。
3. 经过步骤2的改造后，基本排除了queue和threadPool queue会造成OOM的情况，当时就猜想是在while(true)里面造成的OOM，可是由于不确定性，就在queue里面想办法
4. 最后还是回到while(true)中，猜想是由于while(true)一直循环，CPU切片时间太长，一直是100%的利用率，造成CPU没有时间切换线程，所以JVM GC没有及时回收堆内存造成OOM。。
5. 改造while(true)之前，CPU一直是100%甚至超频。。。在while(true)中让其sleep一会，CPU使用率一下就降到3.x、1.x。。。

#### 在实际的应用中，还发现一个问题，这个GC不会回收，造成OOM

> 通过不断的尝试，发现内存的开销都是在BlockingQueue和ThreadPoolExecutor、sqlConnection
所以修改的地方就在这个几个地方。

> 分析了下，把DAO中的实现全都改成connection实现方式，手动关闭连接，将程序中的BlockingQueue和ThreadPoolExecutor

> 声明全部修改为只用private修饰，不再使用static等修饰

修改后的代码如下：

```
    public class ClientAction implements ApplicationListener<ContextRefreshedEvent>{
	protected Logger logger = LoggerFactory.getLogger( this.getClass());
	
	private String FILE_PATH = "classpath:merchant.properties";
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private Properties properties = new Properties();
	private int capacity = 500; // 默认容量为500
	private int coreThreadNumber = 10; // 默认线程10
	private int threadQueueSize = 200;
	static long getDataSleep = 100;
	private BlockingQueue<String> queue;
	private ThreadPoolExecutor executor;
	
	ClientAction(){
		try {
			properties.load(resourceLoader.getResource(FILE_PATH).getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		 String queueSize = properties.getProperty("queueSize");
		 String threadNumber = properties.getProperty("threadNumber");
		 String threadQueueSizeStr = properties.getProperty("threadQueueSize");
		if(queueSize!=null
				&& !"".equals(queueSize)){
			try{
				capacity = Integer.parseInt(queueSize);
			}catch (NumberFormatException e) {
				System.out.println("转换queueSize错误，"+e.getMessage());
				capacity = 500;
			}
		}
		if(threadNumber!=null
				&& !"".equals(threadNumber)){
			try{
				coreThreadNumber = Integer.parseInt(threadNumber);
			}catch (NumberFormatException e) {
				System.out.println("转换threadNumber错误，"+e.getMessage());
				coreThreadNumber = 10;
			}
		}
		if(threadQueueSizeStr!=null
				&& !"".equals(threadQueueSizeStr)){
			try{
				threadQueueSize = Integer.parseInt(threadQueueSizeStr);
			}catch (NumberFormatException e) {
				System.out.println("转换sleepStr错误，"+e.getMessage());
				threadQueueSize = 300;
			}
		}
		String sleepStr = properties.getProperty("sleep");
		if(sleepStr!=null
				&& !"".equals(sleepStr)){
			try{
				getDataSleep = Long.parseLong(sleepStr);
			}catch (NumberFormatException e) {
				System.out.println("转换sleep错误，"+e.getMessage());
				getDataSleep = 100;
			}
		}
		
		queue = new LinkedBlockingQueue<String>(capacity);
		executor = new ThreadPoolExecutor(coreThreadNumber, coreThreadNumber*2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(threadQueueSize), new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				logger.info("队列满，异常处理开始...");
				logger.info("数据队列数量：{}；线程池等待队列数量：{}", new Object[]{queue.size(),executor.getQueue().size()});
				long beginTime = Calendar.getInstance().getTimeInMillis();
				try {
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("队列满，异常处理结束,耗时{}。", (Calendar.getInstance().getTimeInMillis()-beginTime));
			}
		});
		logger.info("初始化queue[{}],threadPool[{}],runnableQueue[{}],sleep[{}]", new Object[]{capacity, coreThreadNumber, threadQueueSize, getDataSleep});
	}
	//将银联智慧推送的消息写入一个单独的文件  added by xuyang
	public void writeUpsmartLog(String str) {
        try {
        	String file_path = "/home/br_monap/apache-tomcat-6.0.37/logs/upsmart.log";
    //        	String file_path = "F:/upsmart.log";
        	File log_file = new File(file_path);
        	if(!log_file.exists()){
        		log_file.createNewFile();
        	}
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	FileOutputStream out = new FileOutputStream(log_file, true); //追加用true        
        	StringBuffer sb = new StringBuffer();
        	sb.append(sdf.format(new Date()) + "-");
        	sb.append(str + "\n");
        	out.write(sb.toString().getBytes("utf-8"));
        	out.close();
        } catch(IOException ex) {
           ex.printStackTrace();
        }
    }
	
	private void initResourcesForWS() {
		String url = Client.DESTINATION;
    //		url = "ws://127.0.0.1:8080/websocket/websocket/test";
		System.out.println("访问URI：" + url);

		try {
			WebSocketClientFactory wscf = new WebSocketClientFactory(); 
			wscf.start();
			WebSocketClient webSocketClient = wscf.newWebSocketClient();
			webSocketClient.setProtocol(Client.PROTOCOL);
			webSocketClient.setMaxIdleTime(Integer.MAX_VALUE);
			webSocketClient.open(new URI(url),
					new WebSocket.OnTextMessage() {
						/**
						 * 处理智惠推送消息的接口
						 */
						public void onMessage(String message) {
							logger.info("接收到智惠推送消息:" + message);
							if(message == null
									|| message.isEmpty()){
								logger.info("message is empty...return");
								return;
							}
							writeUpsmartLog("接收到智惠推送消息:" + message);
							JSONObject jb = JSONObject.fromObject(message);
							 String data = (String)jb.get("data");
							 String sign = (String)jb.get("uuid");
							 String[] datas = retMesg(data);
							 if(datas!=null){
								 for(String strData:datas){
									 try {
										queue.put(strData+"|"+sign); // 数据入队列
									} catch (InterruptedException e) {
										logger.error("数据入队列，异常：");
										e.printStackTrace();
									}
								 }
							 }
						}

						public void onOpen(Connection connection) {
							logger.info("与智惠建立链接");
							writeUpsmartLog("与智惠建立链接");
							try {
								connection.sendMessage("Hello upsmart, I'm " + Client.ACCOUNT);
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}

						public void onClose(int closeCode, String message) {
							logger.info("与智惠的链接断开：" + message);
							writeUpsmartLog("与智惠的链接断开:" + message);
							initResourcesForWS();
						}
					});
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String[] retMesg(String mesg){
		String[] mchnts = mesg.split("\\|");
		if(mchnts.length>=1){
			String[] strs = mchnts[0].split(",");
			if(strs[0].length()>20){
				return mchnts;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println("--------------开始-------------------");
		ClientAction client = new ClientAction();
		client.initResourcesForWS();
		System.out.println("-------------结束-------------------");
		
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			//root application context 没有parent，他就是老大.
			logger.info("excecute onApplicationEven...");
			initResourcesForWS();
			new HandleThread(queue, executor).start();
	      }
	}
    }
    class HandleThread extends Thread{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private BlockingQueue<String> queue;
	
	private ThreadPoolExecutor executor;
	
	final Lock lock = new ReentrantLock(); // 总线程锁。因为每个任务都是一个新的Thread，为确保共享数据的准确性，在这里定义锁
	
	HandleThread(BlockingQueue<String> queue, ThreadPoolExecutor executor){
		this.queue = queue;
		this.executor = executor;
	}

	@Override
	public void run() {
		while(true){
			int dataQueueSize = queue.size();
			logger.info("当前取的数据队列大小:[{}]", dataQueueSize);
			if(dataQueueSize>0){
				List<String> dataLists = new ArrayList<String>();
				for(int i=0; i<dataQueueSize; i++){
					String data = queue.poll();
					if(data!=null
							&& !"".equals(data)){
						dataLists.add(data);
					}
					data = null;
					if(dataLists.size()>=10){
						List<String> dataTempList = new ArrayList<String>();
						dataTempList.addAll(dataLists);
						executor.execute(new HandleDataService(lock, dataTempList));
						dataLists.clear();
					}
				}
				if(!dataLists.isEmpty()){
					List<String> dataTempList = new ArrayList<String>();
					dataTempList.addAll(dataLists);
					executor.execute(new HandleDataService(lock, dataTempList));
					dataLists.clear();
				}
				dataLists = null;
			}
			
			logger.info("线程池执行任务线程数:[{}]", executor.getActiveCount());
			
			try {
				Thread.sleep(ClientAction.getDataSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
    }
    class HandleDataService implements Runnable{
	
	private WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();

    //	private String data;
	
	private List<String> dataLists;
	
	private Lock lock;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private TTDOrderService tTDOrderService = (TTDOrderService) wac.getBean("tTDOrderService");
	
	private ApplyMoneyService applyMoneyService = (ApplyMoneyService) wac.getBean("applyMoneyService");
	
	/*HandleDataService(Lock lock, String data){
		this.lock = lock;
		this.data = data;
	}*/
	
	HandleDataService(Lock lock, List<String> dataLists) {
		this.lock = lock;
		this.dataLists = dataLists;
	}
	
	@Override
	public void run() {
		
		Long beginTime = Calendar.getInstance().getTimeInMillis();
		logger.info("数据[{}]处理开始, 时间[{}]", new Object[]{dataLists, beginTime});
		
		for(int i=0,len=dataLists.size(); i<len; i++){
			
			String dataStr = dataLists.get(i);
			String[] strDatas = dataStr.split("\\|");
			String data = strDatas[0];
			String sign = strDatas[1];
			
			TTDOrdermp dermp = tTDOrderService.addTTDOrdermp(data, sign);
			
			if(dermp == null){
				continue;
			}
			logger.info("加锁开始");
			Long lockBeginTime = Calendar.getInstance().getTimeInMillis();
			try{
				lock.lock(); // 加锁
				logger.info("加锁结束，耗时：{}",(Calendar.getInstance().getTimeInMillis()-lockBeginTime));
				applyMoneyService.createOrUpdateApplyMoney(dermp);
			}catch (Exception e) {
				logger.info("操作汇总表异常", e);
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
		}
		
		System.gc();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.info("数据[{}]处理结束，耗时：{}", new Object[]{dataLists,(Calendar.getInstance().getTimeInMillis()-beginTime)});
	}

	@Override
	protected void finalize() throws Throwable {
		logger.info("单个任务线程回收");
		lock = null;
		dataLists.clear();
		dataLists = null;
		applyMoneyService = null;
		tTDOrderService = null;
		wac = null;
		logger = null;
		super.finalize();
	}
	
    }
```

