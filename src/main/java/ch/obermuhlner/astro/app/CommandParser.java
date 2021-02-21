package ch.obermuhlner.astro.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommandParser {

  private final Map<Command, CommandHandler> commands = new HashMap<>();

  public void add(Command command, CommandHandler handler) {
    commands.put(command, handler);
  }

  public void parse(String[] args) {
    int index = 0;

    while (index < args.length) {
      Command command = findCommand(args[index]);
      if (command == null) {
        throw new RuntimeException("Unknown command: " + args[index]);
      }
      index++;

      List<NamedArguments> optionsWithArguments = new ArrayList<>();
      List<String> commandArguments = new ArrayList<>();

      String optionName = getOptionName(args, index);
      while (optionName != null) {
        index++;

        Option option = command.findOption(optionName);
        if (option == null) {
          throw new RuntimeException("Unknown option for command " + command + ": " + optionName);
        }
        List<String> optionArguments = new ArrayList<>();
        while (optionArguments.size() < option.argumentCount) {
          optionArguments.add(args[index]);
          index++;
        }

        optionsWithArguments.add(new NamedArguments(optionName, optionArguments));

        optionName = getOptionName(args, index);
      }

      while (commandArguments.size() < command.argumentCount) {
        commandArguments.add(args[index]);
        index++;
      }

      CommandHandler commandHandler = commands.get(command);
      commandHandler.handle(new NamedArguments(command.name, commandArguments), new OptionsWithArguments(optionsWithArguments));
    }
  }

  public void foo() {

  }

  private String getOptionName(String[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    String arg = args[index];
    for (String prefix : Arrays.asList("--", "-")) {
      if (arg.startsWith(prefix)) {
        return arg.substring(prefix.length());
      }
    }

    return null;
  }

  private Command findCommand(String arg) {
    for (Command command : commands.keySet()) {
      if (command.name.equals(arg)) {
        return command;
      }
    }

    return null;
  }

  public static class Command {

    private final String name;
    private final int argumentCount;
    private final Option[] options;

    public Command(String name, int argumentCount, Option... options) {
      this.name = name;
      this.argumentCount = argumentCount;
      this.options = options;
    }

    public Option findOption(String optionName) {
      for (Option option : options) {
        if (option.name.equals(optionName)) {
          return option;
        }
      }
      return null;
    }
  }

  public static class Option {

    private final String name;
    private final int argumentCount;

    public Option(String name, int argumentCount) {
      this.name = name;
      this.argumentCount = argumentCount;
    }
  }

  public static class NamedArguments {
    public final String name;
    public final List<String> arguments;

    public NamedArguments(String name, List<String> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Override
    public String toString() {
      return name + " : " + arguments;
    }
  }

  public static class OptionsWithArguments {

    private final List<NamedArguments> optionsWithArguments;

    OptionsWithArguments(List<NamedArguments> optionsWithArguments) {
      this.optionsWithArguments = optionsWithArguments;
    }

    public List<String> getOptionArguments(String optionName, String... defaultArguments) {
      for (NamedArguments optionWithArguments : optionsWithArguments) {
        if (optionWithArguments.name.equals(optionName)) {
          return optionWithArguments.arguments;
        }
      }
      return Arrays.asList(defaultArguments);
    }

    public int handleOption(String optionName, Consumer<List<String>> handleOptionArguments) {
      int count = 0;
      for (NamedArguments optionWithArguments : optionsWithArguments) {
        if (optionWithArguments.name.equals(optionName)) {
          handleOptionArguments.accept(optionWithArguments.arguments);
          count++;
        }
      }
      return count;
    }
  }

  public interface CommandHandler {
    void handle(NamedArguments commandArguments, OptionsWithArguments optionsWithArguments);
  }
}
