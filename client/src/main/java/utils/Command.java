package utils;

import java.io.Serializable;

public class Command implements Serializable {

    public enum CommandType {
        AUTH("/auth %s %s"),                    // запрос авторизации
        REGISTER("/reg %s %s"),                 // запрос регистрации

        LIST("/ls"),                            // список фалой в каталоге

        AUTH_OK("/authOK %s"),                  // удачная авторизация

        ERROR("/error %s");                     // ошибка

        private String commandName;

        public String getCommandName() {
            return commandName;
        }

        CommandType(String commandName) {
            this.commandName = commandName;
        }
    }

    private CommandType type;
    private String[] args;

    public static Command generate(CommandType type, String... args) {
        return new Command(type, args);
    }

    private Command(CommandType type, String... args) {
        this.type = type;
        this.args = args;
    }

    public CommandType getType() {
        return type;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return String.format(type.commandName, args);
    }
}
