package ru.geekbrains.cloud_storage_server.entity;

import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.util.*;

public class ListOfUsers {
    private static Map<User, ChannelHandlerContext> connectedUsers = new HashMap<>();

    public static void addUserToList (User user, ChannelHandlerContext ctx) {
        connectedUsers.put(user, ctx);
    }

    public static void removeUserFromList ( ChannelHandlerContext ctx) {
        connectedUsers.remove(getUser(ctx));
    }

    public static boolean IsUserConnected (User user) {
        for (Map.Entry<User, ChannelHandlerContext> entry : connectedUsers.entrySet()) {
            if (user.equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static Map<User, ChannelHandlerContext> getConnectedUsers() {
        return connectedUsers;
    }

    public static ChannelHandlerContext getChannel (User user) {
        return connectedUsers.get(user);
    }

    public static User getUser (ChannelHandlerContext ctx) {
        User user = null;
        for (Map.Entry<User, ChannelHandlerContext> entry : connectedUsers.entrySet()) {
            if (entry.getValue().channel().equals(ctx.channel())) {
                user = entry.getKey();
                break;
            }
        }
        return user;
    }

    public static List<String> getListOfFiles (ChannelHandlerContext ctx) {
            List<String> files = new ArrayList<>();
            Arrays.asList(new File(ListOfUsers.getUser(ctx).getFolderPath()).listFiles())
                    .stream().forEach((l) -> files.add(l.getName()));

        return files;
    }

    public static long getUsedMemory(User user) {
        return Arrays.asList(new File(user.
                        getFolderPath()).listFiles())
                .stream().mapToLong((f)->f.length()).sum();
    }
}
