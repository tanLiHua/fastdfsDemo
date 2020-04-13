import com.fastdfs.SpringbootApplication;
import com.fastdfs.utils.FastdfsClient;
import com.github.tobato.fastdfs.exception.FdfsConnectException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

/**
 * @author tlh
 * @description
 * @date 2020/4/10
 */
@SpringBootTest(classes = {SpringbootApplication.class})
@RunWith(SpringRunner.class)
public class FastdfsTest {


    @Autowired
    private FastdfsClient fastdfsClient;

    @Test
    public void uploadFile() {
        try {
            String fileUrl = fastdfsClient.uploadFileAndCrtThumbImage(new File("C:\\Users\\Administrator\\Desktop\\mailFile\\3.jpg"));
            System.out.println("上传文件成功,文件存储地址：" +fileUrl);
            //fastdfs没有实现tracker-list的高可用。即使配置了list,当有一个宕机时还是无法启动获取连接
            // 下面进行手动捕获异常再次获取连接。第一次连接不成功会设置连接不可用并抛出异常。我们只需捕获异常再次获取连接就会换一个地址
        } catch (FdfsConnectException e) {
            try {
                String s = fastdfsClient.uploadFileAndCrtThumbImage(new File("C:\\Users\\Administrator\\Desktop\\mailFile\\3.jpg"));
                System.out.println(s);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("上传文件失败：" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //下载后存储在本地的文件路径
    @Test
    public void download() throws Exception {
        String fileUrl="group1/M00/00/00/BbRMcl6P4AeAHlEJAABlq8b6-yc786.jpg";
        String localFileUrl = fastdfsClient.download(fileUrl);
        System.out.println("下载文件本地保存路径：" + localFileUrl);
    }
}
