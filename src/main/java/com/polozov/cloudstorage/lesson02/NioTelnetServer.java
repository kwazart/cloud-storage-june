package com.polozov.cloudstorage.lesson02;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class NioTelnetServer {
    private static final String LS_COMMAND = "ls          view all files from current directory\n\r";
    private static final String MKDIR_COMMAND = "mkdir       view all files from current directory\n\r";
    private static final String TOUCH_COMMAND = "touch       create a file\n\r";
    private static final String CD_COMMAND = "cd          change directory\n\r";
    private static final String RM_COMMAND = "rm          remove file or directory\n\r";
    private static final String COPY_COMMAND = "copy        copy file or directory\n\r";
    private static final String CAT_COMMAND = "cat         output file data\n\r";
    private static final String CHANGENICK_COMMAND = "changenick  change nickname\n\r";

    private Path currentPath = Paths.get("server");

    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    private Map<SocketAddress, String> clients = new HashMap<>();

    public NioTelnetServer() throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(5679));
        server.configureBlocking(false);
        Selector selector = Selector.open();

        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client connected. IP:" + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "skjghksdhg");
        channel.write(ByteBuffer.wrap("Hello user!\n\r".getBytes(StandardCharsets.UTF_8)));
    }

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAddress client = channel.getRemoteAddress();
        int readBytes = channel.read(buffer);

        if (readBytes < 0) {
            channel.close();
            return;
        } else  if (readBytes == 0) {
            return;
        }

        buffer.flip();
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        buffer.clear();

        // TODO: 21.06.2021
        // touch (filename) - создание файла
        // mkdir (dirname) - создание директории
        // cd (path | ~ | ..) - изменение текущего положения
        // rm (filename / dirname) - удаление файла / директории
        // copy (src) (target) - копирование файлов / директории
        // cat (filename) - вывод содержимого текстового файла
        // changenick (nickname) - изменение имени пользователя

        // добавить имя клиента

        if (key.isValid()) {
            String command = sb.toString()
                    .replace("\n", "")
                    .replace("\r", "");

            if (command.contains(" ")) {
                String param = splitCommand(command)[1];
                Path path = currentPath.resolve(param);
                if (command.startsWith("mkdir")) {
                    Files.createDirectories(path);
                } else if (command.startsWith("touch")) {
                    Files.createFile(path);
                } else if (command.startsWith("cd")) {
                    goToAdBack(path, param, selector, client);
                } else if (command.startsWith("rm")) {
                    removeFileOrDir(path);
                } else if (command.startsWith("copy")) {
                    copyFile(command);
                }
            } else if ("--help".equals(command)) {
                sendMessage(LS_COMMAND, selector, client);
                sendMessage(MKDIR_COMMAND, selector, client);
                sendMessage(TOUCH_COMMAND, selector, client);
                sendMessage(CD_COMMAND, selector, client);
                sendMessage(COPY_COMMAND, selector, client);
                sendMessage(CAT_COMMAND, selector, client);
                sendMessage(RM_COMMAND, selector, client);
                sendMessage(CHANGENICK_COMMAND, selector, client);
            } else if ("ls".equals(command)) {
                sendMessage(getFilesList().concat("\n\r"), selector, client);
            } else {
                sendMessage("Enter --help for support info\n\r", selector,client);
            }
        }
        sendMessage(currentPath.toString().concat(": "), selector, client);
    }

    private void copyFile(String command) throws IOException {
        String[] paths = splitCommand(command);
        if (Files.isDirectory(currentPath.resolve(paths[1]))) {
            walkTreeForCopy(currentPath.resolve(Paths.get(paths[1])), currentPath.resolve(Paths.get(paths[2])));
        } else {
            Files.copy(currentPath.resolve(paths[1]).normalize(), currentPath.resolve(paths[2]).normalize());
        }
    }

    private void walkTreeForCopy (Path pathFrom, Path pathTo) throws IOException {
        List<File> list = new ArrayList<>();
        String from = pathFrom.toString();
        String to = pathTo.toString();

        Files.walkFileTree(pathFrom, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                File[] files = new File(dir.toAbsolutePath().toString()).listFiles();
                for (File f: files) {
                    list.add(f);
                }

                if (list.size() != 0) {
                    for (File file : list) {
                        String f = file.toString().replace(from, to);
                        if (file.isDirectory()) {
                            Files.createDirectory(Paths.get(f));
                        } else {
                            Files.copy(file.toPath(), Paths.get(f));
                        }
                    }
                }
                list.clear();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) {
                    ((SocketChannel) key.channel()).write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private String[] splitCommand(String command) {
        return command.split(" ");
    }

    private String getFilesList() {
        String[] servers = new File(currentPath.toAbsolutePath().toString()).list();
        return String.join("\n\r" + "", servers);
    }

    private void goToAdBack(Path path, String param, Selector selector, SocketAddress client) throws IOException {
        if (param.equals("~")) {
            currentPath = Paths.get("server");
        } else if (param.equals("..") && !(currentPath.toString()).equals("server")) {
            currentPath = currentPath.getParent();
        } else if (Files.isDirectory(path)) {
            currentPath = path;
        } else {
            sendMessage("Directory like this is not exist", selector, client);
        }
    }

    private void removeFileOrDir (Path path) throws IOException {
        if (Files.isDirectory(path)) {
            List<File> list = walkTree(path);
            while (!list.isEmpty()) {
                Iterator<File> iterator = list.iterator();
                while (iterator.hasNext()) {
                    File file = iterator.next();
                    if (!file.isDirectory() || (file.isDirectory() && isDirectoryEmpty(file))) {
                        file.delete();
                        iterator.remove();
                        list.remove(file);
                    }
                }
            }
        }
        Files.delete(path);
    }

    private List<File> walkTree (Path path) throws IOException {
        List<File> list = new ArrayList<>();

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                File[] files = new File(dir.toAbsolutePath().toString()).listFiles();
                for (File f: files) {
                    list.add(f);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return list;
    }

    private boolean isDirectoryEmpty(File directory) {
        String[] files = directory.list();
        return files.length == 0;
    }

    public static void main(String[] args) throws Exception {
        new NioTelnetServer();
    }
}
