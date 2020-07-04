package wzg.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzhengu on 2020-07-01 <br/>
 */
public abstract class EasyKey<Value>
{
	public final String name;
	
	public EasyKey(String name){
		this.name = name;
	}
	
	public abstract Value get(Intent intent, Value value);
	public abstract void set(Intent intent, Value value);
	
	public Object getExtra(Intent intent){
		Bundle extras = intent.getExtras();
		return extras==null? null: extras.get(name);
	}
	
	public static class Text extends EasyKey<String>
	{
		Text(String key){
			super(key);
		}
		
		@Override
		public String get(Intent intent, String value){
			try{
				Object obj = getExtra(intent);
				if(obj!=null) return obj.toString();
			}catch(Throwable ex){
				ex.printStackTrace();
			}
			return value;
		}
		
		@Override
		public void set(Intent intent, String value){
			intent.putExtra(name, value);
		}
	}
	
	public static class Int extends EasyKey<Integer>
	{
		public Int(String key){
			super(key);
		}
		
		@Override
		public Integer get(Intent intent, Integer value){
			try{
				Object obj = getExtra(intent);
				if(obj instanceof Integer) return (Integer)obj;
				if(obj!=null) return Integer.parseInt(obj.toString());
			}catch(Throwable ex){
				ex.printStackTrace();
			}
			return value;
		}
		
		@Override
		public void set(Intent intent, Integer value){
			intent.putExtra(name, value);
		}
	}
	
	public static class Bool extends EasyKey<Boolean>
	{
		public Bool(String key){
			super(key);
		}
		
		@Override
		public Boolean get(Intent intent, Boolean value){
			try{
				Object obj = getExtra(intent);
				if(obj instanceof Boolean) return (Boolean)obj;
				if(obj!=null) return Boolean.parseBoolean(obj.toString());
			}catch(Throwable ex){
				ex.printStackTrace();
			}
			return value;
		}
		
		@Override
		public void set(Intent intent, Boolean value){
			intent.putExtra(name, value);
		}
	}
	
	public static class TextList extends EasyKey<List<String>>
	{
		public TextList(String name){
			super(name);
		}
		
		@Override
		public List<String> get(Intent intent, List<String> value){
			try{
				Object obj = getExtra(intent);
				if(obj instanceof List) return (List<String>)obj;
			}catch(Throwable ex){
				ex.printStackTrace();
			}
			return value;
		}
		
		@Override
		public void set(Intent intent, List<String> value){
			ArrayList<String> list;
			if(value==null) list = null;
			else if(value instanceof ArrayList) list = (ArrayList<String>)value;
			else list = new ArrayList<>(value);
			intent.putStringArrayListExtra(name, list);
		}
	}
	
	public static class ObjList<T extends Serializable> extends EasyKey<List<T>>
	{
		public ObjList(String name){
			super(name);
		}
		
		@Override
		public List<T> get(Intent intent, List<T> value){
			try{
				Object obj = getExtra(intent);
				if(obj instanceof List) return (List<T>)obj;
			}catch(Throwable ex){
				ex.printStackTrace();
			}
			return value;
		}
		
		@Override
		public void set(Intent intent, List<T> value){
			intent.putExtra(name, (Serializable)value);
		}
	}
}
