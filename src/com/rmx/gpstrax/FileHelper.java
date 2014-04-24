
package com.rmx.gpstrax;

import java.io.File;
import java.io.PrintWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHelper {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getFile(Context context, String dirName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), dirName);
        if (!file.mkdirs()) {
            Log.e(C.LOG_TAG, "Directory not created");
        }
        return file;
    }

    public static PrintWriter getPrintWriter(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            return pw;
        } catch (Exception e) {
            Log.e(C.LOG_TAG, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void appendToFile(PrintWriter pw, String str) {
        try {
            pw.append(str);
        } catch (Exception e) {
            Log.e(C.LOG_TAG, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
