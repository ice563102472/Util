import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Iterator;


/**
 * 非线程安全的
 * Created by yangzhuo on 17-5-20.
 */
public class FileWrapper extends File {

    private static final long serialVersionUID = 215824904371497156L;
    private volatile long lastModified = 0;
    private int count = -1;
    private transient Scanner scanner = null;
    private List<String> fileList = new ArrayList<>();

    public FileWrapper(String pathname) {
        super(pathname);
        try {
            initScanner();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void initScanner() throws FileNotFoundException {
        if (scanner == null) {
            try {
                scanner = new Scanner(this, "utf8");
            } catch (FileNotFoundException ex) {
                throw new FileNotFoundException(ex.getMessage());
            }
        }
    }

    /**
     * 返回文件行数
     */
    public int getCount() {
        if (this.lastModified() != lastModified) {
            fileList = readLines();
            count = fileList.size();
        } else {
            int fileSize = fileList.size();
            if (fileSize <= 0) {
                fileList = readLines();
            }
            count = fileList.size();
        }
        return count;
    }

    public List<String> readLines() {
        if (this.lastModified() != lastModified) {
            //没有用随机读取文件的策略
            //初始值采用上一次读取文件的值
            fileList = new ArrayList<>(fileList.size());
            if (!Objects.isNull(scanner)) {
                while (scanner.hasNextLine()) {
                    fileList.add(scanner.nextLine());
                }
            }
            lastModified = this.lastModified();
        }
        return fileList;
    }

    public Iterator<String> readLine() {
        if (this.lastModified() != lastModified) {
            fileList = readLines();
        } else if (fileList.size() <= 0) {
            fileList = readLines();
        }
        return fileList.iterator();
    }

    public static void main(String[] args) throws Exception {
        File file = new File("");
        Scanner scanner = new Scanner(file, "utf8");
        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileWrapper)) return false;
        if (!super.equals(o)) return false;
        FileWrapper that = (FileWrapper) o;
        return lastModified == that.lastModified &&
                getCount() == that.getCount() &&
                com.google.common.base.Objects.equal(scanner, that.scanner) &&
                com.google.common.base.Objects.equal(fileList, that.fileList);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(super.hashCode(), lastModified, getCount(), scanner, fileList);
    }
}
