package com.tht.split.bridge;

import java.io.*;
import java.util.concurrent.*;

/** File descriptors from su-owned pipes can be rejected by system_server on user firmware. */
public final class CommandRunner {
    private static final int OUTPUT_LIMIT=24000;
    private static final long FILE_LIMIT=262144;
    private static final ExecutorService IO=Executors.newCachedThreadPool(r->{Thread t=new Thread(r,"tht-command-output");t.setDaemon(true);return t;});
    private CommandRunner() {}
    public static String run(String[] command,File fileDirectory,long timeoutMillis)throws Exception {
        if(command.length==0||timeoutMillis<=0)throw new IllegalArgumentException("Command/timeout");
        File capture=null;Process process=null;Future<String> output=null;
        try {
            ProcessBuilder builder=new ProcessBuilder(command).redirectErrorStream(true);
            if(fileDirectory!=null){
                capture=File.createTempFile("tht-command-",".out",fileDirectory);
                if(!capture.setReadable(false,false)||!capture.setWritable(false,false)||
                   !capture.setReadable(true,true)||!capture.setWritable(true,true))
                    throw new IOException("Không bảo vệ được tệp đầu ra Bridge");
                // /dev/null and an opened shell_data_file avoid passing su:fifo_file to Binder.
                builder.redirectInput(new File("/dev/null")).redirectOutput(capture);
            }
            process=builder.start();
            if(capture==null){
                process.getOutputStream().close();
                final Process running=process;
                output=IO.submit(()->read(running.getInputStream()));
            }
            long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            while(!process.waitFor(50,TimeUnit.MILLISECONDS)){
                if(capture!=null&&capture.length()>FILE_LIMIT)throw new IOException("Đầu ra lệnh vượt giới hạn");
                if(System.nanoTime()>=deadline)throw new IOException("Lệnh quá thời gian");
            }
            if(capture!=null&&capture.length()>FILE_LIMIT)throw new IOException("Đầu ra lệnh vượt giới hạn");
            String result=capture!=null?read(new FileInputStream(capture)):output.get(2,TimeUnit.SECONDS);
            if(process.exitValue()!=0||result.contains("cmd: Failure calling service"))
                throw new IOException("Lệnh "+command[0]+" thất bại: "+result.trim());
            return result;
        }finally{
            if(process!=null){if(process.isAlive())process.destroyForcibly();
                try{process.getInputStream().close();}catch(IOException ignored){}
                try{process.getErrorStream().close();}catch(IOException ignored){}
                try{process.getOutputStream().close();}catch(IOException ignored){}
            }
            if(output!=null)output.cancel(true);
            if(capture!=null&&!capture.delete())capture.deleteOnExit();
        }
    }
    private static String read(InputStream stream)throws IOException {
        try(InputStream in=stream;ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[2048];int n;
            while((n=in.read(buffer))!=-1){if(bytes.size()<OUTPUT_LIMIT)bytes.write(buffer,0,Math.min(n,OUTPUT_LIMIT-bytes.size()));}
            return bytes.toString("UTF-8");
        }
    }
}
