package io.exemplary.aws;

import java.lang.reflect.Field;
import java.util.Vector;

class SqliteLibrary {

    private static String LOADED_LIBRARIES_NAME = "loadedLibraryNames";
    private static String SQLITE_4_JAVA = "sqlite4java";

    /**
     * Hack to unload the sqlite native library when the server
     * has been started from two different class loaders.
     */
    static void unload() {
        try {
            Field field = ClassLoader.class.getDeclaredField(LOADED_LIBRARIES_NAME);
            field.setAccessible(true);
            Vector<String> libraries = (Vector<String>) field.get(null);
            String sqlLite = null;
            for (String library : libraries) {
                if (library.contains(SQLITE_4_JAVA)) {
                    sqlLite = library;
                }
            }
           if (sqlLite != null) libraries.remove(sqlLite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
