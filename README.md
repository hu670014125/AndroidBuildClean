# AndroidBuildClean
AndroidBuildClean 是一个用于清理Android项目中构建build的缓存文件，可以一键清理。
<br>在平时的Android项目开发中工作时间就了就会在Android project 中的build或者app/build 产生大量的临时文件
这些临时文件是没有用的，因为android studio每次编译都会产生新的临时文件，这样时间久了我们的硬盘空间就会不足，特别是
使用固态硬盘的同学。文件太多手动删除太过于麻烦，所以写了这个工具可以自动清理。
<br>
效果图如下：
<br>
![image](https://raw.githubusercontent.com/hu670014125/AndroidBuildClean/master/preview/image.png)
<br><br>可以看到随便一个Android project项目的临时文件都上百兆<br><br>
![image](https://raw.githubusercontent.com/hu670014125/AndroidBuildClean/master/preview/image2.png)
<br><br>可以选择一键删除文件<br><br>
![image](https://raw.githubusercontent.com/hu670014125/AndroidBuildClean/master/preview/image3.png)
<br><br>再次查看之前的路径已没有缓存文件了<br><br>
![image](https://raw.githubusercontent.com/hu670014125/AndroidBuildClean/master/preview/image4.png)