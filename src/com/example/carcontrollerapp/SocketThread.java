package com.example.carcontrollerapp;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Scanner;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketThread extends Thread {
	
	public static final String TAGSOCKET = "SocketThread";
	
	private InputStream sInputStream;
	private OutputStream sOutStream;
	private Scanner sScanner;
	private Socket mSocket = null;
	private BufferedWriter sBufferedWriter;
	private boolean recvPermit = false;
	private Handler recvHandler;
	private Handler SendFileCompleteHandler;
	private String sDestinationIP;
	private boolean isCreatedSuccessfulFlag;
	private int TestOrSendFile;
	private String sFileName;

	//用于发送文件11
	public SocketThread(String destinationIP, int options, Handler handler){
		TestOrSendFile = options;
		SendFileCompleteHandler = handler;
		sDestinationIP = destinationIP; 
	}
	
	//用于即时通讯12
	public SocketThread(Handler handler, String destinationIP, int options){
		TestOrSendFile = options;
		recvHandler = handler;
		sDestinationIP = destinationIP; 
	}
	
	
	//用于刷新下载网页的网址13
	public SocketThread(int options, String destinationIP, Handler handler){
		TestOrSendFile = options;
		recvHandler = handler;
		sDestinationIP = destinationIP; 
	}
	
	//用于纯发送的小车控制14
	public SocketThread(int options, String destinationIP){
		TestOrSendFile = options;
		sDestinationIP = destinationIP; 
	}
	
	public void socketInit(){
		try {
			sInputStream = mSocket.getInputStream();
			sOutStream = mSocket.getOutputStream();
			
			sBufferedWriter = new BufferedWriter(new OutputStreamWriter(sOutStream, "UTF-8"));
			setRecvPermit(true);
			} catch (Exception e) {
				Log.d(TAGSOCKET, "Socket init Error!");
		}
	}
	
	public Boolean socketConnect(){
		try {
			mSocket = new Socket(sDestinationIP, 60000);
			setOnIsCreatedSuccessfulFlag();
			return true;
			
			} catch (Exception e) {

			Log.d(TAGSOCKET,"Connect Error!");
			
			setOffIsCreatedSuccessfulFlag();
			
			if( TestOrSendFile == 12){
				Message msg = recvHandler.obtainMessage();
				msg.obj = "Connect Error!";
				recvHandler.sendMessage(msg);
				}
            
			return false;
		}
	}

	public void socketRecv(){
		
		String recvBuf;
		
		while(mSocket.isConnected()){
			try {
				Scanner sRecv = new Scanner(sInputStream);
				while(sRecv.hasNextLine()){
					recvBuf = sRecv.nextLine();
					
					sendMessage_recvURL(0, recvBuf);
                    
					Log.d(TAGSOCKET,"Receiving: " + recvBuf);
				}
			} catch (Exception e) {
				Log.d(TAGSOCKET,"Receiving Error!");
			}
		}	
	}
	
	public void soketRecvSingleString(){
		String recvBuf;
		
		if(mSocket.isConnected()){
			try {
				Scanner sRecv = new Scanner(sInputStream);
				while(sRecv.hasNextLine()){
					recvBuf = sRecv.nextLine();
					
					sendMessage_recvURL(1, recvBuf);
                    
					Log.d(TAGSOCKET,"Receiving: " + recvBuf);
				}
			} catch (Exception e) {
				Log.d(TAGSOCKET,"Receiving Error!");
			}
		}	
	}
	
	public void socketSend(String sSend){
		try {
//			sBufferedWriter.write(sSend + "\n");
			sBufferedWriter.write(sSend);
			sBufferedWriter.flush();
			} catch (Exception e) {
				Log.d(TAGSOCKET,"Sending Error!");
		}
	}
	
	public void sendFile(File file) throws FileNotFoundException{
		final int BUFFERSIZE = 1024;
		byte[] sendBuffer = new byte[BUFFERSIZE];
		long fileLengthByte = 0;
		
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			fileLengthByte = randomAccessFile.length();
			randomAccessFile.close();
			
			FileInputStream fileInputStream = new FileInputStream(file);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(sOutStream);
			
			dataOutputStream.writeUTF(file.getName());
			dataOutputStream.writeLong(fileLengthByte);
			sendMessage(1, file.getName());
			
			while( dataInputStream.read(sendBuffer) != -1){
				Log.d(TAGSOCKET,sendBuffer.toString());
				dataOutputStream.write(sendBuffer);
				dataOutputStream.flush();
			}
			dataInputStream.close();
			dataOutputStream.close();
			sendMessage(0, null);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAGSOCKET,"Sending Byte Error!");
			e.printStackTrace();
		}
	}
	
	private void sendMessage_recvURL(int what, String content){
		Message msg = recvHandler.obtainMessage();
		msg.what = what;
		msg.obj = content;
		recvHandler.sendMessage(msg);
	}
	
	private void sendMessage(int what, String content){
		Message msg = SendFileCompleteHandler.obtainMessage();
		msg.what = what;
		msg.obj = content;
		SendFileCompleteHandler.sendMessage(msg);
	}
	
	@Override
	public void run(){
		switch (TestOrSendFile) {
		case 11:
			if((socketConnect() == true) && (sFileName != null)){
				socketInit();
				try {
					sendFile(new File(sFileName));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					disConnected();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 12:
			if(socketConnect() == true){
				socketInit();
				soketRecvSingleString();		
			}
			break;
		
		case 13:
			if(socketConnect() == true){
				socketInit();
				soketRecvSingleString();
				try {
					disConnected();
				} catch (IOException e) {
					Log.d(TAGSOCKET,"Disconnect Wrong");
					e.printStackTrace();
				}
			}
			break;
		
		case 14:
			if(socketConnect() == true){
				socketInit();		
			}
			break;
			
		default:
			Log.d(TAGSOCKET,"Not Test or Send File");
			break;		
		} 
		
	}
	
	public class FileSendHandler extends Handler{
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case 0:
				sFileName = msg.obj.toString();
				Log.d(TAGSOCKET, sFileName);
				break;

			default:
				Log.d(TAGSOCKET, "msg.what is Nothing");
				break;
			}
		}
	} 
	
	public Boolean isConnected(){
		return mSocket.isConnected();
	}
	
	public void disConnected() throws IOException{
		setOffIsCreatedSuccessfulFlag();
		mSocket.close();
	}
	
	public boolean isCreatedSuccessful(){
		return isCreatedSuccessfulFlag;
	}
	
	public void setOnIsCreatedSuccessfulFlag(){
		isCreatedSuccessfulFlag = true;
		Log.d(TAGSOCKET, "isCreatedSuccessfulFlag = true");
	}
	
	public void setOffIsCreatedSuccessfulFlag(){
		isCreatedSuccessfulFlag = false;
		Log.d(TAGSOCKET, "isCreatedSuccessfulFlag = false");
	}
	
	private void setRecvPermit(boolean permit){
		recvPermit = permit;
	}
}
