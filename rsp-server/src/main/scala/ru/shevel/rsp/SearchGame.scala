package ru.shevel.rsp

import cats.effect.IO
import cats.syntax.all.*

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}

object SearchGame {
  private val queue: ListBuffer[Player] = ListBuffer.empty
  private val players: mutable.Map[Int, Player] = mutable.Map.empty

  
}


