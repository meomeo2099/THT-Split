package com.tht.split.connection;

import android.app.ActivityOptions;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.provider.Settings;
import org.json.*;
import java.io.IOException;
import java.util.concurrent.*;


/** Off asks the system to launch windows; it never claims observed task layout. */
public final class DirectWindows {
    private final Context context;
    public DirectWindows(Context c){context=c;}
    public JSONObject call(JSONObject q)throws Exception {
        String op=q.getString("op");
        if(op.equals("PING"))return new JSONObject().put("uid",android.os.Process.myUid()).put("sdk",Build.VERSION.SDK_INT).put("verified",false).put("note","Off: mở trực tiếp; không xác minh cửa sổ app khác");
        if(op.equals("HOME")){onMain(()->context.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));return requested();}
        if(op.startsWith("FREEFORM_"))throw new IOException("Off không có quyền đổi thiết lập Android. Bật freeform bằng ADB một lần trước.");
        if(op.equals("RESIZE")||op.equals("FULLSCREEN"))throw new IOException("Off không điều khiển được task đang chạy. Chọn ADB nội bộ để dùng chức năng này.");
        if(op.equals("STOP"))return requested().put("note","Đã dừng điều khiển Off; cửa sổ app do Android quản lý, không gửi yêu cầu fullscreen");
        if(op.equals("LAYOUT")){
            if(Settings.Global.getInt(context.getContentResolver(),"enable_freeform_support",0)!=1&&!context.getPackageManager().hasSystemFeature("android.software.freeform_window_management"))throw new IOException("Cần bật hỗ trợ freeform trước khi dùng Off");
            String left=q.getString("left"),right=q.getString("right");if(left.equals(right))throw new IOException("Chọn hai ứng dụng khác nhau");
            Rect l=rect(q.getJSONArray("leftBounds")),r=rect(q.getJSONArray("rightBounds"));
            onMain(()->launch(left,5,l));
            onMain(()->launch(right,5,r));return requested();
        }
        throw new IOException("Off không hỗ trợ thao tác này");
    }
    private JSONObject requested()throws JSONException{return new JSONObject().put("verified",false).put("status","requested").put("note","Đã gửi yêu cầu mở cửa sổ; cần quan sát màn hình để xác nhận");}
    private Rect rect(JSONArray a)throws Exception{if(a.length()!=4)throw new IOException("Vùng cửa sổ không hợp lệ");Rect r=new Rect(a.getInt(0),a.getInt(1),a.getInt(2),a.getInt(3));if(r.left<0||r.top<0||r.width()<=0||r.height()<=0||r.right>16384||r.bottom>16384)throw new IOException("Vùng cửa sổ không hợp lệ");return r;}
    private void launch(String pkg,int mode,Rect bounds)throws Exception{
        Intent intent=context.getPackageManager().getLaunchIntentForPackage(pkg);if(intent==null||pkg.equals(context.getPackageName()))throw new IOException("Ứng dụng chưa cài hoặc không mở được: "+pkg);
        // Adjacent launch can select the currently focused stack and ignore freeform options.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // Reuse existing app task rather than create duplicates on each swap/restore.
        ActivityOptions options=ActivityOptions.makeBasic();
        HiddenAccess.allow("Landroid/app/ActivityOptions;");
        ActivityOptions.class.getMethod("setLaunchWindowingMode",int.class).invoke(options,mode);
        if(bounds!=null)options.setLaunchBounds(bounds);
        context.startActivity(intent,options.toBundle());
    }
    private interface MainTask{void run()throws Exception;}
    private void onMain(MainTask task)throws Exception {
        FutureTask<Void> f=new FutureTask<>(()->{task.run();return null;});new Handler(Looper.getMainLooper()).post(f);
        try{f.get(8,TimeUnit.SECONDS);}catch(ExecutionException e){Throwable cause=e.getCause();if(cause instanceof Exception)throw (Exception)cause;throw new IOException("Không mở được cửa sổ",cause);}catch(TimeoutException e){f.cancel(false);throw new IOException("Quá thời gian mở cửa sổ",e);}
    }
}
