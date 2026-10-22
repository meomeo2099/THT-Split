package com.tht.split;
import android.content.*;
import android.graphics.Point;
import android.view.WindowManager;
import com.tht.split.connection.Backend;
import com.tht.split.core.LayoutMath;
import org.json.*;

/** Called only on the service's serial worker thread. */
public final class SplitEngine {
    private final Context context;private final Store store,state;public JSONObject lastResult=new JSONObject();
    public SplitEngine(Context c,Store session){context=c;store=new Store(c);state=session;}
    public LayoutMath geometry(boolean toolbar){Point size=new Point();((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);
        return new LayoutMath(size.x,size.y,state.i("top_px",72),state.i("bottom_px",112),toolbar&&supportsTaskResize()?toolbarPx():0,8,state.i("ratio",50));}
    public int toolbarPx(){return Math.round((store.s("button_size","small").equals("large")?60:48)*context.getResources().getDisplayMetrics().density);}
    private String component(String p)throws Exception{Intent i=context.getPackageManager().getLaunchIntentForPackage(p);if(i==null||i.getComponent()==null)throw new IllegalArgumentException("Ứng dụng chưa cài hoặc không mở được: "+p);return i.getComponent().flattenToString();}
    private JSONObject request(String op,boolean toolbar)throws Exception{String l=state.s("left",""),r=state.s("right","");if(l.isEmpty()||r.isEmpty()||l.equals(r))throw new IllegalArgumentException("Hãy chọn hai ứng dụng khác nhau");
        LayoutMath g=geometry(toolbar);return new JSONObject().put("op",op).put("mode",state.s("mode","auto")).put("left",l).put("right",r).put("leftComponent",component(l)).put("rightComponent",component(r)).put("leftBounds",new JSONArray(g.left.array())).put("rightBounds",new JSONArray(g.right.array()));}
    private void call(JSONObject q)throws Exception {lastResult=new Backend(context,state.s("transport","adb")).call(q);if(lastResult.has("actualLeftBounds")||lastResult.has("actualFullscreenBounds"))store.set("last_layout",lastResult.toString());}
    public boolean supportsTaskResize(){return !state.s("transport","adb").equals("off");}
    public void status()throws Exception{call(new JSONObject().put("op","PING"));}
    public void layout(boolean toolbar)throws Exception{call(request("LAYOUT",toolbar));}
    public void resize(boolean toolbar)throws Exception{call(request("RESIZE",toolbar));}
    private JSONObject fullscreenRequest(String op,String side)throws Exception{Point size=new Point();((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(size);return request(op,false).put("side",side).put("fullscreenBounds",new JSONArray(new int[]{0,0,size.x,size.y}));}
    public void fullscreen(String side)throws Exception{call(fullscreenRequest("FULLSCREEN",side));}
    public void stop()throws Exception{call(fullscreenRequest("STOP","left"));}
    public void home()throws Exception{call(new JSONObject().put("op","HOME"));}
    /** "Gỡ kẹt": leave split, go home, redraw the status bar (see ShellServer RECOVER). */
    public void recover()throws Exception{call(new JSONObject().put("op","RECOVER"));}
}
