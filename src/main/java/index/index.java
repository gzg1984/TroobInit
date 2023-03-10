package index;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
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
    static int rootLength=0;
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
                //projectPath =   projectName;
            }

            indexPath = fileRoot + "Index";
            //indexPath =  "Index";
            File fRoot=new File(fileRoot);
            String rootAbsolute=fRoot.getAbsolutePath();
            System.out.println("Root Absolute Path :  " + rootAbsolute);

            rootLength=rootAbsolute.length();

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
                System.out.println("Find No file in " + projectPath);
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
                    System.out.println("file " + f.getAbsolutePath() + " is not txt source, skip");
                    continue;
                }
                Document document = new Document();
                String sourceContent = "";
                try {
                    String fileEncoding = EncodingDetect.getJavaEncode(f);
                    sourceContent = FileUtils.readFileToString(f, fileEncoding);
                    document.add(new Field("content", sourceContent, TextField.TYPE_STORED));
                    document.add(new Field("fileName", f.getName(), TextField.TYPE_STORED));
                    document.add(new Field("filePath", f.getAbsolutePath().substring(rootLength), TextField.TYPE_STORED));
                    // document.add(new Field("projectId",projectId,TextField.TYPE_STORED));
                    indexWriter.addDocument(document);
                } catch (Exception ex) {
                    ex.printStackTrace(); //
                    System.out.println("Cannot create index:" + f.getAbsolutePath());
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
                            //System.out.printf("Try to Recuision list %s\n", file2.getName());
                            if (!file2.getName().equals(".git")){
                                LinkedList<File> fileList = getAllFileRecuision(file2.getAbsolutePath());
                                if (null != fileList) {
                                    list.addAll(fileList);
                                }
                            }

                        } else {
                            list.add(file2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return list;
            }
        } else {
            System.out.println("file not exist!");
            return null;
        }
    }

}
