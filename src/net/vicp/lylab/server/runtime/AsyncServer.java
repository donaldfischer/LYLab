package net.vicp.lylab.server.runtime;

import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.internet.async_test.AsyncSocket;
import net.vicp.lylab.utils.internet.async_test.Transfer;
import net.vicp.lylab.utils.tq.LYTaskQueue;
import net.vicp.lylab.utils.tq.LoneWolf;

/**
 * A server runtime based on Async-Server, AsyncSocket.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young
 * @since 2016.01.22
 * @version 1.0.0
 */
public class AsyncServer extends LoneWolf implements LifeCycle {
	private static final long serialVersionUID = -6058980641184489541L;
	
	protected AtomicBoolean closed = new AtomicBoolean(true);
	protected AsyncSocket asyncSocket;
	protected Transfer transfer;
	protected LYTaskQueue taskQueue = null;
	protected Aop aop;
	protected Integer port = null;
	protected boolean longServer = true;
	protected Protocol protocol;

	@Override
	public void initialize() {
		if(!closed.compareAndSet(true, false))
			return;
		this.begin("Sync Server - Main Thread");
	}
	
	@Override
	public void close() throws Exception {
		if(!closed.compareAndSet(false, true))
			return;
		asyncSocket.close();
		transfer.close();
		this.callStop();
	}
	
	public static void main(String[] args) throws Exception {
	}

	@Override
	public void exec() {
		transfer.setTaskQueue(taskQueue);
		
		asyncSocket = new AsyncSocket(port, transfer, new SimpleHeartBeat());
		aop.setProtocol(protocol);
		asyncSocket.setAopLogic(aop);
		
		transfer.initialize();
		asyncSocket.initialize();
	}

	public AsyncSocket getAsyncSocket() {
		return asyncSocket;
	}

	public void setAsyncSocket(AsyncSocket asyncSocket) {
		this.asyncSocket = asyncSocket;
	}

	public Transfer getTransfer() {
		return transfer;
	}

	public void setTransfer(Transfer transfer) {
		this.transfer = transfer;
	}

	public LYTaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LYTaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public Aop getAop() {
		return aop;
	}

	public void setAop(Aop aop) {
		this.aop = aop;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isLongServer() {
		return longServer;
	}

	public void setLongServer(boolean longServer) {
		this.longServer = longServer;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public boolean isClosed() {
		return closed.get();
	}

}