package ca.team2706.scouting.mcmergemanager.backend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.FTPRequester;

import static java.lang.System.in;
import static java.lang.Thread.sleep;

public class FTPClient {
    org.apache.commons.net.ftp.FTPClient ftpClient = new org.apache.commons.net.ftp.FTPClient();

    // for FTP server credentials
    private String hostname;
    private String password;
    private String username;

    //port for connection
    private int port;

    //Local and remote directory on device for files being downloaded.
    private File localPath;
    private String remotePath;
    private String DocumentsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();

    //Is the client connected?
    private Object connectedThreadLock = new Object();
    private boolean connected = false;
    private boolean syncing = false;
    private boolean initilized = false;


    /**
     * Constructor without port option
     * @param hostname: Server IP Adress
     * @param username: Login Credential
     * @param password: Login Credential
     * @param localPath: Local path for saving to the device
     */
    public FTPClient(String hostname, String username, String password, String localPath, String remotePath){
        this.hostname = hostname;
        this.password = password;
        this.username = username;
        this.localPath = new File(localPath);
        this.remotePath = remotePath;
        this.port = 21;
        Log.d("FTPClient|init", "Local Path: " + localPath + "\nRemote Path: " + remotePath);
        this.initilized = true;
    }
    public FTPClient (String localPath){
        this.localPath = new File(localPath);
        this.initilized = false;
    }

    /**
     * Constructor with port option
     * @param hostname: Server IP Adress
     * @param username: Login Credential
     * @param password: Login Credential
     * @param localPath: Local path for saving to the device
     * @param port: Custom port connection
     */
    public FTPClient(String hostname, String username, String password, String localPath, int port){
        this.hostname = hostname;
        this.password = password;
        this.username = username;
        this.localPath = new File(localPath);
        this.port = port;
        Log.d("FTPClient|init", "Local Path: " + localPath + "\nRemote Path: " + remotePath);
        this.initilized = true;
    }

    /**
     * Tells you whether the FTPClient is connected or not.
     *
     * @return whether the FTPClient is connected or not.
     **/
    public boolean isConnected() {
        synchronized (connectedThreadLock) {
            return connected;
        }
    }

    /**
     * Tries to connect to a server with the parameters supplied with constructor
     *
     * @throws ConnectException
     */
    public void connect() throws ConnectException {
        if(!initilized) return;
        ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            return;
        }
        //his.nukeLocalFiles(true, true, true); //JUST HERE FOR DEBUGGING!!! if things arn't working, remove this line.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (connectedThreadLock) {
                    try {
                        String tolog = "";
                        ftpClient.connect(hostname, port);
                        tolog += ftpClient.getReplyString();
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.login(username, password);
                        tolog += ftpClient.getReplyString();
                        connected = true;
                        Log.i("FTPClient|connect", tolog);
                    } catch (Exception e) {
                        Log.e("FTPClient|connect", e.toString());
                        connected = false;
                    }
                }
            }
        });
        try {
            sleep(5);  //multi-threaded voodoo. Give the AsyncTask 5 ms to get started and get the lock.
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

    /**
     * Downloads all missing files on device from the server, and
     * uploads all missing files on server from device.
     * @param requester: Callback for thread
     * @throws ConnectException
     */
    public void syncAllFiles(final FTPRequester requester, final Activity activity)throws ConnectException{
        if(!initilized) return;
      synchronized (connectedThreadLock){}
        if(syncing){
            Log.e("FTPClient|INFO", "A sync is already in progress!");
            return;
        }else{
            requester.updateSyncBar("_Connecting...", 0, activity, true);
            connect();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run(){
                Log.i("FTPClient|INFO", "Starting SYNC");
                requester.updateSyncBar("_Finding differences...", 0, activity, true);
                int currentProgress = 0;
                int maxUpProgress = 0;
                int maxDownProgress = 0;
                ArrayList<String> localNames;
                ArrayList<String> remoteNames;
                ArrayList<String> filesToUpload = new ArrayList<String>();
                ArrayList<String> filesToDownload = new ArrayList<String>();
                int changed = 0;
                localNames = getLocalDir();
                try{
                    checkFilepath(remotePath, true);
                    remoteNames = getRemoteDir(remotePath, "", 0);
                }catch(Exception e) {
                    Log.e("FTPClient|sync", "Failed to get remote file listing");
                    requester.updateSyncBar("Error while syncing, see debug for more info.", 100, activity, true);
                    syncing = false;
                    return;
                }
                Log.i("FTPClient|INFO", "Local Path: " + localPath.toString() + "/");
                try {
                    for (String remoteName : remoteNames) {
                        String usableName = DocumentsPath + remoteName;
                        if (!localNames.contains(usableName))
                            filesToDownload.add(remoteName);
                    }
                    for (String localName : localNames) {
                        String usableName = remotePath + localName.split(localPath.getAbsolutePath())[1];
                        if(localName==localPath.getAbsolutePath()) continue;
                        if (!remoteNames.contains(usableName))
                            filesToUpload.add(localName);
                    }
                    maxDownProgress = filesToDownload.size();
                    maxUpProgress = filesToUpload.size();
                    for (String fileToDownload : filesToDownload) {
                        String newFile = fileToDownload;
                        downloadSync(fileToDownload);
                        changed += 1;
                        currentProgress += 1;
                        requester.updateSyncBar("Downloading file " + currentProgress + "/" + maxDownProgress, (currentProgress*100) / maxDownProgress, activity, true);
                    }
                    currentProgress = 0;
                    for (String fileToUpload : filesToUpload) {
                        String newFile = fileToUpload;
                        uploadSync(fileToUpload);
                        changed += 1;
                        currentProgress += 1;
                        requester.updateSyncBar("Uploading file " + currentProgress + "/" + maxUpProgress, (currentProgress*100) / maxUpProgress, activity, true);
                    }
                    requester.syncCallback(changed);

                }catch(Exception e){
                    Log.e("FTPClient|sync", e.toString());
                    changed = -1;
                }
                if(changed<1)
                    if(changed==0)
                        requester.updateSyncBar("You're up to date!", 100, activity, false);
                    else
                        requester.updateSyncBar("ERROR", 100, activity, false);
                else
                    requester.updateSyncBar("Done!", 100, activity, false);
                Log.d("FTPClient|INFO", "Sync done!");
                try{ftpClient.disconnect();}catch(Exception e){Log.e("FTPClient|Disconnect", "failed to disconnect", e);}

            }
        });
    }

    /**
     * Upload a file synchronomously
     * @param filename: local file to upload
     */
    private void uploadSync(String filename){
        if(!initilized) return;
        String RemotePath = filename.split("MCMergeManager")[1];
        RemotePath = "/MCMergeManager" + RemotePath;
        File file = new File(filename);
        Log.i("FTPClient|Upload",   "Filepath: " + filename +
                                    "\nFilesize: "+(file.length()) + " bytes"+
                                    "\nParent Dir check: " + String.valueOf(checkFilepath(RemotePath, true))+
                                    "\n"
                                    );
        try {
            InputStream is = new FileInputStream(filename);
            ftpClient.storeFile(RemotePath, is);
        }catch(Exception e) {
            Log.e("FTPClient|Upload", e.toString());
            return;
        }
    }

    /**
     * Download a file synchronomously
     * @param RemotePath: remote file to download
     */
    private void downloadSync(String RemotePath){
        if(!initilized) return;
        String filename = localPath.getAbsolutePath() + RemotePath.split("Team Photos")[1];
        String sizeinbytes = "";
        try{
            ftpClient.changeWorkingDirectory("/");
            sizeinbytes = Long.toString(ftpClient.mlistFile(RemotePath).getSize());
        }catch(Exception e){
            sizeinbytes = "[error]";
        }
        Log.i("FTPClient|Download", "Filepath: " + RemotePath +
                                    "\nFile size: "+sizeinbytes+" bytes"+
                                    "\nParent Dir check: " + String.valueOf(checkFilepath(filename, false))+
                                    "\n"
                                    );
        try {
            OutputStream os = new FileOutputStream(filename);
            ftpClient.retrieveFile(RemotePath, os);
        }catch(Exception e){
            Log.e("FTPClient|Download", e.toString());
            return;
        }
    }

    /**
     * Method for retrieving an array of files on the local device.
     * localDirSlave() is also a part of this.
     * @return
     */
    public ArrayList<String> getLocalDir(){
        checkFilepath(localPath.getAbsolutePath(), false);
        String topLevelPath = localPath.getAbsolutePath();
        ArrayList<String> filenames = new ArrayList<>();
        ArrayList<String> localDirSlaveReturn = new ArrayList<>();
        File topLevel = new File(topLevelPath);
        File[] topDir = topLevel.listFiles();
        for(File file : topDir){
            if(file.isDirectory()){
                filenames.addAll(localDirSlave(file.getAbsolutePath()));
            }else{
                    filenames.add(file.getAbsolutePath());
            }
        }
        return filenames;
    }

    /**
     * Slave to getLocalDir() for recursiveness.
     * @param currentPath: the path to list
     * @return: files in current path and all subfolders
     */
    private ArrayList<String> localDirSlave(String currentPath){
        ArrayList<String> filenames = new ArrayList<>();
        File newLevel = new File(currentPath);
        for(File file : newLevel.listFiles()){
            if(file.isDirectory()){
                filenames.addAll(localDirSlave(file.getAbsolutePath()));
            }else{
                    filenames.add(file.getAbsolutePath());
            }
        }
        return filenames;
    }

    /**
     *  Gets a list of all filenaes of files on the FTP Server
     * @param parentDir: to peice together the dir() command
     * @param currentDir: to peice together the dir() command
     * @param level: used for determining when to stop
     * @return The string array of all filenames on the server
     * @throws IOException
     */
    public ArrayList<String> getRemoteDir(String parentDir, String currentDir, int level) throws IOException {
        if(!initilized) return new ArrayList<String>();
        ArrayList<String> filenames = new ArrayList<>();
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        if( !ftpClient.changeWorkingDirectory(dirToList)) {
            // This folder doesn't exist, or we can't access it
            Log.w("FTPClient|getting dirs", "Failed cd'ing into folder \""+dirToList+"\".");
            return filenames;
        }

       // Log.d("FTPClient", ftpClient.getReplyString() + "(" + ftpClient.getReply() + ")");
        FTPFile[] subFiles = ftpClient.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    continue;
                }
                for (int i = 0; i < level; i++) {
                    System.out.print("\t");
                }
                if (aFile.isDirectory()) {
                    filenames.addAll(getRemoteDir(dirToList, currentFileName, level + 1));
                } else {
                    filenames.add(ftpClient.printWorkingDirectory() + "/" + aFile.getName());
                }
            }
        }
        return filenames;
    }

    /**
     *  Checks the specified path for existance on either
     *   the FTP Server or the local device.
     * @param filename: the filename to check
     * @param isFTP: if the filename should be checked remotely or locally
     * @return: false is path does not exist, and was failed to create or
     *          true if it already exists or was created sucsessfully.
     */
    private boolean checkFilepath(String filename, boolean isFTP){
        if(isFTP){
            String parentDir = new File(filename).getParent();
            Log.d("FTPClient|checkFilePath", filename);
            Log.d("FTPClient|checkFilePath", "Checking for: " + parentDir);
            try{
                ftpClient.changeWorkingDirectory("/");
                String[] CDs = parentDir.split("/");
                for(String CD : CDs){
                    if (CD.equals("")) continue;

                    if(!ftpClient.changeWorkingDirectory(CD)){
                        ftpClient.makeDirectory(CD);
                        return ftpClient.changeWorkingDirectory(CD);
                    }else{
                        return true;
                    }
                }
            }catch(Exception e){
                Log.e("FTPClient|checkFilePath", e.toString());
            }
            return false;
        }else {

            if(!initilized) return false;
            File file = new File(filename);
            if(filename==localPath.getAbsolutePath()){
                return file.mkdirs();
            }
            File parentDir = file.getParentFile();
            if (parentDir.exists()) {
                return true;
            } else {
                return parentDir.mkdirs();
            }
        }
    }

    /**
     * Pretty self explanitory, prepares your device for a new year.
     * @param AreYouSure
     * @param AreYouREALLYSure
     * @param AreYou100PercentCertain
     * All parameters should be pretty self explanitory aswell.
     * @return: if files were nuked properly (should always be true)
     */
    public boolean nukeLocalFiles(boolean AreYouSure, boolean AreYouREALLYSure, boolean AreYou100PercentCertain){
        if(!(AreYouSure&&AreYouREALLYSure&&AreYou100PercentCertain)) return false;
        deleteRecursive(localPath);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        SharedPreferences.Editor editor1 = settings.edit();
        editor1.putString(App.getContext().getString(R.string.PROPERTY_FTPNukeLocalFile),"Replace with 'Yes', then reload the page");
        editor1.commit();
        return true;
    }

    /**
     * helper for the nukeLocalFiles() method
     * @param fileOrDirectory: fileOrDirectory to remove
     */
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()){
                Log.d("FTPClient|NUKE", "Removing folder: " + child.getAbsolutePath());
                deleteRecursive(child);
            }
        fileOrDirectory.delete();
    }
}
