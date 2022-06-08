package ru.geekbrains.cloud_storage_server.messages;


import io.netty.channel.ChannelHandlerContext;
import ru.geekbrains.cloud_storage_common.model.AbstractMessage;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_server.authorization.AuthService;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MessageHandler {

    private static AuthService authService;

    public MessageHandler(AuthService auth) {
        authService = auth;
    }

    // Обработка служебных команд
    public static void handleMessage(ChannelHandlerContext ctx,AbstractMessage msg) throws Exception{
        System.out.println(((ServiceCommand) msg).getCommand());

        if (msg instanceof ServiceCommand) {
            switch (((ServiceCommand) msg).getCommand()) {
                // Подключение клиента к серверу
                case "-connect":
                    ctx.writeAndFlush(new ServerResponse(
                            Arrays.asList("Добро пожаловать!",
                                    "Для доступа к облачному хранилищу необходимо авторизоваться.",
                                    "Отправьте команду -help для получения справки")));
                    break;

                // Вызов справки
                case "-help":
                    List<String> help = readTextFile("help.txt");
                    ctx.writeAndFlush(new ServerResponse(help)); break;

                // Авторизация
                case "-auth":
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Вы успешно авторизировались под ником "
                            + authorize((ServiceCommand) msg).getNickname())));
                    break;

                // Список пользователей/файлов пользователя
                case "-list":
                    // Временно!
                        ctx.writeAndFlush(new ServerResponse(authService.showAllUsers()));
                    break;

                // Регистрация нового пользователя
                case "-registr":
                    //создать папку на сервере
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Регистрация прошла успешно. " +
                            "Ваш ник: " + registration((ServiceCommand) msg).getNickname())));
                    break;

                case "-newnick":
                    // Смена рандомного ника на свой собственный
                    // Не забыть проверить ник на уникальность
                    break;

                case "-newpass":
                    // Сменить пароль
                    break;

                // Завершение работы
                case "-exit":
                    ctx.close();
                    break;

                default:
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Неправильная команда. Вызов справки: -help")));
                    break;
            }
        }

    }

    // Регистрация нового пользователя
    private static User registration(ServiceCommand msg) {
        String [] str_arr =msg.getData();
        var login = str_arr[1];
        var password = str_arr[2];
        User user = authService.createNewUser(login, password);
        user.getFolderPath().mkdirs();
        return user;
    }

    // Авторизация пользователя
    private static User authorize(ServiceCommand msg) {
        String [] str_arr =msg.getData();
        var login = str_arr[1];
        var password = str_arr[2];
        User user = authService.authorizeUserByLoginAndPassword(login, password);
        return user;
    }

    // Чтение из файла и построчная запись данных в List<>
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
