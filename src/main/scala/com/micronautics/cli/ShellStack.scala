package com.micronautics.cli

import scala.collection.mutable

object ShellStack {
  def empty: ShellStack = new ShellStack
}

class ShellStack {
  protected[cli] lazy val stack: mutable.Buffer[ShellLike] = mutable.Buffer.empty

  def isEmpty: Boolean = stack.isEmpty

  def nonEmpty: Boolean = stack.nonEmpty

  def pop(shell: ShellLike): (ShellLike, ShellStack) = {
    val result = stack.head
    stack.remove(0)
    (result, this)
  }

  def push(shell: ShellLike): ShellStack = {
    stack.insert(0, shell)
    this
  }

  def top: ShellLike = stack.head
}
