package com.iurac.recruit.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Book;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImageUtil {

    // 如果图片名称不是默认的 default.png，则会在指定路径下查找同名的图片文件并删除之。
    public static void deleteImage(String imageName,String path){
        if(!imageName.equals("default.png")){
            File folder = new File("D:\\upload\\"+path);
            File[] files = folder.listFiles();
            if (Objects.nonNull(files)){
                for(File file : files){
                    if (file.getName().equals(imageName)) {
                        System.out.println("删除"+file.getName()+"图片");
                        file.delete();
                    }
                }
            }
        }
    }

    // 此方法用于生成一个新的唯一文件名，文件名由当前日期和一个 UUID 组成，再加上指定的文件扩展名。
    public static String getFileName(String extension){
        String today = DateUtil.today();
        String uuid = IdUtil.simpleUUID();
        return today+uuid+extension;
    }


    //此方法用于保存上传的图片文件到指定路径。
    //如果新文件名不是默认的 default.png，则会先删除同名的图片文件，然后将上传的文件保存到指定路径。
    public static void saveImage(MultipartFile file, String newFileName, String path) throws IOException {
        if(!newFileName.equals("default.png")) {
            deleteImage(newFileName, path);
            File target = new File("D:\\upload\\" + path, newFileName);
            file.transferTo(target);
        }
    }
}
