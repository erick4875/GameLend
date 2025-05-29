package com.example.gamelend.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Crea un archivo temporal a partir de una URI de contenido.
     * Esto es útil para obtener un objeto File que Retrofit puede usar para MultipartBody.Part.
     *
     * @param context   Contexto de la aplicación.
     * @param contentUri URI de la imagen seleccionada (ej. de Photo Picker o selector de archivos).
     * @return Un objeto File que apunta a una copia temporal de la imagen, o null si falla.
     */
    public static File getFileFromUri(final Context context, final Uri contentUri) {
        if (contentUri == null) {
            Log.e(TAG, "Content URI es null, no se puede crear el archivo.");
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            Log.e(TAG, "ContentResolver es null.");
            return null;
        }

        // Obtener el nombre del archivo y la extensión de la URI
        String fileName = getFileName(contentResolver, contentUri);
        String extension = getFileExtension(contentResolver, contentUri, fileName);

        // Crear un archivo temporal en el directorio de caché de la aplicación
        // Usar un nombre único para evitar colisiones si se seleccionan múltiples archivos
        String tempFileName = "temp_image_" + UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
        File tempFile = new File(context.getCacheDir(), tempFileName);

        try (InputStream inputStream = contentResolver.openInputStream(contentUri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir InputStream para la URI: " + contentUri);
                return null;
            }

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != EOF) {
                outputStream.write(buffer, 0, bytesRead);
            }
            Log.d(TAG, "Archivo copiado exitosamente a: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (IOException e) {
            Log.e(TAG, "Error al copiar URI a archivo temporal: " + contentUri, e);
            // Eliminar el archivo temporal si la copia falla
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        } catch (SecurityException e) {
            Log.e(TAG, "Excepción de seguridad al acceder a la URI: " + contentUri, e);
            return null;
        }
    }

    /**
     * Intenta obtener el nombre de archivo de una URI de contenido.
     */
    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al obtener nombre de archivo de la URI: " + uri, e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "unknown_file";
    }

    /**
     * Intenta obtener la extensión de archivo de una URI de contenido o de un nombre de archivo.
     */
    private static String getFileExtension(ContentResolver contentResolver, Uri uri, String fileName) {
        String extension = null;
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(contentResolver.getType(uri));
        } else if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return extension != null ? extension.toLowerCase() : "";
    }
}

