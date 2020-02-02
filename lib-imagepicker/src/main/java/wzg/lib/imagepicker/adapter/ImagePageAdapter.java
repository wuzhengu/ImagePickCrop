package wzg.lib.imagepicker.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.LinkedList;
import java.util.List;

/**
 Created by wuzhengu on 2020/02/02 <br/>
 */
public class ImagePageAdapter<PageItem, PageView extends View> extends PagerAdapter
{
	private List<PageItem> mList;
	LinkedList<View> mCache=new LinkedList<>();
	
	public ImagePageAdapter(List<PageItem> list){
		this.mList=list;
	}
	
	@Override
	public int getCount(){
		return mList==null? 0: mList.size();
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object){
		return view==object;
	}
	
	@NonNull
	@Override
	public Object instantiateItem(ViewGroup pa, int p){
		PageView view=null;
		try{
			if(mCache.size()>0) view=(PageView)mCache.remove();
		}catch(Throwable ex){
			ex.printStackTrace();
		}
		view=bindView(pa, p, view);
		pa.addView(view);
		return view;
	}
	
	@Override
	public void destroyItem(ViewGroup pa, int position, Object obj){
		View page=(View)obj;
		pa.removeView(page);
		mCache.add(page);
	}
	
	public PageView bindView(ViewGroup pa, int p, PageView view){
		return view;
	}
	
	public PageItem getItem(int p){
		return mList.get(p);
	}
	
}
