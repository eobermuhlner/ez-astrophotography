package ch.obermuhlner.astro.app

import java.util.*
import java.util.function.Consumer

class CommandParser {
    private val commands: MutableMap<Command, CommandHandler> = HashMap()
    fun add(command: Command, handler: CommandHandler) {
        commands[command] = handler
    }

    fun parse(args: Array<String>) {
        var index = 0
        while (index < args.size) {
            val command = findCommand(args[index]) ?: throw RuntimeException("Unknown command: " + args[index])
            index++
            val optionsWithArguments: MutableList<NamedArguments> = ArrayList()
            val commandArguments: MutableList<String?> = ArrayList()
            var optionName = getOptionName(args, index)
            while (optionName != null) {
                index++
                val option = command.findOption(optionName)
                        ?: throw RuntimeException("Unknown option for command $command: $optionName")
                val optionArguments: MutableList<String?> = ArrayList()
                while (optionArguments.size < option.argumentCount) {
                    optionArguments.add(args[index])
                    index++
                }
                optionsWithArguments.add(NamedArguments(optionName, optionArguments))
                optionName = getOptionName(args, index)
            }
            while (commandArguments.size < command.argumentCount) {
                commandArguments.add(args[index])
                index++
            }
            val commandHandler = commands[command]
            commandHandler!!.handle(NamedArguments(command.name, commandArguments), OptionsWithArguments(optionsWithArguments))
        }
    }

    fun foo() {}
    private fun getOptionName(args: Array<String>, index: Int): String? {
        if (args.size <= index) {
            return null
        }
        val arg = args[index]
        for (prefix in Arrays.asList("--", "-")) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length)
            }
        }
        return null
    }

    private fun findCommand(arg: String): Command? {
        for (command in commands.keys) {
            if (command.name == arg) {
                return command
            }
        }
        return null
    }

    class Command(val name: String, val argumentCount: Int, vararg opts: Option) {
        private val options: Array<Option> = opts as Array<Option>
        fun findOption(optionName: String): Option? {
            for (option in options) {
                if (option.name == optionName) {
                    return option
                }
            }
            return null
        }
    }

    class Option(val name: String, val argumentCount: Int)
    class NamedArguments(val name: String, val arguments: List<String?>) {
        override fun toString(): String {
            return "$name : $arguments"
        }
    }

    class OptionsWithArguments internal constructor(private val optionsWithArguments: List<NamedArguments>) {
        fun getOptionArguments(optionName: String, vararg defaultArguments: String?): List<String?> {
            for (optionWithArguments in optionsWithArguments) {
                if (optionWithArguments.name == optionName) {
                    return optionWithArguments.arguments
                }
            }
            return Arrays.asList(*defaultArguments)
        }

        fun handleOption(optionName: String, handleOptionArguments: Consumer<List<String?>>): Int {
            var count = 0
            for (optionWithArguments in optionsWithArguments) {
                if (optionWithArguments.name == optionName) {
                    handleOptionArguments.accept(optionWithArguments.arguments)
                    count++
                }
            }
            return count
        }
    }

    interface CommandHandler {
        fun handle(commandArguments: NamedArguments?, optionsWithArguments: OptionsWithArguments?)
    }
}