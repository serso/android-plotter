package org.solovyev.android.io;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileSaver implements Runnable {

    @NonNull
    private final File file;
    @NonNull
    private final CharSequence data;

    private FileSaver(@NonNull File file, @NonNull CharSequence data) {
        this.file = file;
        this.data = data;
    }

    public static void save(@NonNull File file, @NonNull CharSequence data) {
        final FileSaver fileSaver = new FileSaver(file, data);
        fileSaver.save();
    }

    public void save() {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file));
            out.write(data.toString());
        } catch (IOException e) {
            Log.e("FileSaver", e.getMessage(), e);
        } finally {
            BaseFileLoader.close(out);
        }
    }

    @Override
    public void run() {
        save();
    }
}
