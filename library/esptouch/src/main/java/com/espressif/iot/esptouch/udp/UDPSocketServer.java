package com.espressif.iot.esptouch.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPSocketServer {
	private DatagramPacket mReceivePacket = new DatagramPacket(new byte[64], 64);;
	private DatagramSocket mServerSocket;
	private boolean mIsClosed = false;

	/**
	 * Constructor of UDP Socket Server
	 *
	 * @param port          the Socket Server port
	 * @param socketTimeout the socket read timeout
	 */
	public UDPSocketServer(int port, int socketTimeout) {
		try {
			this.mServerSocket = new DatagramSocket(port);
			this.mServerSocket.setSoTimeout(socketTimeout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the socket timeout in milliseconds
	 *
	 * @param timeout the timeout in milliseconds or 0 for no timeout.
	 * @return true whether the timeout is set suc
	 */
	public boolean setSoTimeout(int timeout) {
		try {
			this.mServerSocket.setSoTimeout(timeout);
			return true;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Receive one byte from the port and convert it into String
	 *
	 * @return
	 */
	public byte receiveOneByte() {
		try {
			mServerSocket.receive(mReceivePacket);
			return mReceivePacket.getData()[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Byte.MIN_VALUE;
	}

	/**
	 * Receive specific length bytes from the port and convert it into String
	 * 21,24,-2,52,-102,-93,-60
	 * 15,18,fe,34,9a,a3,c4
	 *
	 * @return
	 */
	public byte[] receiveSpecLenBytes(int len) {
		try {
			mServerSocket.receive(mReceivePacket);
			byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
			if (recDatas.length != len) {
				return null;
			}
			return recDatas;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void interrupt() {
		close();
	}

	public synchronized void close() {
		if (!this.mIsClosed) {
			mServerSocket.close();
			this.mIsClosed = true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
