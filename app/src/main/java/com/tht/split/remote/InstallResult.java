package com.tht.split.remote;
import android.content.*;
import android.content.pm.PackageInstaller;
import android.widget.Toast;
import com.tht.split.Store;
public final class InstallResult extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){int result=i.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
        if(result==PackageInstaller.STATUS_PENDING_USER_ACTION){Intent confirmation=i.getParcelableExtra(Intent.EXTRA_INTENT);if(confirmation!=null)try{c.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(RuntimeException e){Toast.makeText(c,"Mở lại THT Split để tiếp tục cài cập nhật",Toast.LENGTH_LONG).show();}}
        else if(result!=PackageInstaller.STATUS_SUCCESS){new Store(c).set("last_error","Cài cập nhật chưa thành công ("+result+")");Toast.makeText(c,"Chưa cài được cập nhật hoặc đã hủy",Toast.LENGTH_LONG).show();}
    }
}
