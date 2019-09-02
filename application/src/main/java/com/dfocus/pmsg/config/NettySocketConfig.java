package com.dfocus.pmsg.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.dfocus.pmsg.common.utils.JwtRsaUtils;
import com.dfocus.pmsg.service.atom.ISecretService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * NettySocketConfig
 *
 * @author XiongNeng
 * @version 1.0
 * @since 2018/1/19
 */
@Slf4j
@Configuration
public class NettySocketConfig {

	@Resource
	private SocketIoProperties socketIoProperties;

	@Autowired
	private ISecretService secretService;

	@Bean
	public SocketIOServer socketIOServer() {
		/*
		 * 创建Socket，并设置监听端口
		 */
		com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
		// 设置主机名，默认是0.0.0.0
		// config.setHostname("localhost");
		// 设置监听端口
		config.setPort(socketIoProperties.getSocketPort());
		// 协议升级超时时间（毫秒），默认10000。HTTP握手升级为ws协议超时时间
		config.setUpgradeTimeout(10000);
		// Ping消息间隔（毫秒），默认25000。客户端向服务器发送一条心跳消息间隔
		config.setPingInterval(socketIoProperties.getPingInterval());
		// Ping消息超时时间（毫秒），默认60000，这个时间间隔内没有接收到心跳消息就会发送超时事件
		config.setPingTimeout(socketIoProperties.getPingTimeout());
		// 握手协议参数使用JWT的Token认证方案
		config.setAuthorizationListener(data -> {
			// jwt认证
			String projectId = data.getSingleUrlParam("projectId");
			String token = data.getSingleUrlParam("token");
			return authenticate(token, projectId);

		});

		final SocketIOServer server = new SocketIOServer(config);

		return server;
	}

	/**
	 * 根据jwt认证授权
	 */
	private boolean authenticate(String token, String projectId) {
		try {
			String publicKey = secretService.selectPublicKeyByProjectId(projectId);
			JwtRsaUtils.verify(publicKey, token);
		}
		catch (Exception e) {
			log.warn("auth fail: " + e.getMessage());
			return false;
		}
		return true;
	}

}
