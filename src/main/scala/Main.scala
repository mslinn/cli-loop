import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField.{HOUR_OF_DAY, MINUTE_OF_HOUR}
import java.time.{LocalDate, LocalTime}
import java.util.{HashMap => JHashMap, List => JList}
import org.jline.builtins.Completers.TreeCompleter.node
import org.jline.builtins.Completers.{FileNameCompleter, RegexCompleter, TreeCompleter}
import org.jline.reader.impl.completer.{AggregateCompleter, ArgumentCompleter, StringsCompleter}
import org.jline.reader.{Candidate, Completer, LineReader, ParsedLine}
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

trait ComplexStringCompleter {
  val prompt: String = new AttributedStringBuilder()
    .style(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN))
    .append("foo")
    .style(AttributedStyle.DEFAULT)
    .append("@bar")
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
    .append(" baz")
    .style(AttributedStyle.DEFAULT)
    .append("> ")
    .toAnsi

  val rightPrompt: String = new AttributedStringBuilder()
    .style(AttributedStyle.DEFAULT.background(AttributedStyle.RED))
    .append(LocalDate.now.format(DateTimeFormatter.ISO_DATE))
    .append("\n")
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED | AttributedStyle.BRIGHT))
    .append(
      LocalTime.now
        .format(
          new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .toFormatter()
        )
    ).toAnsi

  val complexStringsCompleter: StringsCompleter =
    new StringsCompleter("\u001B[1mfoo\u001B[0m", "bar", "\u001B[32mbaz\u001B[0m", "foobar")
}

trait CustomCompleter {
  val customCompleter: Completer = (reader: LineReader, line: ParsedLine, candidates: JList[Candidate]) => {
    if (line.wordIndex == 0) {
      candidates.add(new Candidate("custom"))
      ()
    } else if (line.words.get(0) == "custom")
      if (line.words.get(line.wordIndex - 1) == "Option1") {
        candidates.add(new Candidate("Param1"))
        candidates.add(new Candidate("Param2"))
        ()
      } else {
        if (line.wordIndex == 1) candidates.add(new Candidate("Option1"))
        if (!line.words.contains("Option2")) candidates.add(new Candidate("Option2"))
        if (!line.words.contains("Option3")) candidates.add(new Candidate("Option3"))
        ()
      }
  }
}

trait MiscCompleters {
  val fileNameCompleter: FileNameCompleter = new FileNameCompleter

  val commandStringsCompleter: StringsCompleter =
    new StringsCompleter("bindkey", "cls", "custom", "help", "set", "sleep", "testkey", "tput")
}

trait CommandCompleter extends MiscCompleters with SampleTreeCompleter {
  import org.jline.reader.impl.completer.NullCompleter
  val aggregateCompleter = new AggregateCompleter(
    new ArgumentCompleter(NullCompleter.INSTANCE, treeCompleter)
  )
}

trait SampleArgumentCompleter {
  val argumentCompleter: ArgumentCompleter = new ArgumentCompleter(
    new StringsCompleter("foo11", "foo12", "foo13"),
    new StringsCompleter("foo21", "foo22", "foo23"),
    (reader: LineReader, line: ParsedLine, candidates: JList[Candidate]) => {
      candidates.add(new Candidate("", "", null, "frequency in MHz", null, null, false))
      ()
    })
}

trait SampleRegexCompleter {
  val hashMap: JHashMap[String, Completer] = new JHashMap
  hashMap.put("C1",  new StringsCompleter("cmd1"))
  hashMap.put("C11", new StringsCompleter("--opt11", "--opt12"))
  hashMap.put("C12", new StringsCompleter("arg11", "arg12", "arg13"))
  hashMap.put("C2",  new StringsCompleter("cmd2"))
  hashMap.put("C21", new StringsCompleter("--opt21", "--opt22"))
  hashMap.put("C22", new StringsCompleter("arg21", "arg22", "arg23"))
  val regexCompleter: RegexCompleter = new RegexCompleter("C1 C11* C12+ | C2 C21* C22+", (x: String) => hashMap.get(x))
}

trait SampleTreeCompleter {
  val nodes: List[TreeCompleter.Node] = List(node("bindkey"),
    node("cls"),
    node(
      "custom",
      node("option1", node("param1", "param2")),
      node("option2"),
      node("option3")
    ),
    node("help"),
    node("password"),
    node("set", node("newValue")),
    node("testkey"),
    node("tput", node("bell"))
  )
  val treeCompleter: TreeCompleter = new TreeCompleter(nodes: _*)
}
