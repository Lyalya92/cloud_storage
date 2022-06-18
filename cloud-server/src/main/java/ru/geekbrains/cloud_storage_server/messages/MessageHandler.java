package ru.geekbrains.cloud_storage_server.messages;


import io.netty.channel.ChannelHandlerContext;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;
import ru.geekbrains.cloud_storage_server.authorization.AuthService;
import ru.geekbrains.cloud_storage_server.database.DatabaseService;
import ru.geekbrains.cloud_storage_server.entity.ListOfUsers;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class MessageHandler {

    private static AuthService authService;
    private static DatabaseService databaseService = DatabaseService.getInstance();

    public MessageHandler(AuthService auth) {
        authService = auth;
    }

    // Обработка служебных команд
    public static void handleServiceCommand(ChannelHandlerContext ctx, ServiceCommand msg) throws Exception {
        User user;
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
                    user = authorize(msg);
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
                    user = ListOfUsers.getUser(ctx);
                    if (user != null) {
                        long directorySize = ListOfUsers.getUsedMemory(user);
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
                    user = ListOfUsers.getUser(ctx);
                    if (user != null) {
                        if(databaseService.isNicknameBusy(msg.getData()[1])) {
                            ctx.writeAndFlush(new ServerResponse(Arrays.asList("Этот ник уже занят!")));
                        } else {
                            if (databaseService.changeNickname(user, msg.getData()[1], msg.getData()[2])) {
                                ctx.writeAndFlush(new ServerResponse(Arrays.asList("Пароль успешно изменен")));
                            } else {
                                ctx.writeAndFlush(new ServerResponse(Arrays.asList("Ошибка при смене пароля")));
                            }
                        }
                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

                case "-newpass":
                    user = ListOfUsers.getUser(ctx);
                    if (user != null) {
                        if (databaseService.changePassword(user, msg.getData()[1], msg.getData()[2])) {
                            ctx.writeAndFlush(new ServerResponse(Arrays.asList("Пароль успешно изменен")));
                        } else {
                            ctx.writeAndFlush(new ServerResponse(Arrays.asList("Ошибка при смене пароля")));
                        }
                    } else {
                        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Необходимо сперва авторизоваться!")));
                    }
                    break;

            case "-download":
                File file = new File(ListOfUsers.getUser(ctx).getFolderPath() + msg.getData()[1]);
                if (file.exists()) {
                    sendFileToClient(ctx, file);
                } else {
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл не найден")));
                }
                break;

            case "-send":
                File file1 = new File(ListOfUsers.getUser(ctx).getFolderPath() + msg.getData()[1]);
                File dest_file = new File(databaseService.getFolderPathByNickname(msg.getData()[2]) + msg.getData()[1]);
                Files.copy(file1.toPath(), dest_file.toPath());
                ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл отправлен пользовтелю " + msg.getData()[2])));
                break;

            // Удалить файл на сервере
            case "-delete":
                File f = new File(ListOfUsers.getUser(ctx).getFolderPath() + msg.getData()[1]);
                if (f.delete()) {
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл " + f.getName() + " удален")));
                } else {
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл " + f.getName() + " не обнаружен")));
                }
                break;

            // Переименовать файл на сервере
            case "-rename":
                File oldFile = new File(ListOfUsers.getUser(ctx).getFolderPath() + msg.getData()[1]);
                File newFile = new File(ListOfUsers.getUser(ctx).getFolderPath() + msg.getData()[2]);
                if(oldFile.renameTo(newFile)) {
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл успешно переименован")));
                } else {
                    ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл не был переименован")));
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

    // Отправка содержимого файла клиенту
    private static void sendFileToClient(ChannelHandlerContext ctx, File file) {
        String filename = file.getName();
        int fileSize = (int) file.length();
        try(FileInputStream fis = new FileInputStream(file)) {
            byte [] buffer = new byte[fileSize];
            fis.read(buffer);
            ctx.writeAndFlush(new TransportedFile(filename, buffer, fileSize));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
