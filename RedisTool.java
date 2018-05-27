package com.htiiot.subscription.redis;

import com.htiiot.common.util.JedisMultiPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author: zhangjj Time: 2017.4.20 16:23:15
 */
public class RedisTool {
	
	private static JedisPool pool = null;
	private static Jedis jedis = null;
	
	private static volatile RedisTool redisTool = null;

	public RedisTool() {
		jedis =JedisMultiPool.getJedis();
	}
	
	public static RedisTool getInstance() {
		if (redisTool == null) {
            synchronized (RedisTool.class) {
               if (redisTool == null) {
            	   redisTool = new RedisTool();
               }
           }
		}
		return redisTool;
	}

	public synchronized void close() {
		try {
//			System.out.println(jedis);
			if(jedis != null){
				jedis.close();
			}
		} catch (Exception e) {
			System.out.println("jedis关闭出错..............");
			e.printStackTrace();
		}
//		if(jedis.isConnected()){
//			jedis.disconnect();
//		}
	}

	public synchronized String getKey(String key) {
		key = "subscription_eventid_" + key;
		return jedis.get(key);
	}
	
	public synchronized void setKey(String key,String value){
		key = "subscription_eventid_" + key;
		jedis.set(key, value);
		//设置key的过期时间;单位为秒;
		jedis.expire(key, 10*60);
	}
	
	public synchronized void updateValue(String key,String value){
		String newkey = "subscription_eventid_" + key;
		//删除旧的
		jedis.del(newkey);
		//现在新的
		setKey(key,value);
	}
	
	public synchronized boolean existKey(String key){
		key = "subscription_eventid_" + key;
		return jedis.exists(key);
	}
}
