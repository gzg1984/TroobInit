package index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.nio.file.Paths;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.Fragmenter;

import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;

import java.util.LinkedList;
import java.io.File;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import java.nio.file.FileSystems;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;

public class index {
    static boolean verbose = false;
    static String fileRoot = "/opt/file_root/index_base/";
    static String projectName = "test";
    static String projectPath = fileRoot + projectName;
    static String field = "content";
    static String indexPath = "";

    private static void handleOpthins(String[] args) throws IOException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("p", "project", true, "Project Name");
        options.addOption("r", "root", true, "root folder to search project and create index");
        try {
            CommandLine commandLine = parser.parse(options, args);
            // Set the appropriate variables based on supplied options

            if (commandLine.hasOption('h')) {
                System.out.println("Help Message");
                System.exit(0);
            }

            if (commandLine.hasOption('r')) {
                fileRoot = commandLine.getOptionValue('r');
            }

            if (fileRoot.charAt(fileRoot.length() - 1) != '/') {
                fileRoot = fileRoot + "/";
            }

            if (commandLine.hasOption('p')) {
                projectName = commandLine.getOptionValue('p');
                projectPath = fileRoot + projectName;
            }

            indexPath = fileRoot + "Index";

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        handleOpthins(args);
        CreateIndex();
    }

    private static void CreateIndex() {
        /* search all file in project path */
        searchAllFileInProjectPath();
        /* push file into writer */
        /* write to index */

    }

    public static boolean isBinary(File file) {
        boolean isBinary = false;
        try {
            FileInputStream fin = new FileInputStream(file);
            long len = file.length();
            for (int j = 0; j < (int) len; j++) {
                int t = fin.read();
                if (t < 32 && t != 9 && t != 10 && t != 13) {
                    isBinary = true;
                    break;
                }
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isBinary;
    }

    public static void searchAllFileInProjectPath() {
        IndexWriter indexWriter = null;
        Directory indexDirectory = null;
        try {
            LinkedList<File> fileList = getAllFileRecuision(projectPath);
            if (fileList == null) {
                return;
            }
            if (fileList.size() == 0) {
                return;
            }

            indexDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexPath));
            Analyzer analyzer = new SourceFileAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
            indexWriter.deleteAll();
            for (File f : fileList) {
                // System.out.printf("%s\n", f.getAbsolutePath());
                if (isBinary(f)) {
                    System.out.println("文件" + f.getAbsolutePath() + "不是源代码文件，skip");
                    continue;
                }
                Document document = new Document();
                String sourceContent = "";
                try {
                    String fileEncoding = EncodingDetect.getJavaEncode(f);
                    sourceContent = FileUtils.readFileToString(f, fileEncoding);
                    document.add(new Field("content",sourceContent, TextField.TYPE_STORED));
                    document.add(new Field("fileName", f.getName(), TextField.TYPE_STORED));
                    document.add(new Field("filePath", f.getAbsolutePath() , TextField.TYPE_STORED));
                    //document.add(new Field("projectId",projectId,TextField.TYPE_STORED)); 
                    indexWriter.addDocument(document);
                } catch (Exception ex) {
                    ex.printStackTrace(); //
                    System.out.println("無法創建索引:" + f.getAbsolutePath());
                    continue;
                }
            }

        } catch (

        Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (indexWriter != null) {
                    indexWriter.commit();
                    indexWriter.close();
                    indexDirectory.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static LinkedList<File> getAllFileRecuision(String path) {
        LinkedList<File> list = new LinkedList<File>();
        System.out.printf("Try to Analyze %s\n", path);

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                return null;
            } else {
                for (File file2 : files) {
                    try {
                        if (file2.isDirectory()) {

                            /*
                             * 2020 11 03 这里是用来新增文件的？
                             * 
                             * ProjectFilePO filePo = new ProjectFilePO(parentFileId,relativePath,
                             * FileType.DIRECTORY, null, projectId, file2.getName());
                             * filePo.setUuid(UUID.randomUUID().toString()); poList.add(filePo);
                             */
                            // projectServiceImpl.addProjectFile(filePo);

                            // System.out.println("文件夹:" +
                            // file2.getAbsolutePath());
                            // LinkedList<File> fileList=
                            // getAllFileRecuision(file2.getAbsolutePath(),projectId,filePo.getFileId());
                            /*
                             * 2020 11 03 递归调用本函数自己 LinkedList<File> fileList =
                             * getAllFileRecuision(file2.getAbsolutePath(), projectId, filePo.getUuid(),
                             * poList); if (null != fileList) { list.addAll(fileList); }
                             */

                        } else {
                            // System.out.println("文件:" +
                            // file2.getAbsolutePath());

                            list.add(file2);

                            // parentFileId++;

                            /*
                             * 2020 11 03 这里是用来新增文件的？ projectWithBLOBs filePo = new
                             * projectWithBLOBs(parentFileId, relativePath, FileType.FILE,
                             * String.valueOf(file2.length()), projectId, file2.getName());
                             * filePo.setUuid(UUID.randomUUID().toString()); poList.add(filePo);
                             */

                            // projectServiceImpl.addProjectFile(filePo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return list;
            }
        } else {
            System.out.println("文件不存在!");
            return null;
        }
    }

}
