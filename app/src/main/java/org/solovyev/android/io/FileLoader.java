package org.solovyev.android.io;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileLoader extends BaseFileLoader {

    @NonNull
    private final File file;

    private FileLoader(@NonNull Context context, @NonNull File file) {
        super(context);
        this.file = file;
    }

    @NonNull
    public static FileLoader create(@NonNull Context context, @NonNull File file) {
        return new FileLoader(context, file);
    }

    @NonNull
    public static FileLoader create(@NonNull Context context, @NonNull String dir, @NonNull String name) {
        return new FileLoader(context, getFile(context, dir, name));
    }

    @NonNull
    public static FileLoader create(@NonNull Context context, @NonNull String name) {
        return new FileLoader(context, getFile(context, null, name));
    }

    @NonNull
    private static File getFile(@NonNull Context context, @Nullable String dir, @NonNull String name) {
        return new File(getDir(context, dir), name);
    }

    @NonNull
    private static File getDir(@NonNull Context context, @Nullable String dir) {
        if (dir != null) {
            return context.getDir(dir, Context.MODE_PRIVATE);
        }
        return context.getFilesDir();
    }

    @Nullable
    @Override
    protected InputStream getInputStream() throws IOException {
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }
}
