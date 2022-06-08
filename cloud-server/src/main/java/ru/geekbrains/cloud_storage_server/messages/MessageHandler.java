package ru.geekbrains.cloud_storage_server.messages;


import io.netty.channel.ChannelHandlerContext;
import ru.geekbrains.cloud_storage_common.model.AbstractMessage;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MessageHandler {

    public static void handleMessage(ChannelHandlerContext ctx,AbstractMessage msg) throws Exception{
        System.out.println(((ServiceCommand) msg).getCommand());
        if (msg instanceof ServiceCommand) {
            switch (((ServiceCommand) msg).getCommand()) {
                case "-connect":
                    ctx.writeAndFlush(new ServerResponse(
                            Arrays.asList("Добро пожаловать!",
                                    "Для доступа к облачному хранилищу необходимо авторизоваться.",
                                    "Отправьте команду -help для получения справки")));
                    break;
                case "-help":
                    List<String> help = readTextFile("help.txt");
                    ctx.writeAndFlush(new ServerResponse(help)); break;
                case "-auth":
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Авторизация"))); break;

                default: break;
            }
        }

    }

    private static List<String> readTextFile(String filename) throws IOException {
        List<String> ret = new ArrayList<>();
        FileReader fr = new FileReader(filename);
        Scanner scanner = new Scanner(fr);
        while (scanner.hasNextLine()) {
            ret.add(scanner.nextLine());
        }
        fr.close();
        return ret;
    }
}
