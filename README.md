## 用Java实现的小型数据库

### 目前效果

前提：

1. 固定表结构的存储
   ![image-20200416163829283](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416163829283.png)
2. 目前只能进行insert和select操作
3. 顺序存储
4. 数据页每页大小4kb

目前效果图：

![image-20200416164623208](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416164623208.png)

exit后数据以字节码存储到user.db中

![image-20200416164700244](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416164700244.png)

第二次运行结果

![image-20200416164830644](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416164830644.png)

![image-20200416164935903](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416164935903.png)

批量插入数据使数据量达到两个page以上

![image-20200416165100651](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416165100651.png)

再次exit重新启动，稳定

![image-20200416165225518](https://github.com/HengTian/HengTianDB/tree/master/images/image-20200416165225518.png)

### 数据结构

