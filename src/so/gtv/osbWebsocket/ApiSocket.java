package so.gtv.osbWebsocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 客户端类
 * @author Administrator
 */
@WebSocket(maxTextMessageSize = 64 * 2048)
public class ApiSocket {
	private static ApiSocket as = new ApiSocket();
	private static WebSocketClient client =  new WebSocketClient() ;
	private static Session session ;
	
	public static void main(String[] args) throws Exception {
		String password =   "a4b3c2d1";
		ApiCmd api = ApiSocket.getApi("127.0.0.1");
		if(null!=api){
			api.CheckAuthenticate(password,new AdapterFunction() {
				@Override
				public void call(JSONObject data) {
						if(null==data)return;
						String status = data.getString("status");
						String error = data.getString("error");
						if (StringUtils.isNotBlank(error)&&"error".equals(status)) {
							System.out.println("验证失败.");
							as.onAuthenticationFailure();
						} else {
							System.out.println("验证成功.");
							as.onAuthenticationSuccess();
							//api.SetSceneItemPosition("图像", 10, 10, null);
							api.SetSceneItemTransform("图像",4, 3, 0, null);
//							api.GetCurrentProfile(new Function(){
//								@Override
//								public void call(JSONObject data) {
//									System.out.println(data.getString("profile-name"));
//								}
//							});
//							api.SetCurrentProfile("Untitled", null);
							//api.UpdateService( "rtmp://www.gtv.so/live","test", null);
						//api.StartStopStreaming();
						//api.StartStreaming();
							//api.StopStreaming();
							//api.ToggleMute("麦克风/Aux", null);
							//api.SetMute("麦克风/Aux", false, null);
//							api.GetVersion(null);
//							api.GetVolume("台式音响",null);
//							api.SetVolume("台式音响", 0.7);
//							api.GetSceneList(new Function() {
//								@Override
//								public void call(JSONObject data) {
//									OBSSceneCollection osc = JSON.parseObject(data.toJSONString(),
//											OBSSceneCollection.class);
//									api.GetCurrentScene(new Function() {
//										@Override
//										public void call(JSONObject data) {
//											OBSScene obsScene = JSON.parseObject(data.toJSONString(),
//													OBSScene.class);
//											for (OBSScene o : osc.getScenes()) {
//												if (!o.getName().equals(obsScene.getName())) {
//													api.SetCurrentScene(o.getName(),new AdapterFunction());
//													break;
//												}
//											}
//										}
//									});
//								}
//							});
						}
				}
			});
		}
	}

	/**
	 * 获取命令行
	 * @return
	 */
	public static ApiCmd getApi(String host) {
		try {
			client.start();
			ClientUpgradeRequest cuq = new ClientUpgradeRequest();
			Future<Session> future = client.connect(as,new URI("ws://"+host+":4444"),cuq);
			session = future.get();
			return new ApiCmd(session);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		InetSocketAddress isa = session.getLocalAddress();
		System.out.printf("客户端已经连接地址端口: %s:%d%n", isa.getAddress(), isa.getPort());
	}
	
	@OnWebSocketClose
	public void onClose(Session session ,int code,String msg){
		InetSocketAddress isa = session.getLocalAddress();
		System.out.printf("客户端连接关闭 %s:%s,  code=%d,cmd=%s %n", isa.getAddress(),isa.getPort(),code,msg);
	}
	
	@OnWebSocketError
	public void OnError(Session session ,Throwable throwable){
		InetSocketAddress isa = session.getLocalAddress();
		System.out.printf("客户端连接错误 %s:%s,  error=%s %n", isa.getAddress(),isa.getPort(),throwable.getMessage());
	}
	
	//解决参数是数组的问
	public void onMessage(String ... msg){
		System.out.println("回调数组信息 → "+Arrays.toString(msg));
	}
	
	@OnWebSocketMessage
	public void onMessage(String msg) {
		if(StringUtils.isBlank(msg))return ;
		JSONObject json = JSON.parseObject(msg);
		String updateType = json.getString("update-type");
		if(StringUtils.isNotBlank(updateType)&&null!=json){
			  switch(updateType) {
			    case "SwitchScenes":
			    	onSceneSwitch(json.getString("scene-name"));
			      return;
			    case "ScenesChanged":
					try {
						ApiCmd api = new ApiCmd(session);
						api.GetSceneList(new Function() {
							@Override
							public void call(JSONObject data) {
								OBSSceneCollection obs_scene_coll = JSON.parseObject(data.toJSONString(),OBSSceneCollection.class);
								for(OBSScene obs_scene :obs_scene_coll.getScenes()){
									onScenesChanged(obs_scene);
								}
							}
						});
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
			      return;
			    case "TransitionListChanged":
			    	onTransitionListChanged();
			    	return;
			    case "SwitchTransition":
			    	onSwitchTransition(json.getString("transition-name"));
			    	return;
			    case "StreamStarting":
			     	onStreamStarting(json.getString("preview-only"));
			      return;
			    case "StreamStarted":
			    	onStreamStarted();
			      return;
			    case "StreamStopping":
			    	onStreamStopping(json.getString("preview-only"));
			      return;
			    case "StreamStopped":
			   	    onStreamStopped();
			      return;
			    case "RecordingStarting":
			    	onRecordingStarting();
			      return;
			    case "RecordingStarted":
			    	onRecordingStarted();
			      return;
			    case "RecordingStopping":
			    	onRecordingStopping();
			      return;
			    case "RecordingStopped":
			    	onRecordingStopped();
			      return;
			    case "StreamStatus":
			    	onStreamStatus(json);
			      return;
			    case "Exiting":
			    	onExit();
			      return;
			    default:
			    	System.err.printf("Unknown UpdateType: %s %s ", updateType, json.toJSONString());
			  }
		 }
		
		String messageId = json.getString("message-id");
		Function f = ApiCmd.get(messageId);
		if(null!=f){
			f.call(json);
			 //清理callblack
			 ApiCmd.remove(messageId);	
		}
	}

	/**
	 * 校验成功
	 */
	public void onAuthenticationSuccess(){
	}
	/**
	 * 校验失败
	 */
	public void onAuthenticationFailure(){
	}
	
	/**
	 * SwitchScenes
	 * @param scene
	 */
	public void onSceneSwitch(String scene_name){
		System.out.println(scene_name);
	}
	public void  onScenesChanged(OBSScene os){
		System.out.println(os.getName());
	}
	private void onSwitchTransition(String transition_name) {
		System.out.println("onSwitchTransition = "+transition_name);
	}
	private void onTransitionListChanged() {
		System.out.println("onTransitionListChanged");
	}
	public void  onStreamStarting(String preview_only){
		System.out.println("onStreamStarting preview_only = "+preview_only);
	}
	public void  onStreamStarted(){
		System.out.println("onStreamStarted");
	}
	public void  onStreamStopping(String preview_only){
		System.out.println("onStreamStopping preview_only = "+preview_only);
	}
	public void  onStreamStopped(){
		System.out.println("onStreamStopped");
	}
	public void  onRecordingStarting(){
		System.out.println("onRecordingStarting");
	}
	public void  onRecordingStarted(){
		System.out.println("onRecordingStarted");
	}
	public void  onRecordingStopping(){
		System.out.println("onRecordingStopping");
	}
	public void  onRecordingStopped(){
		System.out.println("onRecordingStopped");
	}
	public void  onStreamStatus(JSONObject json){
		System.out.println(json.toJSONString());
	}
	public void  onExit(){
		System.out.println("onexit");
	}
}