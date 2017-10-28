import java.util.{List => JList}
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.Completers.TreeCompleter.node
import org.jline.reader.impl.completer.{AggregateCompleter, ArgumentCompleter, StringsCompleter}
import org.jline.reader.{Candidate, LineReader, ParsedLine}

trait CommandCompleter extends SampleTreeCompleter {
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

trait SampleTreeCompleter {
  val nodes: List[TreeCompleter.Node] = List(
    node(
      "account",
      node("import", node("<keyFile>")),
      node("list"),
      node("new"),
      node("update", node("<accountAddress>"))
    ),
    node("bindkey"),
    node("console"),
    node("help"),
    node("password"),
    node("set", node("name"), node("<newValue>")),
    node("testkey", node("<key>")),
    node("tput", node("bell"))
  )
  val treeCompleter: TreeCompleter = new TreeCompleter(nodes: _*)
}
