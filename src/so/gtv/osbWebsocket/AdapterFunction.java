package so.gtv.osbWebsocket;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class AdapterFunction implements Function {
	
	@Override
	public void call(JSONObject json) {
		if(null==json)return;
		String status = json.getString("status");
		String error = json.getString("error");
		StringBuffer sb = new StringBuffer();
		 if(StringUtils.isNotBlank(error)&&"error".equals(status)){
				sb.append("错误 → ").append(error).append(",");
				sb.append("数据 → ").append(json.toJSONString()).append(",");
			    if(sb.length()>0){
			    	sb = sb.delete(sb.length()-1, sb.length());
			    }
			    System.out.println(sb.toString());
		 }else{
			    sb.append("数据 → ").append(json.toJSONString());
			    System.out.println(sb.toString());
		 }
	}
}
