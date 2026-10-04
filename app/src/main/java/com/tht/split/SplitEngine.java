package com.tht.split;
import android.content.*;
import android.graphics.Point;
import android.view.WindowManager;
import com.tht.split.bridge.ShellClient;
import com.tht.split.core.LayoutMath;
import org.json.*;

/** Called only on the service's serial worker thread. */
public final class SplitEngine {
    private final Context context;private final Store store,state;private final ShellClient shell;
    public SplitEngine(Context c,Store session){context=c;store=new Store(c);state=session;shell=new ShellClient(store);}
    public LayoutMath geometry(boolean toolbar){Point size=new Point();((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);
        return new LayoutMath(size.x,size.y,state.i("top_px",72),state.i("bottom_px",112),toolbar?toolbarPx():0,8,state.i("ratio",50));}
    public int toolbarPx(){return Math.round((store.s("button_size","small").equals("large")?60:48)*context.getResources().getDisplayMetrics().density);}
    private String component(String p)throws Exception{Intent i=context.getPackageManager().getLaunchIntentForPackage(p);if(i==null||i.getComponent()==null)throw new IllegalArgumentException("Ứng dụng chưa cài hoặc không mở được: "+p);return i.getComponent().flattenToString();}
    private JSONObject request(String op,boolean toolbar)throws Exception{String l=state.s("left",""),r=state.s("right","");if(l.isEmpty()||r.isEmpty()||l.equals(r))throw new IllegalArgumentException("Hãy chọn hai ứng dụng khác nhau");
        LayoutMath g=geometry(toolbar);return new JSONObject().put("op",op).put("mode",state.s("mode","native")).put("left",l).put("right",r).put("leftBounds",new JSONArray(g.left.array())).put("rightBounds",new JSONArray(g.right.array()));}
    public void layout(boolean toolbar)throws Exception{JSONObject q=request("LAYOUT",toolbar);q.put("leftComponent",component(q.getString("left"))).put("rightComponent",component(q.getString("right")));shell.call(q);}
    public void resize(boolean toolbar)throws Exception{shell.call(request("RESIZE",toolbar));}
    public void fullscreen(String side)throws Exception{shell.call(request("FULLSCREEN",false).put("side",side));}
    public void stop()throws Exception{shell.call(request("STOP",false).put("side","left"));}
    public void home()throws Exception{shell.call(new JSONObject().put("op","HOME"));}
}
