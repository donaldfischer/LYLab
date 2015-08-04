package net.vicp.lylab.server.runtime;

import java.net.ServerSocket;

import net.vicp.lylab.core.interfaces.InitializeConfig;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Config;
import net.vicp.lylab.utils.internet.ToClientLongSocket;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.Task;

/**
 * A raw socket can be used for communicating with server, you need close socket after using it.
 * Actually, this class is not as useful as I thought
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2015.07.01
 * @version 1.0.0
 */
public class SyncServer extends Task implements LifeCycle, InitializeConfig {
	private static final long serialVersionUID = 883892527805494627L;
	protected volatile boolean running = true;
	protected ServerSocket serverSocket;
	protected LYTaskQueue lyTaskQueue = null;
	protected Config config;
	
	@Override
	public void initialize() {
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public void exec() {
		try {
			if(this.lyTaskQueue == null)
				lyTaskQueue = new LYTaskQueue();
			try {
				lyTaskQueue.setMaxQueue(config.getInteger("maxQueue"));
			} catch (Exception e) { }
			try {
				lyTaskQueue.setMaxThread(config.getInteger("maxThread"));
			} catch (Exception e) { }
			
			serverSocket = new ServerSocket(config.getInteger("port"));
			while (running) {
				lyTaskQueue.addTask(new ToClientLongSocket(serverSocket, new SimpleHeartBeat()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void obtainConfig(Config config) {
		this.config = config;
	}

	public void setLyTaskQueue(LYTaskQueue lyTaskQueue) {
		if(this.lyTaskQueue == null)
			this.lyTaskQueue = lyTaskQueue;
	}

}
