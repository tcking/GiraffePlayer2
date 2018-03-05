package tcking.github.com.giraffeplayer2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by TangChao on 2018/2/11.
 */

public class LazyLoadManager extends IntentService {
    private static final String TAG = "LazyLoadManager";
    private static String KEY_FINGERPRINT = "fingerprint";
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_OK = 2;
    private static final int STATUS_ERROR = -1;
    private static int loadStatus = STATUS_IDLE;
    private static String lastFingprint;
    private static Message lastMessage;
    private static final String soVersion = "0.8.8";
    private static String abi;
    private int progress = -1;

    public static void setSoFetcher(SoFetcher soFetcher) {
        if (soFetcher != null) {
            LazyLoadManager.soFetcher = soFetcher;
        }
    }

    private static SoFetcher soFetcher=new SoFetcher(){

        @Override
        public String getURL(String abi, String soVersion) {
            return String.format("https://raw.githubusercontent.com/tcking/GiraffePlayerLazyLoadFiles/master/%s/%s/so.zip", soVersion, abi);
        }
    };

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LazyLoadManager() {
        super("GiraffePlayerLazyLoader");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String fingerprint = intent.getStringExtra(KEY_FINGERPRINT);
        log(fingerprint, "loadStatus:" + loadStatus);
        if (loadStatus > 0) {
            sendLastMessage();
            return;
        }
        loadStatus = STATUS_LOADING;
        //https://raw.githubusercontent.com/tcking/GiraffePlayerLazyLoadFiles/master/0.1.17/armeabi/so.zip
        Context context = getApplicationContext();
        try {
            File playerDir = getPlayerRootDir(context);
            File soDir = getPlayerSoDir(context);
            log(fingerprint,"soDir:"+soDir);
            if (new File(soDir, "libijkffmpeg.so").exists()) {
                log(fingerprint,"so files downloaded,try install");
                installSo(soDir);
                return;
            }

            String abi = getCurrentAbi(context);
            String url = soFetcher.getURL(abi, soVersion);
            log(fingerprint, "download so from:" + url);
            progress = -1;
            HttpRequest request = HttpRequest.get(url);
            int code = request.code();
            log(fingerprint, "server:" + code);
            if (code == HttpURLConnection.HTTP_OK) {
                File tmp = new File(new File(playerDir, "tmp"), "so.zip");
                tmp.delete();
                tmp.getParentFile().mkdirs();
                tmp.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(tmp);
                BufferedInputStream buffer = request.buffer();
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                int contentLength = request.contentLength();
                while ((count = buffer.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    if (contentLength > 0) {
                        publishProgress((int) (total * 100 / contentLength));
                    }
                    fileOutputStream.write(data, 0, count);
                }
                fileOutputStream.close();

                unZip(tmp, soDir);
                tmp.delete();
                installSo(soDir);
            } else {
                error("server response " +code);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }


    }

    private void error(String message) {
        loadStatus = STATUS_ERROR;
        if (lastFingprint != null) {
            GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(lastFingprint);
            if (player != null) {
                player.lazyLoadError(message);
            }
        }

    }

    private void installSo(File soDir) throws Exception {
        SoInstaller.installNativeLibraryPath(GiraffePlayer.class.getClassLoader(), soDir);
        log(lastFingprint,"so installed");
        loadStatus = STATUS_OK;
        sendLastMessage();
    }

    private void publishProgress(int _progress) {
        if (progress == _progress) {
            return;
        }
        progress = _progress;
        if (lastFingprint != null) {

            GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(lastFingprint);
            if (player != null) {
                player.lazyLoadProgress(progress);
            }
        }
    }

    /**
     * replay last message to the player
     */
    private void sendLastMessage() {
        GiraffePlayer player = PlayerManager.getInstance().getPlayerByFingerprint(lastFingprint);
        if (player != null) {
            player.doMessage(lastMessage);
        }
        lastMessage = null;
        lastFingprint = null;
    }

    public static void Load(Context context, String fingerprint, Message message) {

        lastFingprint = fingerprint;
        lastMessage = message;

        Intent service = new Intent(context, LazyLoadManager.class);
        service.putExtra(KEY_FINGERPRINT, fingerprint);
        context.startService(service);
    }

    private void log(String fingerprint, String msg) {
        if (GiraffePlayer.debug) {
            Log.d(TAG, String.format("[fingerprint:%s] %s", fingerprint, msg));
        }
    }


    public static String getCurrentAbi(Context context) {
        if (abi != null) {
            return abi;
        }
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String apkPath = applicationInfo.sourceDir;
        ZipFile apk;
        String[] abis;
        if (Build.VERSION.SDK_INT >= 21) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        try {
            apk = new ZipFile(apkPath);
            for (String _abi : abis) {
                Enumeration<? extends ZipEntry> entryEnumeration = apk.entries();
                while (entryEnumeration.hasMoreElements()) {
                    ZipEntry entry = entryEnumeration.nextElement();
                    if (entry == null) {
                        continue;
                    }
                    String entryName = entry.getName();
                    if (entryName.startsWith("lib/") && entryName.endsWith("so")) {
                        int start = entryName.indexOf("/");
                        int end = entryName.lastIndexOf("/");
                        String temp = entryName.substring(start + 1, end);
                        if (_abi.equalsIgnoreCase(temp)) {
                            abi = _abi;
                            return abi;
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        abi = abis[0];
        return abi;
    }


    public static void unZip(File input, File output) throws Exception {
        output.mkdirs();
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(input));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(output.getAbsolutePath() + File.separator + szName);
                folder.mkdirs();
            } else {

                File file = new File(output.getAbsolutePath() + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static File getPlayerRootDir(Context context) {
        return context.getDir("giraffePlayer2", Context.MODE_PRIVATE);

    }

    public static File getPlayerSoDir(Context context) {
        return new File(getPlayerRootDir(context).getAbsolutePath()+File.separator+soVersion+File.separator+"so");

    }

    public interface SoFetcher{
        String getURL(String abi,String soVersion);
    }
}
