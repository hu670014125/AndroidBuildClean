package com.huqs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
/**
 * 主窗口类
 *
 * @author huqs
 */
class MainFrame extends JFrame implements ActionListener {
    private JTextField mTextFieldDirectories = null;
    private TextArea mTextArea = null;
    private File mFile = null;
    private long mFileTotalLength = 0L;
    private JLabel mLabel = null;
    private LinkedHashMap<String, File> mCacheFile = new LinkedHashMap();

    MainFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int windowWidth = getDimension().width / 2;
        int windowHeight = getDimension().height / 2;
        int left = getDimension().width / 2 - windowWidth / 2;
        int top = getDimension().height / 2 - windowHeight / 2;
        this.setBounds(left, top, windowWidth, windowHeight);
        this.setTitle("Android build 清理工具");
        JPanel basePanel = new JPanel();
        basePanel.setBackground(Color.WHITE);
        basePanel.add(getTopPanel());
        mTextArea = new TextArea((int) (getDimension().height / 39.5), (int) (getDimension().width / 16.5));
        mTextArea.setBackground(Color.BLACK);
        mTextArea.setForeground(Color.green);
        mLabel = new JLabel();
        basePanel.add(mLabel);
        basePanel.add(mTextArea);
        this.add(basePanel);
        this.setVisible(true);

    }

    /**
     * 获取顶部的面板
     *
     * @return JPanel
     */
    private JPanel getTopPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("路径："));
        mTextFieldDirectories = new JTextField((int) (getDimension().width / 30.5));
        panel.add(mTextFieldDirectories);
        JButton button = new JButton("请选择");
        button.addActionListener(this);
        panel.add(button);
        return panel;
    }

    /**
     * 获取显示器的分辨率
     *
     * @return Dimension
     */
    private Dimension getDimension() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();
        jfc.setToolTipText("Android项目路径");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.showDialog(new JLabel(), "选择");
        mFile = jfc.getSelectedFile();
        //System.out.println(mFile.getAbsolutePath());
        if (mFile != null) {

            String[] split = mFile.getAbsolutePath().split("/");
            if (split.length >= 2) {
                if ((split[split.length - 1]).equals(split[split.length - 2])) {
                    String replaceFilePath = mFile.getAbsolutePath();
                    System.out.println(replaceFilePath);
                    replaceFilePath = replaceFilePath.substring(0, replaceFilePath.length() - split[split.length - 2].length());
                    mFile = new File(replaceFilePath);
                }
            }
            mTextFieldDirectories.setText(mFile.getAbsolutePath());
            mTextArea.setText("");
            mFileTotalLength = 0L;
            mTextArea.setText("正在搜索文件，请稍后\n");
            mCacheFile.clear();
            new Thread(() -> {
                findDirectories(mFile);
                boolean empty = mTextArea.getText().isEmpty();
                if (empty) {
                    mTextArea.setText("未找到build临时文件");
                } else {
                    mTextArea.append("搜索完成，搜索到文件总大小：" + formatSize(mFileTotalLength)+"\n");
                    if (mCacheFile.size()>0){
                        int code = JOptionPane.showConfirmDialog(MainFrame.this, "已经找到了"+mCacheFile.size()+"项，是否需要删除？", "提示",JOptionPane.YES_NO_OPTION);//返回的是按钮的index  i=0或者1
                        if (code==0){
                           mCacheFile.forEach((s, file) -> {
                               deleteDirectory(file.getAbsolutePath());
                               mTextArea.append(s+"\t已删除\n");
                           });
                            mTextArea.append("文件删除完成");
                        }
                    }
                }
            }).start();
        } else {
//            JOptionPane.showMessageDialog(this, "（路径找不到）", "警告", JOptionPane.ERROR_MESSAGE);
        }


    }

    /**
     * 查找文件夹
     *
     * @param file File
     */
    private void findDirectories(final File file) {
        boolean directory = file.isDirectory();
        boolean ideaProject = isIdeaProject(file);
        //判断当前的目录是否为idea项目
        if (directory && ideaProject) {
            mLabel.setText(file.getAbsolutePath());
            findBuildDirectories(file);
        } else {
            //遍历path下的文件和目录，放在File数组中
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File itemFile : listFiles) {
                    if (itemFile.isDirectory()) mLabel.setText(itemFile.getAbsolutePath());

                    directory = itemFile.isDirectory();
                    ideaProject = isIdeaProject(itemFile);
                    if (directory && ideaProject) {
                        // mLabel.setText(itemFile.getAbsolutePath());
                        findBuildDirectories(itemFile);
                    }
                    findDirectories(itemFile);
                }
            }
        }
    }

    private void findBuildDirectories(File file) {
        if (file.listFiles() == null) return;
        for (File itemFile : file.listFiles()) {
            if (itemFile.isDirectory() && itemFile.getName().equals("build")) {
                if (!mCacheFile.containsKey(itemFile.getAbsolutePath())){

                    long totalSizeOfFilesInDir = getTotalSizeOfFilesInDir(itemFile);
                    mFileTotalLength += totalSizeOfFilesInDir;
                    mCacheFile.put(itemFile.getAbsolutePath(), itemFile);
                    // deleteFolder(itemFile.getAbsolutePath());
                    mTextArea.append(itemFile.getAbsolutePath().trim() + "\t" + formatSize(totalSizeOfFilesInDir) + "\n");
                }
            } else {
                // mLabel.setText(itemFile.getAbsolutePath());
                findBuildDirectories(itemFile);
            }
        }
    }

    /**
     * 根据路径删除指定的目录，无论存在与否
     *
     * @param sPath 要删除的目录path
     * @return 删除成功返回 true，否则返回 false。
     */
    public boolean deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                return deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件path
     * @return 删除成功返回true，否则返回false
     */
    public boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录以及目录下的文件
     *
     * @param sPath 被删除目录的路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isIdeaProject(File file) {
        if (file.isFile()) return false;
        if (file.listFiles() == null) return false;
        for (File itemFile : file.listFiles()) {
            if (itemFile.isDirectory() && itemFile.getName().equals(".idea")) return true;
        }
        return false;
    }


    // 递归方式 计算文件的大小
    private long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile()) {
            return file.length();
        }
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null) {
            for (final File child : children) {
                total += getTotalSizeOfFilesInDir(child);
            }
        }
        return total;
    }

    public String formatSize(long length) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (length == 0L) {
            return wrongSize;
        } else {
            fileSizeString = length < (long) 1024 ? df.format((double) length) + "B" : (length < (long) 1048576 ? df.format((double) length / (double) 1024) + "KB" : (length < (long) 1073741824 ? df.format((double) length / (double) 1048576) + "MB" : df.format((double) length / (double) 1073741824) + "GB"));
            return fileSizeString;
        }
    }
}
