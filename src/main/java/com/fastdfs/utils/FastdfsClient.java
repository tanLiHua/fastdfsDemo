package com.fastdfs.utils;

import com.github.tobato.fastdfs.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author tlh
 * @description
 * @date 2020/4/9
 */
@Component
public class FastdfsClient {

        @Autowired
        private FastFileStorageClient storageClient;

        @Autowired
        private FdfsWebServer fdfsWebServer;

        @Value("${com.fastdfs.demo.fileStoreDirectory}")
        private String fileStoreDirectory;

        /**
         * 上传文件
         * @param file 文件对象
         * @return 文件访问地址
         * @throws IOException
         */
        public String uploadFile(MultipartFile file) throws IOException {
            StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
            return getResAccessUrl(storePath);
        }

        /**
         * 上传图片并且生成缩略图
         * <pre>
         * 支持的图片格式包括"JPG", "JPEG", "PNG", "GIF", "BMP", "WBMP"
         * 缩略图为上传文件名+缩略图后缀 _150x150,如 xxx.jpg,缩略图为 xxx_150x150.jpg
         *
         * 实际样例如下
         *  原图   http://localhost:8098/M00/00/17/rBEAAl33pQaAWNQNAAHYvQQn-YE374.jpg
         *  缩略图 http://localhost:8098/M00/00/17/rBEAAl33pQaAWNQNAAHYvQQn-YE374_150x150.jpg
         *
         * </pre>
         * @return  图片地址
         */
        public String uploadFileAndCrtThumbImage(File file) throws IOException {
            FileInputStream inputStream = new FileInputStream (file);
            StorePath storePath = storageClient.uploadImageAndCrtThumbImage(inputStream, file.length(), FilenameUtils.getExtension(file.getName()), null);
            return getResAccessUrl(storePath);
        }

        /**
         * 上传文件
         * @param file 文件对象
         * @return 文件访问地址
         * @throws IOException
         */
        public String uploadFile(File file) throws IOException {
            FileInputStream inputStream = new FileInputStream (file);
            StorePath storePath = storageClient.uploadFile(inputStream,file.length(), FilenameUtils.getExtension(file.getName()),null);
            return getResAccessUrl(storePath);
        }


        /**
         * 将一段字符串生成一个文件上传
         * @param content 文件内容
         * @param fileExtension
         * @return
         */
        public String uploadFile(String content, String fileExtension) {
            byte[] buff = content.getBytes(Charset.forName("UTF-8"));
            ByteArrayInputStream stream = new ByteArrayInputStream(buff);
            StorePath storePath = storageClient.uploadFile(stream,buff.length, fileExtension,null);
            return getResAccessUrl(storePath);
        }

        // 封装图片完整URL地址
        private String getResAccessUrl(StorePath storePath) {
            String fileUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
            return fileUrl;
        }


    /**
     * 下载文件
     *
     * @param fileUrl http://123.*.*.1/group/gggfg.jpg
     *            文件url
     * @return 下载后存储在本地的文件路径
     */
    public String download(String fileUrl) throws Exception {
        String newFileName = null;
        if (fileUrl != null && !fileUrl.isEmpty() ) {
            //http://localhost:22122/group1/M00/00/00/rBB8pF6QVUCAWoSuAABlq8b6-yc941.jpg
            String tmpPath = fileUrl.substring(fileUrl.indexOf("//")+2);
            String filePath = tmpPath.substring(tmpPath.indexOf("/")+1);
            String group = filePath.substring(0, filePath.indexOf("/"));
            String path = filePath.substring(filePath.indexOf("/") + 1);
            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            BufferedOutputStream bos = null;
            try {


                File newFile = null;
                File dir = new File(fileStoreDirectory);
                if (!dir.exists()) {// 判断文件目录是否存在
                    dir.mkdirs();
                }
                if(dir.isDirectory())
                {
                    newFileName = fileStoreDirectory+ fileName;
                    newFile = new File(newFileName);
                    byte[] bytes = storageClient.downloadFile(group, path, new DownloadByteArray());
                    bos = new BufferedOutputStream(new FileOutputStream(newFile, true));
                    bos.write(bytes);
                    bos.flush();
                }


            } catch (Exception e) {
                //logger.error("文件下载失败", e);
                throw new Exception("文件下载失败" + e.getMessage());
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        } else {
            throw new Exception("非法的文件路径格式：" + fileUrl);
        }

        return newFileName;
    }
        /**
         * 删除文件
         * @param fileUrl 文件访问地址
         * @return
         */
        public void deleteFile(String fileUrl) {
            if (StringUtils.isEmpty(fileUrl)) {
                return;
            }
            try {
                StorePath storePath = StorePath.praseFromUrl(fileUrl);
                storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
            } catch (FdfsUnsupportStorePathException e) {
                e.getMessage();
            }
        }

}
