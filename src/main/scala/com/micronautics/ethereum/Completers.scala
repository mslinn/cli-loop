package com.micronautics.ethereum

import java.util.{List => JList}
import org.jline.builtins.Completers.TreeCompleter.{Node, node}

trait Completers {
  protected[ethereum] val nodes: List[Node] = List(
    node(
      "account",
      node("import", node("<keyFile>")),
      node("list"),
      node("new"),
      node("update", node("<accountAddress>"))
    ),
    node("bindkey"),
    node("javascript"),
    node("help"),
    node("password"),
    node("set", node("name"), node("<newValue>")),
    node("testkey", node("<key>")),
    node("tput", node("bell"))
  )
}
