package com.example.carcontrollerapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectFragment extends Fragment {
	private static final String IP = "192.168.1.103";

	protected static final String FRAGMENTSOCKET = "SocketFragment";
	
	private Button bConnectButton;
	private TextView tLocalIPTextView;
	private Tools tools;
	private SocketThread socketThreadClient;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_connect, parent, false);
		
		tools = new Tools();
		socketThreadClient = null;
		
		tLocalIPTextView = (TextView)v.findViewById(R.id.TextViewLocapIP);		
		tLocalIPTextView.setText(tools.GetLocalIP());
		
		bConnectButton = (Button)v.findViewById(R.id.ButtonConnect);
		bConnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( ( socketThreadClient == null ) || ( socketThreadClient.isCreatedSuccessful() == false ) ){
					socketThreadClient = new SocketThread(14, IP);
					socketThreadClient.start();
					
					if( socketThreadClient.isCreatedSuccessful() == true ){
						Intent i = new Intent(getActivity(), ControllerActivity.class);
						startActivity(i);
						Log.d(FRAGMENTSOCKET, "Socket connect is successful!");
					}else {
						Toast.makeText(getContext(), "Connect Error!", Toast.LENGTH_SHORT).show();
					}	
				}	
			}
		});
	
		return v;
	}

}
