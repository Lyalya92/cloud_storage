package ru.geekbrains.cloud_storage_server.messages;


import io.netty.channel.ChannelHandlerContext;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_server.authorization.AuthService;
import ru.geekbrains.cloud_storage_server.database.DatabaseService;
import ru.geekbrains.cloud_storage_server.entity.ListOfUsers;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MessageHandler {

    private static AuthService authService;
    private static DatabaseService databaseService = DatabaseService.getInstance();

    public MessageHandler(AuthService auth) {
        authService = auth;
    }

    // Обработка служебных команд
    public static void handleServiceCommand(ChannelHandlerContext ctx, ServiceCommand msg) throws Exception{
            switch (msg.getCommand()) {
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
                    var user = authorize(msg);
                    if (ListOfUsers.IsUserConnected(user)) {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Вы уже вошли в систему!")));
                    } else {
                        ListOfUsers.addUserToList(user, ctx);
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Вы успешно авторизировались под ником "
                                + user.getNickname())));
                    }
                    break;

                // Список пользователей
                case "-listU":
                    // Временно!
                        ctx.writeAndFlush(new ServerResponse(databaseService.showAllUsers()));
                    break;

                // Список файлов пользователя
                case "-list":
                    if (ListOfUsers.getUser(ctx)!=null) {
                        List<String> list = ListOfUsers.getListOfFiles(ctx);
                            if (list.isEmpty()) {
                                ctx.writeAndFlush(new ServerResponse(Arrays.asList("У Вас нет загруженных файлов")));
                            } else {
                                ctx.writeAndFlush(new ServerResponse(list));
                            }

                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

                case "-memory":
                    var currentUser = ListOfUsers.getUser(ctx);
                    if (currentUser != null) {
                        long directorySize = ListOfUsers.getUsedMemory(currentUser);
                        String response;
                        if (directorySize < 1024*10) {
                            response = String.format("Использовано: %.2f Кб из 500 Мб", (double) directorySize/1024);
                        } else {
                            response = String.format("Использовано: %.2f Mб из 500 Мб", (double) directorySize/(1024*1024));
                        }

                        ctx.writeAndFlush(new ServerResponse(Arrays.asList(response)));
                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

                // Регистрация нового пользователя
                case "-registr":
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Регистрация прошла успешно. " +
                            "Ваш ник: " + registration(msg).getNickname())));
                    break;

                case "-newnick":
                    if (ListOfUsers.getUser(ctx)!=null) {
                        // Смена рандомного ника на свой собственный
                        // Не забыть проверить ник на уникальность
                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

                case "-newpass":
                    if (ListOfUsers.getUser(ctx)!=null) {
                    // Сменить пароль
                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

                // Завершение работы
                case "-exit":
                    ListOfUsers.removeUserFromList(ctx); // удаляем пользователя из списка подключенных контактов
                    ctx.close();
                    break;

                default:
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Неправильная команда. Вызов справки: -help")));
                    break;
            }
    }

    // Регистрация нового пользователя
    private static User registration(ServiceCommand msg) {
        String [] str_arr = msg.getData();
        var login = str_arr[1];
        var password = str_arr[2];
        User user = authService.createNewUser(login, password);
        new File(user.getFolderPath()).mkdirs();
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
