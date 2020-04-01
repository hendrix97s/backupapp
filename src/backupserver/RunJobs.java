/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backupserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author Luiz
 */
public class RunJobs {

    static final int TAMANHO_BUFFER = 4096; // 4kb
    private static String origemDoctors;
    private static final String origemInfos = "/SysBackup/";
    private static String nameFileZip;
    private static final String hostDstDir = "/domains/axonsistemas.com.br/public_html/backups/";
    private static final String host = "127.0.0.1";
    private static final int port = 21;
    private static String user;
    private static final String userHost = "luiz";
    private static final String pass = "123456789";
    private static String agendamento;

    public static void start() throws IOException, SocketException, InterruptedException {
        construct();
        String[] agendamentos = agendamento.split("-");

        while (true) {
            Calendar cal = Calendar.getInstance();
            String horaDaMaquina = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
            try {
                if (((cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)).equals(agendamentos[0])) || ((cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)).equals(agendamentos[1]))) {
                    System.out.println(cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
                    schedulerJobs();
                }
                Thread.sleep(60000); // 1 minuto
            } catch (InterruptedException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static void schedulerJobs() throws IOException, SocketException, InterruptedException {
        construct();
        moveFilesFromDirectory(origemDoctors + "/dados/doctors.seg");
        compactFile();
        sendFileToServerFtp();
    }

    private static void message(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    private static void construct() throws FileNotFoundException, IOException {
        FileReader infos = new FileReader(origemInfos + "infos.txt");
        BufferedReader readInfos = new BufferedReader(infos);
        String line = readInfos.readLine();
        String[] ar;
        String getLine;
        int index = 0;
        while (line != null) {
            getLine = line.replace("\\", "/");
            ar = getLine.split("/");
            if (index == 0) {
                origemDoctors = line;
            } else if (index == 1) {
                user = line;
            } else {
                agendamento = line;
            }
            index++;
            line = readInfos.readLine();
        }
    }

    // pega a origem do arquivo doctors.seg (responsavel por conter as URIS de backup)
    private static void moveFilesFromDirectory(String origemDocSeg) {
        String URI = origemInfos + "doctors-backup";
        createDirectoryToBackup(URI);
        try (FileReader doctorsSeg = new FileReader(origemDocSeg)) {
            BufferedReader readDoctorsSeg = new BufferedReader(doctorsSeg);
            String line = readDoctorsSeg.readLine();
            String[] ar;
            String[] getExt;
            String getLine;
            String constructOrigem = "";

            while (line != null) {

                getLine = line.replace("\\", "/");
                ar = getLine.split("/");
                getExt = ar[ar.length - 1].split("\\.");
                int index = 0;
                // pega o caminho de origem de cada linha do arquivo DOCTORS.SEG
                while (index < ar.length - 1) {
                    constructOrigem += ar[index].toLowerCase() + "/";
                    index++;
                }

                File dir = new File(URI + constructOrigem);
                if (!dir.exists()) {
                    createDirectoryToBackup(URI + constructOrigem);
                }
                copyFiles(constructOrigem, URI + constructOrigem, getExt[1].toLowerCase());
                constructOrigem = "";
                line = readDoctorsSeg.readLine();
            }
        } catch (Exception e) {
            e.getMessage();
        }

        System.out.println();

    }

    private static void createDirectoryToBackup(String directory) {
        File backup = new File(directory);

        if (backup.mkdirs()) {
            System.out.println("Diretório [" + directory + "] criado!");
        } else {
            System.out.println("120: erro ao criar diretorio! [" + directory + "]");
        }

    }

    private static void copyFiles(String source, String destination, String ext) throws FileNotFoundException, IOException {
        File src = new File(source);
        File dst = new File(destination);
        File[] files = src.listFiles();

        String[] teste;
        for (File f : files) {

            String fileName = f.getName();
            teste = fileName.split("\\.");
            if (teste.length > 1) {
                if (teste[1].toLowerCase().equals(ext)) {
                    FileInputStream fis = new FileInputStream(src + "/" + fileName);
                    FileOutputStream fos = new FileOutputStream(dst + "/" + fileName);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                } else if (ext.equals("*")) {
                    FileInputStream fis = new FileInputStream(src + "/" + fileName);
                    FileOutputStream fos = new FileOutputStream(dst + "/" + fileName);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);

                    }
                }
            }
        }// fim de for
    } // fim de copyFiles

    private static void compactFile() throws IOException {
        String sourceFile = origemInfos + "doctors-backup";
        FileOutputStream fos = new FileOutputStream(origemInfos + returnData() + "-doctors-backup.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        nameFileZip = returnData() + "-doctors-backup.zip";
        ZipUtils.zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void clearDorectory(String directory) throws IOException {
        File f = new File(directory);
        //message("Arquivos deletados!");
    }

    // abre conexão com o server e envia o arquivo compactado
    private static void sendFileToServerFtp() throws SocketException, IOException, InterruptedException {
        FTPClient ftp = new FTPClient();
        ftp.connect(host, port);
        System.out.println(user);
        System.out.println(userHost);
        ftp.login(userHost, pass);
        ftp.changeWorkingDirectory("testeFTP");
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        boolean done;
        try (FileInputStream arqSend = new FileInputStream(origemInfos + nameFileZip)) {
            ftp.makeDirectory(hostDstDir + "backup-" + user);
            done = ftp.storeFile(hostDstDir + "backup-" + user + "/" + user + "_" + nameFileZip, arqSend);

            if (done) {
                System.out.println("Arquivo enviado com sucesso!");
//                message("Arquivo de backup do doctors enviado para o servidor com sucesso!");
            } else {
                System.out.println("Erro ao armazenar aquivo!");
//                message("Erro ao enviar dados para o servidor!");
            }
        }
        String URI = origemInfos + "doctors-backup";
        TimeUnit.SECONDS.sleep(1);
        clearDorectory(URI);

    }

    private static String returnData() {
        Date data = new Date();
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        String formatData = formatador.format(data);
        String arrayFormatData[] = new String[3];
        arrayFormatData = formatData.split("/");
        formatData = arrayFormatData[0] + "-" + arrayFormatData[1] + "-" + arrayFormatData[2];
        return formatData;
    }

    public static void removerArquivos(File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files) {
                System.gc();
                boolean retorno = file.delete();// Aqui que o arquivo deve ser deletado.
                removerArquivos(file);
            }
        }
        f.delete();
    }

    private static void or(boolean equals) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
