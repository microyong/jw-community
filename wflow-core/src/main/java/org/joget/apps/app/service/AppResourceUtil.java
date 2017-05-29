package org.joget.apps.app.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.springframework.web.multipart.MultipartFile;

public class AppResourceUtil {
    /**
     * Gets directory path to temporary files folder
     * @return 
     */
    public static String getBaseDirectory() {
        return SetupManager.getBaseDirectory() + File.separator + "app_resources" + File.separator;
    }
    
    /**
     * Stores files post to the HTTP request to app resources folder
     * @param appId
     * @param appVersion
     * @param file
     * @return the relative path of the stored file, NULL if failure.
     */
    public static String storeFile(String appId, String appVersion, MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            //delete existing before store
            deleteFile(appId, appVersion, file.getOriginalFilename());
            
            FileOutputStream out = null;
            String path =  appId + File.separator + appVersion + File.separator;
            String filename = path;
            try {
                filename += URLDecoder.decode(file.getOriginalFilename(), "UTF-8");
                File uploadFile = new File(getBaseDirectory(), filename);
                if (!uploadFile.isDirectory()) {
                    //create directories if not exist
                    new File(getBaseDirectory(), path).mkdirs();

                    // write file
                    out = new FileOutputStream(uploadFile);
                    out.write(file.getBytes());
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
            }
            return filename;
        }
        return null;
    }
    
    /**
     * Gets the file from app resources folder 
     * @param appId
     * @param appVersion
     * @param filename
     * @return 
     */
    public static File getFile(String appId, String appVersion, String filename) {
        String path = appId + File.separator + appVersion + File.separator + filename;
        if (path != null) {
            try {
                File file = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
                if (file.exists() && !file.isDirectory()) {
                    return file;
                }
            } catch (Exception e) {}
        }
        return null;
    }
    
    /**
     * Deletes the file from app resource folder 
     * @param appId
     * @param appVersion
     * @param filename
     */
    public static void deleteFile(String appId, String appVersion, String filename) {
        File file = getFile(appId, appVersion, filename);
        
        if (file != null && file.exists()) {
            file.delete();
        }
    }
    
    public static void addResourcesToZip(String appId, String appVersion, ZipOutputStream zip) {
        try {
            String path = appId + File.separator + appVersion;
            File folder = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
            
            if (folder.exists()) {
                File[] files = folder.listFiles();
                for (File file : files)
                {
                    if (file.canRead())
                    {
                        FileInputStream fis = null;
                        try {
                            zip.putNextEntry(new ZipEntry("resources/" + file.getName()));
                            fis = new FileInputStream(file);
                            byte[] buffer = new byte[4092];
                            int byteCount = 0;
                            while ((byteCount = fis.read(buffer)) != -1)
                            {
                                zip.write(buffer, 0, byteCount);
                            }
                            zip.closeEntry();
                        } finally {
                            if (fis != null) {
                                fis.close();
                            }
                        }  
                    }
                }
            }
        
        } catch (Exception e) {}
    }
    
    public static void importFromZip(String appId, String appVersion, byte[] zip) {
        ZipInputStream in = null;
        String path = appId + File.separator + appVersion;
        try {
            in = new ZipInputStream(new ByteArrayInputStream(zip));

            ZipEntry entry = null;

            while ((entry = in.getNextEntry()) != null) {
                if (entry.getName().startsWith("resources/") && !entry.isDirectory()) {
                    FileOutputStream out = null;
                    String filename = entry.getName().replaceFirst("resources/", "");
                    try {
                        File folder = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
        
                        File file = new File(getBaseDirectory(), URLDecoder.decode(path + File.separator + filename, "UTF-8"));
                        
                        out = new FileOutputStream(file);
                        int length;
                        byte[] temp = new byte[1024];
                        while ((length = in.read(temp, 0, 1024)) != -1) {
                            out.write(temp, 0, length);
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException iex) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    public static void copyAppResources(String appIdFrom, String appVersionFrom, String appIdTo, String appVersionTo) {
        try {
            String pathFrom = appIdFrom + File.separator + appVersionFrom;
            String pathTo = appIdTo + File.separator + appVersionTo;
            
            File folder = new File(getBaseDirectory(), URLDecoder.decode(pathFrom, "UTF-8"));
            
            if (folder.exists()) {
                FileUtils.copyDirectory(folder, new File(getBaseDirectory(), URLDecoder.decode(pathTo, "UTF-8")), null);
            }
        } catch (Exception e) {}
    }
    
    public static void deleteAppResources(String appId, String appVersion) {
        try {
            String path = appId + File.separator + appVersion;
            File folder = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
            
            if (folder.exists()) {
                FileUtils.deleteDirectory(folder);
            }
        } catch (Exception e) {}
    }
    
    public static void deleteAppResourcesForAllVersion(String appId) {
        try {
            String path = appId;
            File folder = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
            
            if (folder.exists()) {
                FileUtils.deleteDirectory(folder);
            }
        } catch (Exception e) {}
    }
}
